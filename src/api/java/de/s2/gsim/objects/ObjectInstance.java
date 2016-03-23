package de.s2.gsim.objects;

import java.io.Serializable;

import de.s2.gsim.core.GSimException;
import de.s2.gsim.objects.attribute.Attribute;

/**
 * ObjectInstance represents an instanciation of an {@link ObjectClass}.
 * 
 * @author stephan
 *
 */
public interface ObjectInstance extends Serializable {

    public ObjectInstance copy();

    public void destroy() throws GSimException;

    public Attribute getAttribute(String attName) throws GSimException;

    public Attribute getAttribute(String list, String attName) throws GSimException;

    public String[] getAttributeListNames() throws GSimException;

    public Attribute[] getAttributes(String list) throws GSimException;

    public double getIntervalAttributeFrom(String list, String attName) throws GSimException;

    public double getIntervalAttributeTo(String list, String attName) throws GSimException;

    public String getName() throws GSimException;

    public double getNumericalAttribute(String list, String attName) throws GSimException;

    public String[] getSetAttributeValues(String list, String attName) throws GSimException;

    public String getStringAttribute(String list, String attName) throws GSimException;

    public boolean inheritsFrom(String agentclassName);

    public Object resolveName(String path) throws GSimException;

    public void setAttribute(String list, Attribute a) throws GSimException;

    public void setIntervalAttributeValue(String list, String name, double from, double to) throws GSimException;

    public void setNumericalAttributeValue(String list, String name, double value) throws GSimException;

    public void setSetAttributeValues(String list, String name, String... values) throws GSimException;

    public void setStringAttributeValue(String list, String name, String value) throws GSimException;

}
