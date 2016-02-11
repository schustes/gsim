package gsim.objects.impl;

import java.util.ArrayList;
import java.util.List;

import de.s2.gsim.objects.AgentInstanceIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.ObjectInstanceIF;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;
import gsim.def.objects.Frame;
import gsim.def.objects.Instance;
import gsim.def.objects.Unit;

public class ChildObjectInstance implements ObjectInstanceIF, UnitWrapper {

    private static final long serialVersionUID = 1L;

    private boolean destroyed = false;

    private AgentInstanceIF env;

    private String list = "";

    private Instance real;

    public ChildObjectInstance(AgentInstanceIF env, String list, Instance real) {
        this.env = env;
        this.real = real;
    }

    @Override
    public ObjectInstanceIF copy() {
        Instance copy = new Instance(real);
        AgentInstanceIF agent = (AgentInstanceIF) env.copy();
        return new ChildObjectInstance(agent, list, copy);
    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public void destroy() throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            env.removeObject(list, this);
            real = null;
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

        destroyed = true;

    }

    @Override
    public Attribute getAttribute(String attName) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

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

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

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

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

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

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            return real.getAttributes(list);
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

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

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

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

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

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

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

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            NumericalAttribute a = (NumericalAttribute) real.getAttribute(list, attName);
            return a.getValue();
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

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

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

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

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

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
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

            if (o instanceof Attribute || o instanceof ArrayList) {
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
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public synchronized void setAttribute(String list, Attribute a) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            real.setAttribute(list, a);
            env.addOrSetObject(this.list, this);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public synchronized void setIntervalAttributeValue(String list, String attName, double from, double to) throws GSimObjectException {

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
            env.addOrSetObject(this.list, this);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public synchronized void setNumericalAttributeValue(String list, String attName, double value) throws GSimObjectException {

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
            env.addOrSetObject(this.list, this);

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public synchronized void setSetAttributeValues(String list, String attName, String... values) throws GSimObjectException {

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
            env.addOrSetObject(this.list, this);

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public synchronized void setStringAttributeValue(String list, String attName, String value) throws GSimObjectException {

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
            env.addOrSetObject(this.list, this);

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectInstanceIF
     */
    @Override
    public Unit toUnit() {
        return real;
    }

}
