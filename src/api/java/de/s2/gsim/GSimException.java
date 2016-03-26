package de.s2.gsim;

/**
 * The gsim exception.
 *
 * @author Stephan
 *
 */
public class GSimException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public GSimException() {
        super();
    }

    /**
     * Inherited constructor.
     * 
     * @param message
     */
    public GSimException(String message) {
        super(message);
    }

    /**
     * Inherited constructor.
     * 
     * @param message
     * @param cause
     */
    public GSimException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Inherited constructor.
     * 
     * @param cause
     */
    public GSimException(Throwable cause) {
        super(cause);
    }
}
