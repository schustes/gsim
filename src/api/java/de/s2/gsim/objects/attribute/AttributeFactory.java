package de.s2.gsim.objects.attribute;

public class AttributeFactory extends AttributeConstants {

    public AttributeFactory() {
    }

    public static Attribute createDefaultAttribute(DomainAttribute da) {

        Attribute a = null;
        String attType = da.getType();
        String value = da.getDefaultValue();
        String attname = da.getName();

        if (attType.equals(NUMERICAL) || attType.equals(NUMSINGLETON)) {
            if (value.startsWith("?*")) {
            }
            double x = value.equals("") ? 0 : Double.valueOf(value).doubleValue();
            if (!da.isWeighted()) {
                a = new NumericalAttribute(attname, x);
            } else {
                a = new WeightedNumericalAttribute(attname, x, da.getDefaultWeight());
            }
        } else if (attType.equals(ORDERED_SET)) {
            if (!da.isWeighted()) {
                a = new OrderedSetAttribute(attname, INDIFFERENT, da.getFillers());
            } else {
                a = new WeightedOrderedSetAttribute(attname, da.getFillers(), INDIFFERENT, da.getDefaultWeight());
            }
            String[] entries = value.split(",");
            for (int i = 0; i < entries.length; i++) {
                ((OrderedSetAttribute) a).addEntry(entries[i]);
            }
        } else if (attType.equals(SET)) {
            if (!da.isWeighted()) {
                a = new SetAttribute(attname, da.getFillers());
            } else {
                a = new WeightedSetAttribute(attname, da.getFillers(), da.getDefaultWeight());
            }
            String[] entries = value.split(",");
            for (int i = 0; i < entries.length; i++) {
                ((SetAttribute) a).addEntry(entries[i]);
            }
        } else if (attType.equals(STRING) || attType.equals(STRINGSINGLETON)) {
            if (!da.isWeighted()) {
                a = new StringAttribute(attname, value);
            } else {
                a = new WeightedStringAttribute(attname, value, da.getDefaultWeight());
            }
        } else if (attType.equals(INTERVAL)) {
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
                // x = (Double.valueOf(value)).doubleValue() - 1;
                // y = x + 2;
            }

            x = Double.valueOf(da.getFillers()[0]);
            y = Double.valueOf(da.getFillers()[1]);
            double z = value.equals("") ? 0 : Double.valueOf(value).doubleValue();
            if (!da.isWeighted()) {
                a = new IntervalAttribute(attname, x, y, z, AttributeConstants.INDIFFERENT);
            } else {
                a = new WeightedIntervalAttribute(attname, x, y, z, AttributeConstants.INDIFFERENT, da.getDefaultWeight());
            }
        }

        return a;

    }

}
