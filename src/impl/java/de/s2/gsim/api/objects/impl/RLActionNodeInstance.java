package de.s2.gsim.api.objects.impl;

import de.s2.gsim.GSimException;
import de.s2.gsim.objects.Condition;
import de.s2.gsim.objects.Expansion;
import de.s2.gsim.objects.RLActionNode;
import de.s2.gsim.objects.SelectionNode;
import gsim.def.objects.Instance;
import gsim.def.objects.behaviour.ActionDef;
import gsim.def.objects.behaviour.ConditionDef;
import gsim.def.objects.behaviour.ExpansionDef;
import gsim.def.objects.behaviour.RLRule;
import gsim.def.objects.behaviour.RLRuleFrame;
import gsim.def.objects.behaviour.UserRule;
import gsim.def.objects.behaviour.UserRuleFrame;

public class RLActionNodeInstance extends RuleInstance implements RLActionNode, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public RLActionNodeInstance(BehaviourInstance owner, RLRule c) {
        super(owner, c);
    }

    @Override
    public void addOrSetCondition(Condition cond) throws GSimException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        ConditionDef c = new ConditionDef(inst);
        real.setCondition(c);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetConsequent(de.s2.gsim.objects.Action cons) throws GSimException {
        ActionDef a = new ActionDef((Instance) ((UnitWrapper) cons).toUnit());
        real.addConsequence(a);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void addOrSetSelectionNode(SelectionNode sc) throws GSimException {
        ((RLRule) real).removeSelectionRule(((UserRule) ((UnitWrapper) sc).toUnit()));
        ((RLRule) real).addSelectionRule((UserRule) ((UnitWrapper) sc).toUnit());
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public Condition createEvaluator(String paramName, String op, String val) throws GSimException {
        ConditionDef c = real.createCondition(paramName, op, val);
        ((RLRule) real).setEvaluationFunction(c);
        return new ConditionInstance(this, c);
    }

    @Override
    public Expansion createExpansion(String param, String min, String max) throws GSimException {
        ExpansionDef f = new ExpansionDef(param, Double.parseDouble(min), Double.parseDouble(max));
        real.addChildInstance(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionInstance(this, f);
    }

    @Override
    public Expansion createExpansion(String param, String[] fillers) throws GSimException {
        ExpansionDef f = new ExpansionDef(param);
        f.setFillers(fillers);
        real.addChildInstance(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionInstance(this, f);
    }

    @Override
    public SelectionNode createSelectionNode(String name) throws GSimException {
        return new SelectionNodeInstance(this, new UserRule(new UserRuleFrame("shortcut-frame"), name));
    }

    public double getComparisonDiscount() {
        return ((RLRule) real).getAvgBeta();
    }

    @Override
    public double getDiscount() {
        return ((RLRule) real).getDiscount();
    }

    @Override
    public Condition getEvaluator() {
        ConditionDef f = ((RLRule) real).getEvaluationFunction();
        if (f != null) {
            return new EvaluatorInstance(this, f);
        }
        return null;
    }

    @Override
    public String getExecutionRestrictionInterval() throws GSimException {
        throw new GSimException("Not implemented");
    }

    @Override
    public Expansion[] getExpansions() {
        ExpansionDef[] f = ((RLRule) real).getExpansions();
        Expansion[] ff = new Expansion[f.length];
        for (int i = 0; i < f.length; i++) {
            ff[i] = new ExpansionInstance(this, f[i]);
        }
        return ff;
    }

    @Override
    public double getGlobalAverageStepSize() {
        return ((RLRule) real).getAvgStepSize();
    }

    @Override
    public Method getMethod() throws GSimException {
        Method m = Method.valueOf(((RLRule) real).getMethod());
        return m;
    }

    @Override
    public String getName() throws GSimException {
        return real.getName();
    }

    @Override
    public Policy getPolicy() {
        if (((RLRule) real).isComparison()) {
            return Policy.COMPARISON;
        } else if (!((RLRule) real).isComparison()) {
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
        UserRule[] r = ((RLRule) real).getShortSelectionRules();
        SelectionNodeInstance[] sc = new SelectionNodeInstance[r.length];
        for (int i = 0; i < sc.length; i++) {
            sc[i] = new SelectionNodeInstance(this, r[i]);
        }
        return sc;
    }

    public String getUpdateLag() {
        return ((RLRule) real).getUpdateLag();
    }

    @Override
    public void removeSelectionNode(SelectionNode sc) throws GSimException {
        ((RLRule) real).removeSelectionRule(((UserRule) ((UnitWrapper) sc).toUnit()));
        owner.addOrSetRLActionNode(this);
    }

    public void setComparisonDiscount(double d) throws GSimException {
        ((RLRule) real).setAvgBeta(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setDiscount(double d) throws GSimException {
        ((RLRule) real).setDiscount(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setEvaluator(Condition f) throws GSimException {
        ConditionDef c = (ConditionDef) ((UnitWrapper) f).toUnit();
        ((RLRule) real).setEvaluationFunction(c);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setExecutionRestrictionInterval(String t) throws GSimException {
        ((RLRule) real).setRepeatedExecutionTest(t);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setGlobalAverageStepSize(double d) throws GSimException {
        ((RLRule) real).setAvgStepSize(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setMethod(Method p) throws GSimException {
        if (p.equals(Method.NULL)) {
            ((RLRule) real).setMethod("Null");
            owner.addOrSetRLActionNode(this);
        }
        if (p.equals(Method.Q)) {
            ((RLRule) real).setMethod("Q");
            owner.addOrSetRLActionNode(this);
        }

    }

    @Override
    public void setPolicy(Policy p) throws GSimException {
        if (p.equals(Policy.COMPARISON)) {
            ((RLRule) real).setComparison(true);
        } else if (p.equals(Policy.SOFTMAX)) {
            ((RLRule) real).setComparison(false);
        }
        owner.addOrSetRLActionNode(this);
    }

    public void setUpdateLag(String s) throws GSimException {
        ((RLRule) real).setUpdateLag(s);
        owner.addOrSetRLActionNode(this);
    }

}
