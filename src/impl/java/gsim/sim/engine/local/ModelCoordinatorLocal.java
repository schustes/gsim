package gsim.sim.engine.local;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;

import de.s2.gsim.core.DefinitionEnvironment;
import de.s2.gsim.objects.AppAgent;
import de.s2.gsim.objects.ObjectInstanceIF;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.sim.agent.AgentState;
import de.s2.gsim.sim.communication.AgentType;
import de.s2.gsim.sim.engine.DataHandler;
import de.s2.gsim.sim.engine.GSimEngineException;
import de.s2.gsim.sim.engine.ModelState;
import de.s2.gsim.sim.engine.Saveable;
import de.s2.gsim.sim.engine.SimulationID;
import de.s2.gsim.sim.engine.Steppable;
import gsim.core.impl.EnvLocalImpl;
import gsim.def.Environment;
import gsim.def.objects.Instance;
import gsim.def.objects.agent.GenericAgent;
import gsim.sim.agent.ApplicationAgentImpl;
import gsim.sim.agent.RuntimeAgent;
import gsim.sim.agent.RuntimeAgentState;
import gsim.sim.behaviour.SimAction;
import gsim.sim.engine.common.RuntimeAgentFactory;
import gsim.sim.engine.common.ScenarioEvent;
import gsim.sim.engine.common.SimpleClassLoader;

/**
 * Local implementation, and runs for the most part as one would expect from a standalone simulation.
 *
 */
public class ModelCoordinatorLocal implements ModelState, Steppable, Saveable {

    private static Logger logger = Logger.getLogger(ModelCoordinatorLocal.class);

    private HashMap<String, RuntimeAgent> agents = new HashMap<String, RuntimeAgent>();

    private HashMap<String, ApplicationAgentImpl> appAgents = new HashMap<String, ApplicationAgentImpl>();

    private HashMap<String, DataHandler> dataHandlers = new HashMap<String, DataHandler>();

    private String dbName = null;

    private Environment env;

    private HashMap events = new HashMap();

    // private HashMap agentOrder = new HashMap();

    private String host = "localhost";

    private SimulationID id = null;

    private LocalMessenger messenger;

    private String ns = null;

    private String pass = null;

    private HashMap pauseIntervals = null;

    private HashMap<String, Object> props = new HashMap<String, Object>();

    private ArrayList<String> removeList = new ArrayList<String>();

    private long time = 0;

    private double totalDuration = 0;

    private String user = null;

    public ModelCoordinatorLocal(Environment env, HashMap<String, Object> props) {

        this.props = props;
        this.env = env;

        try {
            pauseIntervals = env.getAgentIntervals();
            id = new SimulationID(env.getNamespace());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (props.containsKey("DB_HOST")) {
            host = (String) props.get("DB_HOST");
            if (host != null && host.equals("null")) {
                host = null;
            }
            logger.debug("db:" + host);
        }

        dbName = (String) props.get("DB_NAME");
        user = (String) props.get("DB_USER");
        pass = (String) props.get("DB_PASS");
        if (user == null) {
            user = "sa";
        }
        if (dbName == null) {
            dbName = "northwind";
        }
        if (pass == null) {
            pass = "";
        }
        String managerClass = (String) props.get("DB_MANAGER");
        if (managerClass == null) {
            managerClass = "Postgres";
        }
        Boolean b = (Boolean) props.get("DB_CLEAR");

        if (b != null && b.booleanValue()) {
            gsim.sim.engine.common.DatabaseManager m = null;
            if (managerClass.equals("Postgres")) {
                m = gsim.sim.engine.common.DatabaseManagerPostgres.getInstance();
            } else {
                m = gsim.sim.engine.common.DatabaseManagerMySQL.getInstance();
            }
            java.sql.Connection con = null;
            try {

                logger.debug("host=" + host + "db=" + dbName + ",user=" + user + ",pw=" + pass);

                con = m.getConnection(host, dbName, user, pass);
                DatabaseMetaData d = con.getMetaData();
                ResultSet res = d.getTables(null, "*", null, null);
                while (res.next()) {
                    String tableName = res.getString("TABLE_NAME");
                    Statement s = con.createStatement();
                    s.execute("Delete from " + tableName);
                    s.close();
                }

            } catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            } finally {
                if (con != null) {
                    m.releaseConnection(host, dbName, con);
                }
            }

        }

        try {
            ns = env.getNamespace() + "/" + getId().toString();
            // this.agentOrder = env.getAgentOrdering();

            ArrayList<AgentType> r = new ArrayList<AgentType>();

            String[] path = (String[]) props.get("jars");
            SimpleClassLoader cl = new SimpleClassLoader(path);
            RuntimeAgentFactory factory = new RuntimeAgentFactory(cl);
            SimAction.putCL(env.getNamespace(), cl);

            logger.debug("===== Free memory before agent-create: " + Runtime.getRuntime().freeMemory() / 1024d);

            RuntimeAgent[] a = factory.createAgentsWithRulebase(env, getId().toString(), props);

            for (RuntimeAgent element : a) {
                r.add(element);
                if (element.inheritsFrom("GP")) {
                }
                agents.put(element.getName(), element);
            }

            logger.debug("===== Free memory after agent-create: " + Runtime.getRuntime().freeMemory() / 1024d);

            List<ApplicationAgentImpl> ap = factory.createAppAgents(env);

            dataHandlers = (HashMap<String, DataHandler>) factory.createDataHandlers(env);

            for (ApplicationAgentImpl app : ap) {
                r.add(app);
                appAgents.put(app.getName(), app);
            }
            messenger = new LocalMessenger(r);
        } catch (gsim.def.GSimDefException e) {
            logger.error("Def-exception", e);
        } catch (Exception e2) {
            logger.error("Exception", e2);
        }

        for (RuntimeAgent mr : agents.values()) {
            mr.setMessagingComponent(messenger);
        }
        for (ApplicationAgentImpl mr : appAgents.values()) {
            mr.setMessengerRef(messenger);
            mr.setCoordinatorRef(this);
        }

    }

