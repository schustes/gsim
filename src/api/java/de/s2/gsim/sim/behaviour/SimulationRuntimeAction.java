package de.s2.gsim.sim.behaviour;

import java.io.Serializable;

public interface SimulationRuntimeAction extends Serializable  {

    /**
     * Returns the name of the action.
     *
     * @return the name
     */
    String getName();

    /**
     * Called by the simulation engine when the action of the agent is executed. The agent is referenced in the context
     * object.
     *
     * @param context the runtime context containing information about the state of the agent being executed
     */
	void execute(SimulationRuntimeContext context);
}
