package de.s2.gsim.environment;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.s2.gsim.def.objects.Frame;
import de.s2.gsim.def.objects.Instance;
import de.s2.gsim.def.objects.Unit;
import de.s2.gsim.def.objects.agent.GenericAgent;
import de.s2.gsim.def.objects.agent.GenericAgentClass;

/**
 * EntitesContainer holds all components of an {@link Environment}. An environment is the simulation and setup specific object system that manages agents, objects and their relations to each other.
 * 
 * @author Stephan
 *
 */
public class EntitiesContainer {

	/**
	 * Simulation Namespace the container belongs to.
	 */
	private final String ns;
	
	/**
	 * Toplevel agent class.
	 */
	private GenericAgentClass agentClass;

	/**
	 * Toplevel object class.
	 */
	private Frame objectClass;

	/**
	 * Top level behaviour class.
	 */
	private Frame behaviourClass;

	/**
	 * Set of agent instances.
	 */
	private final Set<GenericAgent> agents = new LinkedHashSet<GenericAgent>();

	/**
	 * Set of agent classes.
	 */
	private final Set<GenericAgentClass> agentSubClasses = new LinkedHashSet<GenericAgentClass>();

	/**
	 * Set of behaviour classes.
	 */
	private final Set<Frame> behaviourClasses = new LinkedHashSet<Frame>();

	/**
	 * Datahandlers managed by the simulation enginge.
	 */
	private final Map<String, String> dataHandlers = new HashMap<String, String>();

	/**
	 * Set of object instances.
	 */
	private final Set<Instance> objects = new LinkedHashSet<Instance>();

	/**
	 * Set of object classes.
	 */
	private final Set<Frame> objectSubClasses = new LinkedHashSet<Frame>();

	/**
	 * Set of units waiting for removal.
	 */
	private final Set<Unit> removed = new LinkedHashSet<Unit>();
	
	public EntitiesContainer(String ns) {
		this.ns = ns;
	}

	/**
	 * Gets all instances (objects or agents) that were generated from the given frame, but no instances of any successor frames.
	 * 
	 * @param frame the frame
	 * @return a list of instances
	 */
	public List<? extends Instance> getInstancesOfClass(Frame frame) {
		Stream<? extends Instance> stream = (frame instanceof GenericAgentClass) ? agents.stream(): objects.stream(); 
		return stream
			.filter(instance -> instance.getDefinition().getTypeName().equals(frame.getTypeName()))
			.collect(Collectors.toList());
	}

	/**
	 * Gets all instances (objects or agents) that inherit from the given frame.
	 * 
	 * @param frame the frame
	 * @return a list of instances
	 */
	public List<? extends Instance> getAllInstancesOfClass(Frame frame) {
		Stream<? extends Instance> stream = (frame instanceof GenericAgentClass) ? agents.stream(): objects.stream(); 
		return stream
			.filter(instance -> instance.inheritsFrom(frame))
			.collect(Collectors.toList());
	}

	public GenericAgentClass getAgentClass() {
		return agentClass;
	}

	public void setAgentClass(GenericAgentClass agentClass) {
		this.agentClass = agentClass;
	}

	public Set<GenericAgent> getAgents() {
		return agents;
	}

	public void addAgent(GenericAgent agent) {
		this.agents.add(agent);
	}

	public Set<GenericAgentClass> getAgentSubClasses() {
		return agentSubClasses;
	}

	public void addAgentClass(GenericAgentClass agentClass) {
		this.agentSubClasses.add(agentClass);
	}

	public Frame getBehaviourClass() {
		return behaviourClass;
	}

	public void setBehaviourClass(Frame behaviourClass) {
		this.behaviourClass = behaviourClass;
	}

	public Set<Frame> getBehaviourClasses() {
		return behaviourClasses;
	}

	public void addBehaviourClass(Frame behaviourClasse) {
		this.behaviourClasses.add(behaviourClass);
	}

	public Map<String, String> getDataHandlers() {
		return dataHandlers;
	}

	public void addDataHandler(String name, String className) {
		this.dataHandlers.put(name, className);
	}

	public String getNs() {
		return ns;
	}

	public Frame getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(Frame objectClass) {
		this.objectClass = objectClass;
	}

	public Set<Instance> getObjects() {
		return objects;
	}

	public void addObject(Instance object) {
		this.objects.add(object);
	}

	public Set<Frame> getObjectSubClasses() {
		return objectSubClasses;
	}

	public void ObjectClass(Frame objectSubClass) {
		this.objectSubClasses.add(objectSubClass);
	}

	public Set<Unit> getRemoved() {
		return removed;
	}

	public void remove(Unit removed) {
		this.removed.add(removed);
	}

}
