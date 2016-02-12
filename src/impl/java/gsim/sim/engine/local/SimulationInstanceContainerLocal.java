package gsim.sim.engine.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import de.s2.gsim.core.SimulationListener;
import de.s2.gsim.sim.engine.Controller;
import de.s2.gsim.sim.engine.GSimEngineException;
import de.s2.gsim.sim.engine.ModelState;
import de.s2.gsim.sim.engine.SimulationID;
import de.s2.gsim.sim.engine.SimulationManager;
import de.s2.gsim.sim.engine.Steppable;
import gsim.def.Environment;

/**
 * Executes one model instance at a time, i.e. all repetitions are processed in sequence.
 */
public class SimulationInstanceContainerLocal implements SimulationManager {

    private static Semaphore blockingSema = new Semaphore(1);

    private static int instanceCountLimit = 10;

    private static HashMap<String, SimulationInstanceContainerLocal> instances = new HashMap<String, SimulationInstanceContainerLocal>();

    private static Logger logger = Logger.getLogger(SimulationInstanceContainerLocal.class);

    private int counter = 0;

    private Environment env = null;

    private ArrayList<SimulationID> finished = new ArrayList<SimulationID>();

    private HashSet<de.s2.gsim.core.SimulationListener> listeners = new HashSet<de.s2.gsim.core.SimulationListener>();

    private ConcurrentHashMap<Listener, ModelState> listening = new ConcurrentHashMap<Listener, ModelState>();

    private String ns;

    private HashMap<String, Object> props = new HashMap<String, Object>();

    private int runs = 0;

    private ConcurrentHashMap<SimulationID, Controller> schedulers = new ConcurrentHashMap<SimulationID, Controller>();

    private ConcurrentHashMap<SimulationID, ModelState> sims = new ConcurrentHashMap<SimulationID, ModelState>();

    private int steps = 0;

    public SimulationInstanceContainerLocal(String ns, HashMap<String, Object> props, int steps, int runs) {
        try {
            this.steps = steps;
            this.runs = runs;
            this.props = props;
            this.ns = ns;
            instances.put(this.ns, this);
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    @Override
    public SimulationID[] getInstances() {
        SimulationID[] ids = new SimulationID[sims.size()];
        sims.keySet().toArray(ids);
        return ids;
    }

    @Override
    public ModelState getModelState(SimulationID uid) {
        return sims.get(uid);
    }

    @Override
    public long getTime(SimulationID uid) throws GSimEngineException {
        Controller c = schedulers.get(uid);

        if (c != null) {
            try {
                return c.getCurrentTime();
            } catch (Exception e) {
                throw new GSimEngineException(e);
            }
        }

        return -999;
    }

    public void init(Environment env) {
        try {
            this.env = (Environment) env.cloneEnvironment();
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    @Override
    public boolean isFinished() {
        return (counter == steps) && (listening.size() == 0);
    }

    @Override
    public void pause() throws GSimEngineException {
        try {
            for (Controller c : schedulers.values()) {
                c.pause();
            }
        } catch (Exception e) {
            throw new GSimEngineException(e);
        }

    }

    public void registerSimulationListener(SimulationListener l) {
        listeners.add(l);
    }

    @Override
    public void resume() throws GSimEngineException {
        try {
            for (Controller c : schedulers.values()) {
                c.resume();
            }
        } catch (Exception e) {
            throw new GSimEngineException(e);
        }
    }

    @Override
    public void shutdown() throws GSimEngineException {

        for (Controller c : schedulers.values()) {
            try {
                c.shutdown();
            } catch (Exception e) {
                throw new GSimEngineException(e);
            }
            c = null;
        }

        schedulers.clear();
        sims.clear();
        listeners.clear();

        instances.remove(ns);

        logger.debug("Remaining simulation instances: " + sims.size());
        logger.debug("Remaining controller instances: " + schedulers.size());
        logger.debug("Remaining managers: " + instances.size());

    }

    @Override
    public void start() throws GSimEngineException {
        try {

            logger.debug("========== SimInstanceContainer: Free memory=" + Runtime.getRuntime().freeMemory() / (1024d * 1000) + " MB ========");

            if (SimulationInstanceContainerLocal.instances.size() > SimulationInstanceContainerLocal.instanceCountLimit) {
                logger.info("blocking until at least one simulation has finished. Limit was reached, limit is:" + instanceCountLimit);
                blockingSema.acquire();
            }

            /*
             * if (SimulationInstanceContainerLocal.instances.size() > SimulationInstanceContainerLocal.instanceCountLimit) { throw new
             * GSimEngineException( "The limit of simultaneous running models is reached (limit=" + instanceCountLimit + ")"); }
             */
            ModelCoordinatorLocal mc = new ModelCoordinatorLocal(env, props);
            StandaloneController c = new StandaloneController(mc);

            Listener l = new Listener();
            SimulationID uid = mc.getId();
            schedulers.put(uid, c);
            listening.put(l, mc);
            c.registerSimulationInstanceListener(l);
            sims.put(uid, mc);

            counter++;
            c.run(steps);

        } catch (Exception e) {
            throw new GSimEngineException("Unhandled exception", e);
        }
    }

    private void finishInstance(Listener listener, SimulationID uid) {

        ModelState instance = listening.remove(listener);

        ModelState svm = sims.get(uid);
        if (svm == instance) {

            // notify before destruction.
            logger.debug("Sim-Instance " + uid + " finished, counter: " + counter);
            notifyInstanceFinished(uid);

            try {
                ((Steppable) svm).destroy();
                Controller c = schedulers.remove(uid);
                c.shutdown();
                c = null;
            } catch (Exception e) {
                logger.error(e);
            }
            svm = null;
            finished.add(uid);
            sims.remove(uid);
            if (blockingSema.availablePermits() == 0) {
                blockingSema.release();
            }
        }

        if (counter < runs) {
            logger.debug("counter:" + counter + ", runs:" + runs);
            try {
                start();
            } catch (GSimEngineException e) {
                logger.error(e);
            }
        } else {
            logger.debug("Sim " + ns + " finished, counter= " + counter);
            notifySimulationFinished();
            // new 9.1.08
            // instances.remove(ns);
            try {
                shutdown();
            } catch (GSimEngineException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyInstanceFinished(SimulationID uid) {
        for (de.s2.gsim.core.SimulationListener l : listeners) {
            l.instanceFinished(uid.toString());
        }
    }

    private void notifyInstanceStep(SimulationID uid, int step) {
        for (de.s2.gsim.core.SimulationListener l : listeners) {
            l.instanceStep(uid.toString(), step);
        }
    }

    private void notifySimulationFinished() {
        for (de.s2.gsim.core.SimulationListener l : listeners) {
            l.simulationFinished(ns);
        }

    }

    public static SimulationInstanceContainerLocal getInstance(String ns) {
        return instances.get(ns);
    }

    public static String[] listNameSpaces() {
        String[] res = new String[instances.size()];
        instances.keySet().toArray(res);
        return res;
    }

    private class Listener implements de.s2.gsim.sim.engine.SimulationInstanceListener {

        @Override
        public void onStep(long step) {
            ModelState instance = listening.get(this);
            for (SimulationID uid : sims.keySet()) {
                ModelState svm = sims.get(uid);
                if (svm == instance) {
                    if (steps == step) {
                        finishInstance(this, uid);
                    } else {
                        notifyInstanceStep(uid, (int) step);
                    }
                }
            }
        }
    }

}
