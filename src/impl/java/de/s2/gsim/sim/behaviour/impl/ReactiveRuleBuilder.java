package de.s2.gsim.sim.behaviour.impl;

import jess.JessException;
import jess.Rete;
import cern.jet.random.Uniform;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.def.objects.Instance;
import de.s2.gsim.def.objects.behaviour.ConditionDef;
import de.s2.gsim.def.objects.behaviour.UserRule;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.Context;
import de.s2.gsim.sim.behaviour.SimAction;

public class ReactiveRuleBuilder {

    private RuntimeAgent agent = null;

    private ConditionBuilder utils = new ConditionBuilder();

    public ReactiveRuleBuilder(RuntimeAgent agent) {
        this.agent = agent;
    }

    public String build(Rete rete) throws GSimEngineException {

        String res = "";

        try {

            Instance[] ruleInstances = agent.getBehaviour().getRules();

            res = "";

            for (int i = 0; i < ruleInstances.length; i++) {
                if (i > 0) {
                    res += "\n";
                }
                UserRule rule = new UserRule(ruleInstances[i]);
                if (rule.isActivated()) {
                    res += buildUserRules_PerActionName(rule);
                }
            }

            // logger.debug(res);

            rete.executeCommand(res);
            return res;

        } catch (JessException e) {
            e.printStackTrace();
        }
        return res;
    }

