package de.s2.gsim.sim.behaviour;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.BehaviourDef;

import java.util.HashMap;
import java.util.Map;

public class SimulationActionExecutor implements java.io.Serializable {

	private static Map<String, ClassLoader> classLoaders = new HashMap<String, ClassLoader>();

	private static final long serialVersionUID = 1L;

	private SimulationRuntimeContextImpl ctx = null;

	private String name = null;

	public SimulationRuntimeContextImpl getContext() {
		return ctx;
	}

	public String getName() {
		return name;
	}

    public final void execute(String actionClassName) {
        SimulationRuntimeAction action = instantiateAction(actionClassName);
        action.execute(ctx);
        ctx.getAgentInternal().setLastActionClassName(actionClassName);
    }

    @Override
	public boolean equals(Object o) {
		if (!(o instanceof SimulationRuntimeAction)) {
			return false;
		} else {
			if (getName().equals(((SimulationRuntimeAction) o).getName())) {
				return true;
			}
		}
		return false;
	}


	public void setContext(SimulationRuntimeContextImpl ctx) {
		this.ctx = ctx;
		RuntimeAgent agent = ctx.getAgentInternal();
		BehaviourDef b = agent.getBehaviour();
        ActionDef[] cs = b.getAvailableActions();
		for (int i = 0; i < cs.length; i++) {
            ActionDef a = cs[i];
			String className = a.getClassName();
			if (className.equals(this.getClass().getName())) {
				name = a.getName();
				agent.setCurrentStrategy(new String[] { a.getName() });
			}
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	private SimulationRuntimeAction instantiateAction(String className) {

        String actionClass = className;
        //SimpleClassLoader cl = new SimpleClassLoader(path);
        try {
            SimulationRuntimeAction  actionImplementation =
                    (SimulationRuntimeAction) Class.forName(actionClass).getDeclaredConstructor().newInstance();
            return actionImplementation;
        } catch (Exception e) {
            throw new GSimBehaviourException(
                    "Something went wrong while extracting class from string " + className, e);
        }

    }

}
