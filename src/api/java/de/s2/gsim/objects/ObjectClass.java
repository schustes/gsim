package de.s2.gsim.objects;

import java.io.Serializable;

import de.s2.gsim.core.GSimException;
import de.s2.gsim.objects.attribute.DomainAttribute;

public interface ObjectClass extends Serializable {

    public void addAttribute(String list, DomainAttribute a) throws GSimException;

    public void destroy() throws GSimException;

    public DomainAttribute getAttribute(String list, String attName) throws GSimException;

    public String[] getAttributeListNames() throws GSimException;

    public DomainAttribute[] getAttributes(String list) throws GSimException;

    public String getDefaultValue(String list, String attName) throws GSimException;

    public String getName() throws GSimException;

    public boolean isDeclaredAttribute(String list, String attName) throws GSimException;

    /**
     * Resolves a path-string (each level is seperated by a '/') into the object it references.
     * 
     * @param path String a path leading to a list, an object, or an attribute
     * @return Object an Attribute if the name specified an attribute; a
     * @link java.util.List of attributes if an attribute list was specified; or null if the path did not specify a valid attribute/list.
     * @throws GSimException
     */
    public Object resolveName(String path) throws GSimException;

    public void setAttribute(String list, DomainAttribute a) throws GSimException;

    public void setDefaultAttributeValue(String list, String attName, String value) throws GSimException;

}
