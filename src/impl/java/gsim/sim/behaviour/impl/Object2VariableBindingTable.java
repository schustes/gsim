package gsim.sim.behaviour.impl;

import java.util.HashMap;
import java.util.Iterator;

import cern.jet.random.Uniform;
import gsim.def.objects.behaviour.ActionDef;
import gsim.def.objects.behaviour.ConditionDef;
import gsim.def.objects.behaviour.ExpansionDef;
import gsim.def.objects.behaviour.RLRule;
import gsim.def.objects.behaviour.UserRule;

/**
 * Contains a map with the objects referenced by the conditions (and possibly consequents) of a rule/RLrule. In a rule, multiple references to object
 * specs in a condition set are bound to the same object instances. These bindings are typically stored in this map, containing unique indices for
 * each object class. These indices are then appended to the variable place holders in the jess-expression.
 *
 */
public class Object2VariableBindingTable {

    private HashMap<String, String> map = new HashMap<String, String>();

    private int runningIdx = 0;

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
        for (ActionDef a : rule.getConsequences()) {
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
            if (c.getParameterName().contains("::")) {
                String object = resolveObjectClass(c.getParameterName());
                addObjectClass(object);
            } else if (c.getParameterValue().contains("::")) {
                String object = resolveObjectClass(c.getParameterValue());
                addObjectClass(object);
            } else {
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
    private void addExpansionRefs(ExpansionDef[] expansions) {
        for (ExpansionDef c : expansions) {
            if (c.getParameterName().contains("::")) {
                String pn = c.getParameterName();
                String object = resolveObjectClass(pn);
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

    private String resolveObjectClass(String s) {
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

}
