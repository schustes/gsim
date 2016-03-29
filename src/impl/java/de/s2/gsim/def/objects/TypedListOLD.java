package de.s2.gsim.def.objects;

import java.util.ArrayList;

public class TypedListOLD extends ArrayList<UnitOLD> {

    private static final long serialVersionUID = 1L;

    private FrameOLD type;

    public TypedListOLD(FrameOLD f) {
        type = f;
    }

    public TypedListOLD(TypedListOLD c) {
        super(c);
    }

    public FrameOLD getType() {
        return type;
    }

}
