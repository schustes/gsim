package de.s2.gsim.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.AgentClassDef;
import de.s2.gsim.api.objects.impl.AgentInstanceDef;
import de.s2.gsim.api.objects.impl.ObjectClassDef;
import de.s2.gsim.api.objects.impl.ObjectInstanceDef;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgent;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.HierarchyTree;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.ObjectInstance;

/**
 * This class hides the interna of the gsim frame and instance approach and exports only more comprehensible objects that the api publishes.
 * 
 * @author stephan
 *
 */
public class EnvironmentWrapper implements ModelDefinitionEnvironment {

    protected Environment env;

    public EnvironmentWrapper(Environment env) {
        this.env = env;
    }

    @Override
    public AgentClass createAgentClass(String name, String parent) throws GSimException {
        try {
            GenericAgentClass g = parent != null ? env.getContainer().getAgentSubClass(parent) : env.getContainer().getAgentClass();

            if (g == null) {
                g = env.getContainer().getAgentClass();
            }

            if (g == null) {
                throw new GSimException("No parent with name " + parent + " found.");
            }

            GenericAgentClass newAgentClass = env.getAgentClassOperations().createAgentSubclass(name, g);
            return new AgentClassDef(env, newAgentClass);

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentClass createAgentClass(String name, String parentName, int order) throws GSimException {
        AgentClass retVal = this.createAgentClass(name, parentName);
        env.addOrSetAgentOrdering(order, name);
        return retVal;
    }

    @Override
    public ObjectClass createObjectClass(String name, String parent) throws GSimException {
        try {
            Frame parentFrame = parent != null ? env.getContainer().getObjectSubClass(parent) : env.getContainer().getObjectClass();

            if (parentFrame == null) {
            	throw new GSimException("No parent with name " + parent + " found!");
            }

            Frame newObjectClass = env.getObjectClassOperations().createObjectSubClass(name, parentFrame);
            return new ObjectClassDef(env, newObjectClass);

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectInstance createObjectInstance(String name, ObjectClass parent) throws GSimException {
        try {
            Frame f = (Frame) ((UnitWrapper) parent).toUnit();
            return new ObjectInstanceDef(env, env.getObjectInstanceOperations().instanciateObject(f, name));
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    //TODO either make HierachyTree to API object or delete this method
    public HierarchyTree exportAgentHierarchy() throws GSimException {
        try {
            return env.exportAgentHierarchy();
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    //TODO either make HierachyTree to API object or delete this method
    public HierarchyTree exportObjectHierarchy() throws GSimException {
        try {
            return env.exportObjectHierarchy();
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public AgentInstance getAgent(String name) throws GSimException {
        try {
            GenericAgent a = env.getAgentInstanceOperations().getAgent(name);

            if (a == null) {
            	throw new GSimException("No agent with name " + name + " found!");
            }

            AgentInstanceDef agent = new AgentInstanceDef(env, a);

            return agent;

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentClass getAgentClass(String name) throws GSimException {
        try {

            GenericAgentClass a = env.getAgentClassOperations().getAgentSubClass(name);

            if (a == null) {
            	throw new GSimException("No agent with name " + name + " found!");
            }

            return new AgentClassDef(env, a);
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentClass[] getAgentClasses(String parent) throws GSimException {
        try {
            if (parent == null) {
                List<GenericAgentClass> gc = env.getAgentClassOperations().getAgentSubClasses();
                AgentClassDef[] res = new AgentClassDef[gc.size()];
                
                for (int i = 0; i < gc.size(); i++) {
                    AgentClassDef ac = new AgentClassDef(env, gc.get(i));
                    res[i] = ac;
                }
                return res;
            } else {
                List<GenericAgentClass> cls = env.getAgentClassOperations().getAllAgentClassSuccessors(parent);
                AgentClassDef[] res = new AgentClassDef[cls.size()];
                for (int i = 0; i < cls.size(); i++) {
                    res[i] = new AgentClassDef(env, cls.get(i));
                }
                return res;
            }
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public String[] getAgentNames(String parent) throws GSimException {
        try {
            return env.getAgentClassOperations().getAgentNames(parent).toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    @Override
    public AgentInstance[] getAgents(String parent) throws GSimException {
        try {
            GenericAgent[] f = env.getAgentClassOperations().getAgents(parent).toArray(new GenericAgent[0]);

            if (parent == null) {
                AgentInstanceDef[] res = new AgentInstanceDef[f.length];
                for (int i = 0; i < f.length; i++) {
                    AgentInstanceDef ac = new AgentInstanceDef(env, f[i]);
                    res[i] = ac;
                }
                return res;
            } else {
                ArrayList<AgentInstanceDef> list = new ArrayList<AgentInstanceDef>();
                for (int i = 0; i < f.length; i++) {
                    if (f[i].inheritsFrom(parent)) {
                        list.add(new AgentInstanceDef(env, f[i]));
                    }
                }
                AgentInstance[] agent = new AgentInstance[list.size()];
                list.toArray(agent);
                return agent;
            }
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public AgentInstance[] getAgents(String parent, int offset, int count) throws GSimException {
        try {

            GenericAgent[] f = env.getAgentInstanceOperations().getAgents(parent, offset, count).toArray(new GenericAgent[0]);

            if (f == null) {
                return null;
            }

            if (parent == null) {
                AgentInstanceDef[] res = new AgentInstanceDef[f.length];
                for (int i = 0; i < f.length; i++) {
                    AgentInstanceDef ac = new AgentInstanceDef(env, f[i]);
                    res[i] = ac;
                }
                return res;
            } else {
                ArrayList<AgentInstanceDef> list = new ArrayList<AgentInstanceDef>();
                for (int i = 0; i < f.length; i++) {
                    if (f[i].inheritsFrom(parent)) {
                        list.add(new AgentInstanceDef(env, f[i]));
                    }
                }
                AgentInstance[] agent = new AgentInstance[list.size()];
                list.toArray(agent);
                return agent;
            }
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    public Environment getComplicatedInterface() {
        return env;
    }

    @Override
    public ObjectClass getObjectClass(String name) throws GSimException {
        try {
            Frame a = env.getObjectClassOperations().getObjectSubClass(name);

            if (a == null) {
                return null; // if proxy mode to prevent too massive
                             // serialisation!
            }

            return new ObjectClassDef(env, a);
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectClass[] getObjectClasses(String parent) throws GSimException {
        try {
            if (parent == null) {
                Frame[] f = env.getObjectClassOperations().getObjectSubClasses().toArray(new Frame[0]);

                if (f == null) {
                    return null; // if proxy mode to prevent too massive
                                 // serialisation!
                }

                ObjectClassDef[] res = new ObjectClassDef[f.length];
                for (int i = 0; i < f.length; i++) {
                    ObjectClassDef ac = new ObjectClassDef(env, f[i]);
                    res[i] = ac;
                }
                return res;
            } else {
                Frame[] cls = env.getObjectClassOperations().getAllSuccessors(parent).toArray(new Frame[0]);
                ObjectClassDef[] res = new ObjectClassDef[cls.length];
                for (int i = 0; i < cls.length; i++) {
                    res[i] = new ObjectClassDef(env, cls[i]);
                }
                return res;
            }
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectInstance[] getObjects(String parent) throws GSimException {
        try {

            ArrayList<ObjectInstanceDef> res = new ArrayList<ObjectInstanceDef>();
            Frame[] f;
            if (parent == null) {
                f = env.getObjectClassOperations().getObjectSubClasses().toArray(new Frame[0]);
            } else {
                f = env.getObjectClassOperations().getAllSuccessors(parent).toArray(new Frame[0]);
            }

            for (int i = 0; i < f.length; i++) {

                List<Instance> in = env.getObjectInstanceOperations().getInstancesOfClass(f[i]);

                for (Instance t : in) {
                    ObjectInstanceDef ac = new ObjectInstanceDef(env, t);
                    res.add(ac);
                }

            }

            if (parent != null) {
                Frame tf = env.getObjectClassOperations().getObjectSubClass(parent);
                List<Instance> in = env.getObjectInstanceOperations().getInstancesOfClass(tf);
                for (Instance t : in) {
                    ObjectInstanceDef ac = new ObjectInstanceDef(env, t);
                    res.add(ac);
                }
            }

            ObjectInstance[] ra = new ObjectInstance[res.size()];
            res.toArray(ra);
            return ra;

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public AgentClass getTopAgentClass() throws GSimException {
        try {

            GenericAgentClass g = env.getAgentClassOperations().getGenericAgentClass();

            if (g == null) {
                return null; // if proxy mode to prevent too massive
                             // serialisation!
            }

            return new AgentClassDef(env, g);
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectClass getTopObjectClass() throws GSimException {
        try {
            return new ObjectClassDef(env, env.getObjectClassOperations().getObjectClass());
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentInstance instanciateAgent(AgentClass parent, String name) throws GSimException {
        try {
            GenericAgent a = env.getAgentInstanceOperations().instanciateAgentWithUniformDistributedAttributes((GenericAgentClass) ((UnitWrapper) parent).toUnit(), name);
            AgentInstanceDef agent = new AgentInstanceDef(env, a);
            return agent;
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentInstance[] instanciateAgentsNormallyDistributed(AgentClass parent, String prefix, double svar, int count) throws GSimException {
        try {
            GenericAgent[] a = env.getAgentInstanceOperations().instanciateAgentsWithNormalDistributedAttributes((GenericAgentClass) ((UnitWrapper) parent).toUnit(), Optional.of(prefix), svar, count).toArray(new GenericAgent[0]);
            		

            AgentInstanceDef[] agents = new AgentInstanceDef[a.length];
            for (int i = 0; i < a.length; i++) {
                agents[i] = new AgentInstanceDef(env, a[i]);
            }
            return agents;
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public AgentInstance[] instanciateAgentsUniformDistributed(AgentClass parent, String prefix, int count) throws GSimException {
        try {
            GenericAgent[] a = env.getAgentInstanceOperations()
            		.instanciateAgentsWithUniformDistributedAttributes((GenericAgentClass) ((UnitWrapper) parent).toUnit(), Optional.of(prefix), count).toArray(new GenericAgent[0]);
            		

            AgentInstanceDef[] agents = new AgentInstanceDef[a.length];
            for (int i = 0; i < a.length; i++) {
                agents[i] = new AgentInstanceDef(env, a[i]);
            }
            return agents;
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public void removeAgentInstances() {
        try {
            for (String a : env.getAgentInstanceOperations().getAgentNames()) {
                env.getAgentInstanceOperations().removeGenericAgent(env.getAgentInstanceOperations().getAgent(a));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
    	this.env = null;
    }


}
