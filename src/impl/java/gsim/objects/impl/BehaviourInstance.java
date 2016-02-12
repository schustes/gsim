package gsim.objects.impl;

import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.Behaviour;
import de.s2.gsim.objects.GSimObjectException;
import de.s2.gsim.objects.RLActionNode;
import de.s2.gsim.objects.Rule;
import gsim.def.objects.Unit;
import gsim.def.objects.agent.BehaviourDef;
import gsim.def.objects.agent.BehaviourFrame;
import gsim.def.objects.behaviour.ActionDef;
import gsim.def.objects.behaviour.ActionFrame;
import gsim.def.objects.behaviour.ConditionDef;
import gsim.def.objects.behaviour.RLRule;
import gsim.def.objects.behaviour.UserRule;

public class BehaviourInstance implements Behaviour, UnitWrapper {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private AgentInstance owner;

    private BehaviourDef real;

    public BehaviourInstance(AgentInstance owner, BehaviourDef real) {
        this.real = real;
        this.owner = owner;
    }

    @Override
    public void addOrSetAction(de.s2.gsim.objects.Action a) {
        UnitWrapper c = (UnitWrapper) a;
        ActionDef x = (ActionDef) c.toUnit();
        real.addAction(x);
    }

    @Override
    public void addOrSetRLActionNode(RLActionNode node) throws GSimObjectException {
        real.addRLRule((RLRule) ((UnitWrapper) node).toUnit());
        owner.setBehaviour(this);
    }

    @Override
    public void addOrSetRule(Rule rule) throws GSimObjectException {
        real.addRule((UserRule) ((UnitWrapper) rule).toUnit());
        owner.setBehaviour(this);
    }

    @Override
    public de.s2.gsim.objects.Action createAction(String name, String cls) throws GSimObjectException {
        de.s2.gsim.objects.Action def = owner.getBehaviour().createAction(name, cls);
        ActionFrame frame = (ActionFrame) def;
        real.addAction(new ActionDef(frame));
        return getAction(name);

    }

    @Override
    public RLActionNode createRLActionNode(String name) throws GSimObjectException {
        RLRule f = real.createRLRule(name, new ConditionDef[0], new ActionDef[0]);
        owner.setBehaviour(this);
        return new RLActionNodeInstance(this, f);
        // throw new RuntimeException("Not allowed on instances");
    }

    @Override
    public Rule createRule(String name) {
        throw new RuntimeException("Not allowed on instances");
    }

    @Override
    public de.s2.gsim.objects.Action getAction(String name) throws GSimObjectException {
        ActionDef a = real.getAction(name);
        if (a == null) {
            throw new GSimObjectException("Action " + name + " does not exist!");
        }
        return new ActionInstanceDefinition(this, a);
    }

    @Override
    public de.s2.gsim.objects.Action[] getAvailableActions() {
        ActionDef[] f = real.getAvailableActions();
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
    public RLActionNode getRLActionNode(String name) {
        RLRule a = real.getRLRule(name);
        RLActionNodeInstance ret = new RLActionNodeInstance(this, a);
        return ret;
    }

    @Override
    public RLActionNode[] getRLActionNodes() {
        RLRule[] f = real.getRLRules();
        RLActionNodeInstance[] r = new RLActionNodeInstance[f.length];
        for (int i = 0; i < f.length; i++) {
            r[i] = new RLActionNodeInstance(this, f[i]);
        }
        return r;
    }

    @Override
    public Rule getRule(String name) {
        UserRule a = real.getRule(name);
        Rule ret = new RuleInstance(this, a);
        return ret;
    }

    @Override
    public Rule[] getRules() {
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
        ActionDef a = real.getAction(name);
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
