package de.s2.gsim.sim.behaviour.impl;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.def.objects.Instance;
import de.s2.gsim.def.objects.agent.BehaviourDef;
import de.s2.gsim.def.objects.agent.BehaviourFrame;
import de.s2.gsim.def.objects.behaviour.ActionDef;
import de.s2.gsim.def.objects.behaviour.ConditionDef;
import de.s2.gsim.def.objects.behaviour.ExpansionDef;
import de.s2.gsim.def.objects.behaviour.RLRule;
import de.s2.gsim.def.objects.behaviour.RLRuleFrame;
import de.s2.gsim.def.objects.behaviour.UserRuleFrame;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.util.Utils;
import jess.Deftemplate;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;

public class JessHandlerUtils {

    private static Logger logger = Logger.getLogger(JessHandler.class);

    public static void addUserParams(Rete rete, RuntimeAgent owner) {
        for (ActionDef r : owner.getBehaviour().getAvailableActions()) {
            addUserParams0(rete, owner, r.getObjectClassParams());
        }
    }

    public static void assertObjectParam(Rete rete, String pathToFrame, String instanceName) {
        try {
            Deftemplate p = rete.findDeftemplate("object-parameter");
            Fact f = new Fact(p);
            f.setSlotValue("object-class", new Value(pathToFrame, RU.STRING));
            f.setSlotValue("instance-name", new Value(instanceName, RU.STRING));
            rete.assertFact(f);
        } catch (JessException e) {
            e.printStackTrace();
        }
    }

