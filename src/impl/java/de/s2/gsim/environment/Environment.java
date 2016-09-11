package de.s2.gsim.environment;

import java.util.HashMap;
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
	 * Name space of the model.
	 */
	private final String ns;

	/**
	 * Runtime configs
	 */
	private AgentRuntimeConfig runtimeConfig;

	/**
	 * Agent class operations
	 */
	private AgentClassOperations agentClassOperations;

	/**
	 * Object class operations.
	 */
	private ObjectClassOperations objectClassOperations;

	/**
	 * Agent instance operations.
	 */
	private AgentInstanceOperations agentInstanceOperations;

	/**
	 * Object instance operations.
	 */
	private ObjectInstanceOperations objectInstanceOperations;

	/**
	 * Constructor.
	 * 
	 * @param ns name space of the model
	 */
	public Environment(String ns) {
		this.ns = ns;
		this.container = new EntitiesContainer(ns);
		this.runtimeConfig = new AgentRuntimeConfig();
    	this.agentInstanceOperations = new AgentInstanceOperations(this.container);
    	this.objectInstanceOperations = new ObjectInstanceOperations(this.container);
    	this.objectClassOperations = new ObjectClassOperations(container);
    	this.agentClassOperations = new AgentClassOperations(container, this.runtimeConfig);
    	this.agentClassOperations.setObjectClassOperations(objectClassOperations);
    	this.objectClassOperations.setAgentClassOperations(agentClassOperations);
		
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
			this.getAgentRuntimeConfig().getAgentPauses().clear();
			this.getAgentRuntimeConfig().getAgentMappings().clear();
			this.getAgentRuntimeConfig().getAgentOrder().clear();
			this.getAgentRuntimeConfig().getAgentRtClassMappings().clear();
			this.getAgentRuntimeConfig().getSystemAgents().clear();
			this.getAgentRuntimeConfig().getAgentPauses().putAll(env.getAgentRuntimeConfig().getAgentPauses());
			this.getAgentRuntimeConfig().getAgentMappings().putAll(env.getAgentRuntimeConfig().getAgentMappings());
			this.getAgentRuntimeConfig().getAgentOrder().putAll(env.getAgentRuntimeConfig().getAgentOrder());
			this.getAgentRuntimeConfig().getAgentRtClassMappings().putAll(env.getAgentRuntimeConfig().getAgentRtClassMappings());
			this.getAgentRuntimeConfig().getSystemAgents().putAll(env.getAgentRuntimeConfig().getSystemAgents());
		} catch (Exception e) {
			throw new GSimDefException("Error in copy env", e);
		}
	}

	/**
	 * Get the runtime config.
	 */
	public AgentRuntimeConfig getAgentRuntimeConfig() {
		return runtimeConfig;
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
		nodes.put(node.getFrame().getName(), node);

		stream.forEach(frame -> {
			node.insert(frame.clone());
			nodes.put(node.getFrame().getName(), node);
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

	public EntitiesContainer getEntities() {
		return null;
	}

	public AgentClassOperations getAgentClassOperations() {
		return this.agentClassOperations;
	}

	public ObjectClassOperations getObjectClassOperations() {
		return this.objectClassOperations;
	}

	public ObjectInstanceOperations getObjectInstanceOperations() {
		return objectInstanceOperations;
	}

	public AgentInstanceOperations getAgentInstanceOperations() {
		return agentInstanceOperations;
	}


}
