package de.s2.gsim.objects;

/**
 * An application agent is called by the simulation agent before and after the normal agents are executed. They can be used for things like data
 * collection, messaging and so on as part of the system itself. But it can also be used as part of the simulation, e.g. to initialise all agents for
 * the step.
 * 
 */
public interface AppAgent {

    /**
     * Agent name.
     * 
     * @return the name
     */
    String getName();

    /**
     * The namespace of the simulation this agent is running in.
     * 
     * @return the namespace
     */
    String getNameSpace();

    /**
     * Called at the end of a step.
     */
    void post();

    /**
     * Called before the start of a step.
     */
    void pre();

}