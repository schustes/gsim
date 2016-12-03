package de.s2.gsim.sim.behaviour.rangeupdate;

import static cern.jet.random.Uniform.staticNextIntFromTo;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionFactHelper.addStateFactCategoryElemFromStatefact;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionRuleBuilder.addCategoryToExperimentalRule;
import static de.s2.gsim.sim.behaviour.util.FactHelper.getFloatSlotValue;
import static de.s2.gsim.sim.behaviour.util.ReteHelper.deleteRule;
import static de.s2.gsim.sim.behaviour.util.ReteHelper.getStateElems;
import static de.s2.gsim.sim.behaviour.util.ReteHelper.getStateFactsForRootRule;

import java.util.ArrayList;
import java.util.List;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.SetAttribute;
import de.s2.gsim.sim.behaviour.BehaviourEngine.RLParameterRanges;
import de.s2.gsim.sim.behaviour.builder.TreeExpansionBuilder;
import de.s2.gsim.sim.behaviour.util.CollectiveTreeDBWriter;
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

        DomainAttribute domainAttribute = agent.getDefinition().resolvePath(Path.attributePath(exp.getParameterName().split("/")));
        Attribute attribute = agent.resolvePath(Path.attributePath(exp.getParameterName().split("/")));

        SetAttribute current = (SetAttribute) attribute;
        List<String> modifiedSet = rlRanges.getNewCategoricalParameterValues(exp.getParameterName(), current.getFillersAndEntries());

        List<Fact> stateFacts = getStateFactsForRootRule(baseRuleName, context);
        for (String newFiller : modifiedSet) {
            update(agent, baseRuleName, domainAttribute.getName(), newFiller, stateFacts, context);
        }

    }

    private static void update(RuntimeAgent agent
            , String baseRuleName
            , String simpleAttributeName
            , String newFiller
            , List<Fact> stateFacts
            , Context context) {

        TreeExpansionBuilder treeBuilder = new TreeExpansionBuilder(agent);

        try {

            List<Fact> selectedStates = chooseStates(stateFacts, context);

            for (Fact state : selectedStates) {

                String stateName = state.getSlotValue("name").stringValue(context);
                String expansionRuleName = "experimental_rule_" + baseRuleName + "@" + stateName + "@";
                RLRule baseRule = agent.getBehaviour().getRLRule(baseRuleName);

                Fact[] elems = getStateElems(stateName, context);

                String newRule = addCategoryToExperimentalRule(treeBuilder, agent, baseRule, stateName, elems, simpleAttributeName, newFiller,
                        context);

                addStateFactCategoryElemFromStatefact(state, simpleAttributeName, newFiller, context);

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

    /**
     * Select the states to which the category is to be appended.
     * 
     * @param states the states to choose from
     * @param context rete context
     * @return fact hierarchy from the selected fact with all its predecessors
     */
    private static List<Fact> chooseStates(List<Fact> states, Context context) {

        List<Fact> set = new ArrayList<Fact>();

        List<Fact> map = new ArrayList<Fact>();

        set.stream().filter(state -> getFloatSlotValue(state, "active", context) > 0).forEach(map::add);

        Fact selectedFact = map.get(staticNextIntFromTo(0, map.size() - 1));
        addAllStateAncestors(set, context, selectedFact, states);

        return set;
    }

    /**
     * Adds all ancestors of the given list of states to the given result list. This happens recursively by looking at the parent slot of the
     * respective statefact.
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
