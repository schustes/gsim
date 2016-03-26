package de.s2.gsim.api.sim.agent.impl;

import java.sql.Connection;

import de.s2.gsim.objects.AppAgent;
import de.s2.gsim.sim.Simulation;
import de.s2.gsim.sim.agent.ApplicationAgent;
import de.s2.gsim.sim.communication.AgentType;
import de.s2.gsim.sim.communication.Messenger;

public abstract class ApplicationAgentImpl implements AgentType, AppAgent, ApplicationAgent {

    private transient Messenger messenger = null;

    private transient Simulation model = null;

    private String name;

    private String ns = "unknown";

    public void destroy() {
        if (messenger != null) {
            messenger.destroy();
            messenger = null;
        }
    }

    @Override
    public final Simulation getSimulation() {
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

    public abstract void save(Connection con);

    public void setCoordinatorRef(Simulation m) {
        model = m;
    }

    public void setMessengerRef(Messenger m) {
        messenger = m;
    }

    public void setName(String name) {
        this.name = name;
    }

}
