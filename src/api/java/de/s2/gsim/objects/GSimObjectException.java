package de.s2.gsim.objects;

public class GSimObjectException extends Exception {

    private static final long serialVersionUID = 1L;

    public GSimObjectException() {
        super();
    }

    public GSimObjectException(String message) {
        super(message);
    }

    public GSimObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public GSimObjectException(Throwable cause) {
        super(cause);
    }
}
