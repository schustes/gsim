package gsim.def.objects.behaviour;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import gsim.def.objects.Frame;

public class RLRuleFrame extends UserRuleFrame {

    public static final String INST_LIST_EXP = "expansions";

    public static final String INST_LIST_LEARNING = "learning-evaluation";

    public static final String INST_LIST_SHORTCUTS = "rules helping to reduce the action space of this node";

    public static final String INST_LIST_TESTS = "tests";

    public static RLRuleFrame RL_RULE_FRAME = new RLRuleFrame("template-rl-frame");

    private static Logger logger = Logger.getLogger(RLRuleFrame.class);

    private static final long serialVersionUID = 1L;

    public RLRuleFrame(Frame f) {
        super(f);
        // init();
    }

    public RLRuleFrame(Frame cloneFrom, String newName) {
        super(cloneFrom);
        super.setTypeName(newName);
    }

    public RLRuleFrame(Frame[] parents, String name, String category) {
        super(parents, name, category);
        init();
    }

    public RLRuleFrame(String name) {
        super(name);
        init();
    }

    public void addDependcyTestFrame(DependencyTestFrame f) {
        DependencyTestDefinitionFrame fr = (DependencyTestDefinitionFrame) getChildFrame(INST_LIST_TESTS, "TestCollection");
        if (fr == null) {
            fr = new DependencyTestDefinitionFrame("TestCollection");
        }
        fr.addDependencyTest(f);
        addChildFrame(INST_LIST_TESTS, fr);
    }

    public void addExpansion(ExpansionFrame cond) {
        super.addChildFrame(INST_LIST_EXP, cond);
    }

    public void addSelectionRule(UserRuleFrame sc) {
        super.addChildFrame(INST_LIST_SHORTCUTS, sc);
    }

    public void addStateVar(String path) {
        DomainAttribute s = this.getAttribute("state-var");

        if (s == null) {
            s = new DomainAttribute("state-var", AttributeType.SET);
        }
        s.setDefault(path);
        s.addFiller(path);

        addOrSetAttribute(ATTR_LIST_ATTRS, s);
    }

    public void addTest(DependencyTestFrame test) {

        Frame in = getChildFrame(RLRuleFrame.INST_LIST_TESTS, "TestCollection");

        DependencyTestDefinitionFrame fr = null;

        if (in == null) {
            fr = new DependencyTestDefinitionFrame("TestCollection");
        } else {
            fr = new DependencyTestDefinitionFrame(in);
        }

        fr.removeChildFrame("TestCollection", test.getTypeName());

        fr.addDependencyTest(test);

        addChildFrame(RLRuleFrame.INST_LIST_TESTS, fr);

    }

    @Override
    public Object clone() {
        Frame f = (Frame) super.clone();
        RLRuleFrame c = new RLRuleFrame(f);
        // c.init();
        return c;
    }

