package de.s2.gsim.sim.behaviour;

/**
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GSimBehaviourException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GSimBehaviourException() {
        super();
    }

    public GSimBehaviourException(String message) {
        super(message);
    }

    public GSimBehaviourException(String message, Throwable cause) {
        super(message, cause);
    }

    public GSimBehaviourException(Throwable cause) {
        super(cause);
    }
}
