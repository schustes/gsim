package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.s2.gsim.GSimException;
import de.s2.gsim.def.Environment;
import de.s2.gsim.def.objects.Frame;
import de.s2.gsim.def.objects.Instance;
import de.s2.gsim.def.objects.TypedList;
import de.s2.gsim.def.objects.agent.BehaviourDef;
import de.s2.gsim.def.objects.agent.GenericAgent;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

public class AgentInstanceDef extends ObjectInstanceDef implements AgentInstance, UnitWrapper {

    private final static Logger logger = Logger.getLogger(AgentInstanceDef.class);
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AgentInstanceDef(Environment env, GenericAgent owner) {
        super(env, owner);
    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void addOrSetObject(String list, ObjectInstance object) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            real = env.addChildInstance(a, new String[] { list }, (Instance) object);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public ObjectInstance createObjectFromListType(String objectName, String listName) {
        Frame f = real.getDefinition().getListType(listName);
        Instance instance = new Instance(objectName, f);
        return new ChildObjectInstance(this, listName, instance);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AgentInstanceDef)) {
            return false;
        }

        try {
            return ((AgentInstance) o).getName().equals(getName());
        } catch (GSimException e) {
            logger.error("Error", e);
        }
        return false;
    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public Behaviour getBehaviour() throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            return new BehaviourInstance(this, a.getBehaviour());
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public ObjectInstance getObject(String list, String objectName) throws GSimException {
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
    public String[] getObjectListNames() throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            return a.getChildInstanceListNames();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public ObjectInstance[] getObjects(String list) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {

            Instance[] f = real.getChildInstances(list);
            ObjectInstance[] ret = new ObjectInstance[f.length];
            for (int i = 0; i < f.length; i++) {
                ret[i] = new ChildObjectInstance(this, list, f[i]);
            }
            return ret;

        } catch (Exception e) {
            throw new GSimException(e);
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
    public void removeObject(String list, ObjectInstance object) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            real = env.removeChildInstance(a, new String[] { list }, object.getName());
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void removeObject(String list, String objectName) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = (GenericAgent) real;
            real = env.removeChildInstance(a, new String[] { list }, objectName);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
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

            if (o instanceof Attribute) {
                return o;
            } else if (o instanceof Instance) {
                return new ObjectInstanceDef(env, (Instance) o);
            } else if (o instanceof TypedList) {
                TypedList list = (TypedList) o;
                ArrayList<ObjectInstanceDef> ret = new ArrayList<ObjectInstanceDef>();
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    Instance f = (Instance) iter.next();
                    ObjectInstanceDef c = new ObjectInstanceDef(env, f);
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
    public void setAttribute(String list, Attribute a) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real = env.modifyAgentAttribute((GenericAgent) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void setBehaviour(Behaviour b) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {

            if (!(b instanceof BehaviourInstance)) {
                throw new GSimException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
            }

            GenericAgent a = (GenericAgent) real;
            real = env.changeAgentBehaviour(a, (BehaviourDef) ((UnitWrapper) b).toUnit());

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setIntervalAttributeValue(String list, String attName, double from, double to) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
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
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setNumericalAttributeValue(String list, String attName, double value) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            NumericalAttribute a = (NumericalAttribute) real.getAttribute(list, attName);
            if (a == null) {
                a = new NumericalAttribute(attName, value);
            }
            a.setValue(value);
            real = env.modifyAgentAttribute((GenericAgent) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setSetAttributeValues(String list, String attName, String... values) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
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
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setStringAttributeValue(String list, String attName, String value) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            StringAttribute a = (StringAttribute) real.getAttribute(list, attName);
            if (a == null) {
                a = new StringAttribute(attName, value);
            }
            a.setValue(value);
            real = env.modifyAgentAttribute((GenericAgent) real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

}
