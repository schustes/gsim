package de.s2.gsim.objects.attribute;

public class StringAttribute extends Attribute {

    public static final long serialVersionUID = -4982728557786529261L;

    private String value = null;

    public StringAttribute(String name, String value) {
        super(name);
        this.value = value;
    }

    @Override
    public Object clone() {
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

    public String getValue() {
        return value;
    }

    public void setValue(String s) {
        value = s;
    }

    @Override
    public String toValueString() {
        return value;
    }

}
