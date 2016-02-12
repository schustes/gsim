package de.s2.gsim.core;

import java.util.HashMap;

import de.s2.gsim.objects.AgentClass;

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

        // AgentClassIF a0 = env.getAgentClass("Traditional");
        // env.instanciateAgents(a0, "traditional", 1, 0, DefinitionEnvironment.RAND_ATT_ONLY);
        // ScenarioManager m = core.createScenarioManager(env, new HashMap(), 53, 1);
        // m.start();

    }
}
