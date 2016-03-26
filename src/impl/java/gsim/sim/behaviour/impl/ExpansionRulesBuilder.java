package gsim.sim.behaviour.impl;

import de.s2.gsim.sim.GSimEngineException;

public class ExpansionRulesBuilder {

    private String role = "";
    private String rtContext;

    public String build(int expandInterval, double revisitCostFraction, double revaluationProbability, String role) throws GSimEngineException {

        this.role = role;
        rtContext = "(parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n";

        String s = createStateContractionRule(expandInterval) + "\n";
        s += createStateExpansionRuleFirstBest(expandInterval, revisitCostFraction) + "\n";
        s += createStateBacktrackUpdateRule() + "\n";
        s += createTreeUpdateBackwardChain() + "\n";
        s += createStateHelpFunctions() + "\n";
        s += createBranchSelectionRules(revaluationProbability) + "\n";

        return s;
    }

    public String createStateContractionRule(int expandInterval) throws GSimEngineException {

        String n = "";
        // if the parent-state value >= either children values (from previous
        // experience), delete children and
        // mark the parent node as expanded-finished.

        n += "\n(defrule contract_rule-1-" + role + "\n";
        n += " (declare (salience 100))\n";
        n += " (parameter (name \"exec-sc\"))\n";
        n += " " + rtContext;
        n += " (timer (time ?t&:(=(mod ?t " + expandInterval + ") " + (expandInterval - (int) ((expandInterval / 4d))) + ")))\n";
        n += " (not (contracting ?))\n";
        n += " ?fact <- (state-fact (name ?sfn) (context \"" + role
                + "\")(value ?selectedStateValue)  (parent ?p&:(neq ?p nil)) (count ?selectedStateCount) (active ?a) )\n";
        n += " (test (= 0 (child-count-" + role + " ?sfn)))\n";
        n += " ?parent <- (state-fact (name ?p) (value ?parentStateValue) )\n";
        n += " ?sibling <- (state-fact (name ?sibling-name&:(neq ?sibling-name ?sfn)) (parent ?p) (value ?siblingStateValue) (count ?siblingStateCount) (active ?b) )\n";
        n += " (test (= 0 (child-count-" + role + " ?sibling-name)))\n";
        n += " (test (>= (+ ?parentStateValue (* ?parentStateValue 0.01)) "
                + " (/ (+ (* ?siblingStateValue ?siblingStateCount) (* ?selectedStateValue ?selectedStateCount) ) ( + ?selectedStateCount ?siblingStateCount) ) )  )\n";
        n += " =>\n";
        n += " (if (or (= ?a 1.0) (= ?b 1.0)) then (modify ?parent (active 1.0)) ) \n";
        n += " (assert (contracting ?sfn)))\n";

        return n;

    }

    String createRetractFollowRules() {
        // retract connected state-elems. you have to retract actions separately
        // because there are ususally more actoins than state-elems

        // retract connected actions
        String n4a = "(defrule updateStateAction \n";
        n4a += " (state-retracted ?sn)\n";
        n4a += " ?fact0 <- (rl-action-node (state-fact-name ?sn))\n";
        n4a += " =>\n";
        n4a += " (retract ?fact0)) \n";

        // The retract of a child-state leads to a situation where its parent
        // states has no actual or potential children left at all anymore
        // (because th other children have already been expanded and retracted
        // again).
        // --> this is ok, it is only relevant for expansion (ignore where
        // leaf=m)
        // --> the state may stay, or become removed later if it is not good
        // enough
        // --> So, only retract the current-state and update the parent!
        String n5 = "(defrule updateLeaf_NextState \n";
        n5 += " (not (contracted3))";
        n5 += " (child-of-state-retracted ?n)\n";
        n5 += " ?fact <- (state-fact (name ?n) (parent ?sn) )\n";
        n5 += " =>\n";
        n5 += " (assert (contracted3))";
        n5 += " (modify ?fact (leaf (+ ?pl 1.0)))) \n";

        return n4a + "\n" + n5 + "\n";

    }

    String createStateDescriptionQueries() {
        String query1 = "(defquery list-states\n";
        query1 += " (declare (variables ?ctx))\n";
        query1 += " (state-fact (context ?ctx)) ) \n";

        String query2 = "(defquery list-states-h\n";
        query2 += " (declare (variables ?h ?ctx))\n";
        query2 += " (state-fact (depth ?hh&:(= ?h ?hh)) (context ?ctx) )) \n";

        String query3 = "(defquery list-state-elems \n";
        query3 += " (declare (variables ?sfn))\n";
        query3 += " (state-fact-element (state-fact-name ?sfn) )) \n";

        String query4 = "\n(defquery list-state-categories \n";
        query4 += " (declare (variables ?sfn))\n";
        query4 += " (state-fact-category (state-fact-name ?sfn) )) \n";

        return query1 + "\n" + query2 + "\n" + query3 + "\n" + query4;

    }