    public double getAvgBeta() {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "avg-beta");
        return Double.parseDouble(a.getDefaultValue());
    }

    public double getAvgStepSize() {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "avg-step-size");
        return Double.parseDouble(a.getDefaultValue());
    }

    public double getDefaultReward() {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "defaultReward");
        return Double.parseDouble(a.getDefaultValue());
    }

    public double getDiscount() {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "discount");
        return Double.parseDouble(a.getDefaultValue());
    }

    public ConditionFrame getEvaluationFunction() {
        Frame[] inst = getChildFrames(INST_LIST_LEARNING);
        ArrayList list = new ArrayList();
        for (int i = 0; i < inst.length; i++) {
            if (!inst[i].getTypeName().startsWith("{")) {
                list.add(new ConditionFrame(inst[i]));
            }
        }

        ConditionFrame cond = null;
        if (list.size() == 1) {
            cond = (ConditionFrame) list.get(0);
        } else if (list.size() > 1) {
            logger.warn("There was more than one learning evaluation function");
        }
        return cond;

    }

    public String getExecutionIntervalTest() {
        DependencyTestFrame[] f = getTests();
        for (DependencyTestFrame t : f) {
            if (t.getTypeName().equals("test")) {
                return t.getWait();
            }
        }
        return "0";
    }

    public ExpansionFrame[] getExpansions() {
        Frame[] f = super.getChildFrames(INST_LIST_EXP);
        ExpansionFrame[] e = new ExpansionFrame[f.length];
        for (int i = 0; i < f.length; i++) {
            e[i] = new ExpansionFrame(f[i]);
        }
        return e;
    }

    public String getMethod() {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "method");
        return a.getDefaultValue();
    }

    public boolean getRetractObsolete() {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "retract-osbolete-actions");
        return Boolean.parseBoolean(a.getDefaultValue());
    }

    public UserRuleFrame[] getSelectionRules() {
        Frame[] f = super.getChildFrames(INST_LIST_SHORTCUTS);
        UserRuleFrame[] res = new UserRuleFrame[f.length];
        for (int i = 0; i < f.length; i++) {
            res[i] = new UserRuleFrame(f[i]);
        }
        return res;
    }

    public double getStateVarMax() {
        DomainAttribute[] da = getAttributes(ATTR_LIST_ATTRS);
        for (int i = 0; i < da.length; i++) {
            if (da[i].getName().equals("state-var-max")) {
                return Double.parseDouble(da[i].getDefaultValue());
            }
        }
        return 10;
    }

    public String[] getStateVars() {
        DomainAttribute[] da = getAttributes(ATTR_LIST_ATTRS);
        for (int i = 0; i < da.length; i++) {
            if (da[i].getName().equals("state-var")) {
                return da[i].getFillers();
            }
        }
        return null;
    }

    public DependencyTestFrame[] getTests() {
        if (getChildFrame(INST_LIST_TESTS, "TestCollection") == null) {
            return new DependencyTestFrame[0];
        }
        DependencyTestDefinitionFrame fr = new DependencyTestDefinitionFrame(getChildFrame(INST_LIST_TESTS, "TestCollection"));
        if (fr != null) {
            return fr.getDependencyTestFrames();
        } else {
            return null;
        }
    }

    public boolean isAveraging() {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "averaging");
        return Boolean.valueOf(a.getDefaultValue()).booleanValue();
    }

    public boolean isComparison() {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "comparison");
        return Boolean.valueOf(a.getDefaultValue()).booleanValue();
    }

    public void removeSelectionRule(UserRuleFrame sc) {
        super.removeChildFrame(INST_LIST_SHORTCUTS, sc.getTypeName());
    }

    public void setAveraging(boolean b) {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "averaging");
        String bool = String.valueOf(b);
        a.setDefault(bool);
        super.addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setAvgBeta(double d) {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "avg-beta");
        String discount = String.valueOf(d);
        a.setDefault(discount);
        super.addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setAvgStepSize(double d) {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "avg-step-size");
        String discount = String.valueOf(d);
        a.setDefault(discount);
        super.addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setComparison(boolean b) {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "comparison");
        String bool = String.valueOf(b);
        a.setDefault(bool);
        super.addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setDefaultReward(double d) {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "defaultReward");
        String r = String.valueOf(d);
        a.setDefault(r);
        super.addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setDiscount(double d) {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "discount");
        String discount = String.valueOf(d);
        a.setDefault(discount);
        super.addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setEvaluationFunction(ConditionFrame f) {
        for (Frame a : super.getChildFrames(RLRuleFrame.INST_LIST_LEARNING)) {
            super.removeChildFrame(RLRuleFrame.INST_LIST_LEARNING, a.getTypeName());
        }

        super.addChildFrame(INST_LIST_LEARNING, f);
    }

    public void setMethod(String s) {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "method");
        a.setDefault(s);
        super.addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setRepeatedExecutionTest(String timeToWait) {
        if (gsim.util.Utils.isNumerical(timeToWait) && Double.parseDouble(timeToWait) <= 0) {
            return;
        }

        DependencyTestFrame t = new DependencyTestFrame("test", "?x1", "?y1", "?z1",
                "(and (eq $?y1 $?y2) (or(>= ?z1 (- $current-time$ (mod $current-time$ " + timeToWait + "))) "
                        + "(>= ?z2 (- $current-time$ (mod $current-time$ " + timeToWait + ")))) )",
                "?x2", "$?y2", "?z2");
        DomainAttribute a = new DomainAttribute("wait", AttributeType.STRING);
        a.setDefault(timeToWait);
        t.addOrSetAttribute("list", a);
        addTest(t);

    }

    public void setRetractObsolete(boolean b) {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "retract-osbolete-actions");
        if (a == null) {
            a = new DomainAttribute("retract-osbolete-actions", AttributeType.STRING);
        }
        a.setDefault(Boolean.toString(b));
        super.addOrSetAttribute(ATTR_LIST_ATTRS, a);
    }

    public void setStateVarMax(double d) {
        DomainAttribute s = this.getAttribute("state-var-max");
        if (s == null) {
            s = new DomainAttribute("state-var-max", AttributeType.SET);
            s.setDefault(String.valueOf(d));
        }
        s.addFiller(String.valueOf(d));

        addOrSetAttribute(ATTR_LIST_ATTRS, s);
    }

    private void init() {

        super.defineObjectList("conditions", ConditionFrame.DEFINITION);
        super.defineObjectList("learning-evaluation", ConditionFrame.DEFINITION);

        super.defineObjectList(INST_LIST_TESTS, DependencyTestDefinitionFrame.DEFINITION);
        super.defineObjectList(INST_LIST_SHORTCUTS, UserRuleFrame.DEFINITION);

        DomainAttribute s = this.getAttribute("state-var");
        if (s == null) {
            s = new DomainAttribute("state-var", AttributeType.SET);
            s.setDefault("");
            addOrSetAttribute(ATTR_LIST_ATTRS, s);
        }

        DomainAttribute averaging = this.getAttribute("averaging");
        if (averaging == null) {
            averaging = new DomainAttribute("averaging", AttributeType.STRING);
            averaging.setDefault("false");
            addOrSetAttribute(ATTR_LIST_ATTRS, averaging);
        }
        DomainAttribute comparison = this.getAttribute("comparison");
        if (comparison == null) {
            comparison = new DomainAttribute("comparison", AttributeType.STRING);
            comparison.setDefault("false");
            addOrSetAttribute(ATTR_LIST_ATTRS, comparison);
        }

        DomainAttribute discount = this.getAttribute("discount");
        if (discount == null) {
            discount = new DomainAttribute("discount", AttributeType.NUMERICAL);
            discount.setDefault("1");
            addOrSetAttribute(ATTR_LIST_ATTRS, discount);
        }
        DomainAttribute avgStep = this.getAttribute("avg-step-size");
        if (avgStep == null) {
            avgStep = new DomainAttribute("avg-step-size", AttributeType.NUMERICAL);
            avgStep.setDefault("0.5");
            addOrSetAttribute(ATTR_LIST_ATTRS, avgStep);
        }
        DomainAttribute beta = this.getAttribute("avg-beta");
        if (beta == null) {
            beta = new DomainAttribute("avg-beta", AttributeType.NUMERICAL);
            beta.setDefault("0.5");
            addOrSetAttribute(ATTR_LIST_ATTRS, beta);
        }
        DomainAttribute method = this.getAttribute("method");
        if (method == null) {
            method = new DomainAttribute("method", AttributeType.STRING);
            method.setDefault("Null");
            addOrSetAttribute(ATTR_LIST_ATTRS, method);
        }
        DomainAttribute defaultReward = this.getAttribute("defaultReward");
        if (defaultReward == null) {
            defaultReward = new DomainAttribute("defaultReward", AttributeType.NUMERICAL);
            defaultReward.setDefault("0.5");
            addOrSetAttribute(ATTR_LIST_ATTRS, defaultReward);
        }

    }

}
