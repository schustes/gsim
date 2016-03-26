package de.s2.gsim.sim;

/**
 * Base interface for Simulation control interfaces.
 * 
 * @author Stephan
 *
 */
public interface SimulationController {

    /**
     * Removes all listeners.
     */
    void clearListeners();

    /**
     * Gets the simulation ids of the simulations instances currently running for the model.
     * 
     * @return List of SimulationID @
     */
    SimulationId[] getSimulationInstances();

    /**
     * Gets a particular simulation instance controller for fine grained control.
     * 
     * @param id the simulation id
     * @return a {@link Steppable} interface
     */
    Steppable getSimulationScheduler(SimulationId id);


    /**
     * Returns the current state of a model being run in a simulation.
     * 
     * @param uid id of the simulation
     * @return ModelState an object containing information about the current state of the model @
     */
    Simulation getModelState(SimulationId uid);

    /**
     * Starts a model run by starting all simulation instances.
     */
    void start();

    /**
     * Pauses the model run by pausing all simulation instances.
     */
    void pause();

    /**
     * Resumes a model run by resuming all simulation instances.
     */
    void resume();

    /**
     * Shuts down a model run and all its simulations.
     */
    void shutdown();

    /**
     * Registers a listener. The ScenarioManager informs listeners about events such as the end of a simulation.
     * 
     * @param listener the listener
     */
    void registerSimulationListener(SimulationListener listener);

    /**
     * Removes a single listener.
     * 
     * @param listener the listener
     */
    void unregisterSimulationListener(SimulationListener listener);

    /**
     * Gets the current time step of a simulation.
     * 
     * @param id simulation id
     * @return long the step
     */
    long getTime(SimulationId id);

}
