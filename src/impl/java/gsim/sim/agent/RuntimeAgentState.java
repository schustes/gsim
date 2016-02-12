package gsim.sim.agent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.sim.agent.AgentState;
import gsim.def.objects.TypedList;
import gsim.def.objects.Unit;

public class RuntimeAgentState implements Serializable, AgentState {

    private static final long serialVersionUID = 1L;

    private String lastAction = "NOTHING";

    private RuntimeAgent r;

    private HashMap<String, RtExecutionContextImpl> roles = new HashMap<String, RtExecutionContextImpl>();

    public RuntimeAgentState(RuntimeAgent a) {
        this.r = a;
        roles = a.getRoles();
        if (a.getCurrentStrategy() != null && a.getCurrentStrategy().length > 0) {
            lastAction = a.getCurrentStrategy()[0];
        }
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (!(o instanceof RuntimeAgentState)) {
            return false;
        }

        RuntimeAgentState other = (RuntimeAgentState) o;
        return (other.r.getName().equals(this.r.getName()));
    }

    @Override
    public Attribute getAgentAttribute(String attName) {
        for (String list : r.getAttributesListNames()) {
            Attribute b = r.getAttribute(list, attName);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    @Override
    public RtExecutionContextImpl getExecutionContext(String name) {
        return roles.get(name);
    }

    @Override
    public String[] getExecutionContextNames() {
        String[] r = new String[roles.size()];
        roles.keySet().toArray(r);
        return r;
    }

    @Override
    public RtExecutionContextImpl[] getExecutionContexts() {
        RtExecutionContextImpl[] r = new RtExecutionContextImpl[roles.size()];
        roles.values().toArray(r);
        return r;
    }

    @Override
    public String getLastAction() {
        return lastAction;
    }

    @Override
    public int hashCode() {
        return r.getName().hashCode();
    }

    @Override
    public String getAgentName() {
        return r.getName();
    }

    @Override
    public String[] getAgentObjectListNames() {
        return r.getObjectLists().keySet().toArray(new String[0]);
    }

    @Override
    public List<ObjectInstance> getAgentObjects(String list) {
        TypedList tlist = r.getObjectLists().get(list);
        List<ObjectInstance> ret = new ArrayList<ObjectInstance>();
        for (Unit unit : tlist) {
            if (unit instanceof ObjectInstance) {
                ret.add((ObjectInstance) unit);
            }
        }
        return ret;
    }

    @Override
    public String[] getAgentAttributesListNames() {
        return r.getAttributesListNames();
    }

    @Override
    public Attribute[] getAgentAttributes(String list) {
        return r.getAttributes(list);
    }

}
