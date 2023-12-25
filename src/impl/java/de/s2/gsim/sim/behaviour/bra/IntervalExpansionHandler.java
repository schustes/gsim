package de.s2.gsim.sim.behaviour.bra;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.sim.behaviour.GSimBehaviourException;
import jess.Context;
import jess.Fact;
import org.apache.log4j.Logger;

import java.util.List;

import static de.s2.gsim.sim.behaviour.bra.StateFactHelper.addStateFactIntervalElemFromParentElem;
import static de.s2.gsim.sim.behaviour.bra.StateFactHelper.appendRemainingStateFactElems;
import static de.s2.gsim.sim.behaviour.bra.StateFactHelper.existsEquivalent;
import static de.s2.gsim.sim.behaviour.bra.StateFactHelper.expandStateDescription;
import static de.s2.gsim.sim.behaviour.bra.StateFactHelper.insertNewActionNodes;
import static de.s2.gsim.sim.behaviour.rangeupdate.DynamicValueRangeExtensionRuleBuilder.createNewExperimentalRule;

public class IntervalExpansionHandler implements java.io.Serializable {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(IntervalExpansionHandler.class);

	private static final long serialVersionUID = 1L;

    public static void createNextStatesNum(RuntimeAgent agent, Fact stateFact, Fact toExpand, List<Fact> allElems, Context context,
			boolean copy) throws GSimBehaviourException {

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

			Fact stateFactElem_split1 = addStateFactIntervalElemFromParentElem(newStateName1, toExpand, from, from + dist / 2d, context);
			Fact stateFactElem_split2 = addStateFactIntervalElemFromParentElem(newStateName2, toExpand, from + dist / 2d, to, context);

			if (existsEquivalent(stateFact, new Fact[] { stateFactElem_split1 }, context)) {
				return;
			}

			context.getEngine().assertFact(stateFactElem_split1);
			context.getEngine().assertFact(stateFactElem_split2);

			// append state-elems of additional attributeDistribution (if more than 1 attribute present)
			appendRemainingStateFactElems(allElems, newStateName1, context);
			appendRemainingStateFactElems(allElems, newStateName2, context);

			RLRule rootRule = agent.getBehaviour().getRLRule(newStateName1.split("_")[0]);

			context.getEngine().executeCommand(createNewExperimentalRule(rootRule, agent, newStateName1, remaining, context,
			        paramName, from, from + dist / 2d, false));
			context.getEngine().executeCommand(
			        createNewExperimentalRule(rootRule, agent, newStateName2, remaining, context, paramName, from + dist / 2d, to, true));
			context.getEngine().assertFact(stateFact_split1);
			context.getEngine().assertFact(stateFact_split2);

			insertNewActionNodes(context, stateFactName, newStateName1);
			insertNewActionNodes(context, stateFactName, newStateName2);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
