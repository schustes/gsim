package de.s2.gsim.sim.agent;

import java.util.Optional;

import de.s2.gsim.objects.AgentInstance;

/**
 * RtAgent represents an agent during simulation time.
 * 
 * It provides access to a {@link RtExecutionContext} and the actual agent.
 * 
 * @author stephan
 *
 */
public interface RtAgent {

    /**
     * Get a specific runtime execution context.
     * 
     * @param name name of the context
     * @return the context
     */
    RtExecutionContext getExecutionContext(String name);

    /**
     * Get the names of the agent's execution contexts.
     * 
     * @return the name of the contexts
     */
    String[] getExecutionContextNames();

    /**
     * Get all execution contexts.
     * 
     * @return the contexts
     */
    RtExecutionContext[] getExecutionContexts();

    /**
     * Determines the last action executed by the agent.
     * 
     * @return name of the action
     */
	Optional<String> getLastAction();

    /**
     * Returns the agent in its current state.
     * 
     * @return the agent
     */
    AgentInstance getAgent();


}