    private String createBranchSelectionRules(double probability) throws GSimEngineException {

        String n1 = "(defrule select-new-expansion-root-" + role + "\n";
        n1 += " (declare (salience 101))\n";
        n1 += " (timer (time ?t))\n";
        n1 += " (not (new-root))\n";
        n1 += " (not (contracting ?))\n";
        n1 += " ?fact <- (state-fact (name ?sfn) (value ?v&:(= ?v (max-value-overall-" + role + " ?t))) (leaf ?lf) ) (context \"" + role + "\")\n";
        n1 += " (test (> (* (+ (count-query-results list-state-elems ?sfn) (count-query-results list-state-elems ?sfn))  2)  (+ (child-count-" + role
                + " ?sfn) ?lf))  )\n";
        n1 += " (test (< (call cern.jet.random.Uniform staticNextDoubleFromTo 0.0 1.0 )" + probability + "))\n";
        n1 += " =>\n";
        // n1+=" (printout t ----------- (call cern.jet.random.Uniform staticNextDoubleFromTo 0.0 1.0 ) --createBranchSelectionRules salience 101 must
        // be last -------)";
        n1 += " (modify ?fact (active 1.0))\n";
        n1 += " (assert (new-root)) \n";
        n1 += " (assert (selected ?sfn))) \n";

        n1 += "\n(defrule unselect-old-branch-" + role + "\n";
        n1 += " (declare (salience 101))\n";
        n1 += " (exists (selected ?)) \n";
        n1 += " ?fact <- (state-fact (name ?sfn) (active 1.0) (context \"" + role + "\")) \n";
        n1 += " (not (selected ?sfn))\n";
        n1 += " (not (unselected ?sfn))\n";
        n1 += " =>\n";
        n1 += " (modify ?fact (active 0.0))\n";
        n1 += " (assert (unselected ?sfn))) \n";

        n1 += "\n(defrule forward-new-selection-" + role + " \n";
        n1 += " (declare (salience 101))\n";
        n1 += " (not (selected ?sfn))\n";
        n1 += " (selected ?p) \n";
        n1 += " ?fact <- (state-fact (name ?sfn) (parent ?p) (active 0.0) (context \"" + role + "\")) \n";
        n1 += " =>\n";
        // n1+=" (printout t -------------createBranchSelectionRules_3 salience 101 must be last ------)";
        n1 += " (modify ?fact (active 1.0))\n";
        n1 += " (assert (selected ?sfn))) \n";

        return n1;
    }

    private String createStateBacktrackUpdateRule() {

        String n = "";

        n = "(defrule state-update-rule-" + role + "\n";
        n += " (declare (salience 101))\n";
        n += rtContext;
        // n += " (parameter (name \"exec-update-rewards\"))\n";
        n += " (timer (time ?t))\n";
        n += " ?m <- (modified ?action ?param ?sfn) \n";
        n += " (state-fact (name ?sfn) (parent ?p) (context \"" + role + "\") )\n";
        n += " (not (modified ?n ?param ?p))\n";
        n += " ?state-description <- (state-fact (name ?p) (value ?old-value) (count ?state-count))\n";
        n += " ?fact <- (rl-action-node (action-name ?an) (function ?func) (time ?t) (count ?c) (updateCount ?uc&:(= ?uc ?c)))\n ";
        n += " (parameter (name ?func) (value ?currentReward))\n";
        n += "  =>\n";
        n += " (modify ?state-description (count (+ ?state-count 1)) (last-activation ?t) )\n";
        // n += " (printout t ----- createStateBacktrackUpdateRule salience 101 must be last--------)\n";
        n += " (assert (modified ?an ?param ?p)))\n";

        return n;

    }

