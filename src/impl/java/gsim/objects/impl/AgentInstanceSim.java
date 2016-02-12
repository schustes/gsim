package gsim.objects.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;
import gsim.def.objects.Frame;
import gsim.def.objects.Instance;
import gsim.def.objects.TypedList;
import gsim.def.objects.Unit;
import gsim.def.objects.UnitUtils;
import gsim.def.objects.agent.BehaviourDef;
import gsim.def.objects.agent.GenericAgent;

public class AgentInstanceSim implements AgentInstance, ObjectInstance, UnitWrapper, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean destroyed = false;

    private GenericAgent real;

    public AgentInstanceSim() {
    }

    public AgentInstanceSim(GenericAgent real) {
        this.real = real;
    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void addOrSetObject(String list, ObjectInstance object) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = real;
            a.addChildInstance(list, (Instance) object);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public ObjectInstance copy() {
        GenericAgent copyAgent = new GenericAgent(real);
        return new AgentInstanceSim(copyAgent);
    }

    @Override
    public ObjectInstance createObjectFromListType(String objectName, String listName) {
        Frame f = real.getDefinition().getListType(listName);
        Instance instance = new Instance(objectName, f);
        return new ChildObjectInstance(this, listName, instance);
        // return new ObjectInstanceSim(instance);
    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void destroy() throws GSimObjectException {

        throw new GSimObjectException("You can't delete an agent from a running simulation with this mechanism");

    }

    @Override
    public Attribute getAttribute(String attName) throws GSimObjectException {

        try {

            return real.getAttribute(attName);

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public Attribute getAttribute(String list, String attName) throws GSimObjectException {

        try {

            return real.getAttribute(list, attName);

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
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
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public Attribute[] getAttributes(String list) throws GSimObjectException {

        try {
            return real.getAttributes(list);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public Behaviour getBehaviour() throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = real;
            return new BehaviourInstance(this, a.getBehaviour());
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public double getIntervalAttributeFrom(String list, String attName) throws GSimObjectException {

        try {
            IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
            return a.getFrom();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public double getIntervalAttributeTo(String list, String attName) throws GSimObjectException {

        try {
            IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
            return a.getTo();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public String getName() throws GSimObjectException {

        try {
            return real.getName();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public double getNumericalAttribute(String list, String attName) throws GSimObjectException {

        try {
            NumericalAttribute a = (NumericalAttribute) real.getAttribute(list, attName);
            return a.getValue();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    @Override
    public ObjectInstance getObject(String list, String objectName) throws GSimObjectException {
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
            GenericAgent a = real;
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
    public ObjectInstance[] getObjects(String list) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {

            Instance[] f = real.getChildInstances(list);
            ObjectInstance[] ret = new ObjectInstance[f.length];
            for (int i = 0; i < f.length; i++) {
                ret[i] = new ChildObjectInstance(this, list, f[i]);
            }
            return ret;

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public String[] getSetAttributeValues(String list, String attName) throws GSimObjectException {

        try {
            SetAttribute set = (SetAttribute) real.getAttribute(list, attName);
            List<String> l = set.getEntries();
            String[] ret = new String[l.size()];
            l.toArray(ret);
            return ret;
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public String getStringAttribute(String list, String attName) throws GSimObjectException {

        try {
            StringAttribute a = (StringAttribute) real.getAttribute(list, attName);
            return a.getValue();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    @Override
    public boolean inheritsFrom(String agentclassName) {
        return real.inheritsFrom(agentclassName);
    }

    @Override
    public void removeAllObjects(String list) {
        Frame type = real.getDefinition().getListType(list);
        real.removeChildInstanceList(list);
        real.defineObjectList(list, type);
    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void removeObject(String list, ObjectInstance object) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = real;
            UnitUtils.getInstance().removeChildInstance(a, new String[] { list }, object.getName());
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
            UnitUtils.getInstance().removeChildInstance(real, new String[] { list }, objectName);
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

            String[] p = path.split("/");

            Object o = real.resolveName(p);

            if (o == null) {
                return null;
            }

            if (o instanceof Attribute) {
                return o;
            } else if (o instanceof Instance) {
                return new ChildObjectInstance(this, p[0], (Instance) o);
            } else if (o instanceof TypedList) {
                TypedList list = (TypedList) o;
                ArrayList<ChildObjectInstance> ret = new ArrayList<ChildObjectInstance>();
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    Instance f = (Instance) iter.next();
                    ChildObjectInstance c = new ChildObjectInstance(this, p[0], f);
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

    public void setAgentInstance(GenericAgent real) {
        this.real = real;
    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setAttribute(String list, Attribute a) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            real.setAttribute(list, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.AgentInstanceIF
     */
    @Override
    public void setBehaviour(Behaviour b) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {

            if (!(b instanceof BehaviourInstance && b instanceof BehaviourClass)) {
                throw new GSimObjectException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
            }

            GenericAgent a = real;
            a.setBehaviour((BehaviourDef) ((UnitWrapper) b).toUnit());

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
            real.setAttribute(list, a);
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
            real.setAttribute(list, a);
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
            real.setAttribute(list, a);
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
            real.setAttribute(list, a);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
