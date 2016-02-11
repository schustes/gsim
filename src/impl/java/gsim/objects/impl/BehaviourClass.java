package gsim.objects.impl;

import java.util.ArrayList;

import de.s2.gsim.objects.ActionIF;
import de.s2.gsim.objects.AgentClassIF;
import de.s2.gsim.objects.BehaviourIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.RLActionNodeIF;
import de.s2.gsim.objects.RuleIF;
import gsim.def.objects.Unit;
import gsim.def.objects.agent.BehaviourFrame;
import gsim.def.objects.behaviour.ActionFrame;
import gsim.def.objects.behaviour.RLRuleFrame;
import gsim.def.objects.behaviour.UserRuleFrame;

public class BehaviourClass implements BehaviourIF, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private AgentClassIF owner;

    private BehaviourFrame real;

    public BehaviourClass(AgentClassIF owner, BehaviourFrame real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addOrSetAction(ActionIF a) {
        UnitWrapper f = (UnitWrapper) a;
        real.addAction((ActionFrame) (f.toUnit()));
    }

    @Override
    public void addOrSetRLActionNode(RLActionNodeIF node) throws GSimObjectException {
        // try {
        // real.removeRLRule(node.getName());
        // } catch (gsim.def.GSimDefException e) {
        // not defined here.
        // }
        real.addRLRule((RLRuleFrame) ((UnitWrapper) node).toUnit());
        owner.setBehaviour(this);

    }

    @Override
    public void addOrSetRule(RuleIF rule) throws GSimObjectException {

        if (rule instanceof RLActionNodeIF) {
            addOrSetRLActionNode((RLActionNodeIF) rule);
            return;
        }

        // try {
        // real.removeRule(rule.getName());
        // } catch (gsim.def.GSimDefException e) {
        // }

        real.addRule((UserRuleFrame) ((UnitWrapper) rule).toUnit());
        owner.setBehaviour(this);

    }

    @Override
    public ActionIF createAction(String name, String cls) throws GSimObjectException {
        ActionFrame a = new ActionFrame(name, cls);
        real.addAction(a);
        return new ActionClass2(this, a);
    }

    @Override
    public RLActionNodeIF createRLActionNode(String name) throws GSimObjectException {
        RLRuleFrame r = real.createRLRule(name);
        real.addRLRule(r);
        RLActionNodeClass c = new RLActionNodeClass(this, r);
        owner.setBehaviour(this);
        return c;
    }

    @Override
    public RuleIF createRule(String name) throws GSimObjectException {
        UserRuleFrame r = real.createRule(name);
        real.addRule(r);
        RuleClass c = new RuleClass(this, r);
        owner.setBehaviour(this);
        return c;
    }

    @Override
    public ActionIF getAction(String name) throws GSimObjectException {
        ActionFrame a = real.getAction(name);
        if (a == null) {
            throw new GSimObjectException("Action " + name + " does not exist!");
        }
        return new ActionClass2(this, a);
    }

    @Override
    public ActionIF[] getAvailableActions() {
        ArrayList<ActionClass2> actions = new ArrayList<ActionClass2>();

        ActionFrame[] f = real.getAvailableActions();

        for (int i = 0; i < f.length; i++) {
            if (!f[i].getTypeName().startsWith("{")) {
                actions.add(new ActionClass2(this, f[i]));
            }
        }

        ActionClass2[] r = new ActionClass2[actions.size()];
        actions.toArray(r);
        return r;
    }

    @Override
    public int getMaxNodes() throws GSimObjectException {
        return real.getMaxNodes();
    }

    @Override
    public double getRevaluationProb() throws GSimObjectException {
        return real.getRevalProb();
    }

    @Override
    public double getRevisitCostFraction() throws GSimObjectException {
        return real.getRevisitCost();
    }

    @Override
    public RLActionNodeIF getRLActionNode(String name) {
        RLRuleFrame a = real.getRLRule(name);
        RLActionNodeClass ret = new RLActionNodeClass(this, a);
        return ret;
    }

    @Override
    public RLActionNodeIF[] getRLActionNodes() {
        ArrayList<RLActionNodeClass> rules = new ArrayList<RLActionNodeClass>();

        RLRuleFrame[] f = real.getRLRule();

        for (int i = 0; i < f.length; i++) {
            if (!f[i].getTypeName().startsWith("{")) {
                rules.add(new RLActionNodeClass(this, f[i]));
            }
        }

        RLActionNodeClass[] r = new RLActionNodeClass[rules.size()];
        rules.toArray(r);
        return r;
    }

    @Override
    public RuleIF getRule(String name) {
        UserRuleFrame a = real.getRule(name);
        RuleIF ret = new RuleClass(this, a);
        return ret;
    }

    @Override
    public RuleIF[] getRules() {
        ArrayList<RuleClass> rules = new ArrayList<RuleClass>();

        UserRuleFrame[] f = real.getRules();
        for (int i = 0; i < f.length; i++) {
            if (!f[i].getTypeName().startsWith("{")) {
                rules.add(new RuleClass(this, f[i]));
            }
        }

        RuleClass[] r = new RuleClass[rules.size()];
        rules.toArray(r);
        return r;
    }

    @Override
    public int getUpdateInterval() throws GSimObjectException {
        return real.getStateUpdateInterval();
    }

    @Override
    public boolean isDeclaredRLNode(String nodeName) throws GSimObjectException {
        return real.getDeclaredFrame(BehaviourFrame.RL_LIST, nodeName) != null;
    }

    @Override
    public boolean isDeclaredRule(String ruleName) throws GSimObjectException {
        return real.getDeclaredFrame(BehaviourFrame.RULE_LIST, ruleName) != null;
    }

    public void removeAvailableAction(String name) throws GSimObjectException {

        ActionFrame[] x = real.getDeclaredAvailableActions();
        boolean check = false;
        for (ActionFrame a : x) {
            if (a.getTypeName().equals(name)) {
                check = true;
            }
        }
        if (!check) {
            throw new GSimObjectException("Action " + name + " cannot be removed in this object-class, because it is defined in a parent");
        }

        ActionFrame a = real.getAction(name);

        if (a != null) {
            real.removeChildFrame(BehaviourFrame.ACTION_LIST, a.getTypeName());
        }
        owner.setBehaviour(this);
    }

    @Override
    public void removeRLActionNode(String name) throws GSimObjectException {
        try {
            real.removeRLRule(name);
            owner.setBehaviour(this);
        } catch (gsim.def.GSimDefException e) {
            throw new GSimObjectException(e.getMessage());
        }
    }

    @Override
    public void removeRule(String name) throws GSimObjectException {
        try {
            real.removeRule(name);
            owner.setBehaviour(this);
        } catch (gsim.def.GSimDefException e) {
            throw new GSimObjectException(e.getMessage());
        }
    }

    @Override
    public void setMaxNodes(int n) throws GSimObjectException {
        real.setMaxNodes(n);
        real.setDirty(true);
        owner.setBehaviour(this);
    }

    @Override
    public void setRevaluationProb(double d) throws GSimObjectException {
        real.setRevalProb(d);
        real.setDirty(true);
        owner.setBehaviour(this);
    }

    @Override
    public void setRevisitCostFraction(double d) throws GSimObjectException {
        real.setRevisitCost(d);
        real.setDirty(true);
        owner.setBehaviour(this);
    }

    @Override
    public void setUpdateInterval(int n) throws GSimObjectException {
        real.setStateUpdateInterval(n);
        real.setDirty(true);
        owner.setBehaviour(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
