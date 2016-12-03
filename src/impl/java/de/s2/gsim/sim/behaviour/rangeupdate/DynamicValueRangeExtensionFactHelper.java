package de.s2.gsim.sim.behaviour.rangeupdate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.s2.gsim.sim.behaviour.GSimBehaviourException;
import de.s2.gsim.sim.behaviour.util.ReteHelper;
import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;

public abstract class DynamicValueRangeExtensionFactHelper {

    private DynamicValueRangeExtensionFactHelper() {
        // static class only
    }

    public static void insertNewActionNodes(Context context, String oldStatefactName, String newStatefactName) throws JessException {

        ArrayList<Fact> connectedActionNodesAll = ReteHelper.getFacts("rl-action-node", oldStatefactName, context);

        for (Fact f : connectedActionNodesAll) {
            context.getEngine().retract(f);
            f.setSlotValue("active", new Value(0.0, RU.FLOAT));
            context.getEngine().assertFact(f);
        }

        Fact[] newConnectedActions = createActionNodes(newStatefactName, connectedActionNodesAll, context);

        for (Fact a : newConnectedActions) {
            context.getEngine().assertFact(a);
        }
    }

    private static Fact[] createActionNodes(String stateFactName, ArrayList<Fact> oldActionNodes, Context context) throws JessException {

        Fact[] ret = new Fact[oldActionNodes.size()];

        int i = 0;

        for (Fact o : oldActionNodes) {

            String name = o.getSlotValue("action-name").stringValue(context);
            // String ruleName = o.getSlotValue("rule").stringValue(context);
            String evalFunc = o.getSlotValue("function").stringValue(context);
            double alpha = o.getSlotValue("alpha").floatValue(context);
            ValueVector args = o.getSlotValue("arg").listValue(context);

            Fact newAction = ReteHelper.createActionFact(context.getEngine(), name, evalFunc, alpha, args, stateFactName);
            newAction.setSlotValue("active", new Value(1.0, RU.FLOAT));
            ret[i] = newAction;
            i++;
        }
        return ret;

    }

