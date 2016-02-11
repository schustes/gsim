package gsim.objects.impl;

import java.util.ArrayList;
import java.util.Iterator;

import de.s2.gsim.objects.AgentClassIF;
import de.s2.gsim.objects.BehaviourIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.ObjectClassIF;
import de.s2.gsim.objects.attribute.DomainAttribute;
import gsim.def.Environment;
import gsim.def.objects.Frame;
import gsim.def.objects.TypedList;
import gsim.def.objects.Unit;
import gsim.def.objects.agent.BehaviourFrame;
import gsim.def.objects.agent.GenericAgentClass;

public class AgentClass extends ObjectClass implements AgentClassIF, UnitWrapper {

    // private Environment env;
    // private GenericAgentClass real;
    // private boolean destroyed=false;

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param env
     *            Environment
     * @param real
     *            GenericAgentClass
     */
    public AgentClass(Environment env, GenericAgentClass real) {
        // this.env=env;
        // this.real=real;
        super(env, real);
    }

    @Override
    public void addAttribute(String list, DomainAttribute a) throws GSimObjectException {
        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            real = env.addAgentClassAttribute((GenericAgentClass) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public void addOrSetObject(String list, ObjectClassIF object) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            real = env.addChildObject((GenericAgentClass) real, new String[] { list }, (Frame) ((UnitWrapper) object).toUnit());
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public void destroy() throws GSimObjectException {
        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            env.removeAgentClass((GenericAgentClass) real);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

        destroyed = true;
    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public BehaviourIF getBehaviour() throws GSimObjectException {
        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            return new BehaviourClass(this, ((GenericAgentClass) real).getBehaviour());
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public String[] getObjectListNames() throws GSimObjectException {
        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            return real.getChildFrameListNames();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public ObjectClassIF getObjectListType(String listName) throws GSimObjectException {
        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            return new ChildObjectClass(this, listName, real.getListType(listName));
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public ObjectClassIF[] getObjects(String list) throws GSimObjectException {
        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            Frame[] f = real.getChildFrames(list);
            ObjectClassIF[] ret = new ObjectClassIF[f.length];
            for (int i = 0; i < f.length; i++) {
                ret[i] = new ChildObjectClass(this, list, f[i]);
            }
            return ret;
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public boolean isDeclaredAttribute(String list, String attName) throws GSimObjectException {
        return real.isDeclaredAttribute(list, attName);
    }

    @Override
    public boolean isDeclaredObject(String list, String objectName) throws GSimObjectException {
        return real.getDeclaredAttribute(list, objectName) == null;
    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public void removeObject(String list, ObjectClassIF object) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            real = env.removeChildFrame((GenericAgentClass) real, new String[] { list }, object.getName());
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public void removeObject(String list, String objectName) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            real = env.removeChildFrame((GenericAgentClass) real, new String[] { list }, objectName);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public Object resolveName(String path) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            Object o = real.resolveName(path.split("/"));

            if (o == null) {
                return null;
            }

            if (o instanceof DomainAttribute) {
                return o;
            } else if (o instanceof Frame) {
                return new ObjectClass(env, (Frame) o);
            } else if (o instanceof TypedList) {
                TypedList list = (TypedList) o;
                ArrayList<ObjectClass> ret = new ArrayList<ObjectClass>();
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    Frame f = (Frame) iter.next();
                    ObjectClass c = new ObjectClass(env, f);
                    ret.add(c);
                }
                return ret;

            } else if (o instanceof ArrayList) {
                return o;
            } else {
                throw new GSimObjectException("Can't handle return value " + o);
            }
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    @Override
    public void setAttribute(String list, DomainAttribute a) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            lock();
            real = env.modifyAgentClassAttribute((GenericAgentClass) real, new String[] { list }, a);
            unlock();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * 
     * @param b
     *            BehaviourIF
     * @throws GSimObjectException
     */
    @Override
    public void setBehaviour(BehaviourIF b) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context..");
        }

        try {
            if (!(b instanceof BehaviourClass)) {
                throw new GSimObjectException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
            }
            real = env.changeAgentClassBehaviour((GenericAgentClass) real, (BehaviourFrame) ((UnitWrapper) b).toUnit());
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    @Override
    public void setDefaultAttributeValue(String list, String attName, String value) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            lock();
            DomainAttribute a = real.getAttribute(list, attName);
            a.setDefault(value);
            real = env.modifyAgentClassAttribute((GenericAgentClass) real, new String[] { list }, a);
            unlock();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public Unit toUnit() {
        return real;
    }

    private void lock() {

    }

    private void unlock() {

    }

}
