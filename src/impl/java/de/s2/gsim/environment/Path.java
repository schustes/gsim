package de.s2.gsim.environment;

import java.util.List;

public class Path<T> {

    enum Type {
        OBJECT, ATTRIBUTE, LIST, PATH
    }

    private final String name;

    private Path<T> next;

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
    
    public Path<T> next() {
        return next;
    }
    
    public Type getType() {
        return type;
    }

    public void append(Path<T> p) {
        this.next = p;
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

    public static <T> Path<T> attributePath(String... path) {
        
        if (path.length < 2) {
            throw new IllegalArgumentException("Path must not be empty");
        }

        return build(Type.ATTRIBUTE, path);
    }

    public static <T extends Unit<?,?>> Path<T> objectPath(String... path) {

        if (path.length < 2) {
            throw new IllegalArgumentException("Path must not be empty");
        }

        return build(Type.OBJECT, path);

    }

    public static <T extends TypedList<?>> Path<T> objectListPath(String... path) {

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

    private static <T> Path<T> build(Type type, String... path) {
        Path<T> initial = new Path<T>(path[0], Type.PATH);
        Path<T> current = initial;
        for (int i = 1; i < path.length - 1; i++) {
            Path<T> next = new Path<T>(path[i], Type.PATH);
            current.append(next);
            current = next;
        }
        if (path.length > 1) {
            Path<T> last = new Path<T>(path[path.length - 1], Type.ATTRIBUTE);
            current.append(last);
        }
        return initial;
    }


}
