package atom.exception;

/**
 * Exception used to report configuration problems
 */
public class ConfigurationException extends Exception {

  private static final long serialVersionUID = 8973143913927237247L;

  public ConfigurationException () {
    super();
  }

  public ConfigurationException (String message) {
    super(message);
  }

  public ConfigurationException (String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationException (Throwable cause) {
    super(cause);
  }

} // end class

