package atom.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Example component for manipulating collections
 */
public class CollectionExample {

  private ArrayList mListProperty;
  private HashMap mMapProperty;
  private HashSet mSetProperty;

  public ArrayList getListProperty () {
    return mListProperty;
  }

  public void setListProperty (ArrayList pListProperty) {
    mListProperty = pListProperty;
  }

  public HashMap getMapProperty () {
    return mMapProperty;
  }

  public void setMapProperty (HashMap pMapProperty) {
    mMapProperty = pMapProperty;
  }

  public HashSet getSetProperty () {
    return mSetProperty;
  }

  public void setSetProperty (HashSet pSetProperty) {
    mSetProperty = pSetProperty;
  }
} // end CollectionExample

