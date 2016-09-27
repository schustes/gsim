package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class BehaviourFrame extends Frame {

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
	public static BehaviourFrame copy(BehaviourFrame f) {
		return new BehaviourFrame(f);
	}

	public BehaviourFrame(BehaviourFrame f, String category) {
		super(f.getName(), f);
		this.category = category;
	}

	/**
	 * Copy constructor
	 */
	public static BehaviourFrame copy(Frame f) {
		return new BehaviourFrame(f);
	}
	
	private BehaviourFrame(Frame f) {
		super(f.getName(), f);
	}

	/**
	 * Copy, but give it a new name.
	 * 
	 * @param f
	 *            BehaviourFrame
	 * @param p
	 *            PerceptionFrame
	 */
	public static BehaviourFrame copy(String name, BehaviourFrame f, String category) {
		Frame ff = Frame.copy(f, name);
		BehaviourFrame bf = new BehaviourFrame(ff);
		return bf;
	}

	/**
	 * Inheritance constructor.
	 */
	public static BehaviourFrame inherit(String name, List<BehaviourFrame> parents) {
		Frame ff = Frame.inherit(parents, name, Optional.of(EntityTypes.BEHAVIOUR.toString()));
		BehaviourFrame bf = new BehaviourFrame(ff);
		return bf;
	}

	public static BehaviourFrame newBehaviour(String name) {
		Frame f = new Frame(name, Optional.of(EntityTypes.BEHAVIOUR.toString()), false, true);
		BehaviourFrame bf = new BehaviourFrame(f);
		bf.init();
		return bf;
	}

	/**
	 * Adds a list of actions. This is the 'action-group' tag of the file action.xml.
	 * 
	 * @param r
	 *            ActionCollectionFrame
	 */
	public void addAction(ActionFrame r) {
		addOrSetChildFrame(ACTION_LIST, r);
	}

	public void addRLRule(RLRuleFrame r) {
		addOrSetChildFrame(RL_LIST, r);
	}

	/**
	 * Adds a rule.
	 * 
	 * @param r
	 *            UserRuleFrame
	 */
	public void addOrSetRule(UserRuleFrame r) {
		addOrSetChildFrame(RULE_LIST, r);
	}

	/**
	 * @return Object
	 */
	@Override
	public BehaviourFrame clone() {
		Frame a = (Frame) super.clone();
        return new BehaviourFrame(a);
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
		return new ActionCollectionFrame(name, actions);
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
		RLRuleFrame f = RLRuleFrame.newRLRuleFrame(name);
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
		UserRuleFrame f = UserRuleFrame.newUserRuleFrame(name)	;
		return f;
	}

	public ActionFrame getAction(String name) {
		for (Frame f: getChildFrames(ACTION_LIST)) {
			if (f.getName().equals(name)) {
				return new ActionFrame(f);
			}
		}
		return null;
	}

	public ActionFrame[] getAvailableActions() {
		List<Frame> f = getChildFrames(ACTION_LIST);
		ActionFrame[] uf = new ActionFrame[f.size()];
		for (int i = 0; i < f.size(); i++) {
			uf[i] = new ActionFrame(f.get(i));
		}
		return uf;
	}

	public ActionFrame[] getDeclaredAvailableActions() {
		List<Frame> f = super.getDeclaredChildFrames(ACTION_LIST);
		if (f.isEmpty()) {
			return new ActionFrame[0];
		}
		ActionFrame[] uf = new ActionFrame[f.size()];
		for (int i = 0; i < f.size(); i++) {
			uf[i] = new ActionFrame(f.get(i));
		}
		return uf;
	}

	public UserRuleFrame getDeclaredRLRule(String name) {
		RLRuleFrame[] frs = getDeclaredRLRules();
		for (int i = 0; i < frs.length; i++) {
			if (frs[i].getName().equals(name)) {
				return frs[i];
			}
		}
		return null;
	}

	public RLRuleFrame[] getDeclaredRLRules() {
		List<Frame> f = getDeclaredChildFrames(RL_LIST);
		if (f.isEmpty()) {
			return new RLRuleFrame[0];
		}
		RLRuleFrame[] uf = new RLRuleFrame[f.size()];
		for (int i = 0; i < f.size(); i++) {
			uf[i] = RLRuleFrame.inherit(f.get(i));
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
			 String ruleName = frs[i].getName();
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
		 List<Frame> f = getDeclaredChildFrames(RULE_LIST);

		 if (f == null) {
			 return new UserRuleFrame[0];
		 }

		 UserRuleFrame[] uf = new UserRuleFrame[f.size()];
		 for (int i = 0; i < f.size(); i++) {
			 uf[i] = new UserRuleFrame(f.get(i));
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
		 List<Frame> f = getChildFrames(RL_LIST);
		 RLRuleFrame[] uf = new RLRuleFrame[f.size()];
		 for (int i = 0; i < f.size(); i++) {
			 uf[i] = RLRuleFrame.inherit(f.get(i));
		 }
		 return uf;
	 }

	 public RLRuleFrame getRLRule(String name) {
		 RLRuleFrame[] frs = this.getRLRule();
		 for (int i = 0; i < frs.length; i++) {
			 if (frs[i].getName().equals(name)) {
				 return frs[i];
			 }
		 }
		 return null;
	 }

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
			 if (frs[i].getName().equals(name)) {
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
		 List<Frame> f = getChildFrames(RULE_LIST);
		 UserRuleFrame[] uf = new UserRuleFrame[f.size()];
		 for (int i = 0; i < f.size(); i++) {
			 uf[i] = new UserRuleFrame(f.get(i));
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

	 public void removeRLRule(String name) {

		 if (super.getDeclaredFrame(RL_LIST, name) == null && getChildFrame(RL_LIST, name.trim()) != null) {
			 throw new GSimDefException("This RLNode can't be removed, because it is defined in a parent of this frame.");
		 } else if (super.getDeclaredFrame(RL_LIST, name) == null && getChildFrame(RL_LIST, name) == null) {
			 throw new GSimDefException("RLNode " + name + " could not be found.");
		 }

		 removed.add(name);

		 super.removeChildFrame(RL_LIST, name);

	 }

	 public void removeRule(String name) {

		 if (super.getDeclaredFrame(RULE_LIST, name) == null && getChildFrame(RL_LIST, name) != null) {
			 throw new GSimDefException("This Rule can't be removed, because it is defined in a parent of this frame.");
		 } else if (super.getDeclaredFrame(RULE_LIST, name) == null && getChildFrame(RL_LIST, name) == null) {
			 throw new GSimDefException("Rule " + name + " could not be found.");
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

		 ActionFrame f = new ActionFrame("{}", "");
		 addOrSetChildFrame(ACTION_LIST, f);
		 addOrSetChildFrame(RULE_LIST, UserRuleFrame.newUserRuleFrame("{all-rules}"));

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

		 //public static RLRuleFrame RL_RULE_FRAME = new RLRuleFrame("template-rl-frame");
		 super.defineObjectList(RL_LIST, RLRuleFrame.newRLRuleFrame("template-rl-frame"));

	 }

}
