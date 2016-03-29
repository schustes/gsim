package de.s2.gsim.def.objects.behaviour;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.s2.gsim.def.objects.Frame;
import de.s2.gsim.def.objects.Instance;
import de.s2.gsim.def.objects.UnitUtils;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.objects.attribute.StringAttribute;

public class RLRule extends UserRule {

    private static Logger logger = Logger.getLogger(RLRule.class);

    private static final long serialVersionUID = 1L;

    public RLRule(Frame parent, String name) {
        super(parent, name);
    }

    public RLRule(Instance inst) {
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
        this.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, s);
    }

    public void addTest(DependencyTest test) {
        Instance in = this.getChildInstance(RLRuleFrame.INST_LIST_TESTS, "TestCollection");
        DependencyTestDefinition fr = null;
        if (in != null) {
            fr = new DependencyTestDefinition(in);
        }
        if (fr == null) {
            fr = new DependencyTestDefinition(new DependencyTestDefinitionFrame("TestCollection"));
        }
        fr.addDependencyTest(test);
        addChildInstance(RLRuleFrame.INST_LIST_TESTS, fr);

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
        Instance[] inst = getChildInstances(RLRuleFrame.INST_LIST_LEARNING);
        ArrayList list = new ArrayList();
        for (int i = 0; i < inst.length; i++) {
            if (!inst[i].getName().startsWith("{")) {
                list.add(new ConditionDef(inst[i]));
            }
        }

        ConditionDef cond = null;
        if (list.size() == 1) {
            cond = (ConditionDef) list.get(0);
        } else if (list.size() > 1) {
            logger.warn("There was more than one learning evaluation function");
            // cond = (Condition) list.get(0);
        }
        return cond;

    }

    public ExpansionDef[] getExpansions() {
        Instance[] f = super.getChildInstances(RLRuleFrame.INST_LIST_EXP);
        ExpansionDef[] e = new ExpansionDef[f.length];
        for (int i = 0; i < f.length; i++) {
            e[i] = new ExpansionDef(f[i]);
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
        Instance[] f = super.getChildInstances(RLRuleFrame.INST_LIST_SHORTCUTS);
        UserRule[] res = new UserRule[f.length];
        for (int i = 0; i < f.length; i++) {
            res[i] = new UserRule(f[i]);
        }
        return res;
    }

    public double getStateVarMax() {
        Attribute[] da = getAttributes(UserRuleFrame.ATTR_LIST_ATTRS);
        for (int i = 0; i < da.length; i++) {
            if (da[i].getName().equals("state-var-max")) {
                return ((NumericalAttribute) da[i]).getValue();
            }
        }
        return 10;
    }

    public String[] getStateVars() {
        SetAttribute da = (SetAttribute) this.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "state-var");
        return da.getFillers();
        // if (da!=null) {
        // String[] ret = new String[da.getEntries().size()];
        // da.getEntries().toArray(ret);
        // return ret;
        // }
        // return new String[0];
    }

    public DependencyTest[] getTests() {
        Instance x = this.getChildInstance(RLRuleFrame.INST_LIST_TESTS, "TestCollection");
        if (x != null) {
            DependencyTestDefinition fr = new DependencyTestDefinition(x);
            if (fr != null) {
                return fr.getDependencyTests();
            } else {
                return null;
            }
        }
        return new DependencyTest[0];
    }

    public boolean hasExpansions() {
        return super.getChildInstances(RLRuleFrame.INST_LIST_EXP).length > 0;
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

    public void removeTest(String testName) {
        Object o = super.resolveName(new String[] { RLRuleFrame.INST_LIST_TESTS, "TestCollection", "list", testName });
        logger.debug("o:" + o);
        UnitUtils.getInstance().removeChildInstance(this, new String[] { RLRuleFrame.INST_LIST_TESTS, "TestCollection", "list" }, testName);
    }

    public void setAveraging(boolean b) {
        StringAttribute a = (StringAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "averaging");
        String bool = String.valueOf(b);
        a.setValue(bool);
        super.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setAvgBeta(double d) {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "avg-beta");
        a.setValue(d);
        super.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setAvgStepSize(double d) {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "avg-step-size");
        a.setValue(d);
        super.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setComparison(boolean b) {
        StringAttribute a = (StringAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "comparison");
        String bool = String.valueOf(b);
        a.setValue(bool);
        super.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setDefaultReward(double d) {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "defaultReward");
        a.setValue(d);
        super.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setDiscount(double d) {
        NumericalAttribute a = (NumericalAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "discount");
        a.setValue(d);
        super.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
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
        this.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setRepeatedExecutionTest(String timeToWait) {

        if (de.s2.gsim.util.Utils.isNumerical(timeToWait) && Double.parseDouble(timeToWait) <= 0) {
            return;
        }

        DependencyTestFrame t = new DependencyTestFrame("test", "?x1", "?y1", "?z1",
                "(and (eq ?y1 ?y2) (or(>= ?z1 (- $current-time$ (mod $current-time$ " + timeToWait + "))) "
                        + "(>= ?z2 (- $current-time$ (mod $current-time$ " + timeToWait + ")))) )",
                "?x2", "?y2", "?z2");

        DependencyTest test = new DependencyTest(t);
        addTest(test);

    }

    public void setRetractObsolete(boolean b) {
        StringAttribute a = (StringAttribute) super.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "retract-osbolete-actions");
        if (a == null) {
            a = new StringAttribute("retract-osbolete-actions", Boolean.toString(b));
        }
        a.setValue(Boolean.toString(b));
        super.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, a);
    }

    public void setStateVarMax(double d) {
        NumericalAttribute s = (NumericalAttribute) this.getAttribute("state-var-max");
        if (s == null) {
            s = new NumericalAttribute("state-var-max", d);
        }
        this.setAttribute(UserRuleFrame.ATTR_LIST_ATTRS, s);
    }

}
