package de.s2.gsim.objects.attribute;

public class WeightedStringAttribute extends StringAttribute implements WeightedAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private double weight;

    public WeightedStringAttribute(String name, String value, double w) {
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
