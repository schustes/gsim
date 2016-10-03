package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;
import java.util.Observable;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Path;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * Implementation of ObjectClass.
 * 
 * An ObjectClass may be observed by an AgentClass.
 * 
 * @author sschuster
 *
 */
public class ObjectClassDef extends Observable implements ObjectClass, UnitWrapper {

    private static final long serialVersionUID = 1L;

    protected boolean destroyed = false;

    protected Environment env;

    protected Frame real;
    
    /**
     * Constructor.
     * 
     * @param env environment instance
     * @param real the reference to the actual frame
     */
    public ObjectClassDef(Environment env, Frame real) {
        this.env = env;
        this.real = real;
    }
	
    @Override
    public void addAttribute(String list, DomainAttribute a) throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
        	if (!real.getAttributeLists().containsKey(list)) {
				env.getObjectClassOperations().addAttributeList(real, list);
        	}
			real = env.getObjectClassOperations().addAttribute(real, Path.attributeListPath(list), a);
        } catch (Exception e) {
            throw new GSimException(e);
        }

        onChange();
        
    }

    @Override
    public void destroy() throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            env.getObjectClassOperations().removeObjectClass(real);
            real = null;
        } catch (Exception e) {
            throw new GSimException(e);
        }

        destroyed = true;
        
        onDestroy();
    }

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

            if (o instanceof Attribute || o instanceof ArrayList) {
                return o;
            } else {
                throw new GSimException("Can't handle return value " + o);
            }

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public void setAttribute(String list, DomainAttribute a) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            real = env.getObjectClassOperations().modifyObjectClassAttribute(real, Path.attributePath(list, a.getName()), a);
        } catch (Exception e) {
            throw new GSimException(e);
        }
        
        onChange();
    }

    @Override
    public void setDefaultAttributeValue(String list, String attName, String value) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
            DomainAttribute a = real.getAttribute(list, attName);
            a.setDefault(value);
            real = env.getObjectClassOperations().modifyObjectClassAttribute(real, Path.attributePath(list, a.getName()), a);
        } catch (Exception e) {
            throw new GSimException(e);
        }
        
        onChange();

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
