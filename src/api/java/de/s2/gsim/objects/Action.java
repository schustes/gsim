package de.s2.gsim.objects;

import de.s2.gsim.GSimException;

/**
 * The <code>ActionIF</code> specifies actions of an agent. It abstracts from the fact whether the action is defined on the frame or instance level.
 *
 * @author Stephan
 *
 */
public interface Action {

    /**
     * Adds a parameter to this action.
     * 
     * @param objectClassName the type of object
     * @throws GSimException
     */
    void addObjectClassParam(String objectClassName) throws GSimException;

    /**
     * Removes all parameters.
     * 
     * @throws GSimException
     */
    void clearObjectClassParams() throws GSimException;

    /**
     * Gets the actual java class that is called when this action becomes activated.
     * 
     * @return fully qualified name of the class
     * @throws GSimException
     */
    String getActionClassName() throws GSimException;

    /**
     * Gets the name of this action.
     * 
     * @return the name
     * @throws GSimException
     */
    String getName() throws GSimException;

    /**
     * Gets the parameters of this action.
     * 
     * @return a list of paths to the respective object of the agent.
     * @throws GSimException
     */
    String[] getObjectClassParams() throws GSimException;

    /**
     * Tests whether this action has parameters.
     * 
     * @return true if at least one parameter is defined, false otherwise
     * @throws GSimException
     */
    boolean hasObjectParameter() throws GSimException;

    /**
     * Remove a single parameter.
     * 
     * @param path path to the parameter
     * @throws GSimException
     */
    void removeObjectClassParam(String path) throws GSimException;

    /**
     * Sets the actual java class that is called when this action becomes activated.
     * 
     * @param className
     * @throws GSimException
     */
    void setActionClassName(String className) throws GSimException;

}
