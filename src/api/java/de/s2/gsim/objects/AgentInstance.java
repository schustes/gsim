package de.s2.gsim.objects;

import de.s2.gsim.GSimException;

/**
 * The <code>AgentInstanceIF</code> class represents an agent instance.
 *
 * @author Stephan
 *
 */
public interface AgentInstance extends ObjectInstance {

    /**
     * Adds (if not yet existing) or updates object instances in a list.
     * 
     * @param list the name of the list
     * @param object the object
     * @throws GSimException
     */
    void addOrSetObject(String list, ObjectInstance object) throws GSimException;

    /**
     * Creates object of the type of object specified for the list and adds it to the list.
     * 
     * @param objectName the name of the object to instanciate
     * @param listName the name of the list
     * @return the newly created object
     * @throws GSimException
     */
    ObjectInstance createObjectFromListType(String objectName, String listName) throws GSimException;

    /**
     * Gets the behaviour associated with this agent instance.
     * 
     * @return the behaviour
     * @throws GSimException
     */
    Behaviour getBehaviour() throws GSimException;

    /**
     * Gets an object from a list.
     * 
     * @param list the list name
     * @param objectName the object name
     * @return the object
     * @throws GSimException
     */
    ObjectInstance getObject(String list, String objectName) throws GSimException;

    /**
     * Gets the object list names defined for this agent.
     * 
     * @return the names of the lists
     * @throws GSimException
     */
    String[] getObjectListNames() throws GSimException;

    /**
     * Gets all objects in a particular list.
     * 
     * @param list the name of the list
     * @return list of objects
     * @throws GSimException
     */
    ObjectInstance[] getObjects(String list) throws GSimException;

    /**
     * Removes an object from the specified list.
     * 
     * @param list the name of the list
     * @param object the object
     * @throws GSimException
     */
    void removeObject(String list, ObjectInstance object) throws GSimException;

    /**
     * Sets the behaviour of this agent.
     * 
     * @param behaviour the behaviour
     * @throws GSimException
     */
    void setBehaviour(Behaviour behaviour) throws GSimException;

}
