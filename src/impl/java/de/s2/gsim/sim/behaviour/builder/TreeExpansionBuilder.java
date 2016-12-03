package de.s2.gsim.sim.behaviour.builder;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.UserRule;
import de.s2.gsim.sim.GSimEngineException;

public class TreeExpansionBuilder {

    private RuntimeAgent agent;

    private ConditionBuilder conditionBuilder;

    private GeneralRLBuilder general;

    private Object2VariableBindingTable refTable = new Object2VariableBindingTable();

    public TreeExpansionBuilder(RuntimeAgent a) {
        agent = a;
        conditionBuilder = new ConditionBuilder();
        general = new GeneralRLBuilder(a, conditionBuilder);
    }

    public String buildExperimentationRule(RLRule rootRule, String stateName, Attribute2ValuesMap exp) throws GSimEngineException {

        refTable.build(rootRule);

        String condStr = createConditionString(rootRule, refTable, exp);

        return general.buildExperimentationRule(rootRule, stateName, condStr);

    }

    public String buildInitialRule(RLRule rootRule, String stateName, Attribute2ValuesMap exp) throws GSimEngineException {
        return buildExperimentationRule(rootRule, stateName, exp);
    }

    private String createConditions(UserRule rule, Object2VariableBindingTable objRefs, String ruleSoFar) throws GSimEngineException {

        String n = "";

        for (ConditionDef condition : rule.getConditions()) {
            n += conditionBuilder.createCondition(agent, condition, objRefs, ruleSoFar);
        }
        return n;
    }

    private String createConditionString(RLRule rule, Object2VariableBindingTable objRefs, Attribute2ValuesMap expansionAttValuePairs)

            throws GSimEngineException {

        String n = "";

        n = createConditions(rule, objRefs, n);

        for (String att : expansionAttValuePairs.getSetAttributes()) {
            n += conditionBuilder.createCategoricalAtomCondition(att, expansionAttValuePairs.getFillers(att), objRefs, n);

        }
        for (String att : expansionAttValuePairs.getIntervalAttributes()) {
            double[] interval = expansionAttValuePairs.getInterval(att);

            n += conditionBuilder.createNumericalAtomCondition(att, interval[0], interval[1], objRefs, n);
        }

        return n;

    }

}
