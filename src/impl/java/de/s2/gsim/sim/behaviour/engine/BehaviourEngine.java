package de.s2.gsim.sim.behaviour.engine;


import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.RLRuleFrame;
import de.s2.gsim.environment.UserRule;
import de.s2.gsim.environment.UserRuleFrame;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.GSimBehaviourException;
import de.s2.gsim.sim.behaviour.bra.Expand;
import de.s2.gsim.sim.behaviour.bra.SimpleSoftmaxSelector;
import de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeUpdateStrategy;
import de.s2.gsim.sim.behaviour.rulebuilder.BuildingUtils;
import de.s2.gsim.sim.behaviour.rulebuilder.GlobalsBuilder;
import de.s2.gsim.sim.behaviour.rulebuilder.ReactiveRuleBuilder;
import de.s2.gsim.sim.behaviour.util.RuleEngineHelper;
import de.s2.gsim.sim.behaviour.util.TreeWriter;
import jess.Deftemplate;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;

public class BehaviourEngine implements java.io.Serializable {

	private static Logger logger = Logger.getLogger(BehaviourEngine.class);

	private static final long serialVersionUID = 1L;

	public Rete rete = new Rete();

	private Set<String> currentConditionSet = new HashSet<String>();

	private boolean DEBUG = true;

	private String debugDir = "/home/gsim/tmp";

	private boolean dirty = false;

	private int episodeStart = -1;

	// private HashSet<String> objectParams = new HashSet<String>();

	private transient RuntimeAgent owner = null;

	private RLParameterRanges rlRanges;

	private boolean rulesChanged = true;

	private double time = 0; // used for learning rules

	private String treeDir = null;

	private int writeTreeToDB = -1;

	public BehaviourEngine() {
		clean();
	}

	public BehaviourEngine(RuntimeAgent ownerAgent, Map<String, Object> props) throws GSimEngineException {
		String s = System.getProperty("debugJess");
		String treeDB = System.getProperty("writeTreeDBInterval");
		String treeFile = System.getProperty("writeTreeFile");
		if (treeDB != null) {
			writeTreeToDB = Integer.parseInt(treeDB);
		}
		if (treeFile != null) {
			treeDir = treeFile;
		}
		if (s != null) {
			DEBUG = true;
			String s2 = System.getProperty("debugJessDir");
			if (s2 != null) {
				debugDir = s2;
			}
		}
		clean();
		createEngine(ownerAgent, props);
		rlRanges = new RLParameterRanges();
		currentConditionSet = buildUniqueConditionSet(ownerAgent);
		initRLRanges();
	}

	public void checkRLParams() {
		for (RLRule r : owner.getBehaviour().getRLRules()) {
			for (ExpansionDef e : r.getExpansions()) {
				// Path<DomainAttribute> framePath = Path.attributePath(e.getParameterName().split("/"));
				Optional<DomainAttribute> domainAttribute = BuildingUtils.extractAttribute(owner.getDefinition(), e.getParameterName());// owner.getDefinition().resolvePath(framePath);
				if (domainAttribute.isPresent()) {
					DynamicValueRangeUpdateStrategy strategy = DynamicValueRangeUpdateStrategy
					        .getStrategyForAttributeType(domainAttribute.get().getType());

					// strategy.apply(owner, r.getName(), e, rlRanges, rete.getGlobalContext());

				}
			}
		}
	}

	public void destroy() {
		try {
			if (rete != null) {
				rete.reset();
				rete.clear();
				rete.clearFocusStack();
				rete.store("AGENT", null);
				rete.store("query-result", null);
				rete.clearStorage();
				rete = null;
				owner = null;
			}
		} catch (JessException e) {
			logger.debug("Some problem with JessHandler.destroy()" + e.getMessage());
		}
	}

