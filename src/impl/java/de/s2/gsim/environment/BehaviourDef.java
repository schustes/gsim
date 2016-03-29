package de.s2.gsim.environment;

import de.s2.gsim.objects.attribute.NumericalAttribute;

public class BehaviourDef extends Instance {

    public static final long serialVersionUID = 47162704396246620L;

    /**
     * Inheritance constructor.
     * 
     * @see Frame(String, Frame, int).
     * @param f
     *            Frame
     * @param id
     *            int
     */
    public BehaviourDef(Frame f) {
        super(f.getTypeName(), f);
    }

    /**
     * Copy constructor.
     * 
     * @param inst
     *            Instance
     */
    public BehaviourDef(Instance inst) {
        super(inst);
    }

    public void addAction(ActionDef r) {
        // ActionCollection c = this.getAvailableActions(listName);
        // if (c==null) {
        // c = new ActionCollection(new ActionCollectionFrame(listName, 123),123);
        // }
        // c.addChildInstance(ActionCollectionFrame.INST_ACTION_LIST, r);
        addChildInstance(BehaviourFrame.ACTION_LIST, r);
    }

    /**
     * Add a new action group (other than those defined in action.xml).
     * 
     * @param r
     *            ActionCollection
     */
    public void addAvailableActions(ActionCollection r) {
        addChildInstance(BehaviourFrame.ACTION_LIST, r);
    }

    public void addRLRule(RLRule r) {
        addChildInstance(BehaviourFrame.RL_LIST, r);
    }

    /**
     * Add a rule.
     * 
     * @param r
     *            UserRule
     */
    public void addRule(UserRule r) {
        addChildInstance(BehaviourFrame.RULE_LIST, r);
    }

    @Override
    public Object clone() {
        BehaviourDef b = new BehaviourDef(this);
        return b;
    }

    /**
     * Create a condition object. No checks are made.
     * 
     * @param var
     *            String
     * @param op
     *            String
     * @param val
     *            String
     * @return ConditionFrame
     */
    public ConditionDef createCondition(String var, String op, String val) {
        ConditionDef f = new ConditionDef(var, op, val);
        return f;
    }

    public ExpansionDef createExpansion(String var, double min, double max) {
        ExpansionDef f = new ExpansionDef(var, min, max);
        return f;
    }

    public RLRule createRLRule(String name, ConditionDef[] conditions, ActionDef[] consequents) {

        BehaviourFrame f = (BehaviourFrame) getDefinition();
        Frame r = f.getRule(name);
        r = new RLRuleFrame(name);

        Instance rule = new Instance(name, r);

        for (int i = 0; i < conditions.length; i++) {
            conditions[i].changeName(conditions[i].getName() + cern.jet.random.Uniform.staticNextDoubleFromTo(Double.MIN_VALUE, Double.MAX_VALUE));
            rule.addChildInstance(UserRuleFrame.INST_LIST_COND, conditions[i]);
        }
        for (int i = 0; i < consequents.length; i++) {
            rule.addChildInstance(UserRuleFrame.INST_LIST_CONS, consequents[i]);
        }

        return new RLRule(rule);

    }

    public UserRule createRule(String name, ConditionDef[] conditions, ActionDef[] consequents) {

        BehaviourFrame f = (BehaviourFrame) getDefinition();
        Frame r = f.getRule(name);
        r = new UserRuleFrame(name);

        Instance rule = new Instance(name, r);

        for (int i = 0; i < conditions.length; i++) {
            rule.addChildInstance(UserRuleFrame.INST_LIST_COND, conditions[i]);
        }
        for (int i = 0; i < consequents.length; i++) {
            rule.addChildInstance(UserRuleFrame.INST_LIST_CONS, consequents[i]);
        }

        return new UserRule(rule);

    }

    public ActionDef getAction(String cat) {
        Instance[] str = getChildInstances(BehaviourFrame.ACTION_LIST);
        for (int i = 0; i < str.length; i++) {
            if (str[i].getName().equals(cat)) {
                return new ActionDef(str[i]);
            }
        }
        return null;
    }

