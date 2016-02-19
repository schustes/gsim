package de.s2.gsim.objects;

import java.io.Serializable;

import de.s2.gsim.core.GSimException;

public interface Rule extends Serializable {

    public void addOrSetCondition(Condition cond) throws GSimException;

    public void addOrSetConsequent(Action cons) throws GSimException;

    public Condition createCondition(String paramName, String op, String val) throws GSimException;

    public Condition[] getConditions() throws GSimException;

    public Action getConsequent(String actionName) throws GSimException;

    public Action[] getConsequents() throws GSimException;

    public String getName() throws GSimException;

    public boolean isActivated() throws GSimException;

    public void removeCondition(Condition cond) throws GSimException;

    public void removeConsequent(Action cons) throws GSimException;

    public void setActivated(boolean b) throws GSimException;

}
