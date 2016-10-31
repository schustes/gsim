package de.s2.gsim.objects;

/**
 * An RLActionNode manages the reinforcement learning and state space partitioning part according to the BRA algorithm of the agent.
 * 
 * It has a set of {@link SelectionNode}s, as described in the BRA paper.
 * 
 * @author stephan
 *
 */
public interface RLActionNode {

    /**
     * Enum for the accumulation of experience
     */
    enum Method {
        /**
         * Simply accumulates experience.
         */
        NULL,
        /**
         * Q-Learning.
         */
        Q
    }

    /**
     * Enum for the learning policy.
     */
    enum Policy {
        /**
         * Comparison policy (see Sutton/Barto for a description).
         */
        COMPARISON,
        /**
         * Softmax policy (see Sutton/Barto for a description).
         */
        SOFTMAX
    }

    void addOrSetCondition(Condition cond);

    void addOrSetConsequent(de.s2.gsim.objects.Action cons);
    /**
     * Adds or sets an {@link Expansion} to the learning node. An expansion is a component of the state description.
     * 
     * @param expansion the expansion
     */
    void addOrSetExpansion(Expansion expansion);

    /**
     * Adds or sets a {@link SelectionNode} to the learning node. A SelectionNode is a rule for generating the BRA expansions.
     * 
     * @param selectionNode the selection node
     */
    void addOrSetSelectionNode(SelectionNode selectionNode);

    /**
     * Creates a {@link Condition}. A condition qualifies the circumstances under which the RLActionNode is activated. An RLActionNode extends a
     * normal if-then rule.
     * 
     * @param paramName the condition parameter name in form of a FQN, e.g. attribute-list-1/attribute-name
     * @param op the operator, e.g. >, <
     * @param val the condition value, e.g. a number that the attribute holds at a certain state of the agent
     * @return the condition
     */
    Condition createEvaluator(String paramName, String op, String val);

    /**
     * Creates a numerical {@link Expansion}, describing the space it covers.
     * 
     * @param param the parameter name in form of a FQN, e.g. attribute-list-1/attribute-name
     * @param min the lower bound of the space
     * @param max the upper bound of the space
     * @return the expansion
     */
    Expansion createExpansion(String param, String min, String max);

    /**
     * Creates a categorical {@link Expansion}, describing the space it covers.
     * 
     * @param param the parameter name in form of a FQN, e.g. attribute-list-1/attribute-name
     * @param fillers the possible attribute values
     * @return the expansion
     */
    Expansion createExpansion(String param, String[] fillers);

    /**
     * Creates a {@link SelectionNode} with the given name.
     * 
     * @param name the name
     * @return the node
     */
    SelectionNode createSelectionNode(String name);

    /**
     * Gets the learning discount factor.
     * 
     * @return the factor
     */
    double getDiscount();

    /**
     * Gets the activation condition of the node.
     * 
     * @return the condition
     */
    Condition getEvaluator();

    /**
     * Gets the restriction interval, which describes how often this node should be executed (e.g. every 3 time steps etc.).
     * 
     * @return the interval
     */
    String getExecutionRestrictionInterval();

    /**
     * Gets the state expansion descriptors of the node.
     * 
     * @return the expansions
     */
    Expansion[] getExpansions();

    /**
     * Gets the step size at which the average reward value gets updated.
     * 
     * @return the step size
     */
    double getGlobalAverageStepSize();

    /**
     * Gets the learning method.
     * 
     * @return the method
     */
    Method getMethod();

    /**
     * Gets the learning policy.
     * 
     * @return the policy
     */
    Policy getPolicy();

    /**
     * Gets the selection node with the given name.
     * 
     * @param name the name
     * @return the selection node
     */
    SelectionNode getSelectionNode(String name);

    /**
     * Gets all selection nodes.
     * 
     * @return the nodes
     */
    SelectionNode[] getSelectionNodes();

    /**
     * Removes an expansion.
     * 
     * @param expansion the expansion
     */
    void removeExpansion(Expansion expansion);

    /**
     * Removes a selection node.
     * 
     * @param selectionNode the node to remove
     */
    void removeSelectionNode(SelectionNode selectionNode);

    /**
     * Sets the learning discount factor.
     * 
     * @param discount the discount
     */
    void setDiscount(double discount);

    /**
     * Sets the activation condition.
     * 
     * @param condition the condition
     */
    void setEvaluator(Condition condition);

    /**
     * Sets the execution interval restriction
     * 
     * @param interval the step size at which the node is active
     */
    void setExecutionRestrictionInterval(String interval);

    /**
     * Set the global average step size used for the average reward calculation.
     * 
     * @param size the step size
     */
    void setGlobalAverageStepSize(double size);

    /**
     * Sets the learning method.
     * 
     * @param method
     */
    void setMethod(Method method);

    /**
     * Sets the learning policy.
     * 
     * @param policy the policy
     */
    void setPolicy(Policy policy);

}
