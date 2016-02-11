package gsim.objects.impl;

import de.s2.gsim.objects.ActionIF;
import de.s2.gsim.objects.ConditionIF;
import de.s2.gsim.objects.ExpansionIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.RLActionNodeIF;
import de.s2.gsim.objects.SelectionNodeIF;
import gsim.def.objects.Instance;
import gsim.def.objects.behaviour.Action;
import gsim.def.objects.behaviour.Condition;
import gsim.def.objects.behaviour.Expansion;
import gsim.def.objects.behaviour.RLRule;
import gsim.def.objects.behaviour.RLRuleFrame;
import gsim.def.objects.behaviour.UserRule;
import gsim.def.objects.behaviour.UserRuleFrame;

public class RLActionNodeInstance extends RuleInstance implements RLActionNodeIF, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public RLActionNodeInstance(BehaviourInstance owner, RLRule c) {
        super(owner, c);
    }

    @Override
    public void addOrSetCondition(ConditionIF cond) throws GSimObjectException {
        Instance inst = (Instance) ((UnitWrapper) cond).toUnit();
        Condition c = new Condition(inst);
        real.setCondition(c);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetConsequent(ActionIF cons) throws GSimObjectException {
        Action a = new Action((Instance) ((UnitWrapper) cons).toUnit());
        real.addConsequence(a);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void addOrSetSelectionNode(SelectionNodeIF sc) throws GSimObjectException {
        ((RLRule) real).removeSelectionRule(((UserRule) ((UnitWrapper) sc).toUnit()));
        ((RLRule) real).addSelectionRule((UserRule) ((UnitWrapper) sc).toUnit());
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public ConditionIF createEvaluator(String paramName, String op, String val) throws GSimObjectException {
        Condition c = real.createCondition(paramName, op, val);
        ((RLRule) real).setEvaluationFunction(c);
        return new ConditionInstance(this, c);
    }

    @Override
    public ExpansionIF createExpansion(String param, String min, String max) throws GSimObjectException {
        Expansion f = new Expansion(param, Double.parseDouble(min), Double.parseDouble(max));
        real.addChildInstance(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionInstance(this, f);
    }

    @Override
    public ExpansionIF createExpansion(String param, String[] fillers) throws GSimObjectException {
        Expansion f = new Expansion(param);
        f.setFillers(fillers);
        real.addChildInstance(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionInstance(this, f);
    }

    @Override
    public SelectionNodeIF createSelectionNode(String name) throws GSimObjectException {
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
    public ConditionIF getEvaluator() {
        Condition f = ((RLRule) real).getEvaluationFunction();
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
    public ExpansionIF[] getExpansions() {
        Expansion[] f = ((RLRule) real).getExpansions();
        ExpansionIF[] ff = new ExpansionIF[f.length];
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
    public void removeSelectionNode(SelectionNodeIF sc) throws GSimObjectException {
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
    public void setEvaluator(ConditionIF f) throws GSimObjectException {
        Condition c = (Condition) ((UnitWrapper) f).toUnit();
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
