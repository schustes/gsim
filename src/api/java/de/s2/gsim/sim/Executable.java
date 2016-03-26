package de.s2.gsim.sim;

/**
 * An Executable allows to hand over whole programs to the simulation engine. The control over the simulation has to be provided by the implementer.
 * The engine then just calls this program via the execute() method.
 * 
 * This is useful for conducting experiments with parameter variations that need to start new simulations several times.
 * 
 * @author stephan
 *
 */
public interface Executable {

    /**
     * Called by the engine once.
     */
    void execute();

}
