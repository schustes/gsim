package de.s2.gsim.environment;

import java.util.List;

import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

public class ExpansionDef extends Instance {

    public ExpansionDef(Instance in) {
        super(in);
    }

    public ExpansionDef(String forParameter) {
        super(forParameter + "-expansion-" + cern.jet.random.Uniform.staticNextDouble(), new ExpansionFrame(forParameter));
    }

    public ExpansionDef(String var, double min, double max) {
        this(var);
        setMax(max);
        setMin(min);
    }

    public List<String> getFillers() {
        SetAttribute a0 = (SetAttribute) this.getAttribute("fillers");
        return a0.getFillersAndEntries();
    }

    public double getMax() {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("max");
        if (a != null) {
            return a.getValue();
        }
        return -1;
    }

    public double getMin() {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("min");
        if (a != null) {
            return a.getValue();
        }
        return -1;
    }

    public String getParameterName() {
        return this.getAttribute("parameter-name").toValueString();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public boolean isNumerical() {
        return getFillers().size() == 0;
    }

    public void setFillers(String[] fillers) {
        SetAttribute a0 = (SetAttribute) this.getAttribute("fillers");
        a0.removeAllEntries();
        for (String s : fillers) {
            a0.addEntry(s);
        }
        this.setAttribute(a0);
    }

    public void setMax(double parameterValue) {
        NumericalAttribute a0 = (NumericalAttribute) this.getAttribute("max");
        a0.setValue(parameterValue);
        this.addOrSetAttribute(ExpansionFrame.ATTR_LIST_ATTRS, a0);
    }

    public void setMin(double parameterValue) {
        NumericalAttribute a0 = (NumericalAttribute) this.getAttribute("min");
        a0.setValue(parameterValue);
        this.addOrSetAttribute(ExpansionFrame.ATTR_LIST_ATTRS, a0);
    }

    public void setParameterName(String parameterName) {
        StringAttribute a0 = (StringAttribute) this.getAttribute("parameter-name");
        a0.setValue(parameterName);
        this.addOrSetAttribute(ExpansionFrame.ATTR_LIST_ATTRS, a0);
    }

}
