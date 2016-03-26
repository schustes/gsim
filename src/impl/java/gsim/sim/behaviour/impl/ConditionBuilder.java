package gsim.sim.behaviour.impl;

import java.io.FileInputStream;
import java.util.HashMap;

import cern.jet.random.Uniform;
import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.SimulationController;
import gsim.def.objects.Instance;
import gsim.def.objects.behaviour.ConditionDef;
import gsim.def.objects.behaviour.ExpansionDef;
import gsim.util.Utils;

/**
 * All conditions may have a variable or constant LHS. That is, on the LHS either attributes of the agent, or attributes of object types contained by
 * the agent can be referenced. On the RHS three cases are distinguished - Atom conditions: The RHS resolves directly to a constant attribute value
 * number or string - Attribute conditions: The RHS refers to an attribute of the agent - Variable conditions: The RHS refers to an attribute of an
 * objects in one of the agent's object lists.
 *
 */
public class ConditionBuilder {

    /**
     * Call this method if the attribute spec contains a reference to another attribute
     * 
     * @param cond
     * @param objRefs
     * @param nRule_1
     * @return
     * @throws GSimEngineException
     */
    public String createAttributeCondition(ConditionDef cond, Object2VariableBindingTable objRefs, String nRule_1) throws GSimEngineException {

        String nRule = "";

        int k = Uniform.staticNextIntFromTo(0, 1000);

        String expr = cond.getParameterValue();
        int p1 = expr.indexOf("{");
        int p2 = expr.lastIndexOf("}");
        String var = expr.substring(p1, p2);
        String[] vars = expr.split("\\{");// if more than 1 var vars.length>1
        if (vars.length == 0 || vars.length == 1 && vars[0].length() == 0) {
            vars = new String[1];
            vars[0] = var;
        }

        String replace = expr;// expr.substring(0, p1)+" ?value"+(k+1)+"
        // "+expr.substring(p2+1);
        for (int r = 0; r < vars.length; r++) {
            if (vars[r].length() > 0) {
                if (vars[r].contains("}")) {
                    String s = vars[r].split("}")[0].trim();
                    nRule += " (parameter (name \"" + s + "\") (value ?value" + (k + 100) + "))\n";
                    replace = replace.replaceAll("\\{" + s + "\\}", "?value" + (k + 100));
                }
            }
        }

        nRule += createLHS(cond.getParameterName(), objRefs, k, nRule_1);

        if (cond.getOperator().equals("=")) {
            nRule += "(value ?value" + (k) + "&:(eq*" + " ?value" + (k) + " " + replace + " " + ")))\n";
        } else if (cond.getOperator().equals("<>")) {
            nRule += "(value ?value" + (k) + "&:(neq" + " ?value" + (k) + " " + replace + " " + ")))\n";
        } else {
            nRule += "(value ?value" + (k) + "&:(and (numberp ?value" + k + ") (" + cond.getOperator() + " ?value" + (k) + " " + replace + " "
                    + "))))\n";
        }

        return nRule;

    }

    public String createCategoricalAtomCondition(String attName, String selectedFiller, Object2VariableBindingTable objRefs, String nRule_1)
            throws GSimEngineException {

        String nRule = "";

        int k = Uniform.staticNextIntFromTo(0, 1000);

        ConditionDef c0 = new ConditionDef(attName, "=", selectedFiller);

        nRule = createLHS(c0.getParameterName(), objRefs, k, nRule_1);

        if (!gsim.util.Utils.isNumerical(selectedFiller)) {
            nRule += "(value ?value" + k + 1251 + "&:(eq ?value" + k + 1251 + " \"" + selectedFiller + "\"  )))\n";
        } else {
            nRule += "(value ?value" + k + 1251 + "&:(and (numberp ?value" + k + 1251 + ")  (= ?value" + k + 1251 + " " + selectedFiller + " ) )))";
        }
        return nRule;

    }

