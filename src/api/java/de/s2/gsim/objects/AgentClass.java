package de.s2.gsim.objects;

import de.s2.gsim.GSimException;

/**
 * The <code>AgentClassIF</code> is an interface to the frame representing an agent.
 *
 * @author Stephan
 *
 */
public interface AgentClass extends ObjectClass {


	/**
	 * Defines object list.
	 * 
	 * @param listName the list name
	 * @param type the object class defining the instances the list can hold.
	 */
	void defineObjectList(String listName, ObjectClass type);

	/**
	 * Defines an attribute list. This is not strictly necessary if an attribute is put to the list - then the list is created on the fly.
	 * However, this might, if not careful, to unexpected 'List name does not exist' exceptions if the list is tried to get, but does not
	 * exist yet.
	 * 
	 * @param listName the list name
	 */
	void defineAttributeList(String listName);

    /**
     * Adds the object class if the object class is not yet defined, or updates it otherwise.
     * 
     * @param list the list where to add the object class.
     * @param object the object class to add/set
     * @throws GSimException
     */
    void addOrSetObject(String list, ObjectClass object) throws GSimException;

    /**
     * Gets the behaviour associated with this agent.
     * 
     * @return the behaviour
     * @throws GSimException
     */
    Behaviour getBehaviour() throws GSimException;

    /**
     * Gets the names of all object lists that are defined for this agent.
     * 
     * @return object list names
     * @throws GSimException
     */
    String[] getObjectListNames() throws GSimException;

    /**
     * Gets the type of object for a particular list.
     * 
     * @param listName
     * @return object class
     * @throws GSimException
     */
    ObjectClass getObjectListType(String listName) throws GSimException;

    /**
     * Gets the default objects in a list. This object classes would be used as a template during the instanciation process.
     * 
     * @param list the name of the object list
     * @return a list of object classes
     * @throws GSimException
     */
    ObjectClass[] getObjects(String list) throws GSimException;

    /**
     * Tests whether an object class was defined in this agent, or whether it was defined somewhere up in the inheritance hierarchy.
     * 
     * @param list the list name
     * @param objectName the object class name
     * @return true if the object was declared in the frame representing this agent, false otherwise
     * @throws GSimException
     */
    boolean isDeclaredObject(String list, String objectName) throws GSimException;

    /**
     * Removes an object class from a list.
     * 
     * @param list the name of the list
     * @param object the object class
     * @throws GSimException
     */
    void removeObject(String list, ObjectClass object) throws GSimException;

    /**
     * Resolves a path into the object it references. Each object can be described by a name that represents its position within the object hierachy.
     * For example the attribute attr of object obj in list list is given by list/obj/attr
     * 
     * @param path the path
     * @return Object an Attribute if the name specified an attribute; an ObjectClass if it specified an ObjectClass; a {@link java.util.List} of
     * attributeDistribution if an attribute list was specified; a {@link java.util.List} of ObjectClasses if an object-list was specified; or null if the path
     * did not specify a valid object/attribute/list.
     * @throws GSimException
     */
    @Override
    Object resolveName(String path) throws GSimException;

    /**
     * Sets the behaviour of this agent.
     * 
     * @param behaviour the behaviour
     * @throws GSimException
     */
    void setBehaviour(Behaviour behaviour) throws GSimException;

}
