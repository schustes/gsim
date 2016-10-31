package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.log4j.Logger;

import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class RLRuleFrame extends UserRuleFrame {

    public static final String INST_LIST_EXP = "expansions";

    public static final String INST_LIST_LEARNING = "learning-evaluation";

    public static final String INST_LIST_SHORTCUTS = "rules helping to reduce the action space of this node";

    public static final String INST_LIST_TESTS = "tests";

   // public static RLRuleFrame RL_RULE_FRAME = new RLRuleFrame("template-rl-frame");

    private static Logger logger = Logger.getLogger(RLRuleFrame.class);

    private RLRuleFrame(String name, Frame... f) {
        super(name, f);
    }
    public static RLRuleFrame inherit1(Frame f) {
        RLRuleFrame rf = new RLRuleFrame(f.getName(), f);
        return rf;
    }

    /**
     * Wrap an existing frame.
     */
    public static RLRuleFrame wrap(Frame orig) {
    	return wrap(orig, orig.getName());
    }

    public static RLRuleFrame inherit(String name, Frame... parents) {
    	Objects.requireNonNull(parents);
    	Frame f = inherit(Arrays.asList(parents), name, Optional.empty());
        RLRuleFrame ff = new RLRuleFrame(f.getName(), f);
        ff.init();
    	return ff;
    }

    public static RLRuleFrame wrap(Frame orig, String newName) {
        RLRuleFrame ur = new RLRuleFrame(newName, orig.getParentFrames().toArray(new Frame[0]));
        Frame.copyInternal(orig, ur);
        return ur;
    }

    public static RLRuleFrame inheritFromRLRuleFrames(List<? extends UserRuleFrame> parents, String name, String category) {
        Frame f = Frame.inherit(parents, name, Optional.of(category));
        RLRuleFrame ff = new RLRuleFrame(f.getName(), f);
        ff.init();
        return ff;
    }

    public static RLRuleFrame newRLRuleFrame(String name) {
    	Frame f = Frame.newFrame(name, Optional.of(CATEGORY));
        RLRuleFrame rf = new RLRuleFrame(f.getName(), f);
        rf.init();
        return rf;
    }

    public void addExpansion(ExpansionFrame cond) {
        super.addOrSetChildFrame(INST_LIST_EXP, cond);
    }

    public void addSelectionRule(UserRuleFrame sc) {
        super.addOrSetChildFrame(INST_LIST_SHORTCUTS, sc);
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

    @Override
    public Frame clone() {
        return RLRuleFrame.wrap(this);
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
        ArrayList<ConditionFrame> list = new ArrayList<>();
        for (Frame f: getChildFrames(INST_LIST_LEARNING)) {
            if (!f.getName().startsWith("{")) {
                list.add(ConditionFrame.copyAndWrap(f));
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

    public ExpansionFrame[] getExpansions() {
        List<Frame> f = super.getChildFrames(INST_LIST_EXP);
        ExpansionFrame[] e = new ExpansionFrame[f.size()];
        for (int i = 0; i < f.size(); i++) {
            e[i] = new ExpansionFrame(f.get(i));
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
        List<Frame> f = getChildFrames(INST_LIST_SHORTCUTS);
        UserRuleFrame[] res = new UserRuleFrame[f.size()];
        for (int i = 0; i < f.size(); i++) {
            res[i] = UserRuleFrame.wrap(f.get(i));
        }
        return res;
    }

    public double getStateVarMax() {
        for (DomainAttribute a: getAttributes(ATTR_LIST_ATTRS)) {
            if (a.getName().equals("state-var-max")) {
                return Double.parseDouble(a.getDefaultValue());
            }
        }
        return 10;
    }

    public List<String> getStateVars() {
        for (DomainAttribute a: getAttributes(ATTR_LIST_ATTRS)) {
            if (a.getName().equals("state-var")) {
                return a.getFillers();
            }
        }
        throw new GSimDefException("The required attribute list " + ATTR_LIST_ATTRS + " is not defined for RLRuleFrame!");
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
        super.removeChildFrame(INST_LIST_SHORTCUTS, sc.getName());
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
            super.removeChildFrame(RLRuleFrame.INST_LIST_LEARNING, a.getName());
        }

        super.addOrSetChildFrame(INST_LIST_LEARNING, f);
    }

    public void setMethod(String s) {
        DomainAttribute a = super.getAttribute(ATTR_LIST_ATTRS, "method");
        a.setDefault(s);
        super.addOrSetAttribute(ATTR_LIST_ATTRS, a);
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
