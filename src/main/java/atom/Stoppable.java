package atom;

/**
 * Interface to be implemented by all components that want to shutdown themselves before de-registration.
 * Atom calls the stop() method before de-registering the component instance from its scope
 */
public interface Stoppable {

  /**
   * Called before component de-registration to shutdown the component
   */
  void stop ();

}
