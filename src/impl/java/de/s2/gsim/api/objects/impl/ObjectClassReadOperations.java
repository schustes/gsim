package de.s2.gsim.api.objects.impl;

import static de.s2.gsim.api.objects.impl.Invariant.precondition;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class ObjectClassReadOperations {

	private ManagedObject obj;

	public ObjectClassReadOperations(ManagedObject obj) {
		this.obj = obj;
	}

	public DomainAttribute getAttribute(Frame frame, String list, String attName) throws GSimException {

		precondition(obj, list, attName);

		try {
			return frame.getAttribute(list, attName);
		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

	public String[] getAttributeListNames(Frame frame) throws GSimException {

		precondition(obj);

		try {
			return frame.getAttributesListNames().toArray(new String[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	public DomainAttribute[] getAttributes(Frame frame, String list) throws GSimException {

		precondition(obj, list);

		try {
			return frame.getAttributes(list).toArray(new DomainAttribute[0]);
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	public String getDefaultValue(Frame frame, String list, String attName) throws GSimException {

		precondition(obj, list, attName);

		try {
			DomainAttribute a = frame.getAttribute(list, attName);
			return a.getDefaultValue();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	public String getName(Frame frame) throws GSimException {

		precondition(obj);

		try {
			return frame.getName();
		} catch (Exception e) {
			throw new GSimException(e);
		}
	}

	public boolean isDeclaredAttribute(Frame frame, String list, String attName) throws GSimException {
		precondition(obj, list, attName);

		return frame.isDeclaredAttribute(list, attName);
	}

	public Object resolveName(Frame frame, String path) throws GSimException {

		precondition(obj, path);

		try {
			return frame.resolvePath(Path.of(path.split("/")));

		} catch (Exception e) {
			throw new GSimException(e);
		}

	}

}
