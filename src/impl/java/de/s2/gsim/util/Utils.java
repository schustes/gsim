package de.s2.gsim.util;

import java.util.ArrayList;
import java.util.HashSet;

import de.s2.gsim.def.objects.Frame;
import de.s2.gsim.def.objects.Instance;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.objects.attribute.NumericalAttribute;

public class Utils {

    public static Object[] addToArray(Object[] o, Object[] newArray, Object n) {
        if (n != null) {
            if (!contains(o, n)) {
                // Object[] newArray = new Object[o.length+1];
                for (int i = 0; i < o.length; i++) {
                    newArray[i] = o[i];
                }
                newArray[newArray.length - 1] = n;
                return newArray;
            } else {
                return o;
            }
        }
        return o;

    }

    public static Object[] addToArray(Object[] o, Object[] newArray, Object[] n) {

        for (int i = 0; i < o.length; i++) {
            newArray[i] = o[i];
        }

        for (int i = 0; i < n.length; i++) {
            newArray[o.length + i] = n[i];
        }

        return newArray;

    }

    public static String[] addToArray(String[] o, String n) {

        if (!contains(o, n)) {
            String[] newArray = new String[o.length + 1];
            for (int i = 0; i < o.length; i++) {
                newArray[i] = o[i];
            }
            newArray[newArray.length - 1] = n;
            return newArray;
        } else {
            return o;
        }
    }

    public static boolean contains(Object[] o, Object n) {
        for (int i = 0; i < o.length; i++) {
            if (o[i].equals(n)) {
                return true;
            }
        }
        return false;
    }

    public static java.util.List drawRandomSample(ArrayList list, int n) {
        int randomStartNumber = cern.jet.random.Uniform.staticNextIntFromTo(0, list.size() - 1);
        boolean[] visited = new boolean[list.size()];

        ArrayList sample = new ArrayList();

        int interval = (int) ((double) list.size() / n);
        int modified = 0;
        int c = randomStartNumber;
        boolean first = true;
        while (modified < n && interval > 0) {
            if (first && !visited[c]) {
                Object s = list.get(c);
                sample.add(s);
                visited[c] = true;
                first = false;
            } else if (first && visited[c]) {
                first = false;
            } else if (c % interval == 0 && !first) {
                if (visited[c]) {
                    boolean b = false;
                    for (int k = c; k < visited.length && !b; k++) {
                        if (!visited[k]) {
                            c = k;
                            b = true;
                        }
                    }
                    if (!b) {
                        boolean b1 = false;
                        for (int k = 0; k <= c && !b1; k++) {
                            if (!visited[k]) {
                                c = k;
                                b1 = true;
                            }
                        }
                    }
                }
                Object s = list.get(c);
                sample.add(s);
                modified++;
                visited[c] = true;
            }

            c++;
            if (c >= list.size()) {
                c = 0;
            }
        }

        if (randomStartNumber < list.size() - 1) {
            randomStartNumber++;
        } else {
            randomStartNumber = 0;
        }

        return sample;

    }

