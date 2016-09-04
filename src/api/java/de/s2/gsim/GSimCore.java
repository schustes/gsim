package de.s2.gsim;

import java.io.InputStream;
import java.util.Map;

import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.sim.BatchManager;
import de.s2.gsim.sim.Executable;
import de.s2.gsim.sim.SimulationController;

/**
 * The core is a central class managing model and simulation objects. New models are created by one of the create methods. A simulation is created by
 * passing the Environment object to the createScenarioManager method, which then instantiates a simulation.
 * 
 * @author Stephan
 *
 */
public interface GSimCore {

    /**
     * Create a gsim Environment.
     * 
     * @param ns namespace (a unique identifier) of the model
     * @param props property table
     * @return environment
     * @throws GSimException
     */
    ModelDefinitionEnvironment create(String ns, Map<String, Object> props) throws GSimException;

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
    ModelDefinitionEnvironment create(String ns, InputStream setup, Map<String, Object> props) throws GSimException;

    /**
     * Creates a BatchManager. A BatchManager is a utility that executes a java class on behalf of the user. For example, the user writes a program
     * that iterates over a number of parameter settings. In remote mode, this class is sent the server.
     * 
     * @param ex executable interface
     * @param props runtime properties
     * @return BatchManager
     * @throws GSimException
     */
    BatchManager createBatchManager(Executable ex, Map<String, Object> props) throws GSimException;

    /**
     * Creates a ScenarioController (used to start and control simulation).
     * 
     * @param env DefinitionEnvironment
     * @param props runtime properties
     * @param steps number of steps the simulation shall run
     * @param runs number of repetitions
     * @return ScenarioManager
     * @throws GSimException
     */
    SimulationController createScenarioManager(ModelDefinitionEnvironment env, Map<String, Object> props, int steps, int runs) throws GSimException;

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
    String[] getRunningSimulations(Map<String, Object> props) throws GSimException;


}
