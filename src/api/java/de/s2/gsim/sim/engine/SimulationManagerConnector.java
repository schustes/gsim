package de.s2.gsim.sim.engine;

public interface SimulationManagerConnector {

    public SimulationID[] getInstances(String ns) throws GSimEngineException;

    public ModelState getSVM(String ns, SimulationID id) throws GSimEngineException;

    public long getTime(String ns, SimulationID id) throws GSimEngineException;

    public boolean isFinished(String ns) throws GSimEngineException;

    public void pause(String ns) throws GSimEngineException;

    public void resume(String ns) throws GSimEngineException;

    public void shutdown(String ns) throws GSimEngineException;

}
