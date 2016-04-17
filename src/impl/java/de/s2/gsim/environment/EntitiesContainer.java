package de.s2.gsim.environment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private final Map<String, GenericAgent> agents = new LinkedHashMap<>();

    /**
     * Set of agent classes.
     */
    private final Map<String, GenericAgentClass> agentSubClasses = new LinkedHashMap<>();

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
    private final Set<Unit<?, ?>> removed = new LinkedHashSet<>();

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
        Stream<? extends Instance> stream = (frame instanceof GenericAgentClass) ? agents.values().stream() : objects.stream();
        return stream.filter(instance -> instance.getDefinition().getName().equals(frame.getName())).collect(Collectors.toList());
    }

    /**
     * Gets all instances (objects or agents) that inherit from the given frame.
     * 
     * @param frame the frame
     * @return a list of instances
     */
    @SuppressWarnings("unchecked")
    public <F extends Instance> List<F> getAllInstancesOfClass(Frame frame, Class<F> c) {
        Stream<F> stream = (Stream<F>) ((frame instanceof GenericAgentClass) ? agents.values().stream() : objects.stream());
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

            Set<GenericAgentClass> g = clone(env.getAgentSubClasses());
            for (GenericAgentClass x : g) {
                agentSubClasses.put(x.getName(), x);
            }
            agents.clear();
            Set<GenericAgent> a = clone(env.getAgents());
            for (GenericAgent x : a) {
                agents.put(x.getName(), x);
            }

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
    private <K extends Unit<?, ?>> Set<K> clone(Collection<K> set) {
        return set.stream().map(e -> e.<K> copy()).collect(Collectors.toSet());
    }

    public GenericAgentClass getAgentClass() {
        return agentClass;
    }

    public void setAgentClass(GenericAgentClass agentClass) {
        this.agentClass = agentClass;
    }

    public Set<GenericAgent> getAgents() {
        return new LinkedHashSet<>(agents.values());
    }

    public void addAgent(GenericAgent agent) {
        this.agents.put(agent.getName(), agent);
    }

    public Set<GenericAgentClass> getAgentSubClasses() {
        return new LinkedHashSet<>(agentSubClasses.values());
    }

    public void addAgentClass(GenericAgentClass agentClass) {
        this.agentSubClasses.put(agentClass.getName(), agentClass);
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

    public Set<Unit<?, ?>> getRemoved() {
        return removed;
    }

    public void remove(Unit<?, ?> removed) {
        this.removed.add(removed);
    }

    public void replaceAgentSubClass(GenericAgentClass oldOne, GenericAgentClass newOne) {
        if (!oldOne.getName().equals(newOne.getName())) {
            throw new IllegalArgumentException("The agent to be replaced has not the same name as the one to replace with.");
        }
        this.agentSubClasses.put(oldOne.getName(), newOne);
    }

}
