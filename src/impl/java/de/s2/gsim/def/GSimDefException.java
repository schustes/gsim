package de.s2.gsim.def;

public class GSimDefException extends java.rmi.RemoteException {

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
