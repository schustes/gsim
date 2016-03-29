package de.s2.gsim.def.objects.agent;

import java.util.ArrayList;

import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.behaviour.ActionCollectionFrame;
import de.s2.gsim.def.objects.behaviour.ActionFrame;
import de.s2.gsim.def.objects.behaviour.ConditionFrame;
import de.s2.gsim.def.objects.behaviour.ExpansionFrame;
import de.s2.gsim.def.objects.behaviour.RLRuleFrame;
import de.s2.gsim.def.objects.behaviour.UserRuleFrame;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class BehaviourFrame extends FrameOLD {

    public final static String ACTION_LIST = "available actions";

    public final static String ATTR_LIST = "attributes";

    public final static String BREADTH_FIRST = "breadth-first";

    public final static String CATEGORY = "behaviour";

    public final static String DEPTH_FIRST = "depth-first";

    public final static String RL_LIST = "classifiers";

    public final static String RULE_LIST = "rules";

    public final static long serialVersionUID = -2932477926775924825L;

    public ArrayList<String> removed = new ArrayList<String>();

    /**
     * Copy constructor.
     * 
     * @param f
     *            BehaviourFrame
     */
    public BehaviourFrame(BehaviourFrame f) {
        super(f);
    }

    public BehaviourFrame(BehaviourFrame f, String category) {
        super(f);
        this.category = category;
    }

    /**
     * Copy constructor
     */
    public BehaviourFrame(FrameOLD f) {
        super(f);
    }

    /**
     * Copy, but give it a new name.
     * 
     * @param f
     *            BehaviourFrame
     * @param p
     *            PerceptionFrame
     */
    public BehaviourFrame(String name, BehaviourFrame f, String category) {
        super(name, f);
        this.category = category;
    }

    /**
     * Inheritance constructor.
     */
    public BehaviourFrame(String name, BehaviourFrame[] parents, String category) {
        super(parents, name, "behaviour");
        this.category = category;
    }

    public BehaviourFrame(String name, String category) {
        super(name, category);
        init();
    }

    /**
     * Adds a list of actions. This is the 'action-group' tag of the file action.xml.
     * 
     * @param r
     *            ActionCollectionFrame
     */
    // public void addStragy(ActionCollectionFrame r) {
    // addChildFrame(ACTION_LIST, r);
    // }
    public void addAction(ActionFrame r) {
        addChildFrame(ACTION_LIST, r);
    }

    public void addRLRule(RLRuleFrame r) {
        addChildFrame(RL_LIST, r);
    }

    /**
     * Adds a rule.
     * 
     * @param r
     *            UserRuleFrame
     */
    public void addRule(UserRuleFrame r) {
        addChildFrame(RULE_LIST, r);
    }

    /**
     * @return Object
     */
    @Override
    public Object clone() {
        FrameOLD a = (FrameOLD) super.clone();
        BehaviourFrame f = new BehaviourFrame(a);
        return f;
    }

    /**
     * Creates a new collection of actions.
     * 
     * @param name
     *            String
     * @param actions
     *            ActionFrame[]
     * @return ActionCollectionFrame
     */
    public ActionCollectionFrame createActionGroupFrame(String name, ActionFrame[] actions) {

        ActionCollectionFrame f = new ActionCollectionFrame(name, actions);
        f.setMutable(true);
        f.setSystem(true);
        return f;
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
    public ConditionFrame createCondition(String var, String op, String val) {
        ConditionFrame f = new ConditionFrame(var, op, val);
        return f;
    }

    public ExpansionFrame createExpansion(String var) {
        ExpansionFrame f = new ExpansionFrame(var);
        return f;
    }

    public ExpansionFrame createExpansion(String var, String min, String max) {
        ExpansionFrame f = new ExpansionFrame(var, min, max);
        return f;
    }

    public RLRuleFrame createRLRule(String name) {
        RLRuleFrame f = new RLRuleFrame(name);
        f.setSystem(false);
        f.setMutable(true);
        return f;
    }

    /**
     * Creates an empty Rule (no conditions, no actions).
     * 
     * @param name
     *            String
     * @return UserRuleFrame
     */
    public UserRuleFrame createRule(String name) {
        UserRuleFrame f = new UserRuleFrame(name);
        f.setSystem(false);
        f.setMutable(true);
        return f;
    }

    public ActionFrame getAction(String name) {
        FrameOLD[] f = getChildFrames(ACTION_LIST);
        for (int i = 0; i < f.length; i++) {
            if (f[i].getTypeName().equals(name)) {
                return new ActionFrame(f[i]);
            }
        }
        return null;
    }

    public ActionFrame[] getAvailableActions() {
        FrameOLD[] f = getChildFrames(ACTION_LIST);
        ActionFrame[] uf = new ActionFrame[f.length];
        for (int i = 0; i < f.length; i++) {
            uf[i] = new ActionFrame(f[i]);
        }
        return uf;
    }

    public ActionFrame[] getDeclaredAvailableActions() {
        FrameOLD[] f = super.getDeclaredChildFrames(ACTION_LIST);
        if (f == null) {
            return new ActionFrame[0];
        }
        ActionFrame[] uf = new ActionFrame[f.length];
        for (int i = 0; i < f.length; i++) {
            uf[i] = new ActionFrame(f[i]);
        }
        return uf;
    }

    public UserRuleFrame getDeclaredRLRule(String name) {
        RLRuleFrame[] frs = getDeclaredRLRules();
        for (int i = 0; i < frs.length; i++) {
            if (frs[i].getTypeName().equals(name)) {
                return frs[i];
            }
        }
        return null;
    }

    public RLRuleFrame[] getDeclaredRLRules() {
        FrameOLD[] f = getDeclaredChildFrames(RL_LIST);
        if (f == null) {
            return new RLRuleFrame[0];
        }
        RLRuleFrame[] uf = new RLRuleFrame[f.length];
        for (int i = 0; i < f.length; i++) {
            uf[i] = new RLRuleFrame(f[i]);
            // uf[i] = new ClassifierFrame(f[i], f[i].getId());
        }
        return uf;
    }

    /**
     * Retrieves a rule with a certain name if it is defined on the lowest level.
     * 
     * @param name
     *            String
     * @return UserRuleFrame
     */
    public UserRuleFrame getDeclaredRule(String name) {
        UserRuleFrame[] frs = getDeclaredRules();
        for (int i = 0; i < frs.length; i++) {
            String ruleName = frs[i].getTypeName();
            if (ruleName.equals(name)) {
                return frs[i];
            }
        }
        return null;
    }

    /**
     * Retrieves all rules if it is defined in the lowest level.
     * 
     * @return UserRuleFrame[]
     */
    public UserRuleFrame[] getDeclaredRules() {
        FrameOLD[] f = getDeclaredChildFrames(RULE_LIST);

        if (f == null) {
            return new UserRuleFrame[0];
        }

        UserRuleFrame[] uf = new UserRuleFrame[f.length];
        for (int i = 0; i < f.length; i++) {
            uf[i] = new UserRuleFrame(f[i]);
        }
        return uf;
    }

    public int getDeleteUnusedAfter() {
        return Integer.parseInt(this.getAttribute("delete-unused-after").getDefaultValue());
    }

    public int getMaxDepth() {
        return Integer.parseInt(this.getAttribute("max-depth").getDefaultValue());
    }

    public int getMaxNodes() {
        return Integer.parseInt(this.getAttribute("max-nodes").getDefaultValue());
    }

    public double getRevalProb() {
        return Double.parseDouble(this.getAttribute("reval-prob").getDefaultValue());
    }

    public double getRevisitCost() {
        return Double.parseDouble(this.getAttribute("revisit-costfraction").getDefaultValue());
    }

    public RLRuleFrame[] getRLRule() {
        FrameOLD[] f = getChildFrames(RL_LIST);
        RLRuleFrame[] uf = new RLRuleFrame[f.length];
        for (int i = 0; i < f.length; i++) {
            uf[i] = new RLRuleFrame(f[i]);
            // uf[i] = new ClassifierFrame(f[i], f[i].getId());
        }
        return uf;
    }

    public RLRuleFrame getRLRule(String name) {
        RLRuleFrame[] frs = this.getRLRule();
        for (int i = 0; i < frs.length; i++) {
            if (frs[i].getTypeName().equals(name)) {
                return frs[i];
            }
        }
        return null;
    }

    /**
     * Returns the action collections that are defined for this behaviour.
     * 
     * @return ActionCollectionFrame[]
     */
    /*
     * public ActionCollectionFrame[] getAvailableActions() { Frame[] f = getChildFrames(ACTION_LIST); ActionCollectionFrame[] uf = new
     * ActionCollectionFrame[f.length]; for (int i = 0; i < f.length; i++) { uf[i] = new ActionCollectionFrame(f[i], f[i].getId()); } return uf; }
     * 
     * public ActionCollectionFrame getAvailableActions(String groupName) { Frame[] f = getChildFrames(ACTION_LIST); for (int i = 0; i < f.length;
     * i++) { if (f[i].getTypeName().equals(groupName)) { return new ActionCollectionFrame(f[i], f[i].getId()); } } return null; }
     */
    /**
     * Retrieves the first action with matching name.
     * 
     * @param action
     *            String
     * @return ActionFrame if found, null else
     */

    /*
     * public ActionFrame getAction(String action) { ActionCollectionFrame[] f = this.getAvailableActions(); for (int i=0;i<f.length;i++) {
     * ActionFrame[] a = f[i].getActions(); for (int j=0;j<a.length;j++) { if (a[j].getTypeName().equals(action)) { return a[j]; } } } return null; }
     */
    // public int getEvaluationCycle() {
    // DomainAttribute a = getAttribute(ATTR_LIST, "Evaluation cycle");
    // return Integer.parseInt(a.getDefaultValue());
    // }
    /**
     * Retrieves a rule with a certain name.
     * 
     * @param name
     *            String
     * @return UserRuleFrame
     */
    public UserRuleFrame getRule(String name) {
        UserRuleFrame[] frs = getRules();
        for (int i = 0; i < frs.length; i++) {
            if (frs[i].getTypeName().equals(name)) {
                return frs[i];
            }
        }
        return null;
    }

    /**
     * Retrieves all rules.
     * 
     * @return UserRuleFrame[]
     */
    public UserRuleFrame[] getRules() {
        FrameOLD[] f = getChildFrames(RULE_LIST);
        UserRuleFrame[] uf = new UserRuleFrame[f.length];
        for (int i = 0; i < f.length; i++) {
            uf[i] = new UserRuleFrame(f[i]);
        }
        return uf;
    }

    public int getStateUpdateInterval() {
        return Integer.parseInt(this.getAttribute("update-interval").getDefaultValue());
    }

    public String getTraversalMode() {
        DomainAttribute a = this.getAttribute("depth-first");
        if (a.getDefaultValue().equals("1")) {
            return "depth-first";
        } else {
            return "breadth-first";
        }
    }

    public boolean isBreadthFirst() {
        DomainAttribute a = this.getAttribute("depth-first");
        return a.getDefaultValue().equals("0");
    }

    public boolean isDepthFirst() {
        DomainAttribute a = this.getAttribute("depth-first");
        return a.getDefaultValue().equals("1");
    }

    public void removeRLRule(String name) throws de.s2.gsim.def.GSimDefException {

        if (super.getDeclaredFrame(RL_LIST, name) == null && getChildFrame(RL_LIST, name.trim()) != null) {
            throw new de.s2.gsim.def.GSimDefException("This RLNode can't be removed, because it is defined in a parent of this frame.");
        } else if (super.getDeclaredFrame(RL_LIST, name) == null && getChildFrame(RL_LIST, name) == null) {
            throw new de.s2.gsim.def.GSimDefException("RLNode " + name + " could not be found.");
        }

        removed.add(name);

        super.removeChildFrame(RL_LIST, name);

    }

    public void removeRule(String name) throws de.s2.gsim.def.GSimDefException {

        if (super.getDeclaredFrame(RULE_LIST, name) == null && getChildFrame(RL_LIST, name) != null) {
            throw new de.s2.gsim.def.GSimDefException("This Rule can't be removed, because it is defined in a parent of this frame.");
        } else if (super.getDeclaredFrame(RULE_LIST, name) == null && getChildFrame(RL_LIST, name) == null) {
            throw new de.s2.gsim.def.GSimDefException("Rule " + name + " could not be found.");
        }

        removed.add(name);
        super.removeChildFrame(RULE_LIST, name);
    }

    public void setDeleteUnusedAfter(int n) {
        DomainAttribute a = this.getAttribute("delete-unused-after");
        a.setDefault(String.valueOf(n));
        addOrSetAttribute(ATTR_LIST, a);
    }

    public void setMaxDepth(int d) {
        DomainAttribute a = this.getAttribute("max-depth");
        a.setDefault(String.valueOf(d));
        addOrSetAttribute(ATTR_LIST, a);
    }

    public void setMaxNodes(int n) {
        DomainAttribute a = this.getAttribute("max-nodes");
        a.setDefault(String.valueOf(n));
        addOrSetAttribute(ATTR_LIST, a);
    }

    public void setRevalProb(double p) {
        DomainAttribute a = this.getAttribute("reval-prob");
        a.setDefault(String.valueOf(p));
        addOrSetAttribute(ATTR_LIST, a);
    }

    public void setRevisitCost(double p) {
        DomainAttribute a = this.getAttribute("revisit-costfraction");
        a.setDefault(String.valueOf(p));
        addOrSetAttribute(ATTR_LIST, a);
    }

    public void setStateUpdateInterval(int d) {
        DomainAttribute a = this.getAttribute("update-interval");
        a.setDefault(String.valueOf(d));
        addOrSetAttribute(ATTR_LIST, a);
    }

    public void setTraversalMode(String mode) {
        DomainAttribute a = this.getAttribute("depth-first");
        if (mode.equals(DEPTH_FIRST)) {
            a.setDefault("1");
        } else {
            a.setDefault("0");
        }
        addOrSetAttribute(ATTR_LIST, a);
    }

    /**
     * Sets attribute placeholders for values that are required by definition.
     */
    private void init() {

        setMutable(false);
        setSystem(true);

        ActionFrame f = new ActionFrame("{}", "");
        addChildFrame(ACTION_LIST, f);
        addChildFrame(RULE_LIST, new UserRuleFrame("{all-rules}"));

        super.defineAttributeList(ATTR_LIST);

        DomainAttribute dattr = new DomainAttribute("max-depth", AttributeType.NUMERICAL);
        dattr.setDefault("10");
        DomainAttribute nattr = new DomainAttribute("max-nodes", AttributeType.NUMERICAL);
        nattr.setDefault("100");
        super.addOrSetAttribute(ATTR_LIST, dattr);
        super.addOrSetAttribute(ATTR_LIST, nattr);
        DomainAttribute uattr = new DomainAttribute("update-interval", AttributeType.NUMERICAL);
        uattr.setDefault("10");
        DomainAttribute delattr = new DomainAttribute("delete-unused-after", AttributeType.NUMERICAL);
        delattr.setDefault("100");
        super.addOrSetAttribute(ATTR_LIST, uattr);
        super.addOrSetAttribute(ATTR_LIST, delattr);

        DomainAttribute tattr = new DomainAttribute("depth-first", AttributeType.NUMERICAL);
        tattr.setDefault("0");
        super.addOrSetAttribute(ATTR_LIST, tattr);

        DomainAttribute rattr = new DomainAttribute("reval-prob", AttributeType.NUMERICAL);
        rattr.setDefault("0.5");
        super.addOrSetAttribute(ATTR_LIST, rattr);

        DomainAttribute cattr = new DomainAttribute("revisit-costfraction", AttributeType.NUMERICAL);
        cattr.setDefault("0");
        super.addOrSetAttribute(ATTR_LIST, cattr);

        super.defineObjectList(RL_LIST, RLRuleFrame.RL_RULE_FRAME);

    }

}
