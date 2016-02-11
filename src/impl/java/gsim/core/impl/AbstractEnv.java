package gsim.core.impl;

import java.util.ArrayList;

import de.s2.gsim.core.DefinitionEnvironment;
import de.s2.gsim.core.GSimException;
import de.s2.gsim.objects.AgentClassIF;
import de.s2.gsim.objects.AgentInstanceIF;
import de.s2.gsim.objects.ObjectClassIF;
import de.s2.gsim.objects.ObjectInstanceIF;
import gsim.def.Environment;
import gsim.def.InheritanceHierarchy;
import gsim.def.objects.Frame;
import gsim.def.objects.Instance;
import gsim.def.objects.agent.GenericAgent;
import gsim.def.objects.agent.GenericAgentClass;
import gsim.objects.impl.AgentClass;
import gsim.objects.impl.AgentInstance;
import gsim.objects.impl.ObjectClass;
import gsim.objects.impl.ObjectInstance;
import gsim.objects.impl.UnitWrapper;

public abstract class AbstractEnv implements DefinitionEnvironment {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected Environment env;

    public AbstractEnv(Environment env) {
        this.env = env;
    }

    @Override
    public AgentClassIF createAgentClass(String name, String parent) throws GSimException {
        try {
            GenericAgentClass g = parent != null ? env.getAgentSubClass(parent) : env.getGenericAgentClass();

            if (g == null) {
                g = env.getAgentClass();
            }

            if (g == null) {
                throw new GSimException("No parent with name " + parent + " found.");
            }

            GenericAgentClass newAgentClass = env.createAgentSubclass(name, g);
            return new AgentClass(env, newAgentClass);

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentClassIF createAgentClass(String name, String parentName, int order) throws GSimException {
        AgentClassIF retVal = this.createAgentClass(name, parentName);
        env.addOrSetAgentOrdering(order, name);
        return retVal;
    }

    @Override
    public ObjectClassIF createObjectClass(String name, String parent) throws GSimException {
        try {
            Frame g = name != null ? env.getObjectSubClass(name) : env.getObjectClass();

            if (g == null) {
                g = env.getObjectClass();
                if (!g.getTypeName().equals(parent)) {
                    throw new GSimException("No parent with name " + parent + " found, and top-object=" + g.getTypeName() + ", param=" + parent);
                }
            }

            if (g == null) {
                throw new GSimException("No parent with name " + parent + " found.");
            }

            Frame newObjectClass = env.createObjectSubClass(name, g);
            return new ObjectClass(env, newObjectClass);

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectInstanceIF createObjectInstance(String name, ObjectClassIF parent) throws GSimException {
        try {
            Frame f = (Frame) ((UnitWrapper) parent).toUnit();
            return new ObjectInstance(env, env.instanciateFrame(f, name));
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public abstract void destroy();

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
    public AgentInstanceIF getAgent(String name) throws GSimException {
        try {
            GenericAgent a = env.getAgent(name);

            if (a == null) {
                return null;
            }

            AgentInstance agent = new AgentInstance(env, a);

            return agent;

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentClassIF getAgentClass(String name) throws GSimException {
        try {

            GenericAgentClass a = env.getAgentSubClass(name);

            if (a == null) {
                return null; // if proxy mode to prevent too massive
                             // serialisation!
            }

            return new AgentClass(env, a);
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentClassIF[] getAgentClasses(String parent) throws GSimException {
        try {
            if (parent == null) {
                GenericAgentClass[] gc = env.getAgentSubClasses();
                AgentClass[] res = new AgentClass[gc.length];

                if (res == null) {
                    return null; // if proxy mode to prevent too massive
                                 // serialisation!
                }

                for (int i = 0; i < gc.length; i++) {
                    AgentClass ac = new AgentClass(env, gc[i]);
                    res[i] = ac;
                }
                return res;
            } else {
                GenericAgentClass[] cls = env.getAllAgentClassSuccessors(parent);
                AgentClass[] res = new AgentClass[cls.length];
                for (int i = 0; i < cls.length; i++) {
                    res[i] = new AgentClass(env, cls[i]);
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
    public AgentInstanceIF[] getAgents(String parent) throws GSimException {
        try {
            GenericAgent[] f = env.getAgents(parent);

            if (f == null) {
                return null; // if proxy mode to prevent too massive
                             // serialisation!
            }

            if (parent == null) {
                AgentInstance[] res = new AgentInstance[f.length];
                for (int i = 0; i < f.length; i++) {
                    AgentInstance ac = new AgentInstance(env, f[i]);
                    res[i] = ac;
                }
                return res;
            } else {
                ArrayList<AgentInstance> list = new ArrayList<AgentInstance>();
                for (int i = 0; i < f.length; i++) {
                    if (f[i].inheritsFrom(parent)) {
                        list.add(new AgentInstance(env, f[i]));
                    }
                }
                AgentInstanceIF[] agent = new AgentInstanceIF[list.size()];
                list.toArray(agent);
                return agent;
            }
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public AgentInstanceIF[] getAgents(String parent, int offset, int count) throws GSimException {
        try {

            GenericAgent[] f = env.getAgents(parent, offset, count);

            if (f == null) {
                return null;
            }

            if (parent == null) {
                AgentInstance[] res = new AgentInstance[f.length];
                for (int i = 0; i < f.length; i++) {
                    AgentInstance ac = new AgentInstance(env, f[i]);
                    res[i] = ac;
                }
                return res;
            } else {
                ArrayList<AgentInstance> list = new ArrayList<AgentInstance>();
                for (int i = 0; i < f.length; i++) {
                    if (f[i].inheritsFrom(parent)) {
                        list.add(new AgentInstance(env, f[i]));
                    }
                }
                AgentInstanceIF[] agent = new AgentInstanceIF[list.size()];
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
    public ObjectClassIF getObjectClass(String name) throws GSimException {
        try {
            Frame a = env.getObjectSubClass(name);

            if (a == null) {
                return null; // if proxy mode to prevent too massive
                             // serialisation!
            }

            return new ObjectClass(env, a);
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectClassIF[] getObjectClasses(String parent) throws GSimException {
        try {
            if (parent == null) {
                Frame[] f = env.getObjectSubClasses();

                if (f == null) {
                    return null; // if proxy mode to prevent too massive
                                 // serialisation!
                }

                ObjectClass[] res = new ObjectClass[f.length];
                for (int i = 0; i < f.length; i++) {
                    ObjectClass ac = new ObjectClass(env, f[i]);
                    res[i] = ac;
                }
                return res;
            } else {
                Frame[] cls = env.getAllObjectSuccessors(parent);
                ObjectClass[] res = new ObjectClass[cls.length];
                for (int i = 0; i < cls.length; i++) {
                    res[i] = new ObjectClass(env, cls[i]);
                }
                return res;
            }
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectInstanceIF[] getObjects(String parent) throws GSimException {
        try {

            ArrayList<ObjectInstance> res = new ArrayList<ObjectInstance>();
            Frame[] f;
            if (parent == null) {
                f = env.getObjectSubClasses();
            } else {
                f = env.getAllObjectSuccessors(parent);
            }

            for (int i = 0; i < f.length; i++) {

                ArrayList<Instance> in = env.getInstancesOfClass(f[i]);

                for (Instance t : in) {
                    ObjectInstance ac = new ObjectInstance(env, t);
                    res.add(ac);
                }

            }

            if (parent != null) {
                Frame tf = env.getObjectSubClass(parent);
                ArrayList<Instance> in = env.getInstancesOfClass(tf);
                for (Instance t : in) {
                    ObjectInstance ac = new ObjectInstance(env, t);
                    res.add(ac);
                }
            }

            ObjectInstanceIF[] ra = new ObjectInstanceIF[res.size()];
            res.toArray(ra);
            return ra;

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public AgentClassIF getTopAgentClass() throws GSimException {
        try {

            GenericAgentClass g = env.getGenericAgentClass();

            if (g == null) {
                return null; // if proxy mode to prevent too massive
                             // serialisation!
            }

            return new AgentClass(env, g);
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectClassIF getTopObjectClass() throws GSimException {
        try {
            return new ObjectClass(env, env.getObjectClass());
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentInstanceIF instanciateAgent(AgentClassIF parent, String name) throws GSimException {
        try {
            GenericAgent a = env.instanciateAgent((GenericAgentClass) ((UnitWrapper) parent).toUnit(), name, Environment.RAND_NONE, 0);
            if (a == null) {
                return null; // if proxy mode to prevent too massive
            }
            // serialisation!
            AgentInstance agent = new AgentInstance(env, a);
            return agent;
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public AgentInstanceIF[] instanciateAgents(AgentClassIF parent, String prefix, int method, double svar, int count) throws GSimException {
        try {
            GenericAgent[] a = env.instanciateAgents((GenericAgentClass) ((UnitWrapper) parent).toUnit(), prefix, method, svar, count);

            if (a == null) {
                return null; // if proxy mode to prevent too massive
                             // serialisation!
            }

            AgentInstance[] agents = new AgentInstance[a.length];
            for (int i = 0; i < a.length; i++) {
                agents[i] = new AgentInstance(env, a[i]);
            }
            return agents;
        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }

    }

    @Override
    public void instanciateAgents2(AgentClassIF parent, String prefix, int method, double svar, int count) throws GSimException {
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

}
