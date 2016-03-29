package de.s2.gsim.def;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Replace with a usual LinkedList ???
 *
 */
public class TimeOrderedSet<T> extends ArrayList<T> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public TimeOrderedSet() {
        super();
    }

    public TimeOrderedSet(Collection c) {
        super(c);
    }

    @Override
    public void add(int idx, T n) {
        this.remove(n);
        this.add(n);
    }

    @Override
    public boolean add(T n) {
        if (size() == 0) {
            super.add(n);
        } else {
            int pos = size() - 1;
            for (int i = 0; i < size(); i++) {
                Object o = get(i);
                if (o.equals(n)) {
                    this.remove(o);
                    pos = i;
                }
            }
            super.add(pos, n);
        }
        return true;
    }

}