    private String createStateExpansionRuleFirstBest(int interval, double costfraction) throws GSimEngineException {
        String n1 = "";

        n1 += "(defrule expand_FIRST_BEST-" + role + "\n";
        n1 += " (timer (time ?t&:(=(mod ?t " + interval + ") 0)))\n";
        n1 += rtContext;
        n1 += " (parameter (name \"exec-sc\"))\n";
        n1 += " (not (parameter (name \"exec-RLRule\")))\n";
        n1 += " (not (expanded)) \n";
        n1 += " (test (< (count-query-results list-states \"" + role + "\") (+ 0 ?*max-node-count*))) \n";
        n1 += " ?fact0 <- (state-fact (name ?sfn) (parent ?p) (depth ?h) (leaf ?lf) (value ?v)  (count ?act) (expansion-count ?epc) ) \n";
        n1 += " (test (= ?h (depth-" + role + ")) ) \n";
        n1 += " (exists (rl-action-node (state-fact-name ?sfn))) \n";
        n1 += " (average-reward (value ?avg)) \n";
        n1 += " (test (or (< (* ?epc (* " + costfraction + " ?avg)) ?v) (= ?epc 0)) )\n";
        n1 += " (test (> (* ?*state-elem-count* 2)  (+ (child-count-" + role + " ?sfn) 0) )  )\n";
        n1 += " (test (= (* ?v (/ ?act ?t)) (max-value-" + role + " ?h ?t)) )\n";
        n1 += " =>\n";
        n1 += "  (expand ?fact0) \n";
        n1 += "  (bind ?c (child-count-" + role + " ?sfn)) \n";
        n1 += "  (assert (expanded))  )\n";
        n1 += "\n(defrule expand_FIRST_BEST_ROOT-" + role + "\n";
        n1 += " (timer (time ?t&:(=(mod ?t " + interval + ") 0)))\n";
        n1 += rtContext;
        n1 += " (parameter (name \"exec-sc\"))\n";
        n1 += " (not (parameter (name \"exec-RLRule\")))\n";
        n1 += " (not (expanded)) \n";
        n1 += " (test (< (count-query-results list-states " + role + ") (+ 0 ?*max-node-count*))) \n";
        n1 += " ?fact0 <- (state-fact (name ?sfn) (parent nil) (depth ?h) (leaf ?lf) (value ?v) (count ?act) (expansion-count ?epc)) \n";
        n1 += " (test (= ?h (depth-" + role + ")) ) \n";
        n1 += " (exists (rl-action-node (state-fact-name ?sfn))) \n";
        n1 += " (test (or (< (* ?epc (* " + costfraction + " ?v)) ?v) (= ?epc 0)) )\n";
        n1 += " (test (= (* ?v (/ ?act ?t)) (max-value-" + role + " ?h ?t)) )\n";

        n1 += " =>\n";
        n1 += " (expand ?fact0) \n";
        n1 += " (bind ?c (child-count-" + role + " ?sfn)) \n";
        n1 += " (assert (expanded) ))\n";

        return n1;
    }

    private String createStateHelpFunctions() {

        String f0 = "\n(deffunction count-all-elems-" + role + " (?sfn) \n";
        f0 += " (return (+ (count-query-results list-state-categories ?sfn) (count-query-results list-state-elems ?sfn))))\n";

        String f1 = "(deffunction depth-" + role + "() \n";
        f1 += " (bind ?d 0)\n";
        f1 += " (bind ?it (run-query list-states \"" + role + "\"))\n";
        f1 += " (while (?it hasNext) \n";
        f1 += "  (bind ?token (call ?it next))\n";
        f1 += "  (bind ?state (call ?token fact 1))\n";
        f1 += "  (bind ?c (fact-slot-value ?state depth))\n";
        f1 += "  (bind ?a (fact-slot-value ?state active))\n";
        f1 += "  (if (and (> ?c ?d) (= ?a 1.0)) then (bind ?d ?c) ) )\n";
        f1 += " (return ?d))\n";

        String f4 = "(deffunction max-value-" + role + "(?h ?t) \n";
        f4 += " (bind ?n -1)\n";
        f4 += " (bind ?it (run-query list-states-h ?h \"" + role + "\"))\n";
        f4 += " (while (?it hasNext) \n";
        f4 += "  (bind ?token (call ?it next))\n";
        f4 += "  (bind ?state (call ?token fact 1))\n";
        f4 += "  (bind ?sfn (fact-slot-value ?state name))\n";
        f4 += "  (bind ?c (fact-slot-value ?state value))\n";
        f4 += "  (bind ?act (fact-slot-value ?state count))\n";
        f4 += "  (bind ?lf (fact-slot-value ?state leaf))\n";
        f4 += "  (bind ?v (* ?c (/ ?act ?t))) \n";
        f4 += "  (if (and (> (count-all-elems-" + role + " ?sfn)  1) (> ?v ?n)) then (bind ?n ?v)) )\n";
        f4 += " (return ?n)) \n";

        f4 += "\n(deffunction max-value-overall-" + role + "(?t) \n";
        f4 += " (bind ?n -1)\n";
        f4 += " (bind ?it (run-query list-states \"" + role + "\"))\n";
        f4 += " (while (?it hasNext) \n";
        f4 += "  (bind ?token (call ?it next))\n";
        f4 += "  (bind ?state (call ?token fact 1))\n";
        f4 += "  (bind ?c (fact-slot-value ?state value))\n";
        f4 += "  (bind ?sfn (fact-slot-value ?state name))\n";
        f4 += "  (bind ?act (fact-slot-value ?state count))\n";
        f4 += "  (bind ?lf0 (fact-slot-value ?state leaf))\n";
        f4 += "  (bind ?v (* ?c (/ ?act ?t))) \n";
        f4 += "  (if (and (> (* (count-query-results list-state-elems ?sfn) 2)  (+ (child-count-" + role
                + " ?sfn) ?lf0)) (> ?v ?n)) then (bind ?n ?v)) )\n";
        f4 += " (return ?n)) \n";

        String f5 = "(deffunction child-count-" + role + "(?sfn) \n";
        f5 += " (bind ?n 0)\n";
        f5 += " (bind ?it (run-query list-states \"" + role + "\"))\n";
        f5 += " (while (?it hasNext) \n";
        f5 += "  (bind ?token (call ?it next))\n";
        f5 += "  (bind ?state (call ?token fact 1))\n";
        f5 += "  (bind ?parent (fact-slot-value ?state parent))\n";
        f5 += "  (if (eq ?parent ?sfn) then (bind ?n (+ ?n 1)) ) )\n";
        f5 += " (return ?n)) \n";

        // String f6 = "(deffunction is-minimal (?sf) \n";
        // f6 += " (bind ?it (run-query list-states \""+this.role+"\"))\n";
        // f6 += " (while (?it hasNext) \n";
        // f6 += " (bind ?token (call ?it next))\n";
        // f6 += " (bind ?state (call ?token fact 1))\n";
        // f6 += " (bind ?sfn (fact-slot-value ?state name))\n";
        // f6 += " (bind ?x (count-query-results list-state-categories ?sf))";
        // f6 += " (if (and (neq ?sfn ?sf) (< (count-query-results list-state-elems ?sfn) ?x) ) then (return false)) )\n";
        // f6 += " (return true))\n";

        // return query1 + "\n" + query2 + "\n" + query3 + "\n" + query4 + "\n" + query5 + "\n" + f1 + "\n" + f4 + "\n" + f5 + "\n" + f6 + "\n";
        return f0 + "\n" + f1 + "\n" + f4 + "\n" + f5 + "\n";

    }

