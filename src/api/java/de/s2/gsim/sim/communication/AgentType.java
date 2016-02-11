package de.s2.gsim.sim.communication;

import de.s2.gsim.objects.AgentInstanceIF;
import de.s2.gsim.sim.agent.ApplicationAgent;

public interface AgentType {

    public String getName();

    public boolean isAgent();

    public AgentInstanceIF toAgent() throws ClassCastException;

    public ApplicationAgent toAppAgent() throws ClassCastException;

}
