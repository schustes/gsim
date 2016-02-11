package de.s2.gsim.objects;

public interface SelectionNodeIF extends RuleIF {

    public void addNodeRef(String formattedString, String op, String val) throws GSimObjectException;

    public void addNodeRef(String actionRef, String objectRef, String relativeAttPath, String op, String val) throws GSimObjectException;

    public String[] getNodeRefs() throws GSimObjectException;

    public ActionIF getReferencedAction(String actionName) throws GSimObjectException;

    // public String[] getReferencedParameters() throws GSimObjectException;

    public ActionIF[] getReferencedActions() throws GSimObjectException;

    public String[] getReferencedParameters(String actionRef) throws GSimObjectException;

}
