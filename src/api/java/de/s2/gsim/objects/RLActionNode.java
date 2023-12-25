package de.s2.gsim.objects;

import de.s2.gsim.objects.attribute.Attribute;

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

	void addOrSetCondition(Path<Attribute> path, String op, String value);

    void addOrSetConsequent(de.s2.gsim.objects.Action cons);
    /**
     * Adds or sets an {@link Expansion} to the learning node. An expansion is a component of the state description.
     * 
     * @param expansion the expansion
     */
    void addOrSetExpansion(Expansion expansion);

    /**
	 * Creates a {@link Evaluator}. An evaluator is basically a container for holding a path to an attribute used as reward for the RL
	 * algorithm and the learning rate alpha to be applied to this node.
	 * 
	 * @param attributeRef
	 * @param alpha
	 * @return the condition
	 */
	Evaluator createEvaluator(String attributeRef, double alpha);

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
	 * Gets the learning discount factor (beta).
	 * 
	 * @return the factor
	 */
    double getDiscount();

    /**
	 * Gets the evaluator of the node; this is a pair holding alpha and a path
	 * to the reward variable.
	 * 
	 * @return the condition
	 */
	Evaluator getEvaluator();

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
     * Gets the step size at which the average reward port gets updated.
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
     * Removes an expansion.
     * 
     * @param expansion the expansion
     */
    void removeExpansion(Expansion expansion);

    /**
     * Sets the learning discount factor.
     * 
     * @param discount the discount
     */
    void setDiscount(double discount);

    /**
	 * Sets the evaluation info.
	 * 
	 * @param eval the evaluator
	 */
	void setEvaluator(Evaluator eval);

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
