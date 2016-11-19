package de.s2.gsim.environment;

import java.util.List;

import javax.management.ObjectInstance;

import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.OrderedSetAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;

/**
 * TODO use list / stream operations.
 * 
 * @author stephan
 *
 */
public abstract class Generator {

	public enum Method {
		Normal, Uniform
	}

	public Generator() {
		super();
	}

	/**
	 * Randomises attribute values of agent itself and all contained objects, for attributes that are declared as 'mutable'.
	 * 
	 * @param a GenericAgent
	 * @param svar double
	 * @return GenericAgent
	 */
	public static GenericAgent randomiseNormalDistributedAttributeValues(GenericAgent a, double svar) {
		GenericAgent agent = null;
		agent = (GenericAgent) randomiseNormallyDistributedAttributeValues(a, svar);
		List<String> lists = a.getChildInstanceListNames();
		for (String listname : lists) {
			List<Instance> containedObjects = agent.getChildInstances(listname);
			for (Instance in : containedObjects) {
				randomiseNormallyDistributedAttributeValues(in, svar);
				agent.setChildInstance(listname, in);
			}
		}
		return agent;
	}

	public static GenericAgent randomiseUniformAttributeValues(GenericAgent a) {
		GenericAgent agent = null;
		agent = (GenericAgent) randomiseUniformDistributedAttributeValues(a);
		List<String> lists = a.getChildInstanceListNames();
		for (String listname : lists) {
			List<Instance> containedObjects = agent.getChildInstances(listname);
			for (Instance in : containedObjects) {
				randomiseUniformDistributedAttributeValues(in);
				agent.setChildInstance(listname, in);
			}
		}
		return agent;
	}

	public static ObjectInstance randomiseNormalAttributeValues(ObjectInstance a, double svar) {
		Instance b = (Instance) ((UnitWrapper) a).toUnit();
		b = randomiseNormallyDistributedAttributeValues(b, svar);
		return a;
	}

	public static ObjectInstance randomiseUniformAttributeValues(ObjectInstance a) {
		Instance b = (Instance) ((UnitWrapper) a).toUnit();
		b = randomiseUniformDistributedAttributeValues(b);
		return a;
	}

	private static Instance randomiseUniformDistributedAttributeValues(Instance obj) {

		List<String> lists = obj.getAttributesListNames();

		for (int j = 0; j < lists.size(); j++) {
			List<Attribute> atts = obj.getAttributes(lists.get(j));
			for (int k = 0; k < atts.size(); k++) {
				DomainAttribute da = obj.getDefinition().getAttribute(lists.get(j), atts.get(k).getName());
				if (da.isMutable()) {
					if (atts.get(k) instanceof SetAttribute) {
						SetAttribute set = (SetAttribute) atts.get(k);
						if (set.getFillers().size() > 0) {
							int v = cern.jet.random.Uniform.staticNextIntFromTo(0, da.getFillers().size() - 1);
							set.removeAllEntries();
							set.addEntry(set.getFillers().get(v));
						}
						// } else if (atts.get(k) instanceof IntervalAttribute) {
						// String value = da.getDefaultValue();
						// double x = 0;
						// double y = 0;
						// if (value.equals("")) {
						// value = "0";
						// x = (Double.valueOf(value)).doubleValue() - 1;
						// y = x + 2;
						// } else if (value.indexOf(" - ") > 0) {
						// String[] fromTo = value.split(" - ");
						// x = (Double.valueOf(fromTo[0])).doubleValue();
						// y = (Double.valueOf(fromTo[1])).doubleValue();
						// } else {
						// x = (Double.valueOf(value)).doubleValue() - 1;
						// y = x + 2;
						// }
						// double v = cern.jet.random.Uniform.staticNextDoubleFromTo(x, y);
						// IntervalAttribute att = (IntervalAttribute) atts.get(k);
						// att.setFrom(v);
						// att.setTo(v);
					} else if (atts.get(k) instanceof NumericalAttribute && isNumerical(da.getDefaultValue())) {
						NumericalAttribute att = (NumericalAttribute) atts.get(k);
						double mean = Double.parseDouble(da.getDefaultValue());
						double v = cern.jet.random.Uniform.staticNextDoubleFromTo(mean - mean, mean * 2);
						att.setValue(v);
					}
					obj.addOrSetAttribute(lists.get(j), atts.get(k));
				}
			}
		}
		return obj;
	}

