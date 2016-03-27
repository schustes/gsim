package de.s2.gsim;

import java.util.HashMap;

import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class Test {

	public static void main(String[] args) throws Exception {

		GSimCore core = GSimCoreFactory.defaultFactory().createCore();

		System.out.println("core: 1" + core);

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
	
	@Test
	public void testInheritance() throws Exception {

		GSimCore core = GSimCoreFactory.defaultFactory().createCore();

		System.out.println("core: 1" + core);

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

}
