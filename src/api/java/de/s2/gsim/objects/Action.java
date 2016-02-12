package de.s2.gsim.objects;

import java.io.Serializable;

/**
 * The <code>ActionIF</code> specifies actions of an agent. It abstracts from the fact whether the action is defined on the frame or instance level.
 *
 * @author Stephan
 *
 */
public interface Action extends Serializable {

    /**
     * Adds a parameter to this action.
     * 
     * @param objectClassName the type of object
     * @throws GSimObjectException
     */
    public void addObjectClassParam(String objectClassName) throws GSimObjectException;

    /**
     * Removes all parameters.
     * 
     * @throws GSimObjectException
     */
    public void clearObjectClassParams() throws GSimObjectException;

    /**
     * Gets the actual java class that is called when this action becomes activated.
     * 
     * @return fully qualified name of the class
     * @throws GSimObjectException
     */
    public String getActionClassName() throws GSimObjectException;

    /**
     * Gets the name of this action.
     * 
     * @return the name
     * @throws GSimObjectException
     */
    public String getName() throws GSimObjectException;

    /**
     * Gets the parameters of this action.
     * 
     * @return a list of paths to the respective object of the agent.
     * @throws GSimObjectException
     */
    public String[] getObjectClassParams() throws GSimObjectException;

    /**
     * Tests whether this action has parameters.
     * 
     * @return true if at least one parameter is defined, false otherwise
     * @throws GSimObjectException
     */
    public boolean hasObjectParameter() throws GSimObjectException;

    /**
     * Remove a single parameter.
     * 
     * @param path path to the parameter
     * @throws GSimObjectException
     */
    public void removeObjectClassParam(String path) throws GSimObjectException;

    /**
     * Sets the actual java class that is called when this action becomes activated.
     * 
     * @param className
     * @throws GSimObjectException
     */
    public void setActionClassName(String className) throws GSimObjectException;

}
