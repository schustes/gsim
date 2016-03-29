package de.s2.gsim.api.objects.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.s2.gsim.GSimException;
import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;
import de.s2.gsim.def.objects.TypedListOLD;
import de.s2.gsim.def.objects.UnitOLD;
import de.s2.gsim.def.objects.UnitUtils;
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

public class AgentInstanceSim implements AgentInstance, ObjectInstance, UnitWrapper, Serializable {

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
    public void addOrSetObject(String list, ObjectInstance object) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            GenericAgent a = real;
            a.addChildInstance(list, (InstanceOLD) object);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public ObjectInstance copy() {
        GenericAgent copyAgent = new GenericAgent(real);
        return new AgentInstanceSim(copyAgent);
    }

    @Override
    public ObjectInstance createObjectFromListType(String objectName, String listName) {
        FrameOLD f = real.getDefinition().getListType(listName);
        InstanceOLD instance = new InstanceOLD(objectName, f);
        return new ChildObjectInstance(this, listName, instance);
        // return new ObjectInstanceSim(instance);
    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void destroy() throws GSimException {

        throw new GSimException("You can't delete an agent from a running simulation with this mechanism");

    }

    @Override
    public Attribute getAttribute(String attName) throws GSimException {

        try {

            return real.getAttribute(attName);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public Attribute getAttribute(String list, String attName) throws GSimException {

        try {

            return real.getAttribute(list, attName);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public String[] getAttributeListNames() throws GSimException {

        try {

            return real.getAttributesListNames();

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public Attribute[] getAttributes(String list) throws GSimException {

        try {
            return real.getAttributes(list);
        } catch (Exception e) {
            throw new GSimException(e);
        }

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
            GenericAgent a = real;
            return new BehaviourInstance(this, a.getBehaviour());
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public double getIntervalAttributeFrom(String list, String attName) throws GSimException {

        try {
            IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
            return a.getFrom();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public double getIntervalAttributeTo(String list, String attName) throws GSimException {

        try {
            IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
            return a.getTo();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
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
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public double getNumericalAttribute(String list, String attName) throws GSimException {

        try {
            NumericalAttribute a = (NumericalAttribute) real.getAttribute(list, attName);
            return a.getValue();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public ObjectInstance getObject(String list, String objectName) throws GSimException {
        InstanceOLD in = real.getChildInstance(list, objectName);
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
            GenericAgent a = real;
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

            InstanceOLD[] f = real.getChildInstances(list);
            ObjectInstance[] ret = new ObjectInstance[f.length];
            for (int i = 0; i < f.length; i++) {
                ret[i] = new ChildObjectInstance(this, list, f[i]);
            }
            return ret;

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public String[] getSetAttributeValues(String list, String attName) throws GSimException {

        try {
            SetAttribute set = (SetAttribute) real.getAttribute(list, attName);
            List<String> l = set.getEntries();
            String[] ret = new String[l.size()];
            l.toArray(ret);
            return ret;
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public String getStringAttribute(String list, String attName) throws GSimException {

        try {
            StringAttribute a = (StringAttribute) real.getAttribute(list, attName);
            return a.getValue();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public boolean inheritsFrom(String agentclassName) {
        return real.inheritsFrom(agentclassName);
    }

    @Override
    public void removeAllObjects(String list) {
        FrameOLD type = real.getDefinition().getListType(list);
        real.removeChildInstanceList(list);
        real.defineObjectList(list, type);
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
            GenericAgent a = real;
            UnitUtils.getInstance().removeChildInstance(a, new String[] { list }, object.getName());
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
            UnitUtils.getInstance().removeChildInstance(real, new String[] { list }, objectName);
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

            String[] p = path.split("/");

            Object o = real.resolveName(p);

            if (o == null) {
                return null;
            }

            if (o instanceof Attribute) {
                return o;
            } else if (o instanceof InstanceOLD) {
                return new ChildObjectInstance(this, p[0], (InstanceOLD) o);
            } else if (o instanceof TypedListOLD) {
                TypedListOLD list = (TypedListOLD) o;
                ArrayList<ChildObjectInstance> ret = new ArrayList<ChildObjectInstance>();
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    InstanceOLD f = (InstanceOLD) iter.next();
                    ChildObjectInstance c = new ChildObjectInstance(this, p[0], f);
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

    public void setAgentInstance(GenericAgent real) {
        this.real = real;
    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void setAttribute(String list, Attribute a) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real.setAttribute(list, a);
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

            if (!(b instanceof BehaviourInstance && b instanceof BehaviourClass)) {
                throw new GSimException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
            }

            GenericAgent a = real;
            a.setBehaviour((BehaviourDef) ((UnitWrapper) b).toUnit());

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
            real.setAttribute(list, a);
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
            real.setAttribute(list, a);
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
                FrameOLD f = real.getDefinition();
                DomainAttribute def = f.getAttribute(list, attName);
                a = new SetAttribute(attName, def.getFillers());
            }
            a.removeAllEntries();
            for (String v : values) {
                a.addEntry(v);
            }
            real.setAttribute(list, a);
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
            real.setAttribute(list, a);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public UnitOLD toUnit() {
        return real;
    }

}
