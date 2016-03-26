package de.s2.gsim.sim.agent;

import java.util.Map;

import de.s2.gsim.sim.Simulation;
import de.s2.gsim.sim.communication.Messenger;

/**
 * An application agent is a special kind of agent that can access the simulation as a whole. It can be called at certain times of a simulation,
 * namely at the beginning of the end of a time step.
 * 
 * @author stephan
 *
 */
public interface ApplicationAgent {

    /**
     * Gets a reference to the simulation the agent is running in.
     * 
     * @return the {@link Simulation}
     */
    Simulation getSimulation();

    /**
     * Gets a reference to the messenger for dispatching messages to other agents running in the simulation.
     * 
     * @return a {@link Messenger} reference
     */
    Messenger getMessengerRef();

    /**
     * Gets the name of the applicaton agent.
     * 
     * @return the name
     */
    String getName();

    /**
     * Gets the namespace identifier of the simulation the agent is running in.
     * 
     * @return the namespace
     */
    String getNameSpace();

    /**
     * Called by the simulation after all agents in the simulation have acted in a time step.
     */
    void post();

    /**
     * Called by the simulation before all agents in the simulation act in the current time step.
     */
    void pre(Map<String, Object> simProps);

}