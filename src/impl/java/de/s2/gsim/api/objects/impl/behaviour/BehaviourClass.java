package de.s2.gsim.api.objects.impl.behaviour;

import java.util.ArrayList;

import de.s2.gsim.GSimException;
import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ActionFrame;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.RLRuleFrame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.environment.UserRuleFrame;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.RLActionNode;
import de.s2.gsim.objects.Rule;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class BehaviourClass implements Behaviour, UnitWrapper {

	public static final long serialVersionUID = 1L;

    private AgentClass owner;

    private BehaviourFrame real;

    public BehaviourClass(AgentClass owner, BehaviourFrame real) {
        this.real = real;
        this.owner = owner;
    }
    
    private BehaviourFrame getReal() {
    	this.real = (BehaviourFrame)((UnitWrapper)owner.getBehaviour()).toUnit();
    	return real;
    }

    @Override
    public void addOrSetAction(de.s2.gsim.objects.Action a) {
        UnitWrapper f = (UnitWrapper) a;
        getReal().addAction((ActionFrame) (f.toUnit()));
        owner.setBehaviour(this);        
    }

    @Override
    public void addOrSetRLActionNode(RLActionNode node) throws GSimException {
        getReal().addRLRule((RLRuleFrame) ((UnitWrapper) node).toUnit());
        owner.setBehaviour(this);
    }

    @Override
    public void addOrSetRule(Rule rule) throws GSimException {

        if (rule instanceof RLActionNode) {
            addOrSetRLActionNode((RLActionNode) rule);
            return;
        }

        getReal().addOrSetRule((UserRuleFrame) ((UnitWrapper) rule).toUnit());
        owner.setBehaviour(this);

    }

    @Override
    public de.s2.gsim.objects.Action createAction(String name, String cls) throws GSimException {
        ActionFrame a = ActionFrame.newActionFrame(name, cls);
        getReal().addAction(a);
		owner.setBehaviour(this);
        return new ActionClass2(this, a);
    }

    @Override
    public RLActionNode createRLActionNode(String name) throws GSimException {
        RLRuleFrame r = getReal().createRLRule(name);
        getReal().addRLRule(r);
        RLActionNodeClass c = new RLActionNodeClass(this, r);
        owner.setBehaviour(this);
        return c;
    }

    @Override
    public Rule createRule(String name) throws GSimException {
        UserRuleFrame r = getReal().createRule(name);
        getReal().addOrSetRule(r);
        RuleClass c = new RuleClass(this, r);
        owner.setBehaviour(this);
        return c;
    }

    @Override
    public de.s2.gsim.objects.Action getAction(String name) throws GSimException {
        ActionFrame a = getReal().getAction(name);
        if (a == null) {
            throw new GSimException("Action " + name + " does not exist!");
        }
        return new ActionClass2(this, a);
    }

    @Override
    public de.s2.gsim.objects.Action[] getAvailableActions() {
        ArrayList<ActionClass2> actions = new ArrayList<ActionClass2>();

        ActionFrame[] f = getReal().getAvailableActions();

        for (int i = 0; i < f.length; i++) {
            if (!f[i].getName().startsWith("{")) {
                actions.add(new ActionClass2(this, f[i]));
            }
        }

        ActionClass2[] r = new ActionClass2[actions.size()];
        actions.toArray(r);
        return r;
    }

    @Override
    public int getMaxNodes() throws GSimException {
        return getReal().getMaxNodes();
    }

    @Override
    public double getRevaluationProb() throws GSimException {
        return getReal().getRevalProb();
    }

    @Override
    public double getRevisitCostFraction() throws GSimException {
        return getReal().getRevisitCost();
    }

    @Override
    public RLActionNode getRLActionNode(String name) {
        RLRuleFrame a = getReal().getRLRule(name);
        RLActionNodeClass ret = new RLActionNodeClass(this, a);
        return ret;
    }

    @Override
    public RLActionNode[] getRLActionNodes() {
        ArrayList<RLActionNodeClass> rules = new ArrayList<RLActionNodeClass>();

        RLRuleFrame[] f = getReal().getRLRule();

        for (int i = 0; i < f.length; i++) {
            if (!f[i].getName().startsWith("{")) {
                rules.add(new RLActionNodeClass(this, f[i]));
            }
        }

        RLActionNodeClass[] r = new RLActionNodeClass[rules.size()];
        rules.toArray(r);
        return r;
    }

    @Override
    public Rule getRule(String name) {
        UserRuleFrame a = getReal().getRule(name);
        Rule ret = new RuleClass(this, a);
        return ret;
    }

    @Override
    public Rule[] getRules() {
        ArrayList<RuleClass> rules = new ArrayList<RuleClass>();

        UserRuleFrame[] f = getReal().getRules();
        for (int i = 0; i < f.length; i++) {
            if (!f[i].getName().startsWith("{")) {
                rules.add(new RuleClass(this, f[i]));
            }
        }

        RuleClass[] r = new RuleClass[rules.size()];
        rules.toArray(r);
        return r;
    }

    @Override
    public int getUpdateInterval() throws GSimException {
        return getReal().getStateUpdateInterval();
    }

    @Override
    public boolean isDeclaredRLNode(String nodeName) throws GSimException {
        return getReal().getDeclaredFrame(BehaviourFrame.RL_LIST, nodeName) != null;
    }

    @Override
    public boolean isDeclaredRule(String ruleName) throws GSimException {
        return getReal().getDeclaredFrame(BehaviourFrame.RULE_LIST, ruleName) != null;
    }

    public void removeAvailableAction(String name) throws GSimException {

        ActionFrame[] x = getReal().getDeclaredAvailableActions();
        boolean check = false;
        for (ActionFrame a : x) {
            if (a.getName().equals(name)) {
                check = true;
            }
        }
        if (!check) {
            throw new GSimException("Action " + name + " cannot be removed in this object-class, because it is defined in a parent");
        }

        ActionFrame a = getReal().getAction(name);

        if (a != null) {
            getReal().removeChildFrame(BehaviourFrame.ACTION_LIST, a.getName());
        }
        owner.setBehaviour(this);
    }

    @Override
    public void removeRLActionNode(String name) throws GSimException {
        try {
            getReal().removeRLRule(name);
            owner.setBehaviour(this);
        } catch (GSimDefException e) {
            throw new GSimException(e.getMessage());
        }
    }

    @Override
    public void removeRule(String name) throws GSimException {
        try {
            getReal().removeRule(name);
            owner.setBehaviour(this);
        } catch (GSimDefException e) {
            throw new GSimException(e.getMessage());
        }
    }

    @Override
    public void setMaxNodes(int n) throws GSimException {
        getReal().setMaxNodes(n);
        owner.setBehaviour(this);
    }

    @Override
    public void setRevaluationProb(double d) throws GSimException {
        getReal().setRevalProb(d);
        owner.setBehaviour(this);
    }

    @Override
    public void setRevisitCostFraction(double d) throws GSimException {
        getReal().setRevisitCost(d);
        owner.setBehaviour(this);
    }

    @Override
    public void setUpdateInterval(int n) throws GSimException {
        getReal().setStateUpdateInterval(n);
        owner.setBehaviour(this);
    }

    @Override
    public Unit<Frame,DomainAttribute> toUnit() {
        return real;
    }

	@Override
	public int getContractInterval() throws GSimException {
		return real.getStateContractInterval();
	}

	@Override
	public void setContractInterval(int n) throws GSimException {
		this.real.setStateContractInterval(n);
		owner.setBehaviour(this);
	}

}
