package de.s2.gsim.sim;

/**
 * Controls a single simulation instance.
 */
public interface SimulationScheduler {

    /**
     * Gets the current time step of the simulation.
     * 
     * @return the time step
     */
    long getCurrentTime();

    /**
     * Pause simulation.
     */
    void pause();

    /**
     * Continues simulation where (and if) it has been paused.
     */
    void resume();

    /**
     * Start simulation, specifying how long it runs at maximum.
     */
    void run(long t);

    /**
     * Gets the {@link Steppable} scheduled by this class.
     * 
     * @return the steppable instance
     */
    Steppable getSteppable();

    /**
     * Method to set the a {@link Steppable} that is scheduled by this class.
     * 
     * @param steppable the {@link Steppable} instance
     */
    void setSteppable(Steppable steppable);

    /**
     * End simulation.
     */
    void shutdown();

}
