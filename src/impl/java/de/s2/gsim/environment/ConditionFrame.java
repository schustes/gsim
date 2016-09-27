package de.s2.gsim.environment;

import java.util.List;
import java.util.Optional;

import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * Represents one condition element. Each possible condition element contains only one parameter, and may have limited range of applicable operators.
 * The one parameter is unique for each consequence. For displaying all condition elements, one has to use the instances..not very accurate..?
 */
public class ConditionFrame extends Frame {

    public final static String ATTR_LIST_ATTRS = "attributes";

    public final static String ATTR_LIST_PARAM = "parameter";

    public final static String CATEGORY = "condition";

    public static ConditionFrame DEFINITION = new ConditionFrame("Condition-template");

    static final long serialVersionUID = 8165736680329194821L;

    public ConditionFrame(Frame f) {
        super(f.getName(), f);
    }

    public ConditionFrame(String forParameter) {
        super(forParameter + "-condition-" + cern.jet.random.Uniform.staticNextDouble(), Optional.of("condition"), true, false);
        DomainAttribute a0 = new DomainAttribute("parameter-name", AttributeType.STRING);
        DomainAttribute a = new DomainAttribute("parameter-class", AttributeType.STRING);
        DomainAttribute b = new DomainAttribute("operator", AttributeType.STRING);
        b.addFiller(">");
        b.addFiller("<");
        b.addFiller(">=");
        b.addFiller("<=");
        b.addFiller("=");
        DomainAttribute c = new DomainAttribute("parameter-value", AttributeType.STRING);
        a0.setDefault(forParameter);
        a.setDefault("");
        b.setDefault("=");

        addOrSetAttribute(ATTR_LIST_ATTRS, a);
        addOrSetAttribute(ATTR_LIST_PARAM, a0);
        addOrSetAttribute(ATTR_LIST_PARAM, b);
        addOrSetAttribute(ATTR_LIST_PARAM, c);
    }

    public ConditionFrame(String var, String op, String val) {
        this(var);
        setDefaults(var, op, val);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConditionFrame)) {
            return false;
        }

        ConditionFrame other = (ConditionFrame) o;

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
        return this.getAttribute("operator").getDefaultValue();
    }

    public List<String> getOperators() {
        return this.getAttribute("operator").getFillers();
    }

    public String getParameterClass() {
        return this.getAttribute("parameter-class").getDefaultValue();
    }

    public String getParameterName() {
        return this.getAttribute("parameter-name").getDefaultValue();
    }

    public String getParameterValue() {
        return this.getAttribute("parameter-value").getDefaultValue();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public void setDefaults(String parameterName, String operator, String parameterValue) {

        DomainAttribute a0 = this.getAttribute("parameter-name");
        a0.setDefault(parameterName);

        DomainAttribute b = this.getAttribute("parameter-value");
        b.setDefault(parameterValue);

        DomainAttribute c = this.getAttribute("operator");
        c.setDefault(operator);

        addOrSetAttribute(ATTR_LIST_PARAM, b);
        addOrSetAttribute(ATTR_LIST_PARAM, c);
        addOrSetAttribute(ATTR_LIST_PARAM, a0);
    }

    public void setOperator(String op) {
        DomainAttribute a0 = this.getAttribute("operator");
        a0.setDefault(op);
        addOrSetAttribute(ATTR_LIST_PARAM, a0);
    }

    public void setParameterName(String parameterName) {
        DomainAttribute a0 = this.getAttribute("parameter-name");
        a0.setDefault(parameterName);
        addOrSetAttribute(ATTR_LIST_PARAM, a0);
    }

    public void setParameterValue(String parameterValue) {
        DomainAttribute a0 = this.getAttribute("parameter-value");
        a0.setDefault(parameterValue);
        addOrSetAttribute(ATTR_LIST_PARAM, a0);
    }

}
