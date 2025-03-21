package de.s2.gsim.sim.behaviour.rulebuilder;

import de.s2.gsim.sim.GSimEngineException;

public abstract class ExpansionRulesBuilder {

	private ExpansionRulesBuilder() {
		// static class
	}

	public static String build(int expandInterval, int contractInterval, double revisitCostFraction, double revaluationProbability,
	        String role)
	        throws GSimEngineException {

		String rtContext = "(parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n";

		String s = createStateContractionRule(contractInterval, rtContext, role) + "\n";
		s += createStateExpansionRuleFirstBest(expandInterval, revisitCostFraction, rtContext, role) + "\n";
		s += createStateBacktrackUpdateRule(rtContext, role) + "\n";
		s += createTreeUpdateBackwardChain(rtContext, role) + "\n";
		s += createStateHelpFunctions(role) + "\n";
		s += createBranchSelectionRules(revaluationProbability, role) + "\n";

        return s;
    }

	/**
	 * The fact 'contracting' ensures that only one leaf pair is deleted at a time. Although larger subtrees deletion seems plausible, it is
	 * not done because the agent must have some cycles to dermine whether the generalised rule applies. So the deletion can be only one
	 * state-pair by the next; otherwise too many descriptors may be deleted!
	 * 
	 * @param contractInterval
	 * @param rtContext
	 * @param role
	 * @return
	 * @throws GSimEngineException
	 */
	private static String createStateContractionRule(int contractInterval, String rtContext, String role) throws GSimEngineException {

        String n = "";
        n += "\n(defrule contract_rule-1-" + role + "\n";
        n += " (declare (salience 100))\n";
        n += " (parameter (name \"exec-sc\"))\n";
        n += " " + rtContext;
		n += " (not (contracting ?))\n";
		n += " (timer (time ?t&:(=(mod ?t " + contractInterval + ") 0)))\n";

		n += " ?fact <- (state-fact (name ?sfn) (context \"" + role
		        + "\")(value ?selectedStateValue)  (last-activation ?la) (parent ?p&:(neq ?p nil)) (count ?selectedStateCount) (active ?a) )\n";
		n += " (test (= 0 (child-count-" + role + " ?sfn)))\n";
        n += " ?parent <- (state-fact (name ?p) (value ?parentStateValue) )\n";
		n += " ?sibling <- (state-fact (name ?sibling-name&:(neq ?sibling-name ?sfn)) (parent ?p) (value ?siblingStateValue) (count ?siblingStateCount) (active ?b) )\n";
		n += " (test (= 0 (child-count-" + role + " ?sibling-name)))\n";

		// test: value of children, weighted by number of activations, is smaller than parent's value - but why this strange weighting - it
		// cancels itself?
		// n += " (test (or (>= (+ ?parentStateValue (* ?parentStateValue 0.01)) "
		// + " (/ (+ (* ?siblingStateValue ?siblingStateCount) (* ?selectedStateValue ?selectedStateCount) ) ( + ?selectedStateCount
		// ?siblingStateCount) ) ) (= ?la -1)) )\n";

		n += " (test (>= ?parentStateValue (/ (+ ?siblingStateValue ?selectedStateValue ) 2) ) )\n";

		n += " =>\n";
		n += "(printout t ----------------------------- CONTRACT -- ?sfn -- ?parentStateValue -- ?siblingStateValue  ------ ?selectedStateValue  ----  (/ (+ ?siblingStateValue ?selectedStateValue ) 2) -----------)";
		n += " (if (or (= ?a 1.0) (= ?b 1.0)) then (modify ?parent (active 1.0)) ) \n"; // set parent active only if the current level was
		// actually active
        n += " (assert (contracting ?sfn)))\n";

        return n;

    }

