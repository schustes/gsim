package de.s2.gsim.objects.attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A DomainAttribute serves as the template or frame for concrete {@link Attribute}s. A DomainAttribute defines the type and the possible values or
 * port range and a default port of the concrete attribute that can be generated from it.
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
	 * Constructor.
	 * 
	 * @param attrName the attribute name
	 * @param attType the attribute type (one of {@link AttributeConstants}
	 * @param defaultValue default port
	 */
	public DomainAttribute(String attrName, AttributeType attType, String defaultValue) {
		name = attrName;
		this.attType = attType;
		this.defaultValue = defaultValue;
	}

	/**
	 * Adds a filler to the possible port range if the attribute is a categorical one.
	 * 
	 * @param fillerValue filler port
	 */
    public void addFiller(String fillerValue) {
        fillers.add(fillerValue);
    }


    @Override
    public DomainAttribute clone() {
        DomainAttribute a = new DomainAttribute(getName(), getType());
        a.fillers = fillers.stream().collect(Collectors.toList());
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
     * Gets the default port defined for this domain attribute.
     * 
     * @return the default port
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the possible values of this domain attribute.
     * 
     * @return the fillers
     * 
     */
    public List<String> getFillers() {
        return new ArrayList<>(fillers);
    }
    
    /**
     * Remove fillers.
     */
    public void clearFillers() {
    	fillers.clear();
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
     * Checks if a port is valid, given the properties set in the domain.xml
     */
    public boolean isFiller(String value) {
        if (getType() == AttributeType.NUMERICAL && isNumerical(value)
                || getType() == AttributeType.STRING && !isNumerical(value)) {
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

    public void setMutable(boolean b) {
        isMutable = b;
    }

    public void setSystem(boolean b) {
        isSystem = b;
    }

    @Override
    public String toString() {
        String s = "";
		s = getName() + " (default-port=" + defaultValue + ")";
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

    public void copyFrom(DomainAttribute newValue) {
        this.fillers = newValue.fillers.stream().collect(Collectors.toList());
        this.defaultValue = newValue.getDefaultValue();
        this.isMutable = newValue.isMutable();
		this.isSystem = newValue.isSystem();
    }

    public static DomainAttribute numericalDomainAttribute(String name, Number defaultValue) {
       return new DomainAttribute(name
                , AttributeType.NUMERICAL
                , String.valueOf(defaultValue));
    }

    public static DomainAttribute intervalDomainAttribute(String name, Number from, Number to) {

        DomainAttribute a =  new DomainAttribute(name, AttributeType.INTERVAL, String.valueOf(from));
        a.addFiller(String.valueOf(from));
        a.addFiller(String.valueOf(to));
        return a;
    }

    public static DomainAttribute stringDomainAttribute(String name, String defaultValue) {
        return new DomainAttribute(name
                , AttributeType.STRING
                , defaultValue);
    }

    public static DomainAttribute setDomainAttribute(String name, String defaultValue, String ... fillers) {

        DomainAttribute a =  new DomainAttribute(name, AttributeType.SET, String.valueOf(defaultValue));
        a.fillers = Arrays.asList(fillers);
        return a;
    }

}
