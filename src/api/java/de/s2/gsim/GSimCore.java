package de.s2.gsim;

import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.sim.SimulationController;

import java.io.InputStream;
import java.util.Map;

/**
 * The core is a central class managing common and simulation objects. New models are created by one of the create methods. A simulation is created by
 * passing the Environment object to the createScenarioManager method, which then instantiates a simulation.
 * 
 * @author Stephan
 *
 */
public interface GSimCore {

    /**
     * Create a gsim Environment.
     * 
     * @param ns namespace (a unique identifier) of the common
     * @param props property table
     * @return environment
     * @throws GSimException
     */
    ModelDefinitionEnvironment create(String ns, Map<String, Object> props) throws GSimException;

    /**
     * 
     * Create a gsim Environment from an existing common file.
     * 
     * @param ns nameSpace (a unique identifier) of the common
     * @param setup input-stream of the common file
     * @param props property table
     * @return DefinitionEnvironment
     * @throws GSimException
     */
    ModelDefinitionEnvironment create(String ns, InputStream setup, Map<String, Object> props) throws GSimException;

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
     * Utility method to open a file containing a common definition in xml format.
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
