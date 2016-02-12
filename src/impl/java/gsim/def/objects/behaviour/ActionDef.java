package gsim.def.objects.behaviour;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;
import gsim.def.objects.Instance;
import gsim.util.Utils;

/**
 * Instance class for the ActionFrame class.
 *
 * @see ActionFrame for details.
 */
public class ActionDef extends Instance {

    static final long serialVersionUID = -6035384056999349555L;

    /**
     * Inheritance constructor.
     * 
     * @param f
     *            ActionFrame
     */
    public ActionDef(ActionFrame f) {
        super(f.getTypeName(), f);
    }

    /**
     * Copy constructor.
     * 
     * @param inst
     *            Instance
     */
    public ActionDef(Instance inst) {
        super(inst);
    }

    /**
     * Add a dialog for a certain parameter. The parameter has to be defined.
     * 
     * @param forParameter
     *            String
     * @param cls
     *            String
     */
    public void addDialogClass(String forParameter, String cls) {

        StringAttribute a = (StringAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, forParameter);
        if (a != null) {
            a.setValue(cls);
            this.setAttribute(ActionFrame.ATTR_LIST_PARAMS, a);
        } else {
            StringAttribute b = new StringAttribute(forParameter, cls);
            this.setAttribute(ActionFrame.ATTR_LIST_PARAMS, b);
        }

    }

    public void addObjectClassParam(String cat, String filterExpr) {
        SetAttribute a = (SetAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, "object-types");

        /*
         * if (filterExpr!=null) { a.setValue(cat+":"+filterExpr); } else { a.setValue(cat); }
         */
        if (filterExpr != null) {
            a.addEntry(cat + ":" + filterExpr);
        } else {
            a.addEntry(cat);
        }
        a.addEntry(cat + ":" + filterExpr);
    }

    /**
     * Add value to user parameter.
     * 
     * @param name
     *            String
     * @param value
     *            String
     */
    public void addValueToUserParameter(String name, String value) {
        SetAttribute a = (SetAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, name);
        a.addEntry(value);
        this.setAttribute(ActionFrame.ATTR_LIST_PARAMS, a);
    }

    /**
     * Two actions are equal if they have the same names and the same user-parameter values.
     * 
     * @param o
     *            Object
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof ActionDef)) {
            return false;
        }

        ActionDef other = (ActionDef) o;

        if (!other.getName().equals(getName())) {
            return false;
        }

        String[] otherParameters = other.getUserParameterNames();
        String[] myParameters = getUserParameterNames();

        for (int i = 0; i < otherParameters.length; i++) {
            boolean b2 = true;
            for (int j = 0; j < myParameters.length; j++) {
                if (otherParameters[i].equals(myParameters[j])) {
                    String[] myParamVals = getUserParameterValues(myParameters[j]);
                    String[] otherParamVals = getUserParameterValues(otherParameters[i]);
                    b2 = Utils.equalArrays(myParamVals, otherParamVals);
                    if (!b2) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * The java class that realises this action.
     * 
     * @return String
     */
    public String getClassName() {
        return this.getAttribute("class-name").toValueString();
    }

