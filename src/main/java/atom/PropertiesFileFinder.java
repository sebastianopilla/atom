package atom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import atom.exception.AtomException;

/**
 * Walks directory trees to find .properties files, returning the corresponding Properties object
 */
public class PropertiesFileFinder {


  /**
   * No-args constructor
   */
  public PropertiesFileFinder () {
    //
  }


  /**
   * Finds the properties for a component starting from the given root
   * @param pRoot root directory for starting the search
   * @param pComponentName absolute name of component
   * @return properties loaded for component, or null if errors
   */
  public Properties findProperties (File pRoot, String pComponentName) {
    if (pRoot == null || !pRoot.exists() || !pRoot.isDirectory() || !pRoot.canRead()) {
      throw new AtomException("Cannot access path " + pRoot + " for searching");
    }
    if (pComponentName == null || "".equals(pComponentName)) {
      throw new AtomException("Parameter pComponentName is null or empty");
    }

    // get a os-specific path and look for a corresponding .properties file
    String[] comps = pComponentName.split("/");
    if (comps.length == 0) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < comps.length; i++) {
      buf.append(comps[i]);
      if (i != comps.length - 1) {
        buf.append(File.separator);
      }
    }
    buf.append(".properties");
    File candidate = new File(pRoot, buf.toString());
    if (!candidate.exists() || !candidate.isFile() || !candidate.canRead()) {
      // layering is optional, if no properties files exists for a component it will never be created
      return null;
    }

    Properties props = new Properties();
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(candidate);
      props.load(fis);
    } catch (IOException ioe) {
      throw new AtomException("Got IOException reading properties file = " + candidate, ioe);
    } finally {
      if (fis != null) {
        // very important, close the file or we'll leak descriptors and under Windows we'll never be able to modify it while the app is running
        try {
          fis.close();
        } catch (IOException ioe) {
          throw new AtomException("Got IOException closing input stream on file = " + candidate, ioe);
        }
      }
    }
    return props;
  } // end findProperties

} // end PropertiesFileFinder

