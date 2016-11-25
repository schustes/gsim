package de.s2.gsim.sim.behaviour.impl;

import java.util.ArrayList;
import java.util.HashMap;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.UserRule;
import de.s2.gsim.environment.UserRuleFrame;
import de.s2.gsim.objects.attribute.StringAttribute;
import de.s2.gsim.sim.GSimEngineException;

public class GeneralRLBuilder {

	private RuntimeAgent agent = null;

	private ConditionBuilder conditionBuilder = null;

	public GeneralRLBuilder(RuntimeAgent agent, ConditionBuilder conditionBuilder) {
		this.conditionBuilder = conditionBuilder;
		this.agent = agent;
	}

	public String buildExperimentationRule(RLRule rule, String stateName, String condStr) throws GSimEngineException {

		String ownerRule = createRuleIdentifier(rule);
		String ruleName = "experimental_rule_" + ownerRule + "@" + stateName + "@";// +
		// createId();
		String defaultIdentifier = ownerRule;

		String role = ParsingUtils.getDefiningRoleForRLRule(agent, ownerRule);

		try {

			RLRule real = resolveEquivalentCondition(rule);

			String nRule = "(defrule " + ruleName + "\n";
			nRule += " (declare (salience +4999))\n";
			nRule += " (parameter (name \"exec-RLRule\"))\n";

			// >>>>>>>>>>>>>>>>>>BG>>>>>>>>>>>>>>>>>//
			// removed
			// nRule += " (not (experimented-" + defaultIdentifier + "))\n";

			nRule += " (state-fact (name ?sfn&:(eq ?sfn \"" + stateName + "\")) (expansion $?exp))\n";

			if (hasCatExpansion(rule)) {
				nRule += " (not (exists (state-fact (active 1.0) (parent ?p&:(eq ?p ?sfn)) ) ))\n";
			} else if (hasNumericalExpansion(rule)) {
				nRule += " (state-fact-element (name ?sfe) (state-fact-name ?sfn) (from ?lower) (to ?upper))\n";
				nRule += " (not (exists (state-fact-element (elem-parent ?sfe) (param-name ?pn&:(member$ ?pn ?exp)) (from ?l2&:(= ?l2 ?lower)) (to ?u2&:(< ?u2 ?upper)) )))\n";
				nRule += " (not (exists (state-fact-element (elem-parent ?sfe) (param-name ?pn&:(member$ ?pn ?exp)) (from ?l3&:(> ?l3 ?lower)) (to ?u3&:(= ?u3 ?upper)) )))\n";
			}

			nRule += " (parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n";
			// nRule += " (parameter (name \"exec-interval\") (value ?exc&:(= 0 (mod ?*current-time* ?exc))))\n";
			nRule += condStr;
			nRule += " ?time <-(timer (time ?n))\n";

			String[] pointers = getPointingNodes(rule);
			if (pointers.length > 0) {
				nRule += " (" + rule.getName() + ")\n";
			}

			nRule += "  =>\n";
			String realOwnerName = createRuleIdentifier(real);
			// nRule+="(printout
			// ********************************EXECUTE**************************)";
			nRule += " (bind ?action (selectBestAction_" + realOwnerName + " ?sfn))\n";
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


	private String createRuleIdentifier(Instance inst) {
		String x = inst.getName();
		x = x.replace(' ', '_');
		x = x.replace('/', '_');
		x = x.replace(',', '_');
		x = x.replace(')', ']');
		x = x.replace('(', '[');
		return x;
	}

	private String[] getPointingNodes(RLRule to) {
		ArrayList<String> list = new ArrayList<String>();
		for (RLRule r : agent.getBehaviour().getRLRules()) {
			if (r.containsAttribute("equivalent-actionset") ) {
				String pointingTo = r.getAttribute("equivalent-actionset").toValueString();
				if (pointingTo.equals(to.getName())) {
					list.add(r.getName());
				}
			}
		}
		String[] res = new String[list.size()];
		list.toArray(res);
		return res;
	}

	private boolean hasCatExpansion(RLRule rule) {
		for (ExpansionDef e : rule.getExpansions()) {
			if (Double.isNaN(e.getMin()) && Double.isNaN(e.getMax())) {
				return true;
			}
		}
		return false;
	}

	private boolean hasNumericalExpansion(RLRule rule) {
		for (ExpansionDef e : rule.getExpansions()) {
			if (!Double.isNaN(e.getMin()) && !Double.isNaN(e.getMax())) {
				return true;
			}
		}
		return false;
	}

	private boolean isConstant(String s) {
		if (s.contains("::")) {
			return false;
		}
		return true;
	}

	private boolean isExistQuantified(ConditionDef c) {
		return c.getOperator().trim().equalsIgnoreCase("EXISTS") || c.getOperator().trim().equalsIgnoreCase("~EXISTS")
				|| c.getOperator().trim().equalsIgnoreCase("NOT EXISTS");
	}

	private RLRule resolveEquivalentCondition(RLRule c) {
		if (c.containsAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "equivalent-state")) {
			StringAttribute s = (StringAttribute) c.getAttribute(UserRuleFrame.ATTR_LIST_ATTRS, "equivalent-state");
			RLRule next = agent.getBehaviour().getRLRule(s.getValue());
			return resolveEquivalentCondition(next);
		}

		return c;

	}

}
