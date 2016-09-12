package de.s2.gsim.environment;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;
import de.s2.gsim.sim.SimTest.TestAction;

public class EnvironmentTest {

	@Rule 
	public ExpectedException expected = ExpectedException.none();

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
		DomainAttribute a = new DomainAttribute("p0-attribute",AttributeType.STRING);
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
		String test2AttrVal = p0.<DomainAttribute>resolvePath(Path.attributePath("p0-framelist", "p0-TestFrame", "p0-child-attributes", a.getName())).getDefaultValue();
		
		assertThat ("By-value", false, equalTo(test1AttrVal.equals(test2AttrVal)));
		
		GenericAgentClass p1 = agentOperations.createAgentSubclass("p1", p0);
//		expected.expect(NoSuchElementException.class);
//		assertThat("no parent list must be declared in child", p1.getDeclaredChildFrames("p0-framelist"), nullValue());

//		a.setDefault("test");
//		p0.replaceChildAttribute(Path.attributePath("p0-framelist", "p0-TestFrame", "p0-child-attributes", a.getName()), a);
//		test2AttrVal = p0.<DomainAttribute>resolvePath(Path.attributePath("p0-framelist", "p0-TestFrame", "p0-child-attributes", a.getName())).getDefaultValue();
//		test1AttrVal = p0.getAttribute("p0-attributes", a.getName()).getDefaultValue();
//		assertThat("Attribute in child was replaced with new one", "test", equalTo(test2AttrVal));
//		assertThat("Attribute in parent was NOT replaced with new one", "test",  not(equalTo(test1AttrVal)));
		
		
		a.setDefault("over");
		p1.addOrSetAttribute("p0-attributes", a);
		test1AttrVal = p0.getAttribute("p0-attributes", a.getName()).getDefaultValue();
		test2AttrVal = p1.getAttribute("p0-attributes", a.getName()).getDefaultValue();
		System.out.println(test1AttrVal+"::"+test2AttrVal);
		assertThat("No backpropagation of overloaded child attributes", test1AttrVal, not(equalTo(test2AttrVal)));
		
	}

	@Test
	public void verify_adding_object_classes() {

		Environment env = new Environment("test");

		ObjectClassOperations objectOperations = env.getObjectClassOperations();

		Frame subFrame = objectOperations.createObjectSubClass("TestObject", objectOperations.getObjectClass());

		assertThat("Sub object class inherits from top class", subFrame.getParentFrames().get(0),
				equalTo(objectOperations.getObjectClass()));

		assertThat("Object count is 1", objectOperations.getObjectSubClasses().size(), equalTo(1));

	}

	@Test
	public void verify_delete_agent() {

	}

	@Test
	public void verify_propagation_of_child_frames() {

	}


}
