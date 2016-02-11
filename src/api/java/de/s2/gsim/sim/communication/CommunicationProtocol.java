package de.s2.gsim.sim.communication;

public abstract class CommunicationProtocol extends AbstractCommunicationProtocol implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public long timeOut = 100000;

    private double commId = 0;

    private String ownName;

    private String partner;

    private CommunicationProtocolRespond receiverProtocol;

    public CommunicationProtocol(String sender, String receiver, CommunicationProtocolRespond receiverProtocol) {
        ownName = sender;
        partner = receiver;
        this.receiverProtocol = receiverProtocol;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CommunicationProtocol) {
            return ((CommunicationProtocol) o).commId == commId;
        }
        return false;
    }

    @Override
    public double getCommId() {
        return commId;
    }

    @Override
    public String getOwnName() {
        return ownName;
    }

    @Override
    public String getPartnerName() {
        return partner;
    }

    public CommunicationProtocolRespond getReceiverProtocol() {
        return receiverProtocol;
    }

    public abstract Message getStartMessage();

    @Override
    public int hashCode() {
        return 1;
    }

    public void setCommId(double id) {
        commId = id;
    }

}
