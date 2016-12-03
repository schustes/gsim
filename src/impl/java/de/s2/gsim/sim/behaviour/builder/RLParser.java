package de.s2.gsim.sim.behaviour.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.BehaviourDef;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.util.ReteHelper;
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

            rete.executeCommand(rules);

            ReteHelper.parseAndInsertGlobalRLFacts(agent, rete);

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
			List<RLRule> r = b.getRLRules();

			if (r.size() == 0) {
                return "";
            }

            HashMap<String, String> updateRules = new HashMap<String, String>();

            String s = "";

            boolean hasExpansion = false;
			for (int i = 0; i < r.size(); i++) {
				if (r.get(i).isActivated() && !r.get(i).containsAttribute("equivalent-actionset")) {

					if (!r.get(i).hasExpansions()) {
						String expRuleFinal = rlBuilder.createRLRuleSet(r.get(i));
                        res += expRuleFinal + "\n";
					} else if (r.get(i).hasExpansions()) {
						String initialStateName = r.get(i).getName() + "_0" + "0";
                        Attribute2ValuesMap exp = new Attribute2ValuesMap();

						extractConditionRefs(r.get(i), exp);

						res += rlBuilder.createRLHelpRuleSetOnly(r.get(i));
						res += tBuilder.buildInitialRule(r.get(i), initialStateName, exp);

                    }

					updateRules.put(r.get(i).getEvaluationFunction().getParameterName(),
					        rlBuilder.buildExperimentationUpdateRule(r.get(i), r.get(i).getEvaluationFunction()));
					updateRules.put(r.get(i).getEvaluationFunction().getParameterName() + "avg",
					        rlBuilder.buildAvgRule(r.get(i), r.get(i).getEvaluationFunction()));
                }

            }

            Map<String, Unit> expansionMap = new HashMap<String, Unit>();

			for (int i = 0; i < r.size(); i++) {

				if (r.get(i).isActivated() && r.get(i).containsAttribute("equivalent-actionset")) {

					String expRuleFinal = rlBuilder.buildIntermediateRule(r.get(i));
                    res += expRuleFinal + "\n";

                }

				if (r.get(i).hasExpansions()) {
					String roleName = ParsingUtils.getDefiningRoleForRLRule(agent, r.get(i).getName());
                    expansionMap.put(roleName, agent.getBehaviour());

                    Instance inst = agent;
                    for (Frame f : inst.getDefinition().getAncestors()) {
                        if (f.getName().equals(roleName)) {
                            GenericAgentClass a = (GenericAgentClass) f;
                            expansionMap.put(roleName, a.getBehaviour());
                        }
                    }
                }

            }

            ExpansionRulesBuilder erb = new ExpansionRulesBuilder();
            for (String n : expansionMap.keySet()) {
                Unit u = expansionMap.get(n);
                if (u instanceof Instance) {
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
                Frame f = (Frame) agent.getDefinition().resolvePath(Path.attributePath(obj.split("/")));
                if (f == null) {
                    String list = cb.resolveList(path);
                    f = agent.getDefinition().getListType(list);
                }
                String att = cb.resolveAttribute(path);
                a = f.resolvePath(Path.attributePath(att.split("/")));
            } else {
                a = agent.getDefinition().resolvePath(Path.attributePath(path.split("/")));
            }
            if (a.getType() == AttributeType.INTERVAL) {
                exp.setIntervalAttributes(path, Double.parseDouble(e.getFillers().get(0)), Double.parseDouble(e.getFillers().get(1)));
            } else if (a.getType() == AttributeType.SET) {
                exp.setSetAttributes(path, e.getFillers());
            }
        }

    }

}