    public ActionDef[] getAvailableActions() {
        Instance[] str = getChildInstances(BehaviourFrame.ACTION_LIST);
        ActionDef[] ss = new ActionDef[str.length];
        for (int i = 0; i < ss.length; i++) {
            ss[i] = new ActionDef(str[i]);
        }
        return ss;
    }

    public int getDeleteUnusedAfter() {
        return (int) ((NumericalAttribute) this.getAttribute("delete-unused-after")).getValue();
    }

    public int getMaxDepth() {
        return (int) ((NumericalAttribute) this.getAttribute("max-depth")).getValue();
    }

    public int getMaxNodes() {
        return (int) ((NumericalAttribute) this.getAttribute("max-nodes")).getValue();
    }

    public double getRevalProb() {
        return ((NumericalAttribute) this.getAttribute("reval-prob")).getValue();
    }

    public double getRevisitCost() {
        return ((NumericalAttribute) this.getAttribute("revisit-costfraction")).getValue();
    }

    public RLRule getRLRule(String name) {
        RLRule[] frs = getRLRules();
        for (int i = 0; i < frs.length; i++) {
            if (frs[i].getName().equals(name)) {
                return frs[i];
            }
        }
        return null;
    }

    public RLRule[] getRLRules() {
        Instance[] rules = getChildInstances(BehaviourFrame.RL_LIST);
        RLRule[] userRules = new RLRule[rules.length];
        for (int i = 0; i < rules.length; i++) {
            userRules[i] = new RLRule(rules[i]);
        }
        return userRules;
    }

    /**
     * Get a rule with a certain name.
     * 
     * @param name
     *            String
     * @return UserRule
     */
    public UserRule getRule(String name) {
        UserRule[] frs = getRules();
        for (int i = 0; i < frs.length; i++) {
            if (frs[i].getName().equals(name)) {
                return frs[i];
            }
        }
        return null;
    }

    /**
     * Get all rules defined for this behaviour.
     * 
     * @return UserRule[]
     */
    public UserRule[] getRules() {
        Instance[] rules = getChildInstances(BehaviourFrame.RULE_LIST);
        UserRule[] userRules = new UserRule[rules.length];
        for (int i = 0; i < rules.length; i++) {
            userRules[i] = new UserRule(rules[i]);
        }
        return userRules;
    }

    public int getStateUpdateInterval() {
        return (int) ((NumericalAttribute) this.getAttribute("update-interval")).getValue();
    }

    public boolean isBreadthFirst() {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("depth-first");
        return a.getValue() == 0;
    }

    public boolean isDepthFirst() {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("depth-first");
        return a.getValue() == 0;
    }

    public void removeAllRLRules() {
        Instance[] classifiers = getChildInstances(BehaviourFrame.RL_LIST);
        for (int i = 0; i < classifiers.length; i++) {
            removeChildInstance(BehaviourFrame.RL_LIST, classifiers[i].getName());
        }
    }

    public void removeRLRules(String name) {
    	UnitOperations.removeChildInstance(this, new String[] { BehaviourFrame.RL_LIST }, name);
    }

    public void removeRule(String name) {
        UnitOperations.removeChildInstance(this, new String[] { BehaviourFrame.RULE_LIST }, name);
    }

    public void setDeleteUnusedAfter(int n) {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("delete-unused-after");
        a.setValue(n);
        this.setAttribute(a);
    }

    public void setMaxDepth(int d) {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("max-depth");
        a.setValue(d);
        this.setAttribute(a);
    }

    public void setMaxNodes(int n) {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("max-value");
        a.setValue(n);
        this.setAttribute(a);
    }

    public void setRevalProb(double p) {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("reval-prob");
        a.setValue(p);
        this.setAttribute(a);
    }

    public void setRevisitCost(double p) {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("revisit-costfraction");
        a.setValue(p);
        this.setAttribute(a);
    }

    public void setStateUpdateInterval(int d) {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("update-interval");
        a.setValue(d);
        this.setAttribute(a);
    }

    public void setTraversalMode(String mode) {
        NumericalAttribute a = (NumericalAttribute) this.getAttribute("depth-first");
        if (mode.equals("depth-first")) {
            a.setValue(1);
        } else {
            a.setValue(0);
        }
        this.setAttribute(a);
    }

}
