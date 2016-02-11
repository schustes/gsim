package de.s2.gsim.sim.agent;

import java.util.HashMap;

import de.s2.gsim.sim.communication.Messenger;
import de.s2.gsim.sim.engine.ModelState;

public interface ApplicationAgent {

    ModelState getCoordinatorRef();

    Messenger getMessengerRef();

    String getName();

    String getNameSpace();

    boolean isAgent();

    void post();

    void pre(HashMap<String, Object> simProps);

}