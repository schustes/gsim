package de.s2.gsim.sim.behaviour.rangeupdate;

import static de.s2.gsim.sim.behaviour.bra.StateFactHelper.addStateFactIntervalElemFromStatefact;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionRuleBuilder.increaseIntervalRangeInExperimentalRule;
import static de.s2.gsim.sim.behaviour.util.FactUtils.getFloatSlotValue;
import static de.s2.gsim.sim.behaviour.util.FactUtils.getStringSlotValue;
import static de.s2.gsim.sim.behaviour.util.RuleEngineHelper.deleteFactByName;
import static de.s2.gsim.sim.behaviour.util.RuleEngineHelper.deleteRule;
import static de.s2.gsim.sim.behaviour.util.RuleEngineHelper.getStateFactsElems;
import static de.s2.gsim.sim.behaviour.util.RuleEngineHelper.getStateFactsForRootRule;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.sim.behaviour.GSimBehaviourException;
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
public class DynamicIntervalUpdateStrategyImpl implements DynamicValueRangeUpdateStrategy {

	private static String debugDir = "/home/gsim/tmp/trees";

	@Override
	public void apply(RuntimeAgent agent, String baseRuleName, ExpansionDef expansion, RLParameterRanges rlRanges, Context context) {

		String evalPath = expansion.getParameterName();
		Path<Attribute> ePath = Path.attributePath(evalPath.split("/"));

		if (PathDefinitionResolutionUtils.referencesChildFrame(agent.getDefinition(), evalPath)) {
			Frame referencedObject = PathDefinitionResolutionUtils.extractChildType(agent.getDefinition(), evalPath);
			String frameName = referencedObject.getName();
			List<Instance> actualInstances = agent.getChildInstances(ePath.getName());
			for (Instance child : actualInstances) {
				String instancePath = evalPath.replaceFirst(frameName, child.getName());
				Attribute attribute = agent.resolvePath(Path.attributePath(instancePath.split("/")));
				DomainAttribute dattr = PathDefinitionResolutionUtils.extractAttribute(agent.getDefinition(), evalPath).get();
				applyUpdates(agent, baseRuleName, expansion, ePath, dattr, attribute, rlRanges, context);
			}

		} else {
			Attribute attribute = agent.resolvePath(ePath);
			DomainAttribute dattr = agent.getDefinition().resolvePath(Path.attributePath(evalPath));
			applyUpdates(agent, baseRuleName, expansion, ePath, dattr, attribute, rlRanges, context);
		}

	}

	private void applyUpdates(RuntimeAgent agent, String baseRuleName, ExpansionDef expansion, Path<Attribute> instancePath,
	        DomainAttribute def, Attribute a, RLParameterRanges rlRanges, Context context) {

		double[] modifiedRange = determineInterval(agent, instancePath, expansion.getParameterName(), a, def, rlRanges);

		if (modifiedRange == null) {
			return;
		}

		try {
			update(agent, baseRuleName, expansion.getParameterName(), modifiedRange, context);
		} catch (JessException ex) {
			throw new GSimBehaviourException(ex);
		}
	}