	public static String createRetractFollowRules() {
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

	public static String createStateDescriptionQueries() {
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

	private static String createBranchSelectionRules(double probability, String role) throws GSimEngineException {

        String n1 = "(defrule select-new-expansion-root-" + role + "\n";
		n1 += " (declare (salience 101))\n";


		n1 += " (timer (time ?t&:(> ?t 10.0 )  ))\n";

		n1 += " (not (new-root))\n";
		n1 += " (not (contracting ?))\n";

		// n1 += " (test (> (numberp ?t) (numberp 10.0) )) \n";

		//n1 += " ?fact <- (state-fact (name ?sfn) (value ?v) (leaf ?lf) (context \"" + role + "\"))\n";
		n1 += " ?fact <- (state-fact (name ?sfn) (count ?count) (value ?v&:(>= ( / (* ?v ?count) ?t)  (max-value-overall-" + role
		        + " ?t))) (leaf ?lf)  (context \"" + role + "\") )\n";
		 
		n1 += " (test (> (* (+ (count-query-results list-state-elems ?sfn) (count-query-results list-state-elems ?sfn)) 2) (+ (child-count-"+ role + " ?sfn) ?lf)) )\n";

		n1 += " (test (< (call cern.jet.random.Uniform staticNextDoubleFromTo 0.0 1.0 )" + probability + "))\n";

//        n1 += " (test (> (* (+ (count-query-results list-state-elems ?sfn) (count-query-results list-state-elems ?sfn)) 2) (+ (child-count-" + role + " ?sfn) ?lf)) )\n";
//		n1 += " (test (< (call cern.jet.random.Uniform staticNextDoubleFromTo 0.0 1.0 )" + probability + "))\n";

        n1 += " =>\n";
		// n1 += " (printout t -------------SELECT : ?sfn ------)";
		n1 += " (modify ?fact (active 1.0))\n";
		n1 += " (assert (new-root)) \n";
        n1 += " (assert (selected ?sfn))) \n";

        n1 += "\n(defrule unselect-old-branch-" + role + "\n";
		n1 += " (declare (salience 102))\n";
		n1 += " (selected ?selectedFactName) \n"; // function exist at the beginning = no fire?
		n1 += " (state-fact (name ?selectedFactName) (parent ?sp) ) \n";
		n1 += " ?fact <- (state-fact (name ?sfn&:(neq ?sfn ?sp) ) (parent ?p&:(neq ?p nil)) (active 1.0) (context \"" + role + "\")) \n";
		// n1 += " (not (state-fact (name ?selectedFactName) (parent ?pp&:(neq ?pp ?sfn)) (context \"" + role + "\") ) ) \n";
        n1 += " (not (selected ?sfn))\n";
        n1 += " (not (unselected ?sfn))\n";
		n1 += " (not (selectedChild ?sp))\n";
        n1 += " =>\n";
		// n1 += " (printout t -------------UNSELECT: ?sfn - [ ?selectedFactName ] - [ ?sp ] ------)";
        n1 += " (modify ?fact (active 0.0))\n";
        n1 += " (assert (unselected ?sfn))) \n";

        n1 += "\n(defrule forward-new-selection-" + role + " \n";
		n1 += " (declare (salience 103))\n";
        n1 += " (selected ?p) \n";
		n1 += " ?fact <- (state-fact (name ?sfn&:(neq ?sfn ?p)) (parent ?p) (active 0.0) (context \"" + role + "\")) \n";
		// both must be zero, otherwise I'm on the dead end
		n1 += " ?sibling <- (state-fact (name ?sfn2&:(neq ?sfn2 ?sfn)) (parent ?p) (active 0.0) (context \"" + role + "\")) \n";
		// n1 += " (not (unselected ?sfn)) \n";
		n1 += " (not (selectedChild ?sfn))\n";
		n1 += " =>\n";
		// n1 += " (printout t ------------- forward: ?sfn ------)";
		n1 += " (modify ?fact (active 1.0)) \n";
		n1 += " (assert (selectedChild ?sfn)) \n";
		n1 += " (assert (selected ?sfn))) \n";

		n1 += "\n(defrule backward-new-selection-" + role + " \n";
		n1 += " (declare (salience 104))\n";
		n1 += " (selected ?sfn) \n";
		n1 += " (not (unselected ?sfn))\n";
		n1 += " (not (selectedChild ?sfn))\n";
		n1 += " (not (selectedParent ?sfn))\n";
		n1 += " (state-fact (name ?sfn) (parent ?parent)  (context \"" + role + "\")) \n";
		n1 += " ?fact <- (state-fact (name ?parent)  (context \"" + role + "\")) \n";
		n1 += " =>\n";
		// n1 += " (printout t ------------- backward: ?sfn - ?parent ------)";
		n1 += " (modify ?fact (active 1.0)) \n";
		n1 += " (assert (selectedParent ?sfn)) \n";
		n1 += " (assert (selected ?parent))) \n";

        return n1;
    }

	private static String createStateBacktrackUpdateRule(String rtContext, String role) {

        String n = "";

        n = "(defrule state-update-rule-" + role + "\n";
        n += " (declare (salience 101))\n";
		n += " " + rtContext;
        n += " ?m <- (modified ?action ?param ?sfn) \n";
        n += " (state-fact (name ?sfn) (parent ?p) (context \"" + role + "\") )\n";
        n += " (not (modified ?n ?param ?p))\n";
        n += " ?state-description <- (state-fact (name ?p) (value ?old-value) (count ?state-count))\n";
		n += " ?fact <- (rl-action-node (action-name ?an) (function ?func) (time ?t) (count ?c) (updateCount ?uc&:(= ?uc ?c)))\n";
        n += " (parameter (name ?func) (value ?currentReward))\n";
		n += " (timer (time ?t))\n";
        n += "  =>\n";
        n += " (modify ?state-description (count (+ ?state-count 1)) (last-activation ?t) )\n";
		n += " (printout t ----- createStateBacktrackUpdateRule salience 101 must be last--------)\n";
        n += " (assert (modified ?an ?param ?p)))\n";

        return n;

    }

	private static String createStateExpansionRuleFirstBest(int interval, double costfraction, String rtContext, String role)
	        throws GSimEngineException {
        String n1 = "";

		n1 += "(defrule expand_FIRST_BEST-" + role + "\n";
		n1 += " " + rtContext;
        n1 += " (parameter (name \"exec-sc\"))\n";
        n1 += " (not (parameter (name \"exec-RLRule\")))\n";
        n1 += " (not (expanded)) \n";
        n1 += " (test (< (count-query-results list-states \"" + role + "\") (+ 0 ?*max-node-count*))) \n";
		n1 += " (timer (time ?t&:(=(mod ?t " + interval + ") 0)))\n";
        n1 += " ?fact0 <- (state-fact (name ?sfn) (parent ?p) (depth ?h) (leaf ?lf) (value ?v)  (count ?act) (expansion-count ?epc) ) \n";
        n1 += " (test (= ?h (depth-" + role + ")) ) \n";
        n1 += " (exists (rl-action-node (state-fact-name ?sfn))) \n";
        n1 += " (average-reward (value ?avg)) \n";
        n1 += " (test (or (< (* ?epc (* " + costfraction + " ?avg)) ?v) (= ?epc 0)) )\n";
        n1 += " (test (> (* ?*state-elem-count* 2)  (+ (child-count-" + role + " ?sfn) 0) )  )\n";
        n1 += " (test (= (* ?v (/ ?act ?t)) (max-value-" + role + " ?h ?t)) )\n";
        n1 += " =>\n";
        n1 += " (bind ?k (max-value-" + role + " ?h ?t) )\n";
       // n1 += " (printout t --- ?v ::: ?k :: ?h)";
        n1 += "  (expand ?fact0) \n";
        n1 += "  (bind ?c (child-count-" + role + " ?sfn)) \n";
        n1 += "  (assert (expanded))  )\n";
        
        n1 += "\n(defrule expand_FIRST_BEST_ROOT-" + role + "\n";
		n1 += " " + rtContext;
        n1 += " (parameter (name \"exec-sc\"))\n";
        n1 += " (not (parameter (name \"exec-RLRule\")))\n";
        n1 += " (not (expanded)) \n";
        n1 += " (test (< (count-query-results list-states " + role + ") (+ 0 ?*max-node-count*))) \n";
		n1 += " (timer (time ?t&:(=(mod ?t " + interval + ") 0)))\n";
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

	private static String createStateHelpFunctions(String role) {

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
        f4 += "  (if (and (>= (count-all-elems-" + role + " ?sfn)  1) (> ?v ?n)) then (bind ?n ?v)) )\n";
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
		f4 += "  (if (> ?v ?n) then (bind ?n ?v)) )\n";
		// f4 += " (if (and (> (* (count-query-results list-state-elems ?sfn) 2) (+ (child-count-" + role + " ?sfn) ?lf0)) (> ?v ?n)) then
		// (bind ?n ?v)) )\n";
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

        return f0 + "\n" + f1 + "\n" + f4 + "\n" + f5 + "\n";

    }

	private static String createTreeUpdateBackwardChain(String rtContext, String role) throws GSimEngineException {

        // if one element gets contracted (rule 1), the respective state-fact
        // and the sibling (complement) of this state has to be retracted.
        String n3 = "(defrule updateLeafInitial_NextState-retract_Current_state-" + role + " \n";
        n3 += " (declare (salience 100))\n";
        n3 += " (contracting ?sfn)\n";
		n3 += " " + rtContext;
        n3 += " (parameter (name \"exec-sc\"))\n";
        n3 += " ?state <- (state-fact (name ?sfn) (context \"" + role + "\") (parent ?sn&:(neq ?sn nil)) (depth ?h) (rule ?ruleName1))\n";
        n3 += " ?sibling <- (state-fact (name ?sibling-name&:(neq ?sibling-name ?sfn)) (parent ?sn) (rule ?ruleName2))\n";
        n3 += " ?parent <- (state-fact (name ?sn) (leaf ?pl) )\n";
        n3 += " =>\n";
        n3 += " (retract ?state) \n";
        n3 += " (retract ?sibling) \n";
        n3 += " (undefrule ?ruleName1) \n";
        n3 += " (undefrule ?ruleName2) \n";
		// n3 += " (printout t ---createTreeUpdateBackwardChain must be first ... salience=100--------- ?sfn ----childcount1= (child-count-"
		// + role
		// + " ?sfn)--- sibling ?sibling-name --childcount2= (child-count-" + role + " ?sibling-name) -------- ?parent crlf)";
        n3 += " (modify ?parent (leaf (+ ?pl 2.0))) \n";
        n3 += " (assert (state-retracted ?sfn))\n";
        n3 += " (assert (state-retracted ?sibling-name))\n";
        n3 += " (assert (child-of-state-retracted ?sn)) )\n";

        String n4 = "(defrule updateStateConnection_Elems_2 \n";
        n4 += " (state-retracted ?sn)\n";
		n4 += " " + rtContext;
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
