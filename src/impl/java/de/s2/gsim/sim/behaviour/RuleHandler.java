package de.s2.gsim.sim.behaviour;

import java.util.HashMap;

public interface RuleHandler {

    /**
     * Updates all action-nodes that were executed until now with the current reward (a sort of back-propagation routine which can be called if some
     * connection between atomic decisions is needed).
     */
    public void endEpisode() throws GSimBehaviourException;

    /**
     * Executes rules for the current role and time. Note: The rulebase is not cleared, so only rules will become activated that were defined for this
     * role.
     * 
     * @param role
     *            String
     * @param modelTime
     *            int
     */
    public void executeUserRules(String role, HashMap globals);

    /**
     * Clear the rulebase from the state of the agent (i.e. all constants and variables that were in the rulebase are retracted).
     */
    public void reset();

    /**
     * Start an episode. An episode is a means to connect several actions with each other. While during an episode actions are updated as usual, a
     * final update will be performed on calling endEpisode(), therefore giving the agent a clue about the causal relationship of a sequence of
     * actions.
     */
    public void startEpisode() throws GSimBehaviourException;

    public void updateRewards();

}
