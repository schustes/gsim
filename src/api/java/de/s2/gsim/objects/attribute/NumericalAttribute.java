package de.s2.gsim.objects.attribute;

public class NumericalAttribute extends Attribute {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private double value = 0;

    public NumericalAttribute(String name, double value) {
        super(name);
        this.value = value;
    }

    @Override
    public Object clone() {
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

    public double getValue() {
        return value;
    }

    public void setValue(double d) {
        value = d;
    }

    @Override
    public String toValueString() {
        return String.valueOf(value);
    }
}
