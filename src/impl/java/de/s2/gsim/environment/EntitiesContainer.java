package de.s2.gsim.environment;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    EntitiesContainer(String ns) {
        this.ns = ns;
    }

    /**
     * Gets all instances (objects or agents) that were generated from the given frame, but no instances of any successor frames.
     * 
     * @param frame the frame
     * @return a list of instances
     */
    public List<? extends Instance> getInstancesOfClass(Frame frame) {
        Stream<? extends Instance> stream = (frame instanceof GenericAgentClass) ? agents.stream() : objects.stream();
        return stream.filter(instance -> instance.getDefinition().getTypeName().equals(frame.getTypeName())).collect(Collectors.toList());
    }

    /**
     * Gets all instances (objects or agents) that inherit from the given frame.
     * 
     * @param frame the frame
     * @return a list of instances
     */
    public List<? extends Instance> getAllInstancesOfClass(Frame frame) {
        Stream<? extends Instance> stream = (frame instanceof GenericAgentClass) ? agents.stream() : objects.stream();
        return stream.filter(instance -> instance.inheritsFrom(frame)).collect(Collectors.toList());
    }

    /**
     * Removes all entities in this instance and adds a clone of all entities from the other EntitiesContainer.
     * 
     * @param env the other container
     * @throws GSimDefException if a problem during the remove-clone-add cycle occurs
     */
    public void copyFromEnvironment(EntitiesContainer env) throws GSimDefException {
        try {

            agentClass = env.getAgentClass().clone();
            objectClass = env.getObjectClass().clone();
            if (env.behaviourClass != null) {
                behaviourClass = env.getBehaviourClass().clone();
            }

            behaviourClasses.clear();
            behaviourClasses.addAll(clone(env.getBehaviourClasses()));
            objects.clear();
            objects.addAll(clone(env.getObjects()));
            objectSubClasses.clear();
            objectSubClasses.addAll(clone(env.getObjectSubClasses()));
            agentSubClasses.clear();
            agentSubClasses.addAll(clone(env.getAgentSubClasses()));
            agents.clear();
            agents.addAll(clone(env.getAgents()));

        } catch (Exception e) {
            throw new GSimDefException("Error in copy env", e);
        }
    }

    /**
     * Clones a list of Unit types by cloning all of its elements into a new Set.
     * 
     * @param set the set to clone from
     * @return the newly created set
     */
    private <K extends Unit> Set<K> clone(Set<K> set) {
        return set.stream().map(e -> e.<K> copy()).collect(Collectors.toSet());
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

    public Set<? extends GenericAgentClass> getAgentSubClasses() {
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

    public String getNamespace() {
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

    public void addObjectClass(Frame objectSubClass) {
        this.objectSubClasses.add(objectSubClass);
    }

    public Set<Unit> getRemoved() {
        return removed;
    }

    public void remove(Unit removed) {
        this.removed.add(removed);
    }

}
