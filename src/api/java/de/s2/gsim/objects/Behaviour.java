package de.s2.gsim.objects;

import java.io.Serializable;

import de.s2.gsim.GSimException;
import de.s2.gsim.environment.Unit;

/**
 * The <code>Behaviour</code> class is the interface representing both behaviour frames and instances. It contains the whole behaviour of the agent,
 * that is, it can have - Normal, reactive rules - RL nodes that implement the BRA algorithm. Furthermore, the Behaviour must refer to all actions the
 * agent is able to see. Only those actions can be referenced in the rules. This is a convention to clearly separate (and implicitly type) the action
 * spaces of different agent roles (agent classes that define possibly exclusive behaviour).
 * 
 * @author Stephan
 *
 */
public interface Behaviour extends Serializable {

    /**
     * Adds or updates an action of this behaviour.
     * 
     * @param action the action
     * @throws GSimException
     */
    void addOrSetAction(Action action) throws GSimException;

    /**
     * Adds or updates an {@link de.s2.gsim.objects.RLActionNode} of this behaviour.
     * 
     * @param node the RLNode
     * @throws GSimException
     */
    void addOrSetRLActionNode(RLActionNode node) throws GSimException;

    /**
     * Adds or sets a {@link de.s2.gsim.objects.Rule} of this behaviour
     * 
     * @param rule the rule
     * @throws GSimException
     */
    void addOrSetRule(Rule rule) throws GSimException;

    /**
     * Creates an action.
     * 
     * @param name name of the action
     * @param cls fully qualified name of the java class that should be executed when the action gets activated
     * @return the action
     * @throws GSimException
     */
    Action createAction(String name, String cls) throws GSimException;

    /**
     * Creates an {@link de.s2.gsim.objects.RLActionNode}.
     * 
     * @param name
     * @return the node
     * @throws GSimException
     */
    RLActionNode createRLActionNode(String name) throws GSimException;

    /**
     * Creates a {@link de.s2.gsim.objects.Rule}.
     * 
     * @param name
     * @return the rule
     * @throws GSimException
     */
    Rule createRule(String name) throws GSimException;

    /**
     * Gets the {@link de.s2.gsim.objects.Action} with the specified name.
     * 
     * @param name name of the action
     * @return the action
     * @throws GSimException
     */
    Action getAction(String name) throws GSimException;

    /**
     * Gets all {@link de.s2.gsim.objects.Action}'s of this behaviour.
     * 
     * @return list of actions
     * @throws GSimException
     */
    Action[] getAvailableActions() throws GSimException;

    /**
     * Gets the maximum possible number of nodes that the BRA process can expand.
     * 
     * @return the maximum allowed number of nodes
     * @throws GSimException
     */
    int getMaxNodes() throws GSimException;

    /**
     * Gets the probability with which existing BRA paths become re-activated.
     * 
     * @return the probability value
     * @throws GSimException
     */
    double getRevaluationProb() throws GSimException;

    /**
     * Gets the cost (zeta) that is used by BRA to determine whether a state successor should be expanded or not. The smaller this parameter, the
     * smaller the likelihood that nodes that were already expanded, are expanded again in the future.
     * 
     * @return the cost parameter value.
     * @throws GSimException
     */
    double getRevisitCostFraction() throws GSimException;

    /**
     * Get an {@link de.s2.gsim.objects.RLActionNode} with the specified name.
     * 
     * @param name the name of the node
     * @return the node
     * @throws GSimException
     */
    RLActionNode getRLActionNode(String name) throws GSimException;

    /**
     * Gets all {@link de.s2.gsim.objects.RLActionNode}'s associated with this behaviour.
     * 
     * @return list of nodes
     * @throws GSimException
     */
    RLActionNode[] getRLActionNodes() throws GSimException;

    /**
     * Gets the {@link de.s2.gsim.objects.Rule} with the specified name.
     * 
     * @param name
     * @return the rule
     * @throws GSimException
     */
    Rule getRule(String name) throws GSimException;

    /**
     * Gets all {@link de.s2.gsim.objects.Rule}'s associated with this behaviour.
     * 
     * @return list of rules
     * @throws GSimException
     */
    Rule[] getRules() throws GSimException;

    /**
     * Gets the interval m with which nodes in the BRA process are updated. Note: The delete cycle is hardcoded at round(m - 1/4m).
     * 
     * @return the interval
     * @throws GSimException
     */
    int getUpdateInterval() throws GSimException;

    /**
     * Checks whether the {@link de.s2.gsim.objects.RLActionNode} with the specified name was declared in this level of the inheritance hierarchy.
     * 
     * @param nodeName the name of the node
     * @return true if declared, false otherwise
     * @throws GSimException
     */
    boolean isDeclaredRLNode(String nodeName) throws GSimException;

    /**
     * Checks whether the {@link de.s2.gsim.objects.Rule} with the specified name was declared in this level of the inheritance hierarchy.
     * 
     * @param ruleName
     * @return true if declared, false otherwise
     * @throws GSimException
     */
    boolean isDeclaredRule(String ruleName) throws GSimException;

    /**
     * Removes the {@link de.s2.gsim.objects.RLActionNode} with the specified name from this behaviour.
     * 
     * @param name the name of the node
     * @throws GSimException
     */
    void removeRLActionNode(String name) throws GSimException;

    /**
     * Removes the {@link de.s2.gsim.objects.Rule} with the specified name from this behaviour.
     * 
     * @param name then name of the rule
     * @throws GSimException
     */
    void removeRule(String name) throws GSimException;

    /**
     * Sets the maximum number of nodes that BRA is allowed to expand.
     * 
     * @param n the maximum number of nodes
     * @throws GSimException
     */
    void setMaxNodes(int n) throws GSimException;

    /**
     * Sets the probability with which existing BRA paths become re-activated.
     * 
     * @param p the probability
     * @throws GSimException
     */
    void setRevaluationProb(double p) throws GSimException;

    /**
     * Sets the cost (zeta), 0 < cost < 1, that is used by BRA to determine whether a state successor should be expanded or not. The smaller this
     * parameter, the smaller the likelihood that nodes that were already expanded, are expanded again in the future.
     * 
     * @param c the cost parameter
     * @throws GSimException
     */
    void setRevisitCostFraction(double c) throws GSimException;

    /**
     * Set the interval (timesteps) at which BRA evaluates new expansions (Note: Because of hardcoded delete cycle, this will also affect when nodes
     * become deleted).
     * 
     * @param n the interval
     * @throws GSimException
     */
    void setUpdateInterval(int n) throws GSimException;
    
   

}