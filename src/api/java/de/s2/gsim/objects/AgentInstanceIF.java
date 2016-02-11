package de.s2.gsim.objects;

/**
 * The <code>AgentInstanceIF</code> class represents an agent instance.
 *
 * @author Stephan
 *
 */
public interface AgentInstanceIF extends ObjectInstanceIF {

    /**
     * Adds (if not yet existing) or updates object instances in a list.
     * 
     * @param list the name of the list
     * @param object the object
     * @throws GSimObjectException
     */
    public void addOrSetObject(String list, ObjectInstanceIF object) throws GSimObjectException;

    /**
     * Creates object of the type of object specified for the list and adds it to the list.
     * 
     * @param objectName the name of the object to instanciate
     * @param listName the name of the list
     * @return the newly created object
     * @throws GSimObjectException
     */
    public ObjectInstanceIF createObjectFromListType(String objectName, String listName) throws GSimObjectException;

    /**
     * Gets the behaviour associated with this agent instance.
     * 
     * @return the behaviour
     * @throws GSimObjectException
     */
    public BehaviourIF getBehaviour() throws GSimObjectException;

    /**
     * Gets an object from a list.
     * 
     * @param list the list name
     * @param objectName the object name
     * @return the object
     * @throws GSimObjectException
     */
    public ObjectInstanceIF getObject(String list, String objectName) throws GSimObjectException;

    /**
     * Gets the object list names defined for this agent.
     * 
     * @return the names of the lists
     * @throws GSimObjectException
     */
    public String[] getObjectListNames() throws GSimObjectException;

    /**
     * Gets all objects in a particular list.
     * 
     * @param list the name of the list
     * @return list of objects
     * @throws GSimObjectException
     */
    public ObjectInstanceIF[] getObjects(String list) throws GSimObjectException;

    /**
     * Removes all objects from an object list.
     * 
     * @param list
     * @throws GSimObjectException
     */
    public void removeAllObjects(String list) throws GSimObjectException;

    /**
     * Removes an object from the specified list.
     * 
     * @param list the name of the list
     * @param object the object
     * @throws GSimObjectException
     */
    public void removeObject(String list, ObjectInstanceIF object) throws GSimObjectException;

    /**
     * Removes an object, given by its name, from the specified list.
     * 
     * @param list the name of the list
     * @param objectName the name of the object
     * @throws GSimObjectException
     */
    public void removeObject(String list, String objectName) throws GSimObjectException;

    /**
     * Sets the behaviour of this agent.
     * 
     * @param behaviour the behaviour
     * @throws GSimObjectException
     */
    public void setBehaviour(BehaviourIF behaviour) throws GSimObjectException;

}
