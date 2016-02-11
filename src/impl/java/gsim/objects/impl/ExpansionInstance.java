package gsim.objects.impl;

import de.s2.gsim.objects.ExpansionIF;
import de.s2.gsim.objects.GSimObjectException;
import gsim.def.objects.Unit;
import gsim.def.objects.behaviour.Expansion;

public class ExpansionInstance implements ExpansionIF, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private RuleInstance owner;

    private Expansion real;

    public ExpansionInstance(RuleInstance owner, Expansion real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addFiller(String filler) throws GSimObjectException {
        String[] fillers = real.getFillers();
        boolean b = false;
        for (String s : fillers) {
            if (s.equals(filler)) {
                b = true;
            }
        }
        if (!b) {
            String[] nf = new String[fillers.length + 1];
            for (int i = 0; i < fillers.length; i++) {
                nf[i] = fillers[i];
            }
            nf[nf.length - 1] = filler;
            real.setFillers(nf);
        }
        owner.addOrSetExpansion(this);

        // throw new UnsupportedOperationException("Not valid here");
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
        return Double.isNaN(Double.valueOf(real.getMin()));
    }

    @Override
    public void setMax(String parameterValue) throws GSimObjectException {
        real.setMax(Double.valueOf(parameterValue));
        owner.addOrSetExpansion(this);
    }

    @Override
    public void setMin(String parameterValue) throws GSimObjectException {
        real.setMin(Double.valueOf(parameterValue));
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
