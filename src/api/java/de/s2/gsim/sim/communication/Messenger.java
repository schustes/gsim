package de.s2.gsim.sim.communication;

import java.util.List;

import de.s2.gsim.sim.GSimEngineException;

public interface Messenger {

    public void addAgentToHandle(AgentType r);

    public void destroy();

    public void endSession() throws GSimEngineException;

    public Communicator getCommunicationInterface();

    public String getNamespace();

    public void setHandledAgents(List<AgentType> list);

    public void startSession() throws GSimEngineException;

}
