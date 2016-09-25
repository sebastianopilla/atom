package atom;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Contains and registers the various JavaBean property editors
 */
public class PropertyEditors {

  /**
   * true if the editors have been registered
   */
  static boolean sRegisteredEditors = false;


  /**
   * Registers all of our editors with the JavaBean PropertyEditorManager
   */
  public static void registerEditors () {
    if (!sRegisteredEditors) {
      PropertyEditorManager.registerEditor(Short.TYPE, ShortPropertyEditor.class);
      PropertyEditorManager.registerEditor(Short.class, ShortPropertyEditor.class);
      PropertyEditorManager.registerEditor(Integer.TYPE, IntegerPropertyEditor.class);
      PropertyEditorManager.registerEditor(Integer.class, IntegerPropertyEditor.class);
      PropertyEditorManager.registerEditor(Long.TYPE, LongPropertyEditor.class);
      PropertyEditorManager.registerEditor(Long.class, LongPropertyEditor.class);
      PropertyEditorManager.registerEditor(Float.TYPE, FloatPropertyEditor.class);
      PropertyEditorManager.registerEditor(Float.class, FloatPropertyEditor.class);
      PropertyEditorManager.registerEditor(Double.TYPE, DoublePropertyEditor.class);
      PropertyEditorManager.registerEditor(Double.class, DoublePropertyEditor.class);
      PropertyEditorManager.registerEditor(Boolean.TYPE, BooleanPropertyEditor.class);
      PropertyEditorManager.registerEditor(Boolean.class, BooleanPropertyEditor.class);
      PropertyEditorManager.registerEditor(String.class, StringPropertyEditor.class);
      PropertyEditorManager.registerEditor(List.class, ListPropertyEditor.class);
      PropertyEditorManager.registerEditor(ArrayList.class, ListPropertyEditor.class);
      PropertyEditorManager.registerEditor(Set.class, SetPropertyEditor.class);
      PropertyEditorManager.registerEditor(HashSet.class, SetPropertyEditor.class);
      PropertyEditorManager.registerEditor(Map.class, MapPropertyEditor.class);
      PropertyEditorManager.registerEditor(HashMap.class, MapPropertyEditor.class);
      PropertyEditorManager.registerEditor(Properties.class, PropertiesPropertyEditor.class);
      PropertyEditorManager.registerEditor(Object.class, ObjectPropertyEditor.class);
      PropertyEditorManager.registerEditor(Date.class, DatePropertyEditor.class);
      PropertyEditorManager.registerEditor(File.class, FilePropertyEditor.class);
      sRegisteredEditors = true;
    }
  } // end registerEditors


  /**
   * Finds the property editor registered for the given class
   *
   * @param pClass class to find the editor for
   * @return property editor, or null if not found
   */
  public static PropertyEditor getPropertyEditor (Class pClass) {
    if (pClass == null) {
      return null;
    }
    return PropertyEditorManager.findEditor(pClass);
  }


  public static class NumericPropertyEditor extends PropertyEditorSupport {
    public String getJavaInitializationString () {
      return String.valueOf(getValue());
    }

    public String getAsText () {
      if (getValue() instanceof Number) {
        return getValue().toString();
      } else {
        return "";
      }
    }

    public NumericPropertyEditor () {
    }
  }


  public static class ShortPropertyEditor extends NumericPropertyEditor {
    public String getJavaInitializationString () {
      if (getValue() == null) {
        return "null";
      }
      return "((short)" + getValue() + ")";
    }

    public void setAsText (String pStr) throws IllegalArgumentException {
      if (pStr == null || pStr.trim().length() == 0) {
        setValue(null);
      } else {
        setValue(Short.valueOf(pStr.trim()));
      }
    }
  }


  public static class IntegerPropertyEditor extends NumericPropertyEditor {
    public void setAsText (String pStr) throws IllegalArgumentException {
      if (pStr == null || pStr.trim().length() == 0) {
        setValue(null);
      } else {
        setValue(Integer.valueOf(pStr.trim()));
      }
    }
  }


  public static class LongPropertyEditor extends NumericPropertyEditor {
    public String getJavaInitializationString () {
      if (getValue() == null) {
        return "null";
      }
      return String.valueOf(getValue()) + "L";
    }

    public void setAsText (String pStr) throws IllegalArgumentException {
      if (pStr == null || pStr.trim().length() == 0) {
        setValue(null);
      } else {
        setValue(Long.valueOf(pStr.trim()));
      }
    }
  }


  public static class FloatPropertyEditor extends NumericPropertyEditor {
    public String getJavaInitializationString () {
      if (getValue() == null) {
        return "null";
      }
      return String.valueOf(getValue()) + "F";
    }

    public void setAsText (String pStr) throws IllegalArgumentException {
      if (pStr == null || pStr.trim().length() == 0) {
        setValue(null);
      } else {
        setValue(Float.valueOf(pStr.trim()));
      }
    }
  }


