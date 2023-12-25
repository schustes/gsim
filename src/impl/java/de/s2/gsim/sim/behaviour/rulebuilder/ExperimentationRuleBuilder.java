package de.s2.gsim.sim.behaviour.rulebuilder;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.util.BuildingUtils;

public abstract class ExperimentationRuleBuilder {

	private ExperimentationRuleBuilder() {
		// static class
	}

	static String buildExperimentationRule(RuntimeAgent agent, RLRule rule, String stateName, String condStr) throws GSimEngineException {

		String ownerRule = createRuleIdentifier(rule);
		String ruleName = "experimental_rule_" + ownerRule + "@" + stateName + "@";// +
		String defaultIdentifier = ownerRule;

		String role = BuildingUtils.getDefiningRoleForRLRule(agent, ownerRule);

		try {

			String nRule = "(defrule " + ruleName + "\n";
			nRule += " (declare (salience +4999))\n";
			nRule += " (parameter (name \"exec-RLRule\"))\n";

			nRule += " (state-fact (name ?sfn&:(eq ?sfn \"" + stateName + "\")) (expansion $?exp))\n";
			nRule += " (not (experimented-" + defaultIdentifier + "))\n";

			if (hasCatExpansion(rule)) {
				nRule += " (not (exists (state-fact (active 1.0) (parent ?p&:(eq ?p ?sfn)) ) ))\n";
			} else if (hasNumericalExpansion(rule)) {
				nRule += " (state-fact-element (name ?sfe) (state-fact-name ?sfn) (from ?lower) (to ?upper))\n";
				nRule += " (not (exists (state-fact-element (elem-parent ?sfe) (param-name ?pn&:(member$ ?pn ?exp)) (from ?l2&:(= ?l2 ?lower)) (to ?u2&:(<= ?u2 ?upper)) )))\n";
				nRule += " (not (exists (state-fact-element (elem-parent ?sfe) (param-name ?pn&:(member$ ?pn ?exp)) (from ?l3&:(>= ?l3 ?lower)) (to ?u3&:(= ?u3 ?upper)) )))\n";
			}

			nRule += " (parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n";
			nRule += condStr;
			nRule += " ?time <-(timer (time ?n))\n";

			nRule += "  =>\n";
			String realOwnerName = createRuleIdentifier(rule);
			nRule += " (bind ?action (selectBestAction_" + realOwnerName + " ?sfn))\n";
			//nRule += " (printout  t _____ SELECTED _____ ?action)\n";
			nRule += " (if (neq ?action NIL) then\n";
			nRule += "  (bind ?c (fact-slot-value ?action count))\n";
			nRule += "  (execute ?action \"" + role + "\")\n";
			nRule += "  (assert (experimented-" + defaultIdentifier + "))\n";
			nRule += "  (modify (call ?action getFactId) (count (+ ?c 1)) (time ?n)) )) \n";

			return nRule;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	private static String createRuleIdentifier(Instance inst) {
		String x = inst.getName();
		x = x.replace(' ', '_');
		x = x.replace('/', '_');
		x = x.replace(',', '_');
		x = x.replace(')', ']');
		x = x.replace('(', '[');
		return x;
	}

	private static boolean hasCatExpansion(RLRule rule) {
		for (ExpansionDef e : rule.getExpansions()) {
			if (Double.isNaN(e.getMin()) && Double.isNaN(e.getMax())) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasNumericalExpansion(RLRule rule) {
		for (ExpansionDef e : rule.getExpansions()) {
			if (!Double.isNaN(e.getMin()) && !Double.isNaN(e.getMax())) {
				return true;
			}
		}
		return false;
	}



}
