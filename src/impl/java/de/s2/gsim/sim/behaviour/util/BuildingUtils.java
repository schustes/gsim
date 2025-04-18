package de.s2.gsim.sim.behaviour.util;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class BuildingUtils {

	public static String getDefiningRoleForRLRule(RuntimeAgent agent, String ruleName) {
		GenericAgentClass def = (GenericAgentClass) agent.getDefinition();

		if (def.getBehaviour().getDeclaredRLRule(ruleName).isPresent()) {
			return def.getName();
		}

		for (Frame f : def.getAncestors()) {
			GenericAgentClass agentClass = (GenericAgentClass) f;
			if (agentClass.getBehaviour().getDeclaredRLRule(ruleName).isPresent()) {
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

	public static Optional<Path<?>> extractChildAttributePathWithoutParent(Frame owningAgent, String pathString) {
		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;

		String frameName = resolveChildFrameWithoutList(owningAgent, pathString);

		Deque<Path<?>> stack = new ArrayDeque<>();
		while (p != null && !p.last().getName().equals(frameName)) {
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

		return Optional.ofNullable(n);
	}

	public static Optional<DomainAttribute> extractAttribute(Frame owner, String path) {
		DomainAttribute a;
		if (BuildingUtils.referencesChildFrame(owner, path.toString())) {
			Frame child = BuildingUtils.extractChildType(owner, path.toString());
			a = (DomainAttribute) child
			        .resolvePath(BuildingUtils.extractChildAttributePathWithoutParent(owner, path.toString()).get());
		} else {
			a = owner.resolvePath(Path.attributePath(path.split("/")));
		}
		return Optional.ofNullable(a);
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

	@SuppressWarnings({ "unchecked" })
	public static Frame extractChildType(Frame owningAgent, String pathString) {

		return resolveChildFrameAndDoOrElse(owningAgent, pathString, (Object resolved) -> {
			if ((resolved instanceof TypedList)) {
				TypedList<Frame> list = (TypedList<Frame>) resolved;
				return list.getType();
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
				return otherwise.get();
			}
			p = p.next();
			if (p != null) {
				pn.append(new Path(p.getName(), p.getType()));
			}
		}
		return otherwise.get();
	}


}
