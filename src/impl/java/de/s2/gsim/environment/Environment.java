package de.s2.gsim.environment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import de.s2.gsim.def.EnvironmentBase;
import de.s2.gsim.def.FramePersistenceManager;
import de.s2.gsim.def.GSimDefException;
import de.s2.gsim.def.InheritanceHierarchy;
import de.s2.gsim.def.InstancePersistenceManager;
import de.s2.gsim.def.TimeOrderedSet;
import de.s2.gsim.def.objects.Frame;
import de.s2.gsim.def.objects.Instance;
import de.s2.gsim.def.objects.agent.BehaviourFrame;
import de.s2.gsim.def.objects.agent.GenericAgent;
import de.s2.gsim.def.objects.agent.GenericAgentClass;

/**
 * @author Stephan
 *
 */
public class Environment {
	
	private EntitiesContainer container;

	private Map<String, String> actionMappings = new HashMap<String, String>();

    private Map<String, String[]> agentMappings = new HashMap<String, String[]>();

    private Map<String, String> agentPauses = new HashMap<String, String>();

    private Map<String, Integer> agentOrder = new HashMap<String, Integer>();
    
    private Map<String, String> agentRtClassMappings = new HashMap<String, String>();
	
    private Map<String, String> systemAgents = new HashMap<String, String>();

    public EntitiesContainer getContainer() {
		return container;
	}
	public void setContainer(EntitiesContainer container) {
		this.container = container;
	}
	public Map<String, String> getActionMappings() {
		return actionMappings;
	}
	public void setActionMappings(Map<String, String> actionMappings) {
		this.actionMappings = actionMappings;
	}
	public Map<String, String[]> getAgentMappings() {
		return agentMappings;
	}
	public void setAgentMappings(Map<String, String[]> agentMappings) {
		this.agentMappings = agentMappings;
	}
	public Map<String, String> getAgentPauses() {
		return agentPauses;
	}
	public void setAgentPauses(Map<String, String> agentPauses) {
		this.agentPauses = agentPauses;
	}
	public Map<String, Integer> getAgentOrder() {
		return agentOrder;
	}
	public void setAgentOrder(Map<String, Integer> agentOrder) {
		this.agentOrder = agentOrder;
	}
	public Map<String, String> getAgentRtClassMappings() {
		return agentRtClassMappings;
	}
	public void setAgentRtClassMappings(Map<String, String> agentRtClassMappings) {
		this.agentRtClassMappings = agentRtClassMappings;
	}
	public Map<String, String> getSystemAgents() {
		return systemAgents;
	}
	public void setSystemAgents(Map<String, String> systemAgents) {
		this.systemAgents = systemAgents;
	}

