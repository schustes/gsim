package de.s2.gsim.sim.behaviour.rulebuilder;

import static de.s2.gsim.sim.behaviour.rulebuilder.ConditionBuilder.createCategoricalAtomCondition;
import static de.s2.gsim.sim.behaviour.rulebuilder.ConditionBuilder.createCondition;
import static de.s2.gsim.sim.behaviour.rulebuilder.ConditionBuilder.createIntervalAtomCondition;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.UserRule;
import de.s2.gsim.sim.GSimEngineException;

public abstract class TreeExpansionBuilder {

	private TreeExpansionBuilder() {
		// static class
	}

	public static String buildExperimentationRule(RuntimeAgent agent, RLRule rootRule, String stateName, ExpansionParameterReferences exp,
	        boolean isUpperBoundInclusive)
	        throws GSimEngineException {

		Object2JessVariableBindingTable refTable = buildRefTable(agent, rootRule);

		String condStr = createConditionString(agent, rootRule, refTable, exp, isUpperBoundInclusive);

		return ExperimentationRuleBuilder.buildExperimentationRule(agent, rootRule, stateName, condStr);

    }

	public static String buildInitialRule(RuntimeAgent agent, RLRule rootRule, String stateName, ExpansionParameterReferences exp)
	        throws GSimEngineException {

		return buildExperimentationRule(agent, rootRule, stateName, exp, true);

    }

	private static Object2JessVariableBindingTable buildRefTable(RuntimeAgent agent, RLRule rootRule) {
		Object2JessVariableBindingTable refTable = new Object2JessVariableBindingTable(agent);
		refTable.build(rootRule);
		return refTable;
	}

	private static String createConditions(RuntimeAgent agent, UserRule rule, Object2JessVariableBindingTable objRefs, String ruleSoFar)
	        throws GSimEngineException {

        String n = "";

        for (ConditionDef condition : rule.getConditions()) {
            n += createCondition(agent, condition, objRefs, ruleSoFar);
        }
        return n;
    }

	private static String createConditionString(RuntimeAgent agent, RLRule rule, Object2JessVariableBindingTable objRefs,
	        ExpansionParameterReferences expansionAttValuePairs, boolean initial)

            throws GSimEngineException {

        String n = "";

		n = createConditions(agent, rule, objRefs, n);

        for (String att : expansionAttValuePairs.getSetAttributes()) {
			n += createCategoricalAtomCondition(agent, att, expansionAttValuePairs.getFillers(att), objRefs, n);

        }
        for (String att : expansionAttValuePairs.getIntervalAttributes()) {
            double[] interval = expansionAttValuePairs.getInterval(att);

			n += createIntervalAtomCondition(agent, att, interval[0], interval[1], objRefs, n, initial);
        }

        return n;

    }

}
