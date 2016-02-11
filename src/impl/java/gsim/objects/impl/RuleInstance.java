package gsim.objects.impl;

import de.s2.gsim.objects.ActionIF;
import de.s2.gsim.objects.ConditionIF;
import de.s2.gsim.objects.ExpansionIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.RuleIF;
import gsim.def.objects.Instance;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.Action;
import gsim.def.objects.behaviour.Condition;
import gsim.def.objects.behaviour.Expansion;
import gsim.def.objects.behaviour.RLRuleFrame;
import gsim.def.objects.behaviour.UserRule;

public class RuleInstance implements RuleIF, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected BehaviourInstance owner;

    protected UserRule real;

    public RuleInstance(BehaviourInstance owner, UserRule real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addOrSetCondition(ConditionIF cond) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        Condition c = new Condition(inst);
        real.setCondition(c);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetConsequent(ActionIF cons) throws GSimObjectException {
        Action a = new Action((Instance) ((UnitWrapper) cons).toUnit());
        real.addConsequence(a);
        owner.addOrSetRule(this);
    }

    public void addOrSetExpansion(ExpansionIF cond) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        Expansion c = new Expansion(inst);
        real.addChildInstance(RLRuleFrame.INST_LIST_EXP, c);
        owner.addOrSetRule(this);
    }

    @Override
    public ConditionIF createCondition(String paramName, String op, String val) throws GSimObjectException {
        Condition c = real.createCondition(paramName, op, val);
        real.addCondition(c);
        return new ConditionInstance(this, c);
    }

    @Override
    public ConditionIF[] getConditions() {
        ConditionInstance[] ret = new ConditionInstance[real.getConditions().length];
        Condition[] c = real.getConditions();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new ConditionInstance(this, c[i]);
        }
        return ret;
    }

    @Override
    public ActionIF getConsequent(String name) {
        Action a = real.getConsequent(name);
        if (a != null) {
            return new ActionInstance(this, a);
        } else {
            return null;
        }
    }

    @Override
    public ActionIF[] getConsequents() {
        ActionInstance[] ret = new ActionInstance[real.getConditions().length];
        Action[] c = real.getConsequences();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new ActionInstance(this, c[i]);
        }
        return ret;
    }

    @Override
    public String getName() throws GSimObjectException {
        return real.getName();
    }

    @Override
    public boolean isActivated() {
        return real.isActivated();
    }

    @Override
    public void removeCondition(ConditionIF cond) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        Condition c = new Condition(inst);
        real.removeCondition(c);
        owner.addOrSetRule(this);
    }

    @Override
    public void removeConsequent(ActionIF cons) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cons).toUnit();
        Action c = new Action(inst);
        real.removeConsequence(c);
        owner.addOrSetRule(this);
    }

    public void removeExpansion(ExpansionIF cond) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        real.removeChildInstance(RLRuleFrame.INST_LIST_EXP, inst.getName());
        owner.addOrSetRule(this);
    }

    @Override
    public void setActivated(boolean b) throws GSimObjectException {
        real.setActivated(b);
        owner.addOrSetRule(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
