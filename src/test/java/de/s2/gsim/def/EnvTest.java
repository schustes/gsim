package de.s2.gsim.def;

import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;
import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.AgentClassSim;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.objects.Action;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.Condition;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class EnvTest {

    ModelDefinitionEnvironment env;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    static GSimCore core = GSimCoreFactory.defaultFactory().createCore();

    @Before
    public void setupEnv() {
       // core = GSimCoreFactory.customFactory("Standalone").createCore();
        env = core.create("test", new HashMap<>());
    }

    @Test
    public void destroy_agent_removes_wrapper() throws Exception {
        String agentName = "Test";
        AgentClass a = env.createAgentClass(agentName, null);
        assertThat("agent class exists", env.getAgentClass(a.getName()), notNullValue());
        a.destroy();
        assertThat("agent class deleted", env.getAgentClass(agentName), nullValue());
    }

    @Test
    public void destroy_agent_from_env_removes_wrapper() throws Exception {
        String agentName = "Test";
        AgentClass a = env.createAgentClass(agentName, null);
        assertThat("agent class exists", env.getAgentClass(agentName), notNullValue());
        env.removeAgentClass(a);
        assertThat("agent class deleted", env.getAgentClass(agentName), nullValue());
    }

    @Test
    public void name_of_destroyed_agent_can_be_reused() throws Exception {
        String agentName = "Test";
        AgentClass a = env.createAgentClass(agentName, null);
        a.destroy();
        assertThat("agent class deleted", env.getAgentClass(agentName), nullValue());
        AgentClass b = env.createAgentClass(agentName, null);
        assertThat("agent class exists again", env.getAgentClass(agentName), notNullValue());
    }

    @Test
    public void verify_behaviour_attributes_are_set() throws Exception {
    	AgentClass parent = env.createAgentClass("Test", null);
    	Behaviour beh = parent.getBehaviour();
    	int count = 99;
    	beh.setMaxNodes(count);
    	int m = beh.getMaxNodes();
    	assertThat("Max nodes property is set", m, equalTo(count));  
    }
    
    @Test
    public void verify_behaviour_class_inheritance() throws Exception {
    
    	AgentClass parent = env.createAgentClass("Test", null);
    	AgentClass child = env.createAgentClass("Test2", "Test");
    	
		de.s2.gsim.objects.Rule rule = createTestRule(parent);

		Action consequent = rule.getConsequents()[0];
    	
		rule = parent.getBehaviour().getRule("test");
    	assertThat("Rule conditions exist", rule.getConditions(), notNullValue());
    	assertThat("Rule consequents exist", rule.getConsequents(), notNullValue());
    	
    	de.s2.gsim.objects.Rule ruleChild = child.getBehaviour().getRule("test");
    	assertThat("Child agent inherits parent behaviour", ruleChild, notNullValue());
    	
    	Condition oldConditionRef = ruleChild.getConditions()[0];
    	Action oldActionRef = ruleChild.getConsequents()[0];
    	
    	rule.getConditions()[0].setOperator("<");
    	rule.getConsequents()[0].setActionClassName("modified.class");
    	String op = ruleChild.getConditions()[0].getOperator();
    	String op1 = oldConditionRef.getOperator();
    	
    	assertThat("Parent modifications are propagated", op, equalTo("<"));    	
    	assertThat("All refs are updated", op, equalTo(op1));
    	
    	String modified = consequent.getActionClassName();
    	assertThat("Original action refs updated", modified, equalTo("modified.class"));

    	String s = oldActionRef.getActionClassName();
    	assertThat("Dependent action refs updated", s, equalTo("modified.class"));

    }

	private de.s2.gsim.objects.Rule createTestRule(AgentClass parent) {
		parent.addAttribute("numbers", new DomainAttribute("counter", AttributeType.NUMERICAL));
		parent.setDefaultAttributeValue("numbers", "counter", "0");
		de.s2.gsim.objects.Rule rule = parent.getBehaviour().createRule("test");
		Condition condition = rule.createCondition("numbers/counter", ">", "0");
		Action action = parent.getBehaviour().createAction("consequent", "de.2s.sim.TestAction");
		parent.getBehaviour().addOrSetAction(action);
		rule.addOrSetConsequent(action);

		rule.addOrSetCondition(condition);
		parent.getBehaviour().addOrSetRule(rule);
		return rule;
	}

    @Test
    public void verify_behaviour_instance_inheritance() throws Exception {

		AgentClass parent = env.createAgentClass("Test", null);
		AgentClass child = env.createAgentClass("Test2", "Test");
		de.s2.gsim.objects.Rule parentRule = createTestRule(parent);

		assertThat("In child class parent rule is available",
				child.getBehaviour().getRule(parentRule.getName()).getName(), equalTo(parentRule.getName()));

		AgentInstance instance = env.instanciateAgent(child, "instance");

		assertThat("In child instance parent rule is available",
				instance.getBehaviour().getRule(parentRule.getName()).getName(), equalTo(parentRule.getName()));

		parentRule.addOrSetCondition(parentRule.createCondition("numbers", "=", "0"));

		assertThat("In child instance parent rule is available",
				instance.getBehaviour().getRule(parentRule.getName()).getConditions().length, equalTo(2));

    }

    @Test
    public void verify_retrieval_of_subtypes() throws Exception {
        AgentClass agentClass = env.createAgentClass("Test", null);
        AgentClass agentClass2 = env.createAgentClass("Test2", null);
        String prefix1 = "test-1";
        String prefix2 = "test-2";
        env.instanciateAgentsUniformDistributed(agentClass, prefix1, 10);
        env.instanciateAgentsUniformDistributed(agentClass2, prefix2, 10);

        AgentInstance[] testInstances = env.getAgents(agentClass.getName());
        AgentInstance[] test2Instances = env.getAgents(agentClass2.getName());

        assertThat("agent instances are separate", testInstances.length, equalTo(10));
        assertThat("agent instances are separate", test2Instances.length, equalTo(10));

        assertThat("agent instances are in correct lists", testInstances[1].getName(), startsWith(prefix1));
        assertThat("agent instances are in correct lists", test2Instances[5].getName(), startsWith(prefix2));

    }

    @Test
    public void removal_of_children_is_propagated() {

		expected.expect(GSimException.class);
		expected.expectMessage("No value present");

        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");

        ObjectClass objClass = env.createObjectClass("obj-class", null);
        objClass.addAttribute("obj-list", new DomainAttribute("att-1", AttributeType.STRING));

        agentClass.addOrSetObject("list", objClass);

        AgentClass subClass = env.createAgentClass("sub", agentClass.getName());
        AgentInstance inst = env.instanciateAgent(subClass, "agent_instance");

        assertThat(inst.getObjects("list").length, equalTo(1));

        agentClass.removeObject("list", objClass);

        assertThat("attribute of child object should be empty", subClass.getAttributes("list").length, equalTo(0));

        assertThat(inst.getObjects("list").length, equalTo(0));

        inst.getObject("list", "obj-class");

    }

    @Test
    public void destroy_is_propagated() {
        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");

        ObjectClass objClass = env.createObjectClass("obj-class", null);
        objClass.addAttribute("obj-list", new DomainAttribute("att-1", AttributeType.STRING));

        agentClass.addOrSetObject("list", objClass);

        AgentClass subClass = env.createAgentClass("sub", agentClass.getName());

        objClass.destroy();

        assertThat("attribute of child object should be empty", subClass.getAttributes("list").length, equalTo(0));

    }

    @Test
    public void objects_of_agent_successors_should_inherit_new_frame_attributes() throws Exception {

        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");

        ObjectClass objClass = env.createObjectClass("obj-class", null);
        objClass.addAttribute("on-the-fly", new DomainAttribute("Test", AttributeType.STRING));

        agentClass.addOrSetObject("list", objClass);// register agentClass for changes of objClass

        AgentInstance inst = env.instanciateAgent(agentClass, "agent");// register inst with changes of agentClass
        assertThat("original attribute is available", inst.getObject("list", "obj-class"), not(nullValue()));
        objClass.addAttribute("on-the-fly", new DomainAttribute("new", AttributeType.STRING));

        assertThat("new attribute is in owning agent", agentClass.getObjects("list")[0].getAttribute("on-the-fly", "new"), notNullValue());
        assertThat("instance has a new instance of the new attribute", inst.getObject("list", "obj-class").getAttribute("new"), notNullValue());

    }

    @Test
    public void objects_of_simagent_classes_should_reference_dependent_objects() throws Exception {

        Environment env = new Environment("");
        GenericAgentClass real = env.getAgentClassOperations().createAgentSubclass("test", null);
        Frame dep = env.getObjectClassOperations().createObjectSubClass("obj", null);
        dep.defineAttributeList("list");
        dep.addOrSetAttribute("list", new DomainAttribute("att", AttributeType.STRING));
        real.addOrSetChildFrame("children", dep);
        AgentClass agentClass = new AgentClassSim(real);
        ObjectClass wrapped = agentClass.getObjects("children")[0];
        wrapped.addAttribute("list", new DomainAttribute("att2", AttributeType.STRING));

        assertThat("new attribute is in owning agent", agentClass.getObjects("children")[0].getAttribute("list", "att2"), notNullValue());
    }


    @Test
    public void agent_instances_should_inherit_new_frame_attributes_in_new_list() throws Exception {
        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");

        AgentInstance inst = env.instanciateAgent(agentClass, "agent");

        agentClass.addAttribute("list", new DomainAttribute("test-att", AttributeType.STRING));

        assertThat("new attribute of agentClass is propagated to instance", inst.getAttribute("list", "test-att"), notNullValue());

    }

    @Test
    public void agent_instances_should_inherit_new_frame_attributes_in_existing_list() throws Exception {
        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");
        agentClass.addAttribute("list", new DomainAttribute("some-att", AttributeType.STRING));

        AgentInstance inst = env.instanciateAgent(agentClass, "agent");

        agentClass.addAttribute("list", new DomainAttribute("new-att", AttributeType.STRING));

        assertThat("new attribute of agentClass is propagated to instance", inst.getAttribute("list", "new-att"), notNullValue());
        assertThat("existing attribute of agentClass is in instance", inst.getAttribute("list", "some-att"), notNullValue());

    }

    @Test
    public void non_existing_agent_attributelist_should_be_created_on_the_fly() throws Exception {
        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");
        agentClass.addAttribute("on-the-fly", new DomainAttribute("Test", AttributeType.STRING));
        assertThat("attribute list exists", agentClass.getAttributeListNames()[0], equalTo("on-the-fly"));
    }

    @Test
    public void non_existing_object_attributelist_should_be_created_on_the_fly() throws Exception {
        env.createObjectClass("Test", null);
        ObjectClass objClass = env.getObjectClass("Test");
        objClass.addAttribute("on-the-fly", new DomainAttribute("Test", AttributeType.STRING));
        assertThat("attribute list exists", objClass.getAttributeListNames()[0], equalTo("on-the-fly"));
    }

    @Test
    public void non_existing_agent_framelist_should_be_created_on_the_fly() throws Exception {
        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");
        agentClass.addOrSetObject("on-the-fly", env.createObjectClass("obj", null));
        assertThat("object list exists", agentClass.getObjectListNames()[0], equalTo("on-the-fly"));
        assertThat("object exists", agentClass.getObjects("on-the-fly").length, equalTo(1));
    }

    @Test
    public void inherited_agent_attribute_should_not_overwrite_parent_attribute() throws Exception {

        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");

        DomainAttribute att = new DomainAttribute("att-1", AttributeType.STRING);
        att.setDefault("Hello world");
        agentClass.addAttribute("list-1", att);

        AgentClass subtype = env.createAgentClass("Sub", "Test");
        DomainAttribute inherited = subtype.getAttribute("list-1", "att-1");
		inherited.setDefault("Hello subworld!");
		subtype.setAttribute("list-1", inherited);

        DomainAttribute original = agentClass.getAttribute("list-1", "att-1");
        inherited = subtype.getAttribute("list-1", "att-1");

        assertThat("Subclass modifications must not influence parent state", original.getDefaultValue(), not(equalTo(inherited.getDefaultValue())));

    }

    @Test
    public void modifications_of_supertype_must_not_overwrite_modified_inherited_attribute() throws Exception {

        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");
        String objectList = "objects";
        String attrList = "olist-1";
        String attrName = "att-1";
        String defaultValue = "Hello world";
        String overridenValue = "Hello subtype world!";
        String secondTimeOverride = "Second time override";

        DomainAttribute att = new DomainAttribute(attrName, AttributeType.STRING);
        att.setDefault(defaultValue);
        ObjectClass objectClass = env.createObjectClass("TestObject", null);
        objectClass.addAttribute(attrList, att);
        agentClass.addOrSetObject(objectList, objectClass);

        AgentClass subtype = env.createAgentClass("Sub", "Test");
        ObjectClass inheritedObject = subtype.getObjects(objectList)[0];
        DomainAttribute inherited = inheritedObject.getAttribute(attrList, attrName);
        inherited.setDefault(overridenValue);
        inheritedObject.setAttribute(attrList, inherited);
        subtype.addOrSetObject(objectList, inheritedObject);

        ObjectClass originalObject = agentClass.getObjects(objectList)[0];
        DomainAttribute originalAttribute = originalObject.getAttribute(attrList, attrName);
        originalAttribute.setDefault(secondTimeOverride);
		DomainAttribute inheritedTest = subtype.getObjects(objectList)[0].getAttribute(attrList, attrName);
        originalObject.setAttribute(attrList, originalAttribute);
        agentClass.addOrSetObject(objectList, originalObject);

        DomainAttribute originalTest = agentClass.getObjects(objectList)[0].getAttribute(attrList, attrName);
		inheritedTest = subtype.getObjects(objectList)[0].getAttribute(attrList, attrName);

        assertThat("Subclass modifications must not influence parent state", originalTest.getDefaultValue(), equalTo(secondTimeOverride));
        assertThat("Subclass modifications must not influence parent state", originalTest.getDefaultValue(),
                not(equalTo(inheritedTest.getDefaultValue())));

    }

    @Test
    public void inherited_agent_object_attribute_must_not_overwrite_parent_attribute() throws Exception {

        env.createAgentClass("Test", null);
        AgentClass agentClass = env.getAgentClass("Test");
        String objectList = "objects";
        String attrList = "olist-1";
        String attrName = "att-1";
        String defaultValue = "Hello world";
        String overridenValue = "Hello subtype world!";

        DomainAttribute att = new DomainAttribute(attrName, AttributeType.STRING);
        att.setDefault(defaultValue);
        ObjectClass objectClass = env.createObjectClass("TestObject", null);
        objectClass.addAttribute(attrList, att);
        agentClass.addOrSetObject(objectList, objectClass);

        AgentClass subtype = env.createAgentClass("Sub", "Test");
        ObjectClass inheritedObject = subtype.getObjects(objectList)[0];
        DomainAttribute inherited = inheritedObject.getAttribute(attrList, attrName);
        inherited.setDefault(overridenValue);
        inheritedObject.setAttribute(attrList, inherited);
        // subtype.addOrSetObject(objectList, inheritedObject);

        DomainAttribute originalTest = agentClass.getObjects(objectList)[0].getAttribute(attrList, attrName);
        DomainAttribute inheritedTest = subtype.getObjects(objectList)[0].getAttribute(attrList, attrName);

        assertThat("Subclass modifications must not influence parent state", originalTest.getDefaultValue(), equalTo(defaultValue));
        assertThat("Subclass modifications must not influence parent state", inheritedTest.getDefaultValue(), not(equalTo(defaultValue)));
        assertThat("Subclass modifications is expected", inheritedTest.getDefaultValue(), equalTo(overridenValue));

    }

}
