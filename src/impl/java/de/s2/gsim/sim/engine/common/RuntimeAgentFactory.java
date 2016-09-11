package de.s2.gsim.sim.engine.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.ApplicationAgentImpl;
import de.s2.gsim.api.sim.agent.impl.RtExecutionContextImpl;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GSimDefException;
import de.s2.gsim.environment.GenericAgent;
import de.s2.gsim.sim.DataHandler;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.impl.JessHandler;

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
            props.put("AGENT_COUNT", String.valueOf(a.length));

            for (GenericAgent element : a) {
                HashMap<String, String> map = new HashMap<String, String>();
                Frame def = element.getDefinition();
                List<Frame> anc = def.getAncestors();
                RuntimeAgent owner = new RuntimeAgent(element, ns + "/" + id);

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
                        rc.create("default", owner, def.getName());
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

    @SuppressWarnings("unchecked")
    public RuntimeAgent[] createAgentsWithRulebase(Environment env, String id, Map props) throws GSimEngineException {

        try {
            ArrayList agentList = new ArrayList();
            List<GenericAgent> a = env.getAgentInstanceOperations().getGenericAgents();

            props.put("AGENT_COUNT", String.valueOf(a.size()));

            for (GenericAgent element : a) {
                HashMap map = new HashMap();
                Frame def = element.getDefinition();
                List<Frame> anc = def.getAncestors();
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
                        rc.create("default", owner, def.getName());
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

    public RuntimeAgent createAgentWithRulebase(GenericAgent a, int agentCount, String simId, Map<String, Object> props) throws GSimEngineException {

        try {

            props.put("AGENT_COUNT", String.valueOf(agentCount));

            HashMap<String, String> map = new HashMap<String, String>();
            Frame def = a.getDefinition();
            List<Frame> anc = def.getAncestors();
            RuntimeAgent owner = new RuntimeAgent(a, simId);

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
                    rc.create("default", owner, def.getName());
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
            Map<String, String> m = env.getAgentRuntimeConfig().getSystemAgents();
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
            Map<String, String> m = env.getAgentRuntimeConfig().getSystemAgents();
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
