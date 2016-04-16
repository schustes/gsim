package de.s2.gsim.sim.behaviour.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Attribute2ValuesMap {

    private HashMap<String, double[]> intervalAttributes = new HashMap<String, double[]>();

    private HashMap<String, List<String>> setAttributes = new HashMap<>();

    public List<String> getFillers(String setAttName) {
        return setAttributes.get(setAttName);
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

    public void setSetAttributes(String name, List<String> fillers) {
        setAttributes.put(name, fillers);
    }

}
