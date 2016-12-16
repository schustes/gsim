package de.s2.gsim.sim;

import java.util.HashMap;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.environment.AgentClassOperations;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgent;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.ObjectClassOperations;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.behaviour.builder.ParsingUtils;

public class ParsingUtilsTest {

	ModelDefinitionEnvironment env;

	GSimCore core;

	@Before
	public void setupEnv() {
		core = GSimCoreFactory.defaultFactory().createCore();
		env = core.create("test", new HashMap<>());
	}


	@Test
	public void verify_frame_resolution_from_expansion() throws Exception {
        Environment env = new Environment("test");
        AgentClassOperations agentOperations = env.getAgentClassOperations();
        ObjectClassOperations objectOperations = env.getObjectClassOperations();

        GenericAgentClass a = agentOperations.createAgentSubclass("Test agent class", agentOperations.getGenericAgentClass());

        Frame child = objectOperations.createObjectSubClass("Object", null);
        Path<List<DomainAttribute>> p = Path.attributeListPath("list");
        objectOperations.addAttribute(child, p, new DomainAttribute("test", AttributeType.STRING));

		a = agentOperations.addObjectList(a, "objects", child);
        
        //a.addChildFrame(Path.objectListPath("objects"), child);
        
        GenericAgent instance = env.getAgentInstanceOperations().instanciateAgentWithUniformDistributedAttributes(a, "my agent");
		
		String typeNameWithList = ParsingUtils.resolveChildFrameWithList(instance.getDefinition(), "objects/Object/test");
		System.out.println(typeNameWithList);
		MatcherAssert.assertThat("Expected object", typeNameWithList, Matchers.equalTo("objects/Object"));

	}

}
