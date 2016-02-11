package gsim.objects.impl;

import de.s2.gsim.objects.ActionIF;
import de.s2.gsim.objects.AgentInstanceIF;
import de.s2.gsim.objects.BehaviourIF;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.RLActionNodeIF;
import de.s2.gsim.objects.RuleIF;
import gsim.def.objects.Unit;
import gsim.def.objects.agent.Behaviour;
import gsim.def.objects.agent.BehaviourFrame;
import gsim.def.objects.behaviour.Action;
import gsim.def.objects.behaviour.ActionFrame;
import gsim.def.objects.behaviour.Condition;
import gsim.def.objects.behaviour.RLRule;
import gsim.def.objects.behaviour.UserRule;

public class BehaviourInstance implements BehaviourIF, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private AgentInstanceIF owner;

    private Behaviour real;

    public BehaviourInstance(AgentInstanceIF owner, Behaviour real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addOrSetAction(ActionIF a) {
        UnitWrapper c = (UnitWrapper) a;
        Action x = (Action) c.toUnit();
        real.addAction(x);
    }

    @Override
    public void addOrSetRLActionNode(RLActionNodeIF node) throws GSimObjectException {
        real.addRLRule((RLRule) ((UnitWrapper) node).toUnit());
        owner.setBehaviour(this);
    }

    @Override
    public void addOrSetRule(RuleIF rule) throws GSimObjectException {
        real.addRule((UserRule) ((UnitWrapper) rule).toUnit());
        owner.setBehaviour(this);
    }

    @Override
    public ActionIF createAction(String name, String cls) throws GSimObjectException {
        ActionIF def = owner.getBehaviour().createAction(name, cls);
        ActionFrame frame = (ActionFrame) def;
        real.addAction(new Action(frame));
        return getAction(name);

    }

    @Override
    public RLActionNodeIF createRLActionNode(String name) throws GSimObjectException {
        RLRule f = real.createRLRule(name, new Condition[0], new Action[0]);
        owner.setBehaviour(this);
        return new RLActionNodeInstance(this, f);
        // throw new RuntimeException("Not allowed on instances");
    }

    @Override
    public RuleIF createRule(String name) {
        throw new RuntimeException("Not allowed on instances");
    }

    @Override
    public ActionIF getAction(String name) throws GSimObjectException {
        Action a = real.getAction(name);
        if (a == null) {
            throw new GSimObjectException("Action " + name + " does not exist!");
        }
        return new ActionInstanceDefinition(this, a);
    }

    @Override
    public ActionIF[] getAvailableActions() {
        Action[] f = real.getAvailableActions();
        ActionInstanceDefinition[] r = new ActionInstanceDefinition[f.length];
        for (int i = 0; i < f.length; i++) {
            r[i] = new ActionInstanceDefinition(this, f[i]);
        }
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
        RLRule a = real.getRLRule(name);
        RLActionNodeInstance ret = new RLActionNodeInstance(this, a);
        return ret;
    }

    @Override
    public RLActionNodeIF[] getRLActionNodes() {
        RLRule[] f = real.getRLRules();
        RLActionNodeInstance[] r = new RLActionNodeInstance[f.length];
        for (int i = 0; i < f.length; i++) {
            r[i] = new RLActionNodeInstance(this, f[i]);
        }
        return r;
    }

    @Override
    public RuleIF getRule(String name) {
        UserRule a = real.getRule(name);
        RuleIF ret = new RuleInstance(this, a);
        return ret;
    }

    @Override
    public RuleIF[] getRules() {
        UserRule[] f = real.getRules();
        RuleInstance[] r = new RuleInstance[f.length];
        for (int i = 0; i < f.length; i++) {
            r[i] = new RuleInstance(this, f[i]);
        }
        return r;
    }

    @Override
    public int getUpdateInterval() throws GSimObjectException {
        return real.getStateUpdateInterval();
    }

    @Override
    public boolean isDeclaredRLNode(String nodeName) throws GSimObjectException {
        return real.getDefinition().getDeclaredFrame(BehaviourFrame.RL_LIST, nodeName) != null;
    }

    @Override
    public boolean isDeclaredRule(String ruleName) throws GSimObjectException {
        return real.getDefinition().getDeclaredFrame(BehaviourFrame.RULE_LIST, ruleName) != null;
    }

    public void removeAvailableAction(String name) throws GSimObjectException {
        Action a = real.getAction(name);
        if (a != null) {
            real.removeChildInstance(BehaviourFrame.ACTION_LIST, a.getName());
        }
        owner.setBehaviour(this);
    }

    @Override
    public void removeRLActionNode(String name) throws GSimObjectException {
        real.removeRLRules(name);
        owner.setBehaviour(this);
    }

    @Override
    public void removeRule(String name) throws GSimObjectException {
        real.removeRule(name);
        owner.setBehaviour(this);
    }

    @Override
    public void setMaxNodes(int n) throws GSimObjectException {
        real.setMaxNodes(n);
        owner.setBehaviour(this);
    }

    @Override
    public void setRevaluationProb(double d) throws GSimObjectException {
        real.setRevalProb(d);
        owner.setBehaviour(this);
    }

    @Override
    public void setRevisitCostFraction(double d) throws GSimObjectException {
        real.setRevisitCost(d);
        owner.setBehaviour(this);
    }

    @Override
    public void setUpdateInterval(int n) throws GSimObjectException {
        real.setStateUpdateInterval(n);
        owner.setBehaviour(this);
    }

    @Override
    public Unit toUnit() {
        return real;
    }

}
