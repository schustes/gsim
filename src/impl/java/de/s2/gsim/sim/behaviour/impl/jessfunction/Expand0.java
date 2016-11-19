package de.s2.gsim.sim.behaviour.impl.jessfunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.sim.behaviour.impl.FactHandler;
import de.s2.gsim.sim.behaviour.impl.TreeExpansionBuilder;

public class Expand0 extends DynamicRuleBuilder implements java.io.Serializable {

	private static Logger logger = Logger.getLogger(Expand0.class);

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param agent
	 * @param stateFact - the state representation
	 * @param toExpand - the element (part) of the state reprentation selected as new element in the next, new state
	 * @param stateElemsWithoutExpansion - list of all elements belonging to the sate
	 * @param context
	 * @return
	 * @throws JessException
	 */
	public boolean createNextStatesCat(RuntimeAgent agent, Fact stateFact, Fact toExpand, ArrayList<Fact> allElemsList, Context context,
	        boolean copy) throws JessException {

		try {

			logger.debug("=======BEGIN EXPEND AGENT:" + agent.getName() + "==============");

			logger.debug("Current rules:");
			StringBuffer defrules = new StringBuffer();
			Iterator<?> iter = context.getEngine().listDefrules();
			while (iter.hasNext()) {
				defrules.append(iter.next());
				defrules.append("\n");
			}
			logger.debug(defrules.toString());

			String stateFactName = stateFact.getSlotValue("name").stringValue(context);
			String toExpandParamName = toExpand.getSlotValue("param-name").stringValue(context);
			String toExpandCategoryValue = toExpand.getSlotValue("category").stringValue(context);

			String rootRule0 = stateFactName.split("_")[0];

			allElemsList.remove(toExpand);// important: list without the
			// extracted element >> that's why it works further down. Ugly.
			StateFactElemCategorySpec unexpandedElemsSpec = extractCategoryElemSpec(allElemsList, context, toExpandParamName);// contains
			// the fillers
			// of
			// other state-elems
			// for the same parameter

			if (unexpandedElemsSpec.facts.size() == 0) {
				Logger.getLogger(Rete.class).debug(" Statefact " + stateFactName + " is now fully expanded.");
				return false;
			} // this means that there are no elems of this state with a value for
			  // this
			  // parameter --> the selected fact is the last, and because there is only
			  // one value, nothing can be expanded

			int oldDepth = (int) stateFact.getSlotValue("depth").floatValue(context);

			// ----------------------------------------------//
			// now: create two states: one with the elems for the selected param,
			// one for ?

			TreeExpansionBuilder b = new TreeExpansionBuilder(agent);

			// ceate and insert extracted new category (state fact, and
			// elem-fact)
			// String toExpandAttributeSpec =
			// toExpand.getSlotValue("param-name").stringValue(context);
			List<String> fillersOfExpandAttribute = Arrays.asList(toExpandCategoryValue);
			List<String> fillersOfSiblingAttributes = unexpandedElemsSpec.fillers;

			String cat1 = toExpand.getSlotValue("category").stringValue(context);
			// the state descriptor:
			Fact newStateFactSplit_1 = expandStateDescription(stateFact, rootRule0, unexpandedElemsSpec.attributeSpec, context,
			        oldDepth + 1, copy);
			String stateNameExpanded_New = newStateFactSplit_1.getSlotValue("name").stringValue(context);

			// the extracted value (=1 elem)
			Fact stateFactElem_1 = super.addStateFactCategoryElem(newStateFactSplit_1,
			        toExpand.getSlotValue("param-name").stringValue(context), cat1, context);

			// StateFacts of other, non-selected attributes

			if (existsEquivalent(stateFact, new Fact[] { stateFactElem_1 }, context)) {
				return false;
			}

			context.getEngine().assertFact(stateFactElem_1);
			//

			// insert the - constant - remaining rest of the original state
			// ??? is the same???

			// the result: one state with one elem (the extracted), this state with
			// the
			// rest (e.g.: state1=a, state2=b or c. Old state a or b or c)
			// expand=create new Fact from parent 'stateFact' (new name is generated)
			Fact newStateFactSplit_2 = expandStateDescription(stateFact, rootRule0, unexpandedElemsSpec.attributeSpec, context,
			        oldDepth + 1, copy);
			// this is the generated name:
			String stateNameExpanded_Siblings = newStateFactSplit_2.getSlotValue("name").stringValue(context);

			// the elems holding the filler values for the remaining categories of
			// the state
			for (Fact s : unexpandedElemsSpec.facts) {
				String cat2 = s.getSlotValue("category").stringValue(context);
				// as spec holds the rest, stateFactElem_2 holds the elements disjunct
				// from
				// the parameter value specified in stateFactElem_1. The union is the
				// set
				// of parameter values defined by the parent state.
				Fact stateFactElem_2 = super.addStateFactCategoryElem(newStateFactSplit_2,
				        toExpand.getSlotValue("param-name").stringValue(context), cat2, context);
				context.getEngine().assertFact(stateFactElem_2);
			}

			// original rule
			// String rootRule0 = stateNameExpanded_New.split("_")[0];
			RLRule r0 = agent.getBehaviour().getRLRule(rootRule0);
			//

			// remaining list holds the original list of sf-elems except the
			// to-expand-elem
			Fact[] remaining = new Fact[allElemsList.size()];
			allElemsList.toArray(remaining);

			// append state-elems of additional attributes (if more than 1 attribute present)
			appendRemainingStateFactElemsCat(remaining, stateNameExpanded_New, context);
			appendRemainingStateFactElemsCat(remaining, stateNameExpanded_Siblings, context);

			if (r0.hasSelectors()) {
				context.getEngine().executeCommand(createNewSelectionNodesCat(b, r0, stateNameExpanded_New,
				        unexpandedElemsSpec.attributeSpec, fillersOfExpandAttribute, remaining, oldDepth + 1, context));
				context.getEngine().executeCommand(createNewSelectionNodesCat(b, r0, stateNameExpanded_Siblings,
				        unexpandedElemsSpec.attributeSpec, fillersOfSiblingAttributes, remaining, oldDepth + 1, context));
			} else {
				String newRule1 = createNewExperimentalRuleCat(b, r0, stateNameExpanded_New, unexpandedElemsSpec.attributeSpec,
				        fillersOfExpandAttribute, remaining, context);
				context.getEngine().executeCommand(newRule1);
				String newRule2 = createNewExperimentalRuleCat(b, r0, stateNameExpanded_Siblings, unexpandedElemsSpec.attributeSpec,
				        fillersOfSiblingAttributes, remaining, context);
				context.getEngine().executeCommand(newRule2);
				System.out.println("===============\n" + newRule1);
				System.out.println(newRule2 + "==================\n");
				logger.debug(newRule1);
				logger.debug(newRule2);
			}

			insertNewActionNodes(context, stateFactName, stateNameExpanded_New);
			insertNewActionNodes(context, stateFactName, stateNameExpanded_Siblings);

			// deactivate old state

			stateFact.setSlotValue("active", new Value(0.0, RU.FLOAT));
			context.getEngine().retract(stateFact);
			// context.getEngine().assertFact(stateFact);

			// recursivelyAddNewAttribute();

			logger.debug("=======END EXPAND AGENT:" + agent.getName() + "=================");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/*
	 * //the same as createNextStates(), but without splitting - simply add category, delete old rules, and create new ones up to root.
	 * private void recursivelyAddNewAttribute(Context context, Fact addedCategory) {
	 * 
	 * String attributeSpec=null; String attributeValue=null;
	 * 
	 * try {
	 * 
	 * String stateFactElemName = addedCategory.getSlotValue("name").stringValue(context); String stateFactName =
	 * addedCategory.getSlotValue("elem-parent").stringValue(context); String toExpandParamName =
	 * addedCategory.getSlotValue("param-name").stringValue(context); String toExpandCategoryValue =
	 * addedCategory.getSlotValue("category").stringValue(context);
	 * 
	 * Fact stateFact = FactHandler.getInstance().getFact("state-fact", stateFactName, context); String stateFactParent =
	 * stateFact.getSlotValue("parent").stringValue(context); Fact parentFact = FactHandler.getInstance().getFact("state-fact",
	 * stateFactParent, context);
	 * 
	 * //now create new statefactelem for elem-parent stateFactParent.
	 * 
	 * //then delete old rules and create new one with complete list of elems (?)
	 * 
	 * allElemsList.remove(toExpand);// important: list without the // extracted element >> that's why it works further down. Ugly.
	 * StateFactElemCategorySpec unexpandedElemsSpec = extractCategoryElemSpec( allElemsList, context, toExpandParamName);//contains the
	 * fillers of other state-elems //for the same parameter
	 * 
	 * if (unexpandedElemsSpec.facts.size() == 0) { Logger.getLogger(Rete.class).debug( " Statefact " + stateFactName +
	 * " is now fully expanded."); return false; } //this means that there are no elems of this state with a value for this //parameter -->
	 * the selected fact is the last, and because there is only //one value, nothing can be expanded
	 * 
	 * int oldDepth = (int) stateFact.getSlotValue("depth").floatValue( context); // ----------------------------------------------// //now:
	 * create two states: one with the elems for the selected param, //one for ?
	 * 
	 * TreeExpansionBuilder b = new TreeExpansionBuilder(agent); // ceate and insert extracted new category (state fact, and // elem-fact)
	 * // String toExpandAttributeSpec = // toExpand.getSlotValue("param-name").stringValue(context); String[] fillersOfExpandAttribute =
	 * new String[] { toExpandCategoryValue }; String[] fillersOfSiblingAttributes = unexpandedElemsSpec.fillers .toArray(new
	 * String[unexpandedElemsSpec.fillers.size()]);
	 * 
	 * String cat1 = toExpand.getSlotValue("category") .stringValue(context); //the state descriptor: Fact newStateFactSplit_1 =
	 * expandStateDescription(stateFact, unexpandedElemsSpec.attributeSpec, context, oldDepth + 1); String stateNameExpanded_New =
	 * newStateFactSplit_1.getSlotValue( "name").stringValue(context);
	 * 
	 * //the extracted value (=1 elem) Fact stateFactElem_1 = super.addStateFactCategoryElem(newStateFactSplit_1, cat1, context);
	 * 
	 * if (existsEquivalent(stateFact, new Fact[] { stateFactElem_1 }, context)) { return false; }
	 * 
	 * context.getEngine().assertFact(stateFactElem_1); // // insert the - constant - remaining rest of the original state //??? is the
	 * same???
	 * 
	 * //the result: one state with one elem (the extracted), this state with the //rest (e.g.: state1=a, state2=b or c. Old state a or b or
	 * c) //expand=create new Fact from parent 'stateFact' (new name is generated) Fact newStateFactSplit_2 =
	 * expandStateDescription(stateFact, unexpandedElemsSpec.attributeSpec, context, oldDepth + 1); //this is the generated name: String
	 * stateNameExpanded_Siblings = newStateFactSplit_2 .getSlotValue("name").stringValue(context);
	 * 
	 * //the elems holding the filler values for the remaining categories of //the state for (Fact s : unexpandedElemsSpec.facts) { String
	 * cat2 = s.getSlotValue("category").stringValue(context); //as spec holds the rest, stateFactElem_2 holds the elements disjunct from
	 * //the parameter value specified in stateFactElem_1. The union is the set //of parameter values defined by the parent state. Fact
	 * stateFactElem_2 = addStateFactCategoryElem(newStateFactSplit_2, cat2, context); context.getEngine().assertFact(stateFactElem_2); } //
	 * original rule String rootRule0 = stateNameExpanded_New.split("_")[0]; RLRule r0 = agent.getBehaviour().getRLRule(rootRule0); // //
	 * remaining list holds the original list of sf-elems except the // to-expand-elem Fact[] remaining = new Fact[allElemsList.size()];
	 * allElemsList.toArray(remaining); if (r0.hasSelectors()) { context.getEngine().executeCommand( this.createNewSelectionNodesCat(b, r0,
	 * stateNameExpanded_New, unexpandedElemsSpec.attributeSpec, fillersOfExpandAttribute, remaining, oldDepth + 1, context));
	 * context.getEngine().executeCommand( this.createNewSelectionNodesCat(b, r0, stateNameExpanded_Siblings,
	 * unexpandedElemsSpec.attributeSpec, fillersOfSiblingAttributes, remaining, oldDepth + 1, context)); } else {
	 * context.getEngine().executeCommand( this.createNewExperimentalRuleCat(b, r0, stateNameExpanded_New,
	 * unexpandedElemsSpec.attributeSpec,//--> List with all elems of parent plus added child-statefact. fillersOfExpandAttribute,
	 * remaining, context)); context.getEngine() .executeCommand( this.createNewExperimentalRuleCat(b, r0, stateNameExpanded_Siblings,
	 * unexpandedElemsSpec.attributeSpec, fillersOfSiblingAttributes, remaining, context)); } insertNewActionNodes(context,
	 * stateNameExpanded_New);
	 * 
	 * insertNewActionNodes(context, stateNameExpanded_Siblings);
	 * 
	 * recursivelyAddNewAttribute(); } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * 
	 * throw new UnsupportedOperationException("Do something about it"); }
	 */
	public void createNextStatesNum(RuntimeAgent agent, Fact stateFact, Fact toExpand, ArrayList<Fact> allElems, Context context,
	        boolean copy) throws JessException {

		try {
			String stateFactName = stateFact.getSlotValue("name").stringValue(context);
			// String toExpandParamName = toExpand.getSlotValue("param-name").stringValue(context);
			// String toExpandCategoryValue = toExpand.getSlotValue("category").stringValue(context);

			String rootRule0 = stateFactName.split("_")[0];

			allElems.remove(toExpand);

			Fact[] remaining = new Fact[allElems.size()];
			allElems.toArray(remaining);

			int oldDepth = (int) stateFact.getSlotValue("depth").floatValue(context);

			String paramName = toExpand.getSlotValue("param-name").stringValue(context);

			double from = toExpand.getSlotValue("from").floatValue(context);
			double to = toExpand.getSlotValue("to").floatValue(context);
			double dist = Math.abs(from - to);

			Fact stateFact_split1 = expandStateDescription(stateFact, paramName, rootRule0, context, oldDepth + 1, copy);
			Fact stateFact_split2 = expandStateDescription(stateFact, paramName, rootRule0, context, oldDepth + 1, copy);

			String newStateName1 = stateFact_split1.getSlotValue("name").stringValue(context);
			String newStateName2 = stateFact_split2.getSlotValue("name").stringValue(context);

			Fact stateFactElem_split1 = addStateFactIntervalElem(newStateName1, toExpand, from, from + dist / 2d, context);
			Fact stateFactElem_split2 = addStateFactIntervalElem(newStateName2, toExpand, from + dist / 2d, to, context);

			if (existsEquivalent(stateFact, new Fact[] { stateFactElem_split1 }, context)) {
				return;
			}

			context.getEngine().assertFact(stateFactElem_split1);
			context.getEngine().assertFact(stateFactElem_split2);

			// append state-elems of additional attributes (if more than 1 attribute present)
			appendRemainingStateFactElemsInterval(remaining, newStateName1, context);
			appendRemainingStateFactElemsInterval(remaining, newStateName2, context);

			TreeExpansionBuilder b = new TreeExpansionBuilder(agent);

			RLRule rootRule = agent.getBehaviour().getRLRule(newStateName1.split("_")[0]);

			if (rootRule.hasSelectors()) {
				context.getEngine().executeCommand(
				        createNewSelectionNodes(b, rootRule, agent, newStateName1, stateFactElem_split1, remaining, context));
				context.getEngine().executeCommand(
				        createNewSelectionNodes(b, rootRule, agent, newStateName2, stateFactElem_split2, remaining, context));
			} else {
				context.getEngine().executeCommand(createNewExperimentalRule(b, rootRule, agent, newStateName1, remaining, context,
				        paramName, from, from + dist / 2d));
				context.getEngine().executeCommand(
				        createNewExperimentalRule(b, rootRule, agent, newStateName2, remaining, context, paramName, from + dist / 2d, to));
			}

			context.getEngine().assertFact(stateFact_split1);
			context.getEngine().assertFact(stateFact_split2);

			insertNewActionNodes(context, stateFactName, newStateName1);
			insertNewActionNodes(context, stateFactName, newStateName2);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void appendRemainingStateFactElemsCat(Fact[] remaining, String stateFactName, Context context) throws JessException {
		for (Fact f : remaining) {
			if (f.getDeftemplate().getBaseName().equals("state-fact-category")) {
				String attributeName = f.getSlotValue("param-name").stringValue(context);
				String categoryValue = f.getSlotValue("category").stringValue(context);
				String name = attributeName + "->" + String.valueOf(categoryValue);
				FactHandler.getInstance().addStateFactCat(context.getEngine(), stateFactName, name, attributeName, categoryValue);
			}
		}
	}

	private void appendRemainingStateFactElemsInterval(Fact[] remaining, String stateFactName, Context context) throws JessException {
		for (Fact f : remaining) {
			if (f.getDeftemplate().getBaseName().equals("state-fact-element")) {
				String attributeName = f.getSlotValue("param-name").stringValue(context);
				String from = f.getSlotValue("from").stringValue(context);
				String to = f.getSlotValue("to").stringValue(context);

				String name = attributeName + "->" + String.valueOf(from) + ":" + String.valueOf(to);

				FactHandler.getInstance().addStateFactElement(context.getEngine(), stateFactName, name, attributeName,
				        Double.parseDouble(from), Double.parseDouble(to));
			}
		}
	}

	private boolean equals(ValueVector v1, ValueVector v2) {
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

	private boolean existsEquivalent(Fact parent, Fact[] sfes, Context ctx) {

		boolean existsCombination = true;

		try {
			String parentSfn = parent.getSlotValue("name").stringValue(ctx);
			ValueVector v = parent.getSlotValue("expansion").listValue(ctx);
			ArrayList<Fact> facts = FactHandler.getInstance().getStateFactElems(parentSfn, ctx);
			for (Fact f : sfes) {
				facts.add(f);
			}
			ArrayList<Fact> all = FactHandler.getInstance().getAllStateFacts(ctx);

			for (Fact sf : all) {
				ValueVector v1 = sf.getSlotValue("expansion").listValue(ctx);

				if (this.equals(v1, v)) {
					ArrayList<Fact> f0 = FactHandler.getInstance().getStateFactElems(sf.getSlotValue("name").stringValue(ctx), ctx);

					for (int i = 0; i < v1.size(); i++) {

						String param = v1.get(i).stringValue(ctx);
						boolean exists = true;

						for (Fact oldFact : f0) {
							String param2 = oldFact.getSlotValue("param-name").stringValue(ctx);

							boolean e = false;

							if (param.equals(param2) && oldFact.getDeftemplate().getBaseName().equals("state-fact-category")) {
								String cat = oldFact.getSlotValue("category").stringValue(ctx);
								for (Fact sf0 : sfes) {
									if (sf0.getDeftemplate().getBaseName().equals("state-fact-category")) {
										String cat2 = sf0.getSlotValue("category").stringValue(ctx);
										if (cat2 != null && cat2.equals(cat)) {
											e = true;
										}
									}
								}
								if (!e) {
									exists = false;
								}
							} else if (param.equals(param2)) {
								if (param.equals(param2) && oldFact.getSlotValue("from") != null) {
									double from = oldFact.getSlotValue("from").floatValue(ctx);
									double to = oldFact.getSlotValue("to").floatValue(ctx);

									for (Fact sf0 : sfes) {
										if (sf0.getDeftemplate().getBaseName().equals("state-fact-elemen")) {
											double from1 = sf0.getSlotValue("from").floatValue(ctx);
											double to1 = sf0.getSlotValue("to").floatValue(ctx);

											if (from == from1 && to == to1) {
												e = true;
											}
										}
									}
									if (!e) {
										exists = false;
									}
								}
							}
						}
						if (!exists) {
							existsCombination = false;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return existsCombination;
	}

	/**
	 * Creates the new state-fact.
	 * 
	 * @param oldDesc
	 * @param paramName
	 * @param context
	 * @param depth
	 * @param where
	 * @return
	 * @throws JessException
	 */
	private Fact expandStateDescription(Fact oldDesc, String rootRuleName, String paramName, Context context, int depth, boolean copy)
	        throws JessException {

		// String ruleName = oldDesc.getSlotValue("rule").stringValue(context);
		int t = getTime(context.getEngine());
		Fact f1 = null;
		if (!copy) {
			f1 = FactHandler.getInstance().addStateFact(context.getEngine(), oldDesc, rootRuleName, paramName, depth, t);
		} else {
			f1 = FactHandler.getInstance().copyStateFact(context.getEngine(), oldDesc, paramName, depth, t);
		}
		return f1;

	}

	private StateFactElemCategorySpec extractCategoryElemSpec(ArrayList<Fact> allElemsList, Context context, String toExpandParamName)
	        throws JessException {

		Iterator iter = allElemsList.iterator();
		StateFactElemCategorySpec spec = new StateFactElemCategorySpec();

		while (iter.hasNext()) {
			Fact f = (Fact) iter.next();
			String pn1 = f.getSlotValue("param-name").stringValue(context);
			if (toExpandParamName.equals(pn1)) {
				spec.facts.add(f);
				spec.attributeSpec = pn1;
				spec.fillers.add(f.getSlotValue("category").stringValue(context));
				iter.remove();
			}
		}

		return spec;
	}

	private class StateFactElemCategorySpec {

		public String attributeSpec = ""; // parameter name. e.g. 'profit'

		public ArrayList<Fact> facts = new ArrayList<Fact>();// facts containing
		// the parameter

		public ArrayList<String> fillers = new ArrayList<String>();// possible
		// fillers
		// present

	}

}