    // public void addAgent(RuntimeAgent agent) {
    // agents.put(agent.getName(), agent);
    // }

    public void addAppAgent(ApplicationAgentImpl obj) {
        appAgents.put(obj.getName(), obj);
    }

    @Override
    public String addNewAgentToRunningModel(String agentClass, String name, int method, double svar) throws GSimEngineException {
        try {
            GenericAgent a = null;
            if (name == null) {
                env.instanciateAgent(env.getAgentSubClass(agentClass), "new-" + agentClass + "-" + new Random().nextInt(), method, svar);
            } else {
                a = env.instanciateAgent(env.getAgentSubClass(agentClass), name, method, svar);
            }

            String[] path = (String[]) props.get("jars");
            SimpleClassLoader cl = new SimpleClassLoader(path);
            RuntimeAgentFactory f = new RuntimeAgentFactory(cl);
            // HashMap agentRtMappings = this.env.getRuntimeRoleMappings();
            // HashMap agentMappings = this.env.getAgentMappings();
            String simId = env.getNamespace() + "/" + id;
            RuntimeAgent ra = f.createAgentWithRulebase(a, /* agentRtMappings, agentMappings, */ agents.size(), simId, props);
            messenger.addAgentToHandle(ra);
            ra.setMessagingComponent(messenger);
            agents.put(ra.getName(), ra);
            return ra.getName();
        } catch (Exception e) {
            throw new GSimEngineException(e);
        }
    }

    @Override
    public void destroy() {
        // for (RuntimeAgent a : this.agents.values()) {
        // a.destroy();
        // }
        agents.clear();
        appAgents.clear();
        events.clear();
    }

    @Override
    public void disconnect() throws GSimEngineException {
    }

    @Override
    public int getAgentCount() throws GSimEngineException {
        return agents.size();
        // throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public String[] getAgentNames() {
        Iterator iter = agents.values().iterator();
        ArrayList<String> list = new ArrayList<String>();
        while (iter.hasNext()) {
            RuntimeAgent a = (RuntimeAgent) iter.next();
            list.add(a.getName());
        }
        String[] res = new String[list.size()];
        list.toArray(res);
        return res;
    }

    @Override
    public AgentState getAgentState(String agentName) {
        RuntimeAgent a = agents.get(agentName);
        if (a != null) {
            return new RuntimeAgentState(a);
        } else {
            return null;
        }
    }

    @Override
    public AppAgent[] getAppAgentState() {
        Iterator iter = appAgents.values().iterator();
        ArrayList<ApplicationAgentImpl> list = new ArrayList<ApplicationAgentImpl>();
        while (iter.hasNext()) {
            ApplicationAgentImpl a = (ApplicationAgentImpl) iter.next();
            list.add(a);
        }
        ApplicationAgentImpl[] res = new ApplicationAgentImpl[list.size()];
        list.toArray(res);
        return res;
    }

    @Override
    public AppAgent getAppAgentState(String name) {
        AppAgent a = appAgents.get(name);
        return a;
    }

