package gsim.objects.impl;

import de.s2.gsim.objects.ActionIF;
import de.s2.gsim.objects.ConditionIF;
import de.s2.gsim.objects.ExpansionIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.RLActionNodeIF;
import de.s2.gsim.objects.SelectionNodeIF;
import gsim.def.objects.Frame;
import gsim.def.objects.behaviour.ActionFrame;
import gsim.def.objects.behaviour.ConditionFrame;
import gsim.def.objects.behaviour.ExpansionFrame;
import gsim.def.objects.behaviour.RLRuleFrame;
import gsim.def.objects.behaviour.UserRuleFrame;

public class RLActionNodeClass extends RuleClass implements RLActionNodeIF, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public RLActionNodeClass(BehaviourClass owner, RLRuleFrame c) {
        super(owner, c);
    }

    @Override
    public void addOrSetCondition(ConditionIF cond) throws GSimObjectException {
        ConditionFrame f = (ConditionFrame) ((UnitWrapper) cond).toUnit();
        real.removeCondition(f);
        real.addCondition(f);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void addOrSetConsequent(ActionIF cons) throws GSimObjectException {
        real.removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        real.addConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void addOrSetExpansion(ExpansionIF cond) throws GSimObjectException {
        Frame inst = (Frame) ((UnitWrapper) cond).toUnit();
        real.addChildFrame(RLRuleFrame.INST_LIST_EXP, inst);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetSelectionNode(SelectionNodeIF sc) throws GSimObjectException {
        ((RLRuleFrame) real).removeSelectionRule(((UserRuleFrame) ((UnitWrapper) sc).toUnit()));
        ((RLRuleFrame) real).addSelectionRule((UserRuleFrame) ((UnitWrapper) sc).toUnit());
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public ConditionIF createEvaluator(String paramName, String op, String val) throws GSimObjectException {
        ConditionFrame c = real.createCondition(paramName, op, val);
        ((RLRuleFrame) real).setEvaluationFunction(c);
        return new ConditionClass(this, c);
    }

    @Override
    public ExpansionIF createExpansion(String param, String min, String max) throws GSimObjectException {

        ExpansionFrame f = null;
        // f = real.getChildFrame(RLRuleFrame.INST_LIST_EXP, param);

        f = new ExpansionFrame(param, min, max);

        real.addChildFrame(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionClass(this, f);
    }

    @Override
    public ExpansionIF createExpansion(String param, String[] fillers) throws GSimObjectException {
        ExpansionFrame f = new ExpansionFrame(param);
        f.setFillers(fillers);
        real.addChildFrame(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionClass(this, f);
    }

    @Override
    public SelectionNodeIF createSelectionNode(String name) throws GSimObjectException {
        UserRuleFrame f = new UserRuleFrame(name);
        return new SelectionNodeClass(this, f);
    }

    public double getComparisonDiscount() {
        return ((RLRuleFrame) real).getAvgBeta();
    }

    @Override
    public double getDiscount() {
        return ((RLRuleFrame) real).getDiscount();
    }

    @Override
    public ConditionIF getEvaluator() throws GSimObjectException {
        ConditionFrame f = ((RLRuleFrame) real).getEvaluationFunction();
        if (f != null) {
            return new EvaluatorClass(this, f);
        } else {
            ConditionIF e = new EvaluatorClass(this, new ConditionFrame(new String("dummy")));
            setEvaluator(e);
            return e;
        }
    }

    @Override
    public String getExecutionRestrictionInterval() throws GSimObjectException {
        return ((RLRuleFrame) real).getExecutionIntervalTest();
    }

    @Override
    public ExpansionIF[] getExpansions() {
        ExpansionFrame[] f = ((RLRuleFrame) real).getExpansions();
        ExpansionIF[] ff = new ExpansionIF[f.length];
        for (int i = 0; i < f.length; i++) {
            ff[i] = new ExpansionClass(this, f[i]);
        }
        return ff;
    }

    @Override
    public double getGlobalAverageStepSize() {
        return ((RLRuleFrame) real).getAvgStepSize();
    }

    @Override
    public Method getMethod() throws GSimObjectException {
        Method m = Method.valueOf(((RLRuleFrame) real).getMethod());
        return m;
    }

    @Override
    public String getName() throws GSimObjectException {
        return real.getTypeName();
    }

    @Override
    public POLICY getPolicy() {
        if (((RLRuleFrame) real).isComparison()) {
            return POLICY.COMPARISON;
        } else if (!((RLRuleFrame) real).isComparison()) {
            return POLICY.SOFTMAX;
        }
        return null;
    }

    @Override
    public SelectionNodeIF getSelectionNode(String name) throws GSimObjectException {
        for (SelectionNodeIF c : getSelectionNodes()) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public SelectionNodeIF[] getSelectionNodes() {
        UserRuleFrame[] r = ((RLRuleFrame) real).getSelectionRules();
        SelectionNodeClass[] sc = new SelectionNodeClass[r.length];
        for (int i = 0; i < sc.length; i++) {
            sc[i] = new SelectionNodeClass(this, r[i]);
        }
        return sc;
    }

    public String getUpdateLag() {
        return ((RLRuleFrame) real).getUpdateLag();
    }

    @Override
    public void removeExpansion(ExpansionIF cond) throws GSimObjectException {
        Frame inst = (Frame) ((UnitWrapper) cond).toUnit();
        real.removeChildFrame(RLRuleFrame.INST_LIST_EXP, inst.getTypeName());
        owner.addOrSetRule(this);
    }

    @Override
    public void removeSelectionNode(SelectionNodeIF sc) throws GSimObjectException {
        ((RLRuleFrame) real).removeSelectionRule(((UserRuleFrame) ((UnitWrapper) sc).toUnit()));
        owner.addOrSetRLActionNode(this);
    }

    public void setComparisonDiscount(double d) throws GSimObjectException {
        ((RLRuleFrame) real).setAvgBeta(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setDiscount(double d) throws GSimObjectException {
        ((RLRuleFrame) real).setDiscount(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setEvaluator(ConditionIF f) throws GSimObjectException {
        ConditionFrame c = (ConditionFrame) ((UnitWrapper) f).toUnit();
        ((RLRuleFrame) real).setEvaluationFunction(c);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setExecutionRestrictionInterval(String t) throws GSimObjectException {
        ((RLRuleFrame) real).setRepeatedExecutionTest(t);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setGlobalAverageStepSize(double d) throws GSimObjectException {
        ((RLRuleFrame) real).setAvgStepSize(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setMethod(Method p) throws GSimObjectException {
        if (p.equals(Method.NULL)) {
            ((RLRuleFrame) real).setMethod("Null");
            owner.addOrSetRLActionNode(this);
        }
        if (p.equals(Method.Q)) {
            ((RLRuleFrame) real).setMethod("Q");
            owner.addOrSetRLActionNode(this);
        }

    }

    @Override
    public void setPolicy(POLICY p) throws GSimObjectException {
        if (p.equals(POLICY.COMPARISON)) {
            ((RLRuleFrame) real).setComparison(true);
        } else if (p.equals(POLICY.SOFTMAX)) {
            ((RLRuleFrame) real).setComparison(false);
        }
        owner.addOrSetRLActionNode(this);
    }

    public void setUpdateLag(String s) throws GSimObjectException {
        ((RLRuleFrame) real).setUpdateLag(s);
        owner.addOrSetRLActionNode(this);
    }
}
