package de.s2.gsim.sim.behaviour.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.s2.gsim.sim.behaviour.GSimBehaviourException;
import jess.Context;
import jess.Fact;
import jess.JessException;

public class FactHelper {
    public static double getFloatSlotValue(Fact f, String slotName, Context context) {
        try {
            return f.getSlotValue("active").floatValue(context);
        } catch (JessException e) {
            throw new GSimBehaviourException("Jess exception", e);
        }

    }

    public static String getStringSlotValue(Fact f, String slotName, Context context) {
        try {
            return f.getSlotValue("active").stringValue(context);
        } catch (JessException e) {
            throw new GSimBehaviourException("Jess exception", e);
        }

    }

    public static StateFactElemCategorySpec extractCategoryElemSpec(List<Fact> allElemsList, Context context, String toExpandParamName)
            throws JessException {

        Iterator<?> iter = allElemsList.iterator();
        StateFactElemCategorySpec spec = new StateFactElemCategorySpec();

        while (iter.hasNext()) {
            Fact f = (Fact) iter.next();
            String pn1 = f.getSlotValue("param-name").stringValue(context);
            if (toExpandParamName.equals(pn1)) {
                spec.facts.add(f);
                spec.attributeSpec = pn1;
                spec.fillers.add(f.getSlotValue("category").stringValue(context));
                iter.remove();
            }
        }

        return spec;
    }

    public static class StateFactElemCategorySpec {

        public String attributeSpec = ""; // parameter name. e.g. 'profit'

        public ArrayList<Fact> facts = new ArrayList<Fact>();// facts containing

        public ArrayList<String> fillers = new ArrayList<String>();// possible

    }
}
