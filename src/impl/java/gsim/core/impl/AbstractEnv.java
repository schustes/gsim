package gsim.core.impl;

import java.util.ArrayList;

import de.s2.gsim.core.GSimException;
import de.s2.gsim.core.ModelDefinitionEnvironment;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.ObjectInstance;
import gsim.def.Environment;
import gsim.def.InheritanceHierarchy;
import gsim.def.objects.Frame;
import gsim.def.objects.Instance;
import gsim.def.objects.agent.GenericAgent;
import gsim.def.objects.agent.GenericAgentClass;
import gsim.objects.impl.AgentClassDef;
import gsim.objects.impl.AgentInstanceDef;
import gsim.objects.impl.ObjectClassDef;
import gsim.objects.impl.ObjectInstanceDef;
import gsim.objects.impl.UnitWrapper;

public abstract class AbstractEnv implements ModelDefinitionEnvironment {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected Environment env;

    public AbstractEnv(Environment env) {
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
            return new ObjectClassDef(env, newObjectClass);

        } catch (Exception e) {
            throw new GSimException("Exception", e);
        }
    }

    @Override
    public ObjectInstance createObjectInstance(String name, ObjectClass parent) throws GSimException {
        try {
            Frame f = (Frame) ((UnitWrapper) parent).toUnit();
            return new ObjectInstanceDef(env, env.instanciateFrame(f, name));
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
    public AgentInstance getAgent(String name) throws GSimException {
        try {
            GenericAgent a = env.getAgent(name);

            if (a == null) {
                return null;
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
                return null; // if proxy mode to prevent too massive
                             // serialisation!
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

                if (res == null) {
                    return null; // if proxy mode to prevent too massive
                                 // serialisation!
                }

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
            Frame a = env.getObjectSubClass(name);

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
                Frame[] f = env.getObjectSubClasses();

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
                Frame[] cls = env.getAllObjectSuccessors(parent);
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
                f = env.getObjectSubClasses();
            } else {
                f = env.getAllObjectSuccessors(parent);
            }

            for (int i = 0; i < f.length; i++) {

                ArrayList<Instance> in = env.getInstancesOfClass(f[i]);

                for (Instance t : in) {
                    ObjectInstanceDef ac = new ObjectInstanceDef(env, t);
                    res.add(ac);
                }

            }

            if (parent != null) {
                Frame tf = env.getObjectSubClass(parent);
                ArrayList<Instance> in = env.getInstancesOfClass(tf);
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

}
