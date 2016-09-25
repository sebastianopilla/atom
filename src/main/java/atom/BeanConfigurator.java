package atom;

import java.beans.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import atom.exception.ConfigurationException;
import atom.exception.AtomException;

/**
 * Configures JavaBeans from Properties objects
 */
public class BeanConfigurator {

  /**
   * No-args constructor
   */
  public BeanConfigurator () {
    //
  }


  /**
   * Configures an object with the given properties
   * @param pObject object to configure
   * @param pConfig properties to apply
   * @param pScope scope for naming resolution of dependencies
   */
  public void configure (Object pObject, Properties pConfig, Scope pScope) {
    if (pObject == null) {
      throw new AtomException("Cannot configure a null object");
    }
    if (pConfig == null) {
      throw new AtomException("Parameter pConfig is null");
    }
    if (pConfig.isEmpty()) {
      // simply return
      return;
    }
    if (pScope == null) {
      throw new AtomException("Parameter pScope is null");
    }

    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(pObject.getClass());

      // get all the property descriptors for the bean, iterate over them, and when a match is found
      // in the properties loaded from the configuration file set the actual value
      PropertyDescriptor[] beanProps = beanInfo.getPropertyDescriptors();
      if ((beanProps != null) && (beanProps.length > 0)) {
        for (PropertyDescriptor beanProp : beanProps) {
          String propertyName = beanProp.getName();
          String valueInFile = pConfig.getProperty(propertyName);
          if (valueInFile != null) {
            try {
              setPropertyValue(pObject, beanProp, propertyName, valueInFile, pScope);
            } catch (ConfigurationException ce) {
              throw new AtomException("ConfigurationException for property " + propertyName + " and value = " + valueInFile, ce);
            }
          } else {
            // if the property name is not found in the configuration, then
            // this may be a linked property value. i.e. propName^=/other/Component.propValue
            if (isLinkedProperty(propertyName, pConfig)) {
              Object linkedValue = getLinkedPropertyValue(propertyName, pConfig, pScope);
              try {
                setPropertyValue(pObject, beanProp, propertyName, linkedValue);
              } catch (ConfigurationException ce) {
                throw new AtomException("ConfigurationException for property " + propertyName + " and value = " + linkedValue, ce);
              }
            }
          }
        }
      }
    } catch (IntrospectionException ie) {
      throw new AtomException("IntrospectionException for class = " + pObject.getClass(), ie);
    }
  } // end configure


  /**
   * Gets the value of a linked property
   * @param pPropertyName name of the linked property
   * @param pConfig configuration containing the linked property
   * @param pScope scope to resolve names
   * @return value of the linked property, or null if it cannot be obtained
   * @throws IntrospectionException if errors
   */
  private Object getLinkedPropertyValue (String pPropertyName, Properties pConfig, Scope pScope) throws IntrospectionException {
    if (pPropertyName == null || "".equals(pPropertyName)) {
      return null;
    }
    if (pConfig == null || pConfig.isEmpty()) {
      return null;
    }
    if (pScope == null) {
      return null;
    }

    Object propertyValue = null;
    String linkedPropertyName = pConfig.getProperty(pPropertyName + "^");
    int idx = linkedPropertyName.lastIndexOf(".");
    String componentName = linkedPropertyName.substring(0, idx);
    String propertyName = linkedPropertyName.substring(idx + 1);
    Object component = pScope.resolveName(componentName, true);
    if (component != null) {
      BeanInfo beanInfo = Introspector.getBeanInfo(component.getClass());
      PropertyDescriptor[] beanProps = beanInfo.getPropertyDescriptors();
      if ((beanProps != null) && (beanProps.length > 0)) {
        for (PropertyDescriptor beanProp : beanProps) {
          if (propertyName.equals(beanProp.getName())) {
            Method readMethod = beanProp.getReadMethod();
            if (readMethod != null) {
              try {
                propertyValue = readMethod.invoke(component, (Object[]) null);
              } catch (IllegalAccessException iae) {
                throw new AtomException("IllegalAccessException for property " + propertyName + "on component " + componentName, iae);
              } catch (InvocationTargetException ite) {
                throw new AtomException("InvocationTargetException for property " + propertyName + "on component " + componentName, ite);
              }
            }
          }
        }
      }
    }
    return propertyValue;
  } // end getLinkedPropertyValue


  /**
   * Determines if in the given properties there is a property names as "propertyName^", i.e. a linked property
   * @param pPropertyName base name for the linked property
   * @param pConfig configuration
   * @return true if the given name is a linked property, false otherwise
   */
  private boolean isLinkedProperty (String pPropertyName, Properties pConfig) {
    if (pPropertyName == null || "".equals(pPropertyName)) {
      return false;
    }
    if (pConfig == null || pConfig.isEmpty()) {
      return false;
    }

    boolean result = false;
    String linkedPropertyName = pConfig.getProperty(pPropertyName + "^");
    if (linkedPropertyName != null && !"".equals(linkedPropertyName)) {
      // the name corresponds, now check if the property value is a real component property
      if (linkedPropertyName.startsWith("/") && linkedPropertyName.lastIndexOf(".") != -1) {
        result = true;
      }
    }

    return result;
  } // end isLinkedProperty


  /**
   * Sets the value of a property for the given object
   * @param pTarget object to set the property into
   * @param pDescriptor property descriptor to get the write method
   * @param pPropertyName name of the property to set
   * @param pNewValueAsString new value of the property as string
   * @param pScope scope for naming resolution
   * @throws ConfigurationException if the property cannot be set to the value
   */
  private void setPropertyValue (Object pTarget, PropertyDescriptor pDescriptor, String pPropertyName, String pNewValueAsString, Scope pScope) throws ConfigurationException {
    if (pTarget == null) {
      throw new AtomException("Parameter pTarget is null");
    }
    if (pDescriptor == null) {
      throw new AtomException("Parameter pDescriptor is null");
    }
    if (pPropertyName == null || "".equals(pPropertyName)) {
      throw new AtomException("Parameter pPropertyName is null or empty");
    }

    // nulls for the new value are permitted, so don't check on pNewValueAsString

    // find the methods to and write the property value
    Method writeMethod = pDescriptor.getWriteMethod();
    if (writeMethod == null) {
      throw new ConfigurationException("No write method for property " + pPropertyName);
    }

    // determine the property type and perform conversion if necessary
    Class targetType = pDescriptor.getPropertyType();
    Object value = null;
    if (targetType.isArray()) {
      // get the property editor for the array members
      PropertyEditor editor = PropertyEditors.getPropertyEditor(targetType.getComponentType());

      // split the string value
      String[] values = pNewValueAsString.split(",");

      // get an array of the appropriate type and length
      value = Array.newInstance (targetType.getComponentType(), values.length);

      // set all values of the array
      for (int i = 0; i < values.length; i++) {
        editor.setAsText(values[i]);
        Object memberval = editor.getValue();
        if (memberval != null) {
          Array.set(value, i, memberval);
        }
      }
    } else {
      PropertyEditor editor = PropertyEditors.getPropertyEditor(targetType);
      if (editor != null) {
        // we end up here if the property type is not an Atom component
        editor.setAsText(pNewValueAsString);
        value = editor.getValue();
      } else {
        // the property type is not registered in the standard editors, so it's probably
        // another component: try to resolve and maybe instantiate it
        if (pNewValueAsString.startsWith("/") && !pNewValueAsString.endsWith("/")) {
          value = pScope.resolveName(pNewValueAsString, true);
        }
      }
    }

    // actually set the new value by calling the setter
    try {
      writeMethod.invoke(pTarget, value);
    } catch (IllegalAccessException iae) {
      throw new ConfigurationException("IllegalAccessException for property " + pPropertyName, iae);
    } catch (InvocationTargetException ite) {
      throw new ConfigurationException("InvocationTargetException for property " + pPropertyName, ite);
    }
  } // end setPropertyValue


  /**
   * Setter that does not perform any text/object conversion
   * @param pTarget object to set the property into
   * @param pDescriptor property descriptor to get the write method
   * @param pPropertyName name of the property to set
   * @param pValue new value of the property
   * @throws ConfigurationException if the property cannot be set to the value
   */
  private void setPropertyValue (Object pTarget, PropertyDescriptor pDescriptor, String pPropertyName, Object pValue) throws ConfigurationException {
    if (pTarget == null) {
      throw new AtomException("Parameter pTarget is null");
    }
    if (pDescriptor == null) {
      throw new AtomException("Parameter pDescriptor is null");
    }
    if (pPropertyName == null || "".equals(pPropertyName)) {
      throw new AtomException("Parameter pPropertyName is null or empty");
    }

    // nulls for the new value are permitted, so don't check on pNewValueAsString

    // find the methods to and write the property value
    Method writeMethod = pDescriptor.getWriteMethod();
    if (writeMethod == null) {
      throw new ConfigurationException("No write method for property " + pPropertyName);
    }

    // actually set the new value by calling the setter
    try {
      writeMethod.invoke(pTarget, pValue);
    } catch (IllegalAccessException iae) {
      throw new ConfigurationException("IllegalAccessException for property " + pPropertyName, iae);
    } catch (InvocationTargetException ite) {
      throw new ConfigurationException("InvocationTargetException for property " + pPropertyName, ite);
    }
  }

} // end BeanConfigurator

