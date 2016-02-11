package gsim.objects.impl;

import java.util.ArrayList;

import de.s2.gsim.objects.AgentClassIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.ObjectClassIF;
import de.s2.gsim.objects.attribute.DomainAttribute;
import gsim.def.objects.Frame;
import gsim.def.objects.Unit;

public class ChildObjectClass implements ObjectClassIF, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean destroyed = false;

    private AgentClassIF env;

    private String list = "";

    private Frame real;

    public ChildObjectClass(AgentClassIF env, String list, Frame real) {
        this.env = env;
        this.real = real;
        this.list = list;
    }

    @Override
    public void addAttribute(String list, DomainAttribute a) throws GSimObjectException {
        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
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

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public DomainAttribute getAttribute(String list, String attName) throws GSimObjectException {
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
     * @link gsim.objects.ObjectClassIF
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
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public DomainAttribute[] getAttributes(String list) throws GSimObjectException {

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
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public String getDefaultValue(String list, String attName) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            return a.getDefaultValue();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public String getName() throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            return real.getTypeName();
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    @Override
    public boolean isDeclaredAttribute(String list, String attName) throws GSimObjectException {
        return real.isDeclaredAttribute(list, attName);
    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
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

            if (o instanceof DomainAttribute || o instanceof ArrayList) {
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
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public void setAttribute(String list, DomainAttribute a) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            real.addOrSetAttribute(list, a);
            env.addOrSetObject(this.list, this);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    /**
     * @see
     * @link gsim.objects.ObjectClassIF
     */
    @Override
    public void setDefaultAttributeValue(String list, String attName, String value) throws GSimObjectException {

        if (destroyed) {
            throw new GSimObjectException("This object was removed from the runtime context.");
        }

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            a.setDefault(value);
            env.addOrSetObject(this.list, this);
        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
