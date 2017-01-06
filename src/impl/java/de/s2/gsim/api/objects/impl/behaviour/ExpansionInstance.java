package de.s2.gsim.api.objects.impl.behaviour;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.Expansion;

public class ExpansionInstance implements Expansion, UnitWrapper {

    private RuleInstance owner;

    private ExpansionDef real;

    public ExpansionInstance(RuleInstance owner, ExpansionDef real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addFiller(String filler) throws GSimException {
    	
    	if (!real.getFillers().contains(filler)) {
    		real.getFillers().add(filler);
    	}
    	
        owner.addOrSetExpansion(this);
    }

    @Override
    public String[] getFillers() {
        return real.getFillers().toArray(new String[0]);
    }

    @Override
    public String getMax() {
        return String.valueOf(real.getMax());
    }

    @Override
    public String getMin() {
        return String.valueOf(real.getMin());
    }

    @Override
    public String getParameterName() {
        return real.getParameterName();
    }

    @Override
    public boolean isNumerical() {
        return Double.isNaN(Double.valueOf(real.getMin()));
    }

    @Override
    public void setMax(String parameterValue) throws GSimException {
        real.setMax(Double.valueOf(parameterValue));
        owner.addOrSetExpansion(this);
    }

    @Override
    public void setMin(String parameterValue) throws GSimException {
        real.setMin(Double.valueOf(parameterValue));
        owner.addOrSetExpansion(this);
    }

    @Override
    public void setParameterName(String parameterName) throws GSimException {
        real.setParameterName(parameterName);
        owner.addOrSetExpansion(this);
    }

    @Override
	public Unit<?, ?> toUnit() {
        return real;
    }

}
