package gsim.core.impl;

import java.util.ArrayList;
import java.util.HashMap;

import de.s2.gsim.core.GSimException;
import de.s2.gsim.core.SimulationController;
import de.s2.gsim.core.SimulationListener;
import de.s2.gsim.sim.engine.GSimEngineException;
import de.s2.gsim.sim.engine.Simulation;
import de.s2.gsim.sim.engine.SimulationID;
import de.s2.gsim.sim.engine.SimulationManager;
import de.s2.gsim.sim.engine.SimulationManagerConnector;
import gsim.def.Environment;
import gsim.sim.engine.local.SimulationInstanceContainerLocal;
import gsim.sim.engine.local.SimulationManagerConnectorLocal;

public class ScenarioManagerLocal implements SimulationController {

    private SimulationManagerConnector connector = null;

    private ArrayList<SimulationListener> listeners = new ArrayList<SimulationListener>();

    private SimulationManager manager = null;

    private String ns = null;

    public ScenarioManagerLocal(Environment env, HashMap props, int steps, int runs) {
        try {
            SimulationInstanceContainerLocal manager = new SimulationInstanceContainerLocal(env.getNamespace(), props, steps, runs);
            manager.init(env);
            this.manager = manager;
            ns = env.getNamespace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ScenarioManagerLocal(String ns) {
        connector = new SimulationManagerConnectorLocal();
        this.ns = ns;
    }

    @Override
    public void clearListeners() {
        System.out.println("ScenarioManagerLocal.unregister:I think I can do nothing here");
    }

    @Override
    public void disconnect() {
        listeners.clear();
    }

    @Override
    public SimulationID[] getSimulationInstances() {
        try {
            if (manager != null) {
                return manager.getInstances();
            } else {
                return connector.getInstances(ns);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Simulation getModelState(SimulationID uid) {
        try {
            if (manager != null) {
                return manager.getModelState(uid);
            } else {
                return connector.getSVM(uid.getGlobalNS(), uid);
            }
        } catch (Exception e) {
            throw new RuntimeException("No SVM " + uid + " found");
        }
    }

    @Override
    public long getTime(SimulationID id) {
        try {
            if (manager != null) {
                return manager.getTime(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;

    }

    @Override
    public void pause() {
        try {
            if (manager != null) {
                manager.pause();
            } else {
                connector.pause(ns);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerSimulationListener(SimulationListener l) throws GSimException {
        try {
            if (manager instanceof SimulationInstanceContainerLocal) {
                SimulationInstanceContainerLocal m = (SimulationInstanceContainerLocal) manager;
                m.registerSimulationListener(l);
            } else {
                SimulationManagerConnectorLocal m = (SimulationManagerConnectorLocal) connector;
                m.registerSimulatonListener(ns, l);
            }
        } catch (GSimEngineException e) {
            throw new GSimException(e);
        }
    }

    @Override
    public void resume() {
        try {
            if (manager != null) {
                manager.resume();
            } else {
                connector.resume(ns);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            if (manager != null) {
                manager.shutdown();
                manager = null;
            } else {
                connector.shutdown(ns);
                connector = null;
            }
            listeners.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            if (manager != null) {
                manager.start();
            } else {
                throw new RuntimeException("You have connected to a running simulation and cannot start it again!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregisterSimulationListener(SimulationListener l) {
        System.out.println("ScenarioManagerLocal.unregister:I think I can do nothing here");
    }

}
