package gsim.objects.impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.s2.gsim.objects.AgentInstanceIF;
import de.s2.gsim.objects.BehaviourIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.ObjectInstanceIF;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;
import gsim.def.Environment;
import gsim.def.objects.Frame;
import gsim.def.objects.Instance;
import gsim.def.objects.TypedList;
import gsim.def.objects.agent.Behaviour;
import gsim.def.objects.agent.GenericAgent;

public class AgentInstance extends ObjectInstance implements AgentInstanceIF, UnitWrapper {

    private final static Logger logger = Logger.getLogger(AgentInstance.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AgentInstance(Environment env, GenericAgent owner) {
        super(env, owner);
    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void addOrSetObject(String list, ObjectInstanceIF object) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            real = env.addChildInstance(a, new String[] { list }, (Instance) object);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public ObjectInstanceIF createObjectFromListType(String objectName, String listName) {
        Frame f = real.getDefinition().getListType(listName);
        Instance instance = new Instance(objectName, f);
        return new ChildObjectInstance(this, listName, instance);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AgentInstance)) {
            return false;
        }

        try {
            return ((AgentInstanceIF) o).getName().equals(getName());
        } catch (GSimObjectException e) {
            logger.error("Error", e);
        }
        return false;
    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public BehaviourIF getBehaviour() throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            return new BehaviourInstance(this, a.getBehaviour());
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public ObjectInstanceIF getObject(String list, String objectName) throws GSimObjectException {
        Instance in = real.getChildInstance(list, objectName);
        if (in != null) {
            return new ChildObjectInstance(this, list, in);
        } else {
            return null;
        }
    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public String[] getObjectListNames() throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            return a.getChildInstanceListNames();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public ObjectInstanceIF[] getObjects(String list) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {

            Instance[] f = real.getChildInstances(list);
            ObjectInstanceIF[] ret = new ObjectInstanceIF[f.length];
            for (int i = 0; i < f.length; i++) {
                ret[i] = new ChildObjectInstance(this, list, f[i]);
            }
            return ret;

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public void removeAllObjects(String list) {
        real.removeChildInstanceList(list);
        real.defineObjectList(list, real.getDefinition().getListType(list));
    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void removeObject(String list, ObjectInstanceIF object) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            real = env.removeChildInstance(a, new String[] { list }, object.getName());
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void removeObject(String list, String objectName) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            real = env.removeChildInstance(a, new String[] { list }, objectName);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
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

            if (o instanceof Attribute) {
                return o;
            } else if (o instanceof Instance) {
                return new ObjectInstance(env, (Instance) o);
            } else if (o instanceof TypedList) {
                TypedList list = (TypedList) o;
                ArrayList<ObjectInstance> ret = new ArrayList<ObjectInstance>();
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    Instance f = (Instance) iter.next();
                    ObjectInstance c = new ObjectInstance(env, f);
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
    public void setAttribute(String list, Attribute a) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            real = env.modifyAgentAttribute((GenericAgent) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void setBehaviour(BehaviourIF b) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {

            if (!(b instanceof BehaviourInstance)) {
                throw new GSimObjectException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
            }

            GenericAgent a = (GenericAgent) real;
            real = env.changeAgentBehaviour(a, (Behaviour) ((UnitWrapper) b).toUnit());

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setIntervalAttributeValue(String list, String attName, double from, double to) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
            if (a == null) {
                a = new IntervalAttribute(attName, from, to);
            }
            a.setFrom(from);
            a.setTo(to);
            real = env.modifyAgentAttribute((GenericAgent) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setNumericalAttributeValue(String list, String attName, double value) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            NumericalAttribute a = (NumericalAttribute) real.getAttribute(list, attName);
            if (a == null) {
                a = new NumericalAttribute(attName, value);
            }
            a.setValue(value);
            real = env.modifyAgentAttribute((GenericAgent) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setSetAttributeValues(String list, String attName, String... values) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            SetAttribute a = (SetAttribute) real.getAttribute(list, attName);
            if (a == null) {
                Frame f = real.getDefinition();
                DomainAttribute def = f.getAttribute(list, attName);
                a = new SetAttribute(attName, def.getFillers());
            }
            a.removeAllEntries();
            for (String v : values) {
                a.addEntry(v);
            }
            real = env.modifyAgentAttribute((GenericAgent) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setStringAttributeValue(String list, String attName, String value) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            StringAttribute a = (StringAttribute) real.getAttribute(list, attName);
            if (a == null) {
                a = new StringAttribute(attName, value);
            }
            a.setValue(value);
            real = env.modifyAgentAttribute((GenericAgent) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

}
