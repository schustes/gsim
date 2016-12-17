package de.s2.gsim.sim;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;

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

public class ConditionBuilderTest {
    GSimCore core;
    Environment env;
    EnvironmentWrapper w;

    @Before
    public void before() {
        core = GSimCoreFactory.defaultFactory().createCore();
        w = (EnvironmentWrapper) core.create("test", new HashMap<>());
        env = w.getComplicatedInterface();
    }

    @Test
    public void verify_constant_expression() throws Exception {

        AgentClassOperations agentOperations = env.getAgentClassOperations();
        ObjectClassOperations objectClassOperations = env.getObjectClassOperations();

        GenericAgentClass agentClass = createTestAgent(agentOperations, objectClassOperations);

        BehaviourFrame bf = agentClass.getBehaviour();
        RLRuleFrame rf = RLRuleFrame.newRLRuleFrame("Test");
        ConditionFrame cf = ConditionFrame.newConditionFrame("list1/obj1/obj1AttrList/obj1Attribute", "EXISTS", "1");
        rf.addCondition(cf);
        rf.addConsequence(ActionFrame.newActionFrame("action", "dummy.class"));
        rf.addCondition(cf);
        rf.setEvaluationFunction(cf);
        bf.addRLRule(rf);
        agentClass = agentOperations.changeAgentClassBehaviour(agentClass, bf);

        env.getAgentInstanceOperations().instanciateAgentWithNormalDistributedAttributes(agentClass, "test-instance", 0);
        core.createScenarioManager(w, new HashMap<>(), 10, 1).start();

    }

    @Test
    public void verify_variable_expression() throws Exception {
        AgentClassOperations agentOperations = env.getAgentClassOperations();
        ObjectClassOperations objectClassOperations = env.getObjectClassOperations();

        GenericAgentClass agentClass = createTestAgent(agentOperations, objectClassOperations);

        BehaviourFrame bf = agentClass.getBehaviour();
        RLRuleFrame rf = RLRuleFrame.newRLRuleFrame("Test");
        ConditionFrame cf = ConditionFrame.newConditionFrame("list1/obj1/obj1AttrList/obj1Attribute", "EXISTS",
                "list2/obj2/obj1AttrList/obj2Attribute");
        rf.addCondition(cf);
        rf.addConsequence(ActionFrame.newActionFrame("action", "dummy.class"));
        rf.addCondition(cf);
        rf.setEvaluationFunction(cf);
        bf.addRLRule(rf);
        agentClass = agentOperations.changeAgentClassBehaviour(agentClass, bf);

        env.getAgentInstanceOperations().instanciateAgentWithNormalDistributedAttributes(agentClass, "test-instance", 0);
        // core.createScenarioManager(w, new HashMap<>(), 10, 1).start();
        runRLsimulation(10);

    }

    private GenericAgentClass createTestAgent(AgentClassOperations agentOperations, ObjectClassOperations objectClassOperations) {
        GenericAgentClass agentClass = agentOperations.createAgentSubclass("p0", agentOperations.getGenericAgentClass());
        Frame obj1 = objectClassOperations.createObjectSubClass("obj1", objectClassOperations.getObjectClass());
        Frame obj2 = objectClassOperations.createObjectSubClass("obj2", objectClassOperations.getObjectClass());

        agentClass = agentOperations.addObjectList(agentClass, "list1", obj1);
        agentClass = agentOperations.addObjectList(agentClass, "list2", obj2);

        DomainAttribute a1 = new DomainAttribute("agentAttribute", AttributeType.NUMERICAL);
        agentClass = agentOperations.addAgentClassAttribute(agentClass, Path.attributeListPath("agentAttrList"), a1);
        DomainAttribute a2 = new DomainAttribute("obj1Attribute", AttributeType.NUMERICAL);
        DomainAttribute a3 = new DomainAttribute("obj2Attribute", AttributeType.NUMERICAL);
        obj1 = objectClassOperations.addAttribute(obj1, Path.attributeListPath("obj1AttrList"), a2);
        obj2 = objectClassOperations.addAttribute(obj2, Path.attributeListPath("obj2AttrList"), a3);

        agentClass = agentOperations.addChildObject(agentClass, Path.objectListPath("list1"), obj1);
        agentClass = agentOperations.addChildObject(agentClass, Path.objectListPath("list2"), obj2);
        return agentClass;
    }

    public void runRLsimulation(int steps) throws Exception {

        SimulationController m = core.createScenarioManager(w, new HashMap<String, Object>(), steps, 1);

        blockUntilSimulationFinished(m);

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
