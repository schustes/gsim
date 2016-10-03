package de.s2.gsim.environment;

import java.util.List;
import java.util.Optional;

import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * ActionFrame (and ActionInstance) are classes that represent actions and translate later, together with conditions into rules. Actions in the model
 * core are not restricted to a static implementation. As convention, every action can be applied selectively. For this each action can be declared
 * with a special parameter 'object parameter to be applied on' in the action.xml file. If this parameter is declared, the objects will be delivered
 * in the context of the rule it was fired. The translation into the rule system will later take care of the proper application of the rules once the
 * object-parameter is specified. There are certain conditions (e.g. Sales development per object class) that use the parameter to determine if a
 * condition is met. But for this, it has to determine the parameter, and that is provided ONLY here.
 */
public class ActionFrame extends Frame {

    public final static String ATTR_LIST_ATTRS = "attributes";

    public final static String ATTR_LIST_PARAMS = "parameters";

    public final static String ATTR_LIST_PROPS = "properties";

    public final static String CATEGORY = "action";

    static final long serialVersionUID = 1273007039000489581L;

    /**
     * Creates an action frame by inheriting the properties of the given one.
     * 
     * @param parent
     * @return the new ActionFrame
     */
    public static ActionFrame inherit(ActionFrame parent) {
        return new ActionFrame(parent);
    }

    /**
     * Creates a action frame.
     * 
     * @param name the name of the action
     * @param actionImplClass the actual java implementation class
     * @return the action frame
     */
    public static ActionFrame newActionFrame(String name, String actionImplClass) {
        return new ActionFrame(name, actionImplClass);
    }

    /**
     * Constructor by inheriting super type.
     * 
     * @param def the frame to inherit from
     */
    protected ActionFrame(Frame def) {
        super(def.getName(), def);
    }

    /**
     * Top level constructor.
     * 
     * @param name the name of the action
     * @param defaultClass the SimAction class to execute
     */
    private ActionFrame(String name, String defaultClass) {
        super(name, Optional.of(CATEGORY), true, false);
        DomainAttribute a = new DomainAttribute("class-name", AttributeType.STRING);
        a.setDefault(defaultClass);
        addOrSetAttribute(ATTR_LIST_ATTRS, a);

        DomainAttribute c = new DomainAttribute("cost", AttributeType.NUMERICAL);
        c.setDefault("0");
        addOrSetAttribute(ATTR_LIST_PROPS, c);

        DomainAttribute d = new DomainAttribute("salience", AttributeType.NUMERICAL);
        d.setDefault("0");
        addOrSetAttribute(ATTR_LIST_PROPS, d);

        DomainAttribute e = new DomainAttribute("object-types", AttributeType.SET);
        d.setDefault("");
        addOrSetAttribute(ATTR_LIST_PARAMS, e);

    }

    /**
     * Add a new Dialog for a certain defined parameter. Pre-condition: The parameter is defined.
     * 
     * @param forParameter
     *            String
     * @param cls
     *            String
     */
    public void addDialogClass(String forParameter, String cls) {

        DomainAttribute a = this.getAttribute(ATTR_LIST_PARAMS, forParameter);
        if (a != null) {
            a.addFiller(cls);
            addOrSetAttribute(ATTR_LIST_PARAMS, a);
        } else {
            DomainAttribute b = new DomainAttribute(forParameter, AttributeType.STRING);
            b.addFiller(cls);
            b.setDefault(cls);
            addOrSetAttribute(ATTR_LIST_PARAMS, b);
        }

    }

    public void addObjectClassParam(String cat, String filterExpr) {
        DomainAttribute a = this.getAttribute(ATTR_LIST_PARAMS, "object-types");

        if (filterExpr != null) {
            a.addFiller(cat + ":" + filterExpr);
        } else {
            a.addFiller(cat);
        }

        String s = "";

        if (a.getDefaultValue().length() > 0) {
            s = a.getDefaultValue() + ",";
        }

        s = filterExpr != null ? s + cat + ":" + filterExpr : s + cat;

        // s=s+cat+":"+filterExpr;

        a.setDefault(s);

        addOrSetAttribute(ATTR_LIST_PARAMS, a);
    }

    /**
     * Add user parameters necessary for the execution of this action.
     * 
     * @param name
     *            String
     */
    public void addUserParameter(String name) {
        DomainAttribute a = new DomainAttribute(name.trim(), AttributeType.SET);
        a.addFiller("{}");
        a.setDefault("{}");
        addOrSetAttribute(ATTR_LIST_PARAMS, a);
    }

    /**
     * Add a value to a specified user-parameter (defined as list).
     * 
     * @param name
     *            String
     * @param value
     *            String
     */
    public void addValueToUserParameter(String name, String value) {
        DomainAttribute a = this.getAttribute(ATTR_LIST_PARAMS, name);
        a.addFiller(value);

        String commaSeparated = a.getDefaultValue();
        if ((commaSeparated != null && commaSeparated.trim().length() == 0) || commaSeparated == null) {
            commaSeparated = value;
        } else {
            commaSeparated += "," + value;
        }
        a.setDefault(commaSeparated);
        addOrSetAttribute(ActionFrame.ATTR_LIST_PARAMS, a);
    }

