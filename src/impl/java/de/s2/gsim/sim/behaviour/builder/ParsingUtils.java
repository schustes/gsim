package de.s2.gsim.sim.behaviour.builder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeType;
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
				if (datt.getType() == AttributeType.NUMERICAL || datt.getType() == AttributeType.INTERVAL) {
					return true;
				} 
				return false;
			}
		}
		throw new GSimEngineException("Attribute reference " + attRef + " resolved to null!");
	}

	// TODO needs correct resolution process analogous to resolveObjectClass, otherwise parameters of referred objects are not inserted as
	// facts
	public static String resolveAttribute_DELETE(String s) {
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

	@Deprecated
	public static String resolveObjectClass_DELETE(String s) {
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

	public static String resolveObjectClassNoList_DELETE(String s) {
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

	public static String extractChildAttributePathWithoutParent(Frame owningAgent, String pathString) {
		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;

		Deque<Path<?>> stack = new ArrayDeque<>();
		while (!isChildFrame(owningAgent, p)) {
			stack.push(p.last());
			p = Path.withoutLast(p);
		}

		Path<?> n = null;
		while (!stack.isEmpty()) {
			if (n == null) {
				n = Path.copy(stack.pop());
			} else {
				n.append(stack.pop());
			}
		}
		
		return n.toString();
	}

	private static boolean isChildFrame(Frame agent, Path<?> p) {
		try {
			Object obj = agent.resolvePath(p);
			return obj != null && (obj instanceof Frame);
		} catch (GSimDefException e) {
			return false;
		}
	}

	public static boolean referencesChildInstance(Instance owningAgent, String pathString) {
		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;
		while (p != null) {
			try {
				if ((owningAgent.resolvePath(p) instanceof Instance)) {
					return true;
				}
			} catch (NoSuchElementException e) {
				return false;
			}
			p = p.next();
		}
		return false;
	}

	public static boolean referencesChildFrame(Frame owningAgent, String pathString) {
		return resolveChildFrameAndDo(owningAgent, pathString, (Object resolved) -> {
			return (resolved instanceof TypedList);
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String resolveChildFrameWithList(Frame owningAgent, String pathString) {
		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;
		Path<?> pn = new Path(p.getName(), p.getType());

		return resolveChildFrameAndDo(owningAgent, pathString, (Object resolved) -> {
			if ((resolved instanceof TypedList)) {
				TypedList<Frame> list = (TypedList<Frame>) resolved;
				return pn.getName() + "/" + list.getType().getName();
			}
			return null;
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String resolveChildFrameWithoutList(Frame owningAgent, String pathString) {
		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;

		return resolveChildFrameAndDo(owningAgent, pathString, (Object resolved) -> {
			if ((resolved instanceof TypedList)) {
				TypedList<Frame> list = (TypedList<Frame>) resolved;
				return list.getType().getName();
			}
			return null;
		});
	}

	// TODO fix with/without list, and need to differentiate between instance and frame names during runtime
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String resolveChildFrameWithList_DELETE(Frame owningAgent, String pathString) {
		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;
		Path<?> pn = new Path(p.getName(), p.getType());
		while (p != null) {
			try {
				Object o = owningAgent.resolvePath(pn);
				if ((o instanceof TypedList)) {
					TypedList<Frame> list = (TypedList<Frame>) o;
					return pn.getName() + "/" + list.getType().getName();
				}
			} catch (GSimDefException e) {
				return null;
			}
			p = p.next();
			pn.append(new Path(p.getName(), p.getType()));
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <R> R resolveChildFrameAndDo(Frame owningAgent, String pathString, Function<Object, R> supplier) {
		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;
		Path<?> pn = new Path(p.getName(), p.getType());
		while (p != null) {
			try {
				Object o = owningAgent.resolvePath(pn);
				if ((o instanceof TypedList)) {
					return supplier.apply(o);
				}
			} catch (GSimDefException e) {
				return null;
			}
			p = p.next();
			pn.append(new Path(p.getName(), p.getType()));
		}
		return null;
	}


}
