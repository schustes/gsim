package de.s2.gsim.api.objects.impl;

import static de.s2.gsim.api.objects.impl.Invariant.precondition;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * A DependentObjectClass wraps any object that exits only in relationship to an owning agent. On changes the owner can directly react/notify other objects.
 */
public class DependentObjectClass implements ObjectClass, UnitWrapper, ManagedObject {

    private static final long serialVersionUID = 1L;

    private boolean destroyed = false;

    private AgentClass env;

    private String list = "";

    private Frame real;

    public DependentObjectClass(AgentClass env, String list, Frame real) {
        this.env = env;
        this.real = real;
        this.list = list;
    }

    @Override
	public void defineAttributeList(String list) throws GSimException {

		precondition(this, list);

		try {
			real.defineAttributeList(list);
			env.addOrSetObject(this.list, this);
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
    public void addAttribute(String list, DomainAttribute a) throws GSimException {

		precondition(this, list, a);

		try {
            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);
        } catch (Exception e) {
            throw new GSimException(e);
        }
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
    public DomainAttribute getAttribute(String list, String attName) throws GSimException {

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
    public DomainAttribute[] getAttributes(String list) throws GSimException {

		precondition(this, list);

        try {
            return real.getAttributes(list).toArray(new DomainAttribute[0]);
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public String getDefaultValue(String list, String attName) throws GSimException {

		precondition(this, list, attName);

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            return a.getDefaultValue();
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
    public boolean isDeclaredAttribute(String list, String attName) throws GSimException {

		precondition(this, list, attName);

		return real.isDeclaredAttribute(list, attName);
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
    public void setAttribute(String list, DomainAttribute a) throws GSimException {

		precondition(this, list, a);

        try {
            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }
    @Override
    public void setDefaultAttributeValue(String list, String attName, String value) throws GSimException {

		precondition(this, attName, value);

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            a.setDefault(value);
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
