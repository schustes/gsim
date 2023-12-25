package de.s2.gsim.sim.behaviour.rangeupdate;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.sim.behaviour.engine.BehaviourEngine.RLParameterRanges;
import jess.Context;

public interface DynamicValueRangeUpdateStrategy {

    /**
     * Extends the port range of state elements of the BRA implementation. The concrete strategy depends on the type of attribute (Set or Interval).
     * If there was no change in the port range, nothing is done.
     * 
     * @param agent the agent
     * @param baseRuleName the rule to update
     * @param exp the expansion for which the port range is checked
     * @param ranges the current port ranges in the rulebase
     * @param context rete context
     */
    void apply(RuntimeAgent agent, String baseRuleName, ExpansionDef exp, RLParameterRanges ranges, Context context);

    static DynamicValueRangeUpdateStrategy getStrategyForAttributeType(AttributeType type) {
        switch (type) {
        case INTERVAL:
            return new DynamicIntervalUpdateStrategyImpl();
        case SET:
            return new DynamicCategoryUpdateStrategyImpl();
        default:
            throw new IllegalArgumentException(String.format("There is no extension strategy for %s", type));

        }
    }

}
