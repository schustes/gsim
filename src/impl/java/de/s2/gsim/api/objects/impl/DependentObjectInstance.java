package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;
import static de.s2.gsim.api.objects.impl.Invariant.*;

import java.util.List;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

/**
 * A dependent object instance wraps a child instance of an owning agent. The dependent object instance updates only its parent, which then may trigger more
 * notifications and events.
 */
public class DependentObjectInstance implements ObjectInstance, UnitWrapper, ManagedObject {

    private static final long serialVersionUID = 1L;

    private boolean destroyed = false;

    private AgentInstance env;

    private String list = "";

    private Instance real;

    public DependentObjectInstance(AgentInstance env, String list, Instance real) {
        this.env = env;
        this.real = real;
		this.list = list;
    }

    @Override
    public ObjectInstance copy() {
        Instance copy = Instance.copy(real);
        AgentInstance agent = (AgentInstance) env.copy();
        return new DependentObjectInstance(agent, list, copy);
    }

    @Override
    public void destroy() throws GSimException {

		precondition(this);

        try {
            env.removeObject(list, this);
            real = null;
        } catch (Exception e) {
            throw new GSimException(e);
        }

        destroyed = true;

    }

    @Override
    public Attribute getAttribute(String attName) throws GSimException {

		precondition(this, attName);

        try {
            return real.getAttribute(attName);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public Attribute getAttribute(String list, String attName) throws GSimException {

		precondition(this, list, attName);

        try {
            return real.getAttribute(list, attName);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public String[] getAttributeListNames() throws GSimException {

		precondition(this);

        try {
            return real.getAttributesListNames().toArray(new String[0]);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public Attribute[] getAttributes(String list) throws GSimException {

		precondition(this, list);

        try {
            return real.getAttributes(list).toArray(new Attribute[0]);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public double getIntervalAttributeFrom(String list, String attName) throws GSimException {

		precondition(this, list, attName);

        try {
            IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
            return a.getFrom();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public double getIntervalAttributeTo(String list, String attName) throws GSimException {

		precondition(this, list, attName);

        try {
            IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
            return a.getTo();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public String getName() throws GSimException {

		precondition(this);

        try {
            return real.getName();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public double getNumericalAttribute(String list, String attName) throws GSimException {

		precondition(this, list, attName);

        try {
            NumericalAttribute a = (NumericalAttribute) real.getAttribute(list, attName);
            return a.getValue();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public String[] getSetAttributeValues(String list, String attName) throws GSimException {

		precondition(this, list, attName);

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

    @Override
    public String getStringAttribute(String list, String attName) throws GSimException {

		precondition(this, list, attName);

        try {
            StringAttribute a = (StringAttribute) real.getAttribute(list, attName);
            return a.getValue();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public boolean inheritsFrom(String agentclassName) {

		precondition(this, agentclassName);

        return real.inheritsFromOrIsOfType(agentclassName);
    }

    @Override
    public Object resolveName(String path) throws GSimException {

		precondition(this, path);

        try {
			return real.resolvePath(Path.of(path.split("/")));
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public void setAttribute(String list, Attribute a) throws GSimException {

		precondition(this, a);

        try {
            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public void setIntervalAttributeValue(String list, String attName, double from, double to) throws GSimException {

		precondition(this, list, attName);

        try {
			IntervalAttribute a;
			if (!real.containsAttribute(list, attName)) {
                a = new IntervalAttribute(attName, from, to);
			} else {
				a = (IntervalAttribute) real.getAttribute(list, attName);
            }

            a.setFrom(from);
            a.setTo(to);

            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
	public void setNumericalAttributeValue(String list, String attName, double value) throws GSimException {

		precondition(this, list, attName);

        try {

			NumericalAttribute a;
			if (real.containsAttribute(list, attName)) {
                a = new NumericalAttribute(attName, value);
			} else {
				a = (NumericalAttribute) real.getAttribute(list, attName);
            }

            a.setValue(value);

            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
	public void setSetAttributeValues(String list, String attName, String... values) throws GSimException {

		precondition(this, list, attName);
		setIntegrity(values);

        try {
			SetAttribute a;
			if (real.containsAttribute(list, attName)) {
                Frame f = real.getDefinition();
                DomainAttribute def = f.getAttribute(list, attName);
                a = new SetAttribute(attName, def.getFillers());
			} else {
				a = (SetAttribute) real.getAttribute(list, attName);
            }

            a.removeAllEntries();

            for (String v : values) {
                a.addEntry(v);
            }

            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
	public void setStringAttributeValue(String list, String attName, String value) throws GSimException {

		precondition(this, list, attName, value);

        try {
        	StringAttribute a;
			if (!real.containsAttribute(list, attName)) {
        		a = new StringAttribute(attName, value);
        	} else {
        		a = (StringAttribute) real.getAttribute(list, attName);
        	}

            a.setValue(value);
            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public Unit<?,?> toUnit() {
        return real;
    }

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

}
