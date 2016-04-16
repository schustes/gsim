package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.Path;
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;

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
    public void addAttribute(String list, DomainAttribute a) throws GSimException {
        try {
            real.addOrSetAttribute(list, a);
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

        try {
            real.addChildFrame(list, (Frame) ((UnitWrapper) object).toUnit());
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
        throw new GSimException("You can't delete this frame here! It belongs to an agent active in a simulation...");
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public DomainAttribute getAttribute(String list, String attName) throws GSimException {
        try {
            return real.getAttribute(list, attName);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public String[] getAttributeListNames() throws GSimException {
        try {
            return real.getAttributesListNames().toArray(new String[0]);
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public DomainAttribute[] getAttributes(String list) throws GSimException {
        try {
            return real.getAttributes(list).toArray(new DomainAttribute[0]);
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.AgentClassIF
     */
    @Override
    public Behaviour getBehaviour() throws GSimException {

        try {
            return new BehaviourClass(this, real.getBehaviour());
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public String getDefaultValue(String list, String attName) throws GSimException {

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            return a.getDefaultValue();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public String getName() throws GSimException {

        try {
            return real.getName();
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

        try {
            return real.getChildFrameListNames().toArray(new String[0]);
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

        try {
            List<Frame> f = real.getChildFrames(list);
            ObjectClass[] ret = new ObjectClass[f.size()];
            for (int i = 0; i < f.size(); i++) {
                ret[i] = new ChildObjectClass(this, list, f.get(i));
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

        try {
            real.removeChildFrame(list, object.getName());
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

        try {
            real.removeChildFrame(list, objectName);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public Object resolveName(String path) throws GSimException {

        try {

            String[] p = path.split("/");

			Object o = real.resolvePath(Path.attributePath(path.split("/")));
			if (o == null) {
				o = real.resolvePath(Path.attributeListPath(path.split("/")));
			} 
			if (o == null) {
				o = real.resolvePath(Path.objectPath(path.split("/")));
			}
			if (o == null) {
				o = real.resolvePath(Path.objectListPath(path.split("/")));
			}

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
                throw new GSimException("Can't handle return value " + o);
            }
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public void setAttribute(String list, DomainAttribute a) throws GSimException {

        try {
            real.addOrSetAttribute(list, a);
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

        try {
            if (!(b instanceof BehaviourClass)) {
                throw new GSimException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
            }
            real.setBehaviour((BehaviourFrame) ((UnitWrapper) b).toUnit());
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public void setDefaultAttributeValue(String list, String attName, String value) throws GSimException {

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            a.setDefault(value);
            real.addOrSetAttribute(list, a);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

}
