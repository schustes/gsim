package de.s2.gsim.sim.engine.common;

import de.s2.gsim.api.sim.agent.impl.RtExecutionContextImpl;
import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.environment.Environment;
import de.s2.gsim.environment.Frame;
import de.s2.gsim.environment.GenericAgent;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.engine.BehaviourEngine;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RuntimeAgentFactory {

    private static Logger logger = Logger.getLogger(RuntimeAgentFactory.class);

    private SimpleClassLoader cl = null;

    public RuntimeAgentFactory(SimpleClassLoader cl) {
        this.cl = cl;
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
                RuntimeAgent owner = RuntimeAgent.runtimeAgent(element,  env.getNamespace() + "/" + id);
                //new RuntimeAgent(element, env.getNamespace() + "/" + id);

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

                BehaviourEngine handler = new BehaviourEngine(owner, props);// RulebasePool.getInstance().get();
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
            RuntimeAgent owner = RuntimeAgent.runtimeAgent(a, simId);//new RuntimeAgent(a, simId);

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

            BehaviourEngine handler = new BehaviourEngine(owner, props);
            owner.setRuleHandler(handler);

            return owner;

        } catch (Exception e) {
            throw new GSimEngineException(e);
        }
    }

}