	private void update(RuntimeAgent agent, String baseRuleName, String attributeRef, double[] modifiedRange, Context context) throws JessException {

		List<Fact> allStates = getStateFactsForRootRule(baseRuleName, context);
		List<Fact> selectedStateFactElems = findAllAffectedStateFactElems(allStates, modifiedRange[0], modifiedRange[1], context);

		double min = findMinElemValue(selectedStateFactElems, context);
		double max = findMaxElemValue(selectedStateFactElems, context);
		boolean isNewLowerBound = min > modifiedRange[0];

		for (Fact stateFactElem : selectedStateFactElems) {

			String stateName = getStringSlotValue(stateFactElem, "state-fact-name", context);

			String expansionRuleName = "experimental_rule_" + baseRuleName + "@" + stateName + "@";
			RLRule baseRule = agent.getBehaviour().getRLRule(baseRuleName);


			double oldMinOfFact = getFloatSlotValue(stateFactElem, "from", context);
			double oldMaxOfFact = getFloatSlotValue(stateFactElem, "to", context);

			String newRule = null;
			if (isNewLowerBound && oldMinOfFact == min) {
				deleteFactByName(context.getEngine(), stateFactElem);

				addStateFactIntervalElemFromStatefact(stateName, attributeRef, stateFactElem, modifiedRange[0], oldMaxOfFact, context);
				newRule = increaseIntervalRangeInExperimentalRule(agent, baseRule, stateName, stateFactElem, modifiedRange[0],
				        oldMaxOfFact, context);

			} else if (oldMaxOfFact == max) {
				deleteFactByName(context.getEngine(), stateFactElem);

				addStateFactIntervalElemFromStatefact(stateName, attributeRef, stateFactElem, oldMinOfFact, modifiedRange[1], context);
				newRule = increaseIntervalRangeInExperimentalRule(agent, baseRule, stateName, stateFactElem, oldMinOfFact,
				        modifiedRange[1], context);

			}

			if (newRule != null) {
				// System.out.println("new rule->" + newRule);
				deleteRule(expansionRuleName, context);
				context.getEngine().executeCommand(newRule);
			}

			debug(context);
		}
	}

	private void debug(Context context) {
		Rete rete = context.getEngine();

		TreeWriter f = new TreeWriter();
		f.output("before_deepening", rete, debugDir);


		f = new TreeWriter();
		f.output("after_deepening", rete, debugDir);
	}

	/**
	 * Determines the interval to update the engine with.
	 * 
	 * @param agent
	 * @param instancePath
	 * @param evalPath
	 * @param attr
	 * @param domainAttribute
	 * @param rlRanges
	 * @return
	 */
	private double[] determineInterval(RuntimeAgent agent, Path<Attribute> instancePath, String evalPath, Attribute attr,
	        DomainAttribute domainAttribute, RLParameterRanges rlRanges) {

		if (!(attr instanceof NumericalAttribute)) {
			throw new IllegalArgumentException("This strategy can only be applied to numerical attributes!");
		}

		double currentMin = Double.parseDouble(domainAttribute.getFillers().get(0));
		double currentMax = Double.parseDouble(domainAttribute.getFillers().get(1));
		double currentVal = ((NumericalAttribute) attr).getValue();
		double[] modifiedRange;
		if (currentVal > currentMax) {
			modifiedRange = rlRanges.getNewIntervalParameterRange(evalPath, new double[] { currentMin, currentVal });
		} else if (currentVal < currentMin) {
			modifiedRange = rlRanges.getNewIntervalParameterRange(evalPath, new double[] { currentVal, currentMax });
		} else {
			modifiedRange = rlRanges.getNewIntervalParameterRange(evalPath, new double[] { currentMin, currentMax });
		}
		return modifiedRange;
	}

	/**
	 * Finds all states that have either an upper limit greater than the given max value or a lower limit less than the given min value.
	 * 
	 * @param states the states to look at
	 * @param min the minimum
	 * @param max the maximum
	 * @param context Rete context
	 * @return a list of statefacts or empty list if not applicable
	 */
	private static List<Fact> findAllAffectedStateFactElems(List<Fact> states, double min, double max, Context context) {

		return states.stream()
		        // .filter(state -> getFloatSlotValue(state, "active", context) > 0)
				.flatMap(state -> {
					Fact[] elems = getStateFactElemsForState(context, state);
					return Arrays.stream(elems);
				})
		        .filter(f -> getFloatSlotValue(f, "from", context) > min || getFloatSlotValue(f, "to", context) < max)
		        .collect(Collectors.toList());
	}

	private static double findMaxElemValue(List<Fact> states, Context context) {
		return states.stream()
				.mapToDouble(fact -> getFloatSlotValue(fact, "to", context))
		        .max().getAsDouble();
	}

	private static double findMinElemValue(List<Fact> states, Context context) {
		return states.stream()
				.mapToDouble(fact -> getFloatSlotValue(fact, "from", context))
		        .min().getAsDouble();
	}

	private static Fact[] getStateFactElemsForState(Context context, Fact state) {
		String stateName = getStringSlotValue(state, "name", context);
		Fact[] elems = getStateFactsElems(stateName, context);
		return elems;
	}




}
