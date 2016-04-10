package de.s2.gsim.environment;

import java.util.ArrayList;

public class TypedList<U> extends ArrayList<U> {

    private static final long serialVersionUID = 1L;

    private Frame type;

    public TypedList(Frame type) {
        this.type = type;
    }

    public TypedList(TypedList<U> c) {
        super(c);
    }

    public Frame getType() {
        return type;
    }
    
}
