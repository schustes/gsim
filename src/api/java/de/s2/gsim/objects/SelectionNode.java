package de.s2.gsim.objects;

import de.s2.gsim.GSimException;

/**
 * A SelectionNode is a rule that selects an action probabilistically instead of deterministically as in normal rules. From these nodes, BRA
 * expansions are built. SelectionNode inherits from Rule, so all methods can be used likewise to build up the state-action tree, for example by
 * adding conditions an consequents as in normal rules.
 * 
 * A SelectionNode contains 0 or several conditions and 1 to n actions. If the condition is met, the RL algorithm selects one of the actions
 * probabilistically.
 * 
 * The addNodeRef methods are used to build up the SelectionNode by consecutive calls. A NodeRef simply is a reference to an action and its
 * parameters. The actions are added to the SelectionNode. If the actions refer to different objects, they are added to the condition according to the
 * specified parameters.
 * 
 * Examples:
 * 
 * addNodeRefWithCondition($move-forward:$tile:colour, "=", "green") addNodeRefWithCondition($move-forward:$tile:colour, "=", "blue")
 * addNodeRefWithCondition($move-backward:$tile:colour, "=", "yellow")
 * 
 * will create a rule like: if (tile.colour="green or tile.colour="blue" or tile.colour="yellow") then select one of (move-forward, move-backward)).
 * 
 * To generate this rule, more intuitive would be calling first addNodeRefWithCondition and then addNodeRefWithoutCondition:
 * 
 * addNodeRefWithCondition(move-forward, tile,colour, "=", "green") addNodeRefWithCondition(move-forward, tile,colour, "=", "blue")
 * addNodeRefWithCondition(move-forward, tile,colour, "=", "yellow") addNodeRefWithoutCondition(move-backward, tile, colour)
 * addNodeRefWithoutCondition(do-not-move, tile, colour) addNodeRefWithoutCondition(move-left, tile, colour)
 * 
 * or: 
 * addNodeRefWithCondition(move-forward, tile,colour, "=", "green") 
 * addCondition(tile, colour, "=", green), using the parent rule methods
 * addAction(move-left), using the parent rule methods
 *
 * 
 * @author Stephan
 *
 */
public interface SelectionNode extends Rule {

    /**
     * Adds a new action, specified by the formattedString parameter. The formattedString ($action::$object:attribute) contains an action and
     * optionally an object and attribute. Together with the given operator and value, a condition from the object and attribute is created. If the
     * condition already exists, the action is added to the consequents, otherwise a new condition is added.
     * 
     * This is shorthand for the more accurate method addNodeRef(...) below.
     * 
     * @param formattedString a string containing action, object and the attribute used for the LHS rule evaluations ($action::$object:attribute)
     * @param op Rule operator
     * @param val Rule criterion value
     * @throws GSimException if a problem occurs
     */
    void addNodeRefWithCondition(String formattedString, String op, String val) throws GSimException;

    /**
     * Adds a new action actionRef to the set of possible actions of this node. The parameters objectRef, relativeAttPath, op and val are used to
     * create a new condition. If the condition already exists, the action is added to the consequents, otherwise a new condition is added.
     * 
     * @param actionRef an action that can be executed by the node
     * @param objectRef the object under evaluation of the node
     * @param relativeAttPath the attribute path in the object to be evaluated
     * @param op Rule operator
     * @param val criterion value
     * @throws GSimException if a problem occurs
     */
    void addNodeRefWithCondition(String actionRef, String objectRef, String relativeAttPath, String op, String val) throws GSimException;

    /**
     * Adds a new action actionRef, with parameter relativeAttPath of referenced object objectRef to the set of possible actions of this node. No
     * extra condition is defined.
     * 
     * @param actionRef an action that can be executed by the node
     * @param objectRef the object under evaluation of the node
     * @param relativeAttPath the attribute path in the object to be evaluated
     * @throws GSimException if a problem occurs
     */
    void addNodeRefWithoutCondition(String actionRef, String objectRef, String relativeAttPath) throws GSimException;

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
