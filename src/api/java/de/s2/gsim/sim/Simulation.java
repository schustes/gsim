package de.s2.gsim.sim;

import java.util.List;

import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.objects.AppAgent;
import de.s2.gsim.sim.agent.RtAgent;

/**
 * A Simulation is an actual running simulation instance. It gets created by the simulation engine by a {@link ModelDefinitionEnvironment} that is
 * passed to the engine when it is created. A Simulation has a state that is defined by its agents and a time step.
 * 
 * There are also methods to interact with the simulation, e.g. by calling mehtods for adding, replacing or removing agents.
 * 
 * @author stephan
 *
 */
public interface Simulation {

    /**
     * Adds an agent to the running simulation, instanciating the agent from the specified agent class of the related
     * {@link ModelDefinitionEnvironment}.
     * 
     * @param agentClass the agent class to create the agent from
     * @param name of the agent
     * @param method generation method (see constants in {@link ModelDefinitionEnvironment}
     * @param svar standard variation used for initialising the agent's properties
     * @return some sort of id
     */
    String addNormallyDistributedNewAgentToRunningModel(String agentClass, String name, double svar);
    
    String addUniformDistributedNewAgentToRunningModel(String agentClass, String name);

    /**
     * Gets the current number of agents in the simulation.
     * 
     * @return the number of agents
     */
    int getAgentCount();

    /**
     * Gets all agent names of agents in the simulation
     * 
     * @return the names
     */
    String[] getAgentNames();

    /**
     * Gets a particular agent.
     * 
     * @param agentName the name of the agent to get
     * @return the agent
     */
    RtAgent getAgent(String agentName);

    /**
     * Get all agents in the simulation.
     * 
     * @return list of agents
     */
    List<RtAgent> getAllAgents();

    /**
     * Get a partial list of agents in the simulation.
     * 
     * @param count the number of agents to get
     * @param offset the offset from where to get the agents
     * @return list of agents
     */
    List<RtAgent> getAllAgents(int count, int offset);

    /**
     * Gets all application agents.
     * 
     * @return the agents
     */
    AppAgent[] getAppAgents();

    /**
     * Retrieves a particular application agent.
     * 
     * @param name the name of the agent
     * @return the application agent
     */
    AppAgent getAppAgent(String name);

    /**
     * Gets the current time step the simulation is in.
     * 
     * @return the time step
     */
    int getCurrentTimeStep();

    /**
     * Gets all DataHandlers registered with the simulation.
     * 
     * @return the data handlers
     */
    DataHandler[] getDataHandlers();

    /**
     * Retrieves a particular DataHandler from the simulation.
     * 
     * @param name name of the handler
     * @return the handler
     */
    DataHandler getDataHandler(String name);

    /**
     * Gets the {@link ModelDefinitionEnvironment} that is used to generate agents and objects of this simulation.
     * 
     * @return the environment
     */
    ModelDefinitionEnvironment getDefinitionEnvironment();

    /**
     * Gets the NameSpace of the simulation. The NameSpace is the first part of a {@link SimulationId} and allows to identify all instances (currently
     * running repetitions) of a simulation.
     * 
     * @return the NameSpace string
     */
    String getNameSpace();

    /**
     * Replaces an agent with the same name as the given one.
     * 
     * @param agent the agent that replaces the one in the simulation
     */
    void replaceAgent(RtAgent agent);

    /**
     * Removes an agent from the simulation.
     * 
     * @param agentName the name of the agent to be removed
     */
    void removeAgent(String agentName);

    /**
     * Set the NameSpace of the simulation. The NameSpace is the first part of a {@link SimulationId} and allows to identify all instances (currently
     * running repetitions) of a simulation.
     * 
     * @param ns the NameSpace string
     */
    void setNameSpace(String ns);

}
