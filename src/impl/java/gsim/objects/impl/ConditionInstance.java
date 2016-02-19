package gsim.objects.impl;

import de.s2.gsim.core.GSimException;
import de.s2.gsim.objects.Condition;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ConditionDef;

public class ConditionInstance implements Condition, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private RuleInstance owner;

    private ConditionDef real;

    public ConditionInstance(RuleInstance owner, ConditionDef real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public String getOperator() {
        return real.getOperator();
    }

    @Override
    public String getParameterName() {
        return real.getParameterName();
    }

    @Override
    public String getParameterValue() {
        return real.getParameterValue();
    }

    @Override
    public void setOperator(String str) throws GSimException {
        real.setOperator(str);
        owner.addOrSetCondition(this);
    }

    @Override
    public void setParameterName(String str) throws GSimException {
        real.setParameterName(str);
        owner.addOrSetCondition(this);
    }

    @Override
    public void setParameterValue(String str) throws GSimException {
        real.setParameterValue(str);
        owner.addOrSetCondition(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