  public static class DoublePropertyEditor extends NumericPropertyEditor {
    public void setAsText (String pStr) throws IllegalArgumentException {
      if (pStr == null || pStr.trim().length() == 0) {
        setValue(null);
      } else {
        setValue(Double.valueOf(pStr.trim()));
      }
    }
  }


  public static class BooleanPropertyEditor extends PropertyEditorSupport {
    static String[] BOOLEAN_EDITOR_TAGS = {"true", "false"};

    public String getJavaInitializationString () {
      if (((Boolean) getValue()).booleanValue()) {
        return "true";
      } else {
        return "false";
      }
    }

    public String getAsText () {
      return ((Boolean) getValue()).booleanValue() ? "true" : "false";
    }

    public void setAsText (String pValue) throws java.lang.IllegalArgumentException {
      setValue(pValue.trim().equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE);
    }

    public String[] getTags () {
      return BOOLEAN_EDITOR_TAGS;
    }
  }


  /**
   * Implements a property editor for strings
   */
  public static class StringPropertyEditor extends PropertyEditorSupport {

    public String getAsText () {
      return (String) getValue();
    }

    public void setAsText (String pValue) {
      setValue(pValue);
    }
  }


  /**
   * Implements a property editor for lists: strings separated by commas
   * (commas cannot be contained in list values)
   */
  public static class ListPropertyEditor extends PropertyEditorSupport {

    public String getAsText () {
      List list = (List) getValue();
      if (list == null) {
        return null;
      }
      Iterator iter = list.iterator();
      return getListOrSetAsText(iter);
    }

    @SuppressWarnings("unchecked")
    public void setAsText (String pValue) {
      if (pValue == null) {
        setValue(null);
        return;
      }
      if ("".equals(pValue)) {
        setValue(Collections.EMPTY_LIST);
        return;
      }
      List list = new ArrayList();
      String[] elems = pValue.split(",");
      if (elems.length > 0) {
        list.addAll(Arrays.asList(elems));
        setValue(list);
      } else {
        setValue(Collections.EMPTY_LIST);
      }
    }
  }


  /**
   * Implements a property editor for sets: strings separated by commas
   * (commas cannot be contained in set values)
   */
  public static class SetPropertyEditor extends PropertyEditorSupport {

    public String getAsText () {
      Set set = (Set) getValue();
      if (set == null) {
        return null;
      }
      Iterator iter = set.iterator();
      return getListOrSetAsText(iter);
    }

    @SuppressWarnings("unchecked")
    public void setAsText (String pValue) {
      if (pValue == null) {
        setValue(null);
        return;
      }
      if ("".equals(pValue)) {
        setValue(Collections.EMPTY_SET);
        return;
      }
      Set set = new HashSet();
      String[] elems = pValue.split(",");
      if (elems.length > 0) {
        set.addAll(Arrays.asList(elems));
        setValue(set);
      } else {
        setValue(Collections.EMPTY_SET);
      }
    }
  }


  /**
   * Utility method to build a string representation of a list or set from an iterator
   * @param pIter iterator for list or set elements
   * @return representation as string of the given iterator; non-string elements are skipped
   */
  private static String getListOrSetAsText (Iterator pIter) {
    StringBuilder buf = new StringBuilder();
    while (pIter.hasNext()) {
      Object elemValue = pIter.next();
      if (elemValue instanceof String) {
        buf.append(elemValue);
        if (pIter.hasNext()) {
          buf.append(",");
        }
      }
    }
    return buf.toString();
  }


  /** Utility method to set a map from an array of string pairs
   * @param pMap the map to set
   * @param pPairs the string pairs to set the map from
   */
  @SuppressWarnings("unchecked")
  private static void internalSetMapAsText (Map pMap, String[] pPairs) {
    for (String pPair : pPairs) {
      String[] splitPair = pPair.split("=");
      if (splitPair.length >= 1) {
        // allows the value to be null or empty
        String key = splitPair[0];
        String value = null;
        if (splitPair.length == 2) {
          value = splitPair[1];
        }
        pMap.put(key, value);
      }
    }
  }


  /**
   * Implements a property editor for maps, expressed as lists of key=value pairs where the tuples are separated by commas
   */
  public static class MapPropertyEditor extends PropertyEditorSupport {

    public String getAsText () {
      Map map = (Map) getValue();
      if (map == null) {
        return null;
      }
      StringBuilder buf = new StringBuilder();
      Iterator iter = map.keySet().iterator();
      while (iter.hasNext()) {
        Object key = iter.next();
        Object value = map.get(key);
        if (!(key instanceof String && value instanceof String)) {
          return null; // cannot be expressed as a string
        }
        value = ((String) value).replaceAll(",", ",,");
        buf.append(key).append("=").append(value);
        if (iter.hasNext()) {
          buf.append(",");
        }
      }
      return buf.toString();
    }

