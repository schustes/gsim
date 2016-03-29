package de.s2.gsim.environment;

import de.s2.gsim.objects.attribute.StringAttribute;

public class ConditionDef extends Instance {

    static final long serialVersionUID = 437787152226881019L;

    public ConditionDef(ConditionFrame template) {
        super("Condition for variable " + template.getParameterName(), template);
    }

    /**
     * Copy constructor.
     * 
     * @param inst
     *            Instance
     */
    public ConditionDef(Instance inst) {
        super(inst);
    }

    public ConditionDef(String parameterName, String operator, String parameterValue) {
        super(parameterName, new ConditionFrame(parameterName));
        super.setAttribute(new StringAttribute("parameter-name", parameterName));
        super.setAttribute(new StringAttribute("operator", operator));
        super.setAttribute(new StringAttribute("parameter-value", parameterValue));
    }

    public boolean equals1(Object o) {
        if (!(o instanceof ConditionDef)) {
            return false;
        }

        ConditionDef other = (ConditionDef) o;

        String var1 = getParameterName();
        String op1 = getOperator();
        String val = getParameterValue();

        String var2 = other.getParameterName();
        String op2 = other.getOperator();
        String val2 = other.getParameterValue();

        if (var1.equals(var2) && op1.equals(op2) && val.equals(val2)) {
            return true;
        }
        return false;

    }

    public String getOperator() {
        return this.getAttribute("operator").toValueString();
    }

    public String getParameterClass() {
        return this.getAttribute("parameter-class").toValueString();
    }

    public String getParameterName() {
        return this.getAttribute("parameter-name").toValueString();
    }

    public String getParameterValue() {
        return this.getAttribute("parameter-value").toValueString();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public void setOperator(String str) {
        StringAttribute s = new StringAttribute("operator", str);
        this.setAttribute(s);
    }

    public void setParameterName(String str) {
        StringAttribute s = new StringAttribute("parameter-name", str);
        this.setAttribute(s);
    }

    public void setParameterValue(String str) {
        StringAttribute s = new StringAttribute("parameter-value", str);
        this.setAttribute(s);
    }

}