    public String createCategoricalAtomCondition(String attName, String[] selectedFillers, Object2VariableBindingTable objRefs, String nRule_1)
            throws GSimEngineException {

        // if (true)return "";

        if (selectedFillers.length == 1) {
            return this.createCategoricalAtomCondition(attName, selectedFillers[0], objRefs, nRule_1);
        }

        String nRule = "";

        int k = Uniform.staticNextIntFromTo(0, 1000);

        StringBuffer name = new StringBuffer();
        for (int i = 0; i < selectedFillers.length; i++) {
            name.append(selectedFillers[i]);
            if (i < selectedFillers.length - 1) {
                name.append(" OR");
            }
        }

        ConditionDef c0 = new ConditionDef(attName, "=", name.toString());
        nRule = createLHS(c0.getParameterName(), objRefs, k, nRule_1);

        nRule += " (value ?value" + k + 1251 + "&:(or ";

        for (int i = 0; i < selectedFillers.length; i++) {
            if (!gsim.util.Utils.isNumerical(selectedFillers[i])) {
                nRule += "(eq ?value" + k + 1251;
                nRule += " \"" + selectedFillers[i] + "\"  )";
            } else {

                nRule += " (and (numberp ?value" + k + 1251 + ")  (= ?value" + k + 1251 + " " + selectedFillers[i] + " ) )";

                // nRule += " (value ?value" + k + 1251 + "&:(and (numberp ?value" + k
                // + 1251 + ") (= ?value" + k + 1251 + " " + selectedFillers[i]
                // + " ) )))";
            }

        }

        nRule += ") ))\n";
        return nRule;

    }

    public String createCondition(Instance agent, ConditionDef cond, Object2VariableBindingTable objRefs, String ruleSoFar) throws GSimEngineException {

        String n = "";

        if (isConstant(cond.getParameterValue()) && !isExistQuantified(cond) && cond.getParameterValue().indexOf("{") < 0) {
            n = "" + createFixedAtomCondition(cond, objRefs, n);
        } else if (isExistQuantified(cond)) {
            n = createExistsQuantifiedCondition(cond, objRefs);
        } else if (isConstant(cond.getParameterValue()) && !isExistQuantified(cond)) {
            n = createAttributeCondition(cond, objRefs, n);
        } else if (!isConstant(cond.getParameterValue()) && !isExistQuantified(cond)) {
            n = createVariableCondition(agent, cond, objRefs, n);
        }

        return n;
    }

    /**
     * Creates initial condition (containing all filler/covering the whole value range).
     * 
     * @param cond
     * @param objRefs
     * @param nRule_1
     * @return
     * @throws GSimEngineException
     */
    public String createDefaultAtomCondition(ExpansionDef cond, Object2VariableBindingTable objRefs, String nRule_1) throws GSimEngineException {

        String nRule = "";

        int k = cern.jet.random.Uniform.staticNextIntFromTo(0, 1000);

        ConditionDef c0 = null;
        ConditionDef c1 = null;

        if (cond.isNumerical()) {
            c0 = new ConditionDef(cond.getParameterName(), ">=", String.valueOf(cond.getMin()));
            c1 = new ConditionDef(cond.getParameterName(), "<", String.valueOf(cond.getMax()));
            nRule = createLHS(c0.getParameterName(), objRefs, k + 9001, nRule_1);
            nRule += "(value ?value" + k + "&:(and (numberp ?value" + k + ") (>= ?value" + k + " " + String.valueOf(cond.getMin()) + " ))))\n";

            nRule += createLHS(c1.getParameterName(), objRefs, k + 9002, nRule + nRule_1);
            nRule += "(value ?value" + k + 891 + "&:(and (numberp ?value" + k + 891 + ") (< ?value" + k + 891 + " " + String.valueOf(cond.getMax())
                    + " ))))\n";

        } else {
            String[] cat = cond.getFillers();
            c0 = new ConditionDef(cond.getParameterName(), "=", cat[0]);
            nRule = createLHS(c0.getParameterName(), objRefs, k + 12001, nRule_1);
            if (!gsim.util.Utils.isNumerical(cat[0])) {
                nRule += "(value ?value" + k + 1251 + "&:(or (eq ?value" + k + 1251 + " \"" + cat[0] + "\" ) ";
            } else {
                nRule += "(value ?value" + k + 1251 + "&:(and (numberp ?value" + k + 1251 + ")  (or (= ?value" + k + 1251 + " " + cat[0] + " ) ";
            }
            if (cat.length > 1) {
                for (int i = 1; i < cat.length; i++) {
                    if (!gsim.util.Utils.isNumerical(cat[0])) {
                        nRule += "(eq ?value" + k + 1251 + " \"" + cat[i] + "\" )";
                    } else {
                        nRule += "(eq ?value" + k + 1251 + " " + cat[i] + " )";
                    }
                }
            }
            if (!gsim.util.Utils.isNumerical(cat[0])) {
                nRule += ") ) )\n";
            } else {
                nRule += ") ) ) )\n";
            }

        }

        return nRule;

    }

