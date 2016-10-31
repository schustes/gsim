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

	public String createShortcut(RLRule owner, UserRule rule, String stateName, String conditionString,
			Object2VariableBindingTable preConditionObjRefs) throws GSimEngineException {

		String ownerRule = createRuleIdentifier(owner);

		String ruleName = "shortcut_rule_" + ownerRule + "__" + createRuleIdentifier(rule) + "@" + stateName.replace("::", "@");// ""+someCounter++;

		String role = ParsingUtils.getDefiningRoleForRLRule(agent, ownerRule);

		String n = "(defrule " + ruleName + "\n";
		n += " (declare (salience 5999))\n";
		n += " (parameter (name \"exec-sc\"))\n";

		// >>>>>>>>>>>>>>>>>>BG>>>>>>>>>>>>>>>>>//
		// removed
		// n += " (not (experimented-" + this.createRuleIdentifier(owner) + "))\n";

		n += " (state-fact (name ?sfn&:(eq ?sfn \"" + stateName + "\")) (expansion $?exp))\n";

		if (hasCatExpansion(owner)) {
			n += " (not (exists (state-fact (active 1.0) (parent ?p&:(eq ?p ?sfn)) ) ))\n";
		} else if (hasNumericalExpansion(owner)) {
			n += " (state-fact-element (name ?sfe) (state-fact-name ?sfn) (from ?lower) (to ?upper))\n";
			n += " (not (exists (state-fact-element (elem-parent ?sfe) (param-name ?pn&:(member$ ?pn ?exp)) (from ?l2&:(= ?l2 ?lower)) (to ?u2&:(< ?u2 ?upper)) )))\n";
			n += " (not (exists (state-fact-element (elem-parent ?sfe) (param-name ?pn&:(member$ ?pn ?exp)) (from ?l3&:(> ?l3 ?lower)) (to ?u3&:(= ?u3 ?upper)) )))\n";
		}

		// n += " (parameter (name \"exec-interval\") (value ?exc&:(= 0 (mod ?*current-time* ?exc))))\n";
		n += " (parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n";
		n += conditionString;

		// >>>>>>>>>>>>>>>>>>BG>>>>>>>>>>>>>>>>>//
		// removed
		// n += " (not (experimented-" + ownerRule + "))\n";

		n += " (list (name \"" + ownerRule + "\") (obj ?list))\n";

		String[] pointers = getPointingNodes(owner);
		if (pointers.length > 0) {
			n += " (" + owner.getName() + ")\n";
		}

		HashMap<Integer, ActionDef> actionRefs = new HashMap<Integer, ActionDef>();
		HashMap<ActionDef, ArrayList<String>> objectRefs = new HashMap<ActionDef, ArrayList<String>>();

		HashMap<Integer, ActionDef> actions = new HashMap<Integer, ActionDef>();
		HashMap<ActionDef, ArrayList<String>> objects = new HashMap<ActionDef, ArrayList<String>>();

		for (int i = 0; i < agent.getBehaviour().getAvailableActions().length; i++) {
			ActionDef action = agent.getBehaviour().getAvailableActions()[i];
			int refPos = i;
			actions.put(refPos, action);
			if (action.hasObjectParameter()) {
				for (String p : action.getObjectClassParams()) {
					ArrayList<String> list = objects.get(action);
					if (list == null) {
						list = new ArrayList<String>();
					}
					list.add(p);
					objects.put(action, list);
				}
			}
		}

		for (int i = 0; i < rule.getConditions().length; i++) {
			ConditionDef cond = rule.getConditions()[i];

			String[] a = cond.getParameterName().split("::")[0].split("\\$");
			if (a.length > 1) {
				String actionRef = a[1].trim();

				String objRef = a.length > 2 ? a[2].trim() : null;

				ActionDef action = agent.getBehaviour().getAction(actionRef);
				actionRefs.put(i, action);

				if (objRef != null) {
					if (action.hasObjectParameter()) {
						for (String param : action.getObjectClassParams()) {
							if (param.equals(objRef)) {
								ArrayList<String> list = new ArrayList<String>();
								if (objectRefs.containsKey(action)) {
									list = objectRefs.get(action);
								}
								list.add(param);
								objectRefs.put(action, list);
							}
						}
					}
				}
			} else {
				// condition specifies a constant - ignore
			}
		}

		HashMap<String, Integer> paramIndices = new HashMap<String, Integer>();
		for (int actionCtxNumber : actionRefs.keySet()) {
			ActionDef action = actionRefs.get(actionCtxNumber);
			ArrayList<String> list = objectRefs.get(action);
			if (list != null) {
				int number = 0;
				for (String param : list) {
					if (!param.startsWith("{")) {

						if (preConditionObjRefs.getBinding(param) == null) {
							// n += " (object-parameter (object-class "+ "\"" +
							// param +"\") (instance-name ?pValue"
							// + (number + actionCtxNumber * 100) + "))\n";

							paramIndices.put(param, number + actionCtxNumber * 100);
							number++;
						}
					}
				}
			}
		}

		String prefix = "";
		String postFix = "";
		HashMap<ActionDef, ArrayList<String>> resolvedObjRefs = new HashMap<ActionDef, ArrayList<String>>();

		Object2VariableBindingTable scCond = new Object2VariableBindingTable();

		for (int i = 0; i < rule.getConditions().length; i++) {

			ConditionDef cond = rule.getConditions()[i];

			String path = cond.getParameterName();

			if (path.contains("::")) {// otherwise action w/o parameters
				String[] p1 = path.split("::");
				String[] pathArray = p1[1].split("/");

				String objRef = p1.length > 1 ? p1[0].split("\\$")[2] : null;

				ActionDef a = actionRefs.get(i);

				prefix = objRef;
				String[] pp = prefix.split("/");
				prefix = "";
				for (int p = 0; p < pp.length - 1; p++) {
					prefix += pp[p];
					if (p < pp.length - 1) {
						prefix += "/";
					}
				}
				postFix = "";
				for (int p = 0; p < pathArray.length; p++) {
					postFix += "/" + pathArray[p];
				}

				ArrayList<String> ar = resolvedObjRefs.get(a);
				if (ar == null) {
					ar = new ArrayList<String>();
				}
				ar.add(objRef);
				resolvedObjRefs.put(a, ar);

				// add sc-conditions !!! IMPORTANT - by this you create the
				// variables for all referenced objects
				// that are used later!!!!

				scCond.build(rule);
				// scCond = utils.buildObjectRefTable(rule.getConditions());
				scCond.merge(preConditionObjRefs); // if the same refs already
				// exist in pre-condition,
				// use their variable values

				if (isConstant(cond.getParameterValue()) && !isExistQuantified(cond) && cond.getParameterValue().indexOf("{") < 0) {
					n += "" + conditionBuilder.createFixedAtomCondition(cond, scCond, n + n);
				} else if (isConstant(cond.getParameterValue()) && !isExistQuantified(cond)) {
					n += "" + conditionBuilder.createAttributeCondition(cond, scCond, n + n);
				} else if (!isConstant(cond.getParameterValue()) && !isExistQuantified(cond)) {
					n += "" + conditionBuilder.createVariableCondition(agent, cond, scCond, n + n);
				} else {
					n += "" + conditionBuilder.createExistsQuantifiedCondition(cond, scCond);
				}
			}
		}

		int actionFactRefs = 0;
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		for (ActionDef a : actionRefs.values()) {

			ArrayList<String> objParams = resolvedObjRefs.get(a);

			String s1 = " ?action" + actionFactRefs + " <- (rl-action-node (action-name ?n" + actionFactRefs + "&:(eq ?n" + actionFactRefs + " \""
					+ a.getClassName() + "\"))";

			if (objParams != null) {
				s1 += " (arg $?arguments" + actionFactRefs + "&:(and ";
				for (String s : objParams) {
					// int shortRef = scCond.getBinding(s);
					String shortRef = scCond.getBinding(s);
					String s0 = "(str-cat " + "\"" + ParsingUtils.resolveList(s) + "/\" " + shortRef + ")";
					if (!s1.contains(s)) {
						s1 += "(member$ " + s0 + " ?arguments" + actionFactRefs + ")";
					}
				}
				s1 += "))";
			}

			resolveEquivalentCondition(owner);
			s1 += "(state-fact-name ?sfn) )\n";

			n += s1;

			map.put(actionFactRefs, a.getName());

			actionFactRefs++;
		}

		n += " =>\n";
		// n+=" (printout t ************************** "+ruleName+" **** crlf
		// )\n";
		for (int i = 0; i < actionFactRefs; i++) {
			n += " (if (not (call ?list contains ?action" + i + ")) then \n";
			n += " (call ?list add ?action" + i + "))";
		}
		n += ")\n";

		return n + "\n";

	}

	private String createId() {
		return String.valueOf(cern.jet.random.Uniform.staticNextIntFromTo(0, 1000));
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
