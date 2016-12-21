package de.s2.gsim.sim.behaviour.builder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

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

		Path<Attribute> attrPath = Path.attributePath(attRef.split("/"));
		try {
			Attribute att = agent.resolvePath(attrPath);
			if (att instanceof NumericalAttribute || att instanceof IntervalAttribute) {
				return true;
			}
			return false;
		} catch (GSimDefException e) {
			e.printStackTrace();
		}

		String listName = resolveList(attRef);
		String ref1 = extractChildAttributePathWithoutParent(agent.getDefinition(), attRef);
		Frame object = agent.getDefinition().getListType(listName);
		if (object != null) {
			DomainAttribute datt = (DomainAttribute) object.resolvePath(Path.attributePath(ref1));
			if (datt.getType() == AttributeType.NUMERICAL || datt.getType() == AttributeType.INTERVAL) {
				return true;
			}
			return false;
		}

		throw new GSimEngineException("Attribute reference " + attRef + " resolved to null!");
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

	public static String extractChildAttributePathWithoutParent(Frame owningAgent, String pathString) {
		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;

		Deque<Path<?>> stack = new ArrayDeque<>();
		while (p != null && !isChildFrame(owningAgent, p)) {
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
		if (!pathString.contains("/")) {
			return false;
		}
		return resolveChildFrameAndDoOrElse(owningAgent, pathString, (Object resolved) -> {
			return (resolved instanceof TypedList);
		}, () -> false);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String resolveChildFrameWithList(Frame owningAgent, String pathString) {

		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;
		Path<?> pn = new Path(p.getName(), p.getType());

		return resolveChildFrameAndDoOrElse(owningAgent, pathString, (Object resolved) -> {
			if ((resolved instanceof TypedList)) {
				TypedList<Frame> list = (TypedList<Frame>) resolved;
				return pn.getName() + "/" + list.getType().getName();
			}
			return null;
		}, () -> null);
	}

	@SuppressWarnings({ "unchecked" })
	public static String resolveChildFrameWithoutList(Frame owningAgent, String pathString) {

		return resolveChildFrameAndDoOrElse(owningAgent, pathString, (Object resolved) -> {
			if ((resolved instanceof TypedList)) {
				TypedList<Frame> list = (TypedList<Frame>) resolved;
				return list.getType().getName();
			}
			return null;
		}, () -> null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <R> R resolveChildFrameAndDoOrElse(Frame owningAgent, String pathString, Function<Object, R> supplier,
	        Supplier<R> otherwise) {
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
			if (p != null) {
				pn.append(new Path(p.getName(), p.getType()));
			}
		}
		return otherwise.get();
	}


}