    private String buildUserRules_PerActionName(UserRule rule) throws GSimEngineException {

        de.s2.gsim.def.objects.behaviour.ActionDef[] consequences = rule.getConsequences();
        String role = ParsingUtils.getDefiningRoleForRule(agent, rule.getName());

        String res = "";

        try {

            if (rule.isActivated()) {
                for (int i = 0; i < consequences.length; i++) {

                    de.s2.gsim.def.objects.behaviour.ActionDef c = consequences[i];

                    String x = rule.getName() + "_" + i;
                    x = x.replace(' ', '_');
                    x = x.replace(')', ']');
                    x = x.replace('(', '[');
                    String head = "(defrule " + x;
                    String val = consequences[i].getName();
                    val = val.replace(' ', '_');
                    val = val.replace(',', '_');
                    val = val.replace(')', ']');
                    val = val.replace('(', '[');
                    String nRule = head + "_" + val;

                    String ruleIdent = x + val + role;

                    ConditionDef[] conditions = rule.getConditions();

                    String salience = "\n ";
                    salience += "(declare (salience " + (int) consequences[i].getSalience() + ")) ";

                    String[] params = c.getObjectClassParams();
                    String rtContext = "\n (parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n";

                    if (params != null && params.length > 0) {
                        res += buildUserRules_PerActionName_P(ruleIdent, nRule + salience + rtContext, params, c, conditions, role, rule);
                    } else {

                        nRule += salience;
                        String ctxString = "";

                        nRule += " (parameter (name \"exec-r\"))\n";
                        // nRule += " (parameter (name \"exec-interval\") (value ?exc&:(= 0 (mod ?*current-time* ?exc))))\n";
                        nRule += " (parameter (name \"executing-role\") (value " + "\"" + role + "\"))\n";

                        // HashMap<String, Integer> p =
                        // utils.buildObjectRefTable(conditions);

                        Object2VariableBindingTable p = new Object2VariableBindingTable();
                        p.build(rule);

                        for (int k = 0; k < conditions.length; k++) {
                            ConditionDef cond = conditions[k];
                            if (isConstant(cond.getParameterValue()) && !isExistQuantified(cond) && cond.getParameterValue().indexOf("{") < 0) {
                                nRule += " " + utils.createFixedAtomCondition(cond, p, nRule);
                            } else if (isConstant(cond.getParameterValue()) && isExistQuantified(cond)) {
                                nRule += "" + utils.createExistsQuantifiedCondition(cond, p);
                            } else if (isConstant(cond.getParameterValue())) {
                                nRule += " " + utils.createAttributeCondition(cond, p, nRule);
                            } else if (!isConstant(cond.getParameterValue())) {
                                nRule += "" + utils.createVariableCondition(agent, cond, p, nRule);
                            }

                        }

                        String ns = agent.getNameSpace().split("/")[0];
                        nRule += " =>\n";
                        nRule += " (bind ?res (call " + SimAction.class.getName() + " valueOf \"" + c.getClassName() + "\" \"" + ns + "\") )\n";
                        nRule += " (bind ?ctx (new " + Context.class.getName() + "))\n";
                        // nRule += " (set ?ctx agent (?*agent*))\n";
                        nRule += " (set ?ctx executionContext (?*agent* getExecutionContext(new java.lang.String \"" + role + "\")))\n";
                        nRule += ctxString;
                        nRule += " (set ?res context ?ctx)\n";
                        // nRule += " (assert (executed-" + ruleIdent + "))\n";
                        nRule += " (call ?res execute))\n";

                        res += nRule + "\n";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    private String buildUserRules_PerActionName_P(String ruleIdent, String nRule, String[] objParams, de.s2.gsim.def.objects.behaviour.ActionDef consequence,
            ConditionDef[] conditions, String role, UserRule rule) {

        try {
            String ctxString = "";

            Object2VariableBindingTable params = new Object2VariableBindingTable();
            params.build(rule);
            // HashMap<String, Integer> params =
            // utils.buildObjectRefTable(conditions);

            nRule += " (parameter (name \"exec-r\"))\n";
            // nRule += " (parameter (name \"exec-interval\") (value ?exc&:(= 0 (mod ?*current-time* ?exc))))\n";

            for (String userParamName : objParams) {

                int number = Uniform.staticNextIntFromTo(0, 1000);

                String binding = params.getBinding(userParamName);

                // number=params.get(userParamName);
                if (!userParamName.startsWith("{")) {
                    nRule += " (object-parameter (object-class ?pName" + number + "&:(eq ?pName" + number + " \"" + userParamName
                            + "\")) (instance-name " + binding + "))\n";
                }
                ctxString += " (call ?ctx addObject (toInstPath ?pName" + number + " " + binding + "))\n";

            }

            for (int k = 0; k < conditions.length; k++) {
                ConditionDef cond = conditions[k];
                if (isConstant(cond.getParameterValue()) && !isExistQuantified(cond) && cond.getParameterValue().indexOf("{") < 0) {
                    nRule += "" + utils.createFixedAtomCondition(cond, params, nRule);
                } else if (isExistQuantified(cond)) {
                    nRule += "" + utils.createExistsQuantifiedCondition(cond, params);
                } else if (isConstant(cond.getParameterValue()) && !isExistQuantified(cond)) {
                    nRule += "" + utils.createAttributeCondition(cond, params, nRule);
                } else if (!isConstant(cond.getParameterValue()) && !isExistQuantified(cond)) {
                    nRule += "" + utils.createVariableCondition(agent, cond, params, nRule);
                }
            }

            String ns = agent.getNameSpace().split("/")[0];
            nRule += " =>\n";
            nRule += " (bind ?res (call " + SimAction.class.getName() +" valueOf \"" + consequence.getClassName() + "\" \"" + ns + "\") )\n";
            nRule += " (bind ?ctx (new " + Context.class.getName() + "))\n";
            nRule += " (set ?ctx executionContext (?*agent* getExecutionContext(new java.lang.String \"" + role + "\")))\n";
            // nRule += " (set ?ctx executionContext (?*agent* getExecutionContext(new java.lang.String \"" + role + "\")))\n";
            nRule += ctxString;
            nRule += " (set ?res context ?ctx)\n";
            // nRule += " (assert (executed-" + ruleIdent + "))\n";
            nRule += " (call ?res execute))\n";

            return nRule;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

}
