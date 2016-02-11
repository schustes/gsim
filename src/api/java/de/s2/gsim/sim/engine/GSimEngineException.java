package de.s2.gsim.sim.engine;

import java.rmi.RemoteException;

public class GSimEngineException extends Exception {

    private static final long serialVersionUID = 1L;

    public GSimEngineException() {
        super();
    }

    public GSimEngineException(Exception cause) {
        super(cause);
    }

    public GSimEngineException(RemoteException cause) {
        super("RemoteException", cause);
    }

    public GSimEngineException(String s) {
        super(s);
    }

    public GSimEngineException(String s, Throwable cause) {
        super(s, cause);
    }

}
