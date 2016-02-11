package de.s2.gsim.core;

import java.io.InputStream;
import java.util.HashMap;

import de.s2.gsim.sim.engine.Executable;

/**
 * The core is a central class managing model and simulation objects. New models are created by one of the create methods. A simulation is created by
 * passing the Environment object to the createScenarioManager method, which then instantiates a simulation.
 * 
 * @author Stephan
 *
 */
public interface GSimCore {

    /**
     * Connects to running simulation.
     * 
     * @param ns namespace of the model
     * @param props property table
     * @return ScenarioConnector
     * @throws GSimException
     */
    ScenarioConnector connectScenarioManager(String ns, HashMap<?, ?> props) throws GSimException;

    /**
     * Create a gsim Environment.
     * 
     * @param ns namespace (a unique identifier) of the model
     * @param props property table
     * @return environment
     * @throws GSimException
     */
    DefinitionEnvironment create(String ns, HashMap<?, ?> props) throws GSimException;

    /**
     * 
     * Create a gsim Environment from an existing model file.
     * 
     * @param ns nameSpace (a unique identifier) of the model
     * @param setup input-stream of the model file
     * @param props property table
     * @return DefinitionEnvironment
     * @throws GSimException
     */
    DefinitionEnvironment create(String ns, InputStream setup, HashMap<?, ?> props) throws GSimException;

    /**
     * Creates a BatchManager. A BatchManager is a utility that executes a java class on behalf of the user. For example, the user writes a program
     * that iterates over a number of parameter settings. In remote mode, this class is sent the server.
     * 
     * @param ex executable interface
     * @param props runtime properties
     * @return BatchManager
     * @throws GSimException
     */
    BatchManager createBatchManager(Executable ex, HashMap<String, Object> props) throws GSimException;

    /**
     * Creates a ScenarioManager (used to start and control simulation).
     * 
     * @param env DefinitionEnvironment
     * @param props runtime properties
     * @param steps number of steps the simulation shall run
     * @param runs number of repetitions
     * @return ScenarioManager
     * @throws GSimException
     */
    ScenarioManager createScenarioManager(DefinitionEnvironment env, HashMap<String, Object> props, int steps, int runs) throws GSimException;

    /**
     * Utility method to open a file containing a model definition in xml format.
     * 
     * return input-stream
     * 
     * @param canonicalPath path to the file
     */
    InputStream getModelDefinition(String canonicalPath);

    /**
     * Connects to the simulation engine and retrieves the ids of simulations currently being executed.
     * 
     * @param props Property table
     * @return List of simulation ids
     * @throws GSimException
     */
    String[] getRunningSimulations(HashMap<?, ?> props) throws GSimException;


}
