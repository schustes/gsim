package gsim.def.objects.behaviour;

import de.s2.gsim.objects.attribute.AttributeConstants;
import de.s2.gsim.objects.attribute.DomainAttribute;
import gsim.def.objects.Frame;

public class ExpansionFrame extends Frame {

    public final static String ATTR_LIST_ATTRS = "attributes";

    public final static String CATEGORY = "expansion";

    public static ExpansionFrame DEFINITION = new ExpansionFrame("Expansion-template");

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ExpansionFrame(Frame f) {
        super(f);
    }

    public ExpansionFrame(String forParameter) {
        super(forParameter, "expansion");
        DomainAttribute a = new DomainAttribute("parameter-name", AttributeConstants.STRING);
        DomainAttribute b = new DomainAttribute("min", AttributeConstants.NUMERICAL);
        DomainAttribute c = new DomainAttribute("max", AttributeConstants.NUMERICAL);
        DomainAttribute d = new DomainAttribute("fillers", AttributeConstants.SET);

        a.setDefault(forParameter);
        b.setDefault(String.valueOf(Double.NaN));
        c.setDefault(String.valueOf(Double.NaN));

        addOrSetAttribute(ATTR_LIST_ATTRS, a);
        addOrSetAttribute(ATTR_LIST_ATTRS, b);
        addOrSetAttribute(ATTR_LIST_ATTRS, c);
        addOrSetAttribute(ATTR_LIST_ATTRS, d);
    }

    public ExpansionFrame(String var, String min, String max) {
        this(var);
        setDefaults(var, min, max, "");
    }

    public void addFiller(String s) {
        DomainAttribute a = this.getAttribute("fillers");
        String[] s0 = a.getFillers();
        if (!gsim.util.Utils.contains(s0, s)) {
            a.addFiller(s);
        }
        super.addOrSetAttribute("fillers", a);
    }

    public String[] getFillers() {
        DomainAttribute a0 = this.getAttribute("fillers");
        return a0.getFillers();
    }

    public String getMax() {
        return this.getAttribute("max").getDefaultValue();
    }

    public String getMin() {
        return this.getAttribute("min").getDefaultValue();
    }

    public String getParameterName() {
        return this.getAttribute("parameter-name").getDefaultValue();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public void setDefaults(String parameterName, String min, String max, String filler) {

        DomainAttribute a = this.getAttribute("parameter-name");
        a.setDefault(parameterName);

        DomainAttribute b = this.getAttribute("min");
        b.setDefault(min);

        DomainAttribute c = this.getAttribute("max");
        c.setDefault(max);

        DomainAttribute d = this.getAttribute("fillers");
        d.setDefault(filler);

        addOrSetAttribute(ATTR_LIST_ATTRS, a);
        addOrSetAttribute(ATTR_LIST_ATTRS, b);
        addOrSetAttribute(ATTR_LIST_ATTRS, c);
        addOrSetAttribute(ATTR_LIST_ATTRS, d);
    }

    public void setFillers(String[] fillers) {
        DomainAttribute a0 = this.getAttribute("fillers");
        a0.setFillers(fillers);
        addOrSetAttribute(ATTR_LIST_ATTRS, a0);
    }

    public void setMax(String parameterValue) {
        DomainAttribute a0 = this.getAttribute("max");
        a0.setDefault(parameterValue);
        addOrSetAttribute(ATTR_LIST_ATTRS, a0);
    }

    public void setMin(String parameterValue) {
        DomainAttribute a0 = this.getAttribute("min");
        a0.setDefault(parameterValue);
        addOrSetAttribute(ATTR_LIST_ATTRS, a0);
    }

    public void setParameterName(String parameterName) {
        DomainAttribute a0 = this.getAttribute("parameter-name");
        a0.setDefault(parameterName);
        addOrSetAttribute(ATTR_LIST_ATTRS, a0);
    }

}
