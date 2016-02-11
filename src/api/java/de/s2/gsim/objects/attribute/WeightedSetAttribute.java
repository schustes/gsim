package de.s2.gsim.objects.attribute;

public class WeightedSetAttribute extends SetAttribute implements WeightedAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private double weight;

    public WeightedSetAttribute(String name, String[] fillers, double w) {
        super(name, fillers);
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
