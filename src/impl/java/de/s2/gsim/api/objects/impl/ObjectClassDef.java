package de.s2.gsim.api.objects.impl;

import static de.s2.gsim.api.objects.impl.Invariant.precondition;

import java.util.Observable;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * Implementation of ObjectClass.
 * 
 * An ObjectClass may be observed by an AgentClass.
 * 
 * @author sschuster
 *
 */
public class ObjectClassDef extends Observable implements ObjectClass, UnitWrapper, ManagedObject {

	private static final long serialVersionUID = 1L;

	protected boolean destroyed = false;

	protected Environment env;

	private Frame real;

	private ObjectClassReadOperations readOperations;

	/**
	 * Constructor.
	 * 
	 * @param env environment instance
	 * @param getReal() the reference to the actual frame
	 */
	public ObjectClassDef(Environment env, Frame real) {
		this.env = env;
		this.real = real;
		readOperations = new ObjectClassReadOperations(this);
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

		precondition(this, list);

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

		precondition(this, list, a);

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

		precondition(this);

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
		return readOperations.getAttribute(getReal(), list, attName);

	}

	@Override
	public String[] getAttributeListNames() throws GSimException {
		return readOperations.getAttributeListNames(getReal());
	}

	@Override
	public DomainAttribute[] getAttributes(String list) throws GSimException {
		return readOperations.getAttributes(getReal(), list);
	}

	@Override
	public String getDefaultValue(String list, String attName) throws GSimException {
		return readOperations.getDefaultValue(getReal(), list, attName);
	}

	@Override
	public String getName() throws GSimException {
		return readOperations.getName(getReal());
	}

	@Override
	public boolean isDeclaredAttribute(String list, String attName) throws GSimException {
		return readOperations.isDeclaredAttribute(getReal(), list, attName);
	}

	@Override
	public Object resolveName(String path) throws GSimException {
		return readOperations.resolveName(getReal(), path);
	}

	@Override
	public void setAttribute(String list, DomainAttribute a) throws GSimException {

		precondition(this, list, a);

		try {
			real = env.getObjectClassOperations().modifyObjectClassAttribute(getReal(), Path.attributePath(list, a.getName()), a);
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

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

}
