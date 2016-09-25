package atom;

import java.io.File;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import java.text.SimpleDateFormat;
import atom.examples.*;
import atom.exception.AtomException;

import static junit.framework.Assert.*;

/**
 * Unit tests for Atom
 */
public class TestAtom {
  
  Atom atom = Atom.getAtom();

  @Before
  public void setUp () throws Exception {
    // this needs a list of directories to be passed as VM parameter
    // split the list around ";" on each OS, since it's passed as a string
    String basePath = System.getProperty("test.basedir");
    if (basePath != null && !"".equals(basePath)) {
      String path = System.getProperty("test.configpath");
      String[] paths = path.split(";");
      for (int i = 0; i < paths.length; i++) {
        File dir = new File(basePath + File.separator + paths[i]);
        atom.addToConfigPath(dir);
      }
    }
  }

  @Test
  public void testHasConfigPath () {
    assertNotNull(atom.getConfigPath());
  }

  @Test(expected = AtomException.class)
  public void testNullEmptyComponentName () {
    assertNull(atom.resolveName(null));
    assertNull(atom.resolveName(""));
  }

  @Test(expected = AtomException.class)
  public void testNonAbsoluteComponentName () {
    assertNull(atom.resolveName("TestGlobal"));
  }

  @Test
  public void testInstantiateComponent () {
    Object o = atom.resolveName("/test/TestGlobal1");
    assertNotNull(o);
  }

  @Test
  public void testRegisterGlobalScope () {
    Object o = atom.resolveName("/test/TestGlobal");
    assertNotNull(o);
    atom.getGlobalScope().register("/test/TestGlobal", o);
    assertTrue(atom.isGlobalComponent("/test/TestGlobal"));
    atom.getGlobalScope().unregister("/test/TestGlobal");
    assertFalse(atom.isGlobalComponent("/test/TestGlobal"));
  }

  @Test
  public void testIntProperty () {
    Object o = atom.resolveName("/test/TestInt");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertEquals(42, c.getIntProp());
  }

  @Test
  public void testBooleanProperty () {
    Object o = atom.resolveName("/test/TestBoolean");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertEquals(true, c.isBooleanProp());
  }

  @Test
  public void testShortProperty () {
    Object o = atom.resolveName("/test/TestShort");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertEquals(42, c.getShortProp());
  }

  @Test
  public void testLongProperty () {
    Object o = atom.resolveName("/test/TestLong");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertEquals(6765764654232L, c.getLongProp());
  }

  @Test
  public void testFloatProperty () {
    Object o = atom.resolveName("/test/TestFloat");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertEquals(new Float(42.14f), new Float(c.getFloatProp()));
  }

  @Test
  public void testDoubleProperty () {
    Object o = atom.resolveName("/test/TestDouble");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertEquals(new Double(46752.148987d), new Double(c.getDoubleProp()));
  }

  @Test
  public void testStringProperty () {
    Object o = atom.resolveName("/test/TestString");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertEquals("this is a string", c.getStringProp());
  }

  @Test
  public void testEmptyStringProperty () {
    Object o = atom.resolveName("/test/TestEmptyString");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertEquals("", c.getStringProp());
  }

