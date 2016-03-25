package de.s2.gsim.objects.attribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A DomainAttribute serves as the template or frame for concrete {@link Attribute}s. A DomainAttribute defines the type and the possible values or
 * value range and a default value of the concrete attribute that can be generated from it.
 * 
 * @author stephan
 *
 */
public class DomainAttribute {

    private final String name;

    private final AttributeType attType;

    private String defaultValue = "";

    private List<String> fillers = new ArrayList<>();

    private boolean isMutable = true;

    private boolean isSystem = false;


    /**
     * Constructor.
     * 
     * @param attrName the attribute name
     * @param attType the attribute type (one of {@link AttributeConstants}
     */
    public DomainAttribute(String attrName, AttributeType attType) {
        name = attrName;
        this.attType = attType;
    }

    /**
     * Adds a filler to the possible value range if the attribute is a categorical one.
     * 
     * @param fillerValue filler value
     */
    public void addFiller(String fillerValue) {
        fillers.add(fillerValue);
    }


    @Override
    public Object clone() {
        DomainAttribute a = new DomainAttribute(getName(), getType());
        String[] fillers = getFillers();
        if (fillers != null) {
            for (int i = 0; i < fillers.length; i++) {
                a.addFiller(fillers[i]);
            }
        }
        a.setDefault(getDefaultValue());
        a.setMutable(isMutable());
        a.setSystem(isSystem());

        return a;

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DomainAttribute) {
            DomainAttribute a = (DomainAttribute) o;
            if (a.getName().equalsIgnoreCase(name)) {
                return true;
            }
        } else if (o instanceof Attribute) {
            return name.equalsIgnoreCase(((Attribute) o).getName());
        }
            return false;
    }

    /**
     * Gets the default value defined for this domain attribute.
     * 
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the possible values of this domain attribute.
     * 
     * @return the fillers
     */
    public String[] getFillers() {
        String[] ss = new String[fillers.size()];
        fillers.toArray(ss);
        return ss;
    }

    /**
     * Gets the name of this domain attribute.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of this domain attribute.
     * 
     * @return the type
     */
    public AttributeType getType() {
        return attType;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 2;
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
