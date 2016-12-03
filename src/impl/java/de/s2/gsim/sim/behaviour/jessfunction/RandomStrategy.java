package de.s2.gsim.sim.behaviour.jessfunction;

import jess.Activation;
import jess.Strategy;

/**
 */
public class RandomStrategy implements Strategy, java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param activation
     *            Activation
     * @param activation1
     *            Activation
     * @return int
     */
    @Override
    public int compare(Activation activation, Activation activation1) {

        int i = activation.getSalience();
        int j = activation1.getSalience();

        if (i != j) {
            return j - i;
        }

        if (activation.isInactive() && !activation1.isInactive()) {
            return -1;
        }

        if (!activation.isInactive() && activation1.isInactive()) {
            return 1;
        }

        if (activation.isInactive() && activation1.isInactive()) {
            return 0;
        }

        // i=j: select one activation randomly, if you don't know any better
        boolean b = cern.jet.random.Uniform.staticNextBoolean();
        if (b) {
            return -1;
        } else {
            return 1;
        }

    }

    /**
     * 
     * @return String
     * @todo Implement this jess.Strategy method
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
