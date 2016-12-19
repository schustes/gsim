package de.s2.gsim.sim.behaviour.builder;

import static de.s2.gsim.sim.behaviour.builder.ParsingUtils.extractChildAttributePathWithoutParent;
import static de.s2.gsim.sim.behaviour.builder.ParsingUtils.resolveChildFrameWithList;
import static de.s2.gsim.sim.behaviour.builder.ParsingUtils.resolveList;

import java.util.List;

import cern.jet.random.Uniform;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.ExpansionDef;
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
	public String createAttributeCondition(Instance owner, ConditionDef cond, Object2VariableBindingTable objRefs, String nRule_1)
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

	public String createCategoricalAtomCondition(Instance owner, String attName, String selectedFiller, Object2VariableBindingTable objRefs,
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

	public String createCategoricalAtomCondition(Instance owner, String attName, List<String> selectedFillers,
	        Object2VariableBindingTable objRefs, String nRule_1)
            throws GSimEngineException {

        if (selectedFillers.size() == 1) {
			return this.createCategoricalAtomCondition(owner, attName, selectedFillers.get(0), objRefs, nRule_1);
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

    public String createCondition(Instance agent, ConditionDef cond, Object2VariableBindingTable objRefs, String ruleSoFar)
            throws GSimEngineException {

        String n = "";

        if (isConstant(cond.getParameterValue()) && !isExistQuantified(cond) && cond.getParameterValue().indexOf("{") < 0) {
			n = "" + createFixedAtomCondition(agent, cond, objRefs, n);
        } else if (isExistQuantified(cond)) {
            n = createExistsQuantifiedCondition(agent, cond, objRefs);
        } else if (isConstant(cond.getParameterValue()) && !isExistQuantified(cond)) {
			n = createAttributeCondition(agent, cond, objRefs, n);
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
	public String createDefaultAtomCondition(Instance owner, ExpansionDef cond, Object2VariableBindingTable objRefs, String nRule_1)
	        throws GSimEngineException {

        String nRule = "";

        int k = cern.jet.random.Uniform.staticNextIntFromTo(0, 1000);

        ConditionDef c0 = null;
        ConditionDef c1 = null;

        if (cond.isNumerical()) {
            c0 = new ConditionDef(cond.getParameterName(), ">=", String.valueOf(cond.getMin()));
            c1 = new ConditionDef(cond.getParameterName(), "<", String.valueOf(cond.getMax()));
			nRule = createLHS(owner, c0.getParameterName(), objRefs, k + 9001, nRule_1);
            nRule += "(value ?value" + k + "&:(and (numberp ?value" + k + ") (>= ?value" + k + " " + String.valueOf(cond.getMin()) + " ))))\n";

			nRule += createLHS(owner, c1.getParameterName(), objRefs, k + 9002, nRule + nRule_1);
            nRule += "(value ?value" + k + 891 + "&:(and (numberp ?value" + k + 891 + ") (< ?value" + k + 891 + " " + String.valueOf(cond.getMax())
                    + " ))))\n";

        } else {
            List<String> cat = cond.getFillers();
            c0 = new ConditionDef(cond.getParameterName(), "=", cat.get(0));
			nRule = createLHS(owner, c0.getParameterName(), objRefs, k + 12001, nRule_1);
            if (!de.s2.gsim.util.Utils.isNumerical(cat.get(0))) {
                nRule += "(value ?value" + k + 1251 + "&:(or (eq ?value" + k + 1251 + " \"" + cat.get(0) + "\" ) ";
            } else {
                nRule += "(value ?value" + k + 1251 + "&:(and (numberp ?value" + k + 1251 + ")  (or (= ?value" + k + 1251 + " " + cat.get(0) + " ) ";
            }
            if (cat.size() > 1) {
                for (int i = 1; i < cat.size(); i++) {
                    if (!de.s2.gsim.util.Utils.isNumerical(cat.get(0))) {
                        nRule += "(eq ?value" + k + 1251 + " \"" + cat.get(i) + "\" )";
                    } else {
                        nRule += "(eq ?value" + k + 1251 + " " + cat.get(i) + " )";
                    }
                }
            }
            if (!de.s2.gsim.util.Utils.isNumerical(cat.get(0))) {
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
    public String createExistsQuantifiedCondition(Instance agent, ConditionDef cond, Object2VariableBindingTable refs) {

		String objectPath = ParsingUtils.resolveChildFrameWithList(agent.getDefinition(), cond.getParameterName());
        String attPath = null;
        if (ParsingUtils.referencesChildFrame(agent.getDefinition(), cond.getParameterName())) {
			attPath = extractChildAttributePathWithoutParent(agent.getDefinition(), cond.getParameterName());
        }

        String s = "";

        int k = Uniform.staticNextIntFromTo(0, 1000);

        boolean negated = cond.getOperator().contains("~") || cond.getOperator().contains("NOT");

        String value = cond.getParameterValue();

        String binding = refs.getBinding(objectPath);

        if (!isConstant(value)) {
			String objPath = ParsingUtils.resolveChildFrameWithoutList(agent.getDefinition(), value);
			String objPathFull = ParsingUtils.resolveChildFrameWithList(agent.getDefinition(), value);
			String list = objPathFull.split("/")[0].trim();
			String remainingPath = value.substring(objPathFull.length() + 1, value.length());

            if (negated) {

				if (refs.getBinding(resolveChildFrameWithList(agent.getDefinition(), value)) != null) {
					binding = refs.getBinding(resolveChildFrameWithList(agent.getDefinition(), value));
                }

                s += " (or (not (exists (object-parameter (object-class ?pName" + k + "&:(eq pName" + k + " \"" + objPath + "\")) (instance-name "
                        + binding + ") )))\n";
                s += "  (and (object-parameter (object-class \"" + objPath + "\") (instance-name " + binding + "))\n";
                s += "  (parameter (name ?n&:(eq ?n (str-cat \"" + list + "/\"" + " " + binding + " " + " \"/" + remainingPath + "\"))) "
                        + " (value ?exparam" + (k + 1) + "))\n";
                value = "?exparam" + (k + 1);
            } else if (!isConstant(value) && !negated) {

				if (refs.getBinding(resolveChildFrameWithList(agent.getDefinition(), cond.getParameterName())) != null) {
					binding = refs.getBinding(resolveChildFrameWithList(agent.getDefinition(), cond.getParameterName()));
                }

				s += " (object-parameter (object-class ?pName" + k + "&:(eq pName" + k + " \"" + objPath + "\")) (instance-name " + binding
				        + "))\n";
				s += " (parameter (name ?n&:(eq ?n (str-cat \"" + list + "/\"" + " " + binding + " " + " \"/" + remainingPath + "\"))) "
                        + " (value ?exparam" + (k + 1) + "))\n";

                value = "?exparam" + (k + 1);
            }
        }

        String listLHS = resolveList(cond.getParameterName().split("/")[0].trim());

        String s2 = buildExistsQuantifiedRHSExpression(cond, objectPath, attPath, k, negated, value, binding, listLHS);

		return s + s2;

    }

    private String buildExistsQuantifiedRHSExpression(ConditionDef cond, String objectPath, String attPath, int k, boolean negated, String value,
            String binding, String list) {
        String s;
        if (!negated) {
            if (attPath == null) {
                s = " (exists (object-parameter (object-class \"" + objectPath + "\")))\n";
            } else {
                s = " (object-parameter (object-class \"" + objectPath + "\") (instance-name " + binding + "))\n";
				s += " (exists (parameter (name ?m&:(eq ?m (str-cat \"" + list + "/\"" + " " + binding + " " + " \"" + attPath + "\")))";
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
                s = "  (not (parameter (name ?m&:(and (call (new java.lang.String ?m) contains  \"" + list + "\") "
				        + " (call (new java.lang.String ?m) contains \"" + attPath + "\"))) ";

                if (cond.getParameterValue().length() > 0) {
                    if (Utils.isNumerical(value)) {
						s += " (value ?v" + k + "&:(eq* ?v" + k + " " + value + ")) )) \n";
                    } else if (!value.contains("?")) {
                        s += " (value ?v" + k + "&:(eq ?v" + k + " \"" + value + "\")))) )) \n";
                    } else {
                        s += " (value ?v" + k + "&:(eq ?v" + k + " " + value + ")))) )) \n";
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
	public String createFixedAtomCondition(Instance owner, ConditionDef cond, Object2VariableBindingTable objRefs, String nRule_1)
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

	public String createNumericalAtomCondition(Instance owner, String attributeName, double min, double max,
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
    public String createVariableCondition(Instance owner, ConditionDef cond, Object2VariableBindingTable objRefs, String nRule_1)
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

	/**
	 * Extract only that part in the child instance (without path to the instance in the owning agent).
	 * 
	 * @param s
	 * @return
	 */
	public String resolveAttribute1(Instance agent, String s) {
		return extractChildAttributePathWithoutParent(agent.getDefinition(), s);
    }


	private String createLHS(Instance owner, String leftVariableName, Object2VariableBindingTable objRefs, int variableIdx, String nRule_1)
            throws GSimEngineException {

        String nRule = "";

        if (!isConstant(leftVariableName)) {
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

    private boolean isConstant(String s) {
        return !s.contains("/");
    }

    private boolean isExistQuantified(ConditionDef c) {
        return c.getOperator().trim().equalsIgnoreCase("EXISTS") || c.getOperator().trim().equalsIgnoreCase("~EXISTS")
                || c.getOperator().trim().equalsIgnoreCase("NOT EXISTS");
    }

    private boolean isJessFunc(String s) {
        return s.trim().startsWith("(");
    }

    private boolean isNumericalAttribute(Instance obj, String pathToAtt) {
        //Attribute a = (Attribute) obj.resolveName(pathToAtt.split("/"));
        Path<Attribute> p = Path.attributePath(pathToAtt.split("/"));
        Attribute a = (Attribute) obj.resolvePath(p);
        return (a instanceof NumericalAttribute);
    }

}
