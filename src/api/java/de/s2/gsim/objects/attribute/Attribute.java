package de.s2.gsim.objects.attribute;

public abstract class Attribute implements java.io.Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean isMutable = true;

    private boolean isSystem = false;

    private String name;

    private double weight = -1;

    protected Attribute(String name) {
        this.name = name;
    }

    @Override
    public abstract Object clone();

    /**
     * Two attributes are equal if they have the same name (!)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Attribute) {
            Attribute a = (Attribute) o;
            if (a.getName().equalsIgnoreCase(name)) {
                return true;
            } else {
                return false;
            }
        } else if (o instanceof DomainAttribute) {
            DomainAttribute a = (DomainAttribute) o;
            if (a.getName().equalsIgnoreCase(name)) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    public abstract boolean equalsValue(Attribute a);

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        return (int) (name.hashCode() * weight);
    }

    public boolean isMutable() {
        return isMutable;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setMutable(boolean b) {
        isMutable = b;
    }

    public void setSystem(boolean b) {
        isSystem = b;
    }

    @Override
    public String toString() {
        String val = toValueString();
        String s = "(value=" + val;
        if (getWeight() > -1) {
            s = s + ", weight=" + getWeight();
        }
        s += ")";
        return s;
    }

    public abstract String toValueString();

}
