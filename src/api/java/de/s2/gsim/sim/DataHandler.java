package de.s2.gsim.sim;

import java.io.Serializable;

/**
 * A DataHandler allows to plugin custom handlers into the simulation to save data from the simulation.
 * 
 * @author stephan
 *
 */
public interface DataHandler extends Serializable {

    /**
     * This mehtods gets called by the simulation engine and passes the current state of the simulation.
     * 
     * @param simulation the simulation in its latest state
     */
    void save(Simulation simulation);

}
