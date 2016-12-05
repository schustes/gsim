package de.s2.gsim.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//import de.s2.gsim.environment.TypedList;
//import de.s2.gsim.environment.Unit;

public class Path<T> {

	public enum Type {
		OBJECT, ATTRIBUTE, LIST, PATH
	}

	private final String name;

	private Path<?> next;

	private final Type type;

	public Path(String name, Type type) {
		this.name = name;
		this.next = null;
		this.type = type;
	}

	public Path(String name, Path<T> next, Type type) {
		this.name = name;
		this.next = next;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Path<?> next() {
		return next;
	}

	public Type getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
	public <X>Path<X> append(Path<X> p) {
		this.last().appendToSelf(p);
		return (Path<X>) this;
	}

	@SuppressWarnings("unchecked")
	private <X>Path<X> appendToSelf(Path<X> p) {
		this.next = p;
		return (Path<X>) this;
	}

	public boolean isTerminal() {
		return next == null;
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		rek(this, b);
		return b.toString();
	}

	private void rek(Path<?> p, StringBuilder b) {
		b.append('/').append(p.name);
		if (p.next != null) {
			rek(p.next(), b);
		}
	}

	public String[] toStringArray() {
		List<String> list = new ArrayList<>();
		rekArray(this, list);
		return list.toArray(new String[0]);
	}

	private void rekArray(Path<?> p, List<String> list) {
		list.add(p.name);
		if (p.next != null) {
			rekArray(p.next, list);
		}
	}

	public static <M extends List<V>, T, V> Path<M> withoutLastAttributeOrObject(Path<T> path, Type typeBeforeLast) {

		Objects.requireNonNull(path);

		Path<?> p = path.next();
		Path<M> p1 = new Path<>(path.getName(), typeBeforeLast);

		while (p != null) {
			if (p.next() != null) {
				p1.appendToSelf(p);
			}
			p = p.next();
		}

		return p1;

	}

	/**
	 * Cuts the last path part from this path.
	 * 
	 * @param path the path
	 * @return the cut path or null if there is nothing left to remove
	 */
	public static <M extends V, T, V> Path<M> withoutLast(Path<T> path) {

		Path<?> p = path;
		String s = p.toString();
		Path<M> p1 = new Path<>(path.getName(), path.getType());

		while (p != null) {
			p = p.next();
			if (p != null && p.next() != null) {
				p1.appendToSelf(new Path<M>(p.name, p.type));
			}
		}

		if (s.equals(p1.toString())) {
			return null;
		}

		return p1;

	}

	public static <T> Path<T> attributePath(String... path) {

		if (path.length < 2) {
			throw new IllegalArgumentException("Path must not be empty");
		}

		return build(Type.ATTRIBUTE, path);
	}

	public static <T> Path<T> objectPath(String... path) {
		// public static <T extends Unit<?,?>> Path<T> objectPath(String... path) {

		if (path.length < 2) {
			throw new IllegalArgumentException("Path must not be empty");
		}

		return build(Type.OBJECT, path);

	}

	public static <T> Path<T> objectListPath(String... path) {
		// public static <T extends TypedList<?>> Path<T> objectListPath(String... path) {

		if (path.length < 1) {
			throw new IllegalArgumentException("Path must not be empty");
		}

		return build(Type.LIST, path);

	}

	public static <T extends List<?>> Path<T> attributeListPath(String... path) {

		if (path.length < 1) {
			throw new IllegalArgumentException("Path must not be empty");
		}

		return build(Type.LIST, path);

	}

	public static <T extends List<?>> Path<T> attributeListPath(List<String> path) {

		if (path.size() < 1) {
			throw new IllegalArgumentException("Path must not be empty");
		}

		return build(Type.LIST, path.toArray(new String[0]));

	}

	private static <T> Path<T> build(Type type, String[] path) {
		Path<T> initial = new Path<T>(path[0], Type.PATH);
		Path<T> current = initial;
		for (int i = 1; i < path.length - 1; i++) {
			Path<T> next = new Path<T>(path[i], Type.PATH);
			current.appendToSelf(next);
			current = next;
		}
		if (path.length > 1) {
			Path<T> last = new Path<T>(path[path.length - 1], Type.ATTRIBUTE);
			current.appendToSelf(last);
		}
		return initial;
	}

	public String lastAsString() {
		Path<?> p = last();
		return p.name;
	}

	public Path<?> last() {
		Path<?> p = next;
		Path<?> ret = this;
		while (p!=null) {
			ret = p;
			p = p.next;
		}
		return ret;
	}


}
