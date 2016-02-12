package de.s2.gsim.sim.engine;

import java.util.List;

import de.s2.gsim.core.ModelDefinitionEnvironment;
import de.s2.gsim.objects.AppAgent;
import de.s2.gsim.sim.agent.AgentState;

/**
 * Interface for the minimal requirements for interaction between state and ScenarioManager. In the EJB implementation for example, there exists
 * actually not state-object; the agents live distributed in the EJB container and can only be contacted by JMS. Thus, a EJB coordinator will have to
 * handle the 'synchronization of an asynchronous system' to provide applications with a unified view on the state of the whole simulation. On the
 * other hand, a local implementation could store agents in a certain object, and can simply call them by their references.
 *
 */
public interface ModelState {

    // void addAgent(RuntimeAgent agent) throws GSimEngineException;

    String addNewAgentToRunningModel(String agentClass, String name, int method, double svar) throws GSimEngineException, Exception;

    void disconnect() throws GSimEngineException;

    int getAgentCount() throws GSimEngineException;

    String[] getAgentNames() throws GSimEngineException;

    AgentState getAgentState(String agentName) throws GSimEngineException;

    AppAgent[] getAppAgentState() throws GSimEngineException;

    AppAgent getAppAgentState(String name) throws GSimEngineException;

    DataHandler getDataHandler(String name) throws GSimEngineException;

    DataHandler[] getDataHandlers() throws GSimEngineException;

    ModelDefinitionEnvironment getDefinitionEnvironment() throws GSimEngineException;

    List<AgentState> getGlobalState() throws GSimEngineException;

    List<AgentState> getGlobalState(int count, int offset) throws GSimEngineException;

    String getNameSpace() throws GSimEngineException;

    void modifyAgentState(AgentState state) throws GSimEngineException;

    void removeAgent(String agentName) throws GSimEngineException;

    void setNameSpace(String ns) throws GSimEngineException;

}
