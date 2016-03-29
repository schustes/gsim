package de.s2.gsim.sim.behaviour.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;
import de.s2.gsim.def.objects.UnitOLD;
import de.s2.gsim.def.objects.agent.BehaviourDef;
import de.s2.gsim.def.objects.agent.BehaviourFrame;
import de.s2.gsim.def.objects.agent.GenericAgentClass;
import de.s2.gsim.def.objects.behaviour.ExpansionDef;
import de.s2.gsim.def.objects.behaviour.RLRule;
import de.s2.gsim.objects.attribute.AttributeConstants;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.GSimEngineException;
import jess.Fact;
import jess.JessException;
import jess.Rete;

public class RLParser {

    private RuntimeAgent agent;

    private RLRulesBuilder rlBuilder;

    private TreeExpansionBuilder tBuilder;

    public RLParser(RuntimeAgent agent) {
        this.agent = agent;
        rlBuilder = new RLRulesBuilder(agent);
        tBuilder = new TreeExpansionBuilder(agent);
    }

    public String build(Rete rete) throws GSimEngineException {

        try {
            String rules = doBuild();

            // System.out.println(rules);
            rete.executeCommand(rules);

            FactHandler.getInstance().parseAndInsertGlobalRLFacts(agent, rete);

            int n = countStateFactElems(rete);
            rete.executeCommand("(defglobal ?*state-elem-count* = " + n + ")");

            return rules;

        } catch (JessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int countStateFactElems(Rete rete) {

        int count = 0;

        try {
            Iterator iter = rete.listFacts();
            while (iter.hasNext()) {
                Fact f = (Fact) iter.next();
                if (f.getDeftemplate().getBaseName().equals("state-fact-element") || f.getDeftemplate().getBaseName().equals("state-fact-category")) {
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private String doBuild() throws GSimEngineException {

        try {
            String res = "";

            BehaviourDef b = agent.getBehaviour();
            RLRule[] r = b.getRLRules();

            if (r.length == 0) {
                return "";
            }

            HashMap<String, String> updateRules = new HashMap<String, String>();

            String s = "";

            boolean hasExpansion = false;
            for (int i = 0; i < r.length; i++) {
                if (r[i].isActivated() && r[i].getAttribute("equivalent-actionset") == null) {

                    if (!r[i].hasExpansions()) {
                        String expRuleFinal = rlBuilder.createRLRuleSet(r[i]);
                        res += expRuleFinal + "\n";
                    } else if (r[i].hasExpansions()) {
                        String initialStateName = r[i].getName() + "_0" + "0";
                        Attribute2ValuesMap exp = new Attribute2ValuesMap();

                        extractConditionRefs(r[i], exp);

                        res += rlBuilder.createRLHelpRuleSetOnly(r[i]);
                        res += tBuilder.buildInitialRule(r[i], initialStateName, exp);

                    }

                    updateRules.put(r[i].getEvaluationFunction().getParameterName(),
                            rlBuilder.buildExperimentationUpdateRule(r[i], r[i].getEvaluationFunction()));
                    updateRules.put(r[i].getEvaluationFunction().getParameterName() + "avg",
                            rlBuilder.buildAvgRule(r[i], r[i].getEvaluationFunction()));
                }

            }

            Map<String, UnitOLD> expansionMap = new HashMap<String, UnitOLD>();

            for (int i = 0; i < r.length; i++) {

                if (r[i].isActivated() && r[i].getAttribute("equivalent-actionset") != null) {

                    String expRuleFinal = rlBuilder.buildIntermediateRule(r[i]);
                    res += expRuleFinal + "\n";

                }

                if (r[i].hasExpansions()) {
                    // hasExpansion = true;
                    String roleName = ParsingUtils.getDefiningRoleForRLRule(agent, r[i].getName());
                    // int expandInterval = this.agent.getBehaviour().getStateUpdateInterval();
                    // double revisitCostFraction = this.agent.getBehaviour().getRevisitCost();
                    // double revaluationProbability = this.agent.getBehaviour().getRevalProb();
                    // s += new ExpansionRulesBuilder().build(expandInterval,revisitCostFraction, revaluationProbability, roleName);
                    expansionMap.put(roleName, agent.getBehaviour());

                    InstanceOLD inst = agent;
                    FrameOLD[] frames = inst.getDefinition().getAncestors();
                    for (FrameOLD f : frames) {
                        if (f.getTypeName().equals(roleName)) {
                            GenericAgentClass a = (GenericAgentClass) f;
                            expansionMap.put(roleName, a.getBehaviour());
                        }
                    }
                }

            }

            ExpansionRulesBuilder erb = new ExpansionRulesBuilder();
            for (String n : expansionMap.keySet()) {
                UnitOLD u = expansionMap.get(n);
                if (u instanceof InstanceOLD) {
                    BehaviourDef beh = (BehaviourDef) u;
                    int expandInterval = beh.getStateUpdateInterval();
                    double revisitCostFraction = beh.getRevisitCost();
                    double revaluationProbability = beh.getRevalProb();
                    s += erb.build(expandInterval, revisitCostFraction, revaluationProbability, n);
                } else {
                    BehaviourFrame beh = (BehaviourFrame) u;
                    int expandInterval = beh.getStateUpdateInterval();
                    double revisitCostFraction = beh.getRevisitCost();
                    double revaluationProbability = beh.getRevalProb();
                    s += erb.build(expandInterval, revisitCostFraction, revaluationProbability, n);
                }
            }
            s += erb.createStateDescriptionQueries();
            s += erb.createRetractFollowRules();

            // if (hasExpansion) {
            // int expandInterval = this.agent.getBehaviour().getStateUpdateInterval();
            // double revisitCostFraction = this.agent.getBehaviour().getRevisitCost();
            // double revaluationProbability = this.agent.getBehaviour().getRevalProb();
            // String roleName = agent.getDefinition().getTypeName();
            // s += new ExpansionRulesBuilder().build(expandInterval,revisitCostFraction, revaluationProbability, roleName);
            // }

            res += s;

            res += "\n";
            for (String rule : updateRules.values()) {
                res += rule;
                res += "\n";
            }
            res += "\n";
            String ns = agent.getNameSpace();
            ns = ns.substring(0, ns.indexOf("/"));

            res += rlBuilder.buildExecutionFunction(ns);

            return res;
        } catch (Exception e) {
            throw new GSimEngineException("Rulesystem for agent " + agent.getName() + " could not be built.", e);
        }
    }

    private void extractConditionRefs(RLRule r, Attribute2ValuesMap exp) {
        for (ExpansionDef e : r.getExpansions()) {
            String path = e.getParameterName();
            DomainAttribute a = null;
            if (path.contains("::")) {
                ConditionBuilder cb = new ConditionBuilder();
                String obj = cb.resolveObjectClass(path);
                FrameOLD f = (FrameOLD) agent.getDefinition().resolveName(obj.split("/"));
                if (f == null) {
                    String list = cb.resolveList(path);
                    f = agent.getDefinition().getListType(list);
                }
                String att = cb.resolveAttribute(path);
                a = (DomainAttribute) f.resolveName(att.split("/"));
            } else {
                a = (DomainAttribute) agent.getDefinition().resolveName(path.split("/"));
            }
            if (a.getType().equals(AttributeConstants.INTERVAL)) {
                exp.setIntervalAttributes(path, Double.parseDouble(e.getFillers()[0]), Double.parseDouble(e.getFillers()[1]));
            } else if (a.getType().equals(AttributeConstants.SET)) {
                exp.setSetAttributes(path, e.getFillers());
            }
        }

    }

}