	/**
	 * Executes rules for the current role and time. Note: The rulebase is not cleared, so only rules will become activated that were
	 * defined for this role.
	 * 
	 * @param role String
	 * @param modelTime int
	 */
	// static BuilderUtils u = new BuilderUtils();
	public void executeUserRules(String role, Map<?, ?> globals) {
		try {

			long total = System.currentTimeMillis();

			Constant roleParameter = new Constant("executing-role", role);

			reset();

			checkRLParams();

			long l = System.currentTimeMillis();
			insertGlobalVariableValues(globals);
			rete.run();

			logger.debug("rules on globals: " + ((System.currentTimeMillis() - l) / 1000d));

			l = System.currentTimeMillis();

			// question: must this be executed any time a new rule is inserted?
			if (rulesChanged) {
				currentConditionSet = this.buildUniqueConditionSet(owner);
				rulesChanged = false;
			}

			JessHandlerUtils.assertCurrentStateIntoRulebase(rete, owner, currentConditionSet);
			addNewActions();

			logger.debug("build state: " + ((System.currentTimeMillis() - l) / 1000d));

			if (hasReactiveRules(role)) {
				if (DEBUG) {
					printFacts("****** FACTS BEFORE REACTIVE ******");
				}
				l = System.currentTimeMillis();
				assertParameter(new Constant("exec-r", "dc"));
				assertParameter(roleParameter);
				rete.run();
				logger.debug("rules on reactive: " + ((System.currentTimeMillis() - l) / 1000d));
				if (DEBUG) {
					printFacts("****** FACTS AFTER REACTIVE ******");
				}
			}

			if (DEBUG) {
				printFacts("****** FACTS BEFORE SHORT ******");
			}
			l = System.currentTimeMillis();
			assertParameter(new Constant("exec-sc", "dc"));
			assertParameter(roleParameter);
			rete.run();
			logger.debug("rules on short: " + ((System.currentTimeMillis() - l) / 1000d));
			if (DEBUG) {
				printFacts("****** FACTS AFTER SHORT ******");
			}
			// }
			if (hasRLRules(role)) {
				if (DEBUG) {
					printFacts("****** FACTS BEFORE RLRule ******");
				}
				l = System.currentTimeMillis();
				assertParameter(new Constant("exec-RLRule", "dc"));
				assertParameter(roleParameter);
				rete.run();
				logger.debug("rules on RLRule: " + ((System.currentTimeMillis() - l) / 1000d));
				if (DEBUG) {
					printFacts("****** FACTS AFTER RLRule ******");
				}
			}

			printTree(role);

			logger.debug("EXECUTE RULES FOR AGENT " + owner.getName() + ", role:" + role + ": "
			        + ((System.currentTimeMillis() - total) / 1000d));

			dirty = true;

			long time = (Long) globals.get("TIME");
			TreeWriter f = new TreeWriter();
			f.output(this.owner.getName() + "_" + "step_" + time, rete, debugDir);

		} catch (JessException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Iterate over all parameters in conditions and extract the common subset to prevent unnecessary duplication of conditions expressed in
	 * strings.
	 * 
	 * @param owner
	 * @return
	 * @throws JessException
	 */
	private Set<String> buildUniqueConditionSet(RuntimeAgent owner) {

		Set<String> uniqueConditions = new HashSet<String>();

		for (Instance r : owner.getBehaviour().getChildInstances(BehaviourFrame.RULE_LIST)) {
			for (Instance cc : r.getChildInstances(UserRuleFrame.INST_LIST_COND)) {
				ConditionDef c = new ConditionDef(cc);
				uniqueConditions.add(c.getParameterName());
				uniqueConditions.add(c.getParameterValue());
			}
		}

		for (Instance r : owner.getBehaviour().getChildInstances(BehaviourFrame.RL_LIST)) {
			for (Instance cc : r.getChildInstances(UserRuleFrame.INST_LIST_COND)) {
				ConditionDef c = new ConditionDef(cc);
				uniqueConditions.add(c.getParameterName());
				uniqueConditions.add(c.getParameterValue());
			}
			for (Instance cc : r.getChildInstances(RLRuleFrame.INST_LIST_EXP)) {
				ExpansionDef c = new ExpansionDef(cc);
				uniqueConditions.add(c.getParameterName());
				uniqueConditions.add(String.valueOf(c.getMin()));
				uniqueConditions.add(c.getParameterName());
				uniqueConditions.add(String.valueOf(c.getMax()));
			}

			for (Instance sc : r.getChildInstances(RLRuleFrame.INST_LIST_SHORTCUTS)) {
				for (Instance cc : sc.getChildInstances(UserRuleFrame.INST_LIST_COND)) {
					ConditionDef c = new ConditionDef(cc);
					if (!c.getParameterName().contains("$")) {
						throw new RuntimeException("Parse error: selectors must reference action-nodes on their LHS");
					}
					uniqueConditions.add(c.getParameterName());
					uniqueConditions.add(c.getParameterValue());
				}

			}
			for (Instance eval : r.getChildInstances(RLRuleFrame.INST_LIST_LEARNING)) {
				if (!eval.getName().startsWith("{")) {
					ConditionDef c = new ConditionDef(eval);
					uniqueConditions.add(c.getParameterName());
					uniqueConditions.add(c.getParameterValue());
				}
			}

		}

		return uniqueConditions;

	}
	public void initRLRanges() {
		for (RLRule r : owner.getBehaviour().getRLRules()) {
			for (ExpansionDef e : r.getExpansions()) {
				String path = e.getParameterName();
				// DomainAttribute a = (DomainAttribute) this.owner.getDefinition().resolveName(path.split("/"));
				DomainAttribute a = extractAtt(path);
				if (a.getType() == AttributeType.SET) {
					rlRanges.initCategoricalParameters(path, e.getFillers());// e!
					// because
					// default
					// values...
				} else if (a.getType() == AttributeType.INTERVAL) {
					rlRanges.initIntervalParameterRange(path, new double[] { e.getMin(), e.getMax() });
					// new double[] { Double.parseDouble(e.getFillers().get(0)), Double.parseDouble(a.getFillers().get(1)) });
				}
			}
		}
	}

	public void initRLRanges_old() {
		for (RLRule r : owner.getBehaviour().getRLRules()) {
			for (ExpansionDef e : r.getExpansions()) {
				String path = e.getParameterName();
				Path<DomainAttribute> p = Path.attributePath(path.split("/"));
				// DomainAttribute a = (DomainAttribute) owner.getDefinition().resolveName(path.split("/"));
				DomainAttribute a = owner.getDefinition().resolvePath(p);
				if (a.getType() == AttributeType.SET) {
					rlRanges.initCategoricalParameters(path, a.getFillers());
				} else if (a.getType() == AttributeType.INTERVAL) {
					rlRanges.initIntervalParameterRange(path,
					        new double[] { Double.parseDouble(a.getFillers().get(0)), Double.parseDouble(a.getFillers().get(1)) });
				}
			}
		}
	}

	public void printTree(String role) {

		if (treeDir != null && time % writeTreeToDB == 0) {
			TreeWriter f = new TreeWriter();
			f.output(owner.getName(), rete, treeDir);
		}

	}

	/**
	 * retract all facts except 'static' ones (those used for RLRule). Also retracts the role-facts - with the change to sinlge inheritance
	 * this shouldn't cause problems, because now agetns with several roles execute overriden rules in the execution context where the
	 * original was defined. For example, if Frame a with context x overrides rule y in Frame b, but frame b as a different exeuction
	 * context than a, then the rule will not be executed when Frame a's role turn is, but (still) when Frame b's turn is.
	 */
	public void reset() {

		try {

			Iterator<?> iter = rete.listFacts();

			while (iter.hasNext()) {

				Fact f = (Fact) iter.next();
				if (!f.getDeftemplate().getBaseName().equals("rl-action-node") && !f.getDeftemplate().getBaseName().equals("average-reward")
				        && !f.getDeftemplate().getBaseName().startsWith("state-fact") && !f.getDeftemplate().getBaseName().equals("list")) {
					rete.retract(f);
				}
			}

		} catch (Exception e) {

			// logger.debug(y + ":::: >>>>>>>>>>>>>>>>>>>>>>>" + current);

			e.printStackTrace();
			System.exit(0);
			/*
			 * Iterator iter = this.rete.listActivations(); while (iter.hasNext()) { Activation a = (Activation) iter.next(); //
			 * logger.debug(a.getRule().getName()); }
			 */
			// logger.debug(y + ":::: >>>>>>>>>>>>>>>>>>>>>>>" + current);
		}

	}

	public void retractConstant(String paramName) {
		try {
			Iterator<?> iter = rete.listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("parameter")) {
					String s = f.getSlotValue("name").stringValue(rete.getGlobalContext());
					if (s.equals(paramName)) {
						rete.retract(f);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void retractExecutedFact(String qualifiedName) {
		try {
			Iterator<?> iter = rete.listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("RLRule-action-node")) {
					ValueVector s = f.getSlotValue("arg").listValue(rete.getGlobalContext());
					for (int i = 0; i < s.size(); i++) {
						String str = (s.get(i)).stringValue(rete.getGlobalContext());
						if (str.equals(qualifiedName)) {
							rete.retract(f);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setOwner(RuntimeAgent ownerAgent) {

		if (rete != null) {
			rete.store("AGENT", ownerAgent);
			try {
				rete.executeCommand("(bind ?*agent* = (fetch AGENT))");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		owner = ownerAgent;
	}

	public void startEpisode() throws GSimBehaviourException {
		if (episodeStart > -1) {
			throw new GSimBehaviourException("Only one episode can be running at a time");
		}
		episodeStart = (int) time;
	}

	/**
	 * Updates only variables that are referenced in the evaluators of RLRule-nodes.
	 */
	public void updateRewards() {

		if (!dirty) {
			return;
		}

		long l = System.currentTimeMillis();

		reset2();

		double timeLocal = time + 1;

		try {
			Deftemplate p = rete.findDeftemplate("timer");
			Fact f = new Fact(p);
			f.setSlotValue("time", new Value(timeLocal, RU.FLOAT));
			rete.assertFact(f);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (RLRule c : owner.getBehaviour().getRLRules()) {
			ConditionDef cond = c.getEvaluationFunction();
			if (cond != null) {
				String attName = cond.getParameterName();
				Path<Attribute> path = Path.attributePath(attName.split("/"));
				Attribute ref = (Attribute) owner.resolvePath(path);
				// Attribute ref = (Attribute) owner.resolveName(attName.split("/"));
				if (ref != null) {
					retractConstant(cond.getParameterName());

					if (ref instanceof IntervalAttribute) {
						double val = (((IntervalAttribute) ref).getFrom() + ((IntervalAttribute) ref).getTo() / 2d);
						JessHandlerUtils.assertParameter(rete, attName, String.valueOf(val));
					} else {
						JessHandlerUtils.assertParameter(rete, attName, ref.toValueString());
					}

					// JessHandlerUtils.assertParameter(rete, attName,
					// ref.toValueString());
				} else {
					logger.debug("Condition in RLRule node " + c.getName() + " refers to variable " + attName
					        + ", but that variable does at the moment not exist in agent " + owner.getName() + " of type "
					        + owner.getDefinition().getName() + ".");
				}
			}
		}

		try {
			if (DEBUG) {
				printFacts("*****FACTS BEFORE UPDATE REWARDS (and rete.run()) *******\n");
			}
			assertParameter(new Constant("exec-update-rewards", "dc"));
			rete.run();
			if (DEBUG) {
				printFacts("*****FACTS AFTER UPDATE REWARDS (and rete.run()) *******\n");
			}
		} catch (JessException e) {
			e.printStackTrace();
		}

		logger.debug("update rewards: " + ((System.currentTimeMillis() - l) / 1000d));
		dirty = false;
	}

	private void addNewActions() throws JessException {
		for (Instance r : owner.getBehaviour().getChildInstances(BehaviourFrame.RL_LIST)) {
			String sfn = r.getName() + "_0" + 0;
			RuleEngineHelper.insertNonExistentExecutedFinalFacts(rete, owner, RLRule.fromInstance(r), sfn);
		}
	}

	private void assertParameter(Constant param) {
		Fact f = null;
		try {
			Deftemplate p = rete.findDeftemplate("parameter");
			String val = param.getValue();

			if (val == null) {
				val = "0";
			}
			f = new Fact(p);
			f.setSlotValue("name", new Value(param.getName(), RU.STRING));

			if (de.s2.gsim.util.Utils.isNumerical(val)) {
				f.setSlotValue("value", new Value(Double.valueOf(val).doubleValue(), RU.FLOAT));
			} else {
				f.setSlotValue("value", new Value(val, RU.STRING));
			}
			rete.assertFact(f);
		} catch (JessException e) {
			f = null;
			e.printStackTrace();
		}

	}

	private void clean() {
		try {
			File dir = new File(debugDir);
			File[] contents = dir.listFiles();
			long now = System.currentTimeMillis();
			for (File f : contents) {
				File[] files = f.listFiles();
				for (File file : files) {
					if (now - file.lastModified() > 1000 * 60 * 60 * 12) {
						file.delete();
					}
				}
			}

		} catch (Exception e) {

		}
	}

	private void createEngine(RuntimeAgent a, Map<String, Object> props) throws GSimEngineException {

		owner = a;

		double maxReward = 1;
		if (props.containsKey("MAX_REWARD")) {
			maxReward = Double.parseDouble((String) props.get("MAX_REWARD"));
		} else {
			// logger.debug("******* NO MAX REWARD ********");
		}

		props.put("MAX_DEPTH", a.getBehaviour().getMaxDepth());
		props.put("MAX_NODE_COUNT", a.getBehaviour().getMaxNodes());

		rete = new Rete();
		rete.store("AGENT", owner);

		String script = "";
		GlobalsBuilder g = new GlobalsBuilder();
		String s0 = g.build(rete, props);
		if (s0.length() > 0) {
			script += s0 + "\n";
		}

		// RLRulesBuilder b = new RLRulesBuilder(this.owner);
		RuleEngineFactory b = new RuleEngineFactory(owner);
		s0 = b.build(rete);
		if (s0.length() > 0) {
			script += s0 + "\n";
		}
		ReactiveRuleBuilder c = new ReactiveRuleBuilder(owner);
		s0 = c.build(rete);
		if (s0.length() > 0) {
			script += s0;
		}

		if (DEBUG) {
			printScript(script);
		}

		// rete.addUserfunction(new ComparisonSelector());
		rete.addUserfunction(new SimpleSoftmaxSelector(maxReward));
		// rete.addUserfunction(new ToInstPath());
		rete.addUserfunction(new Expand());

	}

	private DomainAttribute extractAtt(String path) {
		Optional<DomainAttribute> a = BuildingUtils.extractAttribute(owner.getDefinition(), path);
		return a.orElse(null);

		// DomainAttribute ret = null;
		// if (BuildingUtils.referencesChildFrame(owner.getDefinition(), path)) {
		// Optional<DomainAttribute> a = BuildingUtils.extractAttribute(agent.getDefinition(), path);
		// return a.orElse(null);
		// String obj = BuildingUtils.resolveChildFrameWithList(this.owner.getDefinition(), path);
		// Frame f = null;
		// try {
		// f = (Frame) owner.getDefinition().resolvePath(Path.objectPath(obj.split("/")));
		// } catch (GSimDefException e) {
		// logger.debug("Object with path + " + obj + " doesn't seem to be present yet.");
		// }
		// if (f == null) {
		// String list = BuildingUtils.resolveList(path);
		// f = owner.getDefinition().getListType(list);
		// }
		// String att = BuildingUtils.extractChildAttributePathWithoutParent(owner.getDefinition(), path).get().toString();
		// a = f.resolvePath(Path.attributePath(att.split("/")));
		// } else {
		// a = owner.getDefinition().resolvePath(Path.attributePath(path.split("/")));
		// }
		// return a;
	}

	private boolean hasReactiveRules(String role) {
		for (UserRule rule : owner.getBehaviour().getRules()) {
			String definingRole = BuildingUtils.getDefiningRoleForRule(owner, rule.getName());
			if (definingRole.equals(role)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasRLRules(String role) {
		for (RLRule rule : owner.getBehaviour().getRLRules()) {
			String definingRole = BuildingUtils.getDefiningRoleForRLRule(owner, rule.getName());
			if (definingRole.equals(role)) {
				return true;
			}
		}
		return false;
	}

	private void insertGlobalVariableValues(Map<?, ?> globals) {

		time = (Long) globals.get("TIME");

		try {
			Deftemplate p = rete.findDeftemplate("timer");
			Fact f = new Fact(p);
			f.setSlotValue("time", new Value(time, RU.FLOAT));
			rete.assertFact(f);

			JessHandlerUtils.assertParameter(rete, "current-agent-count", (String) globals.get("AGENT_COUNT"));

			JessHandlerUtils.assertParameter(rete, "exec-interval", (String) globals.get("INTERVAL"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printFacts(String head) {
		String v = head + "\n";
		Iterator<?> it = rete.listFacts();
		while (it.hasNext()) {
			v += ((Fact) it.next()).toString() + "\n";
		}

		try {
			File dir = new File(debugDir + "/facts");
			if (!dir.exists()) {
				System.out.println(dir.mkdirs());
			}
			FileWriter f = new FileWriter(dir.getCanonicalPath() + "/FACTS-" + owner.getName() + ".txt", true);
			f.write(v);
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void printScript(String s) {
		try {
			File dir = new File(debugDir + "/rules/");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			FileWriter f = new FileWriter(dir.getCanonicalPath() + "/RULES-" + owner.getName() + ".txt");
			f.write(s);
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void reset2() {
		try {
			rete.halt();
			Iterator<?> iter = rete.listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().startsWith("exec")) {
					rete.retract(f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void deleteDirContents(String dirName) {
		File dir = new File(dirName);
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				file.delete();
			}
		} else {
			dir.mkdirs();
		}
	}

	private static class Constant implements java.io.Serializable {

		private static final long serialVersionUID = 7594202672886051100L;

		private String name = "test";

		private String value = "";

		public Constant(String name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Constant) {
				Constant c = (Constant) o;
				return c.getValue().equals(getValue()) && c.getName().equals(getName());
			}
			return false;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			return 89;
		}

	};

	public class RLParameterRanges implements Serializable {

		private static final long serialVersionUID = 1L;

		private HashMap<String, ArrayList<String>> categories;

		private HashMap<String, double[]> intervals;

		public RLParameterRanges() {
			intervals = new HashMap<String, double[]>();
			categories = new HashMap<String, ArrayList<String>>();
		}

		public ArrayList<String> getNewCategoricalParameterValues(String attName, List<String> allFillers) {
			ArrayList<String> contained = categories.get(attName);
			ArrayList<String> result = new ArrayList<String>();
			if (contained == null) {
				ArrayList<String> list = new ArrayList<String>();
				for (String s : allFillers) {
					list.add(s);
				}
				categories.put(attName, list);
			} else {

				for (String s : allFillers) {
					boolean exists = false;
					for (String t : contained) {
						if (t.equals(s)) {
							exists = true;
						}
					}
					if (!exists) {
						contained.add(s);
						result.add(s);
					}
				}
			}
			return result;
		}

		/**
		 * 
		 * @param attName
		 * @param currentInterval
		 * @return null if there are no updates for the interval
		 */
		public double[] getNewIntervalParameterRange(String attName, double[] currentInterval) {

			double[] contained = intervals.get(attName);
			double[] result = null;
			if (contained == null) {
				intervals.put(attName, currentInterval);
			} else {
				boolean changed = false;
				if (contained[0] > currentInterval[0]) {
					contained[0] = currentInterval[0];
					changed = true;
				}

				if (contained[1] < currentInterval[1]) {
					contained[1] = currentInterval[1];
					changed = true;
				}

				if (changed) {
					result = contained;
				}
			}
			return result;

		}

		public boolean attributeValueRangeChanged(String attName, double[] currentInterval) {

			if (!intervals.containsKey(attName)) {
				return true;
			}

			double[] contained = intervals.get(attName);

			return (contained[0] > currentInterval[0] || contained[1] < currentInterval[1]);

		}

		public void initCategoricalParameters(String attName, List<String> values) {
			ArrayList<String> list = new ArrayList<String>();
			for (String s : values) {
				list.add(s);
			}
			categories.put(attName, list);
		}

		public void initIntervalParameterRange(String attName, double[] values) {
			intervals.put(attName, values);
		}
	};

	static {
		deleteDirContents("/home/gsim/tmp/agentsrv-logging");
		deleteDirContents("/home/gsim/tmp/facts");
		deleteDirContents("/home/gsim/tmp/trees");
		deleteDirContents("/home/gsim/tmp/rules");
	}

}
