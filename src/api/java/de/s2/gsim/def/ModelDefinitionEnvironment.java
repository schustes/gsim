package de.s2.gsim.def;

import de.s2.gsim.GSimException;
import de.s2.gsim.objects.AgentClass;
import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.ObjectInstance;
import de.s2.gsim.sim.agent.ApplicationAgent;

/**
 * The <code>ModelDefinitionEnvironment</code> is a wrapper for the actual gsim environment managing agents and objects. Agents are instanciated by
 * defining the Agent class to inherit from and some generation parameters or by creating single agents. The process of instanciation denotes the
 * creation of instances from a parent frame (similar, but not identical to object instantiation). The parent frame acts as a template which defines
 * properties and default values.
 * 
 * Note: Names are unique identifiers. When creating single objects or agents, it must be made sure that the name does not exist yet.
 *
 * @author Stephan
 *
 */
public interface ModelDefinitionEnvironment {

    /**
     * Constant for creating random attributeDistribution and weights (weights are only for fuzzy matching utility) during instanciation using a normal
     * distribution.
     */
    static final int RAND_ATT_AND_WEIGHT = 1;

    /**
     * Constant for creating random attributeDistribution and weights (weights are only for fuzzy matching utility) during instanciation using a uniform
     * distribution.
     */
    static final int RAND_ATT_AND_WEIGHT_UNIFORM = 5;

    /**
     * Constant for creating only random attributeDistribution during instanciation using a normal distribution.
     */
    static final int RAND_ATT_ONLY = 2;

    /**
     * Constant for creating random attributeDistribution during instanciation using a uniform distribution.
     */
    static final int RAND_ATT_ONLY_UNIFORM = 6;

    /**
     * Constant specifying that no attributeDistribution should be varied during instanciation.
     */
    static final int RAND_NONE = 4;

    /**
     * Constant for creating random weights (only for fuzzy matching utility) during instanciation using a normal distribution.
     */
    static final int RAND_WEIGHT_ONLY = 3;

    /**
     * Constant for creating random weights (only for fuzzy matching utility) during instanciation using a uniform distribution.
     */
    static final int RAND_WEIGHT_ONLY_UNIFORM = 7;

    /**
     * Creates an AgentClass.
     * 
     * @param name name of the agent type
     * @param parentName name of parent agent type (null if this is the top frame)
     * @return AgentClassIF
     * @throws GSimException
     */
    AgentClass createAgentClass(String name, String parentName) throws GSimException;

    /**
     * Creates an AgentClass and sets the execution order, i.e. the position in which this agent is executed.
     * 
     * @param name name of the agent type
     * @param parentName name of parent agent type (null if this is the top frame)
     * @param order execution order position
     * @return AgentClassIF
     * @throws GSimException
     */
    AgentClass createAgentClass(String name, String parentName, int order) throws GSimException;

    /**
     * Creates an ObjectClass.
     * 
     * @param name name of the object type
     * @param parent parent of the object type (nulli if parent is top frame)
     * @return ObjectClassIF
     * @throws GSimException
     */
    ObjectClass createObjectClass(String name, String parent) throws GSimException;

    /**
     * Instanciates an object.
     * 
     * @param name name of the object
     * @param parent the frame from which the instance is created
     * @return ObjectInstanceIF
     * @throws GSimException
     */
    ObjectInstance createObjectInstance(String name, ObjectClass parent) throws GSimException;

    /**
     * Destroys the environment and frees up any resources connected with it.
     */
    void destroy();

    /**
     * Returns an agent from the environment.
     * 
     * @param name name of the agent
     * @return AgentInstanceIF
     * @throws GSimException
     */
    AgentInstance getAgent(String name) throws GSimException;

    /**
     * Return a particular agent class.
     * 
     * @param name name of the agent class
     * @return AgentClassIF
     */
    AgentClass getAgentClass(String name) throws GSimException;

    /**
     * Return agent classes. If parent is null, all subclasses are returned.
     * 
     * @param parent a parent frame
     * @return List of AgentClassIF
     */
    AgentClass[] getAgentClasses(String parent) throws GSimException;

