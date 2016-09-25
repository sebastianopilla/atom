package atom;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import atom.exception.AtomException;

/**
 * Holds name/component mappings
 */
public class Scope implements Serializable {

  private static final long serialVersionUID = -684556727747047444L;

  // the mappings
  private Map<String,Object> mMapping;

  // this scope name
  private String mName;

  // the parent scope (null if the global scope)
  private Scope mParentScope;

  private LinkedHashSet<String> mCreatedComponents;


  /**
   * No-args constructor
   */
  public Scope () {
    mMapping = new HashMap<String,Object>();
    mCreatedComponents = new LinkedHashSet<String>();
  }


  /**
   * Constructor that sets the scope name
   * @param pName scope name
   */
  public Scope (String pName) {
    this();
    mName = pName;
  }


  /**
   * Registers a component in this scope
   * @param pName full component name
   * @param pComponent component to register
   */
  public void register (String pName, Object pComponent) {
    if (pName == null || "".equals(pName)) {
      return;
    }
    if (pComponent == null) {
      return;
    }

    // protect structural modifications with synchronization on this scope instance
    synchronized (this) {
      mMapping.put(pName, pComponent);
    }
  } // end register


  /**
   * Unregisters from this scope the component with the given name
   * @param pName full component name
   */
  public void unregister (String pName) {
    if (pName == null || "".equals(pName)) {
      return;
    }

    // protect structural modifications with synchronization on this scope instance
    synchronized (this) {
      mMapping.remove(pName);
    }
  } // end unregister


  /**
   * Retrieves in this or in any parent scope the object with the given name.
   * Equivalent to resolveName(pName, true)
   * @param pName full component name
   * @return component instance, or null if absent
   */
  public Object resolveName (String pName) {
    return resolveName(pName, true);
  }


  /**
   * Retrieves in this or in any parent scope the object with the given name, optionally creating it
   * @param pName full component name
   * @param pCreate true to create a component that doesn't exist already
   * @return component instance, or null if absent
   */
  public Object resolveName (String pName, boolean pCreate) {
    if (pName == null || "".equals(pName)) {
      return null;
    }
    Object result = mMapping.get(pName);

    // if the component is not in this scope, traverse the scope hierarchy looking for the name in a parent scope somewhere
    if (result == null) {
      Scope candidate = mParentScope;
      while (candidate != null) {
        result = candidate.resolveName(pName, false);
        if (result != null) {
          break;
        }
        candidate = candidate.mParentScope;
      }
    }

    // if the component is not found in the scope hierarchy, it may have to be created
    if (result == null && pCreate) {
      if (mCreatedComponents.contains(pName)) {
        throw new AtomException("Possible circular reference starting from component " + pName);
      }
      mCreatedComponents.add(pName);
      result = Atom.getAtom().createComponent(pName, this);
    }
    return result;
  } // end resolveName


  /**
   * Sets the parent scope of this one
   * @param pParent parent scope
   */
  public void setParentScope (Scope pParent) {
    mParentScope = pParent;
  }


  /**
   * Get the parent scope of this one (if global, the parent will be null)
   * @return parent scope
   */
  public Scope getParentScope () {
    return mParentScope;
  }
  

  /**
   * Returns this scope name
   * @return scope name
   */
  public String getName () {
    return mName;
  }


  /**
   * Returns an iterator useful to loop over all the components of this scope
   * @return iterator of component names
   */
  public Iterator getComponentsInScope () {
    return mCreatedComponents.iterator();
  }


  /**
   * Returns a textual representation of this scope
   * @return text representation
   */
  public String toString () {
    StringBuilder buf = new StringBuilder();
    buf.append("scope[").append(getName()).append("]={");
    Iterator it = mMapping.keySet().iterator();
    while (it.hasNext()) {
      buf.append(it.next());
      if (it.hasNext()) {
        buf.append("; ");
      }
    }
    buf.append("}");
    return buf.toString();
  }
  
} // end Scope

