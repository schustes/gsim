package de.s2.gsim.api.objects.impl;

import de.s2.gsim.GSimException;
import de.s2.gsim.objects.Condition;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ConditionDef;

public class EvaluatorInstance implements Condition, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private RLActionNodeInstance owner;

    private ConditionDef real;

    public EvaluatorInstance(RLActionNodeInstance owner, ConditionDef real) {
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
        owner.setEvaluator(this);
    }

    @Override
    public void setParameterName(String str) throws GSimException {
        real.setParameterName(str);
        owner.setEvaluator(this);
    }

    @Override
    public void setParameterValue(String str) throws GSimException {
        real.setParameterValue(str);
        owner.setEvaluator(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }
}
