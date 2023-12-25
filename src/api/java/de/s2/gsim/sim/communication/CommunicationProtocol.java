package de.s2.gsim.sim.communication;

import java.util.UUID;

public abstract class CommunicationProtocol extends AbstractCommunicationProtocol implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public long timeOut = 100000;

    private String commId = UUID.randomUUID().toString();

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
    public String getCommId() {
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

    public void setCommId(String id) {
        commId = id;
    }

}
