package de.s2.gsim.sim.engine;

public interface Steppable {

    public void destroy() throws GSimEngineException;

    public SimulationID getId() throws GSimEngineException;

    public String getNameSpace() throws GSimEngineException;

    public void postStep() throws GSimEngineException;

    public void preStep(long time) throws GSimEngineException;

    public void step() throws GSimEngineException;

    public void step(String roleName) throws GSimEngineException;

}
