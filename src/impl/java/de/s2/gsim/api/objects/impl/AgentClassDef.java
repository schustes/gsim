package de.s2.gsim.api.objects.impl;


import static de.s2.gsim.api.objects.impl.ObserverUtils.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.Path;
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class AgentClassDef extends ObjectClassDef implements AgentClass, UnitWrapper, Observer {

	public static final long serialVersionUID = 1L;

	public AgentClassDef(Environment env, GenericAgentClass real) {
		super(env, real);
	}

	@Override
	public void addAttribute(String list, DomainAttribute a) throws GSimException {
		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			real = env.getAgentClassOperations().addAgentClassAttribute((GenericAgentClass) real, Path.attributeListPath(list), a);
		} catch (Exception e) {
			throw new GSimException(e);
		}

		onChange();
		//observe also attributes? Maybe later...

	}

	@Override
	public void addOrSetObject(String list, ObjectClass objectClass) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			Frame object = (Frame) ((UnitWrapper) objectClass).toUnit();
			if (!real.getObjectLists().containsKey(list)) {
				env.getAgentClassOperations().addObjectList((GenericAgentClass)real, list, object);
			}
			real = env.getAgentClassOperations().addChildObject((GenericAgentClass) real, Path.objectListPath(list), object);
		} catch (Exception e) {
			throw new GSimException(e);
		}

		observeDependentObject(objectClass, this);

		onChange();
	}

	@Override
	public void destroy() throws GSimException {
		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			env.getAgentClassOperations().removeAgentClass((GenericAgentClass) real);
		} catch (Exception e) {
			throw new GSimException(e);
		}

		destroyed = true;

		onDestroy();

	}

	@Override
	public Behaviour getBehaviour() throws GSimException {
		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			return new BehaviourClass(this, ((GenericAgentClass) real).getBehaviour());
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public String[] getObjectListNames() throws GSimException {
		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			return real.getChildFrameListNames().toArray(new String[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectClass getObjectListType(String listName) throws GSimException {
		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			ObjectClassDef obj = new ObjectClassDef(env, real.getListType(listName));
			obj.addObserver(this);
			return obj;
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectClass[] getObjects(String list) throws GSimException {
		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			List<Frame> f = real.getChildFrames(list);
			ObjectClass[] ret = new ObjectClass[f.size()];
			for (int i = 0; i < f.size(); i++) {
				ObjectClassDef obj = new ObjectClassDef(env, f.get(i));
				obj.addObserver(this);
				ret[i] = obj;
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

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			real = env.getAgentClassOperations().removeChildFrame((GenericAgentClass) real, Path.objectPath(list, object.getName()));
		} catch (Exception e) {
			throw new GSimException(e);
		}

		stopObservingDependentObject(object, this);
		this.onChange();

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
				o = real.resolvePath(Path.objectPath(path.split("/")));
			}
			if (o == null) {
				o = real.resolvePath(Path.objectListPath(path.split("/")));
			}

			if (o == null) {
				return null;
			}

			if (o instanceof DomainAttribute) {
				return o;
			} else if (o instanceof Frame) {
				return new ObjectClassDef(env, (Frame) o);
			} else if (o instanceof TypedList) {
				TypedList<?> list = (TypedList<?>) o;
				ArrayList<ObjectClassDef> ret = new ArrayList<ObjectClassDef>();
				Iterator<?> iter = list.iterator();
				while (iter.hasNext()) {
					Frame f = (Frame) iter.next();
					ObjectClassDef c = new ObjectClassDef(env, f);
					ret.add(c);
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

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			real = env.getAgentClassOperations().modifyAgentClassAttribute((GenericAgentClass) real, Path.attributePath(list, a.getName()), a);
		} catch (Exception e) {
			throw new GSimException(e);
		}

		onChange();
	}

	@Override
	public void setBehaviour(Behaviour b) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context..");
		}

		try {
			if (!(b instanceof BehaviourClass)) {
				throw new GSimException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
			}
			real = env.getAgentClassOperations().changeAgentClassBehaviour((GenericAgentClass) real, (BehaviourFrame) ((UnitWrapper) b).toUnit());
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
			real = env.getAgentClassOperations().modifyAgentClassAttribute((GenericAgentClass) real, Path.attributePath(list, a.getName()), a);
		} catch (Exception e) {
			throw new GSimException(e);
		}

		onChange();

	}

	@Override
	public Unit<Frame, DomainAttribute> toUnit() {
		return real;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (destroyed) {
			return;
		}

		if (arg instanceof Boolean && ((Boolean)arg)==Boolean.FALSE) {
			stopObservingDependent(o, this);
		}

		super.real = env.getAgentClassOperations().getAgentSubClass(real.getName());
		super.setChanged();
		super.notifyObservers();

	}


}
