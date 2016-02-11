package de.s2.gsim.objects.attribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DomainAttribute implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String attType = "";

    private String defaultValue = "";

    private double defaultWeight = -1;

    private List<String> fillers = new ArrayList<>();

    private boolean isMutable = true;

    private boolean isSystem = false;

    private String name; // "price"

    private List<String> slopes = new ArrayList<>();

    public DomainAttribute(String attrName, String attType) {
        name = attrName;
        this.attType = attType;
    }

    public void addFiller(String fillerValue) {
        fillers.add(fillerValue);
    }

    public void addSlope(String slope) {
        if (attType.equals(AttributeConstants.SET) || attType.equals(AttributeConstants.ORDERED_SET) || attType.equals(AttributeConstants.INTERVAL)) {
            slopes.add(slope);
        } else {
            throw new RuntimeException("This attribute has no slope");
        }
    }

    @Override
    public Object clone() {
        DomainAttribute a = new DomainAttribute(getName(), getType());
        String[] slopes = getSlopes();
        if (slopes != null) {
            for (int i = 0; i < slopes.length; i++) {
                a.addSlope(slopes[i]);
            }
        }
        String[] fillers = getFillers();
        if (fillers != null) {
            for (int i = 0; i < fillers.length; i++) {
                a.addFiller(fillers[i]);
            }
        }
        a.setDefault(getDefaultValue());
        a.setWeight(defaultWeight);
        a.setMutable(isMutable());
        a.setSystem(isSystem());

        return a;

    }

    /**
     * Two attributes are equal if they have the same name (!)
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof DomainAttribute) {
            DomainAttribute a = (DomainAttribute) o;
            if (a.getName().equalsIgnoreCase(name)) {
                return true;
            } else {
                return false;
            }
        } else if (o instanceof Attribute) {
            return name.equalsIgnoreCase(((Attribute) o).getName());
        } else {
            return false;
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public double getDefaultWeight() {
        return defaultWeight;
    }

    public String[] getFillers() {
        String[] ss = new String[fillers.size()];
        fillers.toArray(ss);
        return ss;
    }

    public String getName() {
        return name;
    }

    public String[] getSlopes() {
        if (!(attType.equals(AttributeConstants.SET) || attType.equals(AttributeConstants.ORDERED_SET)
                || attType.equals(AttributeConstants.INTERVAL))) {
            return null;
        }
        String[] ss = new String[slopes.size()];
        slopes.toArray(ss);
        return AttributeConstants.SLOPES_REP;
    }

    public String getType() {
        return attType;
    }

    @Override
    public int hashCode() {
        return 21;
    }

    /**
     * Checks if a value is valid, given the properties set in the domain.xml
     */
    public boolean isFiller(String value) {
        if (getType().equals(AttributeConstants.NUMERICAL) && isNumerical(value)
                || getType().equals(AttributeConstants.STRING) && !isNumerical(value)) {
            return true;
        }
        if (fillers.contains("{}")) {
            return true;
        }
        return fillers.contains(value);
    }

    /**
     * Checks if an Attribute instance complies with the properties set in the domain.xml file
     */
    public boolean isInstance(SetAttribute a) {

        if (!a.getName().equals(name)) {
            return false;
        }

        if (fillers.size() == 0) {
            return true;
        } else {
            Iterator<String> iter = a.getEntries().iterator();
            while (iter.hasNext()) {
                String attrValue = iter.next();
                if (!isFiller(attrValue)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean isMutable() {
        return isMutable;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public boolean isWeighted() {
        return defaultWeight >= 0;
    }

    public void setDefault(String val) {
        defaultValue = val;
    }

    public void setFillers(String[] fillers) {
        this.fillers.clear();
        for (int i = 0; i < fillers.length; i++) {
            this.fillers.add(fillers[i]);
        }
    }

    public void setMutable(boolean b) {
        isMutable = b;
    }

    public void setSystem(boolean b) {
        isSystem = b;
    }

    public void setWeight(double w) {
        defaultWeight = w;
    }

    @Override
    public String toString() {
        String s = "";
        s = getName() + " (" + defaultValue + ")";
        return s;
    }

    private boolean isNumerical(String s) {
        try {
            Double.valueOf(s).doubleValue();
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
