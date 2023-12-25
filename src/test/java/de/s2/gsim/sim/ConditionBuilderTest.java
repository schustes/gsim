package de.s2.gsim.sim;

import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;
import de.s2.gsim.api.impl.EnvironmentWrapper;
import de.s2.gsim.environment.ActionFrame;
import de.s2.gsim.environment.AgentClassOperations;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.ConditionFrame;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.ObjectClassOperations;
import de.s2.gsim.environment.RLRuleFrame;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.behaviour.SimulationRuntimeAction;
import de.s2.gsim.sim.behaviour.SimulationRuntimeContext;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class ConditionBuilderTest {
    GSimCore core;
    Environment env;
    EnvironmentWrapper w;
	static int counter = 0;

	AgentClassOperations agentOperations;
	ObjectClassOperations objectClassOperations;

    @Before
    public void before() {
        core = GSimCoreFactory.defaultFactory().createCore();
        w = (EnvironmentWrapper) core.create("test", new HashMap<>());
        env = w.getComplicatedInterface();
		counter = 0;
		agentOperations = env.getAgentClassOperations();
		objectClassOperations = env.getObjectClassOperations();

    }

    @Test
	public void verify_constant_expression_matches() throws Exception {

		String matchingAttrVal = "1";
		GenericAgentClass agentClass = new AgentBuilder().createTestAgent().withEqualChildObjectsAttributeValue(matchingAttrVal).build();

		BehaviourFrame bf = agentClass.getBehaviour();

		RLRuleFrame rf = buildRule("EXISTS", matchingAttrVal);

		bf.addRLRule(rf);

		agentClass = agentOperations.changeAgentClassBehaviour(agentClass, bf);

		env.getAgentInstanceOperations().instanciateAgentWithNormalDistributedAttributes(agentClass, "test-instance", 0);
		runRLsimulation(10);
		MatcherAssert.assertThat("Action must have been executed", counter, Matchers.greaterThan(0));

	}

	@Test
	public void verify_constant_expression_no_match() throws Exception {

		String conditionAttrVal = "1";
		String actualInstanceAttrVal = "2";
		GenericAgentClass agentClass = new AgentBuilder().createTestAgent().withEqualChildObjectsAttributeValue(actualInstanceAttrVal)
		        .build();

        BehaviourFrame bf = agentClass.getBehaviour();

		RLRuleFrame rf = buildRule("EXISTS", conditionAttrVal);

        bf.addRLRule(rf);

        agentClass = agentOperations.changeAgentClassBehaviour(agentClass, bf);

        env.getAgentInstanceOperations().instanciateAgentWithNormalDistributedAttributes(agentClass, "test-instance", 0);
		runRLsimulation(10);
		MatcherAssert.assertThat("Action must not have been executed", counter, Matchers.equalTo(0));

    }

    @Test
    public void verify_variable_expression() throws Exception {

		GenericAgentClass agentClass = new AgentBuilder().createTestAgent().withEqualChildObjectsAttributeValue().build();

        BehaviourFrame bf = agentClass.getBehaviour();

		RLRuleFrame rf = buildRule("EXISTS", "list2/obj2/obj1AttrList/obj2Attribute");

        bf.addRLRule(rf);
        agentClass = agentOperations.changeAgentClassBehaviour(agentClass, bf);

        env.getAgentInstanceOperations().instanciateAgentWithNormalDistributedAttributes(agentClass, "test-instance", 0);
        runRLsimulation(10);
		MatcherAssert.assertThat("Action must have been executed", counter, Matchers.greaterThan(0));

    }

	@Test
	public void verify_constant_negated_expression() throws Exception {

		GenericAgentClass agentClass = new AgentBuilder().createTestAgent().build();

		BehaviourFrame bf = agentClass.getBehaviour();
		RLRuleFrame rf = buildRule("NOT EXISTS", "1");
		bf.addRLRule(rf);
		agentClass = agentOperations.changeAgentClassBehaviour(agentClass, bf);

		env.getAgentInstanceOperations().instanciateAgentWithNormalDistributedAttributes(agentClass, "test-instance", 0);
		runRLsimulation(10);
		MatcherAssert.assertThat("Action must have been executed", counter, Matchers.greaterThan(0));

	}

	@Test
	public void verify_variable_negated_expression() throws Exception {

		GenericAgentClass agentClass = new AgentBuilder().createTestAgent().build();

		BehaviourFrame bf = agentClass.getBehaviour();
		
		RLRuleFrame rf = buildRule("NOT EXISTS", "list2/obj2/obj1AttrList/obj2Attribute");
		
		bf.addRLRule(rf);

		agentClass = agentOperations.changeAgentClassBehaviour(agentClass, bf);

		env.getAgentInstanceOperations().instanciateAgentWithNormalDistributedAttributes(agentClass, "test-instance", 0);
		runRLsimulation(10);
		MatcherAssert.assertThat("Action must have been executed", counter, Matchers.greaterThan(0));

	}

	private RLRuleFrame buildRule(String op, String value) {
		RLRuleFrame rf = RLRuleFrame.newRLRuleFrame("Test");
		ConditionFrame cf = ConditionFrame.newConditionFrame("list1/obj1/obj1AttrList/obj1Attribute", op, value);
		rf.addCondition(cf);
		rf.addConsequence(ActionFrame.newActionFrame("action", TestAction.class.getName()));
		rf.addCondition(cf);
		ConditionFrame eval = ConditionFrame.newConditionFrame("list1/obj1/obj1AttrList/obj1Attribute", op, "0.1");
		rf.setEvaluationFunction(eval);
		return rf;
	}

	private class AgentBuilder {
		private GenericAgentClass agentClass;


		Frame obj1;
		Frame obj2;

		public AgentBuilder createTestAgent() {

			agentClass = agentOperations.createAgentSubclass("p0", agentOperations.getGenericAgentClass());
			obj1 = objectClassOperations.createObjectSubClass("obj1", objectClassOperations.getObjectClass());
			obj2 = objectClassOperations.createObjectSubClass("obj2", objectClassOperations.getObjectClass());

			agentClass = agentOperations.addObjectList(agentClass, "list1", obj1);
			agentClass = agentOperations.addObjectList(agentClass, "list2", obj2);

			DomainAttribute a1 = new DomainAttribute("agentAttribute", AttributeType.NUMERICAL);
			agentClass = agentOperations.addAgentClassAttribute(agentClass, Path.attributeListPath("agentAttrList"), a1);
			DomainAttribute a2 = new DomainAttribute("obj1Attribute", AttributeType.NUMERICAL);
			DomainAttribute a3 = new DomainAttribute("obj2Attribute", AttributeType.NUMERICAL);
			a2.setDefault("1");
			a3.setDefault("1");

			obj1 = objectClassOperations.addAttribute(obj1, Path.attributeListPath("obj1AttrList"), a2);
			obj2 = objectClassOperations.addAttribute(obj2, Path.attributeListPath("obj2AttrList"), a3);

			agentClass = agentOperations.addObjectList(agentClass, "list1", obj1);
			agentClass = agentOperations.addObjectList(agentClass, "list2", obj2);

			return this;
		}

		public AgentBuilder withEqualChildObjectsAttributeValue() {

			DomainAttribute attr1 = obj1.getAttribute("obj1Attribute");
			attr1.setDefault("1");
			obj1.addOrSetAttribute("obj1AttrList", attr1);
			DomainAttribute attr2 = obj2.getAttribute("obj2Attribute");
			attr2.setDefault("1");
			obj2.addOrSetAttribute("obj1AttrList", attr2);

			agentClass = agentOperations.addChildObject(agentClass, Path.objectListPath("list1"), obj1);
			agentClass = agentOperations.addChildObject(agentClass, Path.objectListPath("list2"), obj2);

			return this;
		}

		public AgentBuilder withEqualChildObjectsAttributeValue(String attVal) {

			DomainAttribute attr1 = obj1.getAttribute("obj1Attribute");
			attr1.setDefault(attVal);
			obj1.addOrSetAttribute("obj1AttrList", attr1);
			DomainAttribute attr2 = obj2.getAttribute("obj2Attribute");
			attr2.setDefault(attVal);
			obj2.addOrSetAttribute("obj1AttrList", attr2);

			agentClass = agentOperations.addChildObject(agentClass, Path.objectListPath("list1"), obj1);
			agentClass = agentOperations.addChildObject(agentClass, Path.objectListPath("list2"), obj2);

			return this;
		}

		public GenericAgentClass build() {
			return agentClass;
		}
	}

    public void runRLsimulation(int steps) throws Exception {

        SimulationController m = core.createScenarioManager(w, new HashMap<String, Object>(), steps, 1);

        blockUntilSimulationFinished(m);

    }

	@SuppressWarnings({ "serial" })
	public static class TestAction implements SimulationRuntimeAction {

		public TestAction() {
			super();
		}

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
		public void execute(SimulationRuntimeContext ctx) {
			counter++;
		}

	}

    private void blockUntilSimulationFinished(SimulationController m) throws InterruptedException {
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
}
