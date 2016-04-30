package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

public class RLRule extends UserRule {

    private static Logger logger = Logger.getLogger(RLRule.class);

    public static RLRule instanciate(Frame parent, String name) {
    	return new RLRule(parent, name);
    }

    public static RLRule fromInstance(Instance instance) {
    	return new RLRule(instance);
    }

    private RLRule(Frame parent, String name) {
        super(parent, name);
    }

    private RLRule(Instance inst) {
        super(inst);
    }

    public void addExpansion(ExpansionDef cond) {
        super.addChildInstance(RLRuleFrame.INST_LIST_EXP, cond);
    }

    public void addSelectionRule(UserRule sc) {
        super.addChildInstance(RLRuleFrame.INST_LIST_SHORTCUTS, sc);
    }

    public void addStateVar(String path) {
        SetAttribute s = (SetAttribute) this.getAttribute("state-var");
        s.addEntry(path);
        this.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, s);
    }

    public double getAvgBeta() {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "avg-beta");
        return a.getValue();
    }

    public double getAvgStepSize() {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "avg-step-size");
        return a.getValue();
    }

    public double getDefaultReward() {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "defaultReward");
        return a.getValue();
    }

    public double getDiscount() {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "discount");
        return a.getValue();
    }

    public ConditionDef getEvaluationFunction() {
        ArrayList<ConditionDef> list = new ArrayList<>();
        for (Instance inst: getChildInstances(RLRuleFrame.INST_LIST_LEARNING)) {
            if (!inst.getName().startsWith("{")) {
                list.add(new ConditionDef(inst));
            }
        }

        ConditionDef cond = null;
        if (list.size() == 1) {
            cond = (ConditionDef) list.get(0);
        } else if (list.size() > 1) {
            logger.warn("There was more than one learning evaluation function");
        }
        return cond;

    }

    public ExpansionDef[] getExpansions() {
        List<Instance> f = getChildInstances(RLRuleFrame.INST_LIST_EXP);
        ExpansionDef[] e = new ExpansionDef[f.size()];
        for (int i = 0; i < f.size(); i++) {
            e[i] = new ExpansionDef(f.get(i));
        }
        return e;
    }

    public String getMethod() {
        StringAttribute a = (StringAttribute) this.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "method");
        return a.getValue();
    }

    public boolean getRetractObsolete() {
        StringAttribute a = (StringAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "retract-osbolete-actions");
        return Boolean.parseBoolean(a.getValue());
    }

    public UserRule[] getShortSelectionRules() {
        List<Instance> f = getChildInstances(RLRuleFrame.INST_LIST_SHORTCUTS);
        UserRule[] res = new UserRule[f.size()];
        for (int i = 0; i < f.size(); i++) {
            res[i] = new UserRule(f.get(i));
        }
        return res;
    }

    public double getStateVarMax() {
        for (Attribute a:getAttributes(UserRuleFrame.ATTR_LIST_ATTRS)) {
            if (a.getName().equals("state-var-max")) {
                return ((NumericalAttribute) a).getValue();
            }
        }
        return 10;
    }

    public List<String> getStateVars() {
        SetAttribute da = (SetAttribute) this.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "state-var");
        return da.getFillers();
    }

    public boolean hasExpansions() {
        return super.getChildInstances(RLRuleFrame.INST_LIST_EXP).size() > 0;
    }

    public boolean hasSelectors() {
        return (getShortSelectionRules().length > 0);
    }

    public boolean isAveraging() {
        StringAttribute a = (StringAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "averaging");
        return Boolean.valueOf(a.getValue()).booleanValue();
    }

    public boolean isComparison() {
        StringAttribute a = (StringAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "comparison");
        return Boolean.valueOf(a.getValue()).booleanValue();
    }

    public void removeSelectionRule(UserRule sc) {
        super.removeChildInstance(RLRuleFrame.INST_LIST_SHORTCUTS, sc.getName());
    }

    public void setAveraging(boolean b) {
        StringAttribute a = (StringAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "averaging");
        String bool = String.valueOf(b);
        a.setValue(bool);
        super.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setAvgBeta(double d) {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "avg-beta");
        a.setValue(d);
        super.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setAvgStepSize(double d) {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "avg-step-size");
        a.setValue(d);
        super.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setComparison(boolean b) {
        StringAttribute a = (StringAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "comparison");
        String bool = String.valueOf(b);
        a.setValue(bool);
        super.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setDefaultReward(double d) {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "defaultReward");
        a.setValue(d);
        super.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setDiscount(double d) {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "discount");
        a.setValue(d);
        super.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setEvaluationFunction(ConditionDef f) {
        for (Instance a : super.getChildInstances(RLRuleFrame.INST_LIST_LEARNING)) {
            super.removeChildInstance(RLRuleFrame.INST_LIST_LEARNING, a.getName());
        }
        super.addChildInstance(RLRuleFrame.INST_LIST_LEARNING, f);
    }

    public void setMethod(String s) {
        StringAttribute a = (StringAttribute) this.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "method");
        a.setValue(s);
        this.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setRetractObsolete(boolean b) {
        StringAttribute a = (StringAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "retract-osbolete-actions");
        if (a == null) {
            a = new StringAttribute("retract-osbolete-actions", Boolean.toString(b));
        }
        a.setValue(Boolean.toString(b));
        super.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setStateVarMax(double d) {
        NumericalAttribute s = (NumericalAttribute) this.getAttribute("state-var-max");
        if (s == null) {
            s = new NumericalAttribute("state-var-max", d);
        }
        this.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, s);
    }

}
