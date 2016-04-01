package de.s2.gsim.api.sim.impl.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;

import de.s2.gsim.api.impl.EnvironmentWrapper;
import de.s2.gsim.api.sim.agent.impl.ApplicationAgentImpl;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.GenericAgent;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.AppAgent;
import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.sim.DataHandler;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.Simulation;
import de.s2.gsim.sim.SimulationId;
import de.s2.gsim.sim.Steppable;
import de.s2.gsim.sim.agent.RtAgent;
import de.s2.gsim.sim.behaviour.SimAction;
import de.s2.gsim.sim.communication.AgentType;
import de.s2.gsim.sim.engine.common.RuntimeAgentFactory;
import de.s2.gsim.sim.engine.common.SimpleClassLoader;

/**
 * Local implementation, and runs for the most part as one would expect from a standalone simulation.
 *
 */
public class ModelCoordinatorLocal implements Simulation, Steppable {

	private static Logger logger = Logger.getLogger(ModelCoordinatorLocal.class);

	private HashMap<String, RuntimeAgent> agents = new HashMap<String, RuntimeAgent>();

	private HashMap<String, ApplicationAgentImpl> appAgents = new HashMap<String, ApplicationAgentImpl>();

	private HashMap<String, DataHandler> dataHandlers = new HashMap<String, DataHandler>();

	private Environment env;

	private SimulationId id = null;

	private LocalMessenger messenger;

	private String ns = null;

	private Map<String, String> pauseIntervals = null;

	private Map<String, Object> props = new HashMap<String, Object>();

	private List<String> removeList = new ArrayList<String>();

	private long time = 0;

	private double totalDuration = 0;

	public ModelCoordinatorLocal(Environment env, Map<String, Object> props) {

		this.props = props;
		this.env = env;

		try {
			pauseIntervals = env.getAgentIntervals();
			id = new SimulationId(env.getNamespace());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			ns = env.getNamespace() + "/" + getId().toString();
			// this.agentOrder = env.getAgentOrdering();

			ArrayList<AgentType> r = new ArrayList<AgentType>();

			String[] path = (String[]) props.get("jars");
			SimpleClassLoader cl = new SimpleClassLoader(path);
			RuntimeAgentFactory factory = new RuntimeAgentFactory(cl);
			SimAction.putCL(env.getNamespace(), cl);

			logger.debug("===== Free memory before agent-create: " + Runtime.getRuntime().freeMemory() / 1024d);

			RuntimeAgent[] a = factory.createAgentsWithRulebase(env, getId().toString(), props);

			for (RuntimeAgent element : a) {
				r.add(element);
				if (element.inheritsFrom("GP")) {
				}
				agents.put(element.getName(), element);
			}

			logger.debug("===== Free memory after agent-create: " + Runtime.getRuntime().freeMemory() / 1024d);

			List<ApplicationAgentImpl> ap = factory.createAppAgents(env);

			dataHandlers = (HashMap<String, DataHandler>) factory.createDataHandlers(env);

			for (ApplicationAgentImpl app : ap) {
				r.add(app);
				appAgents.put(app.getName(), app);
			}
			messenger = new LocalMessenger(r);
        } catch (GSimDefException e) {
			logger.error("Def-exception", e);
		} catch (Exception e2) {
			logger.error("Exception", e2);
		}

		for (RuntimeAgent mr : agents.values()) {
			mr.setMessagingComponent(messenger);
		}
		for (ApplicationAgentImpl mr : appAgents.values()) {
			mr.setMessengerRef(messenger);
			mr.setCoordinatorRef(this);
		}

	}

	// public void addAgent(RuntimeAgent agent) {
	// agents.put(agent.getName(), agent);
	// }

	public void addAppAgent(ApplicationAgentImpl obj) {
		appAgents.put(obj.getName(), obj);
	}

	@Override
	public String addNewAgentToRunningModel(String agentClass, String name, int method, double svar) throws GSimEngineException {
		try {
			GenericAgent a = null;
			if (name == null) {
				env.instanciateAgent(env.getAgentSubClass(agentClass), "new-" + agentClass + "-" + new Random().nextInt(), method, svar);
			} else {
				a = env.instanciateAgent(env.getAgentSubClass(agentClass), name, method, svar);
			}

			String[] path = (String[]) props.get("jars");
			SimpleClassLoader cl = new SimpleClassLoader(path);
			RuntimeAgentFactory f = new RuntimeAgentFactory(cl);
			// HashMap agentRtMappings = this.env.getRuntimeRoleMappings();
			// HashMap agentMappings = this.env.getAgentMappings();
			String simId = env.getNamespace() + "/" + id;
			RuntimeAgent ra = f.createAgentWithRulebase(a, /* agentRtMappings, agentMappings, */ agents.size(), simId, props);
			messenger.addAgentToHandle(ra);
			ra.setMessagingComponent(messenger);
			agents.put(ra.getName(), ra);
			return ra.getName();
		} catch (Exception e) {
			throw new GSimEngineException(e);
		}
	}

