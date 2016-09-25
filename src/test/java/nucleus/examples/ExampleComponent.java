package atom.examples;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Example component
 */
public class ExampleComponent {

  private int mIntProp;
  public int getIntProp () {
    return mIntProp;
  }
  public void setIntProp (int pIntProp) {
    mIntProp = pIntProp;
  }

  private boolean mBooleanProp;
  public boolean isBooleanProp () {
    return mBooleanProp;
  }
  public void setBooleanProp (boolean pBooleanProp) {
    mBooleanProp = pBooleanProp;
  }

  private short mShortProp;
  public short getShortProp () {
    return mShortProp;
  }
  public void setShortProp (short pShortProp) {
    mShortProp = pShortProp;
  }

  private long mLongProp;
  public long getLongProp () {
    return mLongProp;
  }
  public void setLongProp (long pLongProp) {
    mLongProp = pLongProp;
  }

  private float mFloatProp;
  public float getFloatProp () {
    return mFloatProp;
  }
  public void setFloatProp (float pFloatProp) {
    mFloatProp = pFloatProp;
  }

  private double mDoubleProp;
  public double getDoubleProp () {
    return mDoubleProp;
  }
  public void setDoubleProp (double pDoubleProp) {
    mDoubleProp = pDoubleProp;
  }

  private String mStringProp;
  public String getStringProp () {
    return mStringProp;
  }
  public void setStringProp (String pStringProp) {
    mStringProp = pStringProp;
  }

  private int[] mIntArrayProp;
  public int[] getIntArrayProp () {
    return mIntArrayProp;
  }
  public void setIntArrayProp (int[] pIntArrayProp) {
    mIntArrayProp = pIntArrayProp;
  }

  private String[] mStrArrayProp;
  public String[] getStrArrayProp () {
    return mStrArrayProp;
  }
  public void setStrArrayProp (String[] pStrArrayProp) {
    mStrArrayProp = pStrArrayProp;
  }

  private List mListProp;
  public List getListProp () {
    return mListProp;
  }
  public void setListProp (List pListProp) {
    mListProp = pListProp;
  }

  private ExampleComponent2 mDependency;
  public ExampleComponent2 getDependency () {
    return mDependency;
  }
  public void setDependency (ExampleComponent2 pDependency) {
    mDependency = pDependency;
  }

  private Map mMap;
  public Map getMap () {
    return mMap;
  }
  public void setMap (Map pMap) {
    mMap = pMap;
  }

  private Properties mProperties;
  public Properties getProperties () {
    return mProperties;
  }
  public void setProperties (Properties pProperties) {
    mProperties = pProperties;
  }

  private Date mDate1;
  public Date getDate1 () {
    return mDate1;
  }
  public void setDate1 (Date pDate1) {
    mDate1 = pDate1;
  }
  private Date mDate2;
  public Date getDate2 () {
    return mDate2;
  }
  public void setDate2 (Date pDate2) {
    mDate2 = pDate2;
  }

  private File mFile;
  public File getFile () {
    return mFile;
  }
  public void setFile (File pFile) {
    mFile = pFile;
  }
} // end ExampleComponent