    /**
     * Call this method if the operator is exist/not-exists. Then the whole condition is interpreted accordingly; for example, if the condition reads
     * 'object::atribute EXISTS b', it is interpreted as: if there exists an object with attribute value b (no matter how many of them), but it is
     * also possible to ommit the value on the rhs of EXISTS, and the attribute, then it would be interpreted as 'if there exists an object of type
     * object'
     * 
     * @param cond
     * @param refs
     * @return
     */
    public String createExistsQuantifiedCondition(ConditionDef cond, Object2VariableBindingTable refs) {

        String objectPath = resolveObjectClass(cond.getParameterName());
        String attPath = null;
        if (cond.getParameterName().contains("::")) {
            attPath = resolveAttribute(cond.getParameterName());
        }

        String s = "";

        int k = Uniform.staticNextIntFromTo(0, 1000);

        boolean negated = cond.getOperator().contains("~") || cond.getOperator().contains("NOT");

        String value = cond.getParameterValue();

        // int idx = refs.get(objectPath);
        String binding = refs.getBinding(objectPath);
        if (!isConstant(value) && negated) {

            if (refs.getBinding(resolveObjectClass(value)) != null) {
                binding = refs.getBinding(resolveObjectClass(value));
            }

            String[] a = value.split("::");
            String remainingPath = a[1];
            String objPath = a[0].trim().replace("$", "");
            String list = a[0].split("/")[0].trim().replace("$", "");
            s += " (or (not (exists (object-parameter (object-class ?pName" + k + "&:(eq pName" + k + " \"" + objPath + "\")) (instance-name "
                    + binding + ") )))\n";
            s += "  (and (object-parameter (object-class \"" + objPath + "\") (instance-name " + binding + "))\n";
            s += "  (parameter (name ?n&:(eq ?n (str-cat \"" + list + "/\"" + " " + binding + " " + " \"/" + remainingPath + "\"))) "
                    + " (value ?exparam" + (k + 1) + "))\n"; // two
            // missing
            // brackets
            // added
            // below!
            value = "?exparam" + (k + 1);
        } else if (!isConstant(value) && !negated) {

            if (refs.getBinding(resolveObjectClass(cond.getParameterName())) != null) {
                binding = refs.getBinding(resolveObjectClass(cond.getParameterName()));
            }

            String[] a = value.split("::");
            String remainingPath = resolveAttribute(a[1]);
            String objPath = resolveObjectClass(a[0].trim());
            String list = resolveList(a[0].split("/")[0].trim());
            s += "  (object-parameter (object-class ?pName" + k + "&:(eq pName" + k + " \"" + objPath + "\")) (instance-name " + binding + "))\n";
            s += "  (parameter (name ?n&:(eq ?n (str-cat \"" + list + "/\"" + " " + binding + " " + " \"/" + remainingPath + "\"))) "
                    + " (value ?exparam" + (k + 1) + "))\n";

            value = "?exparam" + (k + 1);
        } else if (value.contains("$")) {
            if (refs.getBinding(resolveObjectClass(cond.getParameterName())) != null) {
                binding = refs.getBinding(resolveObjectClass(cond.getParameterName()));
            }

            String[] a = value.split("::");
            String remainingPath = a[1];
            String list = a[0].split("/")[0].trim().replace("$", "");
            s += " (parameter (name ?n&:(eq ?n (str-cat \"" + list + "/\"" + " " + binding + " " + " \"/" + remainingPath + "\"))) "
                    + " (value ?exparam" + (k + 7012) + "))\n";

            value = "?exparam" + (k + 7012);
        }

        String list = resolveList(cond.getParameterName().split("/")[0].trim());

        if (!negated) {
            if (attPath == null) {
                s = " (exists (object-parameter (object-class \"" + objectPath + "\")))\n";
            } else {
                s = " (object-parameter (object-class \"" + objectPath + "\") (instance-name " + binding + "))\n";
                s += " (exists (parameter (name ?m&:(eq ?m (str-cat \"" + list + "/\"" + " " + binding + " " + " \"/" + attPath + "\")))";
                if (cond.getParameterValue().length() > 0) {

                    if (Utils.isNumerical(value)) {
                        s += " (value ?v" + k + "&:(eq* ?v" + k + " " + value + ")))) \n";
                    } else if (!value.contains("?")) {
                        s += " (value \"" + value + "\"))) \n";
                    } else {
                        s += " (value " + value + "))) \n";
                    }
                } else {
                    s += "))\n";
                }
            }
        } else {
            if (attPath == null) {
                s = " (not (exists (object-parameter (object-class \"" + objectPath + "\"))))\n";
            } else {

                s += "  (not (parameter (name ?m&:(and (call (new java.lang.String ?m) contains  \"" + list + "\") "
                        + " (call (new java.lang.String ?m) contains  \"" + attPath + "\"))) ";

                if (cond.getParameterValue().length() > 0) {
                    // last two brackets close first part within the OR
                    if (Utils.isNumerical(value)) {
                        s += " (value ?v" + k + "&:(eq* ?v" + k + " " + value
                        // + ")))) )) \n";
                                + ")) )) \n";
                    } else if (!value.contains("?")) {
                        s += " (value ?v" + k + "&:(eq ?v" + k + " \"" + value + "\")))) )) \n";
                    } else {
                        s += " (value ?v" + k + "&:(eq ?v" + k + " " + value + ")))) )) \n";
                        // s += " (value ?v" + k + "&:(eq ?v" + k + " " + value + ")) ))
                        // \n";
                    }
                } else {
                    s += "))\n";
                }

            }

        }

        // logger.debug(s);
        return s;

    }

