package de.s2.gsim.environment;

import java.util.HashMap;

public class TypedMap<U> extends HashMap<String, TypedList<U>> {

    private static final long serialVersionUID = 1L;

    public TypedMap() {
    }

    public TypedMap(TypedMap<U> map) {
        super(map);
    }

    public TypedList<U> put(String name, TypedList<U> list) {
        return super.put(name, list);
    }

}
