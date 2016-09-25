package atom.exception;

/**
 * Exception for runtime errors
 */
public class AtomException extends RuntimeException {

  private static final long serialVersionUID = 8244403613109880011L;

  public AtomException () {
    super();
  }

  public AtomException (String pMessage) {
    super(pMessage);
  }

  public AtomException (String pMessage, Throwable pCause) {
    super(pMessage, pCause);
  }

  public AtomException (Throwable pCause) {
    super(pCause);
  }
} // end AtomException

