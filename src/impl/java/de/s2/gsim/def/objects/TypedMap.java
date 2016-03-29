package de.s2.gsim.def.objects;

import java.util.HashMap;

public class TypedMap extends HashMap<String, TypedList> {

    private static final long serialVersionUID = 1L;

    public TypedMap() {
    }

    public TypedMap(TypedMap map) {
        super(map);
    }

    public TypedList put(String name, TypedList list) {
        return super.put(name, list);
    }

}
