package de.s2.gsim.environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Stephan
 *
 */
public class Environment implements Cloneable {

    /**
     * Reference to all entities.
     */
    private EntitiesContainer container;

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


    /**
     * Name space of the model.
     */
    private final String ns;

    /**
     * Constructor.
     * 
     * @param ns name space of the model
     */
    public Environment(String ns) {
        this.ns = ns;
        this.container = new EntitiesContainer(ns);
    }

    public Environment clone() {
        Environment newEnvironment = new Environment(this.ns);
        newEnvironment.copyFromEnvironment(this);
        return newEnvironment;
    }

    /**
     * Removes all entities in this instance and all entities from the other Environment.
     * 
     * @param env the other Environment
     * @throws GSimDefException if a problem during the remove-add cycle occurs
     */
    public void copyFromEnvironment(Environment env) throws GSimDefException {
        try {
            this.container.copyFromEnvironment(env.getContainer());
            this.agentPauses.clear();
            this.agentMappings.clear();
            this.agentOrder.clear();
            this.agentRtClassMappings.clear();
            this.systemAgents.clear();
            this.agentPauses.putAll(env.getAgentPauses());
            this.agentMappings.putAll(env.getAgentMappings());
            this.agentOrder.putAll(env.getAgentOrder());
            this.agentRtClassMappings.putAll(env.getAgentRtClassMappings());
            this.systemAgents.putAll(env.getSystemAgents());
        } catch (Exception e) {
            throw new GSimDefException("Error in copy env", e);
        }
    }

    /**
     * Exports a tree representation of the object classes in this Environment.
     * 
     * @return the tree representation of object classes in this Environment
     */
    public HierarchyTree exportObjectHierarchy() {
        return exportType(container.getObjectClass(), container.getObjectSubClasses().stream());
    }

    /**
     * Exports a tree representation of the agent classes in this Environment.
     * 
     * @return the tree representation of agent classes in this Environment
     */
    public HierarchyTree exportAgentHierarchy() {
        return exportType(container.getAgentClass(), container.getAgentSubClasses().stream());
    }

    /**
     * Exports a tree representation of the behaviour classes in this Environment.
     * 
     * @return the tree representation of behaviour classes in this Environment
     */
    public HierarchyTree exportBehaviourHierarchy() {
        return exportType(container.getBehaviourClass(), container.getBehaviourClasses().stream());
    }

    private HierarchyTree exportType(Frame root, Stream<? extends Frame> stream) {

        HashMap<String, HierarchyNode> nodes = new HashMap<String, HierarchyNode>();
        HierarchyNode node = new HierarchyNode(root.clone());
        nodes.put(node.getFrame().getTypeName(), node);

        stream.forEach(frame -> {
            node.insert(frame.clone());
            nodes.put(node.getFrame().getTypeName(), node);
        });

        HierarchyNode[] top = new HierarchyNode[nodes.values().size()];
        nodes.values().toArray(top);

        return new HierarchyTree(node);

    }

    /**
     * Gets the container where all frame and instances are stored.
     * 
     * @return
     */
    public EntitiesContainer getContainer() {
        return container;
    }

    public String getNamespace() {
        return this.ns;
    }

    public void setContainer(EntitiesContainer container) {
        this.container = container;
    }

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


    public EntitiesContainer getEntities() {
        return null;
    }

    public AgentClassOperations getAgentClassOperations() {
        return new AgentClassOperations(this.container);
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
