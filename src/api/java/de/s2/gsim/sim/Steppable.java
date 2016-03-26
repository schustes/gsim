package de.s2.gsim.sim;

/**
 * Steppable is an interface that allows a {@link SimulationScheduler} to proceed a {@link Simulation} stepwise. Specific simulation engine
 * implementations (e.g. stand alone or distributed) provide their own ways of handling this. By implementing this interface, they expose the control
 * of these implementation to external callers.
 * 
 * @author stephan
 *
 */
public interface Steppable {

    /**
     * Destroys this Steppable.
     */
    void destroy();

    /**
     * Get the simulation id that is handled by this Steppable.
     * 
     * @return the simulation id
     */
    SimulationId getId();

    /**
     * Get the name space this Steppable belongs to.
     * 
     * @return the name space string
     */
    String getNameSpace();

    /**
     * Command to execute actions after all agents have acted.
     */
    void postStep();

    /**
     * Command to execute actions before all agents begin to act in the given time step.
     * 
     * @param time the time step
     */
    void preStep(long time);

    /**
     * Command to execute all agent actions.
     */
    void step();

    /**
     * Command to execute all agents actions that inherit from the given role (AgentClass name).
     * 
     * @param roleName the role to execute
     */
    void step(String roleName);

}
