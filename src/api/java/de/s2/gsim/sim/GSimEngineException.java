package de.s2.gsim.sim;

/**
 * Specific simulation engine exception.
 * 
 * @author stephan
 *
 */
public class GSimEngineException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public GSimEngineException() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param cause the cause
     */
    public GSimEngineException(Exception cause) {
        super(cause);
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     */
    public GSimEngineException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * 
     * @param message exception message
     * @param cause cause
     */
    public GSimEngineException(String message, Throwable cause) {
        super(message, cause);
    }

}
