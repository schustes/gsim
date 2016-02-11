package gsim.objects.impl;

import de.s2.gsim.objects.ConditionIF;
import de.s2.gsim.objects.GSimObjectException;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.Condition;

public class ConditionInstance implements ConditionIF, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private RuleInstance owner;

    private Condition real;

    public ConditionInstance(RuleInstance owner, Condition real) {
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
    public void setOperator(String str) throws GSimObjectException {
        real.setOperator(str);
        owner.addOrSetCondition(this);
    }

    @Override
    public void setParameterName(String str) throws GSimObjectException {
        real.setParameterName(str);
        owner.addOrSetCondition(this);
    }

    @Override
    public void setParameterValue(String str) throws GSimObjectException {
        real.setParameterValue(str);
        owner.addOrSetCondition(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
