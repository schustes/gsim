package de.s2.gsim.environment;

import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

/**
 * A userrule is a OR-connected rule with AND connected conditions. For example, the user specifies if a and b then c or d: This gets translated into
 * two rules if a and b then c and if a and b then d. WHich rule is used is solved either by conflict resolution or success (preferred).
 */
public class UserRule extends Instance {

    static final long serialVersionUID = 3338801064775642635L;

    public UserRule(Frame parent, String name) {
        super(name, parent);
        DomainAttribute a = parent.getAttribute("state-var");
        SetAttribute set = (SetAttribute) this.getAttribute("state-var");
        if (a != null && set != null) {
            for (int i = 0; i < a.getFillers().length; i++) {
                set.addEntry(a.getFillers()[i]);
            }
            this.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, set);
        }
    }

    public UserRule(Instance inst) {
        super(inst);
        if (this.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "activated") == null) {
            DomainAttribute a = new DomainAttribute("activated", AttributeType.STRING);
            a.setDefault("true");
            getDefinition().addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);

            StringAttribute sa = new StringAttribute("activated", "true");
            this.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, sa);
        }
    }

    public void addCondition(ConditionDef cond) {
        super.addChildInstance(UserRuleFrame.INST_LIST_COND, cond);
    }

    public void addConsequence(ActionDef cons) {
        super.addChildInstance(UserRuleFrame.INST_LIST_CONS, cons);
    }

    public ConditionDef createCondition(String var, String op, String val) {
        ConditionDef f = new ConditionDef(var, op, val);
        return f;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserRule)) {
            return false;
        }

        UserRule other = (UserRule) o;
        return other.getName().equals(getName());

    }

    public boolean equals1(Object o) {

        if (!(o instanceof UserRule)) {
            return false;
        }

        UserRule other = (UserRule) o;

        ConditionDef[] otherConditions = other.getConditions();
        ConditionDef[] myConditions = getConditions();

        for (int i = 0; i < otherConditions.length; i++) {
            boolean b = false;
            for (int j = 0; j < myConditions.length; j++) {
                if (otherConditions[i].equals(myConditions[j])) {
                    b = true;
                }
            }
            if (!b) {
                return false;
            }
        }

        // all conditions were equal if we come to this point

        ActionDef[] otherActions = other.getConsequences();
        ActionDef[] myActions = getConsequences();

        for (int i = 0; i < otherActions.length; i++) {
            boolean b = false;
            for (int j = 0; j < myActions.length; j++) {
                if (otherActions[i].equals(myActions[j])) {
                    b = true;
                }
            }
            if (!b) {
                return false;
            }
        }

        // all elements of the two rules were equal

        return true;
    }

    public ConditionDef[] getConditions() {
        Instance[] inst = getChildInstances(UserRuleFrame.INST_LIST_COND);

        if (inst == null) {
            return new ConditionDef[0];
        }

        ConditionDef[] cond = new ConditionDef[inst.length];

        for (int i = 0; i < cond.length; i++) {
            cond[i] = new ConditionDef(inst[i]);
        }
        return cond;
    }

    public ActionDef[] getConsequences() {
        Instance[] inst = getChildInstances(UserRuleFrame.INST_LIST_CONS);
        ActionDef[] cons = new ActionDef[inst.length];
        for (int i = 0; i < cons.length; i++) {
            cons[i] = new ActionDef(inst[i]);
        }
        return cons;
    }

    public ActionDef getConsequent(String name) {
        Instance[] inst = getChildInstances(UserRuleFrame.INST_LIST_CONS);
        for (int i = 0; i < inst.length; i++) {
            if (inst[i].getName().equals(name)) {
                return new ActionDef(inst[i]);
            }
        }
        return null;
    }

    public String getUpdateLag() {
        StringAttribute a = (StringAttribute) this.getAttribute(ActionFrame.ATTR_LIST_ATTRS, "update-lag");
        return a.getValue();
    }

    public String getUpdateSpan() {
        StringAttribute a = (StringAttribute) this.getAttribute(ActionFrame.ATTR_LIST_ATTRS, "update-span");
        if (a == null) {
            return null;
        }
        return a.getValue();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public boolean isActivated() {
        Attribute a = this.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "activated");
        Boolean b = Boolean.valueOf(a.toValueString());
        return b.booleanValue();
    }

    public void removeCondition(ConditionDef cond) {
        super.removeChildInstance(UserRuleFrame.INST_LIST_COND, cond.getName());
    }

    public void removeConsequence(ActionDef cons) {
        super.removeChildInstance(UserRuleFrame.INST_LIST_CONS, cons.getName());
    }

    public void setActivated(boolean b) {
        StringAttribute a = new StringAttribute("activated", String.valueOf(b));
        this.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setCondition(ConditionDef cond) {
        super.setChildInstance(UserRuleFrame.INST_LIST_COND, cond);
    }

    public void setConsequence(ActionDef cons) {
        super.setChildInstance(UserRuleFrame.INST_LIST_CONS, cons);
    }

    public void setUpdateLag(String s) {
        this.setAttribute(new StringAttribute("update-lag", s));
    }

    public void setUpdateSpan(String s) {
        this.setAttribute(new StringAttribute("update-span", s));
    }

}
