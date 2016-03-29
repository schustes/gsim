package de.s2.gsim.api.impl;

import java.util.ArrayList;
import java.util.List;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.AgentClassDef;
import de.s2.gsim.api.objects.impl.AgentInstanceDef;
import de.s2.gsim.api.objects.impl.ObjectClassDef;
import de.s2.gsim.api.objects.impl.ObjectInstanceDef;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.def.Environment;
import de.s2.gsim.def.InheritanceHierarchy;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;
import de.s2.gsim.def.objects.agent.GenericAgent;
import de.s2.gsim.def.objects.agent.GenericAgentClass;
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
            GenericAgentClass g = parent != null ? env.getAgentSubClass(parent) : env.getGenericAgentClass();

            if (g == null) {
                g = env.getAgentClass();
            }

            if (g == null) {
                throw new GSimException("No parent with name " + parent + " found.");
            }

            GenericAgentClass newAgentClass = env.createAgentSubclass(name, g);
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
            FrameOLD parentFrame = parent != null ? env.getObjectSubClass(parent) : env.getObjectClass();

            if (parentFrame == null) {
            	throw new GSimException("No parent with name " + parent + " found!");
            }

            FrameOLD newObjectClass = env.createObjectSubClass(name, parentFrame);
            return new ObjectClassDef(env, newObjectClass);

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectInstance createObjectInstance(String name, ObjectClass parent) throws GSimException {
        try {
            FrameOLD f = (FrameOLD) ((UnitWrapper) parent).toUnit();
            return new ObjectInstanceDef(env, env.instanciateFrame(f, name));
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    public InheritanceHierarchy[] exportAgentHierarchy() throws GSimException {
        try {
            return env.exportAgentHierarchy();
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    public InheritanceHierarchy[] exportObjectHierarchy() throws GSimException {
        try {
            return env.exportObjectHierarchy();
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public AgentInstance getAgent(String name) throws GSimException {
        try {
            GenericAgent a = env.getAgent(name);

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

            GenericAgentClass a = env.getAgentSubClass(name);

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
                GenericAgentClass[] gc = env.getAgentSubClasses();
                AgentClassDef[] res = new AgentClassDef[gc.length];
                
//                List<GenericAgentClass> list = new ArrayList<>();
//                List<AgentClassDef> defList = new ArrayList<>();
//                list.stream().parallel().forEach(real -> defList.add(new AgentClassDef(env, real)));
                
                for (int i = 0; i < gc.length; i++) {
                    AgentClassDef ac = new AgentClassDef(env, gc[i]);
                    res[i] = ac;
                }
                return res;
            } else {
                GenericAgentClass[] cls = env.getAllAgentClassSuccessors(parent);
                AgentClassDef[] res = new AgentClassDef[cls.length];
                for (int i = 0; i < cls.length; i++) {
                    res[i] = new AgentClassDef(env, cls[i]);
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
            return env.getAgentNames(parent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    @Override
    public AgentInstance[] getAgents(String parent) throws GSimException {
        try {
            GenericAgent[] f = env.getAgents(parent);

            if (f == null) {
                return null; // if proxy mode to prevent too massive
                             // serialisation!
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

    @Override
    public AgentInstance[] getAgents(String parent, int offset, int count) throws GSimException {
        try {

            GenericAgent[] f = env.getAgents(parent, offset, count);

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
            FrameOLD a = env.getObjectSubClass(name);

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
                FrameOLD[] f = env.getObjectSubClasses();

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
                FrameOLD[] cls = env.getAllObjectSuccessors(parent);
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
            FrameOLD[] f;
            if (parent == null) {
                f = env.getObjectSubClasses();
            } else {
                f = env.getAllObjectSuccessors(parent);
            }

            for (int i = 0; i < f.length; i++) {

                ArrayList<InstanceOLD> in = env.getInstancesOfClass(f[i]);

                for (InstanceOLD t : in) {
                    ObjectInstanceDef ac = new ObjectInstanceDef(env, t);
                    res.add(ac);
                }

            }

            if (parent != null) {
                FrameOLD tf = env.getObjectSubClass(parent);
                ArrayList<InstanceOLD> in = env.getInstancesOfClass(tf);
                for (InstanceOLD t : in) {
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

            GenericAgentClass g = env.getGenericAgentClass();

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
            return new ObjectClassDef(env, env.getObjectClass());
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentInstance instanciateAgent(AgentClass parent, String name) throws GSimException {
        try {
            GenericAgent a = env.instanciateAgent((GenericAgentClass) ((UnitWrapper) parent).toUnit(), name, Environment.RAND_NONE, 0);
            if (a == null) {
                return null; // if proxy mode to prevent too massive
            }
            // serialisation!
            AgentInstanceDef agent = new AgentInstanceDef(env, a);
            return agent;
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentInstance[] instanciateAgents(AgentClass parent, String prefix, int method, double svar, int count) throws GSimException {
        try {
            GenericAgent[] a = env.instanciateAgents((GenericAgentClass) ((UnitWrapper) parent).toUnit(), prefix, method, svar, count);

            if (a == null) {
                return null; // if proxy mode to prevent too massive
                             // serialisation!
            }

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
    public void instanciateAgents2(AgentClass parent, String prefix, int method, double svar, int count) throws GSimException {
        try {
            env.instanciateAgents2((GenericAgentClass) ((UnitWrapper) parent).toUnit(), prefix, method, svar, count);

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public void removeAgentInstances() {
        try {
            for (String a : env.getAgentNames()) {
                env.removeGenericAgent(env.getAgent(a));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
