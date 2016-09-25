package atom.examples;

import atom.Startable;
import atom.Stoppable;

/**
 * Example for starting and stopping a component
 */
public class StartStop implements Startable, Stoppable {

  private String mState;


  /**
   * Called after component initialization and registration to initialize the component
   */
  public void start () {
    mState = "initialized";
  }

  /**
   * Called before component de-registration to shutdown the component
   */
  public void stop () {
    mState = null;
  }

  public String getState () {
    return mState;
  }
} // end StartStop

