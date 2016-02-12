package de.s2.gsim.objects;

import java.io.Serializable;

public interface Rule extends Serializable {

    public void addOrSetCondition(Condition cond) throws GSimObjectException;

    public void addOrSetConsequent(Action cons) throws GSimObjectException;

    public Condition createCondition(String paramName, String op, String val) throws GSimObjectException;

    public Condition[] getConditions() throws GSimObjectException;

    public Action getConsequent(String actionName) throws GSimObjectException;

    public Action[] getConsequents() throws GSimObjectException;

    public String getName() throws GSimObjectException;

    public boolean isActivated() throws GSimObjectException;

    public void removeCondition(Condition cond) throws GSimObjectException;

    public void removeConsequent(Action cons) throws GSimObjectException;

    public void setActivated(boolean b) throws GSimObjectException;

}
