package de.s2.gsim.sim.communication;

import java.util.List;

import de.s2.gsim.sim.GSimEngineException;

public interface Messenger {

    void addAgentToHandle(AgentType r);

    void destroy();

    void endSession() throws GSimEngineException;

    Communicator getCommunicationInterface();

    void startSession() throws GSimEngineException;

}
