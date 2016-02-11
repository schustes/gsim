package gsim.sim.engine.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.s2.gsim.sim.engine.DataHandler;
import de.s2.gsim.sim.engine.GSimEngineException;
import gsim.def.Environment;
import gsim.def.GSimDefException;
import gsim.def.objects.Frame;
import gsim.def.objects.agent.GenericAgent;
import gsim.sim.agent.ApplicationAgentImpl;
import gsim.sim.agent.RtExecutionContextImpl;
import gsim.sim.agent.RuntimeAgent;
import gsim.sim.behaviour.impl.JessHandler;

public class RuntimeAgentFactory {

    private static Logger logger = Logger.getLogger(RuntimeAgentFactory.class);

    private SimpleClassLoader cl = null;

    public RuntimeAgentFactory(SimpleClassLoader cl) {
        this.cl = cl;
    }

    public RuntimeAgent[] createAgentsWithoutRulebase(String ns, GenericAgent[] a, /* HashMap agentRtMappings, HashMap agentMappings, */
            String id, HashMap<String, Object> props) throws GSimEngineException {

        try {
            ArrayList<RuntimeAgent> agentList = new ArrayList<RuntimeAgent>();
            // HashMap agentRtMappings=env.getRuntimeRoleMappings();
            // HashMap agentMappings=env.getAgentMappings();

            // GenericAgent[] a = env.getGenericAgents();

            props.put("AGENT_COUNT", String.valueOf(a.length));

            for (GenericAgent element : a) {
                HashMap<String, String> map = new HashMap<String, String>();
                Frame def = element.getDefinition();
                Frame[] anc = def.getAncestors();
                RuntimeAgent owner = new RuntimeAgent(element, ns + "/" + id);

                /*
                 * Iterator iter = agentMappings.keySet().iterator(); while (iter.hasNext()) { String agentClassName = (String) iter.next(); if
                 * (def.isSuccessor(agentClassName)) { for (Frame element0 : anc) {
                 * 
                 * if (element0.getTypeName().equals(agentClassName)) { String[] roles = (String[]) agentMappings.get(agentClassName); for (String r :
                 * roles) { String cls = (String) agentRtMappings.get(r.trim()) + "@" + new Random().nextInt(); map.put(cls, r + ":" +
                 * agentClassName); } } } } else if (def.getTypeName().equals(agentClassName)) {
                 * 
                 * String[] roles = (String[]) agentMappings.get(agentClassName); if (roles != null) { for (String role : roles) { String roleName =
                 * role.trim(); String cls = (String) agentRtMappings.get(roleName) + "@" + new Random().nextInt(); map.put(cls, roleName + ":" +
                 * agentClassName); } } else { String cls = "gsim.sim.agent.RtExecutionContext"; map.put(cls, "default" + ":" + agentClassName); } } }
                 */
                if (map.size() > 0) {

                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String cls = (String) it.next();
                        String roleAndClass = map.get(cls);
                        String[] rr = roleAndClass.split(":");
                        try {
                            cls = cls.split("@")[0];
                            RtExecutionContextImpl rc = null;
                            try {
                                rc = (RtExecutionContextImpl) Class.forName(cls, false, cl).newInstance();
                            } catch (ClassNotFoundException e) {
                                rc = (RtExecutionContextImpl) Class.forName(cls).newInstance();
                            }
                            rc.create(rr[0], owner, rr[1]);
                            owner.addOrSetExecutionContext(rr[0], rc);
                        } catch (ClassNotFoundException e) {
                            logger.error("Class not found:" + cls, e);
                        } catch (Exception e) {
                            logger.error("Exception", e);
                        }
                    }
                } else {
                    try {
                        RtExecutionContextImpl rc = new RtExecutionContextImpl();
                        rc.create("default", owner, def.getTypeName());
                        owner.addOrSetExecutionContext("default", rc);
                    } catch (Exception e) {
                        logger.error("Exception", e);
                    }
                }

                agentList.add(owner);
            }
            RuntimeAgent[] res = new RuntimeAgent[agentList.size()];
            agentList.toArray(res);
            return res;
        } catch (Exception e) {
            throw new GSimEngineException(e);
        }
    }

    // public RuntimeAgent[] createAgentsWithoutRulebase(Environment env, String
    // id, HashMap props) throws GSimEngineException {
    // return this.createAgentsWithoutRulebase(env, id, props, 0);
    // }

    @SuppressWarnings("unchecked")
    public RuntimeAgent[] createAgentsWithRulebase(Environment env, String id, HashMap props) throws GSimEngineException {

        try {
            ArrayList agentList = new ArrayList();
            // HashMap agentRtMappings = env.getRuntimeRoleMappings();
            // HashMap agentMappings = env.getAgentMappings();

            /*
             * if (agentMappings.size() == 0) { agentMappings.put(GenericAgentClass.NAME, "default"); //agentRtMappings.put("default",
             * "gsim.sim.agent.RtExecutionContext"); HashMap order = env.getAgentOrdering(); order.put("default", 1); }
             */
            GenericAgent[] a = env.getGenericAgents();

            props.put("AGENT_COUNT", String.valueOf(a.length));

            for (GenericAgent element : a) {
                HashMap map = new HashMap();
                Frame def = element.getDefinition();
                Frame[] anc = def.getAncestors();
                RuntimeAgent owner = new RuntimeAgent(element, env.getNamespace() + "/" + id);

                if (map.size() > 0) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String cls = (String) it.next();
                        String roleAndClass = (String) map.get(cls);
                        String[] rr = roleAndClass.split(":");
                        try {
                            cls = cls.split("@")[0];
                            RtExecutionContextImpl rc = null;
                            try {
                                rc = (RtExecutionContextImpl) Class.forName(cls, false, cl).newInstance();
                            } catch (ClassNotFoundException e) {
                                rc = (RtExecutionContextImpl) Class.forName(cls).newInstance();
                            }
                            rc.create(rr[0], owner, rr[1]);
                            owner.addOrSetExecutionContext(rr[0], rc);
                        } catch (ClassNotFoundException e) {
                            logger.error("Class not found:" + cls, e);
                        } catch (Exception e) {
                            logger.error("Exception", e);
                        }
                    }
                } else {
                    try {
                        RtExecutionContextImpl rc = new RtExecutionContextImpl();
                        rc.create("default", owner, def.getTypeName());
                        owner.addOrSetExecutionContext("default", rc);
                    } catch (Exception e) {
                        logger.error("Exception", e);
                    }
                }

                // only possible after all rtcontexts have been created!

                JessHandler handler = new JessHandler(owner, props);// RulebasePool.getInstance().get();
                owner.setRuleHandler(handler);

                agentList.add(owner);
            }

            RuntimeAgent[] res = new RuntimeAgent[agentList.size()];
            agentList.toArray(res);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new GSimEngineException(e);
        }
    }

    public RuntimeAgent createAgentWithRulebase(GenericAgent a, /* HashMap agentRtMappings, HashMap agentMappings, */ int agentCount, String simId,
            HashMap<String, Object> props) throws GSimEngineException {

        try {

            props.put("AGENT_COUNT", String.valueOf(agentCount));

            HashMap<String, String> map = new HashMap<String, String>();
            Frame def = a.getDefinition();
            Frame[] anc = def.getAncestors();
            RuntimeAgent owner = new RuntimeAgent(a, simId);

            /*
             * Iterator iter = agentMappings.keySet().iterator(); while (iter.hasNext()) { String agentClassName = (String) iter.next(); if
             * (def.isSuccessor(agentClassName)) { for (Frame element : anc) { if (element.getTypeName().equals(agentClassName)) { //String role =
             * (String) agentMappings.get(agentClassName); //String cls = (String) agentRtMappings.get(role.trim()) + "@"+ new Random().nextInt();
             * //map.put(cls, role + ":" + agentClassName); } } } else if (def.getTypeName().equals(agentClassName)) { //String role = (String)
             * agentMappings.get(agentClassName); //String roleName = role.trim(); //String cls = (String) agentRtMappings.get(roleName) + "@" + new
             * Random().nextInt(); //map.put(cls, roleName + ":" + agentClassName); } }
             */
            if (map.size() > 0) {
                Iterator it = map.keySet().iterator();
                while (it.hasNext()) {
                    String cls = (String) it.next();
                    String roleAndClass = map.get(cls);
                    String[] rr = roleAndClass.split(":");
                    try {
                        cls = cls.split("@")[0];
                        // RtExecutionContext rc = (RtExecutionContext) Class.forName(cls,
                        // false, cl).newInstance();
                        RtExecutionContextImpl rc = null;
                        try {
                            rc = (RtExecutionContextImpl) Class.forName(cls, false, cl).newInstance();
                        } catch (ClassNotFoundException e) {
                            rc = (RtExecutionContextImpl) Class.forName(cls).newInstance();
                        }

                        rc.create(rr[0], owner, rr[1]);
                        owner.addOrSetExecutionContext(rr[0], rc);
                    } catch (ClassNotFoundException e) {
                        logger.error("Class not found:" + cls, e);
                    } catch (Exception e) {
                        logger.error("Exception", e);
                    }
                }
            } else {
                try {
                    RtExecutionContextImpl rc = new RtExecutionContextImpl();
                    rc.create("default", owner, def.getTypeName());
                    owner.addOrSetExecutionContext("default", rc);
                } catch (Exception e) {
                    logger.error("Exception", e);
                }
            }

            JessHandler handler = new JessHandler(owner, props);
            owner.setRuleHandler(handler);

            return owner;

        } catch (Exception e) {
            throw new GSimEngineException(e);
        }
    }

    public List<ApplicationAgentImpl> createAppAgents(Environment env) throws GSimDefException {
        try {
            HashMap<String, String> m = env.getSystemAgents();
            Iterator iter = m.keySet().iterator();
            ArrayList<ApplicationAgentImpl> agentList = new ArrayList<ApplicationAgentImpl>();
            while (iter.hasNext()) {
                String name = (String) iter.next();
                String cls = m.get(name);
                ApplicationAgentImpl a = null;
                try {
                    try {
                        Object o = Class.forName(cls, false, cl).newInstance();
                        if (o instanceof ApplicationAgentImpl) {
                            a = (ApplicationAgentImpl) o;
                        }
                    } catch (ClassNotFoundException e) {
                        Object o = Class.forName(cls).newInstance();
                        if (o instanceof ApplicationAgentImpl) {
                            a = (ApplicationAgentImpl) o;
                        }
                    }
                    if (a != null) {
                        a.setName(name);
                        agentList.add(a);
                    }
                } catch (Exception e) {
                    logger.error("Exception", e);
                }
            }
            return agentList;
        } catch (Exception e) {
            throw new GSimDefException("Exception", e);
        }
    }

    public Map<String, DataHandler> createDataHandlers(Environment env) throws GSimDefException {
        try {
            HashMap<String, String> m = env.getSystemAgents();
            Iterator iter = m.keySet().iterator();
            HashMap<String, DataHandler> agentList = new HashMap<String, DataHandler>();
            while (iter.hasNext()) {
                String name = (String) iter.next();
                String cls = m.get(name);
                DataHandler a = null;
                try {
                    try {
                        Object o = Class.forName(cls, false, cl).newInstance();
                        if (o instanceof DataHandler) {
                            a = (DataHandler) o;
                        }
                    } catch (ClassNotFoundException e) {
                        Object o = Class.forName(cls).newInstance();
                        if (o instanceof DataHandler) {
                            a = (DataHandler) o;
                        }
                    }
                    if (a != null) {
                        agentList.put(name, a);
                    }
                } catch (Exception e) {
                    logger.error("Exception", e);
                }
            }
            return agentList;
        } catch (Exception e) {
            throw new GSimDefException("Exception", e);
        }
    }

}
