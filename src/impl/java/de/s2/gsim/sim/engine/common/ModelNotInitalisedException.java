package de.s2.gsim.sim.engine.common;

public class ModelNotInitalisedException extends Exception {

    private static final long serialVersionUID = 1L;

    public ModelNotInitalisedException() {
        super();
    }

    public ModelNotInitalisedException(String message) {
        super(message);
    }

    public ModelNotInitalisedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelNotInitalisedException(Throwable cause) {
        super(cause);
    }
}
