package gsim.sim.behaviour.impl;

import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeConstants;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;
import de.s2.gsim.sim.engine.GSimEngineException;
import gsim.def.objects.Frame;
import gsim.def.objects.Instance;
import gsim.def.objects.agent.GenericAgentClass;
import gsim.sim.agent.RuntimeAgent;

public class ParsingUtils {

    public static String getDefiningRoleForRLRule(RuntimeAgent agent, String ruleName) {
        GenericAgentClass def = (GenericAgentClass) agent.getDefinition();

        if (def.getBehaviour().getDeclaredRLRule(ruleName) != null) {
            return def.getTypeName();
        }

        Frame[] ancestors = def.getAncestors();
        for (Frame f : ancestors) {
            GenericAgentClass agentClass = (GenericAgentClass) f;
            if (agentClass.getBehaviour().getDeclaredRLRule(ruleName) != null) {
                return agentClass.getTypeName();
            }
        }
        return "default";
    }

    /*
     * public static String mapRule2Classifier(RuntimeAgent agent, String ruleName) throws GSimEngineException {
     * 
     * GenericAgentClass def = (GenericAgentClass) agent.getDefinition(); Frame[] ancestors = agent.getDefinition().getAncestors();
     * RtExecutionContext[] r = agent.getExecutionContexts();
     * 
     * // check for the immediate defining frame String lastFlatContext = null; for (int i = 0; i < r.length; i++) { for (String s :
     * r[i].getDefiningAgentClasses()) { // logger.debug(s+"-----------------"+def.getTypeName()); if (s.equals(def.getTypeName()) &&
     * def.getBehaviour().getDeclaredRLRule(ruleName) != null) { return r[i].getName(); } else if (s.equals(def.getTypeName())) { lastFlatContext =
     * r[i].getName(); } } }
     * 
     * // check in the inheritance hierarchy for (int i = 0; i < r.length; i++) { for (String s : r[i].getDefiningAgentClasses()) { for (Frame
     * ancestor : ancestors) { // logger.debug(s+"-----------------"+ancestor.getTypeName()); if (s.equals(ancestor.getTypeName()) &&
     * ((GenericAgentClass) ancestor).getBehaviour() .getDeclaredRLRule(ruleName) != null) { return r[i].getName(); } } } }
     * 
     * // must be or assume an instance-level defined rule return lastFlatContext;
     * 
     * // throw new GSimEngineException("No agent class defining " + ruleName + // " was mapped to an execution context!!");
     * 
     * }
     * 
     * public static String mapRule2Role(RuntimeAgent agent, String ruleName) throws GSimEngineException {
     * 
     * // logger.debug("Trying to map "+ ruleName +" to " + agent.getName());
     * 
     * GenericAgentClass def = (GenericAgentClass) agent.getDefinition(); Frame[] ancestors = agent.getDefinition().getAncestors();
     * RtExecutionContext[] r = agent.getExecutionContexts();
     * 
     * // check for the immediate defining frame for (int i = 0; i < r.length; i++) { logger.debug("Checking for context " + r[i].getName()); for
     * (String s : r[i].getDefiningAgentClasses()) { logger.debug("... defining class " + s); logger.debug("... " + s + " defines rule " + ruleName +
     * ":" + (def.getBehaviour().getDeclaredRule(ruleName)!=null)); if (s.equals(def.getTypeName()) && def.getBehaviour().getDeclaredRule(ruleName) !=
     * null) { return r[i].getName(); } } }
     * 
     * // logger.debug("proceeding to inheritance hierarchy");
     * 
     * // check in the inheritance hierarchy for (int i = 0; i < r.length; i++) { for (String s : r[i].getDefiningAgentClasses()) { logger.debug(
     * "Checking for declaring class " +s); for (Frame ancestor : ancestors) { logger.debug("... testing ancestor "+ ancestor.getTypeName()); if
     * (s.equals(ancestor.getTypeName())) { logger.debug("--> ancestor " + ancestor.getTypeName() + " equals declaring class " + s); logger.debug(
     * "ancestor declares rule " + ruleName +"::"+(((GenericAgentClass)ancestor).getBehaviour().getDeclaredRule(ruleName) != null)); } if
     * (s.equals(ancestor.getTypeName()) && ((GenericAgentClass) ancestor).getBehaviour().getDeclaredRule( ruleName) != null) { //logger.debug(
     * "...ancestor "+ ancestor.getTypeName() + //" declares the rule! --> return"); return r[i].getName(); } } } }
     * 
     * throw new GSimEngineException("No agent class defining " + ruleName + " was mapped to an execution context!!");
     * 
     * }
     */

    public static String getDefiningRoleForRule(RuntimeAgent agent, String ruleName) {

        GenericAgentClass def = (GenericAgentClass) agent.getDefinition();

        if (def.getBehaviour().getDeclaredRule(ruleName) != null) {
            return def.getTypeName();
        }

        Frame[] ancestors = def.getAncestors();
        for (Frame f : ancestors) {
            GenericAgentClass agentClass = (GenericAgentClass) f;
            if (agentClass.getBehaviour().getDeclaredRule(ruleName) != null) {
                return agentClass.getTypeName();
            }
        }
        return "default";
    }

    public static boolean isNumericalAttributeSpec(Instance agent, String attRef) throws GSimEngineException {
        Attribute att = null;
        if (!attRef.contains("::")) {
            att = (Attribute) agent.resolveName(attRef.split("/"));
            if (att instanceof NumericalAttribute || att instanceof IntervalAttribute) {
                return true;
            } else {
                return false;
            }
        } else {
            String[] ref0 = attRef.split("::")[0].split("/");
            String[] ref1 = attRef.split("::")[1].split("/");
            String listName = ref0[0];
            Frame object = agent.getDefinition().getListType(listName);
            if (object != null) {
                DomainAttribute datt = (DomainAttribute) object.resolveName(ref1);
                if (datt.getType().equals(AttributeConstants.NUMERICAL) || datt.getType().equals(AttributeConstants.INTERVAL)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        throw new GSimEngineException("Attribute reference " + attRef + " resolved to null!");
    }

    public static String resolveAttribute(String s) {
        // boolean b = false;
        if (s.contains("::")) {
            s = s.split("::")[1];
            // b = true;
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

    public static String resolveList(String s) {
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

    public static String resolveObjectClass(String s) {
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

    public static String resolveObjectClassNoList(String s) {
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
        return object;

    }

}