	@Override
	public void destroy() {
		agents.clear();
		appAgents.clear();
	}

	@Override
	public int getAgentCount() throws GSimEngineException {
		return agents.size();
	}

	@Override
	public String[] getAgentNames() {
		Iterator iter = agents.values().iterator();
		ArrayList<String> list = new ArrayList<String>();
		while (iter.hasNext()) {
			RuntimeAgent a = (RuntimeAgent) iter.next();
			list.add(a.getName());
		}
		String[] res = new String[list.size()];
		list.toArray(res);
		return res;
	}

	@Override
	public RtAgent getAgent(String agentName) {
		return agents.get(agentName);
	}

	@Override
	public AppAgent[] getAppAgents() {
		Iterator iter = appAgents.values().iterator();
		ArrayList<ApplicationAgentImpl> list = new ArrayList<ApplicationAgentImpl>();
		while (iter.hasNext()) {
			ApplicationAgentImpl a = (ApplicationAgentImpl) iter.next();
			list.add(a);
		}
		ApplicationAgentImpl[] res = new ApplicationAgentImpl[list.size()];
		list.toArray(res);
		return res;
	}

	@Override
	public AppAgent getAppAgent(String name) {
		AppAgent a = appAgents.get(name);
		return a;
	}

	public double getAverageStepTime() {
		return 0;
	}

	@Override
	public DataHandler getDataHandler(String name) {
		DataHandler a = dataHandlers.get(name);
		return a;
	}

	@Override
	public DataHandler[] getDataHandlers() {
		DataHandler[] a = new DataHandler[dataHandlers.size()];
		dataHandlers.values().toArray(a);
		return a;
	}

	@Override
	public ModelDefinitionEnvironment getDefinitionEnvironment() throws GSimEngineException {
		EnvironmentWrapper impl = new EnvironmentWrapper(env);
		return impl;
	}

    public Environment getEnvironment() throws GSimEngineException, Exception {
		return env;
	}

	@Override
	public List<RtAgent> getAllAgents() throws GSimEngineException {
		Iterator iter = agents.values().iterator();
		ArrayList<RtAgent> list = new ArrayList<RtAgent>();
		while (iter.hasNext()) {
			RuntimeAgent a = (RuntimeAgent) iter.next();
			list.add(a);
		}
		return list;
	}

	@Override
	public List<RtAgent> getAllAgents(int count, int offset) throws GSimEngineException {

		ArrayList<RuntimeAgent> allList = new ArrayList<RuntimeAgent>();

		ArrayList<RtAgent> list = new ArrayList<RtAgent>();

		if (offset > agents.size()) {

			return list;
		}

		allList.addAll(agents.values());

		int toIndex = allList.size() - offset < count ? allList.size() : count + offset;
		List<RuntimeAgent> subList = allList.subList(offset, toIndex);

		Iterator iter = subList.iterator();

		while (iter.hasNext() && list.size() < count) {
			RuntimeAgent a = (RuntimeAgent) iter.next();
			list.add(a);
		}
		return list;

	}

	@Override
	public SimulationId getId() {
		return id;
	}

	public String getNamespace() {
		return ns;
	}

	@Override
	public String getNameSpace() {
		return ns;
	}

	public void init(Environment env) {
	}

	public boolean isMaster() {
		return true;
	}

	// TODO pass better an AgentInstance
	@Override
	public void replaceAgent(RtAgent agentState) throws GSimEngineException {
		AgentInstance agentInstance = agentState.getAgent();
		RuntimeAgent agent = agents.get(agentInstance.getName());
		for (String list : agentInstance.getObjectListNames()) {
			for (ObjectInstance inst : agentInstance.getObjects(list)) {
                agent.setChildInstance(list, (Instance) inst);
			}
		}
		for (String list : agentInstance.getAttributeListNames()) {
			for (Attribute a : agentInstance.getAttributes(list)) {
				agent.setAttribute(list, a);
			}
		}
	}

	@Override
	public void postStep() {
		postStepActivities();
		for (RuntimeAgent a : agents.values()) {
			a.post();
		}
		// if agents have been removed, remove them
		while (removeList.size() > 0) {
			String s = removeList.remove(0);
			agents.remove(s);
		}
	}

	@Override
	public void preStep(long time) {
		this.time = time;

		Iterator iter = agents.values().iterator();
		while (iter.hasNext()) {
			RuntimeAgent a = (RuntimeAgent) iter.next();
			a.setTime((int) time);
			a.initStep();
		}

		preStepActivities();

	}

