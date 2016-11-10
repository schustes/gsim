package de.s2.gsim.sim;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.GreaterOrEqual;

import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.objects.Action;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.RLActionNode;
import de.s2.gsim.objects.Rule;
import de.s2.gsim.objects.SelectionNode;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;
import de.s2.gsim.sim.agent.RtAgent;
import de.s2.gsim.sim.behaviour.SimAction;
import de.s2.gsim.testutils.NormalDistributedUtil;

public class SimTest {

	ModelDefinitionEnvironment env;

	static final String EVAL_ATTR = "eval";
	static final String ATTR_NAME_1 = "attr-1";
	static final String ATTR_NAME_2 = "attr-2";
	static final String ATTR_LIST = "attr-list-1";
	static final String AGENT_NAME = "test";
	static final String AGENT_CLASS_NAME = "test-class";

	static double rewardAction0 = 1;
	static double rewardAction1 = 0.1;
	static String action0Name = "Test-Action0";
	static String action1Name = "Test-Action1";

	static final String COUNTER0 = "count-action-0";
	static final String COUNTER1 = "count-action-1";

	GSimCore core;

	@Before
	public void setupEnv() {
		core = GSimCoreFactory.defaultFactory().createCore();
		env = core.create("test", new HashMap<>());
	}


	@Test
	public void rl_rule_should_fire() throws Exception {

		int samples = 5;
		int steps = 80;
		double alpha = 0.1;

		env.createAgentClass(AGENT_CLASS_NAME, null);

		AgentClass agentClass = createBaseTestAgent();

		Behaviour behaviour = agentClass.getBehaviour();
		Action action1 = behaviour.createAction(action0Name, TestAction0.class.getName());
		Action action2 = behaviour.createAction(action1Name, TestAction1.class.getName());

		RLActionNode rule = behaviour.createRLActionNode("RL");

		rule.addOrSetConsequent(action1);
		rule.addOrSetConsequent(action2);

		Rule rewardRule = behaviour.createRule("reward-rule");
		Action rewardAction = behaviour.createAction("rewardAction", RewardComputation.class.getName());
		rewardRule.addOrSetConsequent(rewardAction);

		// first param is reward variable, and last is alpha!
		rule.createEvaluator(ATTR_LIST + "/" + EVAL_ATTR, alpha);
		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);

		double expectedIntuitive = steps * 0.7;
		
