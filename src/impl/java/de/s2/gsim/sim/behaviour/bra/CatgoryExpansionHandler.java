package de.s2.gsim.sim.behaviour.bra;

import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionFactHelper.addStateFactCategoryElemFromStatefact;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionFactHelper.appendRemainingStateFactElems;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionFactHelper.existsEquivalent;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionFactHelper.expandStateDescription;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionFactHelper.insertNewActionNodes;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionRuleBuilder.createNewExperimentalRuleCat;
import static de.s2.gsim.sim.behaviour.util.FactHelper.extractCategoryElemSpec;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.sim.behaviour.GSimBehaviourException;
import de.s2.gsim.sim.behaviour.builder.TreeExpansionBuilder;
import de.s2.gsim.sim.behaviour.util.FactHelper.StateFactElemCategorySpec;
import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;

public class CatgoryExpansionHandler implements java.io.Serializable {

	private static Logger logger = Logger.getLogger(CatgoryExpansionHandler.class);

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
    public static boolean createNextStatesCat(RuntimeAgent agent, Fact stateFact, Fact toExpand, List<Fact> allElemsList, Context context,
			boolean copy) throws GSimBehaviourException {

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
            StateFactElemCategorySpec unexpandedElemsSpec = extractCategoryElemSpec(allElemsList, context, toExpandParamName);// contains
			if (unexpandedElemsSpec.facts.size() == 0) {
				Logger.getLogger(Rete.class).debug(" Statefact " + stateFactName + " is now fully expanded.");
				return false;
			} // this means that there are no elems of this state with a value for
			// this
			// parameter --> the selected fact is the last, and because there is only
			// one value, nothing can be expanded

			int oldDepth = (int) stateFact.getSlotValue("depth").floatValue(context);

			TreeExpansionBuilder b = new TreeExpansionBuilder(agent);

			// ceate and insert extracted new category (state fact, and elem-fact)
			List<String> fillersOfExpandAttribute = Arrays.asList(toExpandCategoryValue);
			List<String> fillersOfSiblingAttributes = unexpandedElemsSpec.fillers;

			String cat1 = toExpand.getSlotValue("category").stringValue(context);
			// the state descriptor:
			Fact newStateFactSplit_1 = expandStateDescription(stateFact, rootRule0, unexpandedElemsSpec.attributeSpec, context,
					oldDepth + 1, copy);
			String stateNameExpanded_New = newStateFactSplit_1.getSlotValue("name").stringValue(context);

			// the extracted value (=1 elem)
            Fact stateFactElem_1 = addStateFactCategoryElemFromStatefact(newStateFactSplit_1,
					toExpand.getSlotValue("param-name").stringValue(context), cat1, context);

			// StateFacts of other, non-selected attributes
			if (existsEquivalent(stateFact, new Fact[] { stateFactElem_1 }, context)) {
				return false;
			}

			context.getEngine().assertFact(stateFactElem_1);

			Fact newStateFactSplit_2 = expandStateDescription(stateFact, rootRule0, unexpandedElemsSpec.attributeSpec, context,
					oldDepth + 1, copy);
			// this is the generated name:
			String stateNameExpanded_Siblings = newStateFactSplit_2.getSlotValue("name").stringValue(context);

			// the elems holding the filler values for the remaining categories of
			// the state
			for (Fact s : unexpandedElemsSpec.facts) {
				String cat2 = s.getSlotValue("category").stringValue(context);
				// as spec holds the rest, stateFactElem_2 holds the elements disjunct
				// from the parameter value specified in stateFactElem_1. The union is the
				// set of parameter values defined by the parent state.
				Fact stateFactElem_2 = addStateFactCategoryElemFromStatefact(newStateFactSplit_2,
						toExpand.getSlotValue("param-name").stringValue(context), cat2, context);
				context.getEngine().assertFact(stateFactElem_2);
			}

			// original rule
			RLRule r0 = agent.getBehaviour().getRLRule(rootRule0);
			//

			// remaining list holds the original list of sf-elems except the to-expand-elem
			Fact[] remaining = new Fact[allElemsList.size()];
			allElemsList.toArray(remaining);

			// append state-elems of additional attributes (if more than 1 attribute present)
			appendRemainingStateFactElems(allElemsList, stateNameExpanded_New, context);
			appendRemainingStateFactElems(allElemsList, stateNameExpanded_Siblings, context);

			String newRule1 = createNewExperimentalRuleCat(b, r0, stateNameExpanded_New, unexpandedElemsSpec.attributeSpec,
					fillersOfExpandAttribute, remaining, context);
			context.getEngine().executeCommand(newRule1);
			String newRule2 = createNewExperimentalRuleCat(b, r0, stateNameExpanded_Siblings, unexpandedElemsSpec.attributeSpec,
					fillersOfSiblingAttributes, remaining, context);
			context.getEngine().executeCommand(newRule2);
			//System.out.println("===============\n" + newRule1);
			//System.out.println(newRule2 + "==================\n");
			logger.debug(newRule1);
			logger.debug(newRule2);

			insertNewActionNodes(context, stateFactName, stateNameExpanded_New);
			insertNewActionNodes(context, stateFactName, stateNameExpanded_Siblings);

			// deactivate old state
			stateFact.setSlotValue("active", new Value(0.0, RU.FLOAT));
			context.getEngine().retract(stateFact);

			logger.debug("=======END EXPAND AGENT:" + agent.getName() + "=================");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}


}
