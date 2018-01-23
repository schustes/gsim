package de.s2.gsim.api.objects.impl;

import static de.s2.gsim.api.objects.impl.Invariant.precondition;
import static de.s2.gsim.api.objects.impl.ObserverUtils.observeDependentObject;
import static de.s2.gsim.api.objects.impl.ObserverUtils.stopObservingDependent;
import static de.s2.gsim.api.objects.impl.ObserverUtils.stopObservingDependentObject;

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
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * Definition time agent class. Notifies related objects (agent classes, agent instance) of any changes.
 */
public class AgentClassDef extends ObjectClassDef implements AgentClass, UnitWrapper, Observer {

	public static final long serialVersionUID = 1L;

	public AgentClassDef(Environment env, GenericAgentClass real) {
		super(env, real);
	}

	protected Frame getReal() {
		Frame f = env.getAgentClassOperations().getAgentSubClass(super.getRealRef().getName());
        precondition(this, f);
		super.setReal(f);
		return super.getRealRef();
	}

	@Override
	public void addAttribute(String list, DomainAttribute a) throws GSimException {

		precondition(this, a);

		try {
			setReal(env.getAgentClassOperations().addAgentClassAttribute((GenericAgentClass) getReal(), Path.attributeListPath(list), a));
		} catch (Exception e) {
			throw new GSimException(e);
		}

		onChange();
	}

	@Override
	public void defineAttributeList(String list) throws GSimException {

		precondition(this, list);

		try {
			if (!getReal().getAttributeLists().containsKey(list)) {
				setReal(env.getAgentClassOperations().addAttributeList((GenericAgentClass) getReal(), list));
			}
		} catch (Exception e) {
			throw new GSimException(e);
		}

		onChange();
	}

	@Override
	public void defineObjectList(String list, ObjectClass objectClass) throws GSimException {

		precondition(this, list, objectClass);

		try {
			Frame object = (Frame) ((UnitWrapper) objectClass).toUnit();
			if (!getReal().getObjectLists().containsKey(list)) {
				setReal(env.getAgentClassOperations().addObjectList((GenericAgentClass) getReal(), list, object));
			}
		} catch (Exception e) {
			throw new GSimException(e);
		}

		observeDependentObject(objectClass, this);

		onChange();
	}

	@Override
	public void addOrSetObject(String list, ObjectClass objectClass) throws GSimException {

		precondition(this, list, objectClass);

		try {
			Frame object = (Frame) ((UnitWrapper) objectClass).toUnit();
			if (!getReal().getObjectLists().containsKey(list)) {
				env.getAgentClassOperations().addObjectList((GenericAgentClass) getReal(), list, object);
			}
			if (getReal().containsChildFrame(list, object)) {
				env.getAgentClassOperations().removeChildFrame((GenericAgentClass) getReal(),
						Path.objectPath(list, object.getName()));
			}
			setReal(env.getAgentClassOperations().addChildObject((GenericAgentClass)  getReal(), Path.objectListPath(list), object));
		} catch (Exception e) {
			throw new GSimException(e);
		}

		observeDependentObject(objectClass, this);

		onChange();
	}

	@Override
	public void destroy() throws GSimException {

		precondition(this);

		try {
			env.getAgentClassOperations().removeAgentClass((GenericAgentClass)  getReal());
		} catch (Exception e) {
			throw new GSimException(e);
		}

		destroyed = true;

		onDestroy();

	}

