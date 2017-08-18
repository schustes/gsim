package de.s2.gsim.sim;

import java.io.Serializable;

import cern.jet.random.Uniform;

/**
 * A SimulationId identifies a single simulation instance. It is composed out of a name space that defines the simulation and a unique instance id.
 * While the name space groups all instances of a simulation, the instance id allows to identify single instances. This is necessary if a simulation
 * is started with several parallel repetions.
 * 
 * The string representation of the id is ns/uid, e.g. prisoners-dilemma/8883812312
 * 
 * @author stephan
 *
 */
public class SimulationId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ns = "";

    private long uid = -1;

    /**
     * Constructor.
     * 
     * @param ns the name space
     */
    public SimulationId(String ns) {
        this.ns = ns;
        uid = Uniform.staticNextLongFromTo(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Constructor.
     * 
     * @param ns the name space
     * @param id the id
     */
    public SimulationId(String ns, long id) {
        this.ns = ns;
        uid = id;
    }

    /**
     * Gets the name space
     * 
     * @return the name space string
     */
    public String getNamespace() {
        return ns;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SimulationId) {
            SimulationId o = (SimulationId) other;
            return (o.uid == uid && o.ns.equals(ns));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ns.hashCode() + (int) uid;
    }

    @Override
    public String toString() {
        return String.valueOf(ns + "_" + uid);
    }

    /**
     * Parses the given string and tries to create a SimulationId from it.
     * 
     * The excepted format is ns_uid, e.g. prisoners-dilemma/8883812312 Throws an exception if the format is not correct.
     * 
     * @param idString
     * @return the SimulationId
     */
    public static SimulationId valueOf(String idString) {
        String[] p = idString.split("_");
        if (p.length == 2) {
            String ns = p[0];
            long id = Long.parseLong(p[1].trim());
            return new SimulationId(ns, id);
        }
        throw new GSimEngineException(String.format("SimulationId %s has wrong format!", idString));
    }
}
