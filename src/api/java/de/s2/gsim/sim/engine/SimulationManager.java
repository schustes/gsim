package de.s2.gsim.sim.engine;

public interface SimulationManager {

    public SimulationID[] getInstances() throws GSimEngineException, Exception;

    public ModelState getModelState(SimulationID uid) throws GSimEngineException, Exception;

    public long getTime(SimulationID uid) throws GSimEngineException, Exception;

    public boolean isFinished() throws GSimEngineException, Exception;

    public void pause() throws GSimEngineException, Exception;

    public void resume() throws GSimEngineException, Exception;

    public void shutdown() throws GSimEngineException, Exception;

    public void start() throws GSimEngineException, Exception;

}
