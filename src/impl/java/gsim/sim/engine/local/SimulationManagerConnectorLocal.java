package gsim.sim.engine.local;

import de.s2.gsim.core.ScenarioListener;
import de.s2.gsim.sim.engine.GSimEngineException;
import de.s2.gsim.sim.engine.ModelState;
import de.s2.gsim.sim.engine.SimulationID;
import de.s2.gsim.sim.engine.SimulationManagerConnector;

public class SimulationManagerConnectorLocal implements SimulationManagerConnector {

    @Override
    public SimulationID[] getInstances(String ns) throws GSimEngineException {
        return SimulationInstanceContainerLocal.getInstance(ns).getInstances();
    }

    @Override
    public ModelState getSVM(String ns, SimulationID id) throws GSimEngineException {
        return SimulationInstanceContainerLocal.getInstance(ns).getModelState(id);
    }

    @Override
    public long getTime(String ns, SimulationID id) throws GSimEngineException {
        return SimulationInstanceContainerLocal.getInstance(ns).getTime(id);
    }

    @Override
    public boolean isFinished(String ns) throws GSimEngineException {
        return SimulationInstanceContainerLocal.getInstance(ns).isFinished();
    }

    @Override
    public void pause(String ns) throws GSimEngineException {
        SimulationInstanceContainerLocal.getInstance(ns).pause();
    }

    public void registerSimulatonListener(String ns, ScenarioListener l) throws GSimEngineException {
        SimulationInstanceContainerLocal.getInstance(ns).registerSimulationListener(l);
    }

    @Override
    public void resume(String ns) throws GSimEngineException {
        SimulationInstanceContainerLocal.getInstance(ns).resume();
    }

    @Override
    public void shutdown(String ns) throws GSimEngineException {
        SimulationInstanceContainerLocal.getInstance(ns).shutdown();
    }

}
