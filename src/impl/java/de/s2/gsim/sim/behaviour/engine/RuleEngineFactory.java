package de.s2.gsim.sim.behaviour.engine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.BehaviourDef;
import de.s2.gsim.environment.BehaviourFrame;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgentClass;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.rulebuilder.BuildingUtils;
import de.s2.gsim.sim.behaviour.rulebuilder.ExpansionParameterReferences;
import de.s2.gsim.sim.behaviour.rulebuilder.ExpansionRulesBuilder;
import de.s2.gsim.sim.behaviour.rulebuilder.RLRulesBuilder;
import de.s2.gsim.sim.behaviour.rulebuilder.TreeExpansionBuilder;
import de.s2.gsim.sim.behaviour.util.RuleEngineHelper;
import jess.Fact;
import jess.JessException;
import jess.Rete;

public class RuleEngineFactory {

    private RuntimeAgent agent;

    public RuleEngineFactory(RuntimeAgent agent) {
        this.agent = agent;
    }

    public String build(Rete rete) throws GSimEngineException {

        try {
            String rules = doBuild();

            rete.executeCommand(rules);

            RuleEngineHelper.parseAndInsertGlobalRLFacts(agent, rete);

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
			Iterator<?> iter = rete.listFacts();
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

	@SuppressWarnings("rawtypes")
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

			for (int i = 0; i < r.size(); i++) {
				if (r.get(i).isActivated() && !r.get(i).containsAttribute("equivalent-actionset")) {

					if (!r.get(i).hasExpansions()) {
						String expRuleFinal = RLRulesBuilder.createRLRuleSet(agent, r.get(i));
                        res += expRuleFinal + "\n";
					} else if (r.get(i).hasExpansions()) {
						String initialStateName = r.get(i).getName() + "_0" + "0";
                        ExpansionParameterReferences exp = new ExpansionParameterReferences();

						extractConditionRefs(r.get(i), exp);

						res += RLRulesBuilder.createRLHelpRuleSetOnly(agent, r.get(i));
						res += TreeExpansionBuilder.buildInitialRule(agent, r.get(i), initialStateName, exp);

                    }

					updateRules.put(r.get(i).getEvaluationFunction().getParameterName(),
					        RLRulesBuilder.buildExperimentationUpdateRule(agent, r.get(i), r.get(i).getEvaluationFunction()));
					updateRules.put(r.get(i).getEvaluationFunction().getParameterName() + "avg",
					        RLRulesBuilder.buildAvgRule(agent, r.get(i), r.get(i).getEvaluationFunction()));
                }

            }

            Map<String, Unit> expansionMap = new HashMap<String, Unit>();

			for (int i = 0; i < r.size(); i++) {

				if (r.get(i).isActivated() && r.get(i).containsAttribute("equivalent-actionset")) {

					String expRuleFinal = RLRulesBuilder.buildIntermediateRule(agent, r.get(i));
                    res += expRuleFinal + "\n";

                }

				if (r.get(i).hasExpansions()) {
					String roleName = BuildingUtils.getDefiningRoleForRLRule(agent, r.get(i).getName());
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

            for (String n : expansionMap.keySet()) {
                Unit u = expansionMap.get(n);
                if (u instanceof Instance) {
                    BehaviourDef beh = (BehaviourDef) u;
                    int expandInterval = beh.getStateUpdateInterval();
					int contractInterval = beh.getStateContractInterval();
                    double revisitCostFraction = beh.getRevisitCost();
                    double revaluationProbability = beh.getRevalProb();
					s += ExpansionRulesBuilder.build(expandInterval, contractInterval, revisitCostFraction, revaluationProbability, n);
                } else {
                    BehaviourFrame beh = (BehaviourFrame) u;
                    int expandInterval = beh.getStateUpdateInterval();
					int contractInterval = beh.getStateContractInterval();
                    double revisitCostFraction = beh.getRevisitCost();
                    double revaluationProbability = beh.getRevalProb();
					s += ExpansionRulesBuilder.build(expandInterval, contractInterval, revisitCostFraction, revaluationProbability, n);
                }
            }
			s += ExpansionRulesBuilder.createStateDescriptionQueries();
			s += ExpansionRulesBuilder.createRetractFollowRules();

            res += s;

            res += "\n";
            for (String rule : updateRules.values()) {
                res += rule;
                res += "\n";
            }
            res += "\n";
            String ns = agent.getNameSpace();
            ns = ns.substring(0, ns.indexOf("/"));

			res += RLRulesBuilder.buildExecutionFunction(ns);

            return res;
        } catch (Exception e) {
            throw new GSimEngineException("Rulesystem for agent " + agent.getName() + " could not be built.", e);
        }
    }

    private void extractConditionRefs(RLRule r, ExpansionParameterReferences exp) {
        for (ExpansionDef e : r.getExpansions()) {
            String path = e.getParameterName();

			Optional<DomainAttribute> a = BuildingUtils.extractAttribute(agent.getDefinition(), path);

			if (a.isPresent() && a.get().getType() == AttributeType.INTERVAL) {
				exp.setIntervalAttributes(path, e.getMin(), e.getMax());
			} else if (a.isPresent() && a.get().getType() == AttributeType.SET) {
                exp.setSetAttributes(path, e.getFillers());
            }
        }

    }

}
