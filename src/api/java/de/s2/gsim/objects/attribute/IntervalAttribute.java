package de.s2.gsim.objects.attribute;

public class IntervalAttribute extends NumericalAttribute {

    public static final long serialVersionUID = -77185169091150093L;

    private double from = 0;

    private String slope = "FLAT"; // default-value

    private double to = 0;

    public IntervalAttribute(String name, double from, double to) {
        super(name, (from + to) / 2);
        this.from = from;
        this.to = to;
    }

    public IntervalAttribute(String name, double from, double to, double value) {
        super(name, value);
        this.from = from;
        this.to = to;
    }

    public IntervalAttribute(String name, double from, double to, double value, String slope) {
        super(name, value);
        this.from = from;
        this.to = to;
        if (slope != null) {
            this.slope = slope;
        }
    }

    public IntervalAttribute(String name, double from, double to, String slope) {
        super(name, (from + to) / 2);
        this.from = from;
        this.to = to;
        if (slope != null) {
            this.slope = slope;
        }
    }

    @Override
    public Object clone() {
        IntervalAttribute a = new IntervalAttribute(getName(), getFrom(), getTo(), getValue(), getSlope());
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

    public double getFrom() {
        return from;
    }

    public String getSlope() {
        return slope;
    }

    public double getTo() {
        return to;
    }

    public void setFrom(double from) {
        this.from = from;
    }

    public void setSlope(String slope) {
        this.slope = slope;
    }

    public void setTo(double to) {
        this.to = to;
    }

    @Override
    public String toValueString() {
        return String.valueOf(from) + " - " + String.valueOf(to);
    }

}
