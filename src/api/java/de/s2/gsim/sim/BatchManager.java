package de.s2.gsim.sim;

/**
 * A BatchManager is responsible for executing a program on behalf of the modeller.
 * 
 * @author Stephan
 *
 */
public interface BatchManager {

    /**
     * Starts the execution of the simulation.
     * 
     * @throws GSimEngineException
     */
    public void start() throws GSimEngineException;

}
