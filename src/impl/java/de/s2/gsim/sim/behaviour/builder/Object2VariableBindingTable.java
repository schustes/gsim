package de.s2.gsim.sim.behaviour.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cern.jet.random.Uniform;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.ExpansionDef;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.environment.RLRule;
import de.s2.gsim.environment.UserRule;

/**
 * Maintains a map from agent objects to variable names in the rule program. This is necessary if the same agent objects have to be referenced in
 * different lines of the rule program to match the proper facts in the rule base. Therefore it is important that the semantically same attributes and
 * objects are bound to the same variables during runtime; neither must the semantically identical objects be bound to different variable names nor
 * different a objects to the same variable name.
 * 
 */
public class Object2VariableBindingTable {

	private Instance agent;

    private HashMap<String, String> map = new HashMap<String, String>();

    private int runningIdx = 0;

	public Object2VariableBindingTable(Instance agent) {
		this.agent = agent;
	}

    /**
     * Builds the table from a rule
     * 
     * @param rule
     */
	public void build(UserRule rule) {
        map.clear();
        runningIdx = Uniform.staticNextIntFromTo(0, 1000);
        addConditionsRefs(rule.getConditions());
        if (rule instanceof RLRule) {
            addExpansionRefs(((RLRule) rule).getExpansions());
        }
        for (ActionDef a : rule.getConsequents()) {
            addParamRefs(a);
        }
    }

    /**
     * Returns the binding for a particular object class referenced by conditions or expansion of the rule.
     * 
     * @param objClassName
     * @return
     */
    public String getBinding(String objClassName) {
        return map.get(objClassName);
    }

    /**
     * Returns the original object classes referenced by the rule.
     * 
     * @return
     */
    public Iterator<String> getObjectClassNames() {
        return map.keySet().iterator();
    }

    public void merge(Object2VariableBindingTable another) {
        map.putAll(another.map);
    }

    /**
     * Resolves the objects referenced in each condition, and binds it to a variable later used by jess in its condition part.
     * 
     * @param conditions
     */
    private void addConditionsRefs(ConditionDef[] conditions) {
        for (ConditionDef c : conditions) {
			if (ParsingUtils.referencesChildFrame(agent.getDefinition(), c.getParameterName())) {
                String object = resolveObjectClassWithList(c.getParameterName());
                addObjectClass(object);
            }
            if (ParsingUtils.referencesChildFrame(agent.getDefinition(), c.getParameterValue())) {
                String object = resolveObjectClassWithList(c.getParameterValue());
                addObjectClass(object);
            }
            if (!ParsingUtils.referencesChildFrame(agent.getDefinition(), c.getParameterValue())
                    && !ParsingUtils.referencesChildFrame(agent.getDefinition(), c.getParameterName())) {
                String object = c.getParameterName();
                addObjectClass(object);
            }
        }
    }

    /**
     * Resolves the objects referenced in each expansion, and binds it to a variable later used by jess in its condition part.
     * 
     * @param expansions
     */
	private void addExpansionRefs(List<ExpansionDef> expansions) {
        for (ExpansionDef c : expansions) {
			if (ParsingUtils.referencesChildFrame(agent.getDefinition(), c.getParameterName())) {
                String pn = c.getParameterName();
                String object = resolveObjectClassWithList(pn);
                addObjectClass(object);
            }
        }
    }

    private void addObjectClass(String objClassName) {
        if (!map.containsKey(objClassName)) {
            runningIdx++;
            map.put(objClassName, "?ovar" + runningIdx);
        }
    }

    private void addParamRefs(ActionDef a) {
        if (a.getObjectClassParams() == null || a.getObjectClassParams() != null && a.getObjectClassParams().length == 0) {
            return;
        }
        for (String s : a.getObjectClassParams()) {
            addObjectClass(s);
        }
    }

    private String resolveObjectClassWithList(String s) {
		return ParsingUtils.resolveChildFrameWithList(agent.getDefinition(), s);
	}

}
