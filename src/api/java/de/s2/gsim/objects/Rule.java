package de.s2.gsim.objects;

/**
 * Represents a simple if-then rule.
 * 
 * @author stephan
 */
interface Rule {

    /**
     * Adds or sets a condition.
     * 
     * @param condition the condition
     */
    void addOrSetCondition(Condition condition);

    /**
     * Adds or sets an {@link Action} as consequent .
     * 
     * @param consequent the action
     */
    void addOrSetConsequent(Action consequent);

    /**
     * Creates a {@link Condition}.
     * 
     * @param paramName the condition parameter name
     * @param op operator
     * @param val value
     * @return the condition
     */
    Condition createCondition(String paramName, String op, String val);

    /**
     * Gets a all {@link Condition}s of the rule.
     * 
     * @return the conditions
     */
    Condition[] getConditions();

    /**
     * Gets a consequent of the rule.
     * 
     * @param actionName the action name
     * @return the {@link Action} object
     */
    Action getConsequent(String actionName);

    /**
     * Gets all consequents of the rule.
     * 
     * @return the actions
     */
    Action[] getConsequents();

    /**
     * Gets the name of this rule.
     * 
     * @return the name
     */
    String getName();

    /**
     * Checks whether this rule is active or not. Inactive rules never fire.
     * 
     * @return true if active, false otherwise
     */
    boolean isActivated();

    /**
     * Removes a condition from the rule.
     * 
     * @param condition the condition to remove
     */
    void removeCondition(Condition condition);

    /**
     * Removes a consequent from the rule.
     * 
     * @param consequent the consequent
     */
    void removeConsequent(Action consequent);

    /**
     * Activates or deactivates the rule.
     * 
     * @param activated if true, the rule is active, false otherwise
     */
    void setActivated(boolean activated);

}
