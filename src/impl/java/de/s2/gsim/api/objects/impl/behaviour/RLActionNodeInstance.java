package de.s2.gsim.api.objects.impl.behaviour;

import java.util.List;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.RLRuleFrame;
import de.s2.gsim.objects.Evaluator;
import de.s2.gsim.objects.Expansion;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.RLActionNode;
import de.s2.gsim.objects.attribute.Attribute;

public class RLActionNodeInstance extends RuleInstance implements RLActionNode, UnitWrapper {

    public RLActionNodeInstance(BehaviourInstance owner, RLRule c) {
        super(owner, c);
    }

    @Override
	public void addOrSetCondition(Path<Attribute> path, String op, String val) throws GSimException {
		ConditionDef c = new ConditionDef(path.toString(), op, val);
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
	public Evaluator createEvaluator(String paramName, double val) throws GSimException {
		ConditionDef c = real.createCondition(paramName, "", String.valueOf(val));
        ((RLRule) real).setEvaluationFunction(c);
		return new EvaluatorInstance(c);
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

    public double getComparisonDiscount() {
        return ((RLRule) real).getAvgBeta();
    }

    @Override
    public double getDiscount() {
        return ((RLRule) real).getDiscount();
    }

    @Override
	public Evaluator getEvaluator() {
        ConditionDef f = ((RLRule) real).getEvaluationFunction();
        if (f != null) {
			return new EvaluatorInstance(f);
        }
        return null;
    }

    @Override
    public String getExecutionRestrictionInterval() throws GSimException {
        throw new GSimException("Not implemented");
    }

    @Override
    public Expansion[] getExpansions() {
		List<ExpansionDef> f = ((RLRule) real).getExpansions();
		Expansion[] ff = new Expansion[f.size()];
		for (int i = 0; i < f.size(); i++) {
			ff[i] = new ExpansionInstance(this, f.get(i));
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

    public String getUpdateLag() {
        return ((RLRule) real).getUpdateLag();
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
	public void setEvaluator(Evaluator f) throws GSimException {
        ConditionDef c = (ConditionDef) ((UnitWrapper) f).toUnit();
        ((RLRule) real).setEvaluationFunction(c);
        owner.addOrSetRLActionNode(this);
    }

    @Override
    public void setExecutionRestrictionInterval(String t) throws GSimException {
        throw new UnsupportedOperationException("Method has been removed because it seemed to be never used.");
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
