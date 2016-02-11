package gsim.sim.engine.common;

import java.io.Serializable;

import de.s2.gsim.sim.engine.ModelState;

/**
 * Instances of the ScenarioEvent class get loaded by the Scheduler object during runtime, where its execute-method is called. Sub-classes have to
 * implement the execute method.
 *
 * A ScenarioEvent is identified by two parameter target and action. The idea was that the target object could be any object on which something should
 * be executed, but in practice it's more or less only an additional identificator like the string action - because usually a reference to the model,
 * and with this to any object in the simulation can be set (was more practical). The field optionalParameter can be used to handle possible arguments
 * (the reason for this was to have a common interface for a UI, but I don't know if this makes really sense).
 *
 */
public abstract class ScenarioEvent implements Serializable, Cloneable {

    public final static long serialVersionUID = -4122914036554533857L;

    protected String action;

    protected String file = null;

    protected Object[] optionalParameter = null;

    protected Object paramVal = null;

    protected String target;

    private transient ModelState coordinator = null;

    private boolean isInterval = false;

    private long t = -1;

    public ScenarioEvent(String groupName, String eventName) {
        target = groupName;
        action = eventName;
    }

    @Override
    public abstract Object clone();

    public abstract void execute();

    public String getDescriptionFile() {
        return file;
    }

    public String getEventName() {
        return action;
    }

    public long getExecutionTime() {
        return t;
    }

    public String getGroupName() {
        return target;
    }

    public Object[] getOptionalParameterFillers() {
        return optionalParameter;
    }

    public String getOptionalParameterName() {
        return "";
    }

    public Object getOptionalParameterValue() {
        return paramVal;
    }

    public boolean isInterval() {
        return isInterval;
    }

    // Coordinator is responsible for setting right context
    public void setCoordinatorRef(ModelState state) {
        coordinator = state;
    }

    public void setDescriptionFile(String fileName) {
        file = fileName;
    }

    public void setExecutionTime(long t) {
        this.t = t;
    }

    public void setInterval(boolean b) {
        isInterval = b;
    }

    public void setOptionalFillers(Object[] o) {
        optionalParameter = o;
    }

    public void setOptionalParameterValue(Object o) {
        paramVal = o;
    }

    @Override
    public String toString() {
        return target.toString();
    }

    // subclasses must use this method to get access to agents
    protected ModelState getCoordinatorRef() {
        return coordinator;
    }

}
