package de.s2.gsim.sim.behaviour.rulebuilder;

import java.util.Map;

import jess.JessException;
import jess.Rete;

public class GlobalsBuilder {

    private String begin = "(import jess.*)\n" + "(import gsim.sim.behaviour.*)\n\n" + "(deftemplate episode (slot start-time))\n"
            + "(deftemplate timer (slot time))\n" + "(deftemplate list (slot name) (slot obj))\n"
            + "(deftemplate average-reward (slot value) (slot function) (slot time))\n"
            + "(deftemplate rl-action-node (slot action-name) (slot alpha) (slot count) (slot updateCount) (multislot arg) (slot value) (slot time) (slot function) (slot active) (slot state-fact-name) )\n"
            + "(deftemplate object-parameter (slot object-class) (slot instance-name))\n"
            + "(deftemplate state-fact (slot name) (slot parent) (slot created) (multislot expansion) (slot value) (slot count) (slot active) (slot leaf) (slot depth) (slot last-activation) (slot expansion-count) (slot rule) (slot context))\n"
            + "(deftemplate state-fact-element (slot name) (slot state-fact-name) (slot elem-parent) (slot param-name) (slot from) (slot to) (slot value) )\n"
            + "(deftemplate state-fact-category (slot name) (slot state-fact-name) (slot elem-parent) (slot param-name) (slot category) (slot value))\n"
            + "(deftemplate parameter (slot name) (slot value))\n";

    private String endPart = "(set-reset-globals FALSE)\n" + "(defglobal ?*agent* = (fetch AGENT))\n" + "(defglobal ?*current-time* = -1)\n";

    private String functions = "";

    public GlobalsBuilder() {
        super();
    }

	public String build(Rete rete, Map<?, ?> props) {

        String s = begin + "\n" + createRules() + "\n" + functions + "\n" + createGlobalVariables(props) + "\n" + endPart;
        try {
            rete.executeCommand(s);
        } catch (JessException e) {
            e.printStackTrace();
        }
        return s;
    }

	private String createGlobalVariables(Map<?, ?> props) {
        String s = "";
        if (props.containsKey("AGENT_COUNT")) {
            s += "(defglobal ?*agent-count* = " + (String) props.get("AGENT_COUNT") + ")\n";
        }

        if (props.containsKey("MAX_NODE_COUNT")) {
            s += "(defglobal ?*max-node-count* = " + props.get("MAX_NODE_COUNT") + ")\n";
        }

        if (props.containsKey("MAX_DEPTH")) {
            s += "(defglobal ?*max-depth* = " + props.get("MAX_DEPTH") + ")\n";
        }

        return s;
    }

    private String createRules() {
        String s = "(defrule time-rule (declare (salience 10000)) (timer (time ?t&:(> ?t ?*current-time*)))=>(bind ?*current-time* ?t) )\n\n"
                + "(defquery list-all-actions (rl-action-node))\n";

        s += "(defrule agent-count-rule (declare (salience 100000001))\n";
        s += " (parameter (name \"current-agent-count\") (value ?v))\n";
        s += " =>\n";
        s += " (bind ?*agent-count* ?v))\n";

        return s;
    }

}
