package de.s2.gsim.objects;

import de.s2.gsim.core.GSimException;

public interface RLActionNode extends Rule {

    public void addOrSetExpansion(Expansion cond) throws GSimException;

    public void addOrSetSelectionNode(SelectionNode sc) throws GSimException;

    public Condition createEvaluator(String paramName, String op, String val) throws GSimException;

    public Expansion createExpansion(String param, String min, String max) throws GSimException;

    public Expansion createExpansion(String param, String[] fillers) throws GSimException;

    public SelectionNode createSelectionNode(String name) throws GSimException;

    public double getDiscount() throws GSimException;

    // public String getUpdateLag() throws GSimException;

    // public void setUpdateLag(String s) throws GSimException;

    public Condition getEvaluator() throws GSimException;

    public String getExecutionRestrictionInterval() throws GSimException;

    public Expansion[] getExpansions() throws GSimException;

    public double getGlobalAverageStepSize() throws GSimException;

    public Method getMethod() throws GSimException;

    public POLICY getPolicy() throws GSimException;

    public SelectionNode getSelectionNode(String name) throws GSimException;

    public SelectionNode[] getSelectionNodes() throws GSimException;

    public void removeExpansion(Expansion cond) throws GSimException;

    public void removeSelectionNode(SelectionNode sc) throws GSimException;

    public void setDiscount(double d) throws GSimException;

    public void setEvaluator(Condition f) throws GSimException;

    public void setExecutionRestrictionInterval(String t) throws GSimException;

    public void setGlobalAverageStepSize(double d) throws GSimException;

    public void setMethod(Method p) throws GSimException;

    public void setPolicy(POLICY p) throws GSimException;

    public enum Method {
        NULL, Q
    }

    public enum POLICY {
        COMPARISON, SOFTMAX
    }
}
