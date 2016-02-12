package de.s2.gsim.objects;

public interface RLActionNode extends Rule {

    public void addOrSetExpansion(Expansion cond) throws GSimObjectException;

    public void addOrSetSelectionNode(SelectionNode sc) throws GSimObjectException;

    public Condition createEvaluator(String paramName, String op, String val) throws GSimObjectException;

    public Expansion createExpansion(String param, String min, String max) throws GSimObjectException;

    public Expansion createExpansion(String param, String[] fillers) throws GSimObjectException;

    public SelectionNode createSelectionNode(String name) throws GSimObjectException;

    public double getDiscount() throws GSimObjectException;

    // public String getUpdateLag() throws GSimObjectException;

    // public void setUpdateLag(String s) throws GSimObjectException;

    public Condition getEvaluator() throws GSimObjectException;

    public String getExecutionRestrictionInterval() throws GSimObjectException;

    public Expansion[] getExpansions() throws GSimObjectException;

    public double getGlobalAverageStepSize() throws GSimObjectException;

    public Method getMethod() throws GSimObjectException;

    public POLICY getPolicy() throws GSimObjectException;

    public SelectionNode getSelectionNode(String name) throws GSimObjectException;

    public SelectionNode[] getSelectionNodes() throws GSimObjectException;

    public void removeExpansion(Expansion cond) throws GSimObjectException;

    public void removeSelectionNode(SelectionNode sc) throws GSimObjectException;

    public void setDiscount(double d) throws GSimObjectException;

    public void setEvaluator(Condition f) throws GSimObjectException;

    public void setExecutionRestrictionInterval(String t) throws GSimObjectException;

    public void setGlobalAverageStepSize(double d) throws GSimObjectException;

    public void setMethod(Method p) throws GSimObjectException;

    public void setPolicy(POLICY p) throws GSimObjectException;

    public enum Method {
        NULL, Q
    }

    public enum POLICY {
        COMPARISON, SOFTMAX
    }
}
