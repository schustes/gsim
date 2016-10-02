package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.behaviour.BehaviourClass;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.Path;
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * Implementation of AgentClass interface used during runtime of the simulation. Changes on dependent objects are observed and changes notified,
 * but no environment is called, i.e. no hierarchical changes of the definition environment can occur. Changes remain local to the runtime.
 */
public class AgentClassSim extends Observable implements AgentClass, ObjectClass, Observer {

	public static final long serialVersionUID = 1L;

	private GenericAgentClass real;

	private static Environment dummyEnv = new Environment("");

	public AgentClassSim(GenericAgentClass real) {
		this.real = real;
	}

	@Override
	public void addAttribute(String list, DomainAttribute a) throws GSimException {
		try {
			real.addOrSetAttribute(list, a);
			onChange();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
	public void addOrSetObject(String list, ObjectClass object) throws GSimException {
		try {
			real.addOrSetChildFrame(list, (Frame) ((UnitWrapper) object).toUnit());
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
	public void destroy() throws GSimException {
		throw new GSimException("You can't delete this frame here! It belongs to an agent active in a simulation...");
	}

	@Override
	public DomainAttribute getAttribute(String list, String attName) throws GSimException {
		try {
			return real.getAttribute(list, attName);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public String[] getAttributeListNames() throws GSimException {
		try {
			return real.getAttributesListNames().toArray(new String[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
	public DomainAttribute[] getAttributes(String list) throws GSimException {
		try {
			return real.getAttributes(list).toArray(new DomainAttribute[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
	public Behaviour getBehaviour() throws GSimException {
		try {
			return new BehaviourClass(this, real.getBehaviour());
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
	public String getDefaultValue(String list, String attName) throws GSimException {

		try {
			DomainAttribute a = real.getAttribute(list, attName);
			return a.getDefaultValue();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
	public String getName() throws GSimException {

		try {
			return real.getName();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
	public String[] getObjectListNames() throws GSimException {

		try {
			return real.getChildFrameListNames().toArray(new String[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectClass getObjectListType(String listName) throws GSimException {
		try {
			ObjectClass c = new DependentObjectClass(this, listName, real.getListType(listName));
			return c;
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}


	@Override
	public ObjectClass[] getObjects(String list) throws GSimException {

		try {
			List<Frame> f = real.getChildFrames(list);
			ObjectClass[] ret = new ObjectClass[f.size()];
			for (int i = 0; i < f.size(); i++) {
				ObjectClass c = new DependentObjectClass(this, list, f.get(i));
				ret[i] = c;
			}
			return ret;
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public boolean isDeclaredAttribute(String list, String attName) throws GSimException {
		return real.isDeclaredAttribute(list, attName);
	}

	@Override
	public boolean isDeclaredObject(String list, String objectName) throws GSimException {
		return real.getDeclaredAttribute(list, objectName) == null;
	}

	@Override
	public void removeObject(String list, ObjectClass object) throws GSimException {

		try {
			real.removeChildFrame(list, object.getName());
			onChange();
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public Object resolveName(String path) throws GSimException {

		try {

			Object o = real.resolvePath(Path.attributePath(path.split("/")));
			if (o == null) {
				o = real.resolvePath(Path.attributeListPath(path.split("/")));
			} 
			if (o == null) {
				o = real.resolvePath(Path.objectPath(path.split("/")));
			}
			if (o == null) {
				o = real.resolvePath(Path.objectListPath(path.split("/")));
			}

			if (o == null) {
				return null;
			}

			if (o instanceof Attribute) {
				return o;
			} else if (o instanceof Frame) {
				ObjectClass def = new DependentObjectClass(this,path.split("/")[0], (Frame) o);
				return def;
			} else if (o instanceof TypedList) {
				TypedList<?> list = (TypedList<?>) o;
				List<ObjectClass> ret = new ArrayList<ObjectClass>();
				Iterator<?> iter = list.iterator();
				while (iter.hasNext()) {
					Frame f = (Frame) iter.next();
					ObjectClassDef def = new ObjectClassDef(dummyEnv, f);
					def.addObserver(this);
					ret.add(def);
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

	@Override
	public void setAttribute(String list, DomainAttribute a) throws GSimException {

		try {
			real.addOrSetAttribute(list, a);
			onChange();
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setBehaviour(Behaviour b) throws GSimException {

		try {
			if (!(b instanceof BehaviourClass)) {
				throw new GSimException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
			}
			real.setBehaviour((BehaviourFrame) ((UnitWrapper) b).toUnit());
			onChange();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
	public void setDefaultAttributeValue(String list, String attName, String value) throws GSimException {
		try {
			DomainAttribute a = real.getAttribute(list, attName);
			a.setDefault(value);
			real.addOrSetAttribute(list, a);
			onChange();
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void update(Observable o, Object arg) {
		//TODO is this enough? In the sim instance, dependent objects should be references
		this.setChanged();
		this.notifyObservers();
	}

	private void onChange() {
		setChanged();
		notifyObservers();
	}

}
