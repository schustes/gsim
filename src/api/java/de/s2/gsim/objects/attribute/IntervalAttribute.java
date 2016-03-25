package de.s2.gsim.objects.attribute;

/**
 * An interval attribute specifies a range of values it can take and optionally a concrete value it has at the moment.
 * 
 * @author stephan
 *
 */
public class IntervalAttribute extends NumericalAttribute {

    private double from = 0;

    private double to = 0;

    /**
     * Constructor.
     * 
     * @param name name of the attribute
     * @param from lower bound
     * @param to upper bound
     */
    public IntervalAttribute(String name, double from, double to) {
        super(name, (from + to) / 2);
        this.from = from;
        this.to = to;
    }

    /**
     * Constructor.
     * 
     * @param name name of the attribute
     * @param from lower bound
     * @param to upper bound
     * @param value the value of the attribute
     */
    public IntervalAttribute(String name, double from, double to, double value) {
        super(name, value);
        this.from = from;
        this.to = to;
    }

    @Override
    public Object clone() {
        IntervalAttribute a = new IntervalAttribute(getName(), getFrom(), getTo(), getValue());
        a.setSystem(isSystem());
        a.setMutable(isMutable());
        return a;
    }

    @Override
    public boolean equalsValue(Attribute a) {
        if (a instanceof IntervalAttribute) {
            IntervalAttribute inter = (IntervalAttribute) a;
            if (!(inter.getFrom() == getFrom() && inter.getTo() == getTo())) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the lower bound.
     * 
     * @return the lower bound
     */
    public double getFrom() {
        return from;
    }

    /**
     * Gets the upper bound.
     * 
     * @return the upper bound
     */
    public double getTo() {
        return to;
    }

    /**
     * Set the lower bound.
     * 
     * @param from the lower bound
     */
    public void setFrom(double from) {
        this.from = from;
    }

    /**
     * Sets the upper bound.
     * 
     * @param to the upper bound
     */
    public void setTo(double to) {
        this.to = to;
    }

    @Override
    public String toValueString() {
        return String.valueOf(from) + " - " + String.valueOf(to);
    }

}
