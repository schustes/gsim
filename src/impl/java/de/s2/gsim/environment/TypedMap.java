package de.s2.gsim.environment;

import java.util.HashMap;

public class TypedMap<U,A> extends HashMap<String, TypedList<U,A>> {

    private static final long serialVersionUID = 1L;

    public TypedMap() {
    }

    public TypedMap(TypedMap<U,A> map) {
        super(map);
    }

    public TypedList<U,A> put(String name, TypedList<U,A> list) {
        return super.put(name, list);
    }

}
