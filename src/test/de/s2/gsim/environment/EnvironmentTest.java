package de.s2.gsim.environment;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
