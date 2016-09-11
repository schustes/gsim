package de.s2.gsim.environment;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds information defined at setup time, which is required only at runtime.
 * 
 * @author Stephan
 *
 */
public class AgentRuntimeConfig {
	
    /**
     * Caches mappings from action names to executable class names.
     */
    private final Map<String, String> actionMappings = new HashMap<String, String>();

    /**
     * Caches agent names to roles.
     */
    private final Map<String, String[]> agentMappings = new HashMap<String, String[]>();

    /**
     * Caches the executions cycles of agent classes.
     */
    private final Map<String, String> agentPauses = new HashMap<String, String>();

    /**
     * Caches the ordering of agent roles for determining in which orders they are excuted within a cycle.
     */
    private final Map<String, Integer> agentOrder = new HashMap<String, Integer>();

    /**
     * Caches the agent class names to their runtime equivalents.
     */
    private final Map<String, String> agentRtClassMappings = new HashMap<String, String>();

    /**
     * Caches the system agents active during a simulation.
     */
    private final Map<String, String> systemAgents = new HashMap<String, String>();

    public Map<String, String> getActionMappings() {
        return actionMappings;
    }

    public void addActionMapping(String name, String action) {
        this.actionMappings.put(name, action);
    }

    public Map<String, String[]> getAgentMappings() {
        return agentMappings;
    }

    public void addAgentMappings(String agentName, String[] roles) {
        this.agentMappings.put(agentName, roles);
    }

    public Map<String, String> getAgentPauses() {
        return agentPauses;
    }

    public void addAgentPause(String agentClassName, String interval) {
        this.agentPauses.put(agentClassName, interval);
    }

    public Map<String, Integer> getAgentOrder() {
        return agentOrder;
    }

    public void addAgentOrder(String agentClassName, int order) {
        this.agentOrder.put(agentClassName, order);
    }

    public Map<String, String> getAgentRtClassMappings() {
        return agentRtClassMappings;
    }

    public void addAgentRtClassMapping(String agentClassName, String rtName) {
        this.agentRtClassMappings.put(agentClassName, rtName);
    }

    public Map<String, String> getSystemAgents() {
        return systemAgents;
    }

    public void addSystemAgents(String name, String cls) {
        this.systemAgents.put(name, cls);
    }
    
    public void addOrSetAgentMapping(String agentName, String[] roleNames) {
        agentMappings.put(agentName, roleNames);
    }

    public void addOrSetAgentOrdering(Integer order, String roleName) {
        agentOrder.put(roleName, order);
    }

    public void addOrSetRuntimeRoleMapping(String role, String cls) {
        agentRtClassMappings.put(role, cls);
    }

    public void addOrSetSystemAgent(String name, String cls) {
        systemAgents.put(name, cls);
    }



}
