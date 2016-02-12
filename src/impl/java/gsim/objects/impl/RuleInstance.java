package gsim.objects.impl;

import de.s2.gsim.objects.Condition;
import de.s2.gsim.objects.Expansion;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.Rule;
import gsim.def.objects.Instance;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ActionDef;
import gsim.def.objects.behaviour.ConditionDef;
import gsim.def.objects.behaviour.ExpansionDef;
import gsim.def.objects.behaviour.RLRuleFrame;
import gsim.def.objects.behaviour.UserRule;

public class RuleInstance implements Rule, UnitWrapper {

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
    public void addOrSetCondition(Condition cond) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        ConditionDef c = new ConditionDef(inst);
        real.setCondition(c);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetConsequent(de.s2.gsim.objects.Action cons) throws GSimObjectException {
        ActionDef a = new ActionDef((Instance) ((UnitWrapper) cons).toUnit());
        real.addConsequence(a);
        owner.addOrSetRule(this);
    }

    public void addOrSetExpansion(Expansion cond) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        ExpansionDef c = new ExpansionDef(inst);
        real.addChildInstance(RLRuleFrame.INST_LIST_EXP, c);
        owner.addOrSetRule(this);
    }

    @Override
    public Condition createCondition(String paramName, String op, String val) throws GSimObjectException {
        ConditionDef c = real.createCondition(paramName, op, val);
        real.addCondition(c);
        return new ConditionInstance(this, c);
    }

    @Override
    public Condition[] getConditions() {
        ConditionInstance[] ret = new ConditionInstance[real.getConditions().length];
        ConditionDef[] c = real.getConditions();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new ConditionInstance(this, c[i]);
        }
        return ret;
    }

    @Override
    public de.s2.gsim.objects.Action getConsequent(String name) {
        ActionDef a = real.getConsequent(name);
        if (a != null) {
            return new ActionInstance(this, a);
        } else {
            return null;
        }
    }

    @Override
    public de.s2.gsim.objects.Action[] getConsequents() {
        ActionInstance[] ret = new ActionInstance[real.getConditions().length];
        ActionDef[] c = real.getConsequences();
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
    public void removeCondition(Condition cond) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        ConditionDef c = new ConditionDef(inst);
        real.removeCondition(c);
        owner.addOrSetRule(this);
    }

    @Override
    public void removeConsequent(de.s2.gsim.objects.Action cons) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cons).toUnit();
        ActionDef c = new ActionDef(inst);
        real.removeConsequence(c);
        owner.addOrSetRule(this);
    }

    public void removeExpansion(Expansion cond) throws GSimObjectException {
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
