package de.s2.gsim.environment;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.s2.gsim.objects.attribute.DomainAttribute;

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
    private BehaviourFrame behaviourClass;

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
    private final Set<BehaviourFrame> behaviourClasses = new LinkedHashSet<>();

    /**
     * Set of object instances.
     */
    private final Set<Instance> objects = new LinkedHashSet<Instance>();

    /**
     * Set of object classes.
     */
    private final Set<Frame> objectSubClasses = new LinkedHashSet<Frame>();

    EntitiesContainer(String ns) {
        this.ns = ns;
        this.agentClass = GenericAgentClass.baseGenericAgentClass();
        this.objectClass = Frame.newFrame("Base object");
        this.behaviourClass = BehaviourFrame.newBehaviour("Base behaviour");
    }

    /**
     * Gets all instances (objects or agents) that were generated from the given frame, but no instances of any successor frames.
     * 
     * @param frame the frame
     * @return a list of instances
     */
    public List<? extends Instance> getInstancesOfClass0(Frame frame) {
        Stream<? extends Instance> stream = (frame instanceof GenericAgentClass) ? agents.values().stream() : objects.stream();
        return stream.filter(instance -> instance.getDefinition().getName().equals(frame.getName())).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <F extends Instance> List<F> getInstancesOfClass(Frame frame, Class<F> c) {
        Stream<F> stream = (Stream<F>) ((frame instanceof GenericAgentClass) ? agents.values().stream() : objects.stream());
        return stream.filter(instance -> instance.getDefinition().getName().equals(frame.getName())).collect(Collectors.toList());
    }

    public void modifyChildFrame(BiConsumer<Frame, Path<DomainAttribute>> func, Frame classToRemoveAttributeFrom, Path<DomainAttribute> pathToChildFrame) {
        Collection<? extends Frame> set = classToRemoveAttributeFrom.isSuccessor(agentClass.getName()) ? agentSubClasses.values() : objectSubClasses;
        set.stream().filter(ac -> ac.hasDeclaredChildFrame(classToRemoveAttributeFrom.getName())).forEach(frame -> {
            for (String list : classToRemoveAttributeFrom.getListNamesWithDeclaredChildFrame(classToRemoveAttributeFrom.getName())) {
                //Path<DomainAttribute> newPath = Path.attributePath(pathToChildFrame.toStringArray(), list, classToRemoveAttributeFrom.getName());
                
            	Path<DomainAttribute> newPath = Path.objectPath(pathToChildFrame.toStringArray())
            			.append(Path.attributePath(list, classToRemoveAttributeFrom.getName()));
            	func.accept(frame, newPath);
            }
        });
    }

    public void removeFrameInReferringFrames(BiConsumer<Frame, Path<Frame>> func, Frame removed) {
        Collection<? extends Frame> set = removed.isSuccessor(agentClass.getName()) ? agentSubClasses.values() : objectSubClasses;
        set.stream().filter(a -> a.hasDeclaredChildFrame(removed.getName())).forEach(frame -> {
            for (String list : frame.getListNamesWithDeclaredChildFrame(removed.getName())) {
                Path<Frame> path = Path.objectPath(list, removed.getName());
                func.accept(frame, path);
            }
        });
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
    
	public GenericAgentClass getAgentSubClass(String name) {
		return this.getAgentSubClasses().stream().filter(a -> a.getName().equals(name)).findAny().get();
	}

	public Frame getObjectSubClass(String name) {
		return this.getObjectSubClasses().stream().filter(a -> a.getName().equals(name)).findAny().get();
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

    /**
     * Returns all successors of the given agent class.
     * 
     * @param ofWhich the agent class to get the successors for
     * @return a set of subclasses
     */
    public Set<GenericAgentClass> getAgentSubClasses(GenericAgentClass ofWhich) {
		Set<GenericAgentClass> ret = agentSubClasses.values().stream().filter(a -> a.isSuccessor(ofWhich.getName()))
				.collect(Collectors.toSet());
		return ret;
    }

    /**
     * Get the direct children (h+1) of the given agent class.
     * 
     * @param ofWhich the agent to get the children from
     * @return a set of subclasses
     */
    public Set<GenericAgentClass> getAgentChildren(GenericAgentClass ofWhich) {
        return agentSubClasses.values().parallelStream().filter(a -> a.getParentFrame(ofWhich.getName()) != null).collect(Collectors.toSet());
    }

    public void addAgentClass(GenericAgentClass agentClass) {
        this.agentSubClasses.put(agentClass.getName(), agentClass);
    }

    public BehaviourFrame getBehaviourClass() {
        return behaviourClass;
    }

    public void setBehaviourClass(BehaviourFrame behaviourClass) {
        this.behaviourClass = behaviourClass;
    }

    public Set<BehaviourFrame> getBehaviourClasses() {
        return behaviourClasses;
    }

    public void addBehaviourClass(BehaviourFrame behaviourClass) {
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

    /**
     * Returns all successors in the inheritance tree.
     * 
     * @param parent
     * @return
     */
    public Set<Frame> getObjectSubClasses(Frame parent) {
		return objectSubClasses.stream().filter(f -> f.isSuccessor(parent.getName())).collect(Collectors.toSet());
    }

    /**
     * Returns all successors in the inheritance tree.
     * 
     * @param parent
     * @return
     */
    public Set<Frame> getObjectSubClasses(String parent) {
		return objectSubClasses.stream().filter(f -> f.isSuccessor(parent)).collect(Collectors.toSet());
    }

    /**
     * Returns only next level inheritance.
     * 
     * @param parent
     * @return
     */
    public Set<Frame> getObjectChildren(Frame parent) {
        return objectSubClasses.parallelStream().filter(f -> f.getParentFrame(parent.getName()) != null).collect(Collectors.toSet());
    }

    /**
     * Returns only next level inheritance.
     * 
     * @param parent
     * @return
     */
    public Set<Frame> getObjectChildren(String parent) {
        return objectSubClasses.parallelStream().filter(f -> f.getParentFrame(parent) != null).collect(Collectors.toSet());
    }

    public void addObjectClass(Frame objectSubClass) {
        this.objectSubClasses.add(objectSubClass);
    }

    public void remove(Unit<?, ?> removed) {
        if (removed instanceof GenericAgent) {
            this.agents.remove(removed.getName());
        } else if (removed instanceof GenericAgentClass) {
            this.agentSubClasses.remove(removed.getName());
        } else if (removed instanceof Frame) {
            this.objectSubClasses.remove(removed.getName());
        } else if (removed instanceof Instance) {
            this.objects.remove(removed.getName());
        } else {
            throw new GSimDefException("No unit with name " + removed.getName() + " was removed. Does it exist and is it not a root agent/object?");
        }
        
    }

    public GenericAgentClass replaceAgentSubClass(GenericAgentClass oldOne, GenericAgentClass newOne) {
        if (!oldOne.getName().equals(newOne.getName())) {
            throw new IllegalArgumentException("The agent class to be replaced has not the same name as the one to replace with.");
        }
        this.agentSubClasses.put(oldOne.getName(), newOne);
        return newOne;
    }

    public void replaceAgent(GenericAgent oldOne, GenericAgent newOne) {
        if (!oldOne.getName().equals(newOne.getName())) {
            throw new IllegalArgumentException("The agent to be replaced has not the same name as the one to replace with.");
        }
        this.agents.put(oldOne.getName(), newOne);
    }

    public HierarchyNode[] exportAgentHierarchy() {

        Map<String, HierarchyNode> nodes = new HashMap<>();

        Frame root = agentClass;
        HierarchyNode node = new HierarchyNode((Frame) root.clone());
        nodes.put(node.getFrame().getName(), node);

        for (GenericAgentClass f : agentSubClasses.values()) {
            node.insert(f.clone());
            nodes.put(node.getFrame().getName(), node);
        }

        HierarchyNode[] top = new HierarchyNode[nodes.values().size()];
        nodes.values().toArray(top);

        return top;

    }

    public HierarchyNode[] exportBehaviourHierarchy() {

        Map<String, HierarchyNode> nodes = new HashMap<>();
        Frame root = behaviourClass;
        HierarchyNode node = new HierarchyNode((Frame) root.clone());
        node.insert((Frame) root.clone());

        for (BehaviourFrame c : this.behaviourClasses) {
            node.insert(c.clone());
            nodes.put(node.getFrame().getName(), node);
        }

        HierarchyNode[] top = new HierarchyNode[nodes.values().size()];
        nodes.values().toArray(top);

        return top;

    }

}
