package de.s2.gsim.def.objects;

import java.util.HashMap;

public class TypedMapOLD extends HashMap<String, TypedListOLD> {

    private static final long serialVersionUID = 1L;

    public TypedMapOLD() {
    }

    public TypedMapOLD(TypedMapOLD map) {
        super(map);
    }

    public TypedListOLD put(String name, TypedListOLD list) {
        return super.put(name, list);
    }

}
