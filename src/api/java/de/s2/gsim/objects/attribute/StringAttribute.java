package de.s2.gsim.objects.attribute;

/**
 * A StringAttribute holds a simple string port.
 * 
 * @author stephan
 *
 */
public class StringAttribute extends Attribute {

    private String value = null;

    /**
     * Constructor.
     * 
     * @param name the attribute name
     * @param value the attribute port
     */
    public StringAttribute(String name, String value) {
        super(name);
        this.value = value;
    }

    /**
     * Gets the attribute port.
     * 
     * @return the port
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the port
     * 
     * @param value the port
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toValueString() {
        return value;
    }

    @Override
    public StringAttribute clone() {
        StringAttribute a = new StringAttribute(getName(), getValue());
        a.setSystem(isSystem());
        a.setMutable(isMutable());
        return a;
    }

    @Override
    public boolean equalsValue(Attribute a) {
        if (a instanceof StringAttribute) {
            return ((StringAttribute) a).toValueString().equals(value);
        } else {
            return false;
        }
    }


}