    @SuppressWarnings("unchecked")
    public void setAsText (String pValue) {
      if (pValue == null) {
        setValue(null);
        return;
      }
      if ("".equals(pValue.trim())) {
        setValue(Collections.EMPTY_MAP);
        return;
      }
      Map map = new HashMap();
      String[] pairs = pValue.split(",");
      if (pairs.length > 0) {
        internalSetMapAsText(map, pairs);
        setValue(map);
      } else {
        setValue(Collections.EMPTY_MAP);
      }
    }
  }


  /**
   * Implements a property editor for properties, expressed as lists of key=value pairs where the tuples are separated by commas
   */
  public static class PropertiesPropertyEditor extends PropertyEditorSupport {

    public String getAsText () {
      Properties props = (Properties) getValue();
      if (props == null) {
        return null;
      }
      StringBuilder buf = new StringBuilder();
      Enumeration e = props.keys();
      while (e.hasMoreElements()) {
        String key = (String) e.nextElement();
        String value = props.getProperty(key);
        if (value != null && !"".equals(value)) {
          value = value.replaceAll(",", ",,");
        }
        buf.append(key).append("=").append(value);
        if (e.hasMoreElements()) {
          buf.append(",");
        }
      }
      return buf.toString();
    }

    public void setAsText (String pValue) {
      if (pValue == null) {
        setValue(null);
        return;
      }
      if ("".equals(pValue)) {
        setValue(new Properties());
        return;
      }
      Properties props = new Properties();
      String[] pairs = pValue.split(",");
      if (pairs.length > 0) {
        internalSetMapAsText(props, pairs);
        setValue(props);
      } else {
        setValue(props);
      }
    }
  }


  /**
   * Implements a property editor for objects
   */
  public static class ObjectPropertyEditor extends PropertyEditorSupport {

    public String getAsText () {
      Object o = getValue();
      return (o == null ? null : o.toString());
    }

    public void setAsText (String pValue) {
      setValue(pValue);
    }
  }


  /**
   * Implements a property editor for dates in yyyy-MM-dd format or in RFC 3339 format (yyyy-MM-dd'T'HH:mm:ss)
   */
  public static class DatePropertyEditor extends PropertyEditorSupport {
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String RFC3339_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public String getAsText () {
      SimpleDateFormat fmt = new SimpleDateFormat(RFC3339_FORMAT);
      return fmt.format(getValue());
    }

    public void setAsText (String pValue) {
      if (pValue == null || "".equals(pValue.trim())) {
        setValue(null);
        return;
      }
      // put the most-specific formatters first, if they fail the less-specific will have a go
      SimpleDateFormat fmt1 = new SimpleDateFormat(RFC3339_FORMAT);
      SimpleDateFormat fmt2 = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
      SimpleDateFormat[] formatters = {fmt1, fmt2};
      for (SimpleDateFormat formatter : formatters) {
        Date val = null;
        try {
          val = formatter.parse(pValue);
          if (val != null) {
            setValue(val);
            break;
          }
        } catch (ParseException pe) {
          // exception is ignored deliberately as it is useless
        }
      }
    }
  }


  public static class FilePropertyEditor extends PropertyEditorSupport {

    public String getAsText () {
      File file = (File) getValue();
      if (file == null) {
        return "";
      }
      return file.getPath();
    }

    public void setAsText (String pValue) {
      if (pValue == null || "".equals(pValue)) {
        setValue(null);
        return;
      }

      // if there are braces in the value, substitute them with the value of the system property
      // they indicate: {atom.home} is substituted with the value of System.getProperty("atom.home");
      if (pValue.contains("{")) {
        int start;
        while ((start = pValue.indexOf("{")) >= 0) {
          int end = pValue.indexOf("}");
          if (end < 0) {
            throw new IllegalArgumentException(pValue);
          }
          String propertyValue = System.getProperty(pValue.substring(start + 1, end));
          if (propertyValue == null) {
            throw new IllegalArgumentException(pValue.substring(start + 1, end));
          }
          pValue = pValue.substring(0, start) + propertyValue + pValue.substring(end + 1);
        }
        try {
          setValue(new File(pValue).getCanonicalFile());
        } catch (IOException ioe) {
          throw new IllegalArgumentException(ioe.toString());
        }
      } else {
        // the file names may be expressed with forward slashes, which will be automatically converted
        // if the separator is different
        if (pValue.endsWith ("/")) {
          pValue = pValue.substring (0, pValue.length () - 1);
        }
        if (File.separatorChar != '/') {
          pValue = pValue.replace ('/', File.separatorChar);
        }
        File f = new File (pValue);
        setValue(f);
      }
    }
  }

} // end PropertyEditors
