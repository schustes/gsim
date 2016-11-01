package de.s2.gsim.api.sim.agent.impl;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import de.s2.gsim.api.objects.impl.AgentInstanceSim;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.GenericAgent;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.UserRule;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.sim.agent.ApplicationAgent;
import de.s2.gsim.sim.agent.RtAgent;
import de.s2.gsim.sim.behaviour.impl.JessHandler;
import de.s2.gsim.sim.communication.AgentType;
import de.s2.gsim.sim.communication.Communicator;

public class RuntimeAgent extends GenericAgent implements AgentType, RtAgent {

	private static Logger logger = Logger.getLogger(RuntimeAgent.class);

	protected transient Communicator commInterface = null;

	private String[] currentStrategy = new String[0];

	private HashMap<String, Object> globals = new HashMap<>();

	private String ns = null;

	private HashMap<String, RtExecutionContextImpl> roles = new HashMap<String, RtExecutionContextImpl>();

	private transient JessHandler ruleBase = null;

	private int time = 0;

	private AgentInstanceSim simInstance;

	private Optional<String> lastAction = Optional.empty();

	/**
	 * To make the agent work after calling this constructor, you have to set
	 * the rulebase with setRuleHandler(..) and add at least one runtime-role
	 * with addRuntimeRole(...) - or the agent will just wait to die.
	 * 
	 * @param agent
	 *            GenericAgent
	 */
	public static RuntimeAgent runtimeAgent(GenericAgent base, String ns) {
		GenericAgent ag = GenericAgent.from(base);
		RuntimeAgent a = new RuntimeAgent(ag);
		Instance.copy(base, a);
		a.ns = ns;
		return a;
	}

	private RuntimeAgent(GenericAgent base) {
		super(base.getName(), (GenericAgentClass) base.getDefinition());
	}

	@Override
	public void addChildInstance(String list, Instance inst) {
		super.addChildInstance(list, inst);
		if (ruleBase != null) {
			ruleBase.instanceChanged(list + "/" + inst.getDefinition().getName());
		}
	}

	public void addOrSetExecutionContext(String name, RtExecutionContextImpl r) {

		if (roles.containsKey(name)) {
			RtExecutionContextImpl c = roles.get(name);
			boolean b = true;
			for (String s : r.getDefiningAgentClasses()) {
				for (String t : c.getDefiningAgentClasses()) {
					if (t.equals(s)) {
						b = false;
						logger.debug("Agent class " + t + " was already defining context " + name + " ... ignore!!!");
						return;
					}
				}
			}
			if (b) {
				for (String t : c.getDefiningAgentClasses()) {
					r.addDefiningAgentClass(t);
				}
			}
		}

		roles.put(name, r);

	}

	public void destroy() {
		ruleBase.destroy();
		commInterface = null;
		roles = null;
		ruleBase = null;
		currentStrategy = null;
	}

	public void endEpisode() {
		try {
			ruleBase.endEpisode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void execute(HashMap<String, Object> globals, String roleName) {
		try {

			this.globals = globals;

			GenericAgentClass c = null;
			if (this.inheritsFrom(roleName)) {
				c = (GenericAgentClass) getDefinition().getAncestor(roleName);
			} else if (this.getDefinition().getName().equals(roleName)) {
				c = (GenericAgentClass) getDefinition();
			}

			if (c.getBehaviour().getDeclaredRules().length > 0 || c.getBehaviour().getDeclaredRLRules().length > 0) {
				ruleBase.executeUserRules(roleName, globals);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[] getCurrentStrategy() {
		return currentStrategy;
	}

	public RtExecutionContextImpl getExecutionContext(String name) {
		RtExecutionContextImpl c = new RtExecutionContextImpl();
		c.create(name, this, name);
		return c;
	}

	public RtExecutionContextImpl[] getExecutionContexts() {
		return roles.values().toArray(new RtExecutionContextImpl[0]);
	}

	public Communicator getMessagingComponent() {
		return commInterface;
	}

	public String getNameSpace() {
		return ns;
	}

	public HashMap<String, RtExecutionContextImpl> getRoles() {
		return roles;
	}

	public int getTime() {
		return time;
	}

	// intialise a role - this can be _within_ one time step
	public void init(HashMap<String, Object> globals) {
		this.globals = globals;
	}

	public void initAfterDeserialise() {
		if (ruleBase != null) {
			ruleBase.setOwner(this);
		}
		for (RtExecutionContextImpl c : getExecutionContexts()) {
			c.setOwner(this);
		}

	}

	// initialise the whole new time-step (e.g. for resetting all values of t-1
	// to
	// null/defaults..)
	public void initStep() {
		if (ruleBase != null) {
			ruleBase.reset();
		}
	}

	@Override
	public boolean isAgent() {
		return true;
	}

	public void post() {
	}

	@Override
	public boolean removeChildInstance(String list, String instName) {
		super.removeChildInstance(list, instName);
		ruleBase.retractConstant(list + "/" + instName);
		ruleBase.retractExecutedFact(list + "/" + instName);
		return true;
	}

	public void setCurrentStrategy(String[] actions) {
		currentStrategy = actions;
	}

	public void setMessagingComponent(Communicator a) {
		commInterface = a;
	}

	public void setNameSpace(String ns) {
		this.ns = ns;
	}

	public void setRuleHandler(JessHandler h) {
		ruleBase = h;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void startEpisode() {
		try {
			ruleBase.startEpisode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public AgentInstance toAgent() throws ClassCastException {
		return new AgentInstanceSim(this);
	}

	@Override
	public ApplicationAgent toAppAgent() throws ClassCastException {
		throw new ClassCastException("Cannot be casted, class is " + this.getClass().getName());
	}

	public void updateRewards() {
		ruleBase.updateRewards();
	}

	@Override
	public String[] getExecutionContextNames() {
		String[] r = new String[roles.size()];
		roles.keySet().toArray(r);
		return r;
	}

	@Override
	public Optional<String> getLastAction() {
		return lastAction;
	}

	@Override
	public AgentInstance getAgent() {
		if (this.simInstance == null) {
			this.simInstance = new AgentInstanceSim(this);
		}
		return simInstance;
	}

	public void setLastActionClassName(String name) {
		this.lastAction = Optional.ofNullable(findActionNameByJavaClassName(name));
	}

	private String findActionByClassName(UserRule[] rules, String clsName) {
		for (UserRule u : rules) {
			for (ActionDef action : u.getConsequents()) {
				if (action.getClassName().equals(clsName)) {
					return action.getName();
				}
			}
		}
		return null;
	}

	private String findActionNameByJavaClassName(String clsName) {

		String res = findActionByClassName(super.getBehaviour().getRules(), clsName);
		if (res == null) {
			return findActionByClassName(super.getBehaviour().getRLRules(), clsName);
		}
		return res;

	}

}
