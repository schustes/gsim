package de.s2.gsim.sim;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;



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
import de.s2.gsim.objects.Rule;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;
import de.s2.gsim.sim.agent.RtAgent;
import de.s2.gsim.sim.behaviour.SimAction;

public class SimTest {

	ModelDefinitionEnvironment env;

	static final String ATTR_NAME = "attr-1";
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
	public void sim_action_should_printout_teststring() throws Exception {

		env.createAgentClass(AGENT_CLASS_NAME, null);
		AgentClass agentClass = env.getAgentClass(AGENT_CLASS_NAME);

		DomainAttribute att = new DomainAttribute(ATTR_NAME,AttributeType.STRING);
		String oldValue = "Hello world";
		att.setDefault(oldValue);
		agentClass.addAttribute(ATTR_LIST, att);

		Behaviour behaviour = agentClass.getBehaviour();
		Action action = behaviour.createAction("Test-Action", TestAction.class.getName());
		Rule rule = behaviour.createRule("Test-Rule");
		rule.addOrSetConsequent(action);
		agentClass.setBehaviour(behaviour);

		AgentInstance agent = env.instanciateAgent(agentClass, AGENT_NAME);

		SimulationController m = core.createScenarioManager(env, new HashMap<String,Object>(), 1, 1);
		SimulationId id = m.getSimulationInstances()[0];
		Simulation sim = m.getModelState(id);
		RtAgent rt = sim.getAgent(AGENT_NAME);
		
		String before = rt.getAgent().getAttribute(ATTR_NAME).toValueString();
		
		blockUntilSimulationFinished(m);

		String after = rt.getAgent().getAttribute(ATTR_NAME).toValueString();

		assertThat("Simulation changed agent state", after, not(equalTo(before)));
		assertThat("New value is " + TestAction.newValue, TestAction.newValue, equalTo(after));
		
		assertThat("Agent definition has still old value " + oldValue, ((StringAttribute)agent.getAttribute(ATTR_NAME)).getValue(), equalTo(oldValue));

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
				Simulation sim = m.getModelState(SimulationId.valueOf(uid));
				RtAgent rt = sim.getAgent(AGENT_NAME);
				String before = rt.getAgent().getAttribute(ATTR_NAME).toValueString();
				System.out.println("on-step: " + before);

			}
			
			@Override
			public void instanceFinished(String uid) {

				Simulation sim = m.getModelState(SimulationId.valueOf(uid));
				RtAgent rt = sim.getAgent(AGENT_NAME);
				String before = rt.getAgent().getAttribute(ATTR_NAME).toValueString();
				System.out.println("on-finish: " + before);

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

		static String newValue = "Mofidied";
		
		public Object execute() {
			RuntimeAgent agent = super.getContext().getAgent();
			StringAttribute instanciated = (StringAttribute)agent.getAttribute(ATTR_NAME);
			instanciated.setValue(newValue);
			agent.addOrSetAttribute(ATTR_LIST, instanciated);
			return null;			
		}
	}

}
