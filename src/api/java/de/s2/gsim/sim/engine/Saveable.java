package de.s2.gsim.sim.engine;

import java.util.List;

import de.s2.gsim.sim.agent.RtAgent;

public interface Saveable {

    public int getAgentCount() throws GSimEngineException;

    public RtAgent getAgentState(String agentName) throws GSimEngineException;

    public List<RtAgent> getGlobalState() throws GSimEngineException;

    public List<RtAgent> getGlobalState(int count, int offset) throws GSimEngineException;

    public SimulationID getSimulationID() throws GSimEngineException;

    public int getTimeStep() throws GSimEngineException;

}
