package de.s2.gsim.api.objects.impl;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.ExpansionFrame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.Expansion;

public class ExpansionClass implements Expansion, UnitWrapper {

    private RLActionNodeClass owner;

    private ExpansionFrame real;

    public ExpansionClass(RLActionNodeClass owner, ExpansionFrame real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addFiller(String filler) throws GSimException {
        real.addFiller(filler);
        owner.addOrSetExpansion(this);
    }

    @Override
    public String[] getFillers() {
        return real.getFillers();
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
        return !Double.isNaN(Double.valueOf(real.getMin()));
    }

    @Override
    public void setMax(String parameterValue) throws GSimException {
        real.setMax(parameterValue);
        owner.addOrSetExpansion(this);
    }

    @Override
    public void setMin(String parameterValue) throws GSimException {
        real.setMin(parameterValue);
        owner.addOrSetExpansion(this);
    }

    @Override
    public void setParameterName(String parameterName) throws GSimException {
        real.setParameterName(parameterName);
        owner.addOrSetExpansion(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
