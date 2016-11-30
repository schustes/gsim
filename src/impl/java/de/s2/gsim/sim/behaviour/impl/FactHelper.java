package de.s2.gsim.sim.behaviour.impl;

import de.s2.gsim.sim.behaviour.GSimBehaviourException;
import jess.Context;
import jess.Fact;
import jess.JessException;

public class FactHelper {
    public static double getFloatSlotValue(Fact f, String slotName, Context context) {
        try {
            return f.getSlotValue("active").floatValue(context);
        } catch (JessException e) {
            throw new GSimBehaviourException("Jess exception", e);
        }

    }

    public static String getStringSlotValue(Fact f, String slotName, Context context) {
        try {
            return f.getSlotValue("active").stringValue(context);
        } catch (JessException e) {
            throw new GSimBehaviourException("Jess exception", e);
        }

    }
}
