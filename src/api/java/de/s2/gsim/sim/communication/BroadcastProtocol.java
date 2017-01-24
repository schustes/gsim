package de.s2.gsim.sim.communication;

//import gsim.sim.engine.remote.messaging.BroadcastMessage;

import org.apache.log4j.Logger;

public abstract class BroadcastProtocol implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(BroadcastProtocol.class);

    protected BroadcastProtocolRespond[] listeners;

    protected String sender;

    public BroadcastProtocol(String sender, BroadcastProtocolRespond... listeners) {
        this.sender = sender;
        this.listeners = listeners;
        logger.debug("Broadcast for " + sender + ", listeners: " + listeners.length);
    }

    public abstract Message broadcast();

    public BroadcastProtocolRespond[] getListeners() {
        return listeners;
    }

}
