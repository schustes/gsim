package de.s2.gsim.objects.attribute;

public class WeightedIntervalAttribute extends IntervalAttribute implements WeightedAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private double weight;

    public WeightedIntervalAttribute(String name, double from, double to, double value, double w) {
        super(name, from, to, value);
        weight = w;
    }

    public WeightedIntervalAttribute(String name, double from, double to, double value, String slope, double w) {
        super(name, from, to, value, slope);
        weight = w;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(double w) {
        weight = w;
    }
}
