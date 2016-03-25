package de.s2.gsim.objects.attribute;


/**
 * Factory class to create attributes.
 * 
 * TODO might be moved to implementation!
 * 
 * @author stephan
 *
 */
public class AttributeFactory {

    /**
     * Private constructor to prevent instantiation.
     */
    private AttributeFactory() {
    }

    /**
     * Creates a default attribute from a {@link DomainAttribute}.
     * 
     * @param domainAttribute the domain attribute
     * @return the attribute
     */
    public static Attribute createDefaultAttribute(DomainAttribute domainAttribute) {

        Attribute a = null;
        AttributeType attributeType = domainAttribute.getType();
        String value = domainAttribute.getDefaultValue();
        String attname = domainAttribute.getName();

        if (attributeType == AttributeType.NUMERICAL) {
            double x = value.equals("") ? 0 : Double.valueOf(value).doubleValue();
            a = new NumericalAttribute(attname, x);
        } else if (attributeType == AttributeType.ODERED_SET) {
            a = new OrderedSetAttribute(attname, domainAttribute.getFillers());
            String[] entries = value.split(",");
            for (int i = 0; i < entries.length; i++) {
                ((OrderedSetAttribute) a).addEntry(entries[i]);
            }
        } else if (attributeType == AttributeType.SET) {
            a = new SetAttribute(attname, domainAttribute.getFillers());
            String[] entries = value.split(",");
            for (int i = 0; i < entries.length; i++) {
                ((SetAttribute) a).addEntry(entries[i]);
            }
        } else if (attributeType == AttributeType.STRING) {
            a = new StringAttribute(attname, value);
        } else if (attributeType == AttributeType.INTERVAL) {
            double x = 0, y = 0;
            if (value.equals("")) {
                value = "0";
                x = (Double.valueOf(value)).doubleValue() - 1;
                y = x + 2;
            } else if (value.indexOf(" - ") > 0) {
                String[] fromTo = value.split(" - ");
                x = (Double.valueOf(fromTo[0])).doubleValue();
                y = (Double.valueOf(fromTo[1])).doubleValue();
            } else {
                x = (Double.valueOf(value)).doubleValue();
                y = (Double.valueOf(value)).doubleValue();
            }

            x = Double.valueOf(domainAttribute.getFillers()[0]);
            y = Double.valueOf(domainAttribute.getFillers()[1]);
            double z = value.equals("") ? 0 : Double.valueOf(value).doubleValue();
            a = new IntervalAttribute(attname, x, y, z);
        }

        return a;

    }

}