	@Override
	public Behaviour getBehaviour() throws GSimException {

		precondition(this);

		try {
			return new BehaviourClass(this, ((GenericAgentClass)  getReal()).getBehaviour());
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public String[] getObjectListNames() throws GSimException {

		precondition(this);

		try {
			return  getReal().getChildFrameListNames().toArray(new String[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectClass getObjectListType(String listName) throws GSimException {

		precondition(this, listName);

		try {
			ObjectClassDef obj = new ObjectClassDef(env, getReal().getListType(listName));
			obj.addObserver(this);
			return obj;
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectClass[] getObjects(String list) throws GSimException {

		precondition(this, list);

		try {
			List<Frame> f = getReal().getChildFrames(list);
			ObjectClass[] ret = new ObjectClass[f.size()];
			for (int i = 0; i < f.size(); i++) {
				DependentObjectClass obj = new DependentObjectClass(this, list, f.get(i).clone());
				ret[i] = obj;
			}
			return ret;
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public boolean isDeclaredAttribute(String list, String attName) throws GSimException {
		precondition(this, list, attName);
		try {
			return getReal().isDeclaredAttribute(list, attName);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public boolean isDeclaredObject(String list, String objectName) throws GSimException {
		precondition(this, list, objectName);
		try {
			return getReal().getDeclaredAttribute(list, objectName) == null;
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void removeObject(String list, ObjectClass object) throws GSimException {

		precondition(this, list, object);

		try {
			setReal(env.getAgentClassOperations().removeChildFrame((GenericAgentClass) getReal(), Path.objectPath(list, object.getName())));
		} catch (Exception e) {
			throw new GSimException(e);
		}

		stopObservingDependentObject(object, this);
		this.onChange();

	}

	@Override
	public Object resolveName(String path) throws GSimException {

		precondition(this, path);

		try {
			Object o = getReal().resolvePath(Path.attributePath(path.split("/")));
			if (o == null) {
				o = getReal().resolvePath(Path.attributeListPath(path.split("/")));
			} 
			if (o == null) {
				o = getReal().resolvePath(Path.objectPath(path.split("/")));
			}
			if (o == null) {
				o = getReal().resolvePath(Path.objectListPath(path.split("/")));
			}

			if (o == null) {
				return null;
			}

			if (o instanceof DomainAttribute) {
				return ((DomainAttribute) o).clone();
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
				return ((ArrayList<?>) o).clone();
			} else {
				throw new GSimException("Can't handle return value " + o);
			}
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	@Override
	public void setAttribute(String list, DomainAttribute a) throws GSimException {

		precondition(this, list, a);

		try {
			setReal(env.getAgentClassOperations().modifyAgentClassAttribute((GenericAgentClass) getReal(), Path.attributePath(list, a.getName()), a));
		} catch (Exception e) {
			throw new GSimException(e);
		}

		onChange();
	}

	@Override
	public void setBehaviour(Behaviour b) throws GSimException {

		precondition(this, b);

		try {
			if (!(b instanceof BehaviourClass)) {
				throw new GSimException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
			}
			setReal(env.getAgentClassOperations().changeAgentClassBehaviour((GenericAgentClass) getReal(), (BehaviourFrame) ((UnitWrapper) b).toUnit()));
		} catch (Exception e) {
			throw new GSimException(e);
		}

		onChange();
	}

	@Override
	public void setDefaultAttributeValue(String list, String attName, String value) throws GSimException {

		precondition(this, list, attName, value);

		try {
			DomainAttribute a = getReal().getAttribute(list, attName);
			a.setDefault(value);
			setReal(env.getAgentClassOperations().modifyAgentClassAttribute((GenericAgentClass) getReal(), Path.attributePath(list, a.getName()), a));
		} catch (Exception e) {
			throw new GSimException(e);
		}

		onChange();

	}

	@Override
	public void update(Observable o, Object arg) {
		if (destroyed) {
			return;
		}

		if (arg instanceof Boolean && ((Boolean)arg)==Boolean.FALSE) {
			stopObservingDependent(o, this);
		}

		setReal( env.getAgentClassOperations().getAgentSubClass(getReal().getName()));
		super.setChanged();
		super.notifyObservers();

	}

    public String toString() {
	    return "Wrapper type=" + getClass().getSimpleName() +", wrapped class=" + real.getClass().getSimpleName() +", object name=" + real.getName();
    }


}
