package de.s2.gsim.sim.communication;

public class Conversation implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int commState = 0;

    private Messenger messenger;

    private CommunicationProtocol p;

    private CommunicationProtocolRespond r;

    public Conversation(CommunicationProtocol p, Messenger m) {
        super();
        this.p = p;
        messenger = m;
    }

    public Conversation(CommunicationProtocolRespond r, Messenger m) {
        super();
        this.r = r;
        messenger = m;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Conversation)) {
            return false;
        }
        return ((Conversation) o).getCommId() == getCommId();
    }

    public final double getCommId() {
        if (p != null) {
            return p.getCommId();
        }
        if (r != null) {
            return r.getCommId();
        }
        throw new RuntimeException("No commId present!");
    }

    public AbstractCommunicationProtocol getCurrentRole() {
        if (p != null) {
            return p;
        }
        if (r != null) {
            return r;
        }
        throw new RuntimeException("No protocol present!");
    }

    /**
     * Returns always null on the receiver side!!!!!!
     * 
     * @return CommunicationProtocol
     */
    public CommunicationProtocol getProtocol() {
        return p;
    }

    public CommunicationProtocolRespond getProtocolRespond() {
        return r;
    }

    public int getState() {
        return commState;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public void setState(int state) {
        commState = state;
    }

    public final void start() {
        if (r != null) {
            throw new RuntimeException("THis is a receiver of a communication");
        }
        messenger.getCommunicationInterface().startConversation(this);
    }

}
