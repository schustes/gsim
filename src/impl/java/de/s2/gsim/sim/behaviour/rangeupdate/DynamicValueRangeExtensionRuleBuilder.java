package de.s2.gsim.sim.behaviour.rangeupdate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.behaviour.builder.Attribute2ValuesMap;
import de.s2.gsim.sim.behaviour.builder.TreeExpansionBuilder;
import jess.Context;
import jess.Fact;
import jess.JessException;

public abstract class DynamicValueRangeExtensionRuleBuilder {

    private static Logger logger = Logger.getLogger(DynamicValueRangeExtensionRuleBuilder.class);


    private DynamicValueRangeExtensionRuleBuilder() {
        // static class
    }

    public static String increaseIntervalRangeInExperimentalRule(TreeExpansionBuilder b
            , RuntimeAgent agent
            , RLRule braRule
            , String stateName
            , Fact statefactelem
            , double min
            , double max
            , Context context) throws JessException {

        Attribute2ValuesMap consts = new Attribute2ValuesMap();

        String s = statefactelem.getDeftemplate().getBaseName();
        String pm = statefactelem.getSlotValue("param-name").stringValue(context);

        if (s.equals("state-fact-element")) {
            double[] interval = consts.getInterval(pm);
            double m = statefactelem.getSlotValue("from").floatValue(context);
            double x = statefactelem.getSlotValue("to").floatValue(context);
            if (interval != null) {
                if (min < m) {
                    m = x;
                }
                if (max > m) {
                    x = max;
                }

            }
            consts.setIntervalAttributes(pm, m, x);

        } else {
            List<String> f = consts.getFillers(pm);
            consts.setSetAttributes(pm, f);
        }

        String n = "";
        try {
            n = b.buildExperimentationRule(braRule, stateName, consts);
            Logger.getLogger(DynamicValueRangeExtensionRuleBuilder.class).debug("New rule after dynamic attribute add:\n" + n);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return n;

    }

    public static String addCategoryToExperimentalRule(TreeExpansionBuilder b, RuntimeAgent a, RLRule r, String stateName, Fact[] statefactelems,
            String domainAttr, String newFiller, Context context) throws JessException {

        Attribute2ValuesMap consts = new Attribute2ValuesMap();

        for (int i = 0; i < statefactelems.length; i++) {
            String s = statefactelems[i].getDeftemplate().getBaseName();
            String pm = statefactelems[i].getSlotValue("param-name").stringValue(context);

            DomainAttribute attr = (DomainAttribute) a.getDefinition().resolvePath(Path.attributePath(pm.split("/")));
            String simpleAttrName = attr.getName();

            if (s.equals("state-fact-element")) {
                double m = statefactelems[i].getSlotValue("from").floatValue(context);
                double x = statefactelems[i].getSlotValue("to").floatValue(context);
                consts.setIntervalAttributes(pm, m, x);

            } else {
                String c = statefactelems[i].getSlotValue("category").stringValue(context);

                List<String> f = consts.getFillers(pm);
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
            Logger.getLogger(DynamicValueRangeExtensionRuleBuilder.class).debug("New rule after dynamic attribute add:\n" + n);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return n;

    }



    // TODO condition fehlt!
    public static String createNewExperimentalRule(TreeExpansionBuilder b, RLRule r, RuntimeAgent a, String stateName, Fact[] remainingStatefactelems,
            Context context, String param, double from, double to) throws JessException {

        Attribute2ValuesMap consts = new Attribute2ValuesMap();
        consts.setIntervalAttributes(param, from, to);

        for (int i = 0; i < remainingStatefactelems.length; i++) {
            String s = remainingStatefactelems[i].getDeftemplate().getBaseName();
            String pm = remainingStatefactelems[i].getSlotValue("name").stringValue(context);
            if (s.equals("state-fact-element")) {
                double m = remainingStatefactelems[i].getSlotValue("from").floatValue(context);
                double x = remainingStatefactelems[i].getSlotValue("to").floatValue(context);
                consts.setIntervalAttributes(pm, m, x);

            } else {
                String c = remainingStatefactelems[i].getSlotValue("category").stringValue(context);

                List<String> fillersNow = consts.getFillers(pm);// getFillers(consts, c);
                fillersNow = maybeAddFiller(fillersNow, c);// add filler to array

                consts.setSetAttributes(pm, fillersNow);
            }
        }

        String n = "";
        try {
            n = b.buildExperimentationRule(r, stateName, consts);
            // System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + n);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return n;

    }

    public static String createNewExperimentalRuleCat(TreeExpansionBuilder b, RLRule r, String stateName, String paramToExpand,
            List<String> fillersOfExpand, Fact[] constants, Context context) throws JessException {

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

                List<String> f = consts.getFillers(pm);// getFillers(consts, c);
                f = maybeAddFiller(f, c);

                consts.setSetAttributes(pm, f);
            }
        }

        String n = "";
        try {
            n = b.buildExperimentationRule(r, stateName, consts);
            // System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + n);
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.debug("new rule created: " + n);

        return n;

    }

    public static String createNewSelectionNodes(TreeExpansionBuilder b, RLRule r, RuntimeAgent a, String stateName, Fact elem, Fact[] constants,
            Context context) throws JessException {

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
                List<String> f = consts.getFillers(pm);
                f = maybeAddFiller(f, c);

                consts.setSetAttributes(pm, f);
            }
        }

        return n;

    }

    public static String createNewSelectionNodesCat(TreeExpansionBuilder b, RLRule r, String stateName, String param, List<String> fillers,
            Fact[] constants, int depth, Context context) throws JessException {

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
                List<String> fill = consts.getFillers(c);
                fill.add(c);
                consts.setSetAttributes(c, fill);
            }
        }

        return n;

    }

    private static List<String> maybeAddFiller(List<String> oldFillers, String newFiller) {
        if (oldFillers == null) {
            List<String> mutableList = new ArrayList<>();
            mutableList.add(newFiller);
            return mutableList;
        }
        for (String s : oldFillers) {
            if (s.equals(newFiller)) {
                return oldFillers;
            }
        }

        List<String> newFillers = new ArrayList<>(oldFillers);
        newFillers.add(newFiller);
        return newFillers;

    }

}