    private String createTreeUpdateBackwardChain() throws GSimEngineException {

        // if one element gets contracted (rule 1), the respective state-fact
        // and the sibling (complement) of this state has to be retracted.
        String n3 = "(defrule updateLeafInitial_NextState-retract_Current_state-" + role + " \n";
        n3 += " (declare (salience 100))\n";
        n3 += " (contracting ?sfn)\n";
        n3 += rtContext;
        n3 += " (parameter (name \"exec-sc\"))\n";
        n3 += " ?state <- (state-fact (name ?sfn) (context \"" + role + "\") (parent ?sn&:(neq ?sn nil)) (depth ?h) (rule ?ruleName1))\n";
        n3 += " ?sibling <- (state-fact (name ?sibling-name&:(neq ?sibling-name ?sfn)) (parent ?sn) (rule ?ruleName2))\n";
        n3 += " ?parent <- (state-fact (name ?sn) (leaf ?pl) )\n";
        n3 += " =>\n";
        n3 += " (retract ?state) \n";
        n3 += " (retract ?sibling) \n";
        n3 += " (undefrule ?ruleName1) \n";
        n3 += " (undefrule ?ruleName2) \n";
        n3 += " (printout t ---createTreeUpdateBackwardChain must be first ... salience=100--------- ?sfn ----childcount1= (child-count-" + role
                + " ?sfn)--- sibling ?sibling-name --childcount2= (child-count-" + role + " ?sibling-name) -------- ?parent crlf)";
        n3 += " (modify ?parent (leaf (+ ?pl 2.0))) \n";
        n3 += " (assert (state-retracted ?sfn))\n";
        n3 += " (assert (state-retracted ?sibling-name))\n";
        n3 += " (assert (child-of-state-retracted ?sn)) )\n";

        String n4 = "(defrule updateStateConnection_Elems_2 \n";
        n4 += " (state-retracted ?sn)\n";
        n4 += rtContext;
        n4 += " ?fact1 <- (state-fact-element (name ?n) (state-fact-name ?sn))\n";
        n4 += " =>\n";
        n4 += " (retract ?fact1)) \n";
        n4 += "\n(defrule updateStateConnection_Elems_1 \n";
        n4 += " (state-retracted ?sn)\n";
        n4 += " ?fact1 <- (state-fact-category (name ?n) (state-fact-name ?sn))\n";
        n4 += " =>\n";
        n4 += " (retract ?fact1)) \n";

        return n3 + "\n" + n4;

    }

}
