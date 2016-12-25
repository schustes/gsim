package de.s2.gsim.sim.behaviour.builder;

import static de.s2.gsim.sim.behaviour.builder.ConditionBuilder.createCategoricalAtomCondition;
import static de.s2.gsim.sim.behaviour.builder.ConditionBuilder.createCondition;
import static de.s2.gsim.sim.behaviour.builder.ConditionBuilder.createIntervalAtomCondition;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.UserRule;
import de.s2.gsim.sim.GSimEngineException;

public class TreeExpansionBuilder {

    private RuntimeAgent agent;

    private GeneralRLBuilder general;

	private Object2JessVariableBindingTable refTable;

    public TreeExpansionBuilder(RuntimeAgent a) {
        agent = a;
        general = new GeneralRLBuilder(a);
		refTable = new Object2JessVariableBindingTable(agent);
    }

    public String buildExperimentationRule(RLRule rootRule, String stateName, ExpansionParameterReferences exp) throws GSimEngineException {

        refTable.build(rootRule);

        String condStr = createConditionString(rootRule, refTable, exp);

        return general.buildExperimentationRule(rootRule, stateName, condStr);

    }

    public String buildInitialRule(RLRule rootRule, String stateName, ExpansionParameterReferences exp) throws GSimEngineException {
        return buildExperimentationRule(rootRule, stateName, exp);
    }

    private String createConditions(UserRule rule, Object2JessVariableBindingTable objRefs, String ruleSoFar) throws GSimEngineException {

        String n = "";

        for (ConditionDef condition : rule.getConditions()) {
            n += createCondition(agent, condition, objRefs, ruleSoFar);
        }
        return n;
    }

    private String createConditionString(RLRule rule, Object2JessVariableBindingTable objRefs, ExpansionParameterReferences expansionAttValuePairs)

            throws GSimEngineException {

        String n = "";

        n = createConditions(rule, objRefs, n);

        for (String att : expansionAttValuePairs.getSetAttributes()) {
            n += createCategoricalAtomCondition(this.agent, att, expansionAttValuePairs.getFillers(att), objRefs, n);

        }
        for (String att : expansionAttValuePairs.getIntervalAttributes()) {
            double[] interval = expansionAttValuePairs.getInterval(att);

            n += createIntervalAtomCondition(this.agent, att, interval[0], interval[1], objRefs, n);
        }

        return n;

    }

}
