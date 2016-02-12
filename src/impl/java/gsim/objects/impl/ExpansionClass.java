package gsim.objects.impl;

import de.s2.gsim.objects.Expansion;
import de.s2.gsim.objects.GSimObjectException;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.ExpansionFrame;

public class ExpansionClass implements Expansion, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private RLActionNodeClass owner;

    private ExpansionFrame real;

    public ExpansionClass(RLActionNodeClass owner, ExpansionFrame real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addFiller(String filler) throws GSimObjectException {
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
    public void setMax(String parameterValue) throws GSimObjectException {
        real.setMax(parameterValue);
        owner.addOrSetExpansion(this);
    }

    @Override
    public void setMin(String parameterValue) throws GSimObjectException {
        real.setMin(parameterValue);
        owner.addOrSetExpansion(this);
    }

    @Override
    public void setParameterName(String parameterName) throws GSimObjectException {
        real.setParameterName(parameterName);
        owner.addOrSetExpansion(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