	public Environment(EntitiesContainer container) {
		this.container = container;
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

    protected void copyEnvironment(EnvironmentBase env) throws GSimDefException {
        try {
            behaviourClasses = new TimeOrderedSet<Frame>(this.clone(env.behaviourClasses));
            objects = new TimeOrderedSet<Instance>(this.clone(env.objects));
            objectSubClasses = new TimeOrderedSet<Frame>(this.clone(env.objectSubClasses));
            agentClass = (GenericAgentClass) env.agentClass.clone();

            agentSubClasses = new TimeOrderedSet<GenericAgentClass>(this.clone(env.agentSubClasses));
            agents = new TimeOrderedSet<GenericAgent>(this.clone(env.agents));

            if (env.behaviourClass != null) {
                behaviourClass = (Frame) env.behaviourClass.clone();
            }
            agentClass = (GenericAgentClass) env.agentClass.clone();
            objectClass = (Frame) env.objectClass.clone();

            agentPauses = new HashMap<String, String>(env.agentPauses);
            dataHandlers = new HashMap<String, String>(env.dataHandlers);
            agentMappings = new HashMap<String, String[]>(env.agentMappings);
            agentOrder = new HashMap<String, Integer>(env.agentOrder);
            agentRtClassMappings = new HashMap<String, String>(env.agentRtClassMappings);
            systemAgents = new HashMap<String, String>(env.systemAgents);
        } catch (Exception e) {
            throw new GSimDefException("Error in copy env", e);
        }
    }

    public void dismissChanges() {

        removed.clear();

        FramePersistenceManager frameControl = new FramePersistenceManager(ns);
        InstancePersistenceManager instanceControl = new InstancePersistenceManager(ns);

        ListIterator<GenericAgent> iter = agents.listIterator();
        while (iter.hasNext()) {
            Instance in = iter.next();
            if (in.isDirty()) {
                GenericAgent f = (GenericAgent) instanceControl.reload(in.getName());
                if (f != null) {
                    iter.set(f);
                } else {
                    iter.remove();
                }
            }
        }

        ListIterator<GenericAgentClass> iter2 = agentSubClasses.listIterator();
        while (iter2.hasNext()) {
            Frame in = iter2.next();
            if (in.isDirty()) {
                GenericAgentClass f = (GenericAgentClass) frameControl.reload(in.getTypeName());
                if (f != null) {
                    iter2.set(f);
                } else {
                    iter2.remove();
                }
            }
        }

        ListIterator<Frame> iter3 = objectSubClasses.listIterator();
        while (iter3.hasNext()) {
            Frame in = iter3.next();
            if (in.isDirty()) {
                Frame f = frameControl.reload(in.getTypeName());
                if (f != null) {
                    iter3.set(f);
                } else {
                    iter3.remove();
                }
            }
        }

        ListIterator<Instance> iter4 = objects.listIterator();
        while (iter4.hasNext()) {
            Instance in = iter.next();
            if (in.isDirty()) {
                Instance f = instanceControl.reload(in.getName());
                if (f != null) {
                    iter4.set(f);
                } else {
                    iter4.remove();
                }
            }
        }

    }
	
    public InheritanceHierarchy[] exportObjectHierarchy() {

        HashMap<String, InheritanceHierarchy> nodes = new HashMap<String, InheritanceHierarchy>();
        ListIterator iter = objectSubClasses.listIterator();

        Frame root = objectClass;
        InheritanceHierarchy node = new InheritanceHierarchy((Frame) root.clone());
        nodes.put(node.getFrame().getTypeName(), node);

        while (iter.hasNext()) {
            Frame c = (Frame) iter.next();
            node.insert((Frame) c.clone());
            nodes.put(node.getFrame().getTypeName(), node);
        }

        InheritanceHierarchy[] top = new InheritanceHierarchy[nodes.values().size()];
        nodes.values().toArray(top);

        return top;

    }
    public void saveEnvironment() {
        FramePersistenceManager frameControl = new FramePersistenceManager(ns);
        InstancePersistenceManager instanceControl = new InstancePersistenceManager(ns);

        Iterator iter = removed.iterator();

        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Frame) {
                frameControl.deleteFrame((Frame) o);
            } else if (o instanceof Instance) {
                instanceControl.deleteInstance((Instance) o);
            }
        }
        removed.clear();

        iter = agentSubClasses.listIterator();
        while (iter.hasNext()) {
            Frame f = (Frame) iter.next();
            if (f.isDirty()) {
                frameControl.saveFrame(f);
            }
        }
        iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            Frame f = (Frame) iter.next();
            if (f.isDirty()) {
                frameControl.saveFrame(f);
            }
        }
        iter = behaviourClasses.listIterator();
        while (iter.hasNext()) {
            Frame f = (Frame) iter.next();
            if (f.isDirty()) {
                frameControl.saveFrame(f);
            }
        }

        iter = agents.iterator();
        while (iter.hasNext()) {
            Instance inst = (Instance) iter.next();
            if (inst.isDirty()) {
                inst.setDirty(false);
                instanceControl.saveInstance(inst);
            }
        }
        iter = objects.iterator();
        while (iter.hasNext()) {
            Instance inst = (Instance) iter.next();
            if (inst.isDirty()) {
                inst.setDirty(false);
                instanceControl.saveInstance(inst);
            }
        }

    }
    
    /**
     * @TODO load here frames and instances. !!!!Make sure that the top-level classes are at the top of the loaded classes!!!!
     * 
     */
    protected void init() {
        FramePersistenceManager frameControl = new FramePersistenceManager(ns);
        InstancePersistenceManager instanceControl = new InstancePersistenceManager(ns);

        Frame[] all = frameControl.getAllFrames();
        Instance[] in = instanceControl.loadAll();

        for (int i = 0; i < all.length; i++) {
            if (all[i] instanceof GenericAgentClass) {
                agentSubClasses.add((GenericAgentClass) all[i]);
                all[i].setAncestor(agentClass);

                for (int j = 0; j < in.length; j++) {
                    if (in[j].inheritsFrom(all[i])) {
                        in[j].getDefinition().setAncestor(all[i]);
                        agents.add((GenericAgent) in[j]);
                    }
                }
            } else if (all[i] instanceof BehaviourFrame) {
                behaviourClasses.add(all[i]);
            } else {
                all[i].setAncestor(objectClass);
                objectSubClasses.add(all[i]);
                for (int j = 0; j < in.length; j++) {
                    if (in[j].inheritsFrom(all[i])) {
                        in[j].getDefinition().setAncestor(all[i]);
                        objects.add(in[j]);
                    }
                }
            }
        }
    }
}
