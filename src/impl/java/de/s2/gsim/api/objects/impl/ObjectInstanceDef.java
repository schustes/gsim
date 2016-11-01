package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

/**
 * Implementation of definition object instance. Notifies any agents holding a relationship to it about changes.
 *
 */
public class ObjectInstanceDef extends Observable implements ObjectInstance, UnitWrapper {

    private static final long serialVersionUID = 1L;

    protected boolean destroyed = false;

    protected Environment env;

    protected Instance real;

    public ObjectInstanceDef(Environment env, Instance real) {
        this.env = env;
        this.real = real;
    }

    @Override
    public ObjectInstance copy() {
        Instance copy = Instance.copy(real);
        return new ObjectInstanceDef(env, copy);
    }

    @Override
    public void destroy() throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            env.getObjectInstanceOperations().removeObject(real);
            real = null;
        } catch (Exception e) {
            throw new GSimException(e);
        }

        destroyed = true;
        
        onDestroy();

    }

    @Override
    public Attribute getAttribute(String attName) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {

            return real.getAttribute(attName);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public Attribute getAttribute(String list, String attName) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {

            return real.getAttribute(list, attName);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public String[] getAttributeListNames() throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {

            return real.getAttributesListNames().toArray(new String[0]);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public Attribute[] getAttributes(String list) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            return real.getAttributes(list).toArray(new Attribute[0]);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public double getIntervalAttributeFrom(String list, String attName) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
            return a.getFrom();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public double getIntervalAttributeTo(String list, String attName) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
            return a.getTo();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public String getName() throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            return real.getName();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public double getNumericalAttribute(String list, String attName) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            NumericalAttribute a = (NumericalAttribute) real.getAttribute(list, attName);
            return a.getValue();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public String[] getSetAttributeValues(String list, String attName) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

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

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            StringAttribute a = (StringAttribute) real.getAttribute(list, attName);
            return a.getValue();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public boolean inheritsFrom(String agentclassName) {
        return real.inheritsFromOrIsOfType(agentclassName);
    }

    @Override
    public Object resolveName(String path) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {

        	Object o = real.resolvePath(Path.attributePath(path.split("/")));

			if (o == null) {
				o = real.resolvePath(Path.attributeListPath(path.split("/")));
			} 

            if (o == null) {
                return null;
            }

			if (o instanceof Attribute) {
				return ((Attribute) o).clone();
			} else if (o instanceof ArrayList) {
				return ((ArrayList<?>) o).clone();
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
            real = env.getObjectInstanceOperations().modifyObjectAttribute(real, Path.attributePath(list, a.getName()), a);
        } catch (Exception e) {
            throw new GSimException(e);
        }
        
        onChange();

    }

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
            real = env.getObjectInstanceOperations().modifyObjectAttribute(real, Path.attributePath(list, a.getName()), a);
            onChange();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

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
            real = env.getObjectInstanceOperations().modifyObjectAttribute(real, Path.attributePath(list, a.getName()), a);
            onChange();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

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
            real = env.getObjectInstanceOperations().modifyObjectAttribute(real, Path.attributePath(list,a.getName()), a);
            onChange();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

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
            real = env.getObjectInstanceOperations().modifyObjectAttribute(real, Path.attributePath(list, a.getName()), a);
            onChange();
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public Unit<?, ?> toUnit() {
        return real;
    }

	protected void onChange() {
		setChanged();
		notifyObservers();
	}

	protected void onDestroy() {
		setChanged();
		notifyObservers(false);
	}
}
