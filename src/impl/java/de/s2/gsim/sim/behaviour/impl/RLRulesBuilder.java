package de.s2.gsim.sim.behaviour.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cern.jet.random.Uniform;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.UserRule;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.Context;
import de.s2.gsim.sim.behaviour.SimAction;
import de.s2.gsim.util.Utils;

public class RLRulesBuilder {

    private static int someCounter = 0;

    private RuntimeAgent agent;

    private ConditionBuilder conditionBuilder = null;

    private GeneralRLBuilder general = null;

    public RLRulesBuilder(RuntimeAgent a) {
        agent = a;
        conditionBuilder = new ConditionBuilder();
        general = new GeneralRLBuilder(a, conditionBuilder);
    }

    String buildAvgRule(RLRule rule, ConditionDef evaluationFunction) {

        String ownerRule = createRuleIdentifier(evaluationFunction);

        String role = ParsingUtils.getDefiningRoleForRLRule(agent, createRuleIdentifier(rule));

        String n = "(defrule update-global-average-rule_" + ownerRule + "\n" + " (declare (salience +222199))\n"
                + " (parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n"
                // + " (parameter (name \"exec-update-rewards\"))\n"
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

    String buildExecutionFunction(String ns) {

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

    String buildExperimentationUpdateRule(RLRule rule, ConditionDef evaluationFunction) throws GSimEngineException {
        String span = rule.getUpdateSpan();
        if (span != null) {
            return buildExperimentationUpdateRule1(rule, evaluationFunction, span);
        } else {
            String lag = rule.getUpdateLag();
            if (lag == null) {
                lag = "1";
            }
            return buildExperimentationUpdateRule0(rule, evaluationFunction, lag);
        }
    }

    String buildIntermediateRule(RLRule rule) {

        String nRule = "";

        try {
            String ownerRule = createRuleIdentifier(rule);
            String ruleName = "intermediate_rule_" + ownerRule;

            nRule = "(defrule " + ruleName + "\n";
            nRule += " (declare (salience +4999))\n";
            nRule += " (parameter (name \"exec-RLRule\"))\n";
            nRule += " (parameter (name \"executing-role\") (value " + "\"" + ParsingUtils.getDefiningRoleForRLRule(agent, ownerRule) + "\"))\n";
            // nRule += " (parameter (name \"exec-interval\") (value ?exc&:(= 0 (mod
            // ?*current-time* ?exc))))\n";

            // >>>>>>>>>>>>>>>>>>BG>>>>>>>>>>>>>>>>>//
            // removed
            // nRule += " (not (experimented-" + ownerRule + "))\n";

            Object2VariableBindingTable map = new Object2VariableBindingTable();
            map.build(rule);

            nRule += createConditions(rule, map);

            nRule += " ?time <-(timer (time ?n))\n";

            String[] pointers = getPointingNodes(rule);
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

    String createRLHelpRuleSetOnly(RLRule rule) throws GSimEngineException {
        String[] rules = buildExperimentationTerminalRules(rule, true);

        String res = "";
        for (int i = 0; i < rules.length; i++) {
            res += rules[i];
        }

        res += addShortCuts(rule, res) + "\n";

        return res;
    }

    String createRLRuleSet(RLRule rule) throws GSimEngineException {

        String res = addShortCuts(rule, "");
        String[] rules = new String[0];
        if (res.length() > 0) {
            rules = buildExperimentationTerminalRules(rule, true);
            res += "\n";
        } else {
           rules = buildExperimentationTerminalRules(rule, false);
        }

        // String res = "";
        for (int i = 0; i < rules.length; i++) {
            res += rules[i];
        }

        // res += this.addShortCuts(rule, res) + "\n";

        return res;
    }

    private String addShortCuts(RLRule c, String nRule_1) throws GSimEngineException {

        String res = "";
        String stateName = c.getName() + "_00";// initial state-name=rule-name [of
        // the
        // original rule]

        Object2VariableBindingTable objRefs = new Object2VariableBindingTable();
        objRefs.build(c);

        String condStr = createConditions(c, objRefs);

        UserRule[] shortcuts = c.getShortSelectionRules();
        for (int i = 0; i < shortcuts.length; i++) {
            res += general.createShortcut(c, shortcuts[i], stateName, condStr, objRefs);
        }

        if (shortcuts.length > 0) {
            res += "\n" + createShortcutSelectionRule(c);
        }
        return res;
    }

    private String buildExperimentationFunctions(String ruleIdentifier, ActionDef[] a, FUNCTION f, ConditionDef funct) {

        String s1 = createListActionQuery(ruleIdentifier, a, funct);

        String s2 = createSelectBestActionFunction(ruleIdentifier, f);

        return s1 + "\n" + s2;

    }

    private String[] buildExperimentationTerminalRules(RLRule rule, boolean helpersOnly) throws GSimEngineException {

        someCounter++;

        Set<String> singleRules = new HashSet<String>();

        String expRule = "";
        Object2VariableBindingTable objRefs = new Object2VariableBindingTable();
        objRefs.build(rule);

        String initialStateName = rule.getName() + "_0" + "0";

        if (!helpersOnly) {
            String condStr = createConditions(rule, objRefs);
            expRule += general.buildExperimentationRule(rule, initialStateName, condStr) + "\n";
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

    private String buildExperimentationUpdateRule0(RLRule rule, ConditionDef evaluationFunction, String lag) throws GSimEngineException {

        String ownerRule = createRuleIdentifier(evaluationFunction);

        String role = ParsingUtils.getDefiningRoleForRLRule(agent, createRuleIdentifier(rule));

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

        if (evaluationFunction.getParameterName().contains("::")) {
            String fnc = createLHS(rule, evaluationFunction.getParameterName());
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
        n += " (modify ?state-description (value ?new-value) (last-activation ?t) (count (+ ?state-count 1)) )\n";
        // }

        n += " (assert (modified ?n ?param ?sfn)))\n";

        return n;

    }

    private String buildExperimentationUpdateRule1(RLRule rule, ConditionDef evaluationFunction, String span) {

        String ownerRule = createRuleIdentifier(evaluationFunction);

        String role = ParsingUtils.getDefiningRoleForRLRule(agent, createRuleIdentifier(rule));

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

    private String createConditions(RLRule rule, Object2VariableBindingTable objRefs) throws GSimEngineException {
        StringBuffer result = new StringBuffer();
        for (ConditionDef condition : rule.getConditions()) {
            String sofar = result.toString();
            result.append(conditionBuilder.createCondition(agent, condition, objRefs, sofar));
        }

        return result.toString();
    }

    private String createLHS(RLRule rule, String paramName) throws GSimEngineException {

        Object2VariableBindingTable objRefs = new Object2VariableBindingTable();
        objRefs.build(rule);

        int variableIdx = Uniform.staticNextIntFromTo(0, 1000);

        String list = conditionBuilder.resolveList(paramName);
        String object = conditionBuilder.resolveObjectClass(paramName);
        String att = conditionBuilder.resolveAttribute(paramName);

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

    private String createListActionQuery(String ruleIdentifier, ActionDef[] a, ConditionDef funct) {
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

    private String createRuleIdentifier(Instance inst) {
        String x = inst.getName();
        x = x.replace(':', '_');
        x = x.replace(' ', '_');
        x = x.replace('/', '_');
        x = x.replace(',', '_');
        x = x.replace(')', ']');
        x = x.replace('(', '[');
        return x;
    }

    private String createSelectBestActionFunction(String ruleIdentifier, FUNCTION f) {
        String func3 = "(deffunction selectBestAction_" + ruleIdentifier + " (?ev)\n";
        func3 += " (bind ?it (run-query list-actions_" + ruleIdentifier + " ?ev))\n";
        func3 += " (bind ?action (simplesoftmax-action-selector ?it))\n";
        func3 += " (return ?action))\n";

        return func3;
    }

    private String createShortcutSelectionRule(RLRule r) throws GSimEngineException {
        String ident = createRuleIdentifier(r);
        String role = ParsingUtils.getDefiningRoleForRLRule(agent, r.getName());

        String s = "(defrule " + ident + "_sc_select\n";
        s += " (declare (salience 199999999))\n";
        s += " (parameter (name \"exec-RLRule\"))\n";
        s += " (parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n";
        // s += " (parameter (name \"exec-interval\") (value ?exc&:(= 0 (mod
        // ?*current-time* ?exc))))\n";
        s += " (timer (time ?t))\n";
        s += " (list (name \"" + ident + "\") (obj ?list))\n";
        s += " =>\n";
        // s+= "(printout t ======================(call ?list size) crlf)\n";
        s += " (bind ?it (call ?list iterator))\n";
        s += " (bind ?chosen (simplesoftmax-action-selector ?it))\n";
        s += " (call ?list clear)\n";
        s += " (if (neq ?chosen NIL) then\n";
        s += " (execute ?chosen \"" + role + "\")\n";
        s += "  (bind ?c (fact-slot-value ?chosen count))\n";
        s += "  (modify (call ?chosen getFactId) (count (+ ?c 1)) (time ?t))\n";
        s += "  (assert (experimented-" + ident + "))))\n";

        return s;

    }

    private String[] getPointingNodes(RLRule to) {
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
