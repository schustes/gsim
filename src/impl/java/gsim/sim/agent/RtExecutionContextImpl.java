package gsim.sim.agent;

import java.io.Serializable;
import java.util.ArrayList;

import de.s2.gsim.sim.agent.RtExecutionContext;

public class RtExecutionContextImpl implements Serializable, RtExecutionContext {

    private static final long serialVersionUID = 1L;

    private ArrayList<String> definingAgentClasses = new ArrayList<String>();

    private String name;

    private transient RuntimeAgent owner = null;

    public void addDefiningAgentClass(String name) {
        definingAgentClasses.add(name);
    }

    public void create(String name, RuntimeAgent owner, String definingAgentClass) {
        this.name = name;
        this.owner = owner;
        definingAgentClasses.add(definingAgentClass);
    }

    @Override
    public String[] getDefiningAgentClasses() {
        String[] s = new String[definingAgentClasses.size()];
        definingAgentClasses.toArray(s);
        return s;
    }

    @Override
    public String getName() {
        return name;
    }

    public RuntimeAgent getOwner() {
        return owner;
    }

    public void setOwner(RuntimeAgent a) {
        owner = a;
    }

}
