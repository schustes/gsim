package gsim.def.objects;

import java.util.ArrayList;

public class TypedList extends ArrayList<Unit> {

    private static final long serialVersionUID = 1L;

    private Frame type;

    public TypedList(Frame f) {
        type = f;
    }

    public TypedList(TypedList c) {
        super(c);
    }

    public Frame getType() {
        return type;
    }

}
