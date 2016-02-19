package gsim.objects.impl;

import de.s2.gsim.core.GSimException;
import de.s2.gsim.objects.Condition;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ConditionFrame;

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
    public Unit toUnit() {
        return real;
    }
}
