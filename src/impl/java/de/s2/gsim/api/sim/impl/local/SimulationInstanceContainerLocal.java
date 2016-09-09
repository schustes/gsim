package de.s2.gsim.api.sim.impl.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import de.s2.gsim.environment.Environment;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.Simulation;
import de.s2.gsim.sim.SimulationController;
import de.s2.gsim.sim.SimulationId;
import de.s2.gsim.sim.SimulationListener;
import de.s2.gsim.sim.SimulationScheduler;
import de.s2.gsim.sim.Steppable;

/**
 * Executes one model instance at a time, i.e. all repetitions are processed in sequence.
 */
public class SimulationInstanceContainerLocal implements SimulationController {

	private static Semaphore blockingSema = new Semaphore(1);

	private static int instanceCountLimit = 10;

	private static HashMap<String, SimulationInstanceContainerLocal> instances = new HashMap<String, SimulationInstanceContainerLocal>();

	private static Logger logger = Logger.getLogger(SimulationInstanceContainerLocal.class);

	private int counter = 0;

	private Environment env = null;

	private List<SimulationId> finished = new ArrayList<SimulationId>();

	private Set<SimulationListener> listeners = new HashSet<SimulationListener>();

	private Map<Listener, Simulation> listening = new ConcurrentHashMap<Listener, Simulation>();

	private String ns;

	private Map<String, Object> props = new HashMap<String, Object>();

	private int runs = 0;

	private Map<SimulationId, SimulationScheduler> schedulers = new ConcurrentHashMap<SimulationId, SimulationScheduler>();

	private Map<SimulationId, Simulation> sims = new ConcurrentHashMap<SimulationId, Simulation>();

	private int steps = 0;

	public SimulationInstanceContainerLocal(Environment env, String ns, Map<String, Object> props, int steps, int runs) {
		try {
			this.steps = steps;
			this.runs = runs;
			this.props = props;
			this.ns = ns;
			this.env = (Environment) env.clone();
			instances.put(this.ns, this);
			init();
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	public SimulationId[] getInstances() {
		SimulationId[] ids = new SimulationId[sims.size()];
		sims.keySet().toArray(ids);
		return ids;
	}

	@Override
	public Simulation getModelState(SimulationId uid) {
		return sims.get(uid);
	}

	@Override
	public long getTime(SimulationId uid) throws GSimEngineException {
		SimulationScheduler c = schedulers.get(uid);

		if (c != null) {
			try {
				return c.getCurrentTime();
			} catch (Exception e) {
				throw new GSimEngineException(e);
			}
		}

		return -999;
	}

	public boolean isFinished() {
		return (counter == steps) && (listening.size() == 0);
	}

	@Override
	public void pause() throws GSimEngineException {
		try {
			for (SimulationScheduler c : schedulers.values()) {
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
			for (SimulationScheduler c : schedulers.values()) {
				c.resume();
			}
		} catch (Exception e) {
			throw new GSimEngineException(e);
		}
	}

	@Override
	public void shutdown() throws GSimEngineException {

		for (SimulationScheduler c : schedulers.values()) {
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

			schedulers.values().stream().parallel().forEach(sc->sc.run(steps));
			
		} catch (Exception e) {
			throw new GSimEngineException("Unhandled exception", e);
		}
	}

	private void init() throws GSimEngineException {
		try {

			for (int i=0;i<runs;i++ ) {
				ModelCoordinatorLocal mc = new ModelCoordinatorLocal(env, props);
				StandaloneScheduler c = new StandaloneScheduler(mc);

				Listener l = new Listener();
				SimulationId uid = mc.getId();
				schedulers.put(uid, c);
				listening.put(l, mc);
				c.registerSimulationInstanceListener(l);
				sims.put(uid, mc);

				counter++;
			}

		} catch (Exception e) {
			throw new GSimEngineException("Unhandled exception", e);
		}
	}

	private void finishInstance(Listener listener, SimulationId uid) {

		Simulation instance = listening.remove(listener);

		Simulation svm = sims.get(uid);
		if (svm == instance) {

			// notify before destruction.
			logger.debug("Sim-Instance " + uid + " finished, counter: " + counter);
			notifyInstanceFinished(uid);

			try {
				((Steppable) svm).destroy();
				SimulationScheduler c = schedulers.remove(uid);
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

	private void notifyInstanceFinished(SimulationId uid) {
		for (de.s2.gsim.sim.SimulationListener l : listeners) {
			l.instanceFinished(uid.toString());
		}
	}

	private void notifyInstanceStep(SimulationId uid, int step) {
		for (de.s2.gsim.sim.SimulationListener l : listeners) {
			l.instanceStep(uid.toString(), step);
		}
	}

	private void notifySimulationFinished() {
		for (de.s2.gsim.sim.SimulationListener l : listeners) {
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

	private class Listener implements SimulationInstanceListener {

		@Override
		public void onStep(long step) {
			Simulation instance = listening.get(this);
			for (SimulationId uid : sims.keySet()) {
				Simulation svm = sims.get(uid);
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

	@Override
	public void clearListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public SimulationId[] getSimulationInstances() {
		Collection<SimulationId> ids = this.sims.keySet();
		return ids.toArray(new SimulationId[0]);
	}

	@Override
	public Steppable getSimulationScheduler(SimulationId id) {
		return this.schedulers.get(id).getSteppable();
	}

	@Override
	public void unregisterSimulationListener(SimulationListener listener) {
		this.listeners.remove(listener);
	}

}
