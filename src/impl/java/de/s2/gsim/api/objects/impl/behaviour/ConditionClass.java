package de.s2.gsim.api.objects.impl.behaviour;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ConditionFrame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.Condition;

public class ConditionClass implements Condition, UnitWrapper {

    private RuleClass owner;
    
    private ConditionFrame real;
    
    private ConditionFrame getReal() {
    	
    	for (Condition c: owner.getConditions()) {
    		ConditionFrame f= (ConditionFrame)((UnitWrapper)c).toUnit();
    		if (f.getName().equals(real.getName())) {
    			this.real = f;
    			break;
    		}
    	}
    	return this.real;
    }

    public ConditionClass(RuleClass owner, ConditionFrame real) {
        this.real = real;
        this.owner = owner;
    }
    
    @Override
    public String getOperator() {
        return getReal().getOperator();
    }

    @Override
    public String getParameterName() {
        return getReal().getParameterName();
    }

    @Override
    public String getParameterValue() {
        return getReal().getParameterValue();
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
    public Unit<?,?> toUnit() {
        return real;
    }

}
