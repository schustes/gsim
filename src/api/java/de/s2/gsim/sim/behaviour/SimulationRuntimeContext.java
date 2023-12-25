package de.s2.gsim.sim.behaviour;

import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.sim.communication.Communicator;

import java.util.Optional;

public interface SimulationRuntimeContext  {

    /**
     * Returns the agent that is being executed.
     *
     * @return the agent instance
     */
    AgentInstance getAgent();

    /**
     * Returns the communcation component that can be used to talk to other agents.
     *
     * @return the Communicator instance
     */
    Communicator getCommunicator();

    /**
     * Returns the name of the objects that are passed as parameters to this action.
     *
     * @return array of object names
     */
    String[] getParameters();

    /**
     * Returns the agent's action of the previous round, if any was executed
     * @return an optional with the possibly executed action.
     */
    Optional<String> getLastAction();
}
