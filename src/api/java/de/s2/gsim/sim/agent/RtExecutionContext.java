package de.s2.gsim.sim.agent;

/**
 * Represent an execution context. One ore more frame levels of an agent's inheritance hierarchy may be executed in its own context, so that an agent
 * inheriting different agent classes can apply the actions of these classes in separate rounds of a time steps. This allows, if desired, to separate
 * certain blocks of action. The context are defined in the environment (or XML configuration file) at definition time.
 * 
 * @author stephan
 *
 */
public interface RtExecutionContext {

    /**
     * Gets all agent class names in the inheritance hierarchy that act within this context.
     * 
     * @return an array of agent class names
     */
    String[] getDefiningAgentClasses();

    /**
     * Returns the name of this execution context.
     * 
     * @return the name
     */
    String getName();

}