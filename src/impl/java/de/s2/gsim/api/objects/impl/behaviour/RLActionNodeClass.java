package de.s2.gsim.api.objects.impl.behaviour;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ActionFrame;
import de.s2.gsim.environment.ConditionFrame;
import de.s2.gsim.environment.ExpansionFrame;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.RLRuleFrame;
import de.s2.gsim.environment.UserRuleFrame;
import de.s2.gsim.objects.Condition;
import de.s2.gsim.objects.Evaluator;
import de.s2.gsim.objects.Expansion;
import de.s2.gsim.objects.RLActionNode;
import de.s2.gsim.objects.SelectionNode;
//RLActionNode is WRONG
public class RLActionNodeClass extends RuleClass implements RLActionNode, UnitWrapper {

    public RLActionNodeClass(BehaviourClass owner, RLRuleFrame c) {
        super(owner, c);
    }

    @Override
    public void addOrSetCondition(Condition cond) throws GSimException {
        ConditionFrame f = (ConditionFrame) ((UnitWrapper) cond).toUnit();
        getReal().removeCondition(f);
        getReal().addCondition(f);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void addOrSetConsequent(de.s2.gsim.objects.Action cons) throws GSimException {
        getReal().removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        getReal().addConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void addOrSetExpansion(Expansion cond) throws GSimException {
        Frame inst = (Frame) ((UnitWrapper) cond).toUnit();
        getReal().addOrSetChildFrame(RLRuleFrame.INST_LIST_EXP, inst);
        owner.addOrSetRule(this);
    }

    @Override
    public void addOrSetSelectionNode(SelectionNode sc) throws GSimException {
        ((RLRuleFrame) getReal()).removeSelectionRule(((UserRuleFrame) ((UnitWrapper) sc).toUnit()));
        ((RLRuleFrame) getReal()).addSelectionRule((UserRuleFrame) ((UnitWrapper) sc).toUnit());
        owner.addOrSetRLActionNode(this);
    }

    @Override
	public Evaluator createEvaluator(String paramName, double val) throws GSimException {
		ConditionFrame c = getReal().createCondition(paramName, "", String.valueOf(val));
        ((RLRuleFrame) getReal()).setEvaluationFunction(c);
        owner.addOrSetRLActionNode(this);
		return new EvaluatorClass(c);
    }

    @Override
    public Expansion createExpansion(String param, String min, String max) throws GSimException {

        ExpansionFrame f = null;
        // f = getReal().getChildFrame(RLRuleFrame.INST_LIST_EXP, param);

        f = new ExpansionFrame(param, min, max);

        getReal().addOrSetChildFrame(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionClass(this, f);
    }

    @Override
    public Expansion createExpansion(String param, String[] fillers) throws GSimException {
        ExpansionFrame f = new ExpansionFrame(param);
        f.setFillers(fillers);
        getReal().addOrSetChildFrame(RLRuleFrame.INST_LIST_EXP, f);
        owner.addOrSetRule(this);
        return new ExpansionClass(this, f);
    }

    public double getComparisonDiscount() {
        return ((RLRuleFrame) getReal()).getAvgBeta();
    }

    @Override
    public double getDiscount() {
        return ((RLRuleFrame) getReal()).getDiscount();
    }

    @Override
	public Evaluator getEvaluator() throws GSimException {
        ConditionFrame f = ((RLRuleFrame) getReal()).getEvaluationFunction();
        if (f != null) {
			return new EvaluatorClass(f);
        } else {
			Evaluator e = new EvaluatorClass(ConditionFrame.newConditionFrame(new String("dummy")));
            setEvaluator(e);
            return e;
        }
    }

    @Override
    public String getExecutionRestrictionInterval() throws GSimException {
        throw new UnsupportedOperationException("Method has been removed because it seemed never to be used.");
    }

    @Override
    public Expansion[] getExpansions() {
        ExpansionFrame[] f = ((RLRuleFrame) getReal()).getExpansions();
        Expansion[] ff = new Expansion[f.length];
        for (int i = 0; i < f.length; i++) {
            ff[i] = new ExpansionClass(this, f[i]);
        }
        return ff;
    }

    @Override
    public double getGlobalAverageStepSize() {
        return ((RLRuleFrame) getReal()).getAvgStepSize();
    }

    @Override
    public Method getMethod() throws GSimException {
        Method m = Method.valueOf(((RLRuleFrame) getReal()).getMethod());
        return m;
    }

    @Override
    public String getName() throws GSimException {
        return getReal().getName();
    }

    @Override
    public Policy getPolicy() {
        if (((RLRuleFrame) getReal()).isComparison()) {
            return Policy.COMPARISON;
        } else if (!((RLRuleFrame) getReal()).isComparison()) {
            return Policy.SOFTMAX;
        }
        return null;
    }

    public String getUpdateLag() {
        return ((RLRuleFrame) getReal()).getUpdateLag();
    }

    @Override
    public void removeExpansion(Expansion cond) throws GSimException {
        Frame inst = (Frame) ((UnitWrapper) cond).toUnit();
        getReal().removeChildFrame(RLRuleFrame.INST_LIST_EXP, inst.getName());
        owner.addOrSetRule(this);
    }

    @Override
    public void removeSelectionNode(SelectionNode sc) throws GSimException {
        ((RLRuleFrame) getReal()).removeSelectionRule(((UserRuleFrame) ((UnitWrapper) sc).toUnit()));
        owner.addOrSetRLActionNode(this);
    }

    public void setComparisonDiscount(double d) throws GSimException {
        ((RLRuleFrame) getReal()).setAvgBeta(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setDiscount(double d) throws GSimException {
        ((RLRuleFrame) getReal()).setDiscount(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setEvaluator(Evaluator f) throws GSimException {
		Frame c = (ConditionFrame) ((UnitWrapper) f).toUnit();
        ((RLRuleFrame) getReal()).setEvaluationFunction(c);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setExecutionRestrictionInterval(String t) throws GSimException {
        throw new UnsupportedOperationException("Method has been removed because it seemed never to be used.");
    }

    @Override
    public void setGlobalAverageStepSize(double d) throws GSimException {
        ((RLRuleFrame) getReal()).setAvgStepSize(d);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setMethod(Method p) throws GSimException {
        if (p.equals(Method.NULL)) {
            ((RLRuleFrame) getReal()).setMethod("Null");
            owner.addOrSetRLActionNode(this);
        }
        if (p.equals(Method.Q)) {
            ((RLRuleFrame) getReal()).setMethod("Q");
            owner.addOrSetRLActionNode(this);
        }

    }

    @Override
    public void setPolicy(Policy p) throws GSimException {
        if (p.equals(Policy.COMPARISON)) {
            ((RLRuleFrame) getReal()).setComparison(true);
        } else if (p.equals(Policy.SOFTMAX)) {
            ((RLRuleFrame) getReal()).setComparison(false);
        }
        owner.addOrSetRLActionNode(this);
    }

    public void setUpdateLag(String s) throws GSimException {
        ((RLRuleFrame) getReal()).setUpdateLag(s);
        owner.addOrSetRLActionNode(this);
    }
}