    public static void assertParameter(Rete rete, String name, String val) {
        Fact f = null;
        try {
            Deftemplate p = rete.findDeftemplate("parameter");

            if (val == null) {
                val = "0";
            }
            f = new Fact(p);
            f.setSlotValue("name", new Value(name, RU.STRING));

            if (de.s2.gsim.util.Utils.isNumerical(val)) {
                f.setSlotValue("value", new Value(Double.valueOf(val).doubleValue(), RU.FLOAT));
            } else {
                f.setSlotValue("value", new Value(val, RU.STRING));
            }
            rete.assertFact(f);
        } catch (JessException e) {
            f = null;
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked")
    public static HashSet<String> buildCurrentState(Rete rete, RuntimeAgent owner) throws JessException {

        ArrayList res = new ArrayList();
        BehaviourDef b = owner.getBehaviour();

        for (de.s2.gsim.def.objects.behaviour.ActionDef a : b.getAvailableActions()) {
            if (a.hasObjectParameter()) {
                for (String param : a.getObjectClassParams()) {
                    if (param != null && param.length() > 0) {
                        for (Instance inst : Utils.getChildInstancesOfType(owner, param)) {
                            if (inst.getName().matches(".*" + a.getFilterExpression(param) + ".*")) {
                                assertObjectParam(rete, param, inst.getName());
                            }
                        }
                    }
                }
            }
        }

        HashSet<String> uniqueConditions = new HashSet<String>();

        for (Instance r : owner.getBehaviour().getChildInstances(BehaviourFrame.RULE_LIST)) {
            for (Instance cc : r.getChildInstances(UserRuleFrame.INST_LIST_COND)) {
                ConditionDef c = new ConditionDef(cc);
                uniqueConditions.add(c.getParameterName());
                uniqueConditions.add(c.getParameterValue());
            }
        }

        for (Instance r : owner.getBehaviour().getChildInstances(BehaviourFrame.RL_LIST)) {
            for (Instance cc : r.getChildInstances(UserRuleFrame.INST_LIST_COND)) {
                ConditionDef c = new ConditionDef(cc);
                uniqueConditions.add(c.getParameterName());
                uniqueConditions.add(c.getParameterValue());
            }
            for (Instance cc : r.getChildInstances(RLRuleFrame.INST_LIST_EXP)) {
                ExpansionDef c = new ExpansionDef(cc);
                uniqueConditions.add(c.getParameterName());
                uniqueConditions.add(String.valueOf(c.getMin()));
                uniqueConditions.add(c.getParameterName());
                uniqueConditions.add(String.valueOf(c.getMax()));
            }

            for (Instance sc : r.getChildInstances(RLRuleFrame.INST_LIST_SHORTCUTS)) {
                for (Instance cc : sc.getChildInstances(UserRuleFrame.INST_LIST_COND)) {
                    ConditionDef c = new ConditionDef(cc);
                    if (!c.getParameterName().contains("$")) {
                        throw new RuntimeException("Parse error: selectors must reference action-nodes on their LHS");
                    }
                    uniqueConditions.add(c.getParameterName());
                    uniqueConditions.add(c.getParameterValue());
                }

            }
            for (Instance eval : r.getChildInstances(RLRuleFrame.INST_LIST_LEARNING)) {
                String s = eval.getName();
                if (!eval.getName().startsWith("{")) {
                    ConditionDef c = new ConditionDef(eval);
                    s = c.getParameterName();
                    uniqueConditions.add(c.getParameterName());
                    uniqueConditions.add(c.getParameterValue());
                }
            }

            long h = System.currentTimeMillis();
            String sfn = r.getName() + "_0" + 0;
            FactHandler.getInstance().insertNonExistentExecutedFinalFacts(rete, owner, new RLRule(r), sfn);
            double g = (System.currentTimeMillis() - h) / 1000d;

            logger.debug("NEW FINAL FACTS: " + (g));

        }

        for (String s : uniqueConditions) {
            if (s.contains("::")) {
                res.addAll(JessHandlerUtils.buildReplaceVariablesSC(rete, owner, s));
            }
            HashSet l = JessHandlerUtils.buildReplaceConstants(rete, owner, s);
            res.addAll(l);
        }

        return uniqueConditions;

    }

    public static HashSet<String> buildCurrentState(Rete rete, RuntimeAgent owner, HashSet<String> uniqueConditions) throws JessException {
        for (String s : uniqueConditions) {
            if (s.contains("::")) {
                JessHandlerUtils.buildReplaceVariablesSC(rete, owner, s);
            }
            JessHandlerUtils.buildReplaceConstants(rete, owner, s);
        }

        addUserParams(rete, owner);

        return uniqueConditions;
    }

    private static void addUserParams0(Rete rete, RuntimeAgent owner, String[] params) {
        if (params == null || params != null && params.length == 0) {
            return;
        }
        for (String s : params) {
            String list = s.split("/")[0].trim();
            Instance[] objects = owner.getChildInstances(list);
            for (Instance inst : objects) {
                assertObjectParam(rete, s, inst.getName());
            }
        }
    }

    private static HashSet buildReplaceConstants(Rete rete, Instance owner, String n) {

        if (n.contains("::")) {
            return new HashSet(0);
        }

        if (!n.contains("/")) {
            // This is what I call an 'Atom' (it is a numerical constant value)
            return new HashSet(0);
        }

        String list = n.split("/")[0];

        for (String s : owner.getChildInstanceListNames()) {
            if (s.equals(list)) {
                return buildReplaceConstantsInstanceRef(rete, owner, n);
            }
        }
        return buildReplaceConstantsAttRef(rete, owner, n);
    }

    private static HashSet buildReplaceConstantsAttRef(Rete rete, Instance owner, String n) {

        HashSet list = new HashSet();
        String att = ParsingUtils.resolveAttribute(n);

        if (n.contains("{")) {
            String attRef = n.substring(n.indexOf("{") + 1, n.lastIndexOf("}"));
            Attribute ref = (Attribute) owner.resolveName(attRef.split("/"));
            if (ref instanceof IntervalAttribute) {
                double val = ((IntervalAttribute) ref).getValue();// ( ((IntervalAttribute) ref).getFrom() + ((IntervalAttribute) ref).getTo()) / 2d;
                assertParameter(rete, attRef, String.valueOf(val));
            } else {
                assertParameter(rete, attRef, ref.toValueString());
            }
        } else {
            Attribute ref = (Attribute) owner.resolveName(att.split("/"));
            if (ref instanceof IntervalAttribute) {
                double val = ((IntervalAttribute) ref).getValue();
                // double val = (((IntervalAttribute) ref).getFrom() + ((IntervalAttribute) ref).getTo() / 2d);
                assertParameter(rete, att, String.valueOf(val));
            } else {
                if (ref == null) {
                    System.out.println("not resolved:" + att);
                }
                assertParameter(rete, att, ref.toValueString());
            }
        }

        return list;
    }

    private static HashSet buildReplaceConstantsInstanceRef(Rete rete, Instance owner, String n) {

        HashSet list = new HashSet();

        String listName = ParsingUtils.resolveList(n);
        String object = ParsingUtils.resolveObjectClassNoList(n);

        Instance[] insts = owner.getChildInstances(listName);
        if (insts == null) {
            return list;
        }

        for (Instance inst : insts) {

            /*
             * if (att!=null && n.contains("{")) { String attRef=n.substring(n.indexOf("{")+1, n.lastIndexOf("}")); Attribute ref =
             * (Attribute)owner.resolveName(attRef.split("/")); String fullPath = listName + "/" + object + "/" + attRef; assertParameter(rete,
             * fullPath, ref.toValueString()); // Constant pp = new Constant(fullPath, ref.toValueString()); // list.add(pp); } else if (att!=null) {
             * Attribute ref = (Attribute) inst.resolveName(att.split("/")); String fullPath = listName + "/" + object + "/" + att; if (ref==null) {
             * int a=0; } assertParameter(rete, fullPath, ref.toValueString()); // Constant pp = new Constant(fullPath, ref.toValueString()); //
             * list.add(pp); }
             */
            assertObjectParam(rete, listName + "/" + object, inst.getName());
            // ObjectFactRepresentation t = new
            // ObjectFactRepresentation(inst.getName(), list+"/"+object);
            // list.add(t);

        }

        return list;
    }

    private static HashSet buildReplaceVariablesSC(Rete rete, Instance owner, String n) {

        HashSet list = new HashSet();

        String listName = ParsingUtils.resolveList(n);
        String att = ParsingUtils.resolveAttribute(n);
        String object = ParsingUtils.resolveObjectClassNoList(n);

        Instance[] insts = owner.getChildInstances(listName);
        if (insts == null) {
            return list;
        }

        for (Instance inst : insts) {

            if (att.contains("{")) {
                String attRef = att.substring(1, att.length() - 1);
                Attribute a = (Attribute) owner.resolveName(attRef.split("/"));
                if (a instanceof IntervalAttribute) {
                    double val = (((IntervalAttribute) a).getFrom() + ((IntervalAttribute) a).getTo() / 2d);
                    assertParameter(rete, attRef, String.valueOf(val));
                } else {
                    assertParameter(rete, attRef, a.toValueString());
                }
            } else {
                Attribute ref = (Attribute) inst.resolveName(att.split("/"));

                String fullPath = listName + "/" + inst.getName() + "/" + att;

                if (ref == null) {
                    logger.warn("Could not resolve attribute " + att + ", fullPath=" + fullPath);
                } else {

                    if (ref instanceof IntervalAttribute) {
                        double val = (((IntervalAttribute) ref).getFrom() + ((IntervalAttribute) ref).getTo() / 2d);
                        assertParameter(rete, fullPath, String.valueOf(val));
                    } else {
                        assertParameter(rete, fullPath, ref.toValueString());
                    }
                }

                // assertParameter(rete, fullPath, ref.toValueString());
            }

            assertObjectParam(rete, listName + "/" + object, inst.getName());

            // ObjectFactRepresentation fact = new
            // ObjectFactRepresentation(inst.getName(), listName+"/"+object);
            // list.add(fact);

        }

        return list;
    }

}
