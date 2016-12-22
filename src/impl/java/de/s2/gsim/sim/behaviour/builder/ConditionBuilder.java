package de.s2.gsim.sim.behaviour.builder;

import static de.s2.gsim.sim.behaviour.builder.ParsingUtils.extractChildAttributePathWithoutParent;
import static de.s2.gsim.sim.behaviour.builder.ParsingUtils.referencesChildFrame;
import static de.s2.gsim.sim.behaviour.builder.ParsingUtils.resolveChildFrameWithList;
import static de.s2.gsim.sim.behaviour.builder.ParsingUtils.resolveList;

import java.util.List;

import cern.jet.random.Uniform;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.util.Utils;

/**
 * All conditions may have a variable or constant LHS. That is, on the LHS either attributes of the agent, or attributes of object types contained by
 * the agent can be referenced. On the RHS three cases are distinguished - Atom conditions: The RHS resolves directly to a constant attribute value
 * number or string - Attribute conditions: The RHS refers to an attribute of the agent - Variable conditions: The RHS refers to an attribute of an
 * objects in one of the agent's object lists.
 *
 */
public abstract class ConditionBuilder {

    private ConditionBuilder() {
        // empty on purpose
    }

    /**
     * Call this method if the attribute spec contains a reference to another attribute
     * 
     * @param cond
     * @param objRefs
     * @param nRule_1
     * @return
     * @throws GSimEngineException
     */
    public static String createAttributeCondition(Instance owner, ConditionDef cond, Object2VariableBindingTable objRefs, String nRule_1)
            throws GSimEngineException {

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

        nRule += createLHS(owner, cond.getParameterName(), objRefs, k, nRule_1);

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

    public static String createCategoricalAtomCondition(Instance owner, String attName, String selectedFiller, Object2VariableBindingTable objRefs,
            String nRule_1)
                    throws GSimEngineException {

        String nRule = "";

        int k = Uniform.staticNextIntFromTo(0, 1000);

        ConditionDef c0 = new ConditionDef(attName, "=", selectedFiller);

        nRule = createLHS(owner, c0.getParameterName(), objRefs, k, nRule_1);

        if (!de.s2.gsim.util.Utils.isNumerical(selectedFiller)) {
            nRule += "(value ?value" + k + 1251 + "&:(eq ?value" + k + 1251 + " \"" + selectedFiller + "\"  )))\n";
        } else {
            nRule += "(value ?value" + k + 1251 + "&:(and (numberp ?value" + k + 1251 + ")  (= ?value" + k + 1251 + " " + selectedFiller + " ) )))";
        }
        return nRule;

    }

    public static String createCategoricalAtomCondition(Instance owner, String attName, List<String> selectedFillers,
            Object2VariableBindingTable objRefs, String nRule_1)
                    throws GSimEngineException {

        if (selectedFillers.size() == 1) {
            return createCategoricalAtomCondition(owner, attName, selectedFillers.get(0), objRefs, nRule_1);
        }

        String nRule = "";

        int k = Uniform.staticNextIntFromTo(0, 1000);

        StringBuffer name = new StringBuffer();
        for (int i = 0; i < selectedFillers.size(); i++) {
            name.append(selectedFillers.get(i));
            if (i < selectedFillers.size() - 1) {
                name.append(" OR");
            }
        }

        ConditionDef c0 = new ConditionDef(attName, "=", name.toString());
        nRule = createLHS(owner, c0.getParameterName(), objRefs, k, nRule_1);

        nRule += " (value ?value" + k + 1251 + "&:(or ";

        for (int i = 0; i < selectedFillers.size(); i++) {
            if (!de.s2.gsim.util.Utils.isNumerical(selectedFillers.get(i))) {
                nRule += "(eq ?value" + k + 1251;
                nRule += " \"" + selectedFillers.get(i) + "\"  )";
            } else {
                nRule += " (and (numberp ?value" + k + 1251 + ")  (= ?value" + k + 1251 + " " + selectedFillers.get(i) + " ) )";
            }

        }

        nRule += ") ))\n";
        return nRule;

    }

    public static String createCondition(Instance agent, ConditionDef cond, Object2VariableBindingTable objRefs, String ruleSoFar)
            throws GSimEngineException {

        String n = "";

        if (!referencesChildFrame(agent.getDefinition(), cond.getParameterValue()) && !isExistQuantified(cond)
                && cond.getParameterValue().indexOf("{") < 0) {
            n = "" + createAtomCondition(agent, cond, objRefs, n);
        } else if (isExistQuantified(cond)) {
            n = createExistsQuantifiedCondition(agent, cond, objRefs);
        } else if (!referencesChildFrame(agent.getDefinition(), cond.getParameterValue()) && !isExistQuantified(cond)) {
            n = createAttributeCondition(agent, cond, objRefs, n);
        } else if (!referencesChildFrame(agent.getDefinition(), cond.getParameterValue()) && !isExistQuantified(cond)) {
            n = createVariableCondition(agent, cond, objRefs, n);
        }

        return n;
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
    public static String createExistsQuantifiedCondition(Instance agent, ConditionDef cond, Object2VariableBindingTable refs) {


        int variableIndex = Uniform.staticNextIntFromTo(0, 1000);

        String conditionValueVar = "???";

        String s1 = "";

        if (referencesChildFrame(agent.getDefinition(), cond.getParameterValue())) {
            s1 = buildVariableExistQuantifiedConditionPart(agent, cond, refs, variableIndex);
            conditionValueVar = new StringBuilder("?exparam").append(variableIndex + 1).toString();
        } else {
            conditionValueVar = cond.getParameterValue();
        }

        String objectPath = resolveChildFrameWithList(agent.getDefinition(), cond.getParameterName());
        String attPath = null;
        if (referencesChildFrame(agent.getDefinition(), cond.getParameterName())) {
            attPath = extractChildAttributePathWithoutParent(agent.getDefinition(), cond.getParameterName());
        }

        String s2 = buildConstantExistsQuantifiedConditionPart(agent, cond, refs, objectPath, attPath, variableIndex, conditionValueVar);

        return s1 + s2;

    }

    private static String buildVariableExistQuantifiedConditionPart(Instance agent, ConditionDef cond, Object2VariableBindingTable refs,
            int variableIndex) {

        boolean negated = cond.getOperator().contains("~") || cond.getOperator().contains("NOT");
        StringBuilder s = new StringBuilder();
        String objPath = resolveChildFrameWithList(agent.getDefinition(), cond.getParameterValue());
        String list = objPath.split("/")[0].trim();
        String remainingPath = cond.getParameterValue().substring(objPath.length() + 1, cond.getParameterValue().length());
        String binding = refs.getBinding(resolveChildFrameWithList(agent.getDefinition(), cond.getParameterValue()));

        if (negated) {
            s.append(" (or (not (exists (object-parameter (object-class ?pName")
            .append(variableIndex)
            .append("&:(eq pName")
            .append(variableIndex)
            .append(" \"")
            .append(objPath)
            .append("\")) (instance-name ")
            .append(binding)
            .append(") )))\n");

            s.append("  (and (object-parameter (object-class \"")
            .append(objPath)
            .append("\") (instance-name " )
            .append(binding)
            .append("))\n");

            s.append("  (parameter (name ?n0&:(eq ?n0 (str-cat \"")
            .append(list)
            .append("/\" ")
            .append(binding)
            .append(" \"/")
            .append(remainingPath)
            .append("\")))")
            .append(" (value ?exparam")
            .append(variableIndex + 1)
            .append("))\n");

        } else {

            s.append(" (object-parameter (object-class \"")
            .append(objPath)
            .append("\") (instance-name ")
            .append(binding)
            .append("))\n");

            s.append(" (parameter (name ?n1&:(eq ?n1 (str-cat \"")
            .append(list)
            .append("/\" ")
            .append(binding)
            .append(" \"/" )
            .append(remainingPath)
            .append("\"))) ")
            .append(" (value ?exparam" )
            .append(variableIndex + 1)
            .append("))\n");
        }
        return s.toString();
    }

    private static String buildConstantExistsQuantifiedConditionPart(Instance agent, ConditionDef cond, Object2VariableBindingTable refs,
            String objectPath, String attPath, int variableIndex,
            String valueVariableBinding) {

        boolean negated = cond.getOperator().contains("~") || cond.getOperator().contains("NOT");
        String s;
        String list = resolveList(cond.getParameterName().split("/")[0].trim());

        String binding = refs.getBinding(resolveChildFrameWithList(agent.getDefinition(), objectPath));
        if (!negated) {
            if (attPath == null) {
                s = " (exists (object-parameter (object-class \"" + objectPath + "\")))\n";
            } else {
                s = " (object-parameter (object-class \"" + objectPath + "\") (instance-name " + binding + "))\n";
                s += " (exists (parameter (name ?m&:(eq ?m (str-cat \"" + list + "/\"" + " " + binding + " " + "\"/" + attPath + "\")))";
                if (cond.getParameterValue().length() > 0) {

                    if (Utils.isNumerical(valueVariableBinding)) {
                        s += " (value ?v" + variableIndex + "&:(eq* ?v" + variableIndex + " " + valueVariableBinding + ")))) \n";
                    } else if (!valueVariableBinding.contains("?")) {
                        s += " (value \"" + valueVariableBinding + "\"))) \n";
                    } else {
                        s += " (value " + valueVariableBinding + "))) \n";
                    }
                } else {
                    s += "))\n";
                }
            }
        } else {
            if (attPath == null) {
                s = " (not (exists (object-parameter (object-class \"" + objectPath + "\"))))\n";
            } else {
                s = " (not (parameter (name ?m&:(and (call (new java.lang.String ?m) contains  \"" + list + "\") "
                        + " (call (new java.lang.String ?m) contains \"" + attPath + "\"))) ";

                if (cond.getParameterValue().length() > 0) {
                    if (Utils.isNumerical(valueVariableBinding)) {
                        s += " (value ?v" + variableIndex + "&:(eq* ?v" + variableIndex + " " + valueVariableBinding + ")) )) \n";
                    } else if (!valueVariableBinding.contains("?")) {
                        s += " (value ?v" + variableIndex + "&:(eq ?v" + variableIndex + " \"" + valueVariableBinding + "\")))) )) \n";
                    } else {
                        s += " (value ?v" + variableIndex + "&:(eq ?v" + variableIndex + " " + valueVariableBinding + ")))) )) \n";
                    }
                } else {
                    s += "))\n";
                }
            }

        }
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
    public static String createAtomCondition(Instance owner, ConditionDef cond, Object2VariableBindingTable objRefs, String nRule_1)
            throws GSimEngineException {

        String nRule = "";

        int k = Uniform.staticNextIntFromTo(0, 1000);

        nRule = createLHS(owner, cond.getParameterName(), objRefs, k, nRule_1);

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

    /**
     * Creates atom condition with a value range from min to max.
     * 
     * @param owner
     * @param attributeName
     * @param min
     * @param max
     * @param objRefs
     * @param nRule_1
     * @return
     * @throws GSimEngineException
     */
    public static String createIntervalAtomCondition(Instance owner, String attributeName, double min, double max,
            Object2VariableBindingTable objRefs, String nRule_1)
                    throws GSimEngineException {

        String nRule = "";

        int k = cern.jet.random.Uniform.staticNextIntFromTo(0, 1000);

        ConditionDef c0 = new ConditionDef(attributeName, ">=", String.valueOf(min));
        ConditionDef c1 = new ConditionDef(attributeName, "<", String.valueOf(max));

        nRule = createLHS(owner, c0.getParameterName(), objRefs, k + 12001, nRule_1);

        nRule += "(value ?value" + k + 1251 + "&:(and (numberp ?value" + k + 1251 + ") (>= ?value" + k + 1251 + " " + String.valueOf(min) + " ))))\n";

        nRule += createLHS(owner, c1.getParameterName(), objRefs, k + 12002, nRule + nRule_1);
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
    public static String createVariableCondition(Instance owner, ConditionDef cond, Object2VariableBindingTable objRefs, String nRule_1)
            throws GSimEngineException {

        int k = Uniform.staticNextIntFromTo(0, 1000);

        String list0 = resolveList(cond.getParameterValue());
        String object0 = resolveChildFrameWithList(owner.getDefinition(), cond.getParameterValue());
        String att0 = extractChildAttributePathWithoutParent(owner.getDefinition(), cond.getParameterValue());

        String nRule = "";
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
        String s = createLHS(owner, cond.getParameterName(), objRefs, k, nRule + nRule_1);
        nRule += s;

        String att = extractChildAttributePathWithoutParent(owner.getDefinition(), cond.getParameterName());

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

    private static String createLHS(Instance owner, String leftVariableName, Object2VariableBindingTable objRefs, int variableIdx, String nRule_1)
            throws GSimEngineException {

        String nRule = "";

        if (referencesChildFrame(owner.getDefinition(), leftVariableName)) {
            // if (!referenceChildObject(leftVariableName)) {
            String list = resolveList(leftVariableName);
            String object = resolveChildFrameWithList(owner.getDefinition(), leftVariableName);
            String att = extractChildAttributePathWithoutParent(owner.getDefinition(), leftVariableName);

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

    private static boolean isExistQuantified(ConditionDef c) {
        return c.getOperator().trim().equalsIgnoreCase("EXISTS") || c.getOperator().trim().equalsIgnoreCase("~EXISTS")
                || c.getOperator().trim().equalsIgnoreCase("NOT EXISTS");
    }

    private static boolean isJessFunc(String s) {
        return s.trim().startsWith("(");
    }

    private static boolean isNumericalAttribute(Instance obj, String pathToAtt) {
        //Attribute a = (Attribute) obj.resolveName(pathToAtt.split("/"));
        Path<Attribute> p = Path.attributePath(pathToAtt.split("/"));
        Attribute a = (Attribute) obj.resolvePath(p);
        return (a instanceof NumericalAttribute);
    }

}
