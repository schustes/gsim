package de.s2.gsim.objects;

/**
 * The <code>AgentClassIF</code> is an interface to the frame representing an agent.
 *
 * @author Stephan
 *
 */
public interface AgentClass extends ObjectClass {

    /**
     * Adds the object class if the object class is not yet defined, or updates it otherwise.
     * 
     * @param list the list where to add the object class.
     * @param object the object class to add/set
     * @throws GSimObjectException
     */
    public void addOrSetObject(String list, ObjectClass object) throws GSimObjectException;

    /**
     * Gets the behaviour associated with this agent.
     * 
     * @return the behaviour
     * @throws GSimObjectException
     */
    public Behaviour getBehaviour() throws GSimObjectException;

    /**
     * Gets the names of all object lists that are defined for this agent.
     * 
     * @return object list names
     * @throws GSimObjectException
     */
    public String[] getObjectListNames() throws GSimObjectException;

    /**
     * Gets the type of object for a particular list.
     * 
     * @param listName
     * @return object class
     * @throws GSimObjectException
     */
    public ObjectClass getObjectListType(String listName) throws GSimObjectException;

    /**
     * Gets the default objects in a list. This object classes would be used as a template during the instanciation process.
     * 
     * @param list the name of the object list
     * @return a list of object classes
     * @throws GSimObjectException
     */
    public ObjectClass[] getObjects(String list) throws GSimObjectException;

    /**
     * Tests whether an object class was defined in this agent, or whether it was defined somewhere up in the inheritance hierarchy.
     * 
     * @param list the list name
     * @param objectName the object class name
     * @return true if the object was declared in the frame representing this agent, false otherwise
     * @throws GSimObjectException
     */
    public boolean isDeclaredObject(String list, String objectName) throws GSimObjectException;

    /**
     * Removes an object class from a list.
     * 
     * @param list the name of the list
     * @param object the object class
     * @throws GSimObjectException
     */
    public void removeObject(String list, ObjectClass object) throws GSimObjectException;

    /**
     * Removes an object class, identified by name only, from a list.
     * 
     * @param list the name of the list
     * @param objectName the name of the object class
     * @throws GSimObjectException
     */
    public void removeObject(String list, String objectName) throws GSimObjectException;

    /**
     * Resolves a path into the object it references. Each object can be described by a name that represents its position within the object hierachy.
     * For example the attribute attr of object obj in list list is given by list/obj/attr
     * 
     * @param path the path
     * @return Object an Attribute if the name specified an attribute; an ObjectClass if it specified an ObjectClass; a {@link java.util.List} of
     * attributes if an attribute list was specified; a {@link java.util.List} of ObjectClasses if an object-list was specified; or null if the path
     * did not specify a valid object/attribute/list.
     * @throws GSimObjectException
     */
    @Override
    public Object resolveName(String path) throws GSimObjectException;

    /**
     * Sets the behaviour of this agent.
     * 
     * @param behaviour the behaviour
     * @throws GSimObjectException
     */
    public void setBehaviour(Behaviour behaviour) throws GSimObjectException;

}
