package atom.examples;

/**
 * Example to show circular dependencies
 */
public class ExampleCircularity1 {

  private ExampleCircularity2 mCirc2;
  public ExampleCircularity2 getCirc2 () {
    return mCirc2;
  }
  public void setCirc2 (ExampleCircularity2 pCirc2) {
    mCirc2 = pCirc2;
  }
} // end ExampleCircularity1

