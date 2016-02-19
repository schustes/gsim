package de.s2.gsim.objects;

import de.s2.gsim.core.GSimException;

/**
 * A SelectionNode is a rule for generating the BRA expansions. Such nodes differ from 'normal' rules in that they refer to more than one action,
 * which gets selected according to an Reinforcement Learning mechanism.
 * 
 * @author stephan
 *
 */
public interface SelectionNode extends Rule {

    /**
     * Adds a RL rule node, roughly things like 'action-x if attribute-value > y).
     * 
     * If the node defined by the condition does not exist, it is created. If it exists, the action is added to the consequents.
     * 
     * @param formattedString a string containing action, object and the attribute used for the LHS rule evaluations ($action::$object:attribute)
     * @param op Rule operator
     * @param val Rule criterion value
     * @throws GSimException if a problem occurs
     */
    void addNodeRef(String formattedString, String op, String val) throws GSimException;

    /**
     * A more verbose representation of an RL node.
     * 
     * If the node defined by the condition does not exist, it is created. If it exists, the action is added to the consequents.
     * 
     * @param actionRef an action that can be executed by the node
     * @param objectRef the object under evaluation of the node
     * @param relativeAttPath the attribute path in the object to be evaluated
     * @param op Rule operator
     * @param val criterion value
     * @throws GSimException if a problem occurs
     */
    void addNodeRef(String actionRef, String objectRef, String relativeAttPath, String op, String val) throws GSimException;

    /**
     * Gets all referenced attributes an objects of the the RL node as their string representation.
     * 
     * @return a list of attributes in the condition part of the rule
     * @throws GSimException if a problem occurs
     */
    String[] getNodeRefs() throws GSimException;

    /**
     * Gets a specific action of the node by its name.
     * 
     * @param actionName the action name
     * @return the {@link Action} object
     * @throws GSimException if a problem occurs
     */
    Action getReferencedAction(String actionName) throws GSimException;

    /**
     * Gets all actions referenced by this node.
     * 
     * @return the {@link Action} list
     * @throws GSimException if a problem occurs
     */
    Action[] getReferencedActions() throws GSimException;

    /**
     * Gets all referenced object attributes that the referenced action takes as parameters.
     * 
     * @param actionRef the name of the action
     * @return the parameter list
     * @throws GSimException if a problem occurs
     */
    String[] getReferencedParameters(String actionRef) throws GSimException;

}
