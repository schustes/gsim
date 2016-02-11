package de.s2.gsim.sim.engine;

/**
 * Controls a single SVM instance.
 */
public interface Controller {

    public long getCurrentTime() throws GSimEngineException;

    /**
     * Pause simulation
     */
    public void pause() throws GSimEngineException;

    /**
     * Listeners for being notified about certain events
     * 
     * @param l SimulationInstanceListener
     */
    public void registerSimulationInstanceListener(SimulationInstanceListener l) throws GSimEngineException, Exception;

    /**
     * Continues simulation where (and if) it has been paused
     */
    public void resume() throws GSimEngineException;

    /**
     * Start simulation, specifying how long it runs at maximum
     */
    public void run(long t) throws GSimEngineException;

    /**
     * Method to set the SVM that is scheduled by this class
     * 
     * @param svm ISVM
     */
    public void setSteppable(Steppable svm) throws GSimEngineException;

    /**
     * End simulation. This will usually destroy the SVM and its state connected with it.
     */
    public void shutdown() throws GSimEngineException;

}
