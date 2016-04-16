package de.s2.gsim.sim.behaviour.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RLParameterRanges implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private HashMap<String, ArrayList<String>> categories;

    private HashMap<String, double[]> intervals;

    public RLParameterRanges() {
        intervals = new HashMap<String, double[]>();
        categories = new HashMap<String, ArrayList<String>>();
    }

    public ArrayList<String> getNewCategoricalParameterValues(String attName, List<String> allFillers) {
        ArrayList<String> contained = categories.get(attName);
        ArrayList<String> result = new ArrayList<String>();
        if (contained == null) {
            ArrayList<String> list = new ArrayList<String>();
            for (String s : allFillers) {
                list.add(s);
            }
            categories.put(attName, list);
        } else {

            for (String s : allFillers) {
                boolean exists = false;
                for (String t : contained) {
                    if (t.equals(s)) {
                        exists = true;
                    }
                }
                if (!exists) {
                    contained.add(s);
                    result.add(s);
                }
            }
        }
        return result;
    }

    /**
     * 
     * @param attName
     * @param currentInterval
     * @return null if there are no updates for the interval
     */
    public double[] getNewIntervalParameterRange(String attName, double[] currentInterval) {

        double[] contained = intervals.get(attName);
        double[] result = null;
        if (contained == null) {
            intervals.put(attName, currentInterval);
        } else {
            boolean changed = false;
            if (contained[0] > currentInterval[0]) {
                contained[0] = currentInterval[0];
                changed = true;
            }

            if (contained[1] < currentInterval[1]) {
                contained[1] = currentInterval[1];
                changed = true;
            }

            if (changed) {
                result = contained;
            }
        }
        return result;

    }

    public void initCategoricalParameters(String attName, List<String> values) {
        ArrayList<String> list = new ArrayList<String>();
        for (String s : values) {
            list.add(s);
        }
        categories.put(attName, list);
    }

    public void initIntervalParameterRange(String attName, double[] values) {
        intervals.put(attName, values);
    }

}
