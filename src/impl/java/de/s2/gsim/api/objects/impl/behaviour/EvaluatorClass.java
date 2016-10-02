package de.s2.gsim.api.objects.impl.behaviour;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ConditionFrame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.Condition;

public class EvaluatorClass implements Condition, UnitWrapper {

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
    public Unit toUnit() {
        return real;
    }
}
