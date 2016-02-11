package gsim.sim.agent;

import java.sql.Connection;
import java.util.HashMap;

import de.s2.gsim.objects.AppAgent;
import de.s2.gsim.sim.agent.ApplicationAgent;
import de.s2.gsim.sim.communication.AgentType;
import de.s2.gsim.sim.communication.Messenger;
import de.s2.gsim.sim.engine.ModelState;

public abstract class ApplicationAgentImpl implements AgentType, AppAgent, ApplicationAgent {

    private transient Messenger messenger = null;

    private transient ModelState model = null;

    private String name;

    private String ns = "unknown";

    public void destroy() {
        if (messenger != null) {
            messenger.destroy();
            messenger = null;
        }
    }

    @Override
    public final ModelState getCoordinatorRef() {
        return model;
    }

    @Override
    public final Messenger getMessengerRef() {
        return messenger;
    }

    @Override
    public String getName() {
        if (name == null) {
            return getClass().getSimpleName();
        }
        return name;
    }

    @Override
    public final String getNameSpace() {
        try {
            if (model != null) {
                return model.getNameSpace();
            }
            return ns;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isAgent() {
        return false;
    }

    @Override
    public abstract void post();

    @Override
    public abstract void pre(HashMap<String, Object> simProps);

    public abstract void save(Connection con);

    public void setCoordinatorRef(ModelState m) {
        model = m;
    }

    public void setMessengerRef(Messenger m) {
        messenger = m;
    }

    public void setName(String name) {
        this.name = name;
    }

}