    public static int getTime(Rete rete) {
        try {
            Iterator<?> iter = rete.listFacts();
            while (iter.hasNext()) {
                Fact f = (Fact) iter.next();
                if (f.getDeftemplate().getBaseName().equals("timer")) {
                    return (int) f.getSlotValue("time").floatValue(rete.getGlobalContext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static Fact addStateFactCategoryElemFromStatefact(Fact state, String attributeName, String categoryValue, Context context)
            throws JessException {

        String stateName = state.getSlotValue("name").stringValue(context);
        // String param = state.getSlotValue("expansion").stringValue(context);
        String name = attributeName + "->" + String.valueOf(categoryValue);

        return ReteHelper.addStateFactCat(context.getEngine(), state, stateName, name, attributeName, categoryValue);

    }

    public static Fact addStateFactIntervalElemFromParentElem(String stateName, Fact stateFactElem, double from, double to, Context context)
            throws JessException {

        // String stateName = stateFactElem.getSlotValue("name").stringValue(context);
        String param = stateFactElem.getSlotValue("param-name").stringValue(context);
        String name = param + "->" + String.valueOf(from) + ":" + String.valueOf(to);

        return ReteHelper.addStateFactElement(context.getEngine(), stateFactElem, stateName, name, param, from, to);

    }

    public static Fact addStateFactIntervalElemFromStatefact(String stateName, String paramName, Fact stateFactElem, double from, double to,
            Context context)
            throws JessException {

        String name = paramName + "->" + String.valueOf(from) + ":" + String.valueOf(to);

        return ReteHelper.addStateFactElement(context.getEngine(), stateFactElem, stateName, name, paramName, from, to);

    }

    public static ArrayList<Fact> activateActionNodes(Context context, String stateFactName) throws JessException {
        ArrayList<Fact> connectedActionNodes = ReteHelper.getFacts("rl-action-node", stateFactName, context);
        for (Fact f : connectedActionNodes) {

            context.getEngine().retract(f);
            f.setSlotValue("active", new Value(0.0, RU.FLOAT));
            context.getEngine().assertFact(f);
        }
        return connectedActionNodes;
    }

    public static void appendRemainingStateFactElems(List<Fact> remaining, String stateFactName, Context context) {
        applyIfConditionMatches(remaining, DynamicValueRangeExtensionFactHelper::isNumericalStateFactElem,
                (Fact f) -> appendRemainingStateFactElemsInterval(f, stateFactName, context));
        applyIfConditionMatches(remaining, DynamicValueRangeExtensionFactHelper::isCategoricalStateFactElem,
                (Fact f) -> appendRemainingStateFactElemsCat(f, stateFactName, context));
    }

    private static void applyIfConditionMatches(List<Fact> remaining, Predicate<Fact> p, Consumer<Fact> c) {
        remaining.stream().filter(p).forEach(c);
    }

    public static boolean isNumericalStateFactElem(Fact f) {
        return (f.getDeftemplate().getBaseName().equals("state-fact-element"));
    }

    public static boolean isCategoricalStateFactElem(Fact f) {
        return f.getDeftemplate().getBaseName().equals("state-fact-category");
    }

    public static void appendRemainingStateFactElemsCat(Fact f, String stateFactName, Context context) {
        try {
            String attributeName = f.getSlotValue("param-name").stringValue(context);
            String categoryValue = f.getSlotValue("category").stringValue(context);
            String name = attributeName + "->" + String.valueOf(categoryValue);
            ReteHelper.addStateFactCat(context.getEngine(), stateFactName, name, attributeName, categoryValue);
        } catch (JessException e) {
            throw new GSimBehaviourException(e);
        }

    }

    public static void appendRemainingStateFactElemsInterval(Fact f, String stateFactName, Context context) {
        try {
            String attributeName = f.getSlotValue("param-name").stringValue(context);
            String from = f.getSlotValue("from").stringValue(context);
            String to = f.getSlotValue("to").stringValue(context);
            String name = attributeName + "->" + String.valueOf(from) + ":" + String.valueOf(to);
            ReteHelper.addStateFactElement(context.getEngine(), stateFactName, name, attributeName, Double.parseDouble(from), Double.parseDouble(to));
        } catch (JessException e) {
            throw new GSimBehaviourException(e);
        }
    }

    public static boolean equals(ValueVector v1, ValueVector v2) {
        try {
            for (int i = 0; i < v1.size(); i++) {
                boolean eq = false;
                for (int j = 0; j < v2.size(); j++) {
                    if (v1.get(i).equals(v2.get(j))) {
                        eq = true;
                    }
                }
                if (!eq) {
                    return false;
                }
            }
            return true;
        } catch (JessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean existsEquivalent(Fact parent, Fact[] sfes, Context ctx) {

        boolean existsCombination = true;

        try {
            String parentSfn = parent.getSlotValue("name").stringValue(ctx);
            ValueVector v = parent.getSlotValue("expansion").listValue(ctx);
            ArrayList<Fact> facts = ReteHelper.getStateFactElems(parentSfn, ctx);
            for (Fact f : sfes) {
                facts.add(f);
            }
            ArrayList<Fact> all = ReteHelper.getAllStateFacts(ctx);

            for (Fact sf : all) {
                ValueVector v1 = sf.getSlotValue("expansion").listValue(ctx);

                if (equals(v1, v)) {
                    ArrayList<Fact> f0 = ReteHelper.getStateFactElems(sf.getSlotValue("name").stringValue(ctx), ctx);

                    for (int i = 0; i < v1.size(); i++) {

                        String param = v1.get(i).stringValue(ctx);
                        boolean exists = true;

                        for (Fact oldFact : f0) {
                            String param2 = oldFact.getSlotValue("param-name").stringValue(ctx);

                            boolean e = false;

                            if (param.equals(param2) && oldFact.getDeftemplate().getBaseName().equals("state-fact-category")) {
                                String cat = oldFact.getSlotValue("category").stringValue(ctx);
                                for (Fact sf0 : sfes) {
                                    if (sf0.getDeftemplate().getBaseName().equals("state-fact-category")) {
                                        String cat2 = sf0.getSlotValue("category").stringValue(ctx);
                                        if (cat2 != null && cat2.equals(cat)) {
                                            e = true;
                                        }
                                    }
                                }
                                if (!e) {
                                    exists = false;
                                }
                            } else if (param.equals(param2)) {
                                if (param.equals(param2) && oldFact.getSlotValue("from") != null) {
                                    double from = oldFact.getSlotValue("from").floatValue(ctx);
                                    double to = oldFact.getSlotValue("to").floatValue(ctx);

                                    for (Fact sf0 : sfes) {
                                        if (sf0.getDeftemplate().getBaseName().equals("state-fact-elemen")) {
                                            double from1 = sf0.getSlotValue("from").floatValue(ctx);
                                            double to1 = sf0.getSlotValue("to").floatValue(ctx);

                                            if (from == from1 && to == to1) {
                                                e = true;
                                            }
                                        }
                                    }
                                    if (!e) {
                                        exists = false;
                                    }
                                }
                            }
                        }
                        if (!exists) {
                            existsCombination = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return existsCombination;
    }

    /**
     * Creates the new state-fact.
     * 
     * @param oldDesc
     * @param paramName
     * @param context
     * @param depth
     * @param where
     * @return
     * @throws JessException
     */
    public static Fact expandStateDescription(Fact oldDesc, String rootRuleName, String paramName, Context context, int depth, boolean copy)
            throws JessException {

        // String ruleName = oldDesc.getSlotValue("rule").stringValue(context);
        int t = getTime(context.getEngine());
        Fact f1 = null;
        if (!copy) {
            f1 = ReteHelper.addStateFact(context.getEngine(), oldDesc, rootRuleName, paramName, depth, t);
        } else {
            f1 = ReteHelper.copyStateFact(context.getEngine(), oldDesc, paramName, depth, t);
        }
        return f1;

    }

}
