package de.s2.gsim.api.objects.impl.behaviour;

import java.util.ArrayList;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ActionFrame;
import de.s2.gsim.environment.ConditionFrame;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.RLRuleFrame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.environment.UserRuleFrame;
import de.s2.gsim.objects.Condition;
import de.s2.gsim.objects.Rule;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class RuleClass implements Rule, UnitWrapper {

	protected BehaviourClass owner;

	private UserRuleFrame real;

	protected UserRuleFrame getReal() {
		if (real instanceof RLRuleFrame) {
			real = (UserRuleFrame) ((UnitWrapper) owner.getRLActionNode(real.getName())).toUnit();
		} else {
			real = (UserRuleFrame) ((UnitWrapper) owner.getRule(real.getName())).toUnit();
		}
		return real;
	}

	public RuleClass(BehaviourClass owner, UserRuleFrame real) {
		this.real = real;
		this.owner = owner;
	}

	@Override
	public void addOrSetCondition(Condition cond) throws GSimException {
		ConditionFrame f = (ConditionFrame) ((UnitWrapper) cond).toUnit();
		getReal().removeCondition(f);
		getReal().addCondition(f);
		owner.addOrSetRule(this);
	}

	@Override
	public void addOrSetConsequent(de.s2.gsim.objects.Action cons) throws GSimException {
		getReal().removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
		getReal().addConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
		owner.addOrSetRule(this);
	}

	@Override
	public Condition createCondition(String paramName, String op, String val) throws GSimException {
		ConditionFrame c = getReal().createCondition(paramName, op, val);
		getReal().addCondition(c);
		owner.addOrSetRule(this);
		return new ConditionClass(this, c);
	}

	@Override
	public Condition[] getConditions() {

		ConditionFrame[] c = getReal().getConditions();
		ConditionClass[] ret = new ConditionClass[c.length];
		for (int i = 0; i < c.length; i++) {
			ret[i] = new ConditionClass(this, c[i]);
		}
		return ret;
	}

	@Override
	public de.s2.gsim.objects.Action getConsequent(String name) {
		ActionFrame[] c = getReal().getConsequents();
		for (int i = 0; i < c.length; i++) {
			if (c[i].getName().equals(name)) {
				return new ActionClass(this, c[i]);
			}
		}
		return null;
	}

	@Override
	public de.s2.gsim.objects.Action[] getConsequents() {
		ArrayList<de.s2.gsim.objects.Action> list = new ArrayList<>();

		ActionFrame[] c = getReal().getConsequents();
		for (int i = 0; i < c.length; i++) {
			if (!c[i].getName().startsWith("{")) {
				list.add(new ActionClass(this, c[i]));
			}
		}

		de.s2.gsim.objects.Action[] ret = new ActionClass[list.size()];
		list.toArray(ret);
		return ret;
	}

	@Override
	public String getName() throws GSimException {
		return getReal().getName();
	}

	@Override
	public boolean isActivated() {
		return getReal().isActivated();
	}

	@Override
	public void removeCondition(Condition cond) throws GSimException {
		getReal().removeCondition((ConditionFrame) ((UnitWrapper) cond).toUnit());
		owner.addOrSetRule(this);
	}

	@Override
	public void removeConsequent(de.s2.gsim.objects.Action cons) throws GSimException {
		getReal().removeConsequence((ActionFrame) ((UnitWrapper) cons).toUnit());
		owner.addOrSetRule(this);
	}

	@Override
	public void setActivated(boolean b) throws GSimException {
		getReal().setActivated(b);
		owner.addOrSetRule(this);
	}

	@Override
	public Unit<Frame, DomainAttribute> toUnit() {
		return real;
	}

}
