package gsim.objects.impl;

import java.util.ArrayList;
import java.util.Iterator;

import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import gsim.def.objects.Frame;
import gsim.def.objects.TypedList;
import gsim.def.objects.agent.BehaviourFrame;
import gsim.def.objects.agent.GenericAgentClass;

public class AgentClassSim implements AgentClass, ObjectClass {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private GenericAgentClass real;

    public AgentClassSim(GenericAgentClass real) {
        this.real = real;
    }

    @Override
    public void addAttribute(String list, DomainAttribute a) throws GSimObjectException {
        try {
            real.addOrSetAttribute(list, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public void addOrSetObject(String list, ObjectClass object) throws GSimObjectException {

        try {
            real.addChildFrame(list, (Frame) ((UnitWrapper) object).toUnit());
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
        throw new GSimObjectException("You can't delete this frame here! It belongs to an agent active in a simulation...");
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public DomainAttribute getAttribute(String list, String attName) throws GSimObjectException {
        try {
            return real.getAttribute(list, attName);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public String[] getAttributeListNames() throws GSimObjectException {
        try {
            return real.getAttributesListNames();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public DomainAttribute[] getAttributes(String list) throws GSimObjectException {
        try {
            return real.getAttributes(list);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public Behaviour getBehaviour() throws GSimObjectException {

        try {
            return new BehaviourClass(this, real.getBehaviour());
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public String getDefaultValue(String list, String attName) throws GSimObjectException {

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            return a.getDefaultValue();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public String getName() throws GSimObjectException {

        try {
            return real.getTypeName();
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
    public ObjectClass getObjectListType(String listName) throws GSimObjectException {
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
    public ObjectClass[] getObjects(String list) throws GSimObjectException {

        try {
            Frame[] f = real.getChildFrames(list);
            ObjectClass[] ret = new ObjectClass[f.length];
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
    public void removeObject(String list, ObjectClass object) throws GSimObjectException {

        try {
            real.removeChildFrame(list, object.getName());
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

        try {
            real.removeChildFrame(list, objectName);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public Object resolveName(String path) throws GSimObjectException {

        try {

            String[] p = path.split("/");
            Object o = real.resolveName(p);

            if (o == null) {
                return null;
            }

            if (o instanceof Attribute) {
                return o;
            } else if (o instanceof Frame) {
                return new ChildObjectClass(this, p[0], (Frame) o);
            } else if (o instanceof TypedList) {
                TypedList list = (TypedList) o;
                ArrayList<ChildObjectClass> ret = new ArrayList<ChildObjectClass>();
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    Frame f = (Frame) iter.next();
                    ChildObjectClass c = new ChildObjectClass(this, p[0], f);
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

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public void setAttribute(String list, DomainAttribute a) throws GSimObjectException {

        try {
            real.addOrSetAttribute(list, a);
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
    public void setBehaviour(Behaviour b) throws GSimObjectException {

        try {
            if (!(b instanceof BehaviourClass)) {
                throw new GSimObjectException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
            }
            real.setBehaviour((BehaviourFrame) ((UnitWrapper) b).toUnit());
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public void setDefaultAttributeValue(String list, String attName, String value) throws GSimObjectException {

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            a.setDefault(value);
            real.addOrSetAttribute(list, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

}