    public double getAverageStepTime() {
        return 0;
    }

    @Override
    public DataHandler getDataHandler(String name) {
        DataHandler a = dataHandlers.get(name);
        return a;
    }

    @Override
    public DataHandler[] getDataHandlers() {
        DataHandler[] a = new DataHandler[dataHandlers.size()];
        dataHandlers.values().toArray(a);
        return a;
    }

    @Override
    public DefinitionEnvironment getDefinitionEnvironment() throws GSimEngineException {
        EnvLocalImpl impl = new EnvLocalImpl(env);
        return impl;
    }

    public gsim.def.Environment getEnvironment() throws GSimEngineException, Exception {
        return env;
    }

    @Override
    public List<AgentState> getGlobalState() throws GSimEngineException {
        Iterator iter = agents.values().iterator();
        ArrayList<AgentState> list = new ArrayList<AgentState>();
        while (iter.hasNext()) {
            RuntimeAgent a = (RuntimeAgent) iter.next();
            list.add(new RuntimeAgentState(a));
        }
        return list;
    }

    @Override
    public List<AgentState> getGlobalState(int count, int offset) throws GSimEngineException {

        ArrayList<RuntimeAgent> allList = new ArrayList<RuntimeAgent>();

        ArrayList<AgentState> list = new ArrayList<AgentState>();

        if (offset > agents.size()) {

            return list;
        }

        allList.addAll(agents.values());

        int toIndex = allList.size() - offset < count ? allList.size() : count + offset;
        List<RuntimeAgent> subList = allList.subList(offset, toIndex);

        Iterator iter = subList.iterator();

        while (iter.hasNext() && list.size() < count) {
            RuntimeAgent a = (RuntimeAgent) iter.next();
            list.add(new RuntimeAgentState(a));
        }
        return list;

    }

    @Override
    public SimulationID getId() {
        return id;
    }

    public String getNamespace() {
        return ns;
    }

    @Override
    public String getNameSpace() {
        return ns;
    }

    @Override
    public SimulationID getSimulationID() throws GSimEngineException {
        return id;
    }

    @Override
    public int getTimeStep() throws GSimEngineException {
        return (int) time;
    }

    public void init(Environment env) {
    }

    public boolean isMaster() {
        return true;
    }

    @Override
    public void modifyAgentState(AgentState agentState) throws GSimEngineException {
        RuntimeAgent agent = agents.get(agentState.getAgentName());
        for (String list : agentState.getAgentObjectListNames()) {
            for (ObjectInstanceIF inst : agentState.getAgentObjects(list)) {
                agent.setChildInstance(list, (Instance) inst);
            }
        }
        for (String list : agentState.getAgentAttributesListNames()) {
            for (Attribute a : agentState.getAgentAttributes(list)) {
                agent.setAttribute(list, a);
            }
        }
    }

    @Override
    public void postStep() {
        applyPostAppAgents();
        for (RuntimeAgent a : agents.values()) {
            a.post();
        }
        // if agents have been removed, remove them
        while (removeList.size() > 0) {
            String s = removeList.remove(0);
            agents.remove(s);
        }
    }

    @Override
    public void preStep(long time) {
        this.time = time;

        Iterator iter = agents.values().iterator();
        while (iter.hasNext()) {
            RuntimeAgent a = (RuntimeAgent) iter.next();
            a.setTime((int) time);
            a.initStep();
        }

        applyEvents();
        applyPreAppAgents();

    }

    @Override
    public void removeAgent(String agentName) {
        removeList.add(agentName);
    }

    @Override
    public void setNameSpace(String ns) {
        this.ns = ns;
    }

    @Override
    public void step() {

        logger.debug("==================== STEP " + time + " ====================");
        double l = System.currentTimeMillis();

        HashMap<String, Object> map = createGlobals();
        Iterator iter = agents.values().iterator();

        iter = agents.values().iterator();

        List<String> ordered = orderRoles();
        for (String roleName : ordered) {
            int interval = Integer.parseInt(getPauseInterval(roleName));
            if ((time) % interval == 0) {
                iter = agents.values().iterator();
                while (iter.hasNext()) {
                    RuntimeAgent a = (RuntimeAgent) iter.next();
                    a.execute(map, roleName);
                }
            }
        }

        double l2 = System.currentTimeMillis();
        totalDuration += (l2 - l) / 1000d;

        double avg = totalDuration / (time + 1);
        logger.info("==== Simulation " + ns + " : STEP " + time + " - DURATION was: " + (l2 - l) / 1000d + " seconds (average: " + avg
                + ") ====================");
    }

