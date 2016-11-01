package de.s2.gsim.sim.behaviour;

import java.util.HashMap;
import java.util.Set;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.ActionDef;
import de.s2.gsim.environment.BehaviourDef;

public abstract class SimAction implements java.io.Serializable {

	private static HashMap<String, ClassLoader> classLoaders = new HashMap<String, ClassLoader>();

	private static final long serialVersionUID = 1L;

	protected Set<String> parameterList = null;

	private Context ctx = null;

	private String name = null;

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SimAction)) {
			return false;
		} else {
			if (getName().equals(((SimAction) o).getName())) {
				return true;
			}
		}
		return false;
	}

	public abstract Object execute();

	public final void doExecute() {
		execute();
		ctx.getAgent().setLastActionClassName(name);
	}

	public Context getContext() {
		return ctx;
	}

	public String getName() {
		return name;
	}

	public void setContext(Context ctx) {
		this.ctx = ctx;
		RuntimeAgent agent = ctx.getAgent();
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

	public static void putCL(String ns, ClassLoader cl) {
		classLoaders.put(ns, cl);
	}

	/**
	 * Instantiates an action object of the specified class. This method is typically called from the rulebase.
	 * 
	 * @param s
	 *            String
	 * @return Action
	 */
	public static SimAction valueOf(String className, String ns) {

		ClassLoader cl = classLoaders.get(ns);

		String actionClass = className;
		if (className.indexOf("$") > 0) {
			//actionClass = className.substring(0, className.indexOf("$"));
		}

		try {
			SimAction a = null;
			if (cl != null) {
				try {
					Class<?> cls = Class.forName(actionClass, false, cl);
					a = (SimAction) cls.newInstance();
				} catch (ClassNotFoundException e1) {
					a = (SimAction) Class.forName(actionClass).newInstance();
				}
			} else {
				a = (SimAction) Class.forName(actionClass).newInstance();
			}
			a.name = className;
			return a;
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new RuntimeException(
					"Something went wrong while extracting class from string " + className + ", ClassLoader=" + cl.getClass().getName(), e2);
		}

	}

}
