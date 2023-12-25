package de.s2.gsim.sim.communication;

public abstract class AbstractCommunicationProtocol {

    public abstract String getCommId();

    public abstract String getOwnName();

    public abstract String getPartnerName();

    public void onEnd() {
    }

    public abstract Message respond(Message m);

}
