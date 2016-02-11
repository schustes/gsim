package gsim.sim.behaviour.impl;

import java.util.Collection;
import java.util.HashMap;

public class Attribute2ValuesMap {

    private HashMap<String, double[]> intervalAttributes = new HashMap<String, double[]>();

    private HashMap<String, String[]> setAttributes = new HashMap<String, String[]>();

    public String[] getFillers(String setAttName) {
        String[] s = setAttributes.get(setAttName);
        // logger.debug("////"+s);
        if (s == null) {
            return new String[0];
        }
        return s;
    }

    public double[] getInterval(String numAttName) {
        return intervalAttributes.get(numAttName);
    }

    public Collection<String> getIntervalAttributes() {
        return intervalAttributes.keySet();
    }

    public Collection<String> getSetAttributes() {
        return setAttributes.keySet();
    }

    public void setIntervalAttributes(String name, double from, double to) {
        intervalAttributes.put(name, new double[] { from, to });
    }

    public void setSetAttributes(String name, String... fillers) {
        setAttributes.put(name, fillers);
    }

}
