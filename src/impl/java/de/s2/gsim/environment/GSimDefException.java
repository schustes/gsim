package de.s2.gsim.environment;

public class GSimDefException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GSimDefException() {
        super();
    }

    public GSimDefException(String message) {
        super(message);
    }

    public GSimDefException(String message, Throwable cause) {
        super(message, cause);
    }

}
