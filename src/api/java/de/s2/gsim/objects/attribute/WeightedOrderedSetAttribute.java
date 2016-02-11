package de.s2.gsim.objects.attribute;

public class WeightedOrderedSetAttribute extends OrderedSetAttribute implements WeightedAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private double weight;

    public WeightedOrderedSetAttribute(String name, String[] fillers, String slope, double w) {
        super(name, slope, fillers);
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
