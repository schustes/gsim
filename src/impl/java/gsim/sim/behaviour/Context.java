package gsim.sim.behaviour;

import java.util.HashMap;

import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.sim.communication.CommunicationProtocol;
import de.s2.gsim.sim.engine.GSimEngineException;
import gsim.objects.impl.AgentInstanceSim;
import gsim.sim.agent.RtExecutionContextImpl;
import gsim.sim.agent.RuntimeAgent;

public class Context implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private RtExecutionContextImpl exCtx = null;

    private HashMap<Integer, String> objects = new HashMap<Integer, String>();

    private int sigCounter = 0;

    public Context() {
    }

    public void addObject(String object) {
        objects.put(sigCounter, object);
        sigCounter++;
    }

    public RuntimeAgent getAgent() {
        return exCtx.getOwner();
    }

    public AgentInstance getAgentIF() {
        return new AgentInstanceSim(exCtx.getOwner());
    }

    public RtExecutionContextImpl getExcecutionContext() {
        return exCtx;
    }

    public String getObject(int pos) {
        return objects.get(pos);
    }

    public String[] getObjects() {
        String[] res = new String[objects.size()];
        objects.values().toArray(res);
        return res;
    }

    public void setAgent(Object p) {
        exCtx = (RtExecutionContextImpl) p;
    }

    public void setExecutionContext(RtExecutionContextImpl c) {
        exCtx = c;
    }

    public void startConversation(CommunicationProtocol p) throws GSimEngineException {
        getAgent().getMessagingComponent().createConversation(p).start();
    }

}