	@Override
	public void removeAgent(String agentName) {
		removeList.add(agentName);
	}

	@Override
	public void setNameSpace(String ns) {
		this.ns = ns;
	}

	@Override
	public void step() {

		logger.debug("==================== STEP " + time + " ====================");
		double l = System.currentTimeMillis();

		HashMap<String, Object> map = createGlobals();
		Iterator iter = agents.values().iterator();

		iter = agents.values().iterator();

		List<String> ordered = orderRoles();
		for (String roleName : ordered) {
			int interval = Integer.parseInt(getPauseInterval(roleName));
			if ((time) % interval == 0) {
				iter = agents.values().iterator();
				while (iter.hasNext()) {
					RuntimeAgent a = (RuntimeAgent) iter.next();
					a.execute(map, roleName);
				}
			}
		}

		double l2 = System.currentTimeMillis();
		totalDuration += (l2 - l) / 1000d;

		double avg = totalDuration / (time + 1);
		logger.info("==== Simulation " + ns + " : STEP " + time + " - DURATION was: " + (l2 - l) / 1000d + " seconds (average: " + avg
				+ ") ====================");
	}

	@Override
	public void step(String roleName) {
		Iterator iter = agents.values().iterator();
		HashMap<String, Object> map = createGlobals();
		map.put("INTERVAL", getPauseInterval(roleName));

		int interval = Integer.parseInt(getPauseInterval(roleName));
		if ((time) % interval == 0) {
			while (iter.hasNext()) {
				RuntimeAgent a = (RuntimeAgent) iter.next();
				a.init(map);
			}
			while (iter.hasNext()) {
				RuntimeAgent a = (RuntimeAgent) iter.next();
				a.execute(map, roleName);
			}
		}
	}

	private void postStepActivities() {
		long l = System.currentTimeMillis();
		Iterator iter = appAgents.values().iterator();
		while (iter.hasNext()) {
			ApplicationAgentImpl e = (ApplicationAgentImpl) iter.next();
			e.setCoordinatorRef(this);// !!!
			e.setMessengerRef(messenger);
			e.post();
		}

		iter = dataHandlers.values().iterator();
		while (iter.hasNext()) {
			DataHandler e = (DataHandler) iter.next();

			try {
				e.save(this);
			} catch (Exception ex) {
				logger.error("Error during getting database connection", ex);
			}
		}

		logger.debug("*********** POST: " + (System.currentTimeMillis() - l) / 1000d);
	}

	private void preStepActivities() {
		long l = System.currentTimeMillis();
		Iterator iter = appAgents.values().iterator();
		while (iter.hasNext()) {
			ApplicationAgentImpl e = (ApplicationAgentImpl) iter.next();
			e.setCoordinatorRef(this);// !!!
			e.setMessengerRef(messenger);
			e.pre(props);
		}
		logger.debug("*********** PRE: " + (System.currentTimeMillis() - l) / 1000d);
	}

	private HashMap<String, Object> createGlobals() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("AGENT_COUNT", String.valueOf(agents.size()));
		map.put("TIME", time);
		return map;
	}

	/*
	 * private String[] getOrderedContexts() { String[] ordered = new String[this.agentOrder.size()];
	 * 
	 * Iterator iter = this.agentOrder.keySet().iterator(); while (iter.hasNext()) { String name = (String) iter.next(); int order = (Integer)
	 * this.agentOrder.get(name) - 1; ordered[order] = name; } return ordered; }
	 */
	private String getPauseInterval(String role) {
		try {
			if (pauseIntervals.containsKey(role)) {
				String s = (String) pauseIntervals.get(role);
				if (s.equals("?*agent-count*")) {
					return String.valueOf(agents.size());
				} else {
					return s;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "1";
	}

	private List<String> orderRoles() {
		ArrayList<String> ordered = new ArrayList<String>();
		ArrayList<String> allAgents = new ArrayList<String>();
		try {
			HashMap<String, Integer> defined = env.getAgentOrdering();
			ArrayList<Integer> al = new ArrayList<Integer>(defined.values());
			Collections.sort(al);
			for (int o : al) {
				for (Entry<String, Integer> e : defined.entrySet()) {
					if (e.getValue().equals(o)) {
						if (!ordered.contains(e.getKey())) {
							ordered.add(e.getKey());
						}
					}
				}
			}
			for (String s : allAgents) {
				if (!ordered.contains(s)) {
					ordered.add(s);
				}
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
		return ordered;
	}

	@Override
	public int getCurrentTimeStep() {
		// TODO Auto-generated method stub
		return 0;
	}

}
