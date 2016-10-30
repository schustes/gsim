package de.s2.gsim.sim;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;

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

public class SimTest {

	ModelDefinitionEnvironment env;

	static final String EVAL_ATTR = "eval";
	static final String ATTR_NAME_1 = "attr-1";
	static final String ATTR_NAME_2 = "attr-2";
	static final String ATTR_LIST = "attr-list-1";
	static final String AGENT_NAME = "test";
	static final String AGENT_CLASS_NAME = "test-class";

	GSimCore core;

	@Before
	public void setupEnv() {
		core = GSimCoreFactory.defaultFactory().createCore();
		env = core.create("test", new HashMap<>());
	}
	
	@Test
	public void rl_rule_should_fire() throws Exception {
		env.createAgentClass(AGENT_CLASS_NAME, null);
		
		AgentClass agentClass = createBaseTestAgent();

		Behaviour behaviour = agentClass.getBehaviour();
		Action action1 = behaviour.createAction("Test-Action1", TestAction.class.getName());
		Action action2 = behaviour.createAction("Test-Action2", TestAction.class.getName());
		RLActionNode rule = behaviour.createRLActionNode("RL");
		
		
		//TODO does this work now?
		//SelectionNode n1 = rule.createSelectionNode("N1");
		rule.addOrSetConsequent(action1);
		rule.addOrSetConsequent(action2);
		//rule.addOrSetSelectionNode(n1);

		rule.createEvaluator(EVAL_ATTR, "=", "0");
		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);

		verifyAttributeChange(agent, 1);
	}
	
	@Test
	public void deterministic_rule_should_fire() throws Exception {

		env.createAgentClass(AGENT_CLASS_NAME, null);
		
		AgentClass agentClass = createBaseTestAgent();

		Behaviour behaviour = agentClass.getBehaviour();
		Action action = behaviour.createAction("Test-Action", TestAction.class.getName());
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
		assertThat("New value is " + TestAction.newValue, TestAction.newValue, equalTo(after));
		
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

	public static class TestAction extends SimAction {
		private static final long serialVersionUID = 1L;

		static String newValue = "Test1";
		
		public Object execute() {
			RuntimeAgent agent = super.getContext().getAgent();
			StringAttribute instanciated = (StringAttribute)agent.getAttribute(ATTR_NAME_1);
			instanciated.setValue(newValue);
			agent.addOrSetAttribute(ATTR_LIST, instanciated);
			
			NumericalAttribute eval = (NumericalAttribute)agent.getAttribute(EVAL_ATTR);
			eval.setValue(eval.getValue()+1);
			agent.addOrSetAttribute(ATTR_LIST, eval);
			
			return null;			
		}
	}

	public static class TestAction2 extends SimAction {
		private static final long serialVersionUID = 1L;

		static String newValue = "Test2";
		
		public Object execute() {
			RuntimeAgent agent = super.getContext().getAgent();
			StringAttribute instanciated = (StringAttribute)agent.getAttribute(ATTR_NAME_1);
			instanciated.setValue(newValue);
			agent.addOrSetAttribute(ATTR_LIST, instanciated);
			

			
			return null;			
		}
	}
}
