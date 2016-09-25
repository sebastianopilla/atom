package atom;

/**
 * Interface to be implemented by all components that want to initialize themselves after creation.
 * Atom calls the start() method after the component instance has been created and registered into its scope
 */
public interface Startable {

  /**
   * Called after component initialization and registration to initialize the component
   */
  void start ();

}
