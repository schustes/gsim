package de.s2.gsim.sim.behaviour.util;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.BehaviourDef;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.util.CombinationGenerator;
import de.s2.gsim.util.Utils;
import jess.Context;
import jess.Deftemplate;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class RuleEngineHelper {

	private RuleEngineHelper() {
		// empty
	}

	public static void deleteRule(String ruleName, Context context) {
		try {
			context.getEngine().executeCommand("(undefrule " + ruleName + ")");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Fact addStateFact(Rete rete, Fact oldFact, String rootRuleName, String param, int depth, int time) throws JessException {

		Deftemplate p = rete.findDeftemplate("state-fact");
		Fact f = new Fact(p);
		String parentName = oldFact.getSlotValue("name").stringValue(rete.getGlobalContext());
		String ctx = oldFact.getSlotValue("context").stringValue(rete.getGlobalContext());
		double oldValue = oldFact.getSlotValue("value").floatValue(rete.getGlobalContext());
		double oldCount = oldFact.getSlotValue("count").floatValue(rete.getGlobalContext());

		int uid = UniqueIDGenerator.getNext();
		String stateName = parentName.split("_")[0] + "_" + depth + "" + uid;
		f.setSlotValue("name", new Value(stateName, RU.STRING));
		f.setSlotValue("context", new Value(ctx, RU.STRING));
		f.setSlotValue("parent", new Value(parentName, RU.STRING));
		f.setSlotValue("created", new Value((double) time, RU.FLOAT));
		f.setSlotValue("last-activation", new Value(-1.0, RU.FLOAT));
		f.setSlotValue("active", new Value(1.0, RU.FLOAT));
		f.setSlotValue("expansion-count", new Value(0.0, RU.FLOAT));
		f.setSlotValue("leaf", new Value(0.0, RU.FLOAT));
		f.setSlotValue("value", new Value(oldValue, RU.FLOAT));
		f.setSlotValue("depth", new Value((float) depth, RU.FLOAT));
		f.setSlotValue("count", new Value(oldCount, RU.FLOAT));

		ValueVector v1 = oldFact.getSlotValue("expansion").listValue(rete.getGlobalContext());
		ValueVector v3 = new ValueVector();
		boolean added = false;
		for (int i = 0; i < v1.size(); i++) {
			Value v = v1.get(i);
			String s = v.stringValue(rete.getGlobalContext());
			if (!s.contains(param)) {
				v3.add(new Value(s, RU.STRING));
			} else if (!added) {
				v3.add(new Value(param, RU.STRING));
				// v3.add(new Value(param + ">=" + from, RU.STRING));
				// v3.add(new Value(param + "<" + to, RU.STRING));
				added = true;
			}
		}
		f.setSlotValue("expansion", new Value(v3, RU.LIST));

		String ruleName = "experimental_rule_" + rootRuleName + "@" + stateName + "@";// +
		f.setSlotValue("rule", new Value(ruleName, RU.STRING));

		rete.assertFact(f);

		return f;

	}


	public static Fact addStateFactCat(Rete rete, Fact oldFact, String stateFactName, String name, String attrName, String categoryValue)
	        throws JessException {

		Deftemplate p = rete.findDeftemplate("state-fact-category");
		Fact f = new Fact(p);

		f.setSlotValue("name", new Value(name, RU.STRING));
		f.setSlotValue("param-name", new Value(attrName, RU.STRING));
		f.setSlotValue("state-fact-name", new Value(stateFactName, RU.STRING));
		f.setSlotValue("elem-parent", new Value(oldFact.getSlotValue("name").stringValue(rete.getGlobalContext()), RU.STRING));
		f.setSlotValue("value", new Value(0.0, RU.FLOAT));
		f.setSlotValue("category", new Value(categoryValue, RU.STRING));

		rete.assertFact(f);

		return f;
	}

	/**
	 * Assigns a state-fact-category to an existing state-fact.
	 * 
	 * @param rete
	 * @param stateFactName the existing state-fact
	 * @param name
	 * @param attrName
	 * @param categoryValue
	 * @return
	 * @throws JessException
	 */
	public static Fact addStateFactCat(Rete rete, String stateFactName, String name, String attrName, String categoryValue)
	        throws JessException {

		Deftemplate p = rete.findDeftemplate("state-fact-category");

		Fact f = new Fact(p);

		f.setSlotValue("name", new Value(name, RU.STRING));
		f.setSlotValue("param-name", new Value(attrName, RU.STRING));
		f.setSlotValue("state-fact-name", new Value(stateFactName, RU.STRING));
		f.setSlotValue("value", new Value(0.0, RU.FLOAT));
		f.setSlotValue("category", new Value(categoryValue, RU.STRING));

		rete.assertFact(f);

		return f;
	}

	public static Fact addStateFactElement(Rete rete, String parentName, String stateFactName, String name, String param, double from,
	        double to) throws JessException {

		Deftemplate p = rete.findDeftemplate("state-fact-element");
		Fact f = new Fact(p);

		f.setSlotValue("name", new Value(name, RU.STRING));
		f.setSlotValue("param-name", new Value(param, RU.STRING));
		f.setSlotValue("state-fact-name", new Value(stateFactName, RU.STRING));
		f.setSlotValue("elem-parent", new Value(parentName, RU.STRING));
		f.setSlotValue("value", new Value(0.0, RU.FLOAT));
		f.setSlotValue("from", new Value(from, RU.FLOAT));
		f.setSlotValue("to", new Value(to, RU.FLOAT));

		rete.assertFact(f);

		return f;
	}

	public static void deleteFactByName(Rete rete, Fact fact) throws JessException {
		rete.retract(fact);
	}

	public static Fact addStateFactElement(Rete rete, String stateFactName, String name, String paramName, double from, double to)
	        throws JessException {

		Deftemplate p = rete.findDeftemplate("state-fact-element");

		Fact f = new Fact(p);

		f.setSlotValue("name", new Value(name, RU.STRING));
		f.setSlotValue("param-name", new Value(paramName, RU.STRING));
		f.setSlotValue("state-fact-name", new Value(stateFactName, RU.STRING));
		f.setSlotValue("value", new Value(0.0, RU.FLOAT));
		f.setSlotValue("from", new Value(from, RU.FLOAT));
		f.setSlotValue("to", new Value(to, RU.FLOAT));

		rete.assertFact(f);

		return f;
	}

	// creates a new statefact, but keeps the values of the original one.
	public static Fact copyStateFact(Rete rete, Fact oldFact, String param, int depth, int time) throws JessException {

		Deftemplate p = rete.findDeftemplate("state-fact");
		Fact f = new Fact(p);
		String parentName = oldFact.getSlotValue("name").stringValue(rete.getGlobalContext());
		double oldValue = oldFact.getSlotValue("value").floatValue(rete.getGlobalContext());
		double oldCount = oldFact.getSlotValue("count").floatValue(rete.getGlobalContext());
		String ctx = oldFact.getSlotValue("context").stringValue(rete.getGlobalContext());

		int uid = UniqueIDGenerator.getNext();
		f.setSlotValue("name", new Value(parentName.split("_")[0] + "_" + depth + "" + uid, RU.STRING));
		f.setSlotValue("context", new Value(ctx, RU.STRING));
		f.setSlotValue("parent", new Value(parentName, RU.STRING));
		f.setSlotValue("created", new Value((double) time, RU.FLOAT));
		f.setSlotValue("last-activation", new Value(oldFact.getSlotValue("last-activation").floatValue(rete.getGlobalContext()), RU.FLOAT));
		f.setSlotValue("active", new Value(1.0, RU.FLOAT));
		f.setSlotValue("expansion-count", new Value(oldFact.getSlotValue("expansion-count").floatValue(rete.getGlobalContext()), RU.FLOAT));
		f.setSlotValue("leaf", new Value(oldFact.getSlotValue("leaf").floatValue(rete.getGlobalContext()), RU.FLOAT));
		f.setSlotValue("value", new Value(oldValue, RU.FLOAT));
		f.setSlotValue("depth", new Value((float) depth, RU.FLOAT));
		f.setSlotValue("count", new Value(oldCount, RU.FLOAT));

		ValueVector v1 = oldFact.getSlotValue("expansion").listValue(rete.getGlobalContext());
		ValueVector v3 = new ValueVector();
		boolean added = false;
		for (int i = 0; i < v1.size(); i++) {
			Value v = v1.get(i);
			String s = v.stringValue(rete.getGlobalContext());
			if (!s.contains(param)) {
				v3.add(new Value(s, RU.STRING));
			} else if (!added) {
				v3.add(new Value(param, RU.STRING));
				// v3.add(new Value(param + ">=" + from, RU.STRING));
				// v3.add(new Value(param + "<" + to, RU.STRING));
				added = true;
			}
		}
		f.setSlotValue("expansion", new Value(v3, RU.LIST));

		f.setSlotValue("rule", new Value(oldFact.getSlotValue("rule").stringValue(rete.getGlobalContext()), RU.STRING));

		rete.assertFact(f);

		return f;

	}

	public static Fact createActionFact(Rete rete, String name, String evalFunc, double alpha, ValueVector args, String sfn)
	        throws JessException {

		Deftemplate p = rete.findDeftemplate("rl-action-node");
		Fact f = new Fact(p);

		f.setSlotValue("action-name", new Value(name, RU.STRING));
		f.setSlotValue("state-fact-name", new Value(sfn, RU.STRING));
		f.setSlotValue("function", new Value(evalFunc, RU.STRING));
		f.setSlotValue("value", new Value(0.5, RU.FLOAT));
		f.setSlotValue("alpha", new Value(alpha, RU.FLOAT));
		f.setSlotValue("arg", new Value(args, RU.LIST));
		f.setSlotValue("updateCount", new Value(0.0, RU.FLOAT));
		f.setSlotValue("count", new Value(0.0, RU.FLOAT));
		f.setSlotValue("time", new Value(-1.0, RU.FLOAT));
		f.setSlotValue("active", new Value(1.0, RU.FLOAT));

		return f;

	}

	@SuppressWarnings("unchecked")
	public static List<Fact> getActionsByNonMatchingArgs(String actionClassName, List<String> params, Context context) {
		List<Fact> result = new ArrayList<Fact>();
		try {
			Iterator<Fact> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = iter.next();
				if (f.getDeftemplate().getBaseName().equals("rl-action-node")
				        && f.getSlotValue("action-name").stringValue(context).contains(actionClassName)) {
					ValueVector vv = f.getSlotValue("arg").listValue(context);
					int n = 0;
					for (int i = 0; i < vv.size(); i++) {
						Value v = vv.get(i);
						for (String param : params) {
							String s = v.stringValue(context);
							if (s.equals(param)) {
								n++;
							}
						}
					}
					if (n == 0) {
						result.add(f);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

	public static ArrayList<Fact> getAllStateFacts(Context context) {

		ArrayList<Fact> list = new ArrayList<Fact>();
		try {
			Iterator<?> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("state-fact")) {
					list.add(f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	public static Fact getFact(String defName, String name, Context context) {

		try {
			Iterator<?> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals(defName)) {
					String s1 = f.getSlotValue("name").stringValue(context);
					if (s1.equals(name)) {
						return f;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static ArrayList<Fact> getFacts(String defName, String sfn, Context context) {

		ArrayList<Fact> list = new ArrayList<Fact>();
		try {
			Iterator<?> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals(defName)) {
					String s1 = f.getSlotValue("state-fact-name").stringValue(context);
					if ((sfn != null && s1.equals(sfn)) || (s1.equals("nil"))) {
						list.add(f);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	public static Fact getStateFact(String sfn, Context context) {

		try {

			Iterator<?> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("state-fact")) {
					String s1 = f.getSlotValue("name").stringValue(context);
					if (s1.equals(sfn)) {
						return f;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	// get the fact-elem, if not eq by name, look up the tree
	public static ArrayList<Fact> getStateFactElems(String sfn, Context context) {

		Set<Fact> list = new HashSet<Fact>();
		try {

			Iterator<?> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("state-fact-element")
				        || f.getDeftemplate().getBaseName().equals("state-fact-category")) {
					String s1 = f.getSlotValue("state-fact-name").stringValue(context);
					if (s1.equals(sfn)) {
						list.add(f);
					}
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<Fact>(list);

	}

	public static ArrayList<Fact> getStateFactElems(String sfn, String paramName, Context context, ArrayList<Fact> list) {

		String nextParent = null;
		try {
			Iterator<?> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("state-fact-element")
				        || f.getDeftemplate().getBaseName().equals("state-fact-category")) {
					String s1 = f.getSlotValue("param-name").stringValue(context);
					String sfn0 = f.getSlotValue("state-fact-name").stringValue(context);
					if (sfn0.equals(sfn)) {

						if (s1.equals(paramName)) {
							if (!contains(list, f, context)) {
								list.add(f);
							}
						} else {
							Fact state = getFact("state-fact", sfn, context);
							nextParent = state.getSlotValue("parent").stringValue(context);
						}
					}
				}
			}
			if (nextParent != null && list.isEmpty()) {
				return getStateFactElems(nextParent, paramName, context, list);
			} else {
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		// return new ArrayList<Fact>(map.values());

	}

	public static void insertGlobalAvgFacts(RLRule r, String[] evalFuncNames, Rete rete) throws JessException {
		String[] s = evalFuncNames;// getEvaluationFuncNames(agent);

		for (int i = 0; i < s.length; i++) {
			Deftemplate p = rete.findDeftemplate("average-reward");

			Fact f = new Fact(p);
			f.setSlotValue("function", new Value(s[i], RU.STRING));
			f.setSlotValue("value", new Value(0.0, RU.FLOAT));
			f.setSlotValue("time", new Value(-1.0, RU.FLOAT));

			rete.assertFact(f);
		}
	}

	public static void insertInitialStateFact(Rete rete, RuntimeAgent owner, RLRule r) throws JessException, GSimEngineException {

		Deftemplate p = rete.findDeftemplate("state-fact");

		Fact f = new Fact(p);

		String stateName = r.getName() + "_" + 0 + "" + 0;
		String ruleName = "experimental_rule_" + r.getName() + "@" + stateName + "@";
		String context = BuildingUtils.getDefiningRoleForRLRule(owner, r.getName());

		f.setSlotValue("name", new Value(stateName, RU.STRING));
		f.setSlotValue("created", new Value(0.0, RU.FLOAT));
		f.setSlotValue("active", new Value(1.0, RU.FLOAT));
		f.setSlotValue("expansion-count", new Value(0.0, RU.FLOAT));
		f.setSlotValue("leaf", new Value(0.0, RU.FLOAT));
		f.setSlotValue("count", new Value(0.0, RU.FLOAT));
		f.setSlotValue("value", new Value(0.0, RU.FLOAT));
		f.setSlotValue("depth", new Value(0.0, RU.FLOAT));
		f.setSlotValue("last-activation", new Value(-1.0, RU.FLOAT));
		f.setSlotValue("rule", new Value(ruleName, RU.STRING));
		f.setSlotValue("context", new Value(context, RU.STRING));

		ValueVector v1 = new ValueVector();

		for (ExpansionDef cond : r.getExpansions()) {
			v1.add(new Value(cond.getParameterName(), RU.STRING));
		}

		for (ExpansionDef cond : r.getExpansions()) {

			// logger.debug(cond.getFillers().length);

			String from = String.valueOf(cond.getMin());
			String to = String.valueOf(cond.getMax());
			Fact sf = null;


			if (referencesNumericalAttribute(owner, cond.getParameterName())) {
				// if (Utils.isNumericalAttribute(owner, cond.getParameterName())) {
				sf = addStateFactElement(rete, stateName, cond.getParameterName() + "->" + from + ":" + to, cond.getParameterName(),
				        cond.getMin(), cond.getMax());
				rete.assertFact(sf);
			} else {
				for (String filler : cond.getFillers()) {
					sf = addStateFactCat(rete, stateName, cond.getParameterName() + "->" + filler, cond.getParameterName(), filler);
					rete.assertFact(sf);
				}
			}
		}

		f.setSlotValue("expansion", new Value(v1, RU.LIST));

		rete.assertFact(f);

	}
	
	private static boolean referencesNumericalAttribute(RuntimeAgent owner, String path) {
		Optional<DomainAttribute> a = BuildingUtils.extractAttribute(owner.getDefinition(), path);
		if (a.isPresent()) {
			return a.get().getType().equals(AttributeType.INTERVAL) || a.get().getType().equals(AttributeType.NUMERICAL);
		}
		return false;

	}

	public static void insertListFact(RLRule r, Rete rete) throws JessException {
		String ident = createRuleIdentifier(r);// +"@"+createRuleIdentifier(r);
		Deftemplate p = rete.findDeftemplate("list");
		Fact f = new Fact(p);
		f.setSlotValue("name", new Value(ident, RU.STRING));
		f.setSlotValue("obj", new Value(new ArrayList<>()));
		rete.assertFact(f);
	}

	public static void insertNonExistentExecutedFinalFacts(Rete rete, RuntimeAgent agent, RLRule r, String sfn) throws JessException {

		String evaluationAttributeName = r.getEvaluationFunction().getParameterName();
		String s = r.getEvaluationFunction().getParameterValue();
		double alpha = Double.parseDouble(s);

		for (ActionDef action : r.getConsequents()) {

			String[] params = action.getObjectClassParams();

			if (params != null && params.length > 0) {

				ArrayList<String> parameters = new ArrayList<String>();
				int[] starts = new int[params.length];
				int[] ends = new int[params.length];
				int n = 0;
				int k = 0;
				for (String param : params) {
					starts[n] = k;
					for (Instance inst : Utils.getChildInstancesOfType(agent, param)) {
						String filter = action.getFilterExpression(param);
						if (inst.getName().matches(".*" + filter + ".*")) {
							k++;
							parameters.add(Utils.toInstPath(agent, param, inst.getName()));
						}
					}
					ends[n] = k;
					n++;
				}
				if (k > 0 && k >= params.length) { // otherwise, there are not
					// instances for all the
					// specified parameter types
					CombinationGenerator g = new CombinationGenerator(k, params.length);
					while (g.hasMore()) {
						int[] indices = g.getNext();
						boolean[] listPositions = new boolean[params.length];
						for (int idx : indices) {
							for (int j = 0; j < starts.length; j++) {
								if (starts[j] <= idx && ends[j] > idx) {
									listPositions[j] = true;
								}
							}
						}
						boolean cont = true;
						for (boolean b : listPositions) {
							if (!b) {
								cont = false;
							}
						}
						if (cont) {
							String[] paramInstances = new String[params.length];
							int j = 0;
							for (int i : indices) {
								paramInstances[j] = parameters.get(i);
								j++;
							}
							String[] signature = new String[paramInstances.length];
							for (int i = 0; i < paramInstances.length; i++) {
								String instancePath = paramInstances[i];
								for (int ii = 0; ii < params.length; ii++) {
									if (instancePath.startsWith(params[ii].split("/")[0].trim())) {
										signature[ii] = instancePath;
									}
								}
							}

							Fact f = createActionNode(rete, sfn, signature, evaluationAttributeName, alpha, action);
							if (!ruleBaseContainsExecutedFact(rete, f)) {
								rete.assertFact(f);
							}
						}
					}
					// end combinations

				}
			} else {
				Fact f = createActionNode(rete, sfn, new String[0], evaluationAttributeName, alpha, action);
				if (!ruleBaseContainsExecutedFact(rete, f)) {
					rete.assertFact(f);
				}
			}
		}
	}

	public static void parseAndInsertGlobalRLFacts(RuntimeAgent agent, Rete rete) {
		try {
			for (RLRule g : agent.getBehaviour().getRLRules()) {
				if (g.isActivated()) {
					String sfn = g.getName() + "_0" + 0;
					insertNonExistentExecutedFinalFacts(rete, agent, g, sfn);

					if (!g.containsAttribute("equivalent-state") && !g.containsAttribute("equivalent-actionset")) {
						insertGlobalAvgFacts(g, getEvaluationFunctionNames(agent), rete);
						insertListFact(g, rete);
					}

				}
			}
			for (RLRule g : agent.getBehaviour().getRLRules()) {
				insertInitialStateFact(rete, agent, g);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Fact[] getStateFactsCategories(String parentName, Context context) {

		ArrayList<Fact> ret = new ArrayList<Fact>();

		try {
			Iterator<?> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("state-fact-category")) {
					String s1 = f.getSlotValue("state-fact-name").stringValue(context);
					if (s1.equals(parentName)) {
						ret.add(f);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret.toArray(new Fact[ret.size()]);
	}



	public static Fact[] getStateFactsElems(String parentName, Context context) {

		ArrayList<Fact> ret = new ArrayList<Fact>();

		try {
			Iterator<?> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("state-fact-element")) {
					String s1 = f.getSlotValue("state-fact-name").stringValue(context);
					if (s1.equals(parentName)) {
						ret.add(f);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret.toArray(new Fact[ret.size()]);
	}

	public static List<Fact> getStateFactsForRootRule(String ruleName, Context context) {
		ArrayList<Fact> ret = new ArrayList<Fact>();
		try {
			Iterator<?> iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("state-fact")) {
					String s1 = f.getSlotValue("name").stringValue(context);
					if (s1.startsWith(ruleName)) {
						ret.add(f);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	private static boolean contains(List<Fact> list, Fact f, Context context) {
		try {
			for (Fact a : list) {
				if (f.getDeftemplate().getBaseName().equals("state-fact-category")) {
					String s1 = f.getSlotValue("category").stringValue(context);
					String s2 = a.getSlotValue("category").stringValue(context);
					if (s1.equals(s2)) {
						return true;
					}
				} else {
					double s1a = f.getSlotValue("from").floatValue(context);
					double s1b = f.getSlotValue("to").floatValue(context);
					double s2a = a.getSlotValue("from").floatValue(context);
					double s2b = a.getSlotValue("to").floatValue(context);
					if (s1a == s2a && s1b == s2b) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static Fact createActionNode(Rete rete, String sfn, String[] args, String evaluator, double alpha, ActionDef action)
	        throws JessException {

		Deftemplate p = rete.findDeftemplate("rl-action-node");

		Fact f = new Fact(p);

		f.setSlotValue("action-name", new Value(action.getClassName(), RU.STRING));
		f.setSlotValue("state-fact-name", new Value(sfn, RU.STRING));
		f.setSlotValue("function", new Value(evaluator, RU.STRING));

		f.setSlotValue("value", new Value(0.0, RU.FLOAT));

		f.setSlotValue("alpha", new Value(alpha, RU.FLOAT));
		f.setSlotValue("updateCount", new Value(0.0, RU.FLOAT));
		f.setSlotValue("count", new Value(0.0, RU.FLOAT));
		f.setSlotValue("time", new Value(-1.0, RU.FLOAT));
		f.setSlotValue("active", new Value(1.0, RU.FLOAT));

		ValueVector v2 = new ValueVector();
		for (String inst : args) {
			v2.add(new Value(inst, RU.STRING));
		}

		f.setSlotValue("arg", new Value(v2, RU.LIST));

		return f;
	}

	private static String createRuleIdentifier(Instance inst) {
		String x = inst.getName();
		x = x.replace(' ', '_');
		x = x.replace('/', '_');
		x = x.replace(',', '_');
		x = x.replace(')', ']');
		x = x.replace('(', '[');
		return x;
	}

	private static boolean equals(ValueVector v1, ValueVector v2) {
		try {
			for (int i = 0; i < v1.size(); i++) {
				boolean eq = false;
				for (int j = 0; j < v2.size(); j++) {
					if (v1.get(i).equals(v2.get(j))) {
						eq = true;
					}
				}
				if (!eq) {
					return false;
				}
			}
			return true;
		} catch (JessException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static String[] getEvaluationFunctionNames(RuntimeAgent a) {
		HashSet<String> set = new HashSet<String>();
		BehaviourDef b = a.getBehaviour();
		List<RLRule> rules = b.getRLRules();
		for (RLRule r : rules) {
			ActionDef[] c = r.getConsequents();
			for (int j = 0; j < c.length; j++) {
				set.add(r.getEvaluationFunction().getParameterName());
			}
		}
		String[] ret = new String[set.size()];
		set.toArray(ret);
		return ret;
	}

	private static boolean ruleBaseContainsExecutedFact(Rete rete, Fact fact) {

		try {
			Iterator<?> iter = rete.listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("rl-action-node")) {
					String actionType = f.getSlotValue("action-name").stringValue(rete.getGlobalContext());
					String actionType2 = fact.getSlotValue("action-name").stringValue(rete.getGlobalContext());
					String stateName1 = f.getSlotValue("state-fact-name").stringValue(rete.getGlobalContext());
					String stateName2 = fact.getSlotValue("state-fact-name").stringValue(rete.getGlobalContext());
					ValueVector s1 = f.getSlotValue("arg").listValue(rete.getGlobalContext());
					ValueVector s2 = fact.getSlotValue("arg").listValue(rete.getGlobalContext());

					if (equals(s1, s2) && actionType.equals(actionType2) && stateName1.equals(stateName2)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static class UniqueIDGenerator {

		private static int counter = 1;

		public static int getNext() {
			return counter++;
		}
	}

}
