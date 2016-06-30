package de.s2.gsim.api.objects.impl;

import java.util.ArrayList;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.ActionFrame;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.RLRuleFrame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.environment.UserRuleFrame;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.RLActionNode;
import de.s2.gsim.objects.Rule;

public class BehaviourClass implements Behaviour, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private AgentClass owner;

    private BehaviourFrame real;

    public BehaviourClass(AgentClass owner, BehaviourFrame real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addOrSetAction(de.s2.gsim.objects.Action a) {
        UnitWrapper f = (UnitWrapper) a;
        real.addAction((ActionFrame) (f.toUnit()));
    }

    @Override
    public void addOrSetRLActionNode(RLActionNode node) throws GSimException {
        real.addRLRule((RLRuleFrame) ((UnitWrapper) node).toUnit());
        owner.setBehaviour(this);
    }

    @Override
    public void addOrSetRule(Rule rule) throws GSimException {

        if (rule instanceof RLActionNode) {
            addOrSetRLActionNode((RLActionNode) rule);
            return;
        }

        real.addOrSetRule((UserRuleFrame) ((UnitWrapper) rule).toUnit());
        owner.setBehaviour(this);

    }

    @Override
    public de.s2.gsim.objects.Action createAction(String name, String cls) throws GSimException {
        ActionFrame a = new ActionFrame(name, cls);
        real.addAction(a);
        return new ActionClass2(this, a);
    }

    @Override
    public RLActionNode createRLActionNode(String name) throws GSimException {
        RLRuleFrame r = real.createRLRule(name);
        real.addRLRule(r);
        RLActionNodeClass c = new RLActionNodeClass(this, r);
        owner.setBehaviour(this);
        return c;
    }

    @Override
    public Rule createRule(String name) throws GSimException {
        UserRuleFrame r = real.createRule(name);
        real.addOrSetRule(r);
        RuleClass c = new RuleClass(this, r);
        owner.setBehaviour(this);
        return c;
    }

    @Override
    public de.s2.gsim.objects.Action getAction(String name) throws GSimException {
        ActionFrame a = real.getAction(name);
        if (a == null) {
            throw new GSimException("Action " + name + " does not exist!");
        }
        return new ActionClass2(this, a);
    }

    @Override
    public de.s2.gsim.objects.Action[] getAvailableActions() {
        ArrayList<ActionClass2> actions = new ArrayList<ActionClass2>();

        ActionFrame[] f = real.getAvailableActions();

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
        return real.getMaxNodes();
    }

    @Override
    public double getRevaluationProb() throws GSimException {
        return real.getRevalProb();
    }

    @Override
    public double getRevisitCostFraction() throws GSimException {
        return real.getRevisitCost();
    }

    @Override
    public RLActionNode getRLActionNode(String name) {
        RLRuleFrame a = real.getRLRule(name);
        RLActionNodeClass ret = new RLActionNodeClass(this, a);
        return ret;
    }

    @Override
    public RLActionNode[] getRLActionNodes() {
        ArrayList<RLActionNodeClass> rules = new ArrayList<RLActionNodeClass>();

        RLRuleFrame[] f = real.getRLRule();

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
        UserRuleFrame a = real.getRule(name);
        Rule ret = new RuleClass(this, a);
        return ret;
    }

    @Override
    public Rule[] getRules() {
        ArrayList<RuleClass> rules = new ArrayList<RuleClass>();

        UserRuleFrame[] f = real.getRules();
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
        return real.getStateUpdateInterval();
    }

    @Override
    public boolean isDeclaredRLNode(String nodeName) throws GSimException {
        return real.getDeclaredFrame(BehaviourFrame.RL_LIST, nodeName) != null;
    }

    @Override
    public boolean isDeclaredRule(String ruleName) throws GSimException {
        return real.getDeclaredFrame(BehaviourFrame.RULE_LIST, ruleName) != null;
    }

    public void removeAvailableAction(String name) throws GSimException {

        ActionFrame[] x = real.getDeclaredAvailableActions();
        boolean check = false;
        for (ActionFrame a : x) {
            if (a.getName().equals(name)) {
                check = true;
            }
        }
        if (!check) {
            throw new GSimException("Action " + name + " cannot be removed in this object-class, because it is defined in a parent");
        }

        ActionFrame a = real.getAction(name);

        if (a != null) {
            real.removeChildFrame(BehaviourFrame.ACTION_LIST, a.getName());
        }
        owner.setBehaviour(this);
    }

    @Override
    public void removeRLActionNode(String name) throws GSimException {
        try {
            real.removeRLRule(name);
            owner.setBehaviour(this);
        } catch (GSimDefException e) {
            throw new GSimException(e.getMessage());
        }
    }

    @Override
    public void removeRule(String name) throws GSimException {
        try {
            real.removeRule(name);
            owner.setBehaviour(this);
        } catch (GSimDefException e) {
            throw new GSimException(e.getMessage());
        }
    }

    @Override
    public void setMaxNodes(int n) throws GSimException {
        real.setMaxNodes(n);
        real.setDirty(true);
        owner.setBehaviour(this);
    }

    @Override
    public void setRevaluationProb(double d) throws GSimException {
        real.setRevalProb(d);
        real.setDirty(true);
        owner.setBehaviour(this);
    }

    @Override
    public void setRevisitCostFraction(double d) throws GSimException {
        real.setRevisitCost(d);
        real.setDirty(true);
        owner.setBehaviour(this);
    }

    @Override
    public void setUpdateInterval(int n) throws GSimException {
        real.setStateUpdateInterval(n);
        real.setDirty(true);
        owner.setBehaviour(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
