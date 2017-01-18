package de.s2.gsim.sim.behaviour.rangeupdate;

import static cern.jet.random.Uniform.staticNextIntFromTo;
import static de.s2.gsim.sim.behaviour.bra.StateFactHelper.addStateFactCategoryElemFromStatefact;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionRuleBuilder.addCategoryToExperimentalRule;
import static de.s2.gsim.sim.behaviour.util.FactUtils.getFloatSlotValue;
import static de.s2.gsim.sim.behaviour.util.RuleEngineHelper.deleteRule;
import static de.s2.gsim.sim.behaviour.util.RuleEngineHelper.getStateFactsCategories;
import static de.s2.gsim.sim.behaviour.util.RuleEngineHelper.getStateFactsForRootRule;

import java.util.ArrayList;
import java.util.List;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.sim.behaviour.engine.BehaviourEngine.RLParameterRanges;
import de.s2.gsim.sim.behaviour.util.PathDefinitionResolutionUtils;
import de.s2.gsim.sim.behaviour.util.TreeWriter;
import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.Rete;

/**
 * Strategy implementation for interval attributes.
 * 
 * @author stephan
 *
 */
public class DynamicCategoryUpdateStrategyImpl implements DynamicValueRangeUpdateStrategy {

	private static String debugDir = "/home/gsim/tmp/trees";

	@Override
	public void apply(RuntimeAgent agent, String baseRuleName, ExpansionDef exp, RLParameterRanges rlRanges, Context context) {

		String evalPath = exp.getParameterName();
		Path<Attribute> ePath = Path.attributePath(evalPath.split("/"));

		if (PathDefinitionResolutionUtils.referencesChildFrame(agent.getDefinition(), evalPath)) {
			Frame referencedObject = PathDefinitionResolutionUtils.extractChildType(agent.getDefinition(), exp.getParameterName());
			String frameName = referencedObject.getName();
			List<Instance> actualInstances = agent.getChildInstances(ePath.getName());
			for (Instance child : actualInstances) {
				String instancePath = evalPath.replaceFirst(frameName, child.getName());
				applyRangeUpdates(agent, baseRuleName, rlRanges, context, evalPath, instancePath);
			}
		} else {
			applyRangeUpdates(agent, baseRuleName, rlRanges, context, evalPath, evalPath);
		}


	}

	private void applyRangeUpdates(RuntimeAgent agent, String baseRuleName, RLParameterRanges rlRanges, Context context, String evalPath,
	        String instancePath) {
		Attribute attribute = agent.resolvePath(Path.attributePath(instancePath.split("/")));

		SetAttribute current = (SetAttribute) attribute;
		List<String> modifiedSet = rlRanges.getNewCategoricalParameterValues(evalPath,
		        current.getFillersAndEntries());

		List<Fact> stateFacts = getStateFactsForRootRule(baseRuleName, context);
		for (String newFiller : modifiedSet) {
			update(agent, baseRuleName, evalPath, newFiller, stateFacts, context);
		}
	}

	private static void update(RuntimeAgent agent, String baseRuleName, String attPath, String newFiller, List<Fact> stateFacts,
	        Context context) {

		try {

			List<Fact> selectedStates = chooseStates(stateFacts, context);

			for (Fact state : selectedStates) {

				String stateName = state.getSlotValue("name").stringValue(context);
				String expansionRuleName = "experimental_rule_" + baseRuleName + "@" + stateName + "@";
				RLRule baseRule = agent.getBehaviour().getRLRule(baseRuleName);

				Fact[] elems = getStateFactsCategories(stateName, context);

				String newRule = addCategoryToExperimentalRule(agent, baseRule, stateName, elems, attPath, newFiller,
				        context);

				addStateFactCategoryElemFromStatefact(state, attPath, newFiller, context);

				Rete rete = context.getEngine();

				TreeWriter f = new TreeWriter();
				f.output("before_deepening", rete, debugDir);

				deleteRule(expansionRuleName, context);
				context.getEngine().executeCommand(newRule);

				f = new TreeWriter();
				f.output("after_deepening", rete, debugDir);
			}

		} catch (JessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Select the states to which the category is to be appended.
	 * 
	 * @param states the states to choose from
	 * @param context rete context
	 * @return fact hierarchy from the selected fact with all its predecessors
	 */
	private static List<Fact> chooseStates(List<Fact> states, Context context) {

		List<Fact> set = new ArrayList<Fact>();

		List<Fact> list = new ArrayList<Fact>();

		states.stream().filter(state -> getFloatSlotValue(state, "active", context) > 0).forEach(list::add);

		int pos = staticNextIntFromTo(0, list.size() - 1);
		Fact selectedFact = list.get(pos);
		set.add(selectedFact);
		addAllStateAncestors(set, context, selectedFact, states);

		return set;
	}

	/**
	 * Adds all ancestors of the given list of states to the given result list. This happens recursively by looking at the parent slot of
	 * the respective statefact.
	 * 
	 * @param result the list with the ancestors
	 * @param ctx rete context
	 * @param fact the current fact
	 * @param allStates all facts to look at
	 */
	public static void addAllStateAncestors(List<Fact> result, Context ctx, Fact fact, List<Fact> allStates) {
		try {
			String parentName = fact.getSlotValue("parent").stringValue(ctx);
			for (Fact state : allStates) {
				String name = state.getSlotValue("name").stringValue(ctx);
				if (name.equals(parentName)) {
					result.add(state);
					addAllStateAncestors(result, ctx, state, allStates);
				}
			}
		} catch (JessException e) {
			e.printStackTrace();
		}
	}

}
