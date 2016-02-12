package gsim.sim.agent;

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.s2.gsim.objects.AgentInstance;
import de.s2.gsim.sim.agent.ApplicationAgent;
import de.s2.gsim.sim.communication.AgentType;
import de.s2.gsim.sim.communication.Communication;
import gsim.def.objects.Instance;
import gsim.def.objects.agent.GenericAgent;
import gsim.def.objects.agent.GenericAgentClass;
import gsim.objects.impl.AgentInstanceSim;
import gsim.sim.behaviour.impl.JessHandler;

public class RuntimeAgent extends GenericAgent implements AgentType {

    private static Logger logger = Logger.getLogger(RuntimeAgent.class);

    private static final long serialVersionUID = 1L;

    protected transient Communication commInterface = null;

    private String[] currentStrategy = new String[0];

    private HashMap<String, Object> globals = new HashMap<>();

    private String ns = null;

    private HashMap<String, RtExecutionContextImpl> roles = new HashMap<String, RtExecutionContextImpl>();

    private transient JessHandler ruleBase = null;

    private int time = 0;

    public RuntimeAgent() {
    }

    /**
     * Fully initialises the agent.
     * 
     * @param agent
     *            GenericAgent
     * @param roles
     *            HashMap
     * @param ruleBase
     *            JessHandler
     */
    public RuntimeAgent(GenericAgent agent, HashMap<String, RtExecutionContextImpl> roles, JessHandler ruleBase, String ns) {
        super(agent);
        this.ns = ns;
        this.roles = roles;
        this.ruleBase = ruleBase;
        // tree = new StateActionTree(getName(), ruleBase.rete);
    }

    /**
     * Parrially initialises the agent with its rulebase. However, to give the agent actually the chance to do something, you have to add at least one
     * Runtime role with addRuntimeRole(...)
     * 
     * @param agent
     *            GenericAgent
     * @param ruleBase
     *            JessHandler
     */
    public RuntimeAgent(GenericAgent agent, JessHandler ruleBase, String ns) {
        super(agent);
        this.ns = ns;
        this.ruleBase = ruleBase;
        // tree = new StateActionTree(getName(), ruleBase.rete);
    }

    /**
     * To make the agent work after calling this constructor, you have to set the rulebase with setRuleHandler(..) and add at least one runtime-role
     * with addRuntimeRole(...) - or the agent will just wait to die.
     * 
     * @param agent
     *            GenericAgent
     */
    public RuntimeAgent(GenericAgent agent, String ns) {
        super(agent);
        this.ns = ns;
    }

    @Override
    public void addChildInstance(String list, Instance inst) {
        super.addChildInstance(list, inst);
        if (ruleBase != null) {
            ruleBase.instanceChanged(list + "/" + inst.getDefinition().getTypeName());
        }
    }

    public void addOrSetExecutionContext(String name, RtExecutionContextImpl r) {

        if (roles.containsKey(name)) {
            RtExecutionContextImpl c = roles.get(name);
            boolean b = true;
            for (String s : r.getDefiningAgentClasses()) {
                for (String t : c.getDefiningAgentClasses()) {
                    if (t.equals(s)) {
                        b = false;
                        logger.debug("Agent class " + t + " was already defining context " + name + " ... ignore!!!");
                        return;
                    }
                }
            }
            if (b) {
                for (String t : c.getDefiningAgentClasses()) {
                    r.addDefiningAgentClass(t);
                }
            }
        }

        roles.put(name, r);

    }

    public void destroy() {
        ruleBase.destroy();
        commInterface = null;
        roles = null;
        ruleBase = null;
        currentStrategy = null;
    }

    public void endEpisode() {
        try {
            ruleBase.endEpisode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(HashMap<String, Object> globals, String roleName) {
        try {

            this.globals = globals;
            if (this.inheritsFrom(roleName)) {
                GenericAgentClass c = (GenericAgentClass) getDefinition().getAncestor(roleName);
                if (c == null && getDefinition().getTypeName().equals(roleName)) {
                    c = (GenericAgentClass) getDefinition();
                }

                if (c.getBehaviour().getDeclaredRules().length > 0 || c.getBehaviour().getDeclaredRLRules().length > 0) {
                    executeRules(roleName);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeRules(String role) {

        if (role.equals("Seller-Action")) {

            if (getChildInstances("queue").length == 0) {
                return;
            }

        }

        ruleBase.executeUserRules(role, globals);
    }

    public String[] getCurrentStrategy() {
        return currentStrategy;
    }

    public RtExecutionContextImpl getExecutionContext(String name) {
        RtExecutionContextImpl c = new RtExecutionContextImpl();
        c.create(name, this, name);
        return c;
    }

    public RtExecutionContextImpl[] getExecutionContexts() {
        return roles.values().toArray(new RtExecutionContextImpl[0]);
    }

    public Communication getMessagingComponent() {
        return commInterface;
    }

    public String getNameSpace() {
        return ns;
    }

    public HashMap<String, RtExecutionContextImpl> getRoles() {
        return roles;
    }

    public int getTime() {
        return time;
    }

    // intialise a role - this can be _within_ one time step
    public void init(HashMap<String, Object> globals) {
        this.globals = globals;
    }

    public void initAfterDeserialise() {
        if (ruleBase != null) {
            ruleBase.setOwner(this);
        }
        for (RtExecutionContextImpl c : getExecutionContexts()) {
            c.setOwner(this);
        }

    }

    // initialise the whole new time-step (e.g. for resetting all values of t-1 to
    // null/defaults..)
    public void initStep() {
        if (ruleBase != null) {
            ruleBase.reset();
        }
    }

    @Override
    public boolean isAgent() {
        return true;
    }

    public void post() {
    }

    @Override
    public void removeChildInstance(String list, String instName) {
        super.removeChildInstance(list, instName);
        ruleBase.retractConstant(list + "/" + instName);
        ruleBase.retractExecutedFact(list + "/" + instName);
    }

    public void setCurrentStrategy(String[] actions) {
        currentStrategy = actions;
    }

    public void setMessagingComponent(Communication a) {
        commInterface = a;
    }

    public void setNameSpace(String ns) {
        this.ns = ns;
    }

    public void setRuleHandler(JessHandler h) {
        ruleBase = h;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void startEpisode() {
        try {
            ruleBase.startEpisode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AgentInstance toAgent() throws ClassCastException {
        return new AgentInstanceSim(this);
    }

    @Override
    public ApplicationAgent toAppAgent() throws ClassCastException {
        throw new ClassCastException("Cannot be casted, class is " + this.getClass().getName());
    }

    public void updateRewards() {
        ruleBase.updateRewards();
    }

}
