package de.s2.gsim.core;

/**
 * The ScenarioManager is the interface between a client (local or remote) and the simulation engine. It manages the setup and the number of
 * repetitions of a model.
 * <p>
 * A model is identified by its namespace. For each model (simulation is a synonym), depending on the number of runs, several simulation instances may
 * exist at the same time. Each of them gets assigned a unique identifier, the SimulationID. With this identifier a reference to a single simulation
 * instance can be obtained.
 * <p>
 * A ScenarioManager must always reference a particular model (given by the namespace). The control methods start, stop, pause, resume operate on this
 * particular model.
 *
 */
public interface ScenarioManager extends ScenarioBase {

    /**
     * Starts a model run by starting all simulation instances.
     * 
     * @throws GSimException
     */
    public void start() throws GSimException;

}
