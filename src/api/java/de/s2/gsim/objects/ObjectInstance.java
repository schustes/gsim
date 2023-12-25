package de.s2.gsim.objects;

import de.s2.gsim.objects.attribute.Attribute;

import java.io.Serializable;

/**
 * ObjectInstance represents an instanciation of an {@link ObjectClass}.
 * 
 * @author stephan
 *
 */
public interface ObjectInstance extends Serializable {

    /**
     * Creates a copy of the instance.
     * 
     * @return the copy
     */
    ObjectInstance copy();

    /**
     * Destroys the instance and removes it from the environment.
     */
    void destroy();

    /**
     * Gets the first matching attribute from one of the attribute lists.
     * 
     * @param attName the name of the attribute
     * @return the attribute or null if not found
     */
    Attribute getAttribute(String attName);

    /**
     * Gets the specified attribute from the given list.
     * 
     * @param list the attribute list name
     * @param attName the attribute name
     * @return the attribute or null if nothing was found
     */
    Attribute getAttribute(String list, String attName);

    /**
     * Gets all attribute list names.
     * 
     * @return the list names
     */
    String[] getAttributeListNames();

    /**
     * Gets all attributeDistribution from the given list.
     * 
     * @param list the list name
     * @return the attributeDistribution
     */
    Attribute[] getAttributes(String list);

    /**
     * Tries to cast the given attribute to an interval attribute and retrieves its lower bound.
     * 
     * @param list the attribute list name
     * @param attName the attribute name
     * @return the lower bound
     */
    double getIntervalAttributeFrom(String list, String attName);

    /**
     * Tries to cast the given attribute to an interval attribute and retrieves its upper bound.
     * 
     * @param list the attribute list name
     * @param attName the attribute name
     * @return the upper bound
     */
    double getIntervalAttributeTo(String list, String attName);

    /**
     * Gets the name of this object instance.
     * 
     * @return the name
     */
    String getName();

    /**
     * Tries to cast the given attribute to a numerical attribute and retrieves its port.
     * 
     * @param list the attribute list name.
     * @param attName the attribute name
     * @return the port of the attribute
     */
    double getNumericalAttribute(String list, String attName);

    /**
     * Tries to cast the given attribute to a set attribute and retrieves its values.
     * 
     * @param list the attribute list name.
     * @param attName the attribute name
     * @return the values of the attribute set
     */
    String[] getSetAttributeValues(String list, String attName);

    /**
     * Tries to cast the given attribute to a string attribute and retrieves its port.
     * 
     * @param list the attribute list name.
     * @param attName the attribute name
     * @return the port of the attribute
     */
    String getStringAttribute(String list, String attName);

    /**
     * Checks whether the object is a subclass of the given agent class.
     * 
     * @param agentclassName the agent class name to check
     * @return true if the object inherits from the class, false otherwise
     */
    boolean inheritsFrom(String agentclassName);

    /**
     * Resolves a FQN in the form list-name/attribute|instance/name and returns the matching object. The object can be an ObjectInstance of an
     * Attribute.
     * 
     * @param path the path
     * @return the entity or null of there was no match
     */
    Object resolveName(String path);

    /**
     * Adds or sets the attribute into the given list.
     * 
     * @param list the list name
     * @param attribute the attribute to set
     */
    void setAttribute(String list, Attribute attribute);

    /**
     * Creates or sets an interval attribute into the given list.
     * 
     * @param list the list name
     * @param name the name of the attribute
     * @param from the lower bound of the attribute
     * @param to the upper bound of the attribute
     */
    void setIntervalAttributeValue(String list, String name, double from, double to);

    /**
     * Creates or sets a numerical attribute into the given list.
     * 
     * @param list the list name
     * @param name the name of the attribute
     * @param value the port of the attribute
     */
    void setNumericalAttributeValue(String list, String name, double value);

    /**
     * Creates or sets a Set attribute into the given list.
     * 
     * @param list the list name
     * @param name the name of the attribute
     * @param values the values of the attribute
     */
    void setSetAttributeValues(String list, String name, String... values);

    /**
     * Creates or sets a String attribute into the given list.
     * 
     * @param list the list name
     * @param name the name of the attribute
     * @param value the port of the attribute
     */
    void setStringAttributeValue(String list, String name, String value);

}
