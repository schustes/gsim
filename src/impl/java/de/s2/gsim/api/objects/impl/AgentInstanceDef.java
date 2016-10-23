package de.s2.gsim.api.objects.impl;

import static de.s2.gsim.api.objects.impl.ObserverUtils.*;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.behaviour.BehaviourInstance;
import de.s2.gsim.environment.BehaviourDef;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgent;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.Path;
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

/**
 * Definition time agent instance implementation. Notifies other agents referring to this agent of any changes.
 * It observes any changes in its parent agent class and reloads after possible changes in the defining frame occurred.
 */
public class AgentInstanceDef extends ObjectInstanceDef implements AgentInstance, UnitWrapper, Observer {

	private final static Logger logger = Logger.getLogger(AgentInstanceDef.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *  
	 * @param env the environment
	 * @param owner the actual agent
	 * @param observable the agent class this agent is created from
	 */
	public AgentInstanceDef(Environment env, GenericAgent owner, AgentClassDef observable) {
		super(env, owner);
		observable.addObserver(this);
	}

	@Override
	public void addOrSetObject(String list, ObjectInstance object) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			GenericAgent a = (GenericAgent) real;
			real = env.getAgentInstanceOperations().addChildInstance(a, Path.objectListPath(list), (Instance) object);

			observeDependentObjectInstance(object, this);

		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectInstance createObjectFromListType(String objectName, String listName) {
		Frame f = real.getDefinition().getListType(listName);
		Instance instance = Instance.instanciate(objectName, f);
		return new DependentObjectInstance(this, listName, instance);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AgentInstanceDef)) {
			return false;
		}

		try {
			return ((AgentInstance) o).getName().equals(getName());
		} catch (GSimException e) {
			logger.error("Error", e);
		}
		return false;
	}

	@Override
	public Behaviour getBehaviour() throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			GenericAgent a = (GenericAgent) real;
			return new BehaviourInstance(this, a.getBehaviour());
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectInstance getObject(String list, String objectName) throws GSimException {
		Instance in = real.getChildInstance(list, objectName);
		if (in != null) {
			return new DependentObjectInstance(this, list, in);
		} else {
			return null;
		}
	}

	@Override
	public String[] getObjectListNames() throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			GenericAgent a = (GenericAgent) real;
			return a.getChildInstanceListNames().toArray(new String[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public ObjectInstance[] getObjects(String list) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

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
	public int hashCode() {
		return getName().hashCode() + super.real.getDefinition().getName().hashCode();
	}

	@Override
	public void removeObject(String list, ObjectInstance object) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			GenericAgent a = (GenericAgent) real;
			real = env.getAgentInstanceOperations().removeChildObject(a,  Path.objectPath(list, object.getName()));
			stopObservingDependentObjectInstance(object, this);
		} catch (Exception e) {
			throw new GSimException(e);
		}

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

			if (o instanceof Attribute) {
				return ((Attribute) o).clone();
			} else if (o instanceof Instance) {
				return new ObjectInstanceDef(env, (Instance) o);
			} else if (o instanceof TypedList) {
				TypedList<?> list = (TypedList<?>) o;
				ArrayList<ObjectInstanceDef> ret = new ArrayList<ObjectInstanceDef>();
				Iterator<?> iter = list.iterator();
				while (iter.hasNext()) {
					Instance f = (Instance) iter.next();
					ObjectInstanceDef c = new ObjectInstanceDef(env, f);
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
	public void setAttribute(String list, Attribute a) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			real = env.getAgentInstanceOperations().modifyAgentAttribute((GenericAgent) real, Path.attributePath( list, a.getName()), a);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setBehaviour(Behaviour b) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {

			if (!(b instanceof BehaviourInstance)) {
				throw new GSimException("Passed Behaviour interface " + b + " is not a valid class for Frame-type objects!");
			}

			GenericAgent a = (GenericAgent) real;
			real = env.getAgentInstanceOperations().changeAgentBehaviour(a, (BehaviourDef) ((UnitWrapper) b).toUnit());

		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setIntervalAttributeValue(String list, String attName, double from, double to) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
			if (a == null) {
				a = new IntervalAttribute(attName, from, to);
			}
			a.setFrom(from);
			a.setTo(to);
			real = env.getAgentInstanceOperations().modifyAgentAttribute((GenericAgent) real, Path.attributePath( list, a.getName()), a);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setNumericalAttributeValue(String list, String attName, double value) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			NumericalAttribute a = (NumericalAttribute) real.getAttribute(list, attName);
			if (a == null) {
				a = new NumericalAttribute(attName, value);
			}
			a.setValue(value);
			real = env.getAgentInstanceOperations().modifyAgentAttribute((GenericAgent) real, Path.attributePath( list, a.getName()), a);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setSetAttributeValues(String list, String attName, String... values) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			SetAttribute a = (SetAttribute) real.getAttribute(list, attName);
			if (a == null) {
				Frame f = real.getDefinition();
				DomainAttribute def = f.getAttribute(list, attName);
				a = new SetAttribute(attName, def.getFillers());
			}
			a.removeAllEntries();
			for (String v : values) {
				a.addEntry(v);
			}
			real = env.getAgentInstanceOperations().modifyAgentAttribute((GenericAgent) real, Path.attributePath( list, a.getName()), a);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void setStringAttributeValue(String list, String attName, String value) throws GSimException {

		if (destroyed) {
			throw new GSimException("This object was removed from the runtime context.");
		}

		try {
			StringAttribute a = (StringAttribute) real.getAttribute(list, attName);
			if (a == null) {
				a = new StringAttribute(attName, value);
			}
			a.setValue(value);
			real = env.getAgentInstanceOperations().modifyAgentAttribute((GenericAgent) real, Path.attributePath( list, a.getName()), a);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	@Override
	public void update(Observable o, Object arg) {
		super.real = env.getAgentInstanceOperations().getAgent(real.getName());
	}

}