    /**
     * Creates a condition for any specification on the LHS, and a value on the RHS
     * 
     * @param cond
     * @param objRefs
     * @param nRule_1
     * @return
     * @throws GSimEngineException
     */
    public String createFixedAtomCondition(ConditionDef cond, Object2VariableBindingTable objRefs, String nRule_1) throws GSimEngineException {

        String nRule = "";

        int k = Uniform.staticNextIntFromTo(0, 1000);

        nRule = createLHS(cond.getParameterName(), objRefs, k, nRule_1);

        if (!Utils.isNumerical(cond.getParameterValue()) && !isJessFunc(cond.getParameterValue()) && !cond.getOperator().equals("<>")) {
            nRule += "(value ?value" + k + "&:(eq*" + " ?value" + k + " \"" + cond.getParameterValue() + "\" )))\n";
        } else if (Utils.isNumerical(cond.getParameterValue()) && cond.getOperator().equals("=")) {
            nRule += "(value ?value" + k + "&:(eq*" + " ?value" + k + " " + cond.getParameterValue() + " )))\n";
        } else if (Utils.isNumerical(cond.getParameterValue())) {
            nRule += "(value ?value" + k + "&:(and (numberp ?value" + k + ") (" + cond.getOperator() + " ?value" + k + " " + cond.getParameterValue()
                    + " ))))\n";
        } else if (!Utils.isNumerical(cond.getParameterValue()) && !isJessFunc(cond.getParameterValue()) && cond.getOperator().equals("<>")) {
            nRule += "(value ?value" + k + "&:(neq" + " ?value" + k + " \"" + cond.getParameterValue() + "\" )))\n";
        } else if (!Utils.isNumerical(cond.getParameterValue()) && isJessFunc(cond.getParameterValue()) && cond.getOperator().equals("<>")) {
            nRule += "(value ?value" + k + "&:(neq" + " ?value" + k + " " + cond.getParameterValue() + " )))\n";
        } else if (!Utils.isNumerical(cond.getParameterValue()) && isJessFunc(cond.getParameterValue()) && !cond.getOperator().equals("<>")) {
            nRule += "(value ?value" + k + "&:(eq*" + " ?value" + k + " " + cond.getParameterValue() + " )))\n";
        }

        return nRule;

    }

