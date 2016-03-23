package de.s2.gsim.objects;

import java.io.Serializable;

import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * ObjectClass is a Frame for objects of any kind in a simulation. In particular, an agent is also an object.
 * 
 * @author stephan
 *
 */
public interface ObjectClass extends Serializable {

    /**
     * Add an attribute the object class.
     * 
     * @param list listname where the attribute is to be stored
     * @param attribute the attribute
     */
    void addAttribute(String list, DomainAttribute attribute);

    /**
     * Destroys the object class by removing it from the environment it lives in. The destroy operation assures that no other object, object class or
     * instance deriving from it in a simulation or definition environment references it any further.
     */
    void destroy();

    /**
     * Get an attribute from the given list.
     * 
     * @param list the list name
     * @param attName the attribute name
     * @return the attribute
     */
    DomainAttribute getAttribute(String list, String attName);

    /**
     * Get all attribute list names,
     * 
     * @return the list names
     */
    String[] getAttributeListNames();

    /**
     * Get all attributes defined in the given list.
     * 
     * @param list the list name
     * @return the attributes
     */
    DomainAttribute[] getAttributes(String list);

    /**
     * Get the default value defined for a given attribute.
     * 
     * @param list the list where the attribute is located
     * @param attName the attribute name
     * @return the default value
     */
    String getDefaultValue(String list, String attName);

    /**
     * Get the name of the object class.
     * 
     * @return the name
     */
    String getName();

    /**
     * Determines whether the given attribute is defined in this object class or in a parent class.
     * 
     * @param list the list where the attribute is located in
     * @param attName the attribute name
     * @return true if defined in the current class, false if defined further up in the inheritance hierarchy
     */
    boolean isDeclaredAttribute(String list, String attName);

    /**
     * Resolves a path-string (each level is seperated by a '/') into the object it references.
     * 
     * @param path String a path leading to a list, an object, or an attribute
     * @return Object an Attribute if the name specified an attribute; otherwise on object
     */
    Object resolveName(String path);

    /**
     * Updates or creates a attribute.
     * 
     * @param list the list where the attribute is located in
     * @param attribute the attribute
     */
    public void setAttribute(String list, DomainAttribute attribute);

    /**
     * Updates the default value of the given attribute.
     * 
     * @param list the list where the attribute is located in
     * @param attName the attribute name
     * @param value the default value
     */
    void setDefaultAttributeValue(String list, String attName, String value);

}
