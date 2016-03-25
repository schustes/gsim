package de.s2.gsim.sim.communication;

import de.s2.gsim.sim.engine.GSimEngineException;

public interface Communicator {

    public void broadcast(BroadcastProtocol protocol) throws GSimEngineException;

    public Conversation createConversation(CommunicationProtocol protocol) throws GSimEngineException;

    public void startConversation(Conversation conversation);

}
