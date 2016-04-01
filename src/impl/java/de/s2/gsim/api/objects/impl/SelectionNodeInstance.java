package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.environment.UserRule;
import de.s2.gsim.objects.Condition;
import de.s2.gsim.objects.SelectionNode;

public class SelectionNodeInstance extends RuleInstance implements SelectionNode {

    private RLActionNodeInstance owner;

    private UserRule real;

    public SelectionNodeInstance(RLActionNodeInstance owner, UserRule real) {
        super(null, null);
        this.owner = owner;
        this.real = real;
    }

    @Override
    public void addNodeRefWithCondition(String formattedString, String op, String val) throws GSimException {
        try {
            String[] s0 = formattedString.split("\\$");
            String actionRef = s0[1];
            String objectRef = s0[2].substring(0, s0[2].indexOf("::"));
            String attPath = s0[2].substring(s0[2].indexOf("::") + 2);

            de.s2.gsim.objects.Action action = owner.getConsequent(actionRef);
            if (action == null) {
                throw new GSimException(
                        "Action-node reference " + actionRef + " references an action not defined in" + " parent-node " + owner.getName());
            }

            addOrSetConsequent(action);

            Condition condition = createCondition(objectRef + "::" + attPath, op, val);
            addOrSetCondition(condition);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public void addNodeRefWithCondition(String actionRef, String objectRef, String relativeAttPath, String op, String val) throws GSimException {
        try {

            de.s2.gsim.objects.Action action = owner.getConsequent(actionRef);
            if (action == null) {
                throw new GSimException(
                        "Action-node reference " + actionRef + " references an action not defined in" + " parent-node " + owner.getName());
            }

            addOrSetConsequent(action);

            Condition condition = createCondition(objectRef + "::" + relativeAttPath, op, val);
            addOrSetCondition(condition);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public void addNodeRefWithoutCondition(String actionRef, String objectRef, String relativeAttPath) throws GSimException {
        try {

            de.s2.gsim.objects.Action action = owner.getConsequent(actionRef);
            if (action == null) {
                throw new GSimException(
                        "Action-node reference " + actionRef + " references an action not defined in" + " parent-node " + owner.getName());
            }

            addOrSetConsequent(action);

        } catch (Exception e) {
            throw new GSimException(e);
        }

    }

    @Override
    public void addOrSetCondition(Condition cond) {
        ConditionDef f = (ConditionDef) ((UnitWrapper) cond).toUnit();
        real.removeCondition(f);
        real.addCondition(f);
    }

    @Override
    public void addOrSetConsequent(de.s2.gsim.objects.Action cons) throws GSimException {
        real.removeConsequence((ActionDef) ((UnitWrapper) cons).toUnit());
        real.addConsequence((ActionDef) ((UnitWrapper) cons).toUnit());
        owner.addOrSetSelectionNode(this);
    }

    @Override
    public Condition[] getConditions() {
        ConditionDef[] c = real.getConditions();
        ConditionInstance[] ret = new ConditionInstance[c.length];
        for (int i = 0; i < c.length; i++) {
            ret[i] = new ConditionInstance(this, c[i]);
        }
        return ret;
    }

    @Override
    public de.s2.gsim.objects.Action getConsequent(String name) {
        ActionDef[] c = real.getConsequents();
        for (int i = 0; i < c.length; i++) {
            if (c[i].getName().equals(name)) {
                return new ActionInstance(this, c[i]);
            }
        }
        return null;
    }

    @Override
    public de.s2.gsim.objects.Action[] getConsequents() {
        ActionDef[] c = real.getConsequents();
        ActionInstance[] ret = new ActionInstance[c.length];
        for (int i = 0; i < c.length; i++) {
            ret[i] = new ActionInstance(this, c[i]);
        }
        return ret;
    }

    @Override
    public String[] getNodeRefs() throws GSimException {
        String[] ret = new String[getConditions().length];
        int i = 0;
        for (Condition c : getConditions()) {
            String refString = c.getParameterName();
            ret[i] = refString;
            i++;
        }
        return ret;
    }

    @Override
    public de.s2.gsim.objects.Action getReferencedAction(String name) throws GSimException {
        return getConsequent(name);
    }

    @Override
    public de.s2.gsim.objects.Action[] getReferencedActions() throws GSimException {
        return getConsequents();
    }

    @Override
    public String[] getReferencedParameters(String actionRef) throws GSimException {
        ArrayList<String> list = new ArrayList<String>();
        de.s2.gsim.objects.Action a = getConsequent(actionRef);
        if (a.hasObjectParameter()) {
            String[] s = a.getObjectClassParams();
            for (String u : s) {
                list.add(u);
            }
        }

        String[] ret = new String[list.size()];
        list.toArray(ret);
        return ret;
    }

    @Override
    public boolean isActivated() {
        return real.isActivated();
    }

    @Override
    public void removeCondition(Condition cond) throws GSimException {
        real.removeCondition((ConditionDef) ((UnitWrapper) cond).toUnit());
        owner.addOrSetSelectionNode(this);
    }

    @Override
    public void removeConsequent(de.s2.gsim.objects.Action cons) throws GSimException {
        real.removeConsequence((ActionDef) ((UnitWrapper) cons).toUnit());
        owner.addOrSetSelectionNode(this);
    }

    public void removeNodeRef(String formattedString, String op, String val) throws GSimException {

        try {
            String[] s0 = formattedString.split("\\$");
            String actionRef = s0[1];
            String objectRef = s0[2].substring(0, s0[2].indexOf("::"));
            String attPath = s0[2].substring(s0[2].indexOf("::") + 1);

            de.s2.gsim.objects.Action action = getConsequent(actionRef);
            removeConsequent(action);
            Condition condition = createCondition(objectRef + "::" + attPath, op, val);
            removeCondition(condition);

        } catch (Exception e) {
            throw new GSimException(e);
        }
    }

    @Override
    public void setActivated(boolean b) throws GSimException {
        real.setActivated(b);
        owner.addOrSetSelectionNode(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