    @Override
    public void step(String roleName) {
        Iterator iter = agents.values().iterator();
        HashMap<String, Object> map = createGlobals();
        map.put("INTERVAL", getPauseInterval(roleName));

        int interval = Integer.parseInt(getPauseInterval(roleName));
        if ((time) % interval == 0) {
            while (iter.hasNext()) {
                RuntimeAgent a = (RuntimeAgent) iter.next();
                a.init(map);
            }
            while (iter.hasNext()) {
                RuntimeAgent a = (RuntimeAgent) iter.next();
                a.execute(map, roleName);
            }
        }
    }

    private void applyEvents() {
        Iterator iter = events.values().iterator();
        while (iter.hasNext()) {
            ScenarioEvent e = (ScenarioEvent) iter.next();
            e.setCoordinatorRef(this);// !!!
            e.execute();
        }
    }

    private void applyPostAppAgents() {
        long l = System.currentTimeMillis();
        Iterator iter = appAgents.values().iterator();
        while (iter.hasNext()) {
            ApplicationAgentImpl e = (ApplicationAgentImpl) iter.next();
            e.setCoordinatorRef(this);// !!!
            e.setMessengerRef(messenger);
            e.post();
        }

        iter = dataHandlers.values().iterator();
        while (iter.hasNext()) {
            DataHandler e = (DataHandler) iter.next();

            String managerClass = (String) props.get("DB_MANAGER");
            if (managerClass == null) {
                managerClass = "Postgres";
            }

            gsim.sim.engine.common.DatabaseManager m = null;
            if (managerClass.equalsIgnoreCase("Postgres")) {
                m = gsim.sim.engine.common.DatabaseManagerPostgres.getInstance();
            } else if (managerClass.equalsIgnoreCase("HSQL")) {
                m = gsim.sim.engine.common.DatabaseManagerHSQL.getInstance();
            }
            if (host != null && dbName != null && user != null && pass != null) {
                java.sql.Connection con = null;
                try {
                    con = m.getConnection(host, dbName, user, pass);
                    e.save(this, con);
                } catch (Exception ex) {
                    logger.error("Error during getting database connection", ex);
                } finally {
                    try {
                        if (con != null && !con.isClosed()) {
                            m.releaseConnection(host, dbName, con);
                        }
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        logger.debug("*********** POST: " + (System.currentTimeMillis() - l) / 1000d);
    }

    private void applyPreAppAgents() {
        long l = System.currentTimeMillis();
        Iterator iter = appAgents.values().iterator();
        while (iter.hasNext()) {
            ApplicationAgentImpl e = (ApplicationAgentImpl) iter.next();
            e.setCoordinatorRef(this);// !!!
            e.setMessengerRef(messenger);
            e.pre(props);
        }
        logger.debug("*********** PRE: " + (System.currentTimeMillis() - l) / 1000d);
    }

    private HashMap<String, Object> createGlobals() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("AGENT_COUNT", String.valueOf(agents.size()));
        map.put("TIME", time);
        return map;
    }

    /*
     * private String[] getOrderedContexts() { String[] ordered = new String[this.agentOrder.size()];
     * 
     * Iterator iter = this.agentOrder.keySet().iterator(); while (iter.hasNext()) { String name = (String) iter.next(); int order = (Integer)
     * this.agentOrder.get(name) - 1; ordered[order] = name; } return ordered; }
     */
    private String getPauseInterval(String role) {
        try {
            if (pauseIntervals.containsKey(role)) {
                String s = (String) pauseIntervals.get(role);
                if (s.equals("?*agent-count*")) {
                    return String.valueOf(agents.size());
                } else {
                    return s;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1";
    }

    private List<String> orderRoles() {
        ArrayList<String> ordered = new ArrayList<String>();
        ArrayList<String> allAgents = new ArrayList<String>();
        try {
            HashMap<String, Integer> defined = env.getAgentOrdering();
            ArrayList<Integer> al = new ArrayList<Integer>(defined.values());
            Collections.sort(al);
            for (int o : al) {
                for (Entry<String, Integer> e : defined.entrySet()) {
                    if (e.getValue().equals(o)) {
                        if (!ordered.contains(e.getKey())) {
                            ordered.add(e.getKey());
                        }
                    }
                }
            }
            for (String s : allAgents) {
                if (!ordered.contains(s)) {
                    ordered.add(s);
                }
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return ordered;
    }

}
