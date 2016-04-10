package de.s2.gsim.objects.attribute;

/**
 * Base class for all attributes.
 * 
 * @author stephan
 *
 */
public abstract class Attribute implements Cloneable {

    private boolean isMutable = true;

    private boolean isSystem = false;

    private String name;

    /***
     * Constructor.
     * 
     * @param name the attribute name
     */
    protected Attribute(String name) {
        this.name = name;
    }

    @Override
    public abstract Attribute clone();

    /**
     * Checks whether two attributes have the same value.
     * 
     * @param attribute
     * @return true if both attributes have the same value, false otherwise
     */
    public abstract boolean equalsValue(Attribute attribute);

    /**
     * Creates a readable String representation of the attribute value.
     * 
     * @return the value string
     */
    public abstract String toValueString();

    /**
     * Two attributes are equal if they have the same name
     */
    @Override
    public boolean equals(Object o) {

        if (o instanceof Attribute) {
            Attribute a = (Attribute) o;
            if (a.getName().equalsIgnoreCase(name)) {
                return true;
            }
        } else if (o instanceof DomainAttribute) {
            DomainAttribute a = (DomainAttribute) o;
            if (a.getName().equalsIgnoreCase(name)) {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Gets the name of the attribute.
     * 
     * @return the attribute name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return (int) (name.hashCode());
    }

    /**
     * Checks whether this attribute may be modified or not (important for generators).
     * 
     * @return true if mutable, false otherwise
     */
    public boolean isMutable() {
        return isMutable;
    }

    /**
     * Checks whether this attribute is a system attribute.
     * 
     * @return true if system attribute, false otherwise
     */
    public boolean isSystem() {
        return isSystem;
    }

    /**
     * Set mutable.
     * 
     * @param isMutable true if the attribute can be modified, false otherwise
     */
    public void setMutable(boolean isMutable) {
        this.isMutable = isMutable;
    }

    /**
     * Set system.
     * 
     * @param isSystem true if system attribute, false otherwise
     */
    public void setSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("(value=").append(toValueString()).append(")").toString();
    }

}
