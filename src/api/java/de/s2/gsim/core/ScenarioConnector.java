package de.s2.gsim.core;

import de.s2.gsim.sim.engine.SimulationID;

/**
 * A ScenarioConnector is similar to a <code>SimulatioManager</code>, but it can connect to the simulation instance without knowing the reference. In
 * a distributed environment, this usually happens via JMS communication.
 * 
 * @author Stephan
 *
 */
public interface ScenarioConnector extends ScenarioBase {

    /**
     * Disconnects from the simulation engine.
     *
     * @throws GSimException
     */
    public void disconnect() throws GSimException;

    /**
     * Gets the current time step of a simulation.
     * 
     * @param id simulation id
     * @return long the step
     * @throws GSimException
     */
    public long getTime(SimulationID id) throws GSimException;

}
