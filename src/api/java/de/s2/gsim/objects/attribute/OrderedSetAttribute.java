package de.s2.gsim.objects.attribute;

import java.util.Iterator;

public class OrderedSetAttribute extends SetAttribute {

    public final static long serialVersionUID = -7414629019203902476L;

    private String slope = "FLAT"; // default

    public OrderedSetAttribute(String name, String slope, String[] fillers) {
        super(name, fillers);
        if (slope != null) {
            this.slope = slope;
        }
    }

    @Override
    public Object clone() {
        OrderedSetAttribute a = new OrderedSetAttribute(getName(), getSlope(), fillers);
        Iterator<String> iter = getEntries().iterator();
        while (iter.hasNext()) {
            a.addEntry((String) iter.next());
        }
        a.setSystem(isSystem());
        a.setMutable(isMutable());
        return a;
    }

    public String getFiller(double forOrder) {

        if (forOrder == 0) {
            return fillers[0];
        }

        double size = super.getFillers().length;
        double stepSize = 1d / size;
        double index = forOrder / stepSize - 1;
        return fillers[(int) index];

    }

    public double getOrder(String entry) {

        double size = super.getFillers().length;
        double pos = getPos(entry);

        if (pos > -1) {
            return (pos + 1) / size;
        }

        return 0;
    }

    public String getSlope() {
        return slope;
    }

    private int getPos(String entry) {
        String[] f = getFillers();
        for (int i = 0; i < f.length; i++) {
            if (f[i].equals(entry)) {
                return i;
            }
        }
        return -1;
    }
}
