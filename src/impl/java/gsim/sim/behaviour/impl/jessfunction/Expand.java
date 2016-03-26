package gsim.sim.behaviour.impl.jessfunction;

import java.util.ArrayList;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import gsim.sim.behaviour.impl.FactHandler;
import gsim.sim.behaviour.impl.ParsingUtils;
import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

public class Expand extends DynamicRuleBuilder implements Userfunction, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Value call(ValueVector valueVector, Context context) throws JessException {

        Value v = context.getEngine().fetch("AGENT");
        RuntimeAgent agent = (RuntimeAgent) v.externalAddressValue(context);

        Value val = valueVector.get(1);
        Fact stateFact = val.factValue(context);

        String sfn = stateFact.getSlotValue("name").stringValue(context);
        ArrayList<Fact> allStateFactElems = FactHandler.getInstance().getStateFactElems(sfn, context); // this.getFacts("state-fact-element",
        // sfn, context);

        if (allStateFactElems.size() == 0) {
            return null;// no fillers for attribute
                        // existing!
        }

        java.util.Collections.shuffle(allStateFactElems);// select a random element to expand

        // TODO - add a function that splits allStateFactElems according to the attributes
        // they represent. Each list must be expanded separately (adding the remaining statefactElems
        // as constants.

        // FOR NOW, ONLY ONE ELEMENT GETS EXPANDED!!!
        // while (allStateFactElems.size() > 0) {
        Fact elemToExpand = allStateFactElems.remove(0);

        String pName = elemToExpand.getSlotValue("param-name").stringValue(context);
        Expand0 impl = new Expand0();
        try {
            if (ParsingUtils.isNumericalAttributeSpec(agent, pName)) {
                impl.createNextStatesNum(agent, stateFact, elemToExpand, allStateFactElems, context, false);
            } else {
                impl.createNextStatesCat(agent, stateFact, elemToExpand, allStateFactElems, context, false);
            }
        } catch (Exception e) {
            throw new JessException("", "", e);
        }
        // }

        context.getEngine().retract(stateFact);
        double exCount = stateFact.getSlotValue("expansion-count").floatValue(context) + 1;
        stateFact.setSlotValue("expansion-count", new Value(exCount, RU.FLOAT));
        context.getEngine().assertFact(stateFact);

        return null;
    }

    @Override
    public String getName() {
        return "expand";
    }

    private class StateFactElemCategorySpec {
        public String attributeSpec = ""; // parameter name. e.g. 'profit'

        public ArrayList<Fact> facts = new ArrayList<Fact>();// facts containing
                                                             // the parameter

        public ArrayList<String> fillers = new ArrayList<String>();// possible
                                                                   // fillers
                                                                   // present

    }

}
