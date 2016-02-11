package de.s2.gsim.sim.engine;

import java.io.Serializable;
import java.sql.Connection;

import de.s2.gsim.sim.engine.Saveable;

public interface DataHandler extends Serializable {

    /**
     * public String getName();
     * 
     * public void setName(String name);
     */

    public void save(Saveable s, Connection con);

}
