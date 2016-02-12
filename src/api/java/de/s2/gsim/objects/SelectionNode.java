package de.s2.gsim.objects;

public interface SelectionNode extends Rule {

    public void addNodeRef(String formattedString, String op, String val) throws GSimObjectException;

    public void addNodeRef(String actionRef, String objectRef, String relativeAttPath, String op, String val) throws GSimObjectException;

    public String[] getNodeRefs() throws GSimObjectException;

    public Action getReferencedAction(String actionName) throws GSimObjectException;

    // public String[] getReferencedParameters() throws GSimObjectException;

    public Action[] getReferencedActions() throws GSimObjectException;

    public String[] getReferencedParameters(String actionRef) throws GSimObjectException;

}