    public String createNumericalAtomCondition(String attributeName, double min, double max, Object2VariableBindingTable objRefs, String nRule_1)
            throws GSimEngineException {

        String nRule = "";

        int k = cern.jet.random.Uniform.staticNextIntFromTo(0, 1000);

        ConditionDef c0 = new ConditionDef(attributeName, ">=", String.valueOf(min));
        ConditionDef c1 = new ConditionDef(attributeName, "<", String.valueOf(max));

        nRule = createLHS(c0.getParameterName(), objRefs, k + 12001, nRule_1);

        nRule += "(value ?value" + k + 1251 + "&:(and (numberp ?value" + k + 1251 + ") (>= ?value" + k + 1251 + " " + String.valueOf(min) + " ))))\n";

        nRule += createLHS(c1.getParameterName(), objRefs, k + 12002, nRule + nRule_1);
        nRule += "(value ?value" + k + 1291 + "&:(and (numberp ?value" + k + 1291 + ") (< ?value" + k + 1291 + " " + String.valueOf(max) + " ))))\n";

        return nRule;

    }

    /**
     * Creates conditions for variable or constant LHS, and variable RHS, that is on the LHS attributes of any other object can be referenced, as well
     * as on the RHS. Example: For all objects of type x with attribute v whose value equals the attribute w of objects of type y. Or: For attribute a
     * whose value is not equal to values of attributes b of objects of type x.
     * 
     * @param owner
     *            the agent
     * @param cond
     *            the condition to be parsed
     * @param objRefs
     *            table with the indices of referenced objects by the conditions in the rule
     * @param nRule_1
     *            the rule string so far created from the input
     * @return the string with the jess expression for that condition
     * @throws GSimEngineException
     */
    public String createVariableCondition(Instance owner, ConditionDef cond, Object2VariableBindingTable objRefs, String nRule_1)
            throws GSimEngineException {

        int k = Uniform.staticNextIntFromTo(0, 1000);

        String list0 = resolveList(cond.getParameterValue());
        String object0 = resolveObjectClass(cond.getParameterValue());
        String att0 = resolveAttribute(cond.getParameterValue());

        String nRule = "";
        // int idx = objRefs.get(object0);
        String binding = objRefs.getBinding(object0);

        if (binding == null) {
            binding = "?varbinding" + String.valueOf(k + 299);
        }

        String s0 = " (object-parameter (object-class \"" + object0 + "\") (instance-name " + binding + "))\n";
        if (!nRule_1.contains(s0)) {
            nRule += s0;
        }
        nRule += " (parameter (name ?x" + (k + 300) + "&:(eq ?x" + (k + 300) + " (str-cat \"" + list0 + "/\" " + binding + " \"/" + att0 + "\")))";
        nRule += " (value ?value" + k + "))\n";
        String s = createLHS(cond.getParameterName(), objRefs, k, nRule + nRule_1);
        nRule += s;

        String att = resolveAttribute(cond.getParameterName());

        if (!isNumericalAttribute(owner, att) && !cond.getOperator().equals("<>") || cond.getOperator().equals("=")) {
            nRule += "(value ?valueV" + (k) + "&:(eq*" + " ?valueV" + (k) + " ?value" + k + " " + ")))\n";
        } else if (cond.getOperator().equals("=")) {
            nRule += "(value ?valueV" + (k) + "&:(eq*" + " ?valueV" + (k) + " ?value" + k + " " + ")))\n";
        } else if (isNumericalAttribute(owner, att)) {
            nRule += "(value ?valueV" + (k) + "&:(and (numberp ?valueV" + k + ") (" + cond.getOperator() + " ?valueV" + (k) + " ?value" + k + " "
                    + "))))\n";
        } else if (!isNumericalAttribute(owner, att) && cond.getOperator().equals("<>")) {
            nRule += "(value ?valueV" + (k) + "&:(neq" + " ?valueV" + (k) + " ?value" + k + " " + ")))\n";
        }

        return nRule;

    }