	public static Instance randomiseUniformDistributedAttributeValues(Instance obj, String attrList) {

		List<String> lists = obj.getAttributesListNames();

		List<Attribute> atts = obj.getAttributes(attrList);
		for (int k = 0; k < atts.size(); k++) {
			DomainAttribute da = obj.getDefinition().getAttribute(attrList, atts.get(k).getName());
			if (da.isMutable()) {
				if (atts.get(k) instanceof SetAttribute) {
					SetAttribute set = (SetAttribute) atts.get(k);
					if (set.getFillers().size() > 0) {
						int v = cern.jet.random.Uniform.staticNextIntFromTo(0, da.getFillers().size() - 1);
						set.removeAllEntries();
						set.addEntry(set.getFillers().get(v));
					}
				} else if (atts.get(k) instanceof NumericalAttribute && isNumerical(da.getDefaultValue())) {
					NumericalAttribute att = (NumericalAttribute) atts.get(k);
					double mean = Double.parseDouble(da.getDefaultValue());
					double v = cern.jet.random.Uniform.staticNextDoubleFromTo(mean - mean, mean * 2);
					att.setValue(v);
				}
				obj.addOrSetAttribute(attrList, atts.get(k));
			}
		}
		return obj;
	}

	private static boolean isNumerical(String defaultValue) {
		try {
			Double.parseDouble(defaultValue);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Randomises attribute values. For this, 'svar' is interpreted as the percentage of variation in the values of the default values of
	 * the domain attributes, which are seen as mean values of the normal distribution from which the values are drawn. This means that svar
	 * is expected to be >0 and <1.
	 * 
	 * @param obj Instance
	 * @param svar double
	 * @return Instance
	 */
	private static Instance randomiseNormallyDistributedAttributeValues(Instance obj, double svar) {

		List<String> lists = obj.getAttributesListNames();

		for (int j = 0; j < lists.size(); j++) {
			List<Attribute> atts = obj.getAttributes(lists.get(j));
			for (int k = 0; k < atts.size(); k++) {
				DomainAttribute da = obj.getDefinition().getAttribute(lists.get(j), atts.get(k).getName());
				if (da.isMutable()) {
					if (atts.get(k) instanceof OrderedSetAttribute) {
						OrderedSetAttribute set = (OrderedSetAttribute) atts.get(k);
						double mean = set.getOrder(da.getDefaultValue());
						double v = cern.jet.random.Normal.staticNextDouble(mean, svar);
						if (set.getFillers().size() <= v) {
							v = set.getFillers().size() - 1;
						} else if (v < 0) {
							v = 0;
						}
						List<String> f = set.getFillers();
						double minDiff = 1000;
						double order = 0;
						for (int i = 0; i < f.size(); i++) {
							double o = set.getOrder(f.get(i));
							if (Math.abs(o - v) < minDiff) {
								order = o;
								minDiff = Math.abs(o - v);
							}
						}
						set.removeAllEntries();
						set.addEntry(set.getFiller(order));
					} else if (atts.get(k) instanceof SetAttribute) {
						SetAttribute set = (SetAttribute) atts.get(k);
						if (set.getFillers().size() > 0) {
							int v = cern.jet.random.Uniform.staticNextIntFromTo(0, da.getFillers().size() - 1);
							set.removeAllEntries();
							set.addEntry(set.getFillers().get(v));
						}
					} else if (atts.get(k) instanceof IntervalAttribute) {
						String value = da.getDefaultValue();
						double x = 0;
						double y = 0;
						if (value.equals("")) {
							value = "0";
							x = (Double.valueOf(value)).doubleValue() - 1;
							y = x + 2;
						} else if (value.indexOf(" - ") > 0) {
							String[] fromTo = value.split(" - ");
							x = (Double.valueOf(fromTo[0])).doubleValue();
							y = (Double.valueOf(fromTo[1])).doubleValue();
						} else {
							x = (Double.valueOf(value)).doubleValue() - 1;
							y = x + 2;
						}
						double interval = (y - x);
						if (interval > 0) {
							double mean = interval / 2;
							double v = cern.jet.random.Normal.staticNextDouble(mean, svar);
							if (mean > 0 && v < 0) {
								v = 0;
							}
							IntervalAttribute att = (IntervalAttribute) atts.get(k);
							att.setFrom(v);
							att.setTo(v);
						}
					} else if (atts.get(k) instanceof NumericalAttribute) {
						NumericalAttribute att = (NumericalAttribute) atts.get(k);
						double mean = Double.parseDouble(da.getDefaultValue().equals("") ? "0" : da.getDefaultValue());
						double v = cern.jet.random.Normal.staticNextDouble(mean, svar);
						if (mean > 0 && v < 0) {
							v = 0;
						}
						att.setValue(v);
					}
					obj.addOrSetAttribute(lists.get(j), atts.get(k));
				}
			}
		}
		return obj;
	}

}
