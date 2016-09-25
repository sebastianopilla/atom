package atom.examples;

/**
 * Example to show circular dependencies
 */
public class ExampleCircularity2 {

  private ExampleCircularity1 mCirc1;
  public ExampleCircularity1 getCirc1 () {
    return mCirc1;
  }
  public void setCirc1 (ExampleCircularity1 pCirc1) {
    mCirc1 = pCirc1;
  }

} // end ExampleCircularity2

