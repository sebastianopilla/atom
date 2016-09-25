package atom;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import atom.exception.AtomException;

/**
 * Main class for IOC implementation
 */
public class Atom {

  // keys for mandatory properties
  private static final String CLASS_PROPERTY = "$class";
  private static final String SCOPE_PROPERTY = "$scope";

  // possible scopes
  public static final String GLOBAL_SCOPE = "global";
  public static final String SESSION_SCOPE = "session";
  public static final String REQUEST_SCOPE = "request";
  public static final String ATOM_GLOBAL_SCOPE = "atom.global";
  public static final String ATOM_SESSION_SCOPE = "atom.session";
  public static final String ATOM_REQUEST_SCOPE = "atom.request";

  // singleton (one Atom per classloader)
  private static Atom mInstance = new Atom();

  // list of configuration directories
  private List<File> mConfigPath;

  // applies properties to objects
  private BeanConfigurator mConfigurator;

  // scopes
  private Scope mGlobalScope;


  /**
   * Private constructor
   */
  private Atom () {
    mConfigPath = new ArrayList<File>();
    mGlobalScope = new Scope(GLOBAL_SCOPE);
    PropertyEditors.registerEditors();
    mConfigurator = new BeanConfigurator();
  }


  /**
   * Returns a reference to the Atom
   * @return Atom
   */
  public static Atom getAtom () {
    return mInstance;
  }


  /**
   * Given a name, returns a configured component, creating it if necessary
   * @param pName full component name
   * @return component, or null if not found
   */
  public Object resolveName (String pName) {
    if (null == pName || "".equals(pName)) {
      throw new AtomException("Parameter pName is null or empty");
    }
    if (!pName.startsWith("/")) {
      throw new AtomException("Relative component names aren't supported");
    }

    // find name in global scope
    // if name not found, try to create the component
    Object comp = mGlobalScope.resolveName(pName);
    if (comp != null) {
      return comp;
    } else {
      return createComponent(pName, mGlobalScope);
    }
  } // end resolveName


  /**
   * Creates a configured instance of the given component
   * @param pName full component name
   * @param pScope the scope the desired component will belong to
   * @return component instance, or null if it could not be created
   */
  public Object createComponent (String pName, Scope pScope) {
    // find all its .properties files in the configpath
    List<Properties> props = findConfiguration(pName);
    if (props == null || props.size() == 0) {
      throw new AtomException("There are no configuration files for component " + pName);
    }

    // merge the properties according to the layer ordering
    Properties config = mergeConfigurationLayers(props);
    if (config == null) {
      throw new AtomException("Could not merge configurations for component " + pName);
    }

    // validate the (merged) configuration
    if (!isConfigurationValid(config)) {
      throw new AtomException("Could not validate merged configuration for component " + pName);
    } else {
      // load the class
      Object component = createComponentInstance(config);

      // apply the configuration
      mConfigurator.configure(component, config, pScope);

      // register the component in this scope or in its own scope
      Scope sc = pScope;
      while (!sc.getName().equals(getScopeNameFromConfiguration(config))) {
        sc = sc.getParentScope();
      }
      sc.register(pName, component);

      // initialize the component if it wants to
      if (component instanceof Startable) {
        ((Startable)component).start();
      }

      return component;
    }
  } // end createComponent


  /**
   * Creates an instance of the given component
   * @param pConfig component configuration
   * @return component instance, or null if errors
   */
  private Object createComponentInstance (Properties pConfig) {
    if (pConfig == null || pConfig.isEmpty()) {
      throw new AtomException("Configuration is invalid (null or empty)");
    }

    // assumes that the configuration has been validated already
    String className = pConfig.getProperty(CLASS_PROPERTY);

    Class clazz = null;
    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException cnfe) {
      throw new AtomException("Could not load class " + className, cnfe);
    }

    if (clazz == null) {
      return null;
    }

    Object obj = null;
    try {
      obj = clazz.newInstance();
    } catch (InstantiationException ie) {
      throw new AtomException("Error instantiating class " + className, ie);
    } catch (IllegalAccessException iae) {
      throw new AtomException("Access denied instantiating class " + className, iae);
    }

