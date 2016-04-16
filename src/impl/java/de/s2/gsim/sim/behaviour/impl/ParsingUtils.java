package de.s2.gsim.sim.behaviour.impl;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeConstants;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.sim.GSimEngineException;

public class ParsingUtils {

	public static String getDefiningRoleForRLRule(RuntimeAgent agent, String ruleName) {
		GenericAgentClass def = (GenericAgentClass) agent.getDefinition();

		if (def.getBehaviour().getDeclaredRLRule(ruleName) != null) {
			return def.getName();
		}

		for (Frame f : def.getAncestors()) {
			GenericAgentClass agentClass = (GenericAgentClass) f;
			if (agentClass.getBehaviour().getDeclaredRLRule(ruleName) != null) {
				return agentClass.getName();
			}
		}
		return "default";
	}

	public static String getDefiningRoleForRule(RuntimeAgent agent, String ruleName) {

		GenericAgentClass def = (GenericAgentClass) agent.getDefinition();

		if (def.getBehaviour().getDeclaredRule(ruleName) != null) {
			return def.getName();
		}

		for (Frame f : def.getAncestors()) {
			GenericAgentClass agentClass = (GenericAgentClass) f;
			if (agentClass.getBehaviour().getDeclaredRule(ruleName) != null) {
				return agentClass.getName();
			}
		}
		return "default";
	}

	public static boolean isNumericalAttributeSpec(Instance agent, String attRef) throws GSimEngineException {
		Attribute att = null;
		if (!attRef.contains("::")) {
			att = agent.resolvePath(Path.attributePath(attRef.split("/")));
			if (att instanceof NumericalAttribute || att instanceof IntervalAttribute) {
				return true;
			} 
			return false;
		} else {
			String[] ref0 = attRef.split("::")[0].split("/");
			String[] ref1 = attRef.split("::")[1].split("/");
			String listName = ref0[0];
			Frame object = agent.getDefinition().getListType(listName);
			if (object != null) {
				DomainAttribute datt = (DomainAttribute) object.resolvePath(Path.attributePath(ref1));
				if (datt.getType().equals(AttributeConstants.NUMERICAL) || datt.getType().equals(AttributeConstants.INTERVAL)) {
					return true;
				} 
				return false;
			}
		}
		throw new GSimEngineException("Attribute reference " + attRef + " resolved to null!");
	}

	public static String resolveAttribute(String s) {
		// boolean b = false;
		if (s.contains("::")) {
			s = s.split("::")[1];
			// b = true;
		}

		String[] a = s.split("/");

		// if (a.length==2 && !b) return null;

		String ret = "";
		int y = 0;
		// if (!b) y=2;
		for (int i = y; i < a.length; i++) {
			if (ret.length() > 0) {
				ret += "/";
			}
			ret += a[i];
		}

		return ret;
	}

	public static String resolveList(String s) {
		String[] a = s.split("/");

		String list = a[0];

		if (list.contains("$")) {
			if (list.lastIndexOf("$") != list.indexOf("$")) {
				list = list.substring(list.lastIndexOf("$"));
				list = list.replace("$", "");
			} else {
				list = list.replace("$", "");
			}
		}
		return list;

	}

	public static String resolveObjectClass(String s) {
		String[] a = s.split("/");

		String list = a[0];
		String object = a[1];

		if (object.contains("::")) {
			object = object.substring(0, object.indexOf("::"));
		}

		if (list.contains("$")) {
			if (list.lastIndexOf("$") != list.indexOf("$")) {
				list = list.substring(list.lastIndexOf("$"));
				list = list.replace("$", "");
			} else {
				list = list.replace("$", "");
			}
		}
		return list + "/" + object;

	}

	public static String resolveObjectClassNoList(String s) {
		String[] a = s.split("/");

		String list = a[0];
		String object = a[1];

		if (object.contains("::")) {
			object = object.substring(0, object.indexOf("::"));
		}

		if (list.contains("$")) {
			if (list.lastIndexOf("$") != list.indexOf("$")) {
				list = list.substring(list.lastIndexOf("$"));
				list = list.replace("$", "");
			} else {
				list = list.replace("$", "");
			}
		}
		return object;

	}

}
