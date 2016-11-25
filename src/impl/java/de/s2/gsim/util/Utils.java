package de.s2.gsim.util;

import java.util.HashSet;

import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.Instance;
import de.s2.gsim.objects.Path;

public class Utils {

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

    @SuppressWarnings("rawtypes")
	public static Instance[] getChildInstancesOfType(Instance agent, String type) {
        HashSet set = new HashSet();
        String[] path = type.split("/");
        String listName = path[0];

        String className = path[1];

        if (agent.getChildInstances(listName) == null) {
            return new Instance[0];
        }

        for (Instance obj : agent.getChildInstances(listName)) {
            if (obj.inheritsFromOrIsOfType(className)) {
                set.add(obj);
            }
        }
        Instance[] res = new Instance[set.size()];
        set.toArray(res);
        return res;

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

    public static String toInstPath(Instance agent, String framePath, String instName) {
        String[] p = framePath.split("/");
        for (int i = 0; i < p.length; i++) {
            p[i] = p[i].trim();
        }

        Frame def = agent.getDefinition().resolvePath(Path.objectPath(p));

        if (def == null) {
            def = agent.getDefinition().getListType(p[0]);
        }

        int pos = -1;
        for (int i = 0; i < p.length; i++) {
            if (def.isSuccessor(p[i]) || p[i].equals(def.getName())) {
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

}
