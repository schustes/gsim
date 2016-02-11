package de.s2.gsim.sim.engine;

import org.apache.log4j.Logger;

import cern.jet.random.Uniform;

public class SimulationID implements java.io.Serializable {

    private static Logger logger = Logger.getLogger(SimulationID.class);

    private static final long serialVersionUID = 1L;

    private String ns = "";

    private long uid = -1;

    public SimulationID(String ns) {
        this.ns = ns;
        uid = Uniform.staticNextLongFromTo(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public SimulationID(String ns, long id) {
        this.ns = ns;
        uid = id;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SimulationID) {
            SimulationID o = (SimulationID) other;
            return (o.uid == uid && o.ns.equals(ns));
        }
        return false;
    }

    public String getGlobalNS() {
        return ns;
    }

    @Override
    public int hashCode() {
        return 12;
    }

    @Override
    public String toString() {
        return String.valueOf(ns + "/" + uid);
    }

    public static SimulationID valueOf(String s) {
        String[] p = s.split("/");
        if (p.length == 2) {
            String ns = p[0];
            // String ns = p[0].trim();
            long id = Long.parseLong(p[1].trim());
            return new SimulationID(ns, id);
        } else {
            logger.error("SimulationID string " + s + " has not correct format!!!");
            return null;
        }
    }
}
