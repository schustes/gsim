package gsim.objects.impl;

import java.util.ArrayList;

import de.s2.gsim.objects.Condition;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.Rule;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ActionFrame;
import gsim.def.objects.behaviour.ConditionFrame;
import gsim.def.objects.behaviour.UserRuleFrame;

public class RuleClass implements Rule, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected BehaviourClass owner;

    protected UserRuleFrame real;

    public RuleClass(BehaviourClass owner, UserRuleFrame real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addOrSetCondition(Condition cond) throws GSimObjectException {
        ConditionFrame f = (ConditionFrame) ((UnitWrapper) cond).toUnit();
        real.removeCondition(f);
        real.addCondition(f);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetConsequent(de.s2.gsim.objects.Action cons) throws GSimObjectException {
        real.removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        real.addConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        owner.addOrSetRule(this);
    }

    @Override
    public Condition createCondition(String paramName, String op, String val) throws GSimObjectException {
        ConditionFrame c = real.createCondition(paramName, op, val);
        real.addCondition(c);
        return new ConditionClass(this, c);
    }

    @Override
    public Condition[] getConditions() {

        ConditionFrame[] c = real.getConditions();
        ConditionClass[] ret = new ConditionClass[c.length];
        for (int i = 0; i < c.length; i++) {
            ret[i] = new ConditionClass(this, c[i]);
        }
        return ret;
    }

    @Override
    public de.s2.gsim.objects.Action getConsequent(String name) {
        ActionFrame[] c = real.getConsequences();
        for (int i = 0; i < c.length; i++) {
            if (c[i].getTypeName().equals(name)) {
                return new ActionClass(this, c[i]);
            }
        }
        return null;
    }

    @Override
    public de.s2.gsim.objects.Action[] getConsequents() {
        ArrayList<de.s2.gsim.objects.Action> list = new ArrayList<>();

        ActionFrame[] c = real.getConsequences();
        for (int i = 0; i < c.length; i++) {
            if (!c[i].getTypeName().startsWith("{")) {
                list.add(new ActionClass(this, c[i]));
            }
        }

        de.s2.gsim.objects.Action[] ret = new ActionClass[list.size()];
        list.toArray(ret);
        return ret;
    }

    @Override
    public String getName() throws GSimObjectException {
        return real.getTypeName();
    }

    @Override
    public boolean isActivated() {
        return real.isActivated();
    }

    @Override
    public void removeCondition(Condition cond) throws GSimObjectException {
        real.removeCondition((ConditionFrame) ((UnitWrapper) cond).toUnit());
        owner.addOrSetRule(this);
    }

    @Override
    public void removeConsequent(de.s2.gsim.objects.Action cons) throws GSimObjectException {
        real.removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
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
