package de.s2.gsim.api.objects.impl;

import de.s2.gsim.GSimException;
import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.behaviour.ActionFrame;
import de.s2.gsim.def.objects.behaviour.ConditionFrame;
import de.s2.gsim.def.objects.behaviour.ExpansionFrame;
import de.s2.gsim.def.objects.behaviour.RLRuleFrame;
import de.s2.gsim.def.objects.behaviour.UserRuleFrame;
import de.s2.gsim.objects.Condition;
import de.s2.gsim.objects.Expansion;
import de.s2.gsim.objects.RLActionNode;
import de.s2.gsim.objects.SelectionNode;

public class RLActionNodeClass extends RuleClass implements RLActionNode, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public RLActionNodeClass(BehaviourClass owner, RLRuleFrame c) {
        super(owner, c);
    }

    @Override
    public void addOrSetCondition(Condition cond) throws GSimException {
        ConditionFrame f = (ConditionFrame) ((UnitWrapper) cond).toUnit();
        real.removeCondition(f);
        real.addCondition(f);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void addOrSetConsequent(de.s2.gsim.objects.Action cons) throws GSimException {
        real.removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        real.addConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void addOrSetExpansion(Expansion cond) throws GSimException {
        FrameOLD inst = (FrameOLD) ((UnitWrapper) cond).toUnit();
        real.addChildFrame(RLRuleFrame.INST_LIST_EXP, inst);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetSelectionNode(SelectionNode sc) throws GSimException {
        ((RLRuleFrame) real).removeSelectionRule(((UserRuleFrame) ((UnitWrapper) sc).toUnit()));
        ((RLRuleFrame) real).addSelectionRule((UserRuleFrame) ((UnitWrapper) sc).toUnit());
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public Condition createEvaluator(String paramName, String op, String val) throws GSimException {
        ConditionFrame c = real.createCondition(paramName, op, val);
        ((RLRuleFrame) real).setEvaluationFunction(c);
        return new ConditionClass(this, c);
    }

    @Override
    public Expansion createExpansion(String param, String min, String max) throws GSimException {

        ExpansionFrame f = null;
        // f = real.getChildFrame(RLRuleFrame.INST_LIST_EXP, param);

        f = new ExpansionFrame(param, min, max);

        real.addChildFrame(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionClass(this, f);
    }

    @Override
    public Expansion createExpansion(String param, String[] fillers) throws GSimException {
        ExpansionFrame f = new ExpansionFrame(param);
        f.setFillers(fillers);
        real.addChildFrame(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionClass(this, f);
    }

    @Override
    public SelectionNode createSelectionNode(String name) throws GSimException {
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
    public Condition getEvaluator() throws GSimException {
        ConditionFrame f = ((RLRuleFrame) real).getEvaluationFunction();
        if (f != null) {
            return new EvaluatorClass(this, f);
        } else {
            Condition e = new EvaluatorClass(this, new ConditionFrame(new String("dummy")));
            setEvaluator(e);
            return e;
        }
    }

    @Override
    public String getExecutionRestrictionInterval() throws GSimException {
        return ((RLRuleFrame) real).getExecutionIntervalTest();
    }

    @Override
    public Expansion[] getExpansions() {
        ExpansionFrame[] f = ((RLRuleFrame) real).getExpansions();
        Expansion[] ff = new Expansion[f.length];
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
    public Method getMethod() throws GSimException {
        Method m = Method.valueOf(((RLRuleFrame) real).getMethod());
        return m;
    }

    @Override
    public String getName() throws GSimException {
        return real.getTypeName();
    }

    @Override
    public Policy getPolicy() {
        if (((RLRuleFrame) real).isComparison()) {
            return Policy.COMPARISON;
        } else if (!((RLRuleFrame) real).isComparison()) {
            return Policy.SOFTMAX;
        }
        return null;
    }

    @Override
    public SelectionNode getSelectionNode(String name) throws GSimException {
        for (SelectionNode c : getSelectionNodes()) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public SelectionNode[] getSelectionNodes() {
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
    public void removeExpansion(Expansion cond) throws GSimException {
        FrameOLD inst = (FrameOLD) ((UnitWrapper) cond).toUnit();
        real.removeChildFrame(RLRuleFrame.INST_LIST_EXP, inst.getTypeName());
        owner.addOrSetRule(this);
    }

    @Override
    public void removeSelectionNode(SelectionNode sc) throws GSimException {
        ((RLRuleFrame) real).removeSelectionRule(((UserRuleFrame) ((UnitWrapper) sc).toUnit()));
        owner.addOrSetRLActionNode(this);
    }

    public void setComparisonDiscount(double d) throws GSimException {
        ((RLRuleFrame) real).setAvgBeta(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setDiscount(double d) throws GSimException {
        ((RLRuleFrame) real).setDiscount(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setEvaluator(Condition f) throws GSimException {
        ConditionFrame c = (ConditionFrame) ((UnitWrapper) f).toUnit();
        ((RLRuleFrame) real).setEvaluationFunction(c);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setExecutionRestrictionInterval(String t) throws GSimException {
        ((RLRuleFrame) real).setRepeatedExecutionTest(t);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setGlobalAverageStepSize(double d) throws GSimException {
        ((RLRuleFrame) real).setAvgStepSize(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setMethod(Method p) throws GSimException {
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
    public void setPolicy(Policy p) throws GSimException {
        if (p.equals(Policy.COMPARISON)) {
            ((RLRuleFrame) real).setComparison(true);
        } else if (p.equals(Policy.SOFTMAX)) {
            ((RLRuleFrame) real).setComparison(false);
        }
        owner.addOrSetRLActionNode(this);
    }

    public void setUpdateLag(String s) throws GSimException {
        ((RLRuleFrame) real).setUpdateLag(s);
        owner.addOrSetRLActionNode(this);
    }
}
