package gsim.objects.impl;

import de.s2.gsim.objects.Condition;
import de.s2.gsim.objects.Expansion;
import de.s2.gsim.objects.GSimObjectException;
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
    public void addOrSetCondition(Condition cond) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        ConditionDef c = new ConditionDef(inst);
        real.setCondition(c);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetConsequent(de.s2.gsim.objects.Action cons) throws GSimObjectException {
        ActionDef a = new ActionDef((Instance) ((UnitWrapper) cons).toUnit());
        real.addConsequence(a);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void addOrSetSelectionNode(SelectionNode sc) throws GSimObjectException {
        ((RLRule) real).removeSelectionRule(((UserRule) ((UnitWrapper) sc).toUnit()));
        ((RLRule) real).addSelectionRule((UserRule) ((UnitWrapper) sc).toUnit());
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public Condition createEvaluator(String paramName, String op, String val) throws GSimObjectException {
        ConditionDef c = real.createCondition(paramName, op, val);
        ((RLRule) real).setEvaluationFunction(c);
        return new ConditionInstance(this, c);
    }

    @Override
    public Expansion createExpansion(String param, String min, String max) throws GSimObjectException {
        ExpansionDef f = new ExpansionDef(param, Double.parseDouble(min), Double.parseDouble(max));
        real.addChildInstance(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionInstance(this, f);
    }

    @Override
    public Expansion createExpansion(String param, String[] fillers) throws GSimObjectException {
        ExpansionDef f = new ExpansionDef(param);
        f.setFillers(fillers);
        real.addChildInstance(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionInstance(this, f);
    }

    @Override
    public SelectionNode createSelectionNode(String name) throws GSimObjectException {
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
    public String getExecutionRestrictionInterval() throws GSimObjectException {
        throw new GSimObjectException("Not implemented");
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
    public Method getMethod() throws GSimObjectException {
        Method m = Method.valueOf(((RLRule) real).getMethod());
        return m;
    }

    @Override
    public String getName() throws GSimObjectException {
        return real.getName();
    }

    @Override
    public POLICY getPolicy() {
        if (((RLRule) real).isComparison()) {
            return POLICY.COMPARISON;
        } else if (!((RLRule) real).isComparison()) {
            return POLICY.SOFTMAX;
        }
        return null;
    }

    @Override
    public SelectionNode getSelectionNode(String name) throws GSimObjectException {
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
    public void removeSelectionNode(SelectionNode sc) throws GSimObjectException {
        ((RLRule) real).removeSelectionRule(((UserRule) ((UnitWrapper) sc).toUnit()));
        owner.addOrSetRLActionNode(this);
    }

    public void setComparisonDiscount(double d) throws GSimObjectException {
        ((RLRule) real).setAvgBeta(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setDiscount(double d) throws GSimObjectException {
        ((RLRule) real).setDiscount(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setEvaluator(Condition f) throws GSimObjectException {
        ConditionDef c = (ConditionDef) ((UnitWrapper) f).toUnit();
        ((RLRule) real).setEvaluationFunction(c);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setExecutionRestrictionInterval(String t) throws GSimObjectException {
        ((RLRule) real).setRepeatedExecutionTest(t);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setGlobalAverageStepSize(double d) throws GSimObjectException {
        ((RLRule) real).setAvgStepSize(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setMethod(Method p) throws GSimObjectException {
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
    public void setPolicy(POLICY p) throws GSimObjectException {
        if (p.equals(POLICY.COMPARISON)) {
            ((RLRule) real).setComparison(true);
        } else if (p.equals(POLICY.SOFTMAX)) {
            ((RLRule) real).setComparison(false);
        }
        owner.addOrSetRLActionNode(this);
    }

    public void setUpdateLag(String s) throws GSimObjectException {
        ((RLRule) real).setUpdateLag(s);
        owner.addOrSetRLActionNode(this);
    }

}
