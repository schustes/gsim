package de.s2.gsim.sim.agent;

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

	String getName();

	void pre(Simulation simulation, Messenger ref);

	void post(Simulation simulation, Messenger ref);

}