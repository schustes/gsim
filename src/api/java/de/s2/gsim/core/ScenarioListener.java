package de.s2.gsim.core;

/**
 * This listener can be used to register with a ScenarioManager in order to get notfied about simulation events.
 *
 * @author Stephan
 *
 */
public interface ScenarioListener {

    /**
     * A simulation instance was cancelled from the schedule.
     * 
     * @param uid the id of the simulation instance
     */
    public void instanceCancelled(String uid);

    /**
     * A simulation instance finished.
     * 
     * @param uid the id of the simulation instance
     */
    public void instanceFinished(String uid);

    /**
     * A simulation instance finished a step.
     * 
     * @param uid the id of the simulation instance
     * @param step the time step
     */
    public void instanceStep(String uid, int step);

    /**
     * The simulation crashed (e.g. because of network failures etc.).
     * 
     * @param ns the identifier of the model
     */
    public void simulationCrashed(String ns);

    /**
     * A simulation finished (i.e. all its simulation instances completed successfully).
     * 
     * @param ns the identifier of the model
     */
    public void simulationFinished(String ns);

    /**
     * The simulation was restarted, e.g. because a former trial crashed in between.
     * 
     * @param ns the identifier of the model
     */
    public void simulationRestarted(String ns);

}
