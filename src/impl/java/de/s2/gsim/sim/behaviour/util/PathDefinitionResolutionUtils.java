package de.s2.gsim.sim.behaviour.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.TypedList;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * Contains methods that can determine paths and object as well as attribute types from list definitions. That means, that the actual
 * instances or frames do not have to be necessarily present, but only the list types in a {@link TypedList}. This is useful when this
 * information has to be available before the simulation runs and actual instances are added and so on.
 * 
 * @author Stephan
 *
 */
public abstract class PathDefinitionResolutionUtils {

	private PathDefinitionResolutionUtils() {
		// immutable class
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

	/**
	 * Extracts an attribute from the given path. This can be a child object's attribute, in which case the information is retrieved from
	 * the list type. Or it can be part of the frame, in which case the respective frame's attribute is returned.
	 * 
	 * @param owner
	 * @param path
	 * @return
	 */
	public static Optional<DomainAttribute> extractAttribute(Frame owner, String path) {
		DomainAttribute a;
		if (PathDefinitionResolutionUtils.referencesChildFrame(owner, path.toString())) {
			Frame child = PathDefinitionResolutionUtils.extractChildType(owner, path.toString());
			a = (DomainAttribute) child
			        .resolvePath(PathDefinitionResolutionUtils.extractChildAttributePathWithoutParent(owner, path.toString()).get());
		} else {
			a = owner.resolvePath(Path.attributePath(path.split("/")));
		}
		return Optional.ofNullable(a);
	}

	/**
	 * Determines from the object lists of the owner whether a path string refers to a child or a child's attribute.
	 * 
	 * @param owningAgent
	 * @param pathString
	 * @return
	 */
	public static boolean referencesChildFrame(Frame owningAgent, String pathString) {
		if (!pathString.contains("/")) {
			return false;
		}
		return resolveChildFrameAndDoOrElse(owningAgent, pathString, (TypedList<Frame> resolved) -> {
			return true;
		}, () -> false);
	}

	/**
	 * Transforms a path string into a sub-path containing without the owner's name in the path.
	 * 
	 * @param owningAgent
	 * @param pathString
	 * @return
	 */
	public static String resolveChildFrameWithList(Frame owningAgent, String pathString) {

		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;
		Path<?> pn = new Path<Object>(p.getName(), p.getType());

		return resolveChildFrameAndDoOrElse(owningAgent, pathString, (TypedList<Frame> resolved) -> {
			return pn.getName() + "/" + resolved.getType().getName();
		}, () -> null);
	}

	/**
	 * Transforms a path into a sub-path containing only the path information of the referenced child or returns null if there is no child
	 * referenced by the given path.
	 * 
	 * @param owningAgent
	 * @param pathString
	 * @return
	 */
	public static String resolveChildFrameWithoutList(Frame owningAgent, String pathString) {

		return resolveChildFrameAndDoOrElse(owningAgent, pathString, (TypedList<Frame> resolved) -> {
			TypedList<Frame> list = (TypedList<Frame>) resolved;
			return list.getType().getName();
		}, () -> null);
	}

	/**
	 * Extracts the actual frame of the child referenced by the given path-string or returns null if there is no such child.
	 * 
	 * @param owningAgent
	 * @param pathString
	 * @return
	 */
	public static Frame extractChildType(Frame owningAgent, String pathString) {

		return resolveChildFrameAndDoOrElse(owningAgent, pathString, (TypedList<Frame> resolved) -> {
			return resolved.getType();
		}, () -> null);
	}

	/**
	 * Generic function used to extract something from the frame referenced by the given path string.
	 * 
	 * @param owningAgent owner
	 * @param pathString the path
	 * @param supplier a function that is applied to the {@link TypedList} if it can be resolved up to that point
	 * @param otherwise a supplier that is called when nothing was found or the path did not resolve to a typed list
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <R> R resolveChildFrameAndDoOrElse(Frame owningAgent, String pathString, Function<TypedList<Frame>, R> supplier,
	        Supplier<R> otherwise) {
		Path<Attribute> path = Path.attributePath(pathString.split("/"));
		Path<?> p = path;
		Path<?> pn = new Path(p.getName(), p.getType());
		while (p != null) {
			try {
				Object o = owningAgent.resolvePath(pn);
				if ((o instanceof TypedList)) {
					return supplier.apply((TypedList<Frame>) o);
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
