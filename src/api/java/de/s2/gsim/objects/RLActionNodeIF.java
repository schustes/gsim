package de.s2.gsim.objects;

public interface RLActionNodeIF extends RuleIF {

    public void addOrSetExpansion(ExpansionIF cond) throws GSimObjectException;

    public void addOrSetSelectionNode(SelectionNodeIF sc) throws GSimObjectException;

    public ConditionIF createEvaluator(String paramName, String op, String val) throws GSimObjectException;

    public ExpansionIF createExpansion(String param, String min, String max) throws GSimObjectException;

    public ExpansionIF createExpansion(String param, String[] fillers) throws GSimObjectException;

    public SelectionNodeIF createSelectionNode(String name) throws GSimObjectException;

    public double getDiscount() throws GSimObjectException;

    // public String getUpdateLag() throws GSimObjectException;

    // public void setUpdateLag(String s) throws GSimObjectException;

    public ConditionIF getEvaluator() throws GSimObjectException;

    public String getExecutionRestrictionInterval() throws GSimObjectException;

    public ExpansionIF[] getExpansions() throws GSimObjectException;

    public double getGlobalAverageStepSize() throws GSimObjectException;

    public Method getMethod() throws GSimObjectException;

    public POLICY getPolicy() throws GSimObjectException;

    public SelectionNodeIF getSelectionNode(String name) throws GSimObjectException;

    public SelectionNodeIF[] getSelectionNodes() throws GSimObjectException;

    public void removeExpansion(ExpansionIF cond) throws GSimObjectException;

    public void removeSelectionNode(SelectionNodeIF sc) throws GSimObjectException;

    public void setDiscount(double d) throws GSimObjectException;

    public void setEvaluator(ConditionIF f) throws GSimObjectException;

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
