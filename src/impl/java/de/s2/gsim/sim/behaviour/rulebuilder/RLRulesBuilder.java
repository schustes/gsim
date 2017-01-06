package de.s2.gsim.sim.behaviour.rulebuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cern.jet.random.Uniform;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.Context;
import de.s2.gsim.sim.behaviour.SimAction;
import de.s2.gsim.util.Utils;

public abstract class RLRulesBuilder {

	private RLRulesBuilder() {
		// static class
	}


	public static String buildAvgRule(RuntimeAgent agent, RLRule rule, ConditionDef evaluationFunction) {

        String ownerRule = createRuleIdentifier(evaluationFunction);

        String role = BuildingUtils.getDefiningRoleForRLRule(agent, createRuleIdentifier(rule));

        String n = "(defrule update-global-average-rule_" + ownerRule + "\n" + " (declare (salience +222199))\n"
                + " (parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n"
                + " (timer (time ?currentTime))\n";

        String factString = " ?fact <- (rl-action-node (action-name ?n) (state-fact-name ?sfn) (arg $?param) (value ?old) (count ?times) "
                + "(function \"" + evaluationFunction.getParameterName() + "\")" + " (updateCount ?up) (count ?c&:(= ?up ?c) )" + ")\n";
        n += factString;

        n += " (modified ?n ?param ?sfn)\n";
        n += " (parameter (name \"" + evaluationFunction.getParameterName() + "\") (value ?currentReward))\n";
        n += " ?avg <- (average-reward (function \"" + evaluationFunction.getParameterName() + "\") "
                + "(value ?avgReward) (time ?lastUpdateTime&:(< ?lastUpdateTime ?currentTime)))\n";

        n += "  =>\n";

        n += " (bind ?reward (+ ?avgReward (* " + rule.getAvgStepSize() + " (- ?currentReward ?avgReward))) )\n";
        n += " (modify ?avg (value ?reward) (time ?currentTime)))\n";

        return n;

    }

	public static String buildExecutionFunction(String ns) {

        String nRule = "(deffunction execute(?action ?ctxName) \n";
        nRule += "  (bind $?param (fact-slot-value ?action arg))\n";
        nRule += "  (bind ?res (call " + SimAction.class.getName() +" valueOf (fact-slot-value ?action action-name) \"" + ns + "\"))\n";
        nRule += "  (bind ?ctx (new " + Context.class.getName() + "))\n";
        nRule += "  (set ?ctx executionContext (?*agent* getExecutionContext(new java.lang.String ?ctxName)))\n";
        nRule += "  (foreach ?object $?param (call ?ctx addObject ?object))\n";
        nRule += "  (call ?res setContext ?ctx)\n";
		nRule += "  (call ?res doExecute) )\n ";

        return nRule;
    }

	public static String buildExperimentationUpdateRule(RuntimeAgent agent, RLRule rule, ConditionDef evaluationFunction)
	        throws GSimEngineException {
        String span = rule.getUpdateSpan();
        if (span != null) {
			return buildExperimentationUpdateRule1(agent, rule, evaluationFunction, span);
        } else {
            String lag = rule.getUpdateLag();
            if (lag == null) {
                lag = "1";
            }
			return buildExperimentationUpdateRule0(agent, rule, evaluationFunction, lag);
        }
    }

