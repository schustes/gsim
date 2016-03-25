package de.s2.gsim.objects.attribute;

public enum AttributeType {

    /**
     * A numerical interval attribute specififes a numeric range (e.g. 0-100).
     */
    INTERVAL,

    /**
     * A numerical attribute holds numbers (e.g. 12).
     */
    NUMERICAL,

    /**
     * Set attribute (e.g. {a,b,c}), which can hold several values at once.
     */
    SET,

    /**
     * Ordered set attribute is like a SetAttribute, but it furthermore assumes that the components can be ordered according to some criterion like
     * better or worse.
     */
    ODERED_SET,

    /**
     * A string attribute holds String values (e.g. 'string').
     */
    STRING;
}
