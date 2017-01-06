package de.s2.gsim.sim;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;

import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.environment.Generator;
import de.s2.gsim.objects.Action;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.Expansion;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.RLActionNode;
import de.s2.gsim.objects.Rule;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
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
	static final String VAL_LIST = "attr-list-2";
	static final String AGENT_NAME = "test";
	static final String AGENT_CLASS_NAME = "test-class";

	static double rewardAction0 = 100;
	static double rewardAction1 = 20;
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

	@Test
	public void bra_expansion_numerical() throws Exception {

		int samples = 1;
		int steps = 10;
		int expandInterval=2;
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

		rule.createEvaluator(ATTR_LIST + "/" + EVAL_ATTR, alpha);
		agentClass.setBehaviour(behaviour);
		
		DomainAttribute interval1 = new DomainAttribute("wealth", AttributeType.INTERVAL);
		interval1.addFiller("0");
		interval1.addFiller("10");
		interval1.setDefault("4");
		agentClass.addAttribute(ATTR_LIST, interval1);

		Expansion expansion = rule.createExpansion(ATTR_LIST+"/wealth", "0", "10");
		expansion.setMin("0");
		expansion.setMax("10");
		expansion.addFiller("0");
		expansion.addFiller("10");
		rule.addOrSetExpansion(expansion);
		behaviour.setMaxNodes(3);
		behaviour.setRevaluationProb(0.3);
		behaviour.setUpdateInterval(expandInterval);
		behaviour.setRevisitCostFraction(0.5);
		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);

		double expectedIntuitive = steps * 0.7;
		
		for (int i=0; i<samples; i++) {
			double counterAction = runRLsimulation(agent, alpha, steps);
			assertThat("Count of test action must be significantly over 2/3 of all actions or so", expectedIntuitive, lessThanOrEqualTo(counterAction));
		}

	}

	@Test
	public void bra_expansion_categorical_and_numerical() throws Exception {

		int samples = 1;
		int steps = 100;
		int expandInterval=2;
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
		
		DomainAttribute cat = new DomainAttribute("Letters", AttributeType.SET);
		cat.addFiller("A");
		cat.addFiller("B");
		cat.addFiller("C");
		cat.setDefault("B");
		agentClass.addAttribute(VAL_LIST, cat);

		Expansion expansion = rule.createExpansion(VAL_LIST + "/Letters", new String[] { "A", "B", "C" });
		rule.addOrSetExpansion(expansion);

		DomainAttribute interval1 = new DomainAttribute("wealth", AttributeType.INTERVAL);
		interval1.addFiller("0");
		interval1.addFiller("10");
		interval1.setDefault("4");
		agentClass.addAttribute(VAL_LIST, interval1);
		Expansion expansion2 = rule.createExpansion(VAL_LIST + "/wealth", "0", "10");
		expansion2.setMin("0");
		expansion2.setMax("10");
		expansion2.addFiller("0");
		expansion2.addFiller("10");
		rule.addOrSetExpansion(expansion2);

		behaviour.setMaxNodes(10);
		behaviour.setRevaluationProb(0.9);
		behaviour.setUpdateInterval(expandInterval);
		behaviour.setRevisitCostFraction(0);

		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);

		for (int i=0; i<samples; i++) {
			double counterAction = runRLsimulation(agent, alpha, steps);
			System.out.println("COUNT: " + counterAction);
		//	assertThat("Count of test action must be significantly over 2/3 of all actions or so", expectedIntuitive, lessThanOrEqualTo(counterAction));
		}
	}
	@Test
	public void bra_expansion_categorical() throws Exception {

		int samples = 1;
		int steps = 10;
		int expandInterval=2;
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
		
		DomainAttribute cat = new DomainAttribute("Letters", AttributeType.SET);
		cat.addFiller("A");
		cat.addFiller("B");
		cat.addFiller("C");
		agentClass.addAttribute(ATTR_LIST, cat);

		Expansion expansion = rule.createExpansion(ATTR_LIST+"/Letters", new String[] {"A", "B", "C"});
		rule.addOrSetExpansion(expansion);
		behaviour.setMaxNodes(3);
		behaviour.setRevaluationProb(0.3);
		behaviour.setUpdateInterval(expandInterval);
		behaviour.setRevisitCostFraction(0.5);
		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);

		for (int i=0; i<samples; i++) {
			double counterAction = runRLsimulation(agent, alpha, steps);
		//	assertThat("Count of test action must be significantly over 2/3 of all actions or so", expectedIntuitive, lessThanOrEqualTo(counterAction));
		}
	}

	@Test
	public void bra_expansion_should_append_new_category() throws Exception {

		int samples = 1;
		int steps = 500;
		int expandInterval=2;
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
		
		DomainAttribute cat = new DomainAttribute("Letters", AttributeType.SET);
		cat.addFiller("A");
		cat.addFiller("B");
		cat.addFiller("C");
		cat.addFiller("D");
		agentClass.addAttribute(ATTR_LIST, cat);
		
		Action action = behaviour.createAction("Test-Action", AddNewCatAction.class.getName());
		Rule rrule = behaviour.createRule("Test-Rule");
		rrule.addOrSetConsequent(action);
		rrule.createCondition(ATTR_LIST + "/" + COUNTER0, ">", "4");

		Expansion expansion = rule.createExpansion(ATTR_LIST+"/Letters", new String[] {"A", "B", "C"});
		rule.addOrSetExpansion(expansion);
		behaviour.setMaxNodes(10);
		behaviour.setRevaluationProb(0.3);
		behaviour.setUpdateInterval(expandInterval);
		behaviour.setRevisitCostFraction(0.5);
		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);
		
		

		for (int i=0; i<samples; i++) {
			double counterAction = runRLsimulation(agent, alpha, steps);
			//get handle of runtime agent, check fact base
			//check: Action on for sf with sf-cat X is executed most of the time
		//	assertThat("Count of test action must be significantly over 2/3 of all actions or so", expectedIntuitive, lessThanOrEqualTo(counterAction));
		}
	}

	/**
	 * Tests enlarges the attribute used for state description in the first steps from 1 to 10. As a result, the bra tree should expand the
	 * lower half of the state up to some time around 10, after that consquently switch to the tree for the upper bound, deleting the
	 * original subtree of the first steps.
	 * 
	 * @throws Exception
	 */
	@Test
	public void bra_expansion_should_extend_interval() throws Exception {

		int samples = 1;
		int steps = 200;
		int expandInterval = 2;
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

        ObjectClass objectClass = env.createObjectClass("test", null);

        agentClass.addOrSetObject("object-list", objectClass);

		DomainAttribute interval1 = new DomainAttribute("wealth", AttributeType.INTERVAL);
		interval1.addFiller("0");
		interval1.addFiller("1");
		interval1.setDefault("0.5");
        objectClass.addAttribute(ATTR_LIST, interval1);
        // agentClass.addAttribute(ATTR_LIST, interval1);

		// Expansion expansion = rule.createExpansion("object-list/test::" + ATTR_LIST + "/wealth", "0", "10");
		Expansion expansion = rule.createExpansion("object-list/test/" + ATTR_LIST + "/wealth", "0", "1");
		// expansion.setMin("0");
		// expansion.setMax("1");
		// expansion.addFiller("0");
		// expansion.addFiller("10");
		rule.addOrSetExpansion(expansion);

		Action action = behaviour.createAction("Test-Action", AddNewNumberAction.class.getName());
		Rule rrule = behaviour.createRule("Test-Rule");
		rrule.addOrSetConsequent(action);
		// rrule.createCondition(ATTR_LIST + "/" + COUNTER0, ">", "4");

		behaviour.setMaxNodes(20);
		behaviour.setRevaluationProb(0.25);
		behaviour.setUpdateInterval(expandInterval);
		behaviour.setContractInterval(expandInterval * 5);
		behaviour.setRevisitCostFraction(0.5);
		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);

		for (int i = 0; i < samples; i++) {
			double counterAction = runRLsimulation(agent, alpha, steps);
			// get handle of runtime agent, check fact base
			// check: Action on for sf with sf-cat X is executed most of the time
			// assertThat("Count of test action must be significantly over 2/3 of all actions or so", expectedIntuitive,
			// lessThanOrEqualTo(counterAction));
		}
	}


	@SuppressWarnings("unused")
	private void scientific_rl_rule_test() throws Exception {

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

			double started;

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
				if (step == 1) {
					started = System.currentTimeMillis();
				}
				double stepDuration = (System.currentTimeMillis() - started) / 1000d;
				started = System.currentTimeMillis();
				System.out.println(uid + " -- Duration (" + step + "): " + stepDuration);
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

			shuffleAttributes(agent);

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

			shuffleAttributes(agent);

			return null;			
		}

	}

	public static class TestAction1a extends SimAction {
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

			shuffleAttributes(agent);
			
			NumericalAttribute a = (NumericalAttribute) agent.resolvePath(Path.attributePath("object-list", "test", ATTR_LIST, "wealth"));
			a.setValue(new Random().doubles(1, 20, 30).reduce(0, (x, y) -> x));
			return null;			
		}

	}

	public static class AddNewCatAction extends SimAction {
		private static final long serialVersionUID = 1L;
		public Object execute() {
			RuntimeAgent agent = super.getContext().getAgent();
			SetAttribute categories = (SetAttribute)agent.getAttribute("Letters");
			categories.removeAllEntries();
			categories.addEntry("X");
			agent.addOrSetAttribute(ATTR_LIST, categories);
			return null;			
		}

	}

	static int av = 0;
	public static class AddNewNumberAction extends SimAction {
		private static final long serialVersionUID = 1L;

		public Object execute() {

			RuntimeAgent agent = super.getContext().getAgent();

			Path<Attribute> p = Path.attributePath("object-list", "test", ATTR_LIST, "wealth");
			NumericalAttribute a = (NumericalAttribute) agent.resolvePath(p);
			double current = a.getValue();

			if (av == 2) {
				current = -2;
			}

			if (av == 3) {
				current = 0;
			}

			if (av % 3 == 0 && av < 15) {
				current += 2;
			}

			if (av == 15) {
				current = current - 0.001;
			}

			a.setValue(current);
			// a.setValue(new Random().doubles(1, 20, 30).reduce(0, (x, y) -> y));
			agent.replaceChildAttribute(p, a);

			av++;

			return null;
		}

	}

	private static void shuffleAttributes(RuntimeAgent agent) {
		Generator.randomiseUniformDistributedAttributeValues(agent, VAL_LIST);
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