    public void clearObjectClassParams() {
        DomainAttribute a = this.getAttribute(ATTR_LIST_PARAMS, "object-types");

        String s = "";
        a.setDefault(s);

        addOrSetAttribute(ATTR_LIST_PARAMS, a);
    }

    /**
     * Get name of realising class.
     * 
     * @return String
     */
    public String getClassName() {
        DomainAttribute att = this.getAttribute(ActionFrame.ATTR_LIST_ATTRS, "class-name");
        return att.getDefaultValue();
    }

    /**
     * Cost that agents encounter when applying the action.
     * 
     * @return double
     */
    public double getCost() {
        DomainAttribute att = this.getAttribute(ActionFrame.ATTR_LIST_PROPS, "cost");
        return Double.valueOf(att.getDefaultValue()).doubleValue();
    }

    public String getFilterExpression(String objParam) {
        DomainAttribute a = this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, "object-types");
        for (String filler: a.getFillers()) {
            String[] x = filler.split(":");
            String s = x[0].trim();
            if (s.equals(objParam) && x.length > 0) {
                return x[1].trim();
            } else {
                return ".*";
            }
        }
        return null;
    }

    public String[] getObjectClassParams() {
        DomainAttribute a = this.getAttribute(ATTR_LIST_PARAMS, "object-types");
        List<String> s = a.getFillers();
        String[] s2 = new String[s.size()];
        for (int i = 0; i < s.size(); i++) {
            String[] x = s.get(i).split(":");
            s2[i] = x[0].trim();
        }

        if (s2.length == 0) {
            return null;
        }
        return s2;
    }

    /**
     * If there are several actions in the same rule, it is possible to use the salience property, which translates into the salience later in the
     * rule base, to define a execution order.
     * 
     * @return double
     */
    public double getSalience() {
        DomainAttribute att = this.getAttribute(ActionFrame.ATTR_LIST_PROPS, "salience");
        return Double.valueOf(att.getDefaultValue()).doubleValue();
    }

    /**
     * Dialog necessary to specify parameter values (optional to use).
     * 
     * @param forParameter
     *            String
     * @return String
     */
    public String getUserParameterDialogClass(String forParameter) {
        DomainAttribute att = this.getAttribute(ActionFrame.ATTR_LIST_ATTRS, forParameter);
        return att.getDefaultValue();
    }

    /**
     * List of defined user-parameters.
     * 
     * @return String[]
     */
    public String[] getUserParameterNames() {
        List<DomainAttribute> atts = getAttributes(ActionFrame.ATTR_LIST_PARAMS);
        String[] s = new String[atts.size()];
        for (int i = 0; i < atts.size(); i++) {
            s[i] = atts.get(i).getName();
        }
        return s;
    }

    /**
     * Get the default value for a certain parameter.
     * 
     * @param paramName
     *            String
     * @return String
     */
    public String getUserParameterValue(String paramName) {
        DomainAttribute att = this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, paramName);
        return att.getDefaultValue();
    }

    /*
     * public void setHasObjectParameter(boolean b) { DomainAttribute a = new DomainAttribute("has-product-parameter", AttributeConstants.STRING);
     * a.setDefault(String.valueOf(b)); addOrSetAttribute(ATTR_LIST_ATTRS, a); }
     */
    public boolean hasObjectParameter() {
        return getObjectClassParams() != null;
    }

    public boolean hasUserParameter(String name) {
        return (this.getAttribute(ATTR_LIST_PARAMS, name) != null);
    }

    public void removeObjectClassParam(String cat) {
        DomainAttribute a = this.getAttribute(ATTR_LIST_PARAMS, "object-types");

        String[] f1 = new String[a.getFillers().size() - 1];
        int j = 0;
        for (int i = 0; i < a.getFillers().size(); i++) {
            if (!a.getFillers().get(i).equals(cat)) {
                f1[j] = a.getFillers().get(i);
                j++;
            }
        }

        String s = "";
        if (a.getDefaultValue().contains(cat)) {
            String[] split = cat.split(",");
            for (String aa : split) {
                if (!aa.trim().equals(cat)) {
                    if (s.length() > 0) {
                        s += ",";
                    }
                    s += aa.trim();
                }
            }
        }

        a.setDefault(s);

        addOrSetAttribute(ATTR_LIST_PARAMS, a);
    }

    public void setClassName(String c) {
        DomainAttribute att = this.getAttribute(ActionFrame.ATTR_LIST_ATTRS, "class-name");
        att.setDefault(c);
        addOrSetAttribute(ATTR_LIST_ATTRS, att);
    }

    /**
     * Set cost.
     * 
     * @param d
     *            double
     */
    public void setCost(double d) {
        DomainAttribute att = new DomainAttribute("cost", AttributeType.NUMERICAL);
        att.setDefault(String.valueOf(d));
        addOrSetAttribute(ATTR_LIST_PROPS, att);
    }

    /**
     * Set salience.
     * 
     * @param d
     *            double
     */
    public void setSalience(double d) {
        DomainAttribute att = new DomainAttribute("salience", AttributeType.NUMERICAL);
        att.setDefault(String.valueOf(d));
        addOrSetAttribute(ATTR_LIST_PROPS, att);
    }

}
