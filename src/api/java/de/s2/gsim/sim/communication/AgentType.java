package de.s2.gsim.sim.communication;

import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.sim.agent.ApplicationAgent;

public interface AgentType {

    public String getName();

    public boolean isAgent();

    public AgentInstance toAgent() throws ClassCastException;

    public ApplicationAgent toAppAgent() throws ClassCastException;

}
