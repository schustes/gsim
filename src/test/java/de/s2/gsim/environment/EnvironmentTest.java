package de.s2.gsim.environment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.internal.matchers.GreaterOrEqual;

import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;

public class EnvironmentTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void non_existing_agent_framelist_with_nonterminal_path_should_throw_exception() throws Exception {

        Path<List<DomainAttribute>> path = Path.attributeListPath("test", "test");

        expected.expect(GSimDefException.class);
		expected.expectMessage("Path " + path + " does not exist and is not terminal, so no list can be created!");

        Environment env = new Environment("test");
        AgentClassOperations agentOperations = env.getAgentClassOperations();

        GenericAgentClass a = agentOperations.createAgentSubclass("Test agent class", agentOperations.getGenericAgentClass());
        agentOperations.addAgentClassAttribute(a, path, new DomainAttribute("test", AttributeType.STRING));


    }


    @Test
    public void verify_adding_agent_classes() {

        Environment env = new Environment("test");

        AgentClassOperations agentOperations = env.getAgentClassOperations();

        GenericAgentClass a = agentOperations.createAgentSubclass("Test agent class", agentOperations.getGenericAgentClass());

        assertThat("Agent inherits from top class", a.getParentFrames().get(0), equalTo(agentOperations.getGenericAgentClass()));

        assertThat("Agent count is 1", agentOperations.getAgentSubClasses().size(), equalTo(1));

    }

    @Test
    public void verify_find_agent_classes() {

        Environment env = new Environment("test");

        AgentClassOperations agentOperations = env.getAgentClassOperations();

        for (int i = 0; i < 1000; i++) {
            agentOperations.createAgentSubclass(String.valueOf(i), agentOperations.getGenericAgentClass());
        }

        double start = System.currentTimeMillis();
        GenericAgentClass found = agentOperations.getAgentSubClass("50");
        double total = System.currentTimeMillis() - start;
        System.out.println("Duration find: " + (total / 1000d));

        assertThat("Agent class was found", found, notNullValue());
        assertThat("Correct agent class was found", found.getName(), equalTo("50"));

    }

    @Test
    public void agentclass_not_found_throws_nosuch_element_exception() {

        expected.expect(NoSuchElementException.class);
        expected.expectMessage("No value present");

        Environment env = new Environment("test");

        AgentClassOperations agentOperations = env.getAgentClassOperations();

        for (int i = 0; i < 10; i++) {
            agentOperations.createAgentSubclass(String.valueOf(i), agentOperations.getGenericAgentClass());
        }

        agentOperations.getAgentSubClass("50");

    }

    @Test
    public void verify_inheritance() {

        Environment env = new Environment("test");

        AgentClassOperations agentOperations = env.getAgentClassOperations();

        GenericAgentClass p0 = agentOperations.createAgentSubclass("p0", agentOperations.getGenericAgentClass());
        p0.defineAttributeList("p0-attributes");
        Frame f = Frame.newFrame("p0-TestFrame");
        DomainAttribute a = new DomainAttribute("p0-attribute", AttributeType.STRING);
        p0.defineObjectList("p0-framelist", f);
        a.setDefault("p0-attr");
        p0.addOrSetAttribute("p0-attributes", a);

        f.addOrSetAttribute("p0-child-attributes", a);
        p0.addOrSetChildFrame("p0-framelist", f);
        DomainAttribute astroke = f.getAttribute("p0-child-attributes", a.getName());
        astroke.setDefault("c0");
        f.addOrSetAttribute("p0-child-attributes", astroke);
        p0.addOrSetChildFrame("p0-framelist", f);

        String test1AttrVal = p0.getAttribute("p0-attributes", a.getName()).getDefaultValue();
        String test2AttrVal = p0.<DomainAttribute> resolvePath(Path.attributePath("p0-framelist", "p0-TestFrame", "p0-child-attributes", a.getName()))
                .getDefaultValue();

        assertThat("By-value", false, equalTo(test1AttrVal.equals(test2AttrVal)));

        GenericAgentClass p1 = agentOperations.createAgentSubclass("p1", p0);
        a.setDefault("over");
        p1.addOrSetAttribute("p0-attributes", a);
        test1AttrVal = p0.getAttribute("p0-attributes", a.getName()).getDefaultValue();
        test2AttrVal = p1.getAttribute("p0-attributes", a.getName()).getDefaultValue();
        System.out.println(test1AttrVal + "::" + test2AttrVal);
        assertThat("No backpropagation of overloaded child attributes", test1AttrVal, not(equalTo(test2AttrVal)));

    }

    @Test
    public void verify_agent_instanciation_normal_distributed_attributes() {
        Environment env = new Environment("test");

        double numericalVal = 5d;

        AgentInstanceOperations operations = env.getAgentInstanceOperations();
        AgentClassOperations agentClassOperations = env.getAgentClassOperations();
        GenericAgentClass agentClass = agentClassOperations.createAgentSubclass("agent", agentClassOperations.getGenericAgentClass());
        DomainAttribute a = new DomainAttribute("attribute", AttributeType.NUMERICAL);
        a.setDefault(String.valueOf(numericalVal));
        agentClass.addOrSetAttribute("attributes", a);

        double svar = 0.5;
        int popSize = 50;
        double err = 2.576 * (svar / (Math.sqrt((double) popSize)));// 99%
        // 95%=1,645
        int countExpected = 0;
        int samples = 100;

        for (int i = 0; i < samples; i++) {
            List<GenericAgent> population = operations.instanciateAgentsWithNormalDistributedAttributes(agentClass, Optional.empty(), svar, popSize);
            double sum = 0;
            for (GenericAgent agent : population) {
                sum += ((NumericalAttribute) agent.getAttribute("attribute")).getValue();
            }
            double avg = sum / popSize;

            if (avg <= (numericalVal + err) && avg >= (numericalVal - err)) {
                countExpected++;
            }

        }

        double minimumExpectedCorrectSamples = ((double) samples) * 0.99;
		assertThat("Distribution in expected confidence interval ", (double) countExpected,
				greaterThanOrEqualTo(minimumExpectedCorrectSamples));

    }

    @Test
    public void verify_adding_object_classes() {

        Environment env = new Environment("test");

        ObjectClassOperations objectOperations = env.getObjectClassOperations();

        Frame subFrame = objectOperations.createObjectSubClass("TestObject", objectOperations.getObjectClass());

        assertThat("Sub object class inherits from top class", subFrame.getParentFrames().get(0), equalTo(objectOperations.getObjectClass()));

        assertThat("Object count is 1", objectOperations.getObjectSubClasses().size(), equalTo(1));

    }

    @Test
    public void verify_delete_agent() {

        expected.expect(NoSuchElementException.class);

        Environment env = new Environment("test");
        AgentClassOperations agentOperations = env.getAgentClassOperations();
        GenericAgentClass sub = agentOperations.createAgentSubclass("TestSub", null);
        agentOperations.removeAgentClass(sub);

        agentOperations.getAgentSubClass(sub.getName());

    }

    @Test
    public void verify_behaviour_frame_inheritance() {

        Environment env = new Environment("test");

        AgentClassOperations agentOperations = env.getAgentClassOperations();
        GenericAgentClass base = agentOperations.createAgentSubclass("Base", null);
        GenericAgentClass sub = agentOperations.createAgentSubclass("Sub", base);

        BehaviourFrame baseBehaviour = base.getBehaviour();

        ActionFrame action = ActionFrame.newActionFrame("base-action", "com.test.action1");
        UserRuleFrame urf = UserRuleFrame.newUserRuleFrame("rule-1");
        ConditionFrame cf = ConditionFrame.newConditionFrame("a", ">", "b");
        
        baseBehaviour.addAction(action);
        urf.addCondition(cf);
        urf.addConsequence(action);
        baseBehaviour.addOrSetRule(urf);
        
        env.getAgentClassOperations().changeAgentClassBehaviour(base, baseBehaviour);

        BehaviourFrame subBehaviour = sub.getBehaviour();
        UserRuleFrame subRule = subBehaviour.getRule("rule-1");
        assertThat("Subrule is present in child agent after parent was modified", subRule, notNullValue());
        
        UserRuleFrame newRule = subBehaviour.createRule("rule-1");
        ConditionFrame newCondition = newRule.createCondition("a", "<", "b");
        newRule.addCondition(newCondition);
        newRule.addConsequence(action);
        subBehaviour.addOrSetRule(newRule);
        
        env.getAgentClassOperations().changeAgentClassBehaviour(sub, subBehaviour);
        
        UserRuleFrame pRule = env.getAgentClassOperations().getAgentSubClass("Base").getBehaviour().getRule("rule-1");
        UserRuleFrame sRule = env.getAgentClassOperations().getAgentSubClass("Sub").getBehaviour().getRule("rule-1");
        
        assertThat("Overridden rule is retrieved", sRule.getConditions()[0].getOperator(), equalTo("<"));
        assertThat("Parent rule is not modified", pRule.getConditions()[0].getOperator(), equalTo(">"));
    }
    
    @Test
    public void verify_behaviour_instance_inheritance() {

        Environment env = new Environment("test");

        AgentClassOperations agentOperations = env.getAgentClassOperations();
        GenericAgentClass base = agentOperations.createAgentSubclass("Base", null);

        BehaviourFrame baseBehaviour = base.getBehaviour();

        ActionFrame action = ActionFrame.newActionFrame("base-action", "com.test.action1");
        UserRuleFrame urf = UserRuleFrame.newUserRuleFrame("rule-1");
        ConditionFrame cf = ConditionFrame.newConditionFrame("a", ">", "b");
        
        baseBehaviour.addAction(action);
        urf.addCondition(cf);
        urf.addConsequence(action);
        baseBehaviour.addOrSetRule(urf);
        
        env.getAgentClassOperations().changeAgentClassBehaviour(base, baseBehaviour);
        
        GenericAgent agent = env.getAgentInstanceOperations().instanciateAgentWithUniformDistributedAttributes(base, "Instance");
        
        UserRule ruleInstance = agent.getBehaviour().getRule("rule-1");
        
        assertThat("Instance rule is available", ruleInstance.getConditions()[0].getOperator(), equalTo(">"));
        
        urf.addCondition(ConditionFrame.newConditionFrame("a", "<", "c"));
        baseBehaviour.addOrSetRule(urf);
        
        env.getAgentClassOperations().changeAgentClassBehaviour(base, baseBehaviour);
        
        agent = env.getAgentInstanceOperations().getAgent("Instance");
        ruleInstance = agent.getBehaviour().getRule("rule-1");
        
        assertThat("Instance rule is modified", ruleInstance.getConditions().length, equalTo(2));
        
    }

    @Test
    public void verify_propagation_of_child_frames() {

    }

}
