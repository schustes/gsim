package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;
import java.util.Observable;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.Path;
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

    private Frame real;
    
    /**
     * Constructor.
     * 
     * @param env environment instance
     * @param getReal() the reference to the actual frame
     */
    public ObjectClassDef(Environment env, Frame real) {
        this.env = env;
        this.real = real;
    }
    
    protected Frame getRealRef() {
    	return this.real;
    }
    
    protected Frame getReal() {
    	this.real = env.getObjectClassOperations().getObjectSubClass(real.getName());
    	return this.real;
    }
    
    protected void setReal(Frame real) {
    	this.real = real;
    }

	@Override
	public void defineAttributeList(String list) throws GSimException {
		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			if (!getReal().getAttributeLists().containsKey(list)) {
				env.getObjectClassOperations().addAttributeList(getReal(), list);
			}
		} catch (Exception e) {
			throw new GSimException(e);
		}

		onChange();

	}

    @Override
    public void addAttribute(String list, DomainAttribute a) throws GSimException {
        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
        	if (!getReal().getAttributeLists().containsKey(list)) {
				env.getObjectClassOperations().addAttributeList(getReal(), list);
        	}
        	this.real = env.getObjectClassOperations().addAttribute(getReal(), Path.attributeListPath(list), a);
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
            env.getObjectClassOperations().removeObjectClass(getReal());
            this.real = null;
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
            return getReal().getAttribute(list, attName);
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
            return getReal().getAttributesListNames().toArray(new String[0]);
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
            return getReal().getAttributes(list).toArray(new DomainAttribute[0]);
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
            DomainAttribute a = getReal().getAttribute(list, attName);
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
            return getReal().getName();
        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public boolean isDeclaredAttribute(String list, String attName) throws GSimException {
        return getReal().isDeclaredAttribute(list, attName);
    }

    @Override
    public Object resolveName(String path) throws GSimException {

        if (destroyed) {
            throw new GSimException("This object was removed from the runtime context.");
        }

        try {
        	Object o = getReal().resolvePath(Path.attributePath(path.split("/")));

			if (o == null) {
				o = getReal().resolvePath(Path.attributeListPath(path.split("/")));
			} 

			if (o == null) {
                return null;
            }

            if (o instanceof Attribute || o instanceof ArrayList) {
				return ((Attribute) o).clone();
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
        	real = env.getObjectClassOperations().modifyObjectClassAttribute(getReal(), Path.attributePath(list, a.getName()), a);
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
            DomainAttribute a = getReal().getAttribute(list, attName);
            a.setDefault(value);
           real = env.getObjectClassOperations().modifyObjectClassAttribute(getReal(), Path.attributePath(list, a.getName()), a);
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
