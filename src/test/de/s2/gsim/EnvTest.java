package de.s2.gsim;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class EnvTest {

	ModelDefinitionEnvironment env;
	
	public static void main(String[] args) throws Exception {
		GSimCore core = GSimCoreFactory.defaultFactory().createCore();
		System.out.println("core: 1" + core);
		core = GSimCoreFactory.defaultFactory().createCore();
		core = GSimCoreFactory.customFactory("Standalone").createCore();
		System.out.println("core: 2" + core);

		ModelDefinitionEnvironment env = core.create("test", new HashMap<>());

		env.createAgentClass("Test", null);
		AgentClass agentClass = env.getAgentClass("Test");
		System.out.println(agentClass + ":" + agentClass.getName());

		DomainAttribute att = new DomainAttribute("att-1",AttributeType.STRING);
		att.setDefault("Hello world");
		agentClass.addAttribute("list-1", att);

		AgentClass subtype = env.createAgentClass("Sub", "Test");
		DomainAttribute inherited = subtype.getAttribute("list-1", "att-1");
		inherited.setDefault("Hello subworld!");
		subtype.setAttribute("list-1", inherited);

		System.out.println("att-1 value parent:" + inherited.getDefaultValue());
		System.out.println("att-1 value subtype:" + subtype.getAttribute("list-1", "att-1").getDefaultValue());

		// AgentClassIF a0 = env.getAgentClass("Traditional");
		// env.instanciateAgents(a0, "traditional", 1, 0, DefinitionEnvironment.RAND_ATT_ONLY);
		// ScenarioManager m = core.createScenarioManager(env, new HashMap(), 53, 1);
		// m.start();

	}
	
	@Before
	public void setupEnv() {

		GSimCore core = GSimCoreFactory.defaultFactory().createCore();
		core = GSimCoreFactory.customFactory("Standalone").createCore();
		env = core.create("test", new HashMap<>());

	}
	
	@Test
	public void inherited_agent_attribute_should_not_overwrite_parent_attribute() throws Exception {

		env.createAgentClass("Test", null);
		AgentClass agentClass = env.getAgentClass("Test");

		DomainAttribute att = new DomainAttribute("att-1",AttributeType.STRING);
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
	public void inherited_agent_object_attribute_must_not_overwrite_parent_attribute() throws Exception {

		env.createAgentClass("Test", null);
		AgentClass agentClass = env.getAgentClass("Test");
		String objectList = "objects";
		String attrList = "olist-1";
		String attrName = "att-1";
		String defaultValue = "Hello world";
		String overridenValue ="Hello subtype world!";
		String secondTimeOverride = "Second time override";

		DomainAttribute att = new DomainAttribute(attrName,AttributeType.STRING);
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
		originalObject.setAttribute(attrList, originalAttribute);
		agentClass.addOrSetObject(objectList, originalObject);

		DomainAttribute originalTest = agentClass.getObjects(objectList)[0].getAttribute(attrList, attrName);
		DomainAttribute inheritedTest = subtype.getObjects(objectList)[0].getAttribute(attrList, attrName);
	
		assertThat("Subclass modifications must not influence parent state", originalTest.getDefaultValue(), equalTo(secondTimeOverride));
		assertThat("Subclass modifications must not influence parent state", originalTest.getDefaultValue(), not(equalTo(inheritedTest.getDefaultValue())));

	}

}