    public static boolean equalArrays(Object[] o1, Object[] o2) {

        if (o1.length != o2.length) {
            return false;
        }

        for (int i = 0; i < o1.length; i++) {
            boolean b = false;
            for (int j = 0; j < o2.length; j++) {
                if (o1[i].equals(o2[j])) {
                    b = true;
                }
            }
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public static Instance[] getChildInstancesOfType(Instance agent, String type) {
        HashSet set = new HashSet();
        String[] path = type.split("/");
        String listName = path[0];

        String className = path[1];

        if (agent.getChildInstances(listName) == null) {
            return new Instance[0];
        }

        for (Instance obj : agent.getChildInstances(listName)) {
            if (obj.inheritsFrom(className)) {
                set.add(obj);
            }
        }
        Instance[] res = new Instance[set.size()];
        set.toArray(res);
        return res;

    }

    /**
     * Checks if the condition represented in the first three parameters includes logically those represented in the latter parameters. Inclusion
     * means that for any value of val2 the first condition is always true.
     */
    public static boolean includes(String var1, String op1, double val1, String var2, String op2, double val2) {

        if (!var1.equals(var2)) {
            return false;
        }

        boolean b = true;

        if (op1.equals("=")) {
            if (op2.equals("=")) {
                if (val1 != val2) {
                    b = false;
                }
            } else {
                b = false;
            }
        } else if (op1.equals(">")) {
            if (op2.equals("<")) {
                b = false; // if (val1>=val2) b= false;//ok
            } else if (op2.equals("<=")) {
                b = false; // if (val1>=val2) b= false;//ok
            } else if (op2.equals("=")) {
                if (val1 >= val2) {
                    b = false; // ok
                }
            } else if (op2.equals(">=")) {
                if (val1 <= val2) {
                    b = false;
                }
            } else if (op2.equals(">")) {
                if (val1 < val2) {
                    b = false;
                }
            }

        } else if (op1.equals("<")) {

            if (op2.equals(">")) {
                b = false; // if (val1<=val2) b= false;
            } else if (op2.equals(">=")) {
                b = false; // if (val1<=val2) b=false;//ok
            } else if (op2.equals("=")) {
                if (val1 <= val2) {
                    b = false; // ok
                }
            } else if (op2.equals("<=")) {
                if (val1 <= val2) {
                    b = false;
                }
            } else if (op2.equals("<")) {
                if (val1 < val2) {
                    b = false;
                }
            }

        } else if (op1.equals(">=")) {
            if (op2.equals("<")) {
                b = false; // if (val1>=val2) b= false;//ok
            } else if (op2.equals("<=")) {
                b = false; // if (val1>=val2) b= false;//ok
            } else if (op2.equals("=")) {
                if (val1 > val2) {
                    b = false; // ok
                }
            } else if (op2.equals(">=")) {
                if (val1 != val2) {
                    b = false;
                }
            } else if (op2.equals(">")) {
                if (val1 > val2) {
                    b = false;
                }
            }

        } else if (op1.equals("<=")) {

            if (op2.equals(">")) {
                b = false; // if (val1<=val1) b = false;//ok
            } else if (op2.equals(">=")) {
                b = false; // if (val1<=val2) b=false;//ok
            } else if (op2.equals("=")) {
                if (val1 < val2) {
                    b = false; // ok
                }
            } else if (op2.equals("<=")) {
                if (val1 != val2) {
                    b = false;
                }
            } else if (op2.equals("<")) {
                if (val1 < val2) {
                    b = false;
                }
            }
        }
        return b;
    }

    /**
     * Checks if the condition represented in the first three parameters intersects with those represented in the latter. Intersection means that
     * there is the possibility that for some values of a variable both conditons may hold.
     */
    public static boolean intersects(String var1, String op1, double val1, String var2, String op2, double val2) {

        if (!var1.equals(var2)) {
            return false;
        }

        boolean b = false;

        if (op1.equals("=")) {
            if (op2.equals(">=")) {
                if (val1 == val2) {
                    b = true;
                }
            } else if (op2.equals("<=")) {
                if (val2 == val1) {
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val2 == val1) {
                    b = true;
                }
            }
        } else if (op1.equals(">")) {
            if (op2.equals("<")) {
                if (val1 < val2) {
                    b = true;
                }
            } else if (op2.equals("<=")) {
                if (val1 <= val2) {
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val1 < val2) {
                    b = true;
                }
            }
        } else if (op1.equals("<")) {

            if (op2.equals(">")) {
                if (val1 > val2) {
                    b = true;
                }
            } else if (op2.equals(">=")) {
                if (val1 >= val2) {
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val1 == val2) {
                    b = true;
                }
            }

        } else if (op1.equals(">=")) {
            if (op2.equals("<")) {
                if (val1 <= val2) {
                    b = true;
                }
            } else if (op2.equals("<=")) {
                if (val1 <= val2) {
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val1 == val2) {
                    b = true;
                }
            }

        } else if (op1.equals("<=")) {

            if (op2.equals(">")) {
                if (val1 >= val1) {
                    b = true;
                }
            } else if (op2.equals(">=")) {
                if (val1 >= val2) {
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val1 == val2) {
                    b = true;
                }
            }
        }
        return b;
    }

    /**
     * Checks if the condition represented in the first three parameters is such that for any values of the condition represented in the latter
     * parameters, the evaluation would always evaluate to false.
     */
    public static boolean isAlwayFalse(String var1, String op1, double val1, String var2, String op2, double val2) {

        boolean b = false;

        if (op1.equals(">")) {
            if (op2.equals("<")) {
                if (val1 >= val2) {
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val1 <= val2) {
                    b = true;
                }
            } else if (op2.equals("<=")) {
                if (val1 > val2) {
                    b = true;
                }
            }
        } else if (op1.equals(">=")) {
            if (op2.equals("<")) {
                if (val1 >= val2) {
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val1 != val2) {
                    b = true;
                }
            } else if (op2.equals("<=")) {
                if (val1 > val2) {
                    // if (val1 != val2)
                    b = true;
                }
            }

        } else if (op1.equals("<")) {
            if (op2.equals(">")) {
                if (val1 <= val2) {
                    b = true;
                }
            } else if (op2.equals(">=")) {
                if (val1 != val2) {
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val1 <= val2) {
                    b = true;
                }
            }
        } else if (op1.equals("<=")) {
            if (op2.equals(">")) {
                if (val1 <= val2) {
                    b = true;
                }
            } else if (op2.equals(">=")) {
                if (val1 < val2) {
                    // if (val1 != val2)
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val1 != val2) {
                    b = true;
                }
            }

        } else if (op1.equals("=")) {

            if (op2.equals("<")) {
                if (val1 >= val2) {
                    b = true;
                }
            } else if (op2.equals(">")) {
                if (val1 <= val2) {
                    b = true;
                }
            } else if (op2.equals(">=")) {
                if (val1 != val2) {
                    b = true;
                }
            } else if (op2.equals("<=")) {
                if (val1 != val2) {
                    b = true;
                }
            } else if (op2.equals("=")) {
                if (val1 != val2) {
                    b = true;
                }
            }

        }

        return b;

    }

    public static boolean isNumerical(String s) {
        try {
            double d = Double.valueOf(s).doubleValue();
            return true;
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static boolean isNumericalAttribute(Instance obj, String pathToAtt) {
        Attribute a = (Attribute) obj.resolveName(pathToAtt.split("/"));
        return (a instanceof NumericalAttribute);
    }

    public static double numericalValue(AgentInstance in, String list, String att) {
        try {
            Attribute attribute = in.getAttribute(list, att);
            if (att != null && attribute instanceof NumericalAttribute) {
                NumericalAttribute a = (NumericalAttribute) attribute;
                return a.getValue();
            } else if (att != null && attribute instanceof IntervalAttribute) {
                IntervalAttribute a = (IntervalAttribute) attribute;
                return (a.getFrom() + a.getTo()) / 2d;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static double numericalValue(Instance in, String att) {
        Attribute attribute = in.getAttribute(att);
        if (att != null && attribute instanceof NumericalAttribute) {
            NumericalAttribute a = (NumericalAttribute) attribute;
            return a.getValue();
        } else if (att != null && attribute instanceof IntervalAttribute) {
            IntervalAttribute a = (IntervalAttribute) attribute;
            return (a.getFrom() + a.getTo()) / 2d;
        } else {
            return -1;
        }

    }

    public static double numericalValue(Instance in, String list, String att) {
        Attribute attribute = in.getAttribute(list, att);
        if (att != null && attribute instanceof NumericalAttribute) {
            NumericalAttribute a = (NumericalAttribute) attribute;
            return a.getValue();
        } else if (att != null && attribute instanceof IntervalAttribute) {
            IntervalAttribute a = (IntervalAttribute) attribute;
            return (a.getFrom() + a.getTo()) / 2d;
        } else {
            return -1;
        }

    }

    public static String printArray(String[] array) {
        String result = "";
        for (int i = 0; i < array.length; i++) {
            result += array[i];
            if (i < array.length - 1) {
                result += ",";
            }
        }
        return result;
    }

    public static String[] removeFromArray(String[] o, String n) {

        if (contains(o, n)) {
            String[] newArray = new String[o.length - 1];
            int j = 0;
            for (int i = 0; i < o.length; i++) {
                if (!o[i].equals(n)) {
                    newArray[j] = o[i];
                    j++;
                }
            }
            return newArray;
        } else {
            return o;
        }

    }

    public static void shuffle(java.util.List list) {
        for (int i = list.size(); i > 1; i--) {
            // logger.debug("shuffle");
            swap(list, i - 1, cern.jet.random.Uniform.staticNextIntFromTo(0, i - 1));
        }
    }

    public static String toInstPath(Instance agent, String framePath, String instName) {
        String[] p = framePath.split("/");
        for (int i = 0; i < p.length; i++) {
            p[i] = p[i].trim();
        }

        Frame def = (Frame) agent.getDefinition().resolveName(p);

        if (def == null) {
            def = agent.getDefinition().getListType(p[0]);
        }

        int pos = -1;
        for (int i = 0; i < p.length; i++) {
            if (def.isSuccessor(p[i]) || p[i].equals(def.getTypeName())) {
                pos = i - 1;
            }
        }
        String prefix = "";
        for (int i = 0; i <= pos; i++) {
            prefix += p[i];
            if (i < pos) {
                prefix += "/";
            }
        }
        String postfix = "";
        for (int i = pos + 2; i < p.length; i++) {
            postfix += p[i];
            if (i < p.length - 1) {
                postfix += "/";
            }
        }

        if (postfix.length() > 0) {
            return prefix + "/" + instName + "/" + postfix;
        }
        return prefix + "/" + instName;

    }

    private static void swap(java.util.List list, int i, int j) {
        Object tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

}