    public String resolveAttribute(String s) {
        if (s.contains("::")) {
            s = s.split("::")[1];
        }

        String[] a = s.split("/");

        // if (a.length==2 && !b) return null;

        String ret = "";
        int y = 0;
        // if (!b) y=2;
        for (int i = y; i < a.length; i++) {
            if (ret.length() > 0) {
                ret += "/";
            }
            ret += a[i];
        }

        return ret;
    }

    public String resolveList(String s) {
        String[] a = s.split("/");

        String list = a[0];

        if (list.contains("$")) {
            if (list.lastIndexOf("$") != list.indexOf("$")) {
                list = list.substring(list.lastIndexOf("$"));
                list = list.replace("$", "");
            } else {
                list = list.replace("$", "");
            }
        }
        return list;

    }

    public String resolveObjectClass(String s) {
        String[] a = s.split("/");

        String list = a[0];
        String object = a[1];

        if (object.contains("::")) {
            object = object.substring(0, object.indexOf("::"));
        }

        if (list.contains("$")) {
            if (list.lastIndexOf("$") != list.indexOf("$")) {
                list = list.substring(list.lastIndexOf("$"));
                list = list.replace("$", "");
            } else {
                list = list.replace("$", "");
            }
        }
        return list + "/" + object;

    }

    private String createLHS(String leftVariableName, Object2VariableBindingTable objRefs, int variableIdx, String nRule_1)
            throws GSimEngineException {

        String nRule = "";

        if (!isConstant(leftVariableName)) {
            String list = resolveList(leftVariableName);
            String object = resolveObjectClass(leftVariableName);
            String att = resolveAttribute(leftVariableName);

            // int idx = objRefs.get(object);
            String binding = objRefs.getBinding(object);
            if (binding == null) {
                binding = "?varbinding" + String.valueOf(variableIdx + 299);
            }

            String s0 = " (object-parameter (object-class \"" + object + "\") (instance-name " + binding + "))\n";
            String s1 = " (parameter (name ?x" + (variableIdx + 200) + "&:(eq ?x" + (variableIdx + 200) + " (str-cat \"" + list + "/\" " + binding
                    + " \"/" + att + "\"))) ";

            if (!nRule_1.contains(s0)) {
                nRule += s0 + s1;
            } else {
                nRule += s1;
            }
        } else {
            nRule += "(parameter (name \"" + leftVariableName + "\") ";
        }
        return nRule;
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

    private boolean isJessFunc(String s) {
        return s.trim().startsWith("(");
    }

    private boolean isNumericalAttribute(Instance obj, String pathToAtt) {
        Attribute a = (Attribute) obj.resolveName(pathToAtt.split("/"));
        return (a instanceof NumericalAttribute);
    }

    public static void main(String[] args) throws Exception {

        GSimCore core = GSimCoreFactory.defaultFactory().createCore();

        ModelDefinitionEnvironment env = core.create("test", new FileInputStream("/projects/phd/sim/main/dist/gsim/repos/models/nhs.definition"),
                new HashMap());
        AgentClass a0 = env.getAgentClass("Traditional");
        @SuppressWarnings("unused")
        AgentClass a1 = env.getAgentClass("Forward_looking_trust");
        @SuppressWarnings("unused")
        AgentClass a2 = env.getAgentClass("Independent-Competitor");
        AgentClass c1 = env.getAgentClass("Type A patient");
        @SuppressWarnings("unused")
        AgentClass c2 = env.getAgentClass("Type C patient");

        env.instanciateAgents(a0, "traditional", 1, 0, ModelDefinitionEnvironment.RAND_ATT_ONLY);
        // env.instanciateAgents(a1, "trust", 1, 0, Env.RAND_ATT_ONLY);
        // env.instanciateAgents(a2, "indie", 1, 0, Env.RAND_ATT_ONLY);
        env.instanciateAgents(c1, "A", 1, 0, ModelDefinitionEnvironment.RAND_ATT_ONLY);
        // env.instanciateAgents(c2, "C", 1, 0, Env.RAND_ATT_ONLY);

        SimulationController m = core.createScenarioManager(env, new HashMap(), 53, 1);
        m.start();

    }

}
