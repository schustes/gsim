package de.s2.gsim.core;

import de.s2.gsim.sim.engine.ModelState;
import de.s2.gsim.sim.engine.SimulationID;

/**
 * Base interface for Simulation control interfaces.
 * 
 * @author Stephan
 *
 */
abstract interface ScenarioBase {

    /**
     * Removes all listeners.
     * 
     * @throws GSimException
     */
    public void clearListeners() throws GSimException;

    /**
     * Gets the simulation ids of the simulations instances currently running for the model.
     * 
     * @return List of SimulationID
     * @throws GSimException
     */
    public SimulationID[] getInstances() throws GSimException;

    /**
     * Returns the current state of a simulation.
     * 
     * @param uid id of the simulation
     * @return ModelState an object containing information about the current state of the model
     * @throws GSimException
     */
    public ModelState getModelState(SimulationID uid) throws GSimException;

    /**
     * Pauses the model run by pausing all simulation instances.
     * 
     * @throws GSimException
     */
    public void pause() throws GSimException;

    /**
     * Registers a listener. The ScenarioManager informs listeners about events such as the end of a simulation.
     * 
     * @param listener the listener
     * @throws GSimException
     */
    public void registerSimulationListener(ScenarioListener listener) throws GSimException;

    /**
     * Resumes a model run by resuming all simulation instances.
     * 
     * @throws GSimException
     */
    public void resume() throws GSimException;

    /**
     * Shuts down a model run and all its simulations.
     * 
     * @throws GSimException
     */
    public void shutdown() throws GSimException;

    /**
     * Removes a single listener.
     * 
     * @param listener the listener
     * @throws GSimException
     */
    public void unregisterSimulationListener(ScenarioListener listener) throws GSimException;

}
