package de.s2.gsim.objects;

import java.io.Serializable;

import de.s2.gsim.objects.attribute.DomainAttribute;

public interface ObjectClassIF extends Serializable {

    public void addAttribute(String list, DomainAttribute a) throws GSimObjectException;

    public void destroy() throws GSimObjectException;

    public DomainAttribute getAttribute(String list, String attName) throws GSimObjectException;

    public String[] getAttributeListNames() throws GSimObjectException;

    public DomainAttribute[] getAttributes(String list) throws GSimObjectException;

    public String getDefaultValue(String list, String attName) throws GSimObjectException;

    public String getName() throws GSimObjectException;

    public boolean isDeclaredAttribute(String list, String attName) throws GSimObjectException;

    /**
     * Resolves a path-string (each level is seperated by a '/') into the object it references.
     * 
     * @param path String a path leading to a list, an object, or an attribute
     * @return Object an Attribute if the name specified an attribute; a
     * @link java.util.List of attributes if an attribute list was specified; or null if the path did not specify a valid attribute/list.
     * @throws GSimObjectException
     */
    public Object resolveName(String path) throws GSimObjectException;

    public void setAttribute(String list, DomainAttribute a) throws GSimObjectException;

    public void setDefaultAttributeValue(String list, String attName, String value) throws GSimObjectException;

}
