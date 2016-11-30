package de.s2.gsim.sim.behaviour.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.sim.behaviour.impl.jessfunction.DynamicRuleBuilder;
import de.s2.gsim.sim.behaviour.impl.jessfunction.Expand0;
import de.s2.gsim.sim.behaviour.util.CollectiveTreeDBWriter;
import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.Rete;

public abstract class RLRulesUpdate {

	private static String debugDir = "/home/gsim/tmp/trees";

	/**
	 * Modify rules, statefacts and statefact category values with new filler
	 * 
	 * @param agent owning agent
	 * @param pathToParameter the path to the referenced attribute that changed its value range
	 * @param newFiller the new value in the value range
	 * @param ctx rete context
	 */
	public static void update(RuntimeAgent agent, String baseRuleName, String resolvedAttributeName, String newFiller, Context context) {

		DynamicRuleBuilder builder = new DynamicRuleBuilder();
		TreeExpansionBuilder treeBuilder = new TreeExpansionBuilder(agent);

		try {

			ArrayList<Fact> allStates = getStateFactsForRootRule(baseRuleName, context);
			List<Fact> selectedStates = chooseStates(allStates, context);

			for (Fact state : selectedStates) {

				String stateName = state.getSlotValue("name").stringValue(context);

				String expansionRuleName = "experimental_rule_" + baseRuleName + "@" + stateName + "@";
				RLRule baseRule = agent.getBehaviour().getRLRule(baseRuleName);

				Fact[] elems = getStateElems(stateName, context);

				// clone the rule associated with the state, but only if the rule belongs to the set of rules to which the new category is
				// added
				// (1 rule <-> 1 state)
				String newRule = builder.addCategoryToExperimentalRule(treeBuilder, agent, baseRule, stateName, elems,
				        resolvedAttributeName, newFiller, context);
				// add the new element to this state (exactly 1 at a time)
				builder.addStateFactCategoryElemFromStatefact(state, resolvedAttributeName, newFiller, context);

				Rete rete = context.getEngine();

				CollectiveTreeDBWriter f = new CollectiveTreeDBWriter();
				f.output("before_deepening", rete, debugDir);

				// delete rule on current level
				deleteRule(expansionRuleName, context);
				context.getEngine().executeCommand(newRule);

				// make new expansion one level deeper - why?
				double depth = state.getSlotValue("depth").floatValue(context);
				if (depth > 0) {
					// System.out.println(">>>>>>>>>EXPANDDD>>>>>>>>>>>>>>>>");
					// expandOriginalNodeToDeeperLevel(stateName, newFiller, context);
				}
				f = new CollectiveTreeDBWriter();
				f.output("after_deepening", rete, debugDir);
			}

		} catch (JessException e) {
			e.printStackTrace();
		}
	}

	public static void update(RuntimeAgent agent, String baseRuleName, String resolvedAttributeName, double min, double max,
	        Context context) {

		DynamicRuleBuilder builder = new DynamicRuleBuilder();
		TreeExpansionBuilder treeBuilder = new TreeExpansionBuilder(agent);

		try {

			ArrayList<Fact> allStates = getStateFactsForRootRule(baseRuleName, context);
			List<Fact> selectedStates = chooseStates(allStates, context);

			for (Fact state : selectedStates) {

				String stateName = state.getSlotValue("name").stringValue(context);

				String expansionRuleName = "experimental_rule_" + baseRuleName + "@" + stateName + "@";
				RLRule baseRule = agent.getBehaviour().getRLRule(baseRuleName);

				Fact[] elems = getStateElems(stateName, context);

				// to do add interval equivalent
				String newRule = builder.increaseIntervalRangeInExperimentalRule(treeBuilder, agent, baseRule, stateName, elems,
				        resolvedAttributeName, min, max, context);

                // builder.addStateFactIntervalElemFromParentElem(resolvedAttributeName, state, min, max, context);
                builder.addStateFactIntervalElemFromStatefact(stateName, resolvedAttributeName, state, min, max, context);
                // builder.addStateFactIntervalElemFromStatefact(resolvedAttributeName, state, min, max, context);

				Rete rete = context.getEngine();

				CollectiveTreeDBWriter f = new CollectiveTreeDBWriter();
				f.output("before_deepening", rete, debugDir);

				deleteRule(expansionRuleName, context);
				context.getEngine().executeCommand(newRule);

				f = new CollectiveTreeDBWriter();
				f.output("after_deepening", rete, debugDir);
			}

		} catch (JessException e) {
			e.printStackTrace();
		}
	}

