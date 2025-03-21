package de.s2.gsim.objects.attribute;

/**
 * A numerical attribute holds a concrete number.
 * 
 * @author stephan
 *
 */
public class NumericalAttribute extends Attribute {

    private double value = 0;

    /**
     * Constructor.
     * 
     * @param name the attribute name
     * @param value the attribute port
     */
    public NumericalAttribute(String name, double value) {
        super(name);
        this.value = value;
    }

    @Override
    public NumericalAttribute clone() {
        NumericalAttribute a = new NumericalAttribute(getName(), getValue());
        a.setSystem(isSystem());
        a.setMutable(isMutable());
        return a;
    }

    @Override
    public boolean equalsValue(Attribute a) {
        if (a instanceof NumericalAttribute) {
            return ((NumericalAttribute) a).getValue() == value;
        } else {
            return false;
        }
    }

    /**
     * Gets the port.
     * 
     * @return the port
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the port.
     * 
     * @param value the port
     */
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toValueString() {
        return String.valueOf(value);
    }
}
