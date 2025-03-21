package de.s2.gsim.sim.behaviour.bra;

import static de.s2.gsim.sim.behaviour.bra.CatgoryExpansionHandler.createNextStatesCat;
import static de.s2.gsim.sim.behaviour.bra.IntervalExpansionHandler.createNextStatesNum;
import static java.util.Collections.shuffle;

import java.util.List;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.util.PathDefinitionResolutionUtils;
import de.s2.gsim.sim.behaviour.util.RuleEngineHelper;
import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

/**
 * Jess function that implements a state space expansion according to BRA (step: expand).
 * 
 * @author stephan
 *
 */
public class Expand implements Userfunction, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Value call(ValueVector valueVector, Context context) throws JessException {

        Value jessValue = context.getEngine().fetch("AGENT");
        RuntimeAgent agent = (RuntimeAgent) jessValue.externalAddressValue(context);

        Value val = valueVector.get(1);
        Fact stateFact = val.factValue(context);

        String statefactName = stateFact.getSlotValue("name").stringValue(context);
        List<Fact> unexpandedStateFactElems = RuleEngineHelper.getStateFactElems(statefactName, context);

        if (unexpandedStateFactElems.size() == 0) {
            return null;
        }

        Fact elemToExpand = selectRandomStatefact(unexpandedStateFactElems);
        unexpandedStateFactElems.remove(elemToExpand);

        String attributeRef = elemToExpand.getSlotValue("param-name").stringValue(context);
        try {
			if (isNumericalAttributeSpec(agent, attributeRef)) {
                createNextStatesNum(agent, stateFact, elemToExpand, unexpandedStateFactElems, context, false);
            } else {
                createNextStatesCat(agent, stateFact, elemToExpand, unexpandedStateFactElems, context, false);
            }
        } catch (Exception e) {
            throw new JessException("", "", e);
        }

        context.getEngine().retract(stateFact);
        double exCount = stateFact.getSlotValue("expansion-count").floatValue(context) + 1;
        stateFact.setSlotValue("expansion-count", new Value(exCount, RU.FLOAT));
        context.getEngine().assertFact(stateFact);
  
        return null;
    }

	private static boolean isNumericalAttributeSpec(Instance agent, String domainAttRef) throws GSimEngineException {

		DomainAttribute da = PathDefinitionResolutionUtils.extractAttribute(agent.getDefinition(), domainAttRef).get();

		if (da.getType() == AttributeType.NUMERICAL || da.getType() == AttributeType.INTERVAL) {
			return true;
		}

		return false;

	}

    // select a random element to expand
    private Fact selectRandomStatefact(List<Fact> allStateFactElems) {
        shuffle(allStateFactElems);
        Fact elemToExpand = allStateFactElems.get(0);
        return elemToExpand;
    }

    @Override
    public String getName() {
        return "expand";
    }


}
