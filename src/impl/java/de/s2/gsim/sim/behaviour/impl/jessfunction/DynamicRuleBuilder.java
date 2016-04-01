package de.s2.gsim.sim.behaviour.impl.jessfunction;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.behaviour.impl.Attribute2ValuesMap;
import de.s2.gsim.sim.behaviour.impl.FactHandler;
import de.s2.gsim.sim.behaviour.impl.TreeExpansionBuilder;
import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;

public class DynamicRuleBuilder {

    private static Logger logger = Logger.getLogger(DynamicRuleBuilder.class);

    public DynamicRuleBuilder() {
        super();
    }

    public String addCategoryToExperimentalRule(TreeExpansionBuilder b, RuntimeAgent a, RLRule r, String stateName, Fact[] statefactelems,
            String domainAttr, String newFiller, Context context) throws JessException {

        Attribute2ValuesMap consts = new Attribute2ValuesMap();

        for (int i = 0; i < statefactelems.length; i++) {
            String s = statefactelems[i].getDeftemplate().getBaseName();
            String pm = statefactelems[i].getSlotValue("param-name").stringValue(context);

            DomainAttribute attr = (DomainAttribute) a.getDefinition().resolveName(pm.split("/"));
            String simpleAttrName = attr.getName();

            if (s.equals("state-fact-element")) {
                double m = statefactelems[i].getSlotValue("from").floatValue(context);
                double x = statefactelems[i].getSlotValue("to").floatValue(context);
                consts.setIntervalAttributes(pm, m, x);

            } else {
                String c = statefactelems[i].getSlotValue("category").stringValue(context);

                String[] f = consts.getFillers(pm);// getFillers(consts, c);
                f = maybeAddFiller(f, c);
                if (simpleAttrName.equals(domainAttr)) {
                    f = maybeAddFiller(f, newFiller);
                }
                consts.setSetAttributes(pm, f);
            }
        }

        String n = "";
        try {
            n = b.buildExperimentationRule(r, stateName, consts);
            Logger.getLogger(DynamicRuleBuilder.class).debug("New rule after dynamic attribute add:\n" + n);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return n;

    }

    public Fact addStateFactCategoryElem(Fact state, String attributeName, String categoryValue, Context context) throws JessException {

        String stateName = state.getSlotValue("name").stringValue(context);
        // String param = state.getSlotValue("expansion").stringValue(context);
        String name = attributeName + "->" + String.valueOf(categoryValue);

        return FactHandler.getInstance().addStateFactCat(context.getEngine(), state, stateName, name, attributeName, categoryValue);

    }

    public Fact addStateFactIntervalElem(Fact state, double from, double to, Context context) throws JessException {

        String stateName = state.getSlotValue("name").stringValue(context);
        String param = state.getSlotValue("name").stringValue(context);
        String name = param + "->" + String.valueOf(from) + ":" + String.valueOf(to);

        return FactHandler.getInstance().addStateFactElement(context.getEngine(), state, stateName, name, param, from, to);

    }

    protected ArrayList<Fact> activateActionNodes(Context context, String stateFactName) throws JessException {
        ArrayList<Fact> connectedActionNodes = FactHandler.getInstance().getFacts("rl-action-node", stateFactName, context);
        for (Fact f : connectedActionNodes) {

            context.getEngine().retract(f);
            f.setSlotValue("active", new Value(0.0, RU.FLOAT));
            context.getEngine().assertFact(f);
        }
        return connectedActionNodes;
    }

    protected String createNewExperimentalRule(TreeExpansionBuilder b, RLRule r, RuntimeAgent a, String stateName, Fact[] statefactelems,
            Context context) throws JessException {

        Attribute2ValuesMap consts = new Attribute2ValuesMap();

        for (int i = 0; i < statefactelems.length; i++) {
            String s = statefactelems[i].getDeftemplate().getBaseName();
            String pm = statefactelems[i].getSlotValue("name").stringValue(context);
            if (s.equals("state-fact-element")) {
                double m = statefactelems[i].getSlotValue("from").floatValue(context);
                double x = statefactelems[i].getSlotValue("to").floatValue(context);
                consts.setIntervalAttributes(pm, m, x);

            } else {
                String c = statefactelems[i].getSlotValue("category").stringValue(context);

                String[] fillersNow = consts.getFillers(pm);// getFillers(consts, c);
                fillersNow = maybeAddFiller(fillersNow, c);// add filler to array

                consts.setSetAttributes(pm, fillersNow);
            }
        }

        String n = "";
        try {
            n = b.buildExperimentationRule(r, stateName, consts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return n;

    }

    protected String createNewExperimentalRuleCat(TreeExpansionBuilder b, RLRule r, String stateName, String paramToExpand, String[] fillersOfExpand,
            Fact[] constants, Context context) throws JessException {

        Attribute2ValuesMap consts = new Attribute2ValuesMap();

        consts.setSetAttributes(paramToExpand, fillersOfExpand);

        for (int i = 0; i < constants.length; i++) {
            String s = constants[i].getDeftemplate().getBaseName();
            String pm = constants[i].getSlotValue("param-name").stringValue(context);
            if (s.equals("state-fact-element")) {
                double m = constants[i].getSlotValue("from").floatValue(context);
                double x = constants[i].getSlotValue("to").floatValue(context);
                consts.setIntervalAttributes(pm, m, x);
            } else {
                String c = constants[i].getSlotValue("category").stringValue(context);

                String[] f = consts.getFillers(pm);// getFillers(consts, c);
                f = maybeAddFiller(f, c);

                consts.setSetAttributes(pm, f);
            }
        }

        String n = "";
        try {
            n = b.buildExperimentationRule(r, stateName, consts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.debug("new rule created: " + n);

        return n;

    }

    protected String createNewSelectionNodes(TreeExpansionBuilder b, RLRule r, RuntimeAgent a, String stateName, Fact elem, Fact[] constants,
            Context context) throws JessException {

        String sfn = elem.getSlotValue("state-fact-name").stringValue(context);

        String n = "";

        Attribute2ValuesMap consts = new Attribute2ValuesMap();

        for (int i = 0; i < constants.length; i++) {
            String s = constants[i].getDeftemplate().getBaseName();
            String pm = constants[i].getSlotValue("name").stringValue(context);
            if (s.equals("state-fact-element")) {
                double m = constants[i].getSlotValue("from").floatValue(context);
                double x = constants[i].getSlotValue("to").floatValue(context);
                consts.setIntervalAttributes(pm, m, x);
            } else {
                String c = constants[i].getSlotValue("category").stringValue(context);
                String[] f = consts.getFillers(pm);// getFillers(consts, c);
                f = maybeAddFiller(f, c);

                consts.setSetAttributes(pm, f);
            }
        }

        try {
            n = b.createShortCuts(r, sfn, consts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // //logger.debug(n);

        return n;

    }

    protected String createNewSelectionNodesCat(TreeExpansionBuilder b, RLRule r, String stateName, String param, String[] fillers, Fact[] constants,
            int depth, Context context) throws JessException {

        String sfn = stateName;

        String n = "";

        Attribute2ValuesMap consts = new Attribute2ValuesMap();

        for (int i = 0; i < constants.length; i++) {
            String s = constants[i].getDeftemplate().getBaseName();
            String pm = constants[i].getSlotValue("name").stringValue(context);
            if (s.equals("state-fact-element")) {
                double m = constants[i].getSlotValue("from").floatValue(context);
                double x = constants[i].getSlotValue("to").floatValue(context);
                consts.setIntervalAttributes(pm, m, x);
            } else {
                String c = constants[i].getSlotValue("category").stringValue(context);
                String[] fill = consts.getFillers(c);
                if (fill == null) {
                    fill = new String[] { c };
                } else {
                    String[] y = new String[fill.length + 1];
                    System.arraycopy(fill, 0, y, 0, fill.length);
                    y[fill.length] = c;
                    fill = y;
                }
                consts.setSetAttributes(c, fill);
            }
        }

        try {
            n = b.createShortCuts(r, sfn, consts);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // //logger.debug(n);

        return n;

    }

    protected int getTime(Rete rete) {
        try {
            Iterator iter = rete.listFacts();
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

    protected void insertNewActionNodes(Context context, String oldStatefactName, String newStatefactName) throws JessException {

        ArrayList<Fact> connectedActionNodesAll = FactHandler.getInstance().getFacts("rl-action-node", oldStatefactName, context);

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

    private Fact[] createActionNodes(String stateFactName, ArrayList<Fact> oldActionNodes, Context context) throws JessException {

        Fact[] ret = new Fact[oldActionNodes.size()];

        int i = 0;

        for (Fact o : oldActionNodes) {

            String name = o.getSlotValue("action-name").stringValue(context);
            // String ruleName = o.getSlotValue("rule").stringValue(context);
            String evalFunc = o.getSlotValue("function").stringValue(context);
            double alpha = o.getSlotValue("alpha").floatValue(context);
            ValueVector args = o.getSlotValue("arg").listValue(context);

            Fact newAction = FactHandler.getInstance().createActionFact(context.getEngine(), name, evalFunc, alpha, args, stateFactName);
            newAction.setSlotValue("active", new Value(1.0, RU.FLOAT));
            ret[i] = newAction;
            i++;
        }
        return ret;

    }

    private String[] maybeAddFiller(String[] oldFillers, String newFiller) {
        for (String s : oldFillers) {
            if (s.equals(newFiller)) {
                return oldFillers;
            }
        }

        String[] newFillers = new String[oldFillers.length + 1];
        System.arraycopy(oldFillers, 0, newFillers, 0, oldFillers.length);
        newFillers[newFillers.length - 1] = newFiller;
        return newFillers;

    }

}