package de.s2.gsim.api.objects.impl;

import static de.s2.gsim.api.objects.impl.Invariant.intervalIntegrity;
import static de.s2.gsim.api.objects.impl.Invariant.precondition;
import static de.s2.gsim.api.objects.impl.Invariant.setIntegrity;
import static de.s2.gsim.api.objects.impl.ObserverUtils.observeDependentObjectInstance;
import static de.s2.gsim.api.objects.impl.ObserverUtils.stopObservingDependentObjectInstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.behaviour.BehaviourClass;
import de.s2.gsim.api.objects.impl.behaviour.BehaviourInstance;
import de.s2.gsim.environment.BehaviourDef;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgent;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

/**
 * AgentInstance implementation used to wrap agents during simulation time.
 */
public class AgentInstanceSim extends Observable
        implements AgentInstance, ObjectInstance, UnitWrapper, Serializable, Observer, ManagedObject {

	private static final long serialVersionUID = 1L;

	private boolean destroyed = false;

	private GenericAgent real;

	private ObjectInstanceReadOperations readOperations;

	public AgentInstanceSim() {
		this.readOperations = new ObjectInstanceReadOperations(this, real);
	}

	public AgentInstanceSim(GenericAgent real) {
		this.real = real;
		this.readOperations = new ObjectInstanceReadOperations(this, real);
	}

	@Override
	public void addOrSetObject(String list, ObjectInstance object) throws GSimException {

		precondition(this, object);

		try {
			GenericAgent a = real;
			a.addChildInstance(list, (Instance) ((UnitWrapper) object).toUnit());
			observeDependentObjectInstance(object, this);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectInstance copy() {
		GenericAgent copyAgent = GenericAgent.from(real);
		return new AgentInstanceSim(copyAgent);
	}

	@Override
	public ObjectInstance createObjectFromListType(String objectName, String listName) {

		precondition(this, objectName, listName);

		Frame f = real.getDefinition().getListType(listName);
		Instance instance = Instance.instanciate(objectName, f);
		return new DependentObjectInstance(this, listName, instance);
	}

	@Override
	public void destroy() throws GSimException {
		throw new GSimException("You can't delete an agent from a running simulation with this mechanism");
	}

	@Override
	public Attribute getAttribute(String attName) throws GSimException {
		return readOperations.getAttribute(attName);
	}

	@Override
	public Attribute getAttribute(String list, String attName) throws GSimException {
		return readOperations.getAttribute(list, attName);
	}

	@Override
	public String[] getAttributeListNames() throws GSimException {
		return readOperations.getAttributeListNames();
	}

	@Override
	public Attribute[] getAttributes(String list) throws GSimException {
		return readOperations.getAttributes(list);
	}

	@Override
	public Behaviour getBehaviour() throws GSimException {

		precondition(this);

		try {
			GenericAgent a = real;
			return new BehaviourInstance(this, a.getBehaviour());
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public double getIntervalAttributeFrom(String list, String attName) throws GSimException {
		return readOperations.getIntervalAttributeFrom(list, attName);
	}

	@Override
	public double getIntervalAttributeTo(String list, String attName) throws GSimException {
		return readOperations.getIntervalAttributeTo(list, attName);
	}

	@Override
	public String getName() throws GSimException {
		return readOperations.getName();
	}

	@Override
	public double getNumericalAttribute(String list, String attName) throws GSimException {
		return readOperations.getNumericalAttribute(list, attName);
	}

	@Override
	public ObjectInstance getObject(String list, String objectName) throws GSimException {

		precondition(this, list, objectName);

		Instance in = real.getChildInstance(list, objectName);
		if (in != null) {
			return new DependentObjectInstance(this, list, in);
		} else {
			return null;
		}
	}

	@Override
	public String[] getObjectListNames() throws GSimException {

		precondition(this);

		try {
			GenericAgent a = real;
			return a.getChildInstanceListNames().toArray(new String[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectInstance[] getObjects(String list) throws GSimException {

		precondition(this, list);

		try {

			List<Instance> f = real.getChildInstances(list);
			ObjectInstance[] ret = new ObjectInstance[f.size()];
			for (int i = 0; i < f.size(); i++) {
				ret[i] = new DependentObjectInstance(this, list, f.get(i));
			}
			return ret;

		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public String[] getSetAttributeValues(String list, String attName) throws GSimException {
		return readOperations.getSetAttributeValues(list, attName);
	}

	@Override
	public String getStringAttribute(String list, String attName) throws GSimException {
		return readOperations.getStringAttribute(list, attName);
	}

	@Override
	public boolean inheritsFrom(String agentclassName) {
		return readOperations.inheritsFrom(agentclassName);
	}

	@Override
	public void removeObject(String list, ObjectInstance object) throws GSimException {

		precondition(this, list, object);

		try {
			real.removeChildInstance(list, object.getName());
			stopObservingDependentObjectInstance(object, this);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Object resolveName(String path) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {

			String[] p = path.split("/");

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
				return ((Attribute) o).clone();
			} else if (o instanceof Instance) {
				return new DependentObjectInstance(this, p[0], (Instance) o);
			} else if (o instanceof TypedList) {
				TypedList<Instance> list = (TypedList<Instance>) o;
				List<DependentObjectInstance> ret = new ArrayList<DependentObjectInstance>();
				Iterator<Instance> iter = list.iterator();
				while (iter.hasNext()) {
					Instance f = iter.next();
					DependentObjectInstance c = new DependentObjectInstance(this, p[0], f);
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

	public void setAgentInstance(GenericAgent real) {
		this.real = real;
	}

	@Override
	public void setAttribute(String list, Attribute a) throws GSimException {

		precondition(this, list, a);

		try {
			real.addOrSetAttribute(list, a);
			onChange();
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setBehaviour(Behaviour b) throws GSimException {

		precondition(this, b);

		try {

			if (!(b instanceof BehaviourInstance && b instanceof BehaviourClass)) {
				throw new GSimException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
			}

			GenericAgent a = real;
			a.setBehaviour((BehaviourDef) ((UnitWrapper) b).toUnit());

			onChange();

		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setIntervalAttributeValue(String list, String attName, double from, double to) throws GSimException {

		precondition(this, list, attName);
		intervalIntegrity(from, to);

		try {
			IntervalAttribute a;
			if (!real.containsAttribute(list, attName)) {
				a = new IntervalAttribute(attName, from, to);
			} else {
				a = (IntervalAttribute) real.getAttribute(list, attName);
			}
			a.setFrom(from);
			a.setTo(to);
			real.addOrSetAttribute(list, a);
			onChange();
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setNumericalAttributeValue(String list, String attName, double value) throws GSimException {

		precondition(this, list, attName);

		try {
			NumericalAttribute a;
			if (!real.containsAttribute(list, attName)) {
				a = new NumericalAttribute(attName, value);
			} else {
				a = (NumericalAttribute) real.getAttribute(list, attName);
			}
			a.setValue(value);
			real.addOrSetAttribute(list, a);
			onChange();
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setSetAttributeValues(String list, String attName, String... values) throws GSimException {

		precondition(this, list, attName);
		setIntegrity(values);

		try {
			SetAttribute a;
			if (!real.containsAttribute(list, attName)) {
				Frame f = real.getDefinition();
				DomainAttribute def = f.getAttribute(list, attName);
				a = new SetAttribute(attName, def.getFillers());
			} else {
				a = (SetAttribute) real.getAttribute(list, attName);
			}
			a.removeAllEntries();
			for (String v : values) {
				a.addEntry(v);
			}
			real.addOrSetAttribute(list, a);
			onChange();
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setStringAttributeValue(String list, String attName, String value) throws GSimException {

		precondition(this, list, attName, value);

		try {
			StringAttribute a;
			if (!real.containsAttribute(list, attName)) {
				a = new StringAttribute(attName, value);
			} else {
				a = (StringAttribute) real.getAttribute(list, attName);
			}
			a.setValue(value);
			real.addOrSetAttribute(list, a);
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

	@Override
	public void update(Observable o, Object arg) {
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

}