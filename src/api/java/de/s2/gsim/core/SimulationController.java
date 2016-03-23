package de.s2.gsim.core;

import de.s2.gsim.sim.engine.Simulation;
import de.s2.gsim.sim.engine.SimulationID;

/**
 * Base interface for Simulation control interfaces.
 * 
 * @author Stephan
 *
 */
public interface SimulationController {

    /**
     * Removes all listeners.
     * 
     * @throws GSimException
     */
    void clearListeners() throws GSimException;

    /**
     * Gets the simulation ids of the simulations instances currently running for the model.
     * 
     * @return List of SimulationID
     * @throws GSimException
     */
    SimulationID[] getSimulationInstances() throws GSimException;

    /**
     * Returns the current state of a model being run in a simulation.
     * 
     * @param uid id of the simulation
     * @return ModelState an object containing information about the current state of the model
     * @throws GSimException
     */
    Simulation getModelState(SimulationID uid) throws GSimException;

    /**
     * Starts a model run by starting all simulation instances.
     * 
     * @throws GSimException
     */
    void start() throws GSimException;

    /**
     * Pauses the model run by pausing all simulation instances.
     * 
     * @throws GSimException
     */
    void pause() throws GSimException;

    /**
     * Resumes a model run by resuming all simulation instances.
     * 
     * @throws GSimException
     */
    void resume() throws GSimException;

    /**
     * Shuts down a model run and all its simulations.
     * 
     * @throws GSimException
     */
    void shutdown() throws GSimException;

    /**
     * Registers a listener. The ScenarioManager informs listeners about events such as the end of a simulation.
     * 
     * @param listener the listener
     * @throws GSimException
     */
    void registerSimulationListener(SimulationListener listener) throws GSimException;

    /**
     * Removes a single listener.
     * 
     * @param listener the listener
     * @throws GSimException
     */
    void unregisterSimulationListener(SimulationListener listener) throws GSimException;

    /**
     * Disconnects the controller from the simulation engine, freeing up any sort of communication resources possibly bound to it.
     *
     * @throws GSimException
     */
    void disconnect() throws GSimException;

    /**
     * Gets the current time step of a simulation.
     * 
     * @param id simulation id
     * @return long the step
     * @throws GSimException
     */
    long getTime(SimulationID id) throws GSimException;

}
