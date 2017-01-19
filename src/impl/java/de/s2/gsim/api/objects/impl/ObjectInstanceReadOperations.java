package de.s2.gsim.api.objects.impl;

import static de.s2.gsim.api.objects.impl.Invariant.precondition;

import java.util.List;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

public class ObjectInstanceReadOperations {

	private ManagedObject obj;

	private Instance real;

	public ObjectInstanceReadOperations(ManagedObject obj, Instance inst) {
		this.obj = obj;
		this.real = inst;
	}

	public Attribute getAttribute(String attName) throws GSimException {

		precondition(obj, attName);

		try {
			return real.getAttribute(attName);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	public Attribute getAttribute(String list, String attName) throws GSimException {

		precondition(obj, list, attName);

		try {
			return real.getAttribute(list, attName);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	public String[] getAttributeListNames() throws GSimException {

		precondition(obj);

		try {
			return real.getAttributesListNames().toArray(new String[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	public Attribute[] getAttributes(String list) throws GSimException {

		precondition(obj, list);

		try {
			return real.getAttributes(list).toArray(new Attribute[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	public double getIntervalAttributeFrom(String list, String attName) throws GSimException {

		precondition(obj, list, attName);

		try {
			IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
			return a.getFrom();
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	public double getIntervalAttributeTo(String list, String attName) throws GSimException {

		precondition(obj, list, attName);

		try {
			IntervalAttribute a = (IntervalAttribute) real.getAttribute(list, attName);
			return a.getTo();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	public String getName() throws GSimException {

		precondition(obj);

		try {
			return real.getName();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	public double getNumericalAttribute(String list, String attName) throws GSimException {

		precondition(obj, list, attName);

		try {
			NumericalAttribute a = (NumericalAttribute) real.getAttribute(list, attName);
			return a.getValue();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	public String[] getSetAttributeValues(String list, String attName) throws GSimException {

		precondition(obj, list, attName);

		try {
			SetAttribute set = (SetAttribute) real.getAttribute(list, attName);
			List<String> l = set.getEntries();
			String[] ret = new String[l.size()];
			l.toArray(ret);
			return ret;
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	public String getStringAttribute(String list, String attName) throws GSimException {

		precondition(obj, list, attName);

		try {
			StringAttribute a = (StringAttribute) real.getAttribute(list, attName);
			return a.getValue();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	public boolean inheritsFrom(String agentclassName) {

		precondition(obj, agentclassName);

		return real.inheritsFromOrIsOfType(agentclassName);
	}

	public Object resolveName(String path) throws GSimException {

		precondition(obj, path);

		try {
			return real.resolvePath(Path.of(path.split("/")));
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

}
