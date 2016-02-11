package de.s2.gsim.sim.engine;

import java.util.List;

import de.s2.gsim.sim.agent.AgentState;

public interface Saveable {

    public int getAgentCount() throws GSimEngineException;

    public AgentState getAgentState(String agentName) throws GSimEngineException;

    public List<AgentState> getGlobalState() throws GSimEngineException;

    public List<AgentState> getGlobalState(int count, int offset) throws GSimEngineException;

    public SimulationID getSimulationID() throws GSimEngineException;

    public int getTimeStep() throws GSimEngineException;

}
