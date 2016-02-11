package gsim.objects.impl;

import java.util.ArrayList;

import de.s2.gsim.objects.ActionIF;
import de.s2.gsim.objects.ConditionIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.RuleIF;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ActionFrame;
import gsim.def.objects.behaviour.ConditionFrame;
import gsim.def.objects.behaviour.UserRuleFrame;

public class RuleClass implements RuleIF, UnitWrapper {

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
    public void addOrSetCondition(ConditionIF cond) throws GSimObjectException {
        ConditionFrame f = (ConditionFrame) ((UnitWrapper) cond).toUnit();
        real.removeCondition(f);
        real.addCondition(f);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetConsequent(ActionIF cons) throws GSimObjectException {
        real.removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        real.addConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        owner.addOrSetRule(this);
    }

    @Override
    public ConditionIF createCondition(String paramName, String op, String val) throws GSimObjectException {
        ConditionFrame c = real.createCondition(paramName, op, val);
        real.addCondition(c);
        return new ConditionClass(this, c);
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
        ArrayList<ActionIF> list = new ArrayList<ActionIF>();

        ActionFrame[] c = real.getConsequences();
        for (int i = 0; i < c.length; i++) {
            if (!c[i].getTypeName().startsWith("{")) {
                list.add(new ActionClass(this, c[i]));
            }
        }

        ActionIF[] ret = new ActionClass[list.size()];
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
    public void removeCondition(ConditionIF cond) throws GSimObjectException {
        real.removeCondition((ConditionFrame) ((UnitWrapper) cond).toUnit());
        owner.addOrSetRule(this);
    }

    @Override
    public void removeConsequent(ActionIF cons) throws GSimObjectException {
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