    /**
     * Returns the name of all agents with the given parent.
     * 
     * @param parent the name of the parent agent
     * @return the names of all subtypes of the parent
     */
    String[] getAgentNames(String parent) throws GSimException;

    /**
     * Returns a list of agent instances. All instances of lower levels of the frame hierarchy are returned. If very many agents are expected, use
     * method <code>getAgents(String parent, int offset, int count)</code>
     * 
     * @param parent a parent frame (if null, all agents are returned)
     * @return List of AgentInstanceIF
     */
    AgentInstance[] getAgents(String parent) throws GSimException;

    /**
     * Returns a list of agent instances of a particular type. All instances of lower levels of the frame hierarchy are returned. The offset and count
     * should be used when the number of agents in the simulation is very large.
     * 
     * @param parent a parent frame (if null, all agents are returned)
     * @param offset offset
     * @param count number of agents to retrieve
     * @return List of AgentInstanceIF
     * @throws GSimException
     */
    AgentInstance[] getAgents(String parent, int offset, int count) throws GSimException;

    /**
     * Return a particular object class.
     * 
     * @param name the name of the object class
     * @return ObjectClassIF
     */
    ObjectClass getObjectClass(String name) throws GSimException;

    /**
     * Return object classes. If parent is null, all subclasses are returned.
     * 
     * @param parent the parent class
     * @return ObjectClassIF[]
     */
    ObjectClass[] getObjectClasses(String parent) throws GSimException;

    /**
     * 
     * @param parent
     * @return list of ObjectInstanceIF
     * @throws GSimException
     */
    ObjectInstance[] getObjects(String parent) throws GSimException;

    /**
     * Return the generic root (the system object from which all agent classes must inherit their properties)
     * 
     * @return AgentClassIF
     * @throws GSimException
     */
    AgentClass getTopAgentClass() throws GSimException;

    /**
     * Return the top level object class from which all objects must inherit.
     * 
     * @return ObjectClassIF
     * @throws GSimException
     */
    ObjectClass getTopObjectClass() throws GSimException;

    /**
     * Instanciate a single agent.
     * 
     * @param parent the frame from which the agent is created
     * @param name name of the agent (must be unique)
     * @return AgentInstanceIF
     * @throws GSimException
     */
    AgentInstance instanciateAgent(AgentClass parent, String name) throws GSimException;

    /**
     * Creates a list of agents.
     * 
     * @param parent the frame from which the agent is created
     * @param prefix common name prefix, to which a running number will be appended
     * @param method method constant defining what to do with the attributeDistribution (e.g. vary randomly)
     * @param standardVariation standard variation (only needed if method is based on normal distribution)
     * @param count number of agents to instanciate
     * @return List of AgentInstanceIF
     * @throws GSimException
     */
    AgentInstance[] instanciateAgentsNormallyDistributed(AgentClass parent, String prefix, double standardVariation, int count)
            throws GSimException;

    AgentInstance[] randomiseAttribute(AgentClass agentClass, String attrPath, double standardVariation)
            throws GSimException;

    /**
     * Creates a list of agents.
     * 
     * @param parent the frame from which the agent is created
     * @param prefix common name prefix, to which a running number will be appended
     * @param method method constant defining what to do with the attributeDistribution (e.g. vary randomly)
     * @param standardVariation standard variation (only needed if method is based on normal distribution)
     * @param count number of agents to instanciate
     * @return List of AgentInstanceIF
     * @throws GSimException
     */
    AgentInstance[] instanciateAgentsUniformDistributed(AgentClass parent, String prefix, int count)
            throws GSimException;

    /**
     * Clears the environment from agents.
     */
    void removeAgentInstances();


    /**
     * Clears the environment from objects.
     */
    void removeObjectInstances(String className);

    /**
     * Removes agent class, all of its subclasses and instance from the environment.
     */
    void removeAgentClass(AgentClass cls);

    /**
     * Removes object class, all of its subclasses and instance from the environment.
     */
    void removeObjectClass(ObjectClass cls);

    void addSystemAgent(ApplicationAgent agent);

}
