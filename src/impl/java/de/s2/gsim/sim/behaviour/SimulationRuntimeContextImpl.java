package de.s2.gsim.sim.behaviour;

import de.s2.gsim.api.objects.impl.AgentInstanceSim;
import de.s2.gsim.api.sim.agent.impl.RtExecutionContextImpl;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.sim.communication.Communicator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

public class SimulationRuntimeContextImpl implements Serializable, SimulationRuntimeContext {

    private static final long serialVersionUID = 1L;

    private RtExecutionContextImpl exCtx = null;

    private HashMap<Integer, String> objects = new HashMap<Integer, String>();

    private int sigCounter = 0;

    public SimulationRuntimeContextImpl() {
    }

    public void addObject(String object) {
        objects.put(sigCounter, object);
        sigCounter++;
    }

    public AgentInstance getAgent() {
        return new AgentInstanceSim(exCtx.getOwner());
    }

    RuntimeAgent getAgentInternal() {
        return exCtx.getOwner();
    }

    @Override
    public Communicator getCommunicator() {
        return exCtx.getOwner().getMessagingComponent();
    }

    public String[] getParameters() {
        String[] res = new String[objects.size()];
        objects.values().toArray(res);
        return res;
    }

    @Override
    public Optional<String> getLastAction() {
        return exCtx.getOwner().getLastAction();
    }

    public void setAgent(Object p) {
        exCtx = (RtExecutionContextImpl) p;
    }

    public void setExecutionContext(RtExecutionContextImpl c) {
        exCtx = c;
    }

}
