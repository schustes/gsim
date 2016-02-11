package de.s2.gsim.sim.communication;

public abstract class CommunicationProtocolRespond extends AbstractCommunicationProtocol implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public long timeOut = 100000;

    protected transient AgentType agent;

    private double commId = 0;

    private String initiator;

    @Override
    public boolean equals(Object o) {
        if (o instanceof CommunicationProtocolRespond) {
            return ((CommunicationProtocolRespond) o).commId == commId;
        }
        return false;
    }

    @Override
    public double getCommId() {
        return commId;
    }

    @Override
    public String getOwnName() {
        return agent.getName();
    }

    @Override
    public String getPartnerName() {
        return initiator;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public void setAgent(AgentType a) {
        agent = a;
    }

    public void setCommId(double id) {
        commId = id;
    }

    public void setPartnerName(String s) {
        initiator = s;
    }

    protected AgentType getAgent() {
        return agent;
    }

}
