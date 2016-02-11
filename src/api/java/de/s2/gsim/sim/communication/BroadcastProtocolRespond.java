package de.s2.gsim.sim.communication;

public abstract class BroadcastProtocolRespond implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public BroadcastProtocolRespond() {
    }

    public abstract void onMessage(AgentType agent, Message m);

}
