package de.s2.gsim.api.objects.impl;

import de.s2.gsim.GSimException;
import de.s2.gsim.def.objects.UnitOLD;
import de.s2.gsim.def.objects.behaviour.ConditionFrame;
import de.s2.gsim.objects.Condition;

public class EvaluatorClass implements Condition, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private RLActionNodeClass owner;

    private ConditionFrame real;

    public EvaluatorClass(RLActionNodeClass owner, ConditionFrame real) {
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
    public UnitOLD toUnit() {
        return real;
    }
}
