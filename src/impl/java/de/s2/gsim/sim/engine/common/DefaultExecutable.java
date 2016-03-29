package de.s2.gsim.sim.engine.common;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;
//import gsim.tools.setup.ModelInstanceSetupPanel;

import de.s2.gsim.GSimCore;
import de.s2.gsim.GSimCoreFactory;
import de.s2.gsim.def.ModelDefinitionEnvironment;
import de.s2.gsim.sim.Executable;
import de.s2.gsim.sim.SimulationController;
import de.s2.gsim.sim.SimulationListener;

public class DefaultExecutable implements Executable, Serializable {

    private static Logger logger = Logger.getLogger(DefaultExecutable.class);

    private static final long serialVersionUID = 1L;

    private ModelDefinitionEnvironment env;

    private transient SimulationController m;

    private String mode;

    private HashMap<String, Object> props;

    private int runs = 0;

    private int steps = 0;

    public DefaultExecutable(ModelDefinitionEnvironment env, String mode, HashMap<String, Object> props, int steps, int runs) {
        this.mode = mode;
        this.props = props;
        this.steps = steps;
        this.runs = runs;
        this.env = env;
    }

    @Override
    public void execute() {
        try {
            GSimCore core = GSimCoreFactory.defaultFactory().createCore();

            m = core.createScenarioManager(env, props, steps, runs);

            m.registerSimulationListener(new SimulationListener() {
                @Override
                public void instanceCancelled(String uid) {
                }

                @Override
                public void instanceFinished(String uid) {
                }

                @Override
                public void instanceStep(String uid, int step) {
                }

                @Override
                public void simulationCrashed(String ns) {
                }

                @Override
                public void simulationFinished(String ns) {
                    logger.debug("Executable received msg-finished, id=" + ns + ", trying to shutdown..");
                    try {
                        m.shutdown();
                        env.destroy();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void simulationRestarted(String ns) {
                }

            });

            m.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
