package de.s2.gsim.objects;

import java.io.Serializable;

/**
 * The <code>BehaviourIF</code> class is the interface representing both behaviour frames and instances.
 *
 * @author Stephan
 *
 */
public interface BehaviourIF extends Serializable {

    /**
     * Adds or updates an action of this behaviour.
     * 
     * @param action the action
     * @throws GSimObjectException
     */
    public void addOrSetAction(ActionIF action) throws GSimObjectException;

    /**
     * Adds or updates an {@link de.s2.gsim.objects.RLActionNodeIF} of this behaviour.
     * 
     * @param node the RLNode
     * @throws GSimObjectException
     */
    public void addOrSetRLActionNode(RLActionNodeIF node) throws GSimObjectException;

    /**
     * Adds or sets a {@link de.s2.gsim.objects.RuleIF} of this behaviour
     * 
     * @param rule the rule
     * @throws GSimObjectException
     */
    public void addOrSetRule(RuleIF rule) throws GSimObjectException;

    /**
     * Creates an action.
     * 
     * @param name name of the action
     * @param cls fully qualified name of the java class that should be executed when the action gets activated
     * @return the action
     * @throws GSimObjectException
     */
    public ActionIF createAction(String name, String cls) throws GSimObjectException;

    /**
     * Creates an {@link de.s2.gsim.objects.RLActionNodeIF}.
     * 
     * @param name
     * @return the node
     * @throws GSimObjectException
     */
    public RLActionNodeIF createRLActionNode(String name) throws GSimObjectException;

    /**
     * Creates a {@link de.s2.gsim.objects.RuleIF}.
     * 
     * @param name
     * @return the rule
     * @throws GSimObjectException
     */
    public RuleIF createRule(String name) throws GSimObjectException;

    /**
     * Gets the {@link de.s2.gsim.objects.ActionIF} with the specified name.
     * 
     * @param name name of the action
     * @return the action
     * @throws GSimObjectException
     */
    public ActionIF getAction(String name) throws GSimObjectException;

    /**
     * Gets all {@link de.s2.gsim.objects.ActionIF}'s of this behaviour.
     * 
     * @return list of actions
     * @throws GSimObjectException
     */
    public ActionIF[] getAvailableActions() throws GSimObjectException;

    /**
     * Gets the maximum possible number of nodes that the BRA process can expand.
     * 
     * @return the maximum allowed number of nodes
     * @throws GSimObjectException
     */
    public int getMaxNodes() throws GSimObjectException;

    /**
     * Gets the probability with which existing BRA paths become re-activated.
     * 
     * @return the probability value
     * @throws GSimObjectException
     */
    public double getRevaluationProb() throws GSimObjectException;

    /**
     * Gets the cost (zeta) that is used by BRA to determine whether a state successor should be expanded or not. The smaller this parameter, the
     * smaller the likelihood that nodes that were already expanded, are expanded again in the future.
     * 
     * @return the cost parameter value.
     * @throws GSimObjectException
     */
    public double getRevisitCostFraction() throws GSimObjectException;

    /**
     * Get an {@link de.s2.gsim.objects.RLActionNodeIF} with the specified name.
     * 
     * @param name the name of the node
     * @return the node
     * @throws GSimObjectException
     */
    public RLActionNodeIF getRLActionNode(String name) throws GSimObjectException;

    /**
     * Gets all {@link de.s2.gsim.objects.RLActionNodeIF}'s associated with this behaviour.
     * 
     * @return list of nodes
     * @throws GSimObjectException
     */
    public RLActionNodeIF[] getRLActionNodes() throws GSimObjectException;

    /**
     * Gets the {@link de.s2.gsim.objects.RuleIF} with the specified name.
     * 
     * @param name
     * @return the rule
     * @throws GSimObjectException
     */
    public RuleIF getRule(String name) throws GSimObjectException;

    /**
     * Gets all {@link de.s2.gsim.objects.RuleIF}'s associated with this behaviour.
     * 
     * @return list of rules
     * @throws GSimObjectException
     */
    public RuleIF[] getRules() throws GSimObjectException;

    /**
     * Gets the interval m with which nodes in the BRA process are updated. Note: The delete cycle is hardcoded at round(m - 1/4m).
     * 
     * @return the interval
     * @throws GSimObjectException
     */
    public int getUpdateInterval() throws GSimObjectException;

    /**
     * Checks whether the {@link de.s2.gsim.objects.RLActionNodeIF} with the specified name was declared in this level of the inheritance hierarchy.
     * 
     * @param nodeName the name of the node
     * @return true if declared, false otherwise
     * @throws GSimObjectException
     */
    public boolean isDeclaredRLNode(String nodeName) throws GSimObjectException;

    /**
     * Checks whether the {@link de.s2.gsim.objects.RuleIF} with the specified name was declared in this level of the inheritance hierarchy.
     * 
     * @param ruleName
     * @return true if declared, false otherwise
     * @throws GSimObjectException
     */
    public boolean isDeclaredRule(String ruleName) throws GSimObjectException;

    /**
     * Removes the {@link de.s2.gsim.objects.RLActionNodeIF} with the specified name from this behaviour.
     * 
     * @param name the name of the node
     * @throws GSimObjectException
     */
    public void removeRLActionNode(String name) throws GSimObjectException;

    /**
     * Removes the {@link de.s2.gsim.objects.RuleIF} with the specified name from this behaviour.
     * 
     * @param name then name of the rule
     * @throws GSimObjectException
     */
    public void removeRule(String name) throws GSimObjectException;

    /**
     * Sets the maximum number of nodes that BRA is allowed to expand.
     * 
     * @param n the maximum number of nodes
     * @throws GSimObjectException
     */
    public void setMaxNodes(int n) throws GSimObjectException;

    /**
     * Sets the probability with which existing BRA paths become re-activated.
     * 
     * @param p the probability
     * @throws GSimObjectException
     */
    public void setRevaluationProb(double p) throws GSimObjectException;

    /**
     * Sets the cost (zeta), 0 < cost < 1, that is used by BRA to determine whether a state successor should be expanded or not. The smaller this
     * parameter, the smaller the likelihood that nodes that were already expanded, are expanded again in the future.
     * 
     * @param c the cost parameter
     * @throws GSimObjectException
     */
    public void setRevisitCostFraction(double c) throws GSimObjectException;

    /**
     * Set the interval (timesteps) at which BRA evaluates new expansions (Note: Because of hardcoded delete cycle, this will also affect when nodes
     * become deleted).
     * 
     * @param n the interval
     * @throws GSimObjectException
     */
    public void setUpdateInterval(int n) throws GSimObjectException;

}