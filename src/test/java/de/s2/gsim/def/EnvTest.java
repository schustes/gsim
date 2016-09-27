package de.s2.gsim.def;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;
import de.s2.gsim.api.objects.impl.AgentClassSim;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class EnvTest {

	ModelDefinitionEnvironment env;
	
	

	@Before
	public void setupEnv() {
		GSimCore core = GSimCoreFactory.defaultFactory().createCore();
		core = GSimCoreFactory.customFactory("Standalone").createCore();
		env = core.create("test", new HashMap<>());
	}
	
	@Test 
	public void objects_of_agent_instances_should_inherit_new_frame_attributes() throws Exception {
		
		env.createAgentClass("Test", null);
		AgentClass agentClass = env.getAgentClass("Test");

		ObjectClass objClass = env.createObjectClass("obj-class", null);
		objClass.addAttribute("on-the-fly", new DomainAttribute("Test", AttributeType.STRING));
		
		agentClass.addOrSetObject("list", objClass);//register agentClass for changes of objClass

		AgentInstance inst = env.instanciateAgent(agentClass, "agent");//register inst with changes of agentClass
		assertThat("original attribute is available", inst.getObject("list", "obj-class"), not(nullValue()));
		objClass.addAttribute("on-the-fly", new DomainAttribute("new", AttributeType.STRING));

		assertThat("new attribute is in owning agent", agentClass.getObjects("list")[0].getAttribute("on-the-fly", "new"), notNullValue());
		assertThat("instance has a new instance of the new attribute", inst.getObject("list","obj-class").getAttribute("new"), notNullValue());

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
	public void objects_of_object_instances_should_inherit_new_frame_attributes() throws Exception {
		throw new UnsupportedOperationException("Test must be implemented");
	}

	@Test 
	public void agent_instances_should_inherit_new_frame_attributes() throws Exception {
		throw new UnsupportedOperationException("Test must be implemented");
	}

	@Test 
	public void objects_of_agent_classes_should_inherit_new_frame_attributes() throws Exception {
		throw new UnsupportedOperationException("Test must be implemented");
	}

	@Test 
	public void objects_of_object_classes__should_inherit_new_frame_attributes() throws Exception {
		throw new UnsupportedOperationException("Test must be implemented");
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
