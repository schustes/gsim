package de.s2.gsim.sim.behaviour.impl.jessfunction;

import jess.JessException;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

public class ComparisonSelector extends SimpleSoftmaxSelector implements Userfunction, java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ComparisonSelector() {
    }

    public ComparisonSelector(double maxReward) {
        super(maxReward);
    }

    @Override
    public Value call(ValueVector vv, jess.Context context) throws JessException {
        return super.call(vv, context);
    }

    @Override
    public String getName() {
        return "comparison-action-selector";
    }

}
