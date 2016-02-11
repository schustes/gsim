package de.s2.gsim.objects.attribute;

public class WeightedNumericalAttribute extends NumericalAttribute implements WeightedAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private double weight;

    public WeightedNumericalAttribute(String name, double value, double w) {
        super(name, value);
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
