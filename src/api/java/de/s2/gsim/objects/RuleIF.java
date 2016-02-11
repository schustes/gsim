package de.s2.gsim.objects;

import java.io.Serializable;

public interface RuleIF extends Serializable {

    public void addOrSetCondition(ConditionIF cond) throws GSimObjectException;

    public void addOrSetConsequent(ActionIF cons) throws GSimObjectException;

    public ConditionIF createCondition(String paramName, String op, String val) throws GSimObjectException;

    public ConditionIF[] getConditions() throws GSimObjectException;

    public ActionIF getConsequent(String actionName) throws GSimObjectException;

    public ActionIF[] getConsequents() throws GSimObjectException;

    public String getName() throws GSimObjectException;

    public boolean isActivated() throws GSimObjectException;

    public void removeCondition(ConditionIF cond) throws GSimObjectException;

    public void removeConsequent(ActionIF cons) throws GSimObjectException;

    public void setActivated(boolean b) throws GSimObjectException;

}
