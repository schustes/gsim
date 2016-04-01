package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;
import java.util.Iterator;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class AgentClassDef extends ObjectClassDef implements AgentClass, UnitWrapper {

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
    public AgentClassDef(Environment env, GenericAgentClass real) {
        // this.env=env;
        // this.real=real;
        super(env, real);
    }

    @Override
    public void addAttribute(String list, DomainAttribute a) throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real = env.addAgentClassAttribute((GenericAgentClass) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public void addOrSetObject(String list, ObjectClass object) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real = env.addChildObject((GenericAgentClass) real, new String[] { list }, (Frame) ((UnitWrapper) object).toUnit());
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public void destroy() throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            env.removeAgentClass((GenericAgentClass) real);
        } catch (Exception e) {
            throw new GSimException(e);
        }

        destroyed = true;
    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public Behaviour getBehaviour() throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            return new BehaviourClass(this, ((GenericAgentClass) real).getBehaviour());
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public String[] getObjectListNames() throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            return real.getChildFrameListNames();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public ObjectClass getObjectListType(String listName) throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            return new ChildObjectClass(this, listName, real.getListType(listName));
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public ObjectClass[] getObjects(String list) throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            Frame[] f = real.getChildFrames(list);
            ObjectClass[] ret = new ObjectClass[f.length];
            for (int i = 0; i < f.length; i++) {
                ret[i] = new ChildObjectClass(this, list, f[i]);
            }
            return ret;
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public boolean isDeclaredAttribute(String list, String attName) throws GSimException {
        return real.isDeclaredAttribute(list, attName);
    }

    @Override
    public boolean isDeclaredObject(String list, String objectName) throws GSimException {
        return real.getDeclaredAttribute(list, objectName) == null;
    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public void removeObject(String list, ObjectClass object) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real = env.removeChildFrame((GenericAgentClass) real, new String[] { list }, object.getName());
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public void removeObject(String list, String objectName) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real = env.removeChildFrame((GenericAgentClass) real, new String[] { list }, objectName);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public Object resolveName(String path) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            Object o = real.resolveName(path.split("/"));

            if (o == null) {
                return null;
            }

            if (o instanceof DomainAttribute) {
                return o;
            } else if (o instanceof Frame) {
                return new ObjectClassDef(env, (Frame) o);
            } else if (o instanceof TypedList) {
                TypedList list = (TypedList) o;
                ArrayList<ObjectClassDef> ret = new ArrayList<ObjectClassDef>();
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    Frame f = (Frame) iter.next();
                    ObjectClassDef c = new ObjectClassDef(env, f);
                    ret.add(c);
                }
                return ret;

            } else if (o instanceof ArrayList) {
                return o;
            } else {
                throw new GSimException("Can't handle return value " + o);
            }
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public void setAttribute(String list, DomainAttribute a) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            lock();
            real = env.modifyAgentClassAttribute((GenericAgentClass) real, new String[] { list }, a);
            unlock();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * 
     * @param b
     *            BehaviourIF
     * @throws GSimException
     */
    @Override
    public void setBehaviour(Behaviour b) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context..");
        }

        try {
            if (!(b instanceof BehaviourClass)) {
                throw new GSimException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
            }
            real = env.changeAgentClassBehaviour((GenericAgentClass) real, (BehaviourFrame) ((UnitWrapper) b).toUnit());
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public void setDefaultAttributeValue(String list, String attName, String value) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            lock();
            DomainAttribute a = real.getAttribute(list, attName);
            a.setDefault(value);
            real = env.modifyAgentClassAttribute((GenericAgentClass) real, new String[] { list }, a);
            unlock();
        } catch (Exception e) {
            throw new GSimException(e);
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
