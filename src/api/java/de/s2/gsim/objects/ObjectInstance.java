package de.s2.gsim.objects;

import java.io.Serializable;

import de.s2.gsim.objects.attribute.Attribute;

public interface ObjectInstance extends Serializable {

    public ObjectInstance copy();

    public void destroy() throws GSimObjectException;

    public Attribute getAttribute(String attName) throws GSimObjectException;

    public Attribute getAttribute(String list, String attName) throws GSimObjectException;

    public String[] getAttributeListNames() throws GSimObjectException;

    public Attribute[] getAttributes(String list) throws GSimObjectException;

    public double getIntervalAttributeFrom(String list, String attName) throws GSimObjectException;

    public double getIntervalAttributeTo(String list, String attName) throws GSimObjectException;

    public String getName() throws GSimObjectException;

    public double getNumericalAttribute(String list, String attName) throws GSimObjectException;

    public String[] getSetAttributeValues(String list, String attName) throws GSimObjectException;

    public String getStringAttribute(String list, String attName) throws GSimObjectException;

    public boolean inheritsFrom(String agentclassName);

    public Object resolveName(String path) throws GSimObjectException;

    public void setAttribute(String list, Attribute a) throws GSimObjectException;

    public void setIntervalAttributeValue(String list, String name, double from, double to) throws GSimObjectException;

    public void setNumericalAttributeValue(String list, String name, double value) throws GSimObjectException;

    public void setSetAttributeValues(String list, String name, String... values) throws GSimObjectException;

    public void setStringAttributeValue(String list, String name, String value) throws GSimObjectException;

}