	// select the state-fact-elems to which the new category is appended
	private static ArrayList<Fact> chooseStates(ArrayList<Fact> states, Context context) {

		ArrayList<Fact> set = new ArrayList<Fact>();

		try {

			HashMap<Fact, Integer> map = new HashMap<Fact, Integer>();

			int depth = -1;

			for (Fact state : states) {

				double active = state.getSlotValue("active").floatValue(context);

				if (active > 0) {

					String stateName = state.getSlotValue("name").stringValue(context);

					Fact[] elems = getStateElems(stateName, context);

					int d = (int) Double.parseDouble(state.getSlotValue("depth").stringValue(context));

					if (d > depth) {
						// map with state-name:elemcount

						if (map.containsKey(stateName)) {
							if (map.get(state) < elems.length) {
								map.put(state, elems.length);
							}
						} else {
							map.put(state, elems.length);
						}
					}
				}
			}

			Fact selected = null;
			int random = cern.jet.random.Uniform.staticNextIntFromTo(0, map.size() - 1);
			ArrayList<Fact> list = new ArrayList<Fact>();
			list.addAll(map.keySet());
			selected = list.get(random);
			/*
			 * for (Fact state : map.keySet()) { int len = map.get(state); if (len > maxLen) { maxLen = len; selected = state; } }
			 */

            addAllStateAncestors(set, context, selected, states);

			set.add(selected);

		} catch (JessException e) {
			e.printStackTrace();
		}

		return set;
	}



	private static void expandOriginalNodeToDeeperLevel(RuntimeAgent agent, String stateName, String newCategory, Context context)
	        throws JessException {
		Expand0 impl = new Expand0();

        ArrayList<Fact> allStateFactElems = ReteHelper.getInstance().getStateFactElems(stateName, context);

        Fact stateFact = ReteHelper.getInstance().getStateFact(stateName, context);

		Fact elemToExpand = null;
		int depth = -1;
		for (Fact f : allStateFactElems) {
			String cName = f.getSlotValue("category").stringValue(context);
            Fact sf = ReteHelper.getInstance().getStateFact(f.getSlotValue("state-fact-name").stringValue(context), context);

			double d = sf.getSlotValue("depth").floatValue(context);
			if (cName.equals(newCategory) && d > depth) {
				elemToExpand = f;
				depth = (int) d;
			}
		}

		try {
			impl.createNextStatesCat(agent, stateFact, elemToExpand, allStateFactElems, context, false);

		} catch (Exception e) {
			throw new JessException("", "", e);
		}

	}

	private static Fact[] getStateElems(String parentName, Context context) {

		ArrayList<Fact> ret = new ArrayList<Fact>();

		try {
			Iterator iter = context.getEngine().listFacts();
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

	private static Fact[] getStateFactsElems(String parentName, Context context) {

		ArrayList<Fact> ret = new ArrayList<Fact>();

		try {
			Iterator iter = context.getEngine().listFacts();
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

	private static ArrayList<Fact> getStateFactsForRootRule(String name, Context context) {
		ArrayList<Fact> ret = new ArrayList<Fact>();
		try {
			Iterator iter = context.getEngine().listFacts();
			while (iter.hasNext()) {
				Fact f = (Fact) iter.next();
				if (f.getDeftemplate().getBaseName().equals("state-fact")) {
					String s1 = f.getSlotValue("name").stringValue(context);
					if (s1.startsWith(name)) {
						ret.add(f);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}


}