    /**
     * The cost for agents of applying this action.
     * 
     * @return double
     */
    public double getCost() {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PROPS, "cost");
        return a.getValue();
    }

    public String getFilterExpression(String objParam) {
        SetAttribute a = (SetAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, "object-types");
        String regExp = "";

        if (a == null) {
            return ".*";
        }

        for (int i = 0; i < a.getEntries().size(); i++) {
            String val = (String) a.getEntries().get(i);

            String[] x = val.split(":");

            if (x.length == 1) {
                return ".*";// no filter given by user (only
                            // object-param before the ':')
            }

            String s = x[0].trim();
            regExp = x[1].trim();

            if (s.equals(objParam) && x.length > 0) {
                if (regExp.length() > 0) {
                    regExp += "|";
                }
                regExp += ".*" + x[1] + ".*";
            } else {
                // return "";
            }

            if (regExp.length() == 0) {
                return ".*";
            }
            return regExp;
        }
        return ".*";
    }

    public String[] getObjectClassParams() {
        SetAttribute a = (SetAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, "object-types");
        ArrayList list = new ArrayList();
        if (a != null) {
            for (int i = 0; i < a.getEntries().size(); i++) {
                String val = (String) a.getEntries().get(i);
                String[] s = val.split(":");
                if (s.length > 0) {
                    list.add(s[0]);
                }
            }
        }
        String[] ret = new String[list.size()];
        list.toArray(ret);
        if (ret.length == 0) {
            return null;
        }
        return ret;
    }

    /**
     * get salience property.
     * 
     * @return double
     */
    public double getSalience() {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PROPS, "salience");
        return a.getValue();
    }

    /**
     * Get the UI-class for setting the parameters declared for this action (in action.xml).
     * 
     * @param forParameter
     *            String
     * @return String
     */
    public String getUserParameterDialogClass(String forParameter) {
        Attribute att = this.getAttribute(ActionFrame.ATTR_LIST_ATTRS, forParameter);
        return att.toValueString();
    }

    /**
     * Lists all parameters that are defined.
     * 
     * @return String[]
     */
    public String[] getUserParameterNames() {
        Attribute[] atts = getAttributes(ActionFrame.ATTR_LIST_PARAMS);
        if (atts != null) {
            String[] s = new String[atts.length];
            for (int i = 0; i < atts.length; i++) {
                s[i] = atts[i].getName();
            }
            return s;
        }
        return new String[0];
    }

    /**
     * Gets the value(s) of a certain parameter.
     * 
     * @param paramName
     *            String
     * @return String[]
     */
    public String[] getUserParameterValues(String paramName) {
        SetAttribute att = (SetAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, paramName);
        Iterator iter = att.getEntries().iterator();
        HashSet s = new HashSet();
        while (iter.hasNext()) {
            s.add(iter.next());
        }
        String[] str = new String[s.size()];
        s.toArray(str);

        return str;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    /*
     * public void setHasObjectParameter(boolean b) { StringAttribute a = new StringAttribute("has-product-parameter", String.valueOf(b));
     * setAttribute(ActionFrame.ATTR_LIST_ATTRS, a); }
     */
    public boolean hasObjectParameter() {
        SetAttribute a = (SetAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, "object-types");
        if (a != null) {
            return a.getEntries().size() > 0;
        }

        return false;
    }

    public void removeObjectClassParam(String cat) {
        SetAttribute a = (SetAttribute) this.getAttribute(ActionFrame.ATTR_LIST_PARAMS, "object-types");

        Iterator iter = a.getEntries().iterator();
        while (iter.hasNext()) {
            String m = (String) iter.next();
            if (m.contains(cat)) {
                iter.remove();
            }
        }
        this.setAttribute(ActionFrame.ATTR_LIST_PARAMS, a);
    }

    /**
     * Remove a certain value from a certain parameter (if applicable).
     * 
     * @param name
     *            String
     * @param value
     *            String
     */
    public void removeValueFromUserParameter(String name, String value) {
        SetAttribute a = (SetAttribute) this.getAttribute(name);
        a.getEntries().remove(value);
        this.setAttribute(ActionFrame.ATTR_LIST_PARAMS, a);
    }

    /**
     * Set realising class.
     * 
     * @param className
     *            String
     */
    public void setClassName(String className) {
        this.setAttribute(new StringAttribute("class-name", className));
    }

    /**
     * Set the cost.
     * 
     * @param s
     *            double
     */
    public void setCost(double s) {
        this.setAttribute(new NumericalAttribute("cost", s));
    }

    /**
     * Set salience.
     * 
     * @param s
     *            double
     */
    public void setSalience(double s) {
        this.setAttribute(new NumericalAttribute("salience", s));
    }

}
