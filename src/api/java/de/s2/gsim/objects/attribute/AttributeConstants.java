package de.s2.gsim.objects.attribute;

/**
 * Contants class for attributes.
 * 
 * @author stephan
 *
 */
public abstract class AttributeConstants {

    public static String INDIFFERENT = "FLAT";

    public static String INTERVAL = "NumericalInterval";

    public static String LESS_IS_BETTER = "LIB";

    public static String MORE_IS_BETTER = "MIB";

    public static String NUMERICAL = "Numerical";

    public static String NUMSINGLETON = "NumSingleton";

    public static String ORDERED_SET = "OrderedSet";

    public static String SET = "Set";

    public static String[] SLOPES_REP = { "More is better", "Less is better", "Indifferent" };

    public static String STRING = "String";

    public static String STRINGSINGLETON = "StringSingleton";

    public static String getPreferenceType(Attribute a) {
        if (a instanceof IntervalAttribute) {
            return INTERVAL;
        } else if (a instanceof OrderedSetAttribute) {
            return ORDERED_SET;
        } else if (a instanceof SetAttribute) {
            return SET;
        } else if (a instanceof NumericalAttribute) {
            return NUMSINGLETON;
        } else if (a instanceof StringAttribute) {
            return STRINGSINGLETON;
        }
        return null;
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private AttributeConstants() {
        // to prevent instantiation.
    }
}
