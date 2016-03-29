package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;

import de.s2.gsim.GSimException;
import de.s2.gsim.def.Environment;
import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.UnitOLD;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class ObjectClassDef implements ObjectClass, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected boolean destroyed = false;

    protected Environment env;

    protected FrameOLD real;

    public ObjectClassDef(Environment env, FrameOLD real) {
        this.env = env;
        this.real = real;
    }

    @Override
    public void addAttribute(String list, DomainAttribute a) throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real = env.addObjectClassAttribute(real, new String[] { list }, a);
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
            env.removeObjectClass(real);
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
            return real.getAttributesListNames();
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
            return real.getAttributes(list);
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
            return real.getTypeName();
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
            Object o = real.resolveName(path.split("/"));

            if (o == null) {
                return null;
            }

            if (o instanceof Attribute || o instanceof ArrayList) {
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
            real = env.modifyObjectClassAttribute(real, new String[] { list }, a);
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
            real = env.modifyObjectClassAttribute(real, new String[] { list }, a);
        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public UnitOLD toUnit() {
        return real;
    }

}