  @Test
  public void testArraysProperty () {
    Object o = atom.resolveName("/test/TestIntArray");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    int[] arr1 = c.getIntArrayProp();
    assertNotNull(arr1);
    assertEquals(5, arr1.length);
    for (int i = 0; i < arr1.length; i++) {
      assertEquals(i + 1, arr1[i]);
    }
    Object s = atom.resolveName("/test/TestStringArray");
    assertNotNull(s);
    assertTrue(s instanceof ExampleComponent);
    ExampleComponent d = (ExampleComponent)s;
    String[] arr2 = d.getStrArrayProp();
    assertNotNull(arr2);
    assertEquals(4, arr2.length);
    String[] expected = {"this", "is", "an", "array"};
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], arr2[i]);
    }
  }

  @Test
  public void testListProperties () {
    Object o = atom.resolveName("/test/TestLists");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertNotNull(c.getListProp());
    assertEquals(5, c.getListProp().size());
    for (int i = 1; i <= 5; i++) {
      String exp = String.valueOf(i);
      String act = (String)c.getListProp().get(i - 1);
      assertEquals(exp, act);
    }
  }

  @Test
  public void testDependency () {
    Object o = atom.resolveName("/test/TestObjectDependent");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertNotNull(c.getDependency());
    assertTrue(c.getDependency() instanceof ExampleComponent2);
  }

  @Test(expected = AtomException.class)
  public void testCircularity () {
    Object o = atom.resolveName("/test/circularity/TestCircularity1");
    assertNotNull(o);
    assertTrue(o instanceof ExampleCircularity1);
  }

  @Test
  public void testLayered () {
    Object o = atom.resolveName("/test/TestLayered");
    assertNotNull(o);
    assertTrue(o instanceof ExampleComponent);
    ExampleComponent c = (ExampleComponent)o;
    assertEquals(80, c.getIntProp());
  }

  @Test
  public void testLinkedProperty () {
    Object o = atom.resolveName("/test/links/TestLinked");
    assertNotNull(o);
    LinkedProperty l = (LinkedProperty)o;
    Object p = atom.resolveName("/test/links/TestLinking");
    assertNotNull(p);
    LinkingProperty x = (LinkingProperty)p;
    assertEquals(l.getCurrentWeather(), x.getTodaysWeather());
  }

  @Test
  public void testStartStop () {
    Object o = atom.resolveName("/test/startstop/TestStartStop");
    assertNotNull(o);
    StartStop s = (StartStop)o;
    assertNotNull(s.getState());
    // this is artificial, only to test the response to the stop command
    s.stop();
    assertNull(s.getState());
  }

  @Test
  public void testInitial () {
    Atom.getAtom().createComponent("/Initial", Atom.getAtom().getGlobalScope());
    Object o = atom.getGlobalScope().resolveName("/Initial", false);
    assertNotNull(o);
  }

  @Test
  public void testMapProperty () {
    Object o = atom.resolveName("/test/TestMap");
    assertNotNull(o);
    ExampleComponent c = (ExampleComponent)o;
    assertNotNull(c.getMap());
    assertTrue(c.getMap().size() > 0);
    assertTrue(c.getMap().containsKey("first"));
    assertEquals("bla", c.getMap().get("first"));
    assertTrue(c.getMap().containsKey("second"));
    assertEquals("blabla", c.getMap().get("second"));
  }

  @Test
  public void testPropertiesProperty () {
    Object o = atom.resolveName("/test/TestProperties");
    assertNotNull(o);
    ExampleComponent c = (ExampleComponent)o;
    assertNotNull(c.getProperties());
    assertTrue(c.getProperties().size() > 0);
    assertEquals("bla", c.getProperties().getProperty("first"));
    assertEquals("blabla", c.getProperties().getProperty("second"));
  }

  @Test
  public void testPropertiesOfComponentNames () {
    Object o = atom.resolveName("/test/TestPropertiesOfComponentNames");
    assertNotNull(o);
    ExampleComponent c = (ExampleComponent)o;
    assertNotNull(c.getProperties());
    assertTrue(c.getProperties().size() > 0);
    assertEquals("/test/TestFloat", c.getProperties().getProperty("testFloat"));
    assertEquals("/test/TestBoolean", c.getProperties().getProperty("testBoolean"));
  }

  @Test
  public void testDateProperty () throws Exception {
    Object o = atom.resolveName("/test/TestDate");
    assertNotNull(o);
    ExampleComponent c = (ExampleComponent)o;
    assertNotNull(c.getDate1());
    assertNotNull(c.getDate2());
    assertEquals((new SimpleDateFormat("yyyy-MM-dd")).parse("2009-01-26"), c.getDate1());
    assertEquals((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse("2010-02-28 13:45:22"), c.getDate2());
  }

  @Test
  public void testRelativeFileProperty () throws Exception {
    Object o = atom.resolveName("/test/TestRelativeFile");
    assertNotNull(o);
    ExampleComponent c = (ExampleComponent)o;
    assertNotNull(c.getFile());
    assertTrue(c.getFile().exists());
    assertTrue(c.getFile().canRead());
    assertTrue(c.getFile().isFile());
  }

  @Test
  public void testFileWithVariableProperty () throws Exception {
    Object o = atom.resolveName("/test/TestFileWithVariable");
    assertNotNull(o);
    ExampleComponent c = (ExampleComponent)o;
    assertNotNull(c.getFile());
    assertTrue(c.getFile().exists());
    assertTrue(c.getFile().canRead());
    assertTrue(c.getFile().isFile());
  }

  @Test
  public void testAbsoluteFileProperty () throws Exception {
    Object o = atom.resolveName("/test/TestAbsoluteFile");
    assertNotNull(o);
    ExampleComponent3 c = (ExampleComponent3)o;
    
    // check the appropriate file depending on the OS we're on
    File testFile = null;
    String operatingSystem = System.getProperty("os.name").toLowerCase();
    if (operatingSystem.indexOf("win") >= 0) {
      testFile = c.getWindowsFile();
    } else if (operatingSystem.indexOf("nux") >= 0) {
      testFile = c.getLinuxFile();
    }
    assertNotNull(testFile);
    assertTrue(testFile.exists());
    assertTrue(testFile.canRead());
    assertTrue(testFile.isFile());
  }

  @Test
  public void testDirectoryProperty () throws Exception {
    Object o = atom.resolveName("/test/TestDirectory");
    assertNotNull(o);
    ExampleComponent c = (ExampleComponent)o;
    assertNotNull(c.getFile());
    assertTrue(c.getFile().exists());
    assertTrue(c.getFile().canRead());
    assertTrue(c.getFile().isDirectory());
  }

  @Test
  public void testPlusEquals () {
    Object o = atom.resolveName("/test/collections/TestPlusEquals");
    assertNotNull(o);
    CollectionExample c = (CollectionExample)o;
    List listProperty = c.getListProperty();
    assertNotNull(listProperty);
    assertTrue(listProperty.contains("one"));
    assertTrue(listProperty.contains("two"));
    assertTrue(listProperty.contains("three"));
    Map mapProperty = c.getMapProperty();
    assertNotNull(mapProperty);
    assertTrue(mapProperty.containsKey("one"));
    assertTrue(mapProperty.containsKey("two"));
    assertTrue(mapProperty.containsKey("three"));
    assertTrue(mapProperty.containsValue("one"));
    assertTrue(mapProperty.containsValue("two"));
    assertTrue(mapProperty.containsValue("three"));
    Set setProperty = c.getSetProperty();
    assertNotNull(setProperty);
    assertTrue(setProperty.contains("one"));
    assertTrue(setProperty.contains("two"));
    assertTrue(setProperty.contains("three"));
  }

  @Test
  public void testMinusEquals () {
    Object o = atom.resolveName("/test/collections/TestMinusEquals");
    assertNotNull(o);
    CollectionExample c = (CollectionExample)o;
    List listProperty = c.getListProperty();
    assertNotNull(listProperty);
    assertTrue(listProperty.contains("one"));
    assertTrue(listProperty.contains("two"));
    assertFalse(listProperty.contains("three"));
    assertTrue(listProperty.contains("four"));
    Map mapProperty = c.getMapProperty();
    assertNotNull(mapProperty);
    assertTrue(mapProperty.containsKey("one"));
    assertFalse(mapProperty.containsKey("two"));
    assertTrue(mapProperty.containsKey("three"));
    assertTrue(mapProperty.containsKey("four"));
    assertTrue(mapProperty.containsValue("one"));
    assertFalse(mapProperty.containsValue("two"));
    assertTrue(mapProperty.containsValue("three"));
    assertTrue(mapProperty.containsValue("four"));
    Set setProperty = c.getSetProperty();
    assertNotNull(setProperty);
    assertTrue(setProperty.contains("one"));
    assertTrue(setProperty.contains("two"));
    assertTrue(setProperty.contains("three"));
    assertFalse(setProperty.contains("four"));
  }

} // end TestAtom