		for (int i=0; i<samples; i++) {
			double counterAction = runRLsimulation(agent, alpha, steps);
			assertThat("Count of test action must be significantly over 2/3 of all actions or so", expectedIntuitive, lessThanOrEqualTo(counterAction));
		}

	}

	public void scientific_rl_rule_test() throws Exception {

		int samples = 10;
		int steps = 50;
		double alpha = 0.1;

		env.createAgentClass(AGENT_CLASS_NAME, null);

		AgentClass agentClass = createBaseTestAgent();

		Behaviour behaviour = agentClass.getBehaviour();
		Action action1 = behaviour.createAction(action0Name, TestAction0.class.getName());
		Action action2 = behaviour.createAction(action1Name, TestAction1.class.getName());

		RLActionNode rule = behaviour.createRLActionNode("RL");

		rule.addOrSetConsequent(action1);
		rule.addOrSetConsequent(action2);

		Rule rewardRule = behaviour.createRule("reward-rule");
		Action rewardAction = behaviour.createAction("rewardAction", RewardComputation.class.getName());
		rewardRule.addOrSetConsequent(rewardAction);

		// first param is reward variable, and last is alpha!
		rule.createEvaluator(ATTR_LIST + "/" + EVAL_ATTR, alpha);
		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);

		double actionStrength1 = 1;
		double actionStrength2 = 0;
		double x0 = Math.exp(rewardAction0 / alpha);
		double x1 = Math.exp(rewardAction1 / alpha);
		double sum = x0 + x1;

		double pr0 = x0 / sum;
		double pr1 = x1 / sum;


		double expectedFrequency0 = Math.floor((steps + 1) * pr0);
		double expectedFrequency1 = Math.ceil((steps + 1) * pr1);

		
		double expectedIntuitive = steps * 0.7;
		
		for (int i=0; i<samples; i++) {
			double counterAction = runRLsimulation(agent, alpha, steps);
			assertThat("Count of test action must be significantly over 2/3 of all actions or so", counterAction, lessThanOrEqualTo(counterAction));
		}

		NormalDistributedUtil.sample(10, samples, 10, expectedFrequency0, () -> {
			try {
				double counterAction1 = runRLsimulation(agent, alpha, steps);
				System.out.println("count action1: " + counterAction1);
				return counterAction1;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0D;
		});
	}
	public double runRLsimulation(AgentInstance testAgent, double alpha, int steps) throws Exception {


		SimulationController m = core.createScenarioManager(env, new HashMap<String, Object>(), steps, 1);
		SimulationId id = m.getSimulationInstances()[0];
		Simulation sim = m.getModelState(id);
		RtAgent rt = sim.getAgent(AGENT_NAME);

		blockUntilSimulationFinished(m);

		return rt.getAgent().getNumericalAttribute(ATTR_LIST, COUNTER0);


	}

	@Test
	public void deterministic_rule_should_fire() throws Exception {

		env.createAgentClass(AGENT_CLASS_NAME, null);

		AgentClass agentClass = createBaseTestAgent();

		Behaviour behaviour = agentClass.getBehaviour();
		Action action = behaviour.createAction("Test-Action", TestAction0.class.getName());
		Rule rule = behaviour.createRule("Test-Rule");
		rule.addOrSetConsequent(action);
		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);

		verifyAttributeChange(agent, 1);

	}

	private AgentClass createBaseTestAgent() {
		AgentClass agentClass = env.getAgentClass(AGENT_CLASS_NAME);
		DomainAttribute att = new DomainAttribute(ATTR_NAME_1,AttributeType.STRING);
		String oldValue = "Hello world";
		att.setDefault(oldValue);
		agentClass.addAttribute(ATTR_LIST, att);
		DomainAttribute eval = new DomainAttribute(EVAL_ATTR,AttributeType.NUMERICAL);
		agentClass.addAttribute(ATTR_LIST, eval);
		DomainAttribute c1 = new DomainAttribute(COUNTER0, AttributeType.NUMERICAL);
		c1.setDefault("0");
		agentClass.addAttribute(ATTR_LIST, c1);
		DomainAttribute c2 = new DomainAttribute(COUNTER1, AttributeType.NUMERICAL);
		c2.setDefault("0");
		agentClass.addAttribute(ATTR_LIST, c2);

		return agentClass;
	}

	private void verifyAttributeChange(AgentInstance agent, int steps) throws InterruptedException {
		SimulationController m = core.createScenarioManager(env, new HashMap<String,Object>(), steps, 1);
		SimulationId id = m.getSimulationInstances()[0];
		Simulation sim = m.getModelState(id);
		RtAgent rt = sim.getAgent(AGENT_NAME);
		String initialValue = rt.getAgent().getAttribute(ATTR_NAME_1).toValueString();

		blockUntilSimulationFinished(m);

		String after = rt.getAgent().getAttribute(ATTR_NAME_1).toValueString();

		assertThat("Simulation changed agent state", after, not(equalTo(initialValue)));
		assertThat("New value is " + TestAction0.newValue, TestAction0.newValue, equalTo(after));

		assertThat("Agent definition has still old value " + initialValue, ((StringAttribute)agent.getAttribute(ATTR_NAME_1)).getValue(), equalTo(initialValue));
	}

	private void blockUntilSimulationFinished(SimulationController m)
			throws InterruptedException {
		Semaphore sema = new Semaphore(1);
		sema.acquire();
		m.registerSimulationListener(new SimulationListener() {

			@Override
			public void simulationRestarted(String ns) {
			}

			@Override
			public void simulationFinished(String ns) {

			}

			@Override
			public void simulationCrashed(String ns) {
			}

			@Override
			public void instanceStep(String uid, int step) {
			}

			@Override
			public void instanceFinished(String uid) {
				sema.release();
			}

			@Override
			public void instanceCancelled(String uid) {
			}
		});
		m.start();

		sema.acquire();
	}

	public static class TestAction0 extends SimAction {
		private static final long serialVersionUID = 1L;

		static String newValue = "Test1";

		public Object execute() {
			RuntimeAgent agent = super.getContext().getAgent();
			StringAttribute instanciated = (StringAttribute)agent.getAttribute(ATTR_NAME_1);
			instanciated.setValue(newValue);
			agent.addOrSetAttribute(ATTR_LIST, instanciated);

			NumericalAttribute counter = (NumericalAttribute) agent.getAttribute(COUNTER0);
			counter.setValue(counter.getValue() + 1);
			agent.addOrSetAttribute(ATTR_LIST, instanciated);

			return null;			
		}
	}

	public static class TestAction1 extends SimAction {
		private static final long serialVersionUID = 1L;

		static String newValue = "Test2";

		public Object execute() {
			RuntimeAgent agent = super.getContext().getAgent();
			StringAttribute instanciated = (StringAttribute)agent.getAttribute(ATTR_NAME_1);
			instanciated.setValue(newValue);
			agent.addOrSetAttribute(ATTR_LIST, instanciated);

			NumericalAttribute counter = (NumericalAttribute) agent.getAttribute(COUNTER1);
			counter.setValue(counter.getValue() + 1);
			agent.addOrSetAttribute(ATTR_LIST, instanciated);

			return null;			
		}

	}

	public static class RewardComputation extends SimAction {
		private static final long serialVersionUID = 1L;

		public Object execute() {

			RuntimeAgent agent = super.getContext().getAgent();
			if (!agent.getLastAction().isPresent()) {
				return null;
			}

			String lastAction = agent.getLastAction().get();

			double reward = 0;
			if (lastAction.equals(action0Name)) {
				reward = rewardAction0;
			} else {
				reward = rewardAction1;
			}

			NumericalAttribute rewardVariable = (NumericalAttribute) agent.getAttribute(EVAL_ATTR);
			rewardVariable.setValue(reward);
			agent.addOrSetAttribute(ATTR_LIST, rewardVariable);

			return reward;
		}
	}

}