    return obj;
  } // end createComponentInstance


  /**
   * Validates a configuration by looking at the $class and $scope properties.
   * Does not attempt to load the class
   * @param pConfig configuration
   * @return true if the configuration is valid, false otherwise
   */
  private boolean isConfigurationValid (Properties pConfig) {
    if (pConfig == null || pConfig.isEmpty()) {
      throw new AtomException("configuration is invalid (null or empty)");
    }

    // the 2 mandatory properties $class and $scope must be present
    if (pConfig.getProperty(CLASS_PROPERTY) == null) {
      throw new AtomException("Configuration is invalid - missing $class property");
    }
    if (pConfig.getProperty(SCOPE_PROPERTY) == null) {
     throw new AtomException("Configuration is invalid - missing $scope property");
    }

    // do not verify if the class is loadable right now, it will be done later
    // verify however that the scope is something we understand
    String scope = pConfig.getProperty(SCOPE_PROPERTY);
    return GLOBAL_SCOPE.equals(scope) || SESSION_SCOPE.equals(scope) || REQUEST_SCOPE.equals(scope);
  } // end isConfigurationValid


  /**
   * Scans the config path (all layers) and finds all the properties files that configure the given component 
   * @param pComponentName full component name
   * @return list of properties, null or empty if no files found
   */
  @SuppressWarnings("unchecked")
  private List<Properties> findConfiguration (String pComponentName) {
    if (null == pComponentName || "".equals(pComponentName)) {
      throw new AtomException("Parameter pComponentName is null or empty");
    }
    Iterator it = mConfigPath.iterator();
    ArrayList propFiles = new ArrayList();
    PropertiesFileFinder finder = new PropertiesFileFinder();
    while (it.hasNext()) {
      Properties props = finder.findProperties((File)it.next(), pComponentName);
      if (props != null) {
        propFiles.add(props);
      }
    }
    return propFiles;
  } // end findConfiguration


  /**
   * Merge a list of configurations into one, the last ones overwrite the previous
   * @param pList list of properties
   * @return merged property, or null if errors
   */
  private Properties mergeConfigurationLayers (List<Properties> pList) {
    if (pList == null || pList.isEmpty()) {
      throw new AtomException("Properties list is null or empty");
    }

    Properties result = new Properties();
    for (Properties p : pList) {
      Enumeration names = p.propertyNames();
      while (names.hasMoreElements()) {
        String name = (String) names.nextElement();
        String value = p.getProperty(name);

        // treat null values by removing the key, as later layers may want to erase previously configured values
        if (value == null) {
          if (result.containsKey(name)) {
            result.remove(name);
          }
        } else {
          result.setProperty(name, value);
        }
      }
    }

    // do a second pass to handle "+=" and "-=" properties
    Set<String> propNames = result.stringPropertyNames();
    Set<String> toRemove = new HashSet<String>();
    for (String propName : propNames) {
      if (propName.endsWith("+") || propName.endsWith("-")) {
        String val = result.getProperty(propName);
        String baseName = propName.substring(0, propName.length() - 1);
        String baseValue = result.getProperty(baseName);
        if (baseValue == null || "".equals(baseValue)) {
          result.setProperty(baseName, val);
        } else {
          if (propName.endsWith("+")) {
            // handle "+=" by simply appending the desired property value, separating with a comma
            result.setProperty(baseName, baseValue + "," + val);
          } else if (propName.endsWith("-")) {
            // handle "-=" by splitting the current value into an array of strings, searching for the value
            // to remove and adding everything else to a temporary list which is then joined again into a comma-separated string
            String[] comps = baseValue.split(",");
            if (comps.length > 0) {
              StringBuilder baseValueList = new StringBuilder();
              for (String comp : comps) {
                if (!val.equals(comp)) {
                  baseValueList.append(comp).append(",");
                }
              }
              if (baseValueList.charAt(baseValueList.length() - 1) == ',') {
                baseValueList.deleteCharAt(baseValueList.length() - 1);
              }
              result.setProperty(baseName, baseValueList.toString());
            }
          }
        }
        toRemove.add(propName);
      }
    }
    for (String rem : toRemove) {
      result.remove(rem);
    }

    return result;
  } // end mergeConfigurationLayers


  /**
   * Returns true if a component with the given name exists in the global scope
   * @param pName full component name
   * @return true or false
   */
  public boolean isGlobalComponent (String pName) {
    if (pName == null || "".equals(pName)) {
      return false;
    }
    return (null != mGlobalScope.resolveName(pName, false));
  }


  /**
   * Appends the directory to the config path, if the config path doesn't have it already
   * @param pDirectory directory with component configuration files
   */
  public void addToConfigPath (File pDirectory) {
    if (pDirectory != null) {
      if (pDirectory.exists() && pDirectory.isDirectory() && pDirectory.canRead()) {
        if (!mConfigPath.contains(pDirectory)) {
          mConfigPath.add(pDirectory);
        }
      }
    }
  }


  /**
   * Takes a path relative to the configpath and returns the corresponding physical path. If there are multiple files with such
   * a name in the configpath, returns the last one it finds
   * @param pPath path to a file, relative to the configpath
   * @return absolute path to the file, null if errors
   */
  public String convertConfigPathToPhysicalPath (String pPath) {
    if (pPath == null || "".equals(pPath)) {
      throw new AtomException("Parameter pPath is null or empty");
    }

    String result = null;
    for (File dir : mConfigPath) {
      if (dir == null || !dir.exists() || !dir.isDirectory() || !dir.canRead()) {
        throw new AtomException("Cannot access path " + dir + " for searching");
      }

      // get a os-specific path and look for a corresponding file
      String[] comps = pPath.split("/");
      if (comps.length == 0) {
        return null;
      }
      StringBuilder buf = new StringBuilder(dir.getAbsolutePath());
      for (int i = 0; i < comps.length; i++) {
        buf.append(comps[i]);
        if (i != comps.length - 1) {
          buf.append(File.separator);
        }
      }

      // believe it or not, this converts a _single_ backslash into a regular slash;
      result = buf.toString().replaceAll("\\\\", "/");

      // silly workaround to use the file:// protocol on Windows paths
      if (!result.startsWith("/")) {
        result = "/" + result;
      }
    }
    return result;
  } // end convertConfigPathToPhysicalPath


  /**
   * Takes a path relative to the configpath and returns the corresponding physical paths.
   * Each element of the returned array is an existing readable physical file corresponding to the given config path
   * @param pPath path to a file, relative to the configpath
   * @return array of absolute paths to the files, null if errors
   */
  public String[] convertConfigPathToPhysicalPaths (String pPath) {
    if (pPath == null || "".equals(pPath)) {
      throw new AtomException("Parameter pPath is null or empty");
    }

    List<String> paths = new ArrayList<String>();
    for (File dir : mConfigPath) {
      if (dir == null || !dir.exists() || !dir.isDirectory() || !dir.canRead()) {
        throw new AtomException("Cannot access path " + dir + " for searching");
      }

      // get a os-specific path and look for a corresponding file
      String[] comps = pPath.split("/");
      if (comps.length == 0) {
        return null;
      }
      StringBuilder buf = new StringBuilder(dir.getAbsolutePath());
      for (int i = 0; i < comps.length; i++) {
        buf.append(comps[i]);
        if (i != comps.length - 1) {
          buf.append(File.separator);
        }
      }

      // believe it or not, this converts a _single_ backslash into a regular slash;
      String path = buf.toString().replaceAll("\\\\", "/");

      // silly workaround to use the file:// protocol on Windows paths
      if (!path.startsWith("/")) {
        path = "/" + path;
      }

      // if this file exists and is readable, add it to the list
      File candidate = new File(path);
      if (candidate.exists() && candidate.canRead() && candidate.isFile()) {
        paths.add(path);
      }
    }

    String[] dummystringarr = {};
    return paths.toArray(dummystringarr);
  } // end convertConfigPathToPhysicalPath


  /**
   * Reads a file in the configpath and returns its content as a string. If there are multiple files with such
   * a name in the configpath, returns the last one it finds
   * @param pPath path to a file, relative to the configpath
   * @return file content as string, null if errors
   */
  public String readConfigurationFile (String pPath) {
    if (pPath == null || "".equals(pPath)) {
      throw new AtomException("Parameter pPath is null or empty");
    }

    String result = null;
    String fullPath = convertConfigPathToPhysicalPath(pPath);
    if (fullPath == null || "".equals(fullPath)) {
      return null;
    }
    File candidate = new File(fullPath);
    if (candidate.exists() && candidate.isFile() && candidate.canRead()) {
      FileReader reader = null;
      StringBuilder fbuf = new StringBuilder();
      try {
        reader = new FileReader(candidate);
        int cnt = 0;
        char[] cbuf = new char[4096];
        while ((cnt = reader.read(cbuf)) != -1) {
          fbuf.append(cbuf, 0, cnt);
        }
      } catch (IOException ioe) {
        throw new AtomException("Got IOException reading from file " + candidate, ioe);
      } finally {
        try {
          if (reader != null) {
            reader.close();
          }
        } catch (IOException ioe) {
          throw new AtomException("Got IOException closing file " + candidate, ioe);
        }
      }
      result = fbuf.toString();
    }
    return result;
  } // end readConfigurationFile


  /**
   * Merges into a string all the files in the configpath that match the given partial path
   * @param pPath path to a file, relative to the configpath
   * @return file content as string, null if errors
   */
  public String readMergedConfigurationFile (String pPath) {
    if (pPath == null || "".equals(pPath)) {
      throw new AtomException("Parameter pPath is null or empty");
    }

    StringBuilder content = new StringBuilder();
    for (File dir : mConfigPath) {
      String filePath = null;
      if (dir == null || !dir.exists() || !dir.isDirectory() || !dir.canRead()) {
        throw new AtomException("Cannot access path " + dir + " for searching");
      }

      // get a os-specific path and look for a corresponding file
      String[] comps = pPath.split("/");
      if (comps.length == 0) {
        return null;
      }
      StringBuilder buf = new StringBuilder(dir.getAbsolutePath());
      for (int i = 0; i < comps.length; i++) {
        buf.append(comps[i]);
        if (i != comps.length - 1) {
          buf.append(File.separator);
        }
      }

      // believe it or not, this converts a _single_ backslash into a regular slash;
      filePath = buf.toString().replaceAll("\\\\", "/");

      // silly workaround to use the file:// protocol on Windows paths
      if (!filePath.startsWith("/")) {
        filePath = "/" + filePath;
      }

      File candidate = new File(filePath);
      if (candidate.exists() && candidate.isFile() && candidate.canRead()) {
        FileReader reader = null;
        try {
          reader = new FileReader(candidate);
          int cnt = 0;
          char[] cbuf = new char[4096];
          while ((cnt = reader.read(cbuf)) != -1) {
            content.append(cbuf, 0, cnt);
          }
        } catch (IOException ioe) {
          throw new AtomException("Got IOException reading from file " + candidate);
        } finally {
          try {
            if (reader != null) {
              reader.close();
            }
          } catch (IOException ioe) {
            throw new AtomException("Got IOException closing file " + candidate);
          }
        }
      }
    }
    return content.toString();
  } // end readMergedConfigurationFile


  /**
   * Retrieves the scope of this component from its configuration
   * @param pConfig configuration
   * @return scope name, or empty if errors
   */
  private String getScopeNameFromConfiguration (Properties pConfig) {
    if (pConfig == null || pConfig.isEmpty()) {
      return "";
    }
    return pConfig.getProperty(SCOPE_PROPERTY);
  }


  /**
   * Retrieves the current config path
   * @return config path
   */
  public List getConfigPath () {
    return mConfigPath;
  }


  /**
   * Retrieves the global scope so other scopes can reference it as parent
   * @return global scope
   */
  public Scope getGlobalScope () {
    return mGlobalScope;
  }

} // end Atom

