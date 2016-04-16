package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Path;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class ChildObjectClass implements ObjectClass, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean destroyed = false;

    private AgentClass env;

    private String list = "";

    private Frame real;

    public ChildObjectClass(AgentClass env, String list, Frame real) {
        this.env = env;
        this.real = real;
        this.list = list;
    }

    @Override
    public void addAttribute(String list, DomainAttribute a) throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);
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
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            env.removeObject(list, this);
            real = null;
        } catch (Exception e) {
            throw new GSimException(e);
        }

        destroyed = true;
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public DomainAttribute getAttribute(String list, String attName) throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

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
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

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

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            return real.getAttributes(list).toArray(new DomainAttribute[0]);
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

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

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
    public boolean isDeclaredAttribute(String list, String attName) throws GSimException {
        return real.isDeclaredAttribute(list, attName);
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
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

            if (o instanceof DomainAttribute || o instanceof ArrayList) {
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

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);
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

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            a.setDefault(value);
            env.addOrSetObject(this.list, this);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
