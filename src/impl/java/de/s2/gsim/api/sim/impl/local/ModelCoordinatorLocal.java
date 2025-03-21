package de.s2.gsim.api.sim.impl.local;

import de.s2.gsim.api.impl.EnvironmentWrapper;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.GenericAgent;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.sim.DataHandler;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.Simulation;
import de.s2.gsim.sim.SimulationId;
import de.s2.gsim.sim.Steppable;
import de.s2.gsim.sim.agent.ApplicationAgent;
import de.s2.gsim.sim.agent.RtAgent;
import de.s2.gsim.sim.communication.AgentType;
import de.s2.gsim.sim.engine.common.RuntimeAgentFactory;
import de.s2.gsim.sim.engine.common.SimpleClassLoader;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Local implementation, and runs for the most part as one would expect from a standalone simulation.
 *
 */
public class ModelCoordinatorLocal implements Simulation, Steppable {

	private static Logger logger = Logger.getLogger(ModelCoordinatorLocal.class);

	private HashMap<String, RuntimeAgent> agents = new HashMap<String, RuntimeAgent>();

	private Map<String, ApplicationAgent> appAgents = new LinkedHashMap<String, ApplicationAgent>();

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
			pauseIntervals = env.getAgentRuntimeConfig().getAgentPauses();
			id = new SimulationId(env.getNamespace());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			ns = env.getNamespace() + "/" + getId().toString();

			Set<AgentType> agentTypes = new HashSet<>();

			String[] path = (String[]) props.get("jars");
			SimpleClassLoader cl = new SimpleClassLoader(path);
			RuntimeAgentFactory factory = new RuntimeAgentFactory(cl);
			//SimulationRuntimeAction.putCL(env.getNamespace(), cl);

			RuntimeAgent[] a = factory.createAgentsWithRulebase(env, getId().toString(), props);

			for (RuntimeAgent element : a) {
				agentTypes.add(element);
				agents.put(element.getName(), element);
			}

			for (ApplicationAgent applicationAgent: env.getAgentRuntimeConfig().getSystemAgents()) {
			    agentTypes.add((AgentType)applicationAgent);
            }

            this.messenger = new LocalMessenger(agentTypes);

            for (ApplicationAgent applicationAgent: env.getAgentRuntimeConfig().getSystemAgents()) {
                addApplicationAgent(applicationAgent);
            }


        } catch (GSimDefException e) {
			logger.error("Def-exception", e);
		} catch (Exception e2) {
			logger.error("Exception", e2);
		}

		for (RuntimeAgent mr : agents.values()) {
			mr.setMessagingComponent(messenger);
		}

	}

	@Override
	public String addNormallyDistributedNewAgentToRunningModel(String agentClass, String name, double svar) throws GSimEngineException {
		try {
			GenericAgent a = null;
			if (name == null) {
				a = env.getAgentInstanceOperations()
						.instanciateAgentWithNormalDistributedAttributes(env.getAgentClassOperations().getAgentSubClass(agentClass), "new-" + agentClass + "-" + new Random().nextInt(), svar);
			} else {
				a= env.getAgentInstanceOperations()
						.instanciateAgentWithNormalDistributedAttributes(env.getAgentClassOperations().getAgentSubClass(agentClass),name, svar);
			}

			return addToRuntime(a);
		} catch (Exception e) {
			throw new GSimEngineException(e);
		}
	}

	private String addToRuntime(GenericAgent a) {
		String[] path = (String[]) props.get("jars");
		SimpleClassLoader cl = new SimpleClassLoader(path);
		RuntimeAgentFactory f = new RuntimeAgentFactory(cl);
		String simId = env.getNamespace() + "/" + id;
		RuntimeAgent ra = f.createAgentWithRulebase(a, agents.size(), simId, props);
		messenger.addAgentToHandle(ra);
		ra.setMessagingComponent(messenger);
		agents.put(ra.getName(), ra);
		return ra.getName();
	}
	
	@Override
	public String addUniformDistributedNewAgentToRunningModel(String agentClass, String name) {
		try {
			GenericAgent a = null;
			if (name == null) {
				a = env.getAgentInstanceOperations()
						.instanciateAgentWithUniformDistributedAttributes(env.getAgentClassOperations().getAgentSubClass(agentClass), "new-" + agentClass + "-" + new Random().nextInt());
			} else {
				a= env.getAgentInstanceOperations()
						.instanciateAgentWithUniformDistributedAttributes(env.getAgentClassOperations().getGenericAgentClass(), name);
			}

			return addToRuntime(a);
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
	public List<ApplicationAgent> getAppAgents() {
		return new ArrayList<>(this.appAgents.values());
	}

	@Override
	public ApplicationAgent getAppAgent(String name) {
		return this.appAgents.get(name);
	}

	public double getAverageStepTime() {
		return 0;
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
	public Collection<RtAgent> getAllAgents() throws GSimEngineException {
		Iterator iter = agents.values().iterator();
		Queue<RtAgent> list = new ConcurrentLinkedQueue<>();
		while (iter.hasNext()) {
			RuntimeAgent a = (RuntimeAgent) iter.next();
			list.add(a);
		}
		return list;
	}

	@Override
	public Collection<RtAgent> getAllAgents(int count, int offset) throws GSimEngineException {

        List<RuntimeAgent> allList = new ArrayList<>();

        Collection<RtAgent> list = new ConcurrentLinkedQueue<>();

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
				agent.addOrSetAttribute(list, a);
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

		List<String> ordered = orderRoles();
		for (String roleName : ordered) {
			int interval = Integer.parseInt(getPauseInterval(roleName));
			if ((time) % interval == 0) {
				agents.values().stream().forEach(a -> {
                    a.execute(map, roleName);
                });
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
		Iterator<ApplicationAgent> iter = appAgents.values().iterator();
		while (iter.hasNext()) {
			ApplicationAgent e = iter.next();
			e.post(this, messenger);
		}

		logger.debug("*********** POST: " + (System.currentTimeMillis() - l) / 1000d);
	}

	private void preStepActivities() {
		long l = System.currentTimeMillis();
		Iterator<ApplicationAgent> iter = appAgents.values().iterator();
		while (iter.hasNext()) {
			ApplicationAgent e = iter.next();
			e.pre(this, messenger);
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
			Map<String, Integer> defined = env.getAgentRuntimeConfig().getAgentOrder();
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
	public long getCurrentTimeStep() {
		return this.time;
	}

	@Override
	public void addApplicationAgent(ApplicationAgent agent) {
		this.appAgents.put(agent.getName(), agent);
		if (agent instanceof AgentType) {
            messenger.addAgentToHandle((AgentType) agent);
        }
	}

}