	public static String buildIntermediateRule(RuntimeAgent agent, RLRule rule) {

        String nRule = "";

        try {
            String ownerRule = createRuleIdentifier(rule);
            String ruleName = "intermediate_rule_" + ownerRule;

            nRule = "(defrule " + ruleName + "\n";
            nRule += " (declare (salience +4999))\n";
            nRule += " (parameter (name \"exec-RLRule\"))\n";
            nRule += " (parameter (name \"executing-role\") (value " + "\"" + BuildingUtils.getDefiningRoleForRLRule(agent, ownerRule) + "\"))\n";
            // nRule += " (parameter (name \"exec-interval\") (value ?exc&:(= 0 (mod
            // ?*current-time* ?exc))))\n";

            // >>>>>>>>>>>>>>>>>>BG>>>>>>>>>>>>>>>>>//
            // removed
            // nRule += " (not (experimented-" + ownerRule + "))\n";

			Object2JessVariableBindingTable map = new Object2JessVariableBindingTable(agent);
            map.build(rule);

			nRule += createConditions(agent, rule, map);

            nRule += " ?time <-(timer (time ?n))\n";

			String[] pointers = getPointingNodes(agent, rule);
            if (pointers.length > 0) {
                nRule += " (" + rule.getName() + ")\n";
            }

            nRule += "  =>\n";
            nRule += " (assert (" + rule.getAttribute("equivalent-actionset").toValueString() + ")))\n";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return nRule;
    }

	public static String createRLHelpRuleSetOnly(RuntimeAgent agent, RLRule rule) throws GSimEngineException {
		String[] rules = buildExperimentationTerminalRules(agent, rule, true);

        String res = "";
        for (int i = 0; i < rules.length; i++) {
            res += rules[i];
        }

        return res;
    }

	public static String createRLRuleSet(RuntimeAgent agent, RLRule rule) throws GSimEngineException {

		String res = "";
        String[] rules = new String[0];
        if (res.length() > 0) {
			rules = buildExperimentationTerminalRules(agent, rule, true);
            res += "\n";
        } else {
			rules = buildExperimentationTerminalRules(agent, rule, false);
        }

        // String res = "";
        for (int i = 0; i < rules.length; i++) {
            res += rules[i];
        }

        // res += this.addShortCuts(rule, res) + "\n";

        return res;
    }

	private static String buildExperimentationFunctions(String ruleIdentifier, ActionDef[] a, FUNCTION f, ConditionDef funct) {

        String s1 = createListActionQuery(ruleIdentifier, a, funct);

        String s2 = createSelectBestActionFunction(ruleIdentifier, f);

        return s1 + "\n" + s2;

    }

	private static String[] buildExperimentationTerminalRules(RuntimeAgent agent, RLRule rule, boolean helpersOnly)
	        throws GSimEngineException {

        Set<String> singleRules = new HashSet<String>();

        String expRule = "";
		Object2JessVariableBindingTable objRefs = new Object2JessVariableBindingTable(agent);
        objRefs.build(rule);

        String initialStateName = rule.getName() + "_0" + "0";

        if (!helpersOnly) {
			String condStr = createConditions(agent, rule, objRefs);
			expRule += ExperimentationRuleBuilder.buildExperimentationRule(agent, rule, initialStateName, condStr) + "\n";
        }

        singleRules.add(expRule);

        if (helpersOnly || expRule.length() > 0) {
            FUNCTION f = rule.isComparison() ? FUNCTION.COMPARISON : FUNCTION.SIMPLE_SOFTMAX;

            String helpRulesForRootRule = "\n";
            if (!rule.containsAttribute("equivalent-state") && !rule.containsAttribute("equivalent-actionset") ) {
                helpRulesForRootRule = buildExperimentationFunctions(createRuleIdentifier(rule), rule.getConsequents(), f,
                        rule.getEvaluationFunction());

                helpRulesForRootRule += "\n";
                singleRules.add(helpRulesForRootRule);
            }

            f = null;

        }

        String[] ss = new String[singleRules.size()];
        singleRules.toArray(ss);

        return ss;
    }

	private static String buildExperimentationUpdateRule0(RuntimeAgent agent, RLRule rule, ConditionDef evaluationFunction, String lag)
	        throws GSimEngineException {

        String ownerRule = createRuleIdentifier(evaluationFunction);

        String role = BuildingUtils.getDefiningRoleForRLRule(agent, createRuleIdentifier(rule));

        String n = "(defrule update-rule_" + ownerRule + "\n" + " (declare (salience +22199))\n" + " (parameter (name \"executing-role\") (value "
                + "\"" + role + "\"))\n" + " (timer (time ?t))\n"
                + " ?fact <- (rl-action-node (action-name ?n) (arg $?param) (value ?old) (state-fact-name ?sfn) (count ?times) " + " (function \""
                + evaluationFunction.getParameterName() + "\")" + " (updateCount ?up) (count ?c&:(< ?up ?c) ) ";

        if (Utils.isNumerical(lag) && Double.valueOf(lag) == 0) {
            n += " (time ?x&:(= ?x ?t)   )) \n";
        } else {
            n += " (time ?x&:(and (> ?x -1) (< ?x (- ?t (mod ?t " + lag + ") ))) )) \n";
        }

        n += " (not (modified ?n ?param ?sfn))\n";

		if (BuildingUtils.referencesChildFrame(agent.getDefinition(), evaluationFunction.getParameterName())) {
			String fnc = createLHS(agent, rule, evaluationFunction.getParameterName());
            n += fnc + "\n";
        } else {
            n += " (parameter (name \"" + evaluationFunction.getParameterName() + "\") (value ?currentReward))\n";
        }

        if (rule.isComparison()) {
            n += " (average-reward (function \"" + evaluationFunction.getParameterName() + "\")  (value ?avgR) )\n";
        }

        // if (rule.hasExpansions()) {
        n += " ?state-description <- (state-fact (name ?sfn) (count ?state-count) (value ?old-value) )\n";
        // }

        n += "  =>\n";
        // n+=" (printout t VVVV___(- ?t ?x)___ VVVV_(= 1 (- ?t
        // ?x))__VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV)\n";

        double discount = rule.getDiscount();

        if (rule.isAveraging()) {
            n += " (bind ?reward (* ?old " + discount + "))\n";
            n += " (bind ?reward (+(*(/ 1 ?times) ?currentReward) (*(/ ?up ?times) ?old)))\n";
        } else if (rule.isComparison()) {
            n += " (bind ?reward (+ ?old (* " + discount/* rule.getAvgBeta() */
                    + " (- ?currentReward ?avgR))) )\n";
        } else {
            n += " (bind ?reward (+ ?old (* " + discount + " (- ?currentReward ?old))))\n";
        }

        n += " (modify ?fact (alpha " + evaluationFunction.getParameterValue() + ") (updateCount ?c) (value ?reward) (time (+ ?x 0)))\n";
        // + ") (updateCount (+ ?up 1)) (value ?reward) (time (+ ?x 0)))\n";

        // if (rule.hasExpansions()) {
        n += " (bind ?new-value (+ ?old-value (* 0.5 (- ?currentReward ?old-value))))\n";
        n += " (bind ?sc (+ ?state-count 1) )\n";
        //n+=" (printout t :: ?sfn : ?t : ?state-count :  ?new-value)";
        n += " (modify ?state-description (value ?new-value) (last-activation ?t) (count (+ ?state-count 1)) )\n";
        // }

        n += " (assert (modified ?n ?param ?sfn)))\n";

        return n;

    }

	private static String buildExperimentationUpdateRule1(RuntimeAgent agent, RLRule rule, ConditionDef evaluationFunction, String span) {

        String ownerRule = createRuleIdentifier(evaluationFunction);

        String role = BuildingUtils.getDefiningRoleForRLRule(agent, createRuleIdentifier(rule));

        String n = "(defrule update-rule_" + ownerRule + "\n" + " (declare (salience +22199))\n" + " (parameter (name \"executing-role\") (value "
                + "\"" + role + "\"))\n" + " (timer (time ?currentTime))\n"
                + " ?fact <- (rl-action-node (action-name ?n) (arg $?param) (value ?old) (state-fact-name ?sfn) (count ?times) " + " (function \""
                + evaluationFunction.getParameterName() + "\")" + " (updateCount ?up) (count ?c&:(< ?up ?c) ) ";

        n += " (time ?executionTime&:(and (> ?executionTime -1) " + " (= 0 (mod ?currentTime " + span + " )) (<= (- ?currentTime ?executionTime) "
                + span + "  ) ) )) \n";

        n += " (not (modified ?n ?param ?sfn))\n";

        n += " (parameter (name \"" + evaluationFunction.getParameterName() + "\") (value ?currentReward))\n";

        if (rule.isComparison()) {
            n += " (average-reward (function \"" + evaluationFunction.getParameterName() + "\")  (value ?avgR) )\n";
        }

        // if (rule.hasExpansions()) {
        n += " ?state-description <- (state-fact (name ?sfn) (count ?state-count) (value ?old-value) )\n";
        // }

        n += "  =>\n";

        double discount = rule.getDiscount();

        if (rule.isAveraging()) {
            n += " (bind ?reward (* ?old " + discount + "))\n";
            n += " (bind ?reward (+(*(/ 1 ?times) ?currentReward) (*(/ ?up ?times) ?old)))\n";
        } else if (rule.isComparison()) {
            n += " (bind ?reward (+ ?old (* " + discount/* rule.getAvgBeta() */
                    + " (- ?currentReward ?avgR))) )\n";
        } else {
            n += " (bind ?reward (+ ?old (* " + discount + " (- ?currentReward ?old))))\n";
        }

        n += " (modify ?fact (alpha " + evaluationFunction.getParameterValue()
                + ") (updateCount ?times) (value ?reward) (time (+ ?executionTime 0)))\n";

        // if (rule.hasExpansions()) {
        n += " (bind ?new-value (+ ?old-value (* 0.5 (- ?currentReward ?old-value))))\n";
        n += " (modify ?state-description (value ?new-value) (last-activation ?t) (count (+ ?state-count 1)) )\n";
        // }

        n += " (assert (modified ?n ?param ?sfn)))\n";

        return n;

    }

	private static String createConditions(RuntimeAgent agent, RLRule rule, Object2JessVariableBindingTable objRefs)
	        throws GSimEngineException {
        StringBuffer result = new StringBuffer();
        for (ConditionDef condition : rule.getConditions()) {
            String sofar = result.toString();
			result.append(ConditionBuilder.createCondition(agent, condition, objRefs, sofar));
        }

        return result.toString();
    }

	private static String createLHS(RuntimeAgent agent, RLRule rule, String paramName) throws GSimEngineException {

		Object2JessVariableBindingTable objRefs = new Object2JessVariableBindingTable(agent);
        objRefs.build(rule);

        int variableIdx = Uniform.staticNextIntFromTo(0, 1000);

		String list = BuildingUtils.resolveList(paramName);// conditionBuilder.resolveList(paramName);
		String object = BuildingUtils.resolveChildFrameWithList(agent.getDefinition(), paramName);// conditionBuilder.resolveObjectClass(paramName);
		String att = BuildingUtils.extractChildAttributePathWithoutParent(agent.getDefinition(), paramName);// conditionBuilder.resolveAttribute(paramName);

        String binding = objRefs.getBinding(object);
        if (binding == null) {
            binding = "?varbinding" + String.valueOf(variableIdx + 299);
        }

        // String s0 = " (object-parameter (object-class \"" + object + "\") (instance-name " + binding +"))\n";
        String s0 = " (object-parameter (object-class \"" + object + "\") (instance-name " + binding;
        s0 += "&:(eq " + "(str-cat \"" + list + "/\" " + binding + ")" + " (nth$ 1 ?param ))) ) \n ";
        String s1 = " (parameter (name ?x" + (variableIdx + 200) + "&:(eq ?x" + (variableIdx + 200) + " (str-cat \"" + list + "/\" " + binding
                + " \"/" + att + "\"))) (value ?currentReward)) ";

        return s0 + s1;
    }

	private static String createListActionQuery(String ruleIdentifier, ActionDef[] a, ConditionDef funct) {
        String query1 = "(defquery list-actions_" + ruleIdentifier + "\n";
        query1 += " (declare (variables ?ev))\n";
        String ss2 = "	";
        if (a.length > 1) {
            ss2 = " (rl-action-node (action-name ?n&:(or \n";
            for (int i = 0; i < a.length; i++) {
                ss2 += "  (eq ?n \"" + a[i].getClassName() + "\")";
            }
            ss2 += "))";
        } else if (a.length == 1) {
            ss2 = " (rl-action-node (action-name ?n&:";
            ss2 += "  (eq ?n \"" + a[0].getClassName() + "\")) ";
        }
        ss2 += " (state-fact-name ?ev)";
        ss2 += " (function \"" + funct.getParameterName() + "\")";
        ss2 += " (time ?tt)) ";

        query1 += ss2;
        query1 += ")\n";

        return query1;
    }

	private static String createRuleIdentifier(Instance inst) {
        String x = inst.getName();
        x = x.replace(':', '_');
        x = x.replace(' ', '_');
        x = x.replace('/', '_');
        x = x.replace(',', '_');
        x = x.replace(')', ']');
        x = x.replace('(', '[');
        return x;
    }

	private static String createSelectBestActionFunction(String ruleIdentifier, FUNCTION f) {
        String func3 = "(deffunction selectBestAction_" + ruleIdentifier + " (?ev)\n";
        func3 += " (bind ?it (run-query list-actions_" + ruleIdentifier + " ?ev))\n";
        func3 += " (bind ?action (simplesoftmax-action-selector ?it))\n";
        func3 += " (return ?action))\n";

        return func3;
    }

	private static String[] getPointingNodes(RuntimeAgent agent, RLRule to) {
        ArrayList<String> list = new ArrayList<String>();
        for (RLRule r : agent.getBehaviour().getRLRules()) {
            if (r.getAttribute("equivalent-actionset") != null) {
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

    private enum FUNCTION {
        COMPARISON, SIMPLE_SOFTMAX
    }

}
