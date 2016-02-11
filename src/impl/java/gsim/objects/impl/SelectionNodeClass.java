package gsim.objects.impl;

import java.util.ArrayList;

import de.s2.gsim.objects.ActionIF;
import de.s2.gsim.objects.ConditionIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.SelectionNodeIF;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ActionFrame;
import gsim.def.objects.behaviour.ConditionFrame;
import gsim.def.objects.behaviour.UserRuleFrame;

public class SelectionNodeClass extends RuleClass implements SelectionNodeIF {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private RLActionNodeClass owner;

    private UserRuleFrame real;

    public SelectionNodeClass(RLActionNodeClass owner, UserRuleFrame real) {
        super(null, real);
        this.owner = owner;
        this.real = real;
    }

    @Override
    public void addNodeRef(String formattedString, String op, String val) throws GSimObjectException {
        try {
            String[] s0 = formattedString.split("\\$");
            String actionRef = s0[1];
            String objectRef = s0[2].substring(0, s0[2].indexOf("::"));
            String attPath = s0[2].substring(s0[2].indexOf("::") + 2);

            ActionIF action = owner.getConsequent(actionRef);
            if (action == null) {
                throw new GSimObjectException(
                        "Action-node reference " + actionRef + " references an action not defined in" + " parent-node " + owner.getName());
            }

            addOrSetConsequent(action);

            ConditionIF condition = createCondition(objectRef + "::" + attPath, op, val);
            addOrSetCondition(condition);

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }

    }

    @Override
    public void addNodeRef(String actionRef, String objectRef, String relativeAttPath, String op, String val) throws GSimObjectException {
        ActionIF action = owner.getConsequent(actionRef);
        if (action == null) {
            throw new GSimObjectException(
                    "Action-node reference " + actionRef + " references an action not defined in" + " parent-node " + owner.getName());
        }

        addOrSetConsequent(action);
        String s = "$" + actionRef + "$" + objectRef + "::" + relativeAttPath;
        ConditionIF condition = createCondition(s, op, val);
        addOrSetCondition(condition);

    }

    @Override
    public void addOrSetCondition(ConditionIF cond) {
        ConditionFrame f = (ConditionFrame) ((UnitWrapper) cond).toUnit();
        real.removeCondition(f);
        real.addCondition(f);
    }

    @Override
    public void addOrSetConsequent(ActionIF cons) throws GSimObjectException {
        real.removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        real.addConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        owner.addOrSetSelectionNode(this);
    }

    @Override
    public ConditionIF[] getConditions() {
        ConditionFrame[] c = real.getConditions();
        ConditionClass[] ret = new ConditionClass[c.length];
        for (int i = 0; i < c.length; i++) {
            ret[i] = new ConditionClass(this, c[i]);
        }
        return ret;
    }

    @Override
    public ActionIF getConsequent(String name) {
        ActionFrame[] c = real.getConsequences();
        for (int i = 0; i < c.length; i++) {
            if (c[i].getTypeName().equals(name)) {
                return new ActionClass(this, c[i]);
            }
        }
        return null;
    }

    @Override
    public ActionIF[] getConsequents() {
        ArrayList<ActionClass> list = new ArrayList<ActionClass>();

        ActionFrame[] c = real.getConsequences();

        for (int i = 0; i < c.length; i++) {
            if (!c[i].getTypeName().startsWith("{")) {
                list.add(new ActionClass(this, c[i]));
            }
        }

        ActionClass[] ret = new ActionClass[list.size()];
        list.toArray(ret);
        return ret;
    }

    @Override
    public String getName() {
        return real.getTypeName();
    }

    @Override
    public String[] getNodeRefs() throws GSimObjectException {
        String[] ret = new String[getConditions().length];
        int i = 0;
        for (ConditionIF c : getConditions()) {
            String refString = c.getParameterName();
            ret[i] = refString;
            i++;
        }
        return ret;
    }

    @Override
    public ActionIF getReferencedAction(String name) throws GSimObjectException {
        return getConsequent(name);
    }

    @Override
    public ActionIF[] getReferencedActions() throws GSimObjectException {
        return getConsequents();
    }

    @Override
    public String[] getReferencedParameters(String actionRef) throws GSimObjectException {
        ArrayList<String> list = new ArrayList<String>();
        ActionIF a = getConsequent(actionRef);
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
    public void removeCondition(ConditionIF cond) throws GSimObjectException {
        real.removeCondition((ConditionFrame) ((UnitWrapper) cond).toUnit());
        owner.addOrSetSelectionNode(this);
    }

    @Override
    public void removeConsequent(ActionIF cons) throws GSimObjectException {
        real.removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        owner.addOrSetSelectionNode(this);
    }

    public void removeNodeRef(String formattedString, String op, String val) throws GSimObjectException {

        try {
            String[] s0 = formattedString.split("\\$");
            String actionRef = s0[1];
            String objectRef = s0[2].substring(0, s0[2].indexOf("::"));
            String attPath = s0[2].substring(s0[2].indexOf("::") + 1);

            ActionIF action = getConsequent(actionRef);
            removeConsequent(action);
            ConditionIF condition = createCondition(objectRef + "::" + attPath, op, val);
            removeCondition(condition);

        } catch (Exception e) {
            throw new GSimObjectException(e);
        }
    }

    @Override
    public void setActivated(boolean b) throws GSimObjectException {
        real.setActivated(b);
        owner.addOrSetSelectionNode(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
