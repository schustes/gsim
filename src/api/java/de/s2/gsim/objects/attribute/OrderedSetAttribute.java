package de.s2.gsim.objects.attribute;

import java.util.Iterator;
import java.util.List;

/**
 * An OrderedSetAttribute is a SetAttribute that defines an order on its components.
 * 
 * @author stephan
 *
 */
public class OrderedSetAttribute extends SetAttribute {

    /**
     * Constructor.
     * 
     * @param name the name of the attribute
     * @param fillers the values of the attribute
     */
    public OrderedSetAttribute(String name, List<String> fillers) {
        super(name, fillers);
    }

    @Override
    public OrderedSetAttribute clone() {
        OrderedSetAttribute a = new OrderedSetAttribute(getName(), fillers);
        Iterator<String> iter = getEntries().iterator();
        while (iter.hasNext()) {
            a.addEntry((String) iter.next());
        }
        a.setSystem(isSystem());
        a.setMutable(isMutable());
        return a;
    }

    /**
     * Gets the value at the specified position in the order.
     * 
     * @param forOrder order position
     * @return the value
     */
    public String getFiller(double forOrder) {

        if (forOrder == 0) {
            return fillers.get(0);
        }

        double size = super.getFillers().size();
        double stepSize = 1d / size;
        double index = forOrder / stepSize - 1;
        return fillers.get((int) index);

    }

    /**
     * Get the order of the specified entry.
     * 
     * @param entry the entry
     * @return the order
     */
    public double getOrder(String entry) {

        double size = super.getFillers().size();
        double pos = getPos(entry);

        if (pos > -1) {
            return (pos + 1) / size;
        }

        return 0;
    }

    /**
     * Gets the position in the filler array for the given entry.
     * 
     * @param entry the entry
     * @return the position
     */
    private int getPos(String entry) {
        List<String> f = getFillers();
        for (int i = 0; i < f.size(); i++) {
            if (f.get(i).equals(entry)) {
                return i;
            }
        }
        return -1;
    }
}
