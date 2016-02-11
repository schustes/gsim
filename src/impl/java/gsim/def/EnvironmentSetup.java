package gsim.def;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import de.s2.gsim.objects.attribute.AttributeConstants;
import de.s2.gsim.objects.attribute.DomainAttribute;
import gsim.def.objects.Frame;
import gsim.def.objects.agent.GenericAgentClass;
import gsim.def.objects.behaviour.ActionFrame;
import gsim.def.objects.behaviour.ConditionFrame;
import gsim.def.objects.behaviour.DependencyTestFrame;
import gsim.def.objects.behaviour.ExpansionFrame;
import gsim.def.objects.behaviour.RLRuleFrame;
import gsim.def.objects.behaviour.UserRuleFrame;
import gsim.util.Utils;

public class EnvironmentSetup extends EnvironmentBase {

    private static final long serialVersionUID = 1L;

    private HashMap<String, ActionFrame> actionCollections = new HashMap<String, ActionFrame>();

    private Document doc = null;

    private HashMap<String, Element> inheritingAgents = new HashMap<String, Element>();

    private HashMap<String, Element> inheritingObjects = new HashMap<String, Element>();

    private Logger logger = Logger.getLogger(EnvironmentSetup.class);

    private HashMap<String, Element> topLevelAgents = new HashMap<String, Element>();

    private HashMap<String, Element> topLevelObjects = new HashMap<String, Element>();

    public EnvironmentSetup() {
    }

    public EnvironmentSetup(InputStream file) {
        if (file != null) {
            try {
                SAXBuilder builder = new SAXBuilder();
                doc = builder.build(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new NullPointerException("No input file");
        }
    }

    public void runSetup() throws GSimDefException {
        try {
            if (doc == null) {
                return;
            }
            Element imports = doc.getRootElement().getChild("import");
            if (imports != null) {
                String[] files = imports.getAttributeValue("files").split(",");
                for (String s : files) {
                    String fileName = s.trim();
                    try {
                        EnvironmentSetup rek = new EnvironmentSetup(new FileInputStream(fileName));
                        rek.actionCollections = actionCollections;
                        rek.runSetup();
                        topLevelAgents.putAll(rek.topLevelAgents);
                        topLevelObjects.putAll(rek.topLevelObjects);
                        inheritingAgents.putAll(rek.inheritingAgents);
                        inheritingObjects.putAll(rek.inheritingObjects);
                        agentRtClassMappings.putAll(rek.agentRtClassMappings);
                        systemAgents.putAll(rek.systemAgents);
                        agentRtClassMappings.putAll(rek.agentRtClassMappings);
                        agentOrder.putAll(rek.agentOrder);
                        systemAgents.putAll(rek.systemAgents);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            loadPossiblyNewActions();
            readSystemAgents();
            buildExecutionOrder(doc);
            readDataHandlers(doc);
            putAgentsToLists(doc);
            putObjectsToLists(doc);
            createAgentSkeletons();
            createObjects();
            HashMap<String, Element> resolved = new HashMap<String, Element>(topLevelAgents);
            resolved.putAll(inheritingAgents);
            Iterator iter = resolved.values().iterator();
            while (iter.hasNext()) {
                Element e = (Element) iter.next();
                String name = e.getAttributeValue("name");

                GenericAgentClass frame = getAgentSubClass(name);
                if (frame != null) {// other wise not resolvable
                    GenericAgentClass c = frame;

                    if (e.getChild("object-lists") != null) {

                        Iterator it = e.getChild("object-lists").getChildren().iterator();
                        while (it.hasNext()) {
                            Element list = (Element) it.next();

                            Frame object = getObjectSubClass(list.getAttributeValue("type"));

                            c.defineObjectList(list.getAttributeValue("name"), object);
                            agentSubClasses.add(c);

                            GenericAgentClass[] suc = getAllAgentClassSuccessors(c.getTypeName());
                            for (GenericAgentClass s : suc) {
                                s.setAncestor(c);
                                agentSubClasses.add(s);
                            }

                            if (list.hasChildren()) {

                                Iterator it2 = list.getChildren("object").iterator();
                                while (it2.hasNext()) {
                                    Element o = (Element) it2.next();
                                    String nName = o.getAttribute("name") == null ? object.getTypeName() : o.getAttributeValue("name");
                                    Frame child = new Frame(new Frame[] { object }, nName, object.getCategory());// new Frame(nName, object,
                                    // this.getUniqueId());

                                    if (o.hasChildren()) {
                                        Iterator it3 = o.getChildren().iterator();// list elem of object
                                        while (it3.hasNext()) {
                                            Element listElem = (Element) it3.next();
                                            String listName = listElem.getAttributeValue("name");
                                            // logger.debug(listElem.getName());
                                            Iterator it4 = listElem.getChildren().iterator();// attrs, e.g. SET
                                            while (it4.hasNext()) {
                                                Element attr = (Element) it4.next();

                                                // Iterator it5 = listProper.getChildren().iterator();// attribte-lists
                                                // while (it5.hasNext()) {
                                                // Element x = (Element) it5.next();
                                                DomainAttribute da = createAttFromXML(attr, true);

                                                if (list.getAttribute("mutable") != null) {
                                                    da.setMutable(Boolean.parseBoolean(list.getAttributeValue("mutable")));
                                                }
                                                child.addOrSetAttribute(listName, da);
                                            }
                                        }
                                    }
                                    addChildObject(c, new String[] { list.getAttributeValue("name") }, child);
                                }
                            }
                        }
                    }

                }
                if (frame != null && frame.getBehaviour() != null && frame.getBehaviour().getRLRule() != null) {
                    checkExpansionRefs(frame);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GSimDefException("Setup error", e);
        }
    }

    public void setInputStream(InputStream file) {
        if (file != null) {
            try {
                SAXBuilder builder = new SAXBuilder();
                doc = builder.build(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new NullPointerException("No input file");
        }
    }

    private void addClassifiers(GenericAgentClass agent, Element nodesElem) throws GSimDefException {

        Element slearning = nodesElem.getChild("state-learning");
        if (slearning != null) {
            try {
                if (slearning.getAttribute("max-nodes") != null) {
                    agent.getBehaviour().setMaxNodes(slearning.getAttribute("max-nodes").getIntValue());
                }
                if (slearning.getAttribute("max-depth") != null) {
                    agent.getBehaviour().setMaxDepth(slearning.getAttribute("max-depth").getIntValue());
                }
                if (slearning.getAttribute("update-interval") != null) {
                    agent.getBehaviour().setStateUpdateInterval(slearning.getAttribute("update-interval").getIntValue());
                }
                if (slearning.getAttribute("delete-unused-after") != null) {
                    agent.getBehaviour().setDeleteUnusedAfter(slearning.getAttribute("delete-unused-after").getIntValue());
                }
                if (slearning.getAttribute("method") != null) {
                    agent.getBehaviour().setTraversalMode(slearning.getAttributeValue("method"));
                }
                if (slearning.getAttribute("revaluation-probability") != null) {
                    agent.getBehaviour().setRevalProb(Double.parseDouble(slearning.getAttributeValue("revaluation-probability")));
                }
                if (slearning.getAttribute("revisit-costfraction") != null) {
                    agent.getBehaviour().setRevisitCost(Double.parseDouble(slearning.getAttributeValue("revisit-costfraction")));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Iterator ito = nodesElem.getChildren("rl-node").iterator();

        while (ito.hasNext()) {
            Element nodeElem = (Element) ito.next();

            if (nodeElem.getChild("action-node-ref") == null && nodeElem.getChild("condition-node-ref") == null) {

                String ruleName = nodeElem.getAttribute("name").getValue();
                RLRuleFrame rf = agent.getBehaviour().createRLRule(ruleName);

                if (nodeElem.getAttribute("restrict-repeat-interval") != null) {
                    String repeat = nodeElem.getAttributeValue("restrict-repeat-interval");
                    rf.setRepeatedExecutionTest(repeat);
                }

                if (nodeElem.getAttribute("retract-osbolete-actions") != null) {
                    String doRetract = nodeElem.getAttributeValue("retract-osbolete-actions");
                    rf.setRetractObsolete(Boolean.parseBoolean(doRetract));
                } else {
                    rf.setRetractObsolete(false);
                }

                if (nodeElem.getChild("condition-nodes") != null) {
                    Iterator condIter = nodeElem.getChild("condition-nodes").getChildren("condition-node").iterator();
                    while (condIter.hasNext()) {
                        Element cE = (Element) condIter.next();
                        String param = cE.getAttribute("param").getValue();
                        String op = translate(cE.getAttribute("op").getValue());
                        String val = "";
                        if (cE.getAttribute("value") != null) {
                            val = cE.getAttribute("value").getValue();
                        }
                        ConditionFrame c = agent.getBehaviour().createCondition(param, op, val);
                        rf.addCondition(c);
                    }
                    condIter = nodeElem.getChild("condition-nodes").getChildren("expand-node").iterator();
                    while (condIter.hasNext()) {
                        Element cE = (Element) condIter.next();
                        String param = cE.getAttribute("param").getValue();

                        ExpansionFrame c = agent.getBehaviour().createExpansion(param);
                        if (cE.getAttribute("min") != null && cE.getAttribute("max") != null) {
                            String min = cE.getAttribute("min").getValue();
                            String max = cE.getAttribute("max").getValue();
                            c.setMin(min);
                            c.setMax(max);
                        }
                        rf.addExpansion(c);
                    }

                }

                Element actions = nodeElem.getChild("action-nodes");
                String[] actionrefs = actions.getAttributeValue("ref").split(",");
                for (String r : actionrefs) {
                    ActionFrame f = agent.getBehaviour().getAction(r.trim());
                    rf.addConsequence(f);
                }

                if (nodeElem.getChildren("selection-nodes") != null) {

                    Iterator sIter = nodeElem.getChildren("selection-nodes").iterator();
                    int i = 0;
                    while (sIter.hasNext()) {
                        Element sE = (Element) sIter.next();
                        Iterator scIter = sE.getChildren("selection-node").iterator();
                        while (scIter.hasNext()) {
                            Element cE = (Element) scIter.next();

                            String name = "selection-node-" + i;
                            if (cE.getAttribute("name") != null) {
                                name = cE.getAttributeValue("name");
                            }
                            UserRuleFrame f = agent.getBehaviour().createRule(name);

                            Iterator condIter = cE.getChildren("node-ref").iterator();
                            while (condIter.hasNext()) {
                                Element refElem = (Element) condIter.next();
                                String refString = refElem.getAttributeValue("ref");
                                String[] s0 = refString.split("\\$");
                                String actionRef = s0[1];

                                ActionFrame action = agent.getBehaviour().getAction(actionRef);
                                f.addConsequence(action);

                                String op = "";
                                if (refElem.getAttribute("op") != null) {
                                    op = translate(refElem.getAttributeValue("op"));
                                }
                                String val = "";
                                if (refElem.getAttribute("value") != null) {
                                    val = refElem.getAttribute("value").getValue();
                                }

                                ConditionFrame condition = agent.getBehaviour().createCondition(refString, op, val);

                                f.addCondition(condition);

                            }
                            i++;
                            rf.addSelectionRule(f);
                        }
                    }
                }

                Element learningEval = nodeElem.getChild("function");
                if (learningEval != null) {
                    String cond = learningEval.getAttribute("variable").getValue();
                    String op = ">=";

                    ConditionFrame c = null;
                    if (learningEval.getAttribute("alpha") != null) {
                        c = agent.getBehaviour().createCondition(cond, op, learningEval.getAttributeValue("alpha"));
                    } else {
                        c = agent.getBehaviour().createCondition(cond, op, "0.1");
                    }
                    rf.setEvaluationFunction(c);
                    if (learningEval.getAttribute("update-lag") != null) {
                        String exper = learningEval.getAttributeValue("update-lag");
                        rf.setUpdateLag(exper);
                    } else if (learningEval.getAttribute("update-span") != null) {
                        String exper = learningEval.getAttributeValue("update-span");
                        rf.setUpdateSpan(exper);
                    }
                }

                Element discount = nodeElem.getChild("discount");
                if (discount != null) {
                    try {
                        double d = discount.getAttribute("value").getDoubleValue();
                        rf.setDiscount(d);
                    } catch (DataConversionException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                Element defaultReward = nodeElem.getChild("default-reward");
                if (defaultReward != null) {
                    try {
                        double d = discount.getAttribute("value").getDoubleValue();
                        rf.setDefaultReward(d);
                    } catch (DataConversionException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                Element aDiscount = nodeElem.getChild("averaging");
                if (aDiscount != null) {
                    try {
                        double d = aDiscount.getAttribute("discount").getDoubleValue();
                        rf.setAvgStepSize(d);
                    } catch (DataConversionException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                Element selector = nodeElem.getChild("selector");
                if (selector != null) {
                    String d = selector.getAttributeValue("value");
                    if (d.equals("comparison")) {
                        rf.setComparison(true);
                    } else {
                        rf.setComparison(false);
                    }
                }

                Element learning = nodeElem.getChild("method");
                if (learning != null) {
                    String d = learning.getAttributeValue("value");
                    if (d.equals("Q")) {
                        rf.setMethod("Q");
                    } else {
                        rf.setMethod("NULL");
                    }
                }

                Iterator it = nodeElem.getChildren("dependency-test").iterator();
                while (it.hasNext()) {

                    Element elem = (Element) it.next();

                    String testName = elem.getAttributeValue("name");

                    Element forElem = elem.getChild("for-each");
                    String fat = forElem.getAttributeValue("action-type");
                    String fot = forElem.getAttributeValue("object-types");
                    String ft = forElem.getAttributeValue("time");

                    Element notExistsElem = forElem.getChild("not-exists");

                    Element actionType = notExistsElem.getChild("action-type");
                    Element objectType = notExistsElem.getChild("object-types");
                    Element time = notExistsElem.getChild("time");
                    String at = actionType.getAttributeValue("binding");
                    String ot = objectType.getAttributeValue("binding");
                    String t = time.getAttributeValue("binding");

                    Element test = notExistsElem.getChild("test-expression");
                    String testExpr = replace(test.getAttributeValue("jess"));

                    DependencyTestFrame dtf = new DependencyTestFrame(testName, at, ot, t, testExpr, fat, fot, ft);
                    rf.addDependcyTestFrame(dtf);

                }

                agent.getBehaviour().addRLRule(rf);

            }
        }
        addRLRefs(agent, nodesElem);
        addRLConditionRefs(agent, nodesElem);
    }

    private void addReactiveRules(GenericAgentClass agent, Element rules) throws NumberFormatException {

        Iterator ito = rules.getChildren("rule").iterator();
        while (ito.hasNext()) {
            Element e = (Element) ito.next();
            String ruleName = e.getAttribute("name").getValue();
            UserRuleFrame rf = agent.getBehaviour().createRule(ruleName);
            if (e.getChildren("condition") != null) {
                Iterator condIter = e.getChildren("condition").iterator();
                while (condIter.hasNext()) {
                    Element cE = (Element) condIter.next();
                    String param = cE.getAttribute("param").getValue();
                    String op = translate(cE.getAttribute("op").getValue());
                    String val = "";
                    if (cE.getAttribute("value") != null) {
                        val = cE.getAttribute("value").getValue();
                    }
                    ConditionFrame c = agent.getBehaviour().createCondition(param, op, val);
                    rf.addCondition(c);
                }
            }

            Element consequent = e.getChild("consequent");
            String action = consequent.getAttribute("ref").getValue().trim();
            ActionFrame f = agent.getBehaviour().getAction(action.trim());
            org.jdom.Attribute sal = consequent.getAttribute("salience");
            if (sal != null) {
                f.setSalience(Double.parseDouble(sal.getValue()));
            }
            rf.addConsequence(f);
            agent.getBehaviour().addRule(rf);
        }
    }

    private void addRLConditionRefs(GenericAgentClass agent, Element nodesElem) throws GSimDefException {

        HashMap<String, String> unresolved = new HashMap<String, String>();
        HashMap<String, Element> nodes = new HashMap<String, Element>();

        Iterator ito = nodesElem.getChildren("rl-node").iterator();

        while (ito.hasNext()) {
            Element nodeElem = (Element) ito.next();
            String name = nodeElem.getAttributeValue("name");
            if (nodeElem.getChild("condition-node-ref") != null) {
                unresolved.put(name, nodeElem.getChild("condition-node-ref").getAttributeValue("ref"));
                nodes.put(name, nodeElem);
            }
        }
        ito = nodesElem.getChildren("rl-node").iterator();

        HashMap<String, Integer> trials = new HashMap<String, Integer>();
        while (unresolved.size() > 0) {
            Iterator<String> it = unresolved.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();
                String refName = unresolved.get(name);
                RLRuleFrame a = agent.getBehaviour().getRLRule(refName);
                if (a != null) {
                    RLRuleFrame rf = new RLRuleFrame(name);
                    Element e = nodes.get(name);
                    if (e.getChild("condition-nodes") != null) {
                        Iterator cIter = e.getChild("condition-nodes").getChildren("condition-node").iterator();
                        while (cIter.hasNext()) {
                            Element cE = (Element) cIter.next();
                            String param = cE.getAttribute("param").getValue();
                            String op = translate(cE.getAttribute("op").getValue());
                            String val = "";
                            if (cE.getAttribute("value") != null) {
                                val = cE.getAttribute("value").getValue();
                            }
                            ConditionFrame c = agent.getBehaviour().createCondition(param, op, val);
                            rf.addCondition(c);
                        }
                    }
                    it.remove();
                    DomainAttribute da = new DomainAttribute("equivalent-actionset", AttributeConstants.STRING);
                    da.setDefault(refName);
                    rf.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, da);
                    agent.getBehaviour().addRLRule(rf);
                } else {
                    int k = 0;
                    if (trials.containsKey(name)) {
                        k = trials.get(name);
                    }
                    if (k > unresolved.size() + 1) {
                        throw new GSimDefException(
                                "Aborting RL-nodes generation by condition-reference, because node " + name + " cannot be resolved.");
                    }
                }

                int k = 0;
                if (trials.containsKey(name)) {
                    k = trials.get(name);
                }
                k++;
                trials.put(name, k);

            }
        }

    }

    private void addRLRefs(GenericAgentClass agent, Element nodesElem) throws GSimDefException {

        HashMap<String, String> unresolved = new HashMap<String, String>();
        HashMap<String, Element> nodes = new HashMap<String, Element>();

        Iterator ito = nodesElem.getChildren("rl-node").iterator();

        while (ito.hasNext()) {
            Element nodeElem = (Element) ito.next();
            String name = nodeElem.getAttributeValue("name");
            if (nodeElem.getChild("action-node-ref") != null) {
                unresolved.put(name, nodeElem.getChild("action-node-ref").getAttributeValue("ref"));
                nodes.put(name, nodeElem);
            }
        }
        ito = nodesElem.getChildren("rl-node").iterator();

        HashMap<String, Integer> trials = new HashMap<String, Integer>();
        while (unresolved.size() > 0) {
            Iterator<String> it = unresolved.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();
                String refName = unresolved.get(name);
                RLRuleFrame a = agent.getBehaviour().getRLRule(refName);
                if (a != null) {
                    RLRuleFrame rf = new RLRuleFrame(a, name);
                    for (ConditionFrame c : rf.getConditions()) {
                        rf.removeCondition(c);
                    }
                    Element e = nodes.get(name);
                    if (e.getChild("condition-nodes") != null) {
                        Iterator cIter = e.getChild("condition-nodes").getChildren("condition-node").iterator();
                        while (cIter.hasNext()) {
                            Element cE = (Element) cIter.next();
                            String param = cE.getAttribute("param").getValue();
                            String op = translate(cE.getAttribute("op").getValue());
                            String val = "";
                            if (cE.getAttribute("value") != null) {
                                val = cE.getAttribute("value").getValue();
                            }
                            ConditionFrame c = agent.getBehaviour().createCondition(param, op, val);
                            rf.addCondition(c);
                        }
                    }
                    it.remove();
                    DomainAttribute da = new DomainAttribute("equivalent-state", AttributeConstants.STRING);
                    da.setDefault(refName);
                    rf.addOrSetAttribute(UserRuleFrame.ATTR_LIST_ATTRS, da);
                    agent.getBehaviour().addRLRule(rf);
                }

                int k = 0;
                if (trials.containsKey(name)) {
                    k = trials.get(name);
                }
                k++;
                if (k > unresolved.size() + 1) {
                    throw new GSimDefException("Aborting RL-nodes generation by reference, because node " + name + " cannot be resolved.");
                }
                trials.put(name, k);

            }
        }

    }

    // include per default the empty runtime-role
    private void buildExecutionOrder(Document doc) {
        int maxOrder = 0;
        if (doc.getRootElement().getChild("execution-contexts") != null) {
            Element root = doc.getRootElement().getChild("execution-contexts");
            Iterator iter = root.getChildren("context").iterator();
            while (iter.hasNext()) {
                Element e = (Element) iter.next();
                String roleName = e.getAttributeValue("ref");
                String pause = e.getAttributeValue("exec-interval");
                if (pause != null) {
                    agentPauses.put(roleName, pause);
                } else {
                    agentPauses.put(roleName, "1");
                }
                int order = Integer.parseInt(e.getAttributeValue("order"));
                if (order > maxOrder && !roleName.equals("default")) {
                    maxOrder = order;
                }
                agentOrder.put(roleName, new Integer(order));
                String cls = e.getAttributeValue("class");
                if (cls == null) {
                    cls = "gsim.sim.agent.RtExecutionContext";
                }
                agentRtClassMappings.put(roleName, cls);
            }
        }
        agentRtClassMappings.put("default", "gsim.sim.agent.RtExecutionContext");
        // this.agentOrder.put("default", new Integer(maxOrder + 1));
    }

    private void checkExpansionRefs(GenericAgentClass agent) throws GSimDefException {

        for (RLRuleFrame n : agent.getBehaviour().getDeclaredRLRules()) {
            for (ExpansionFrame f : n.getExpansions()) {
                String attRef = f.getParameterName();

                DomainAttribute att = null;
                if (!attRef.contains("::")) {
                    att = (DomainAttribute) agent.resolveName(attRef.split("/"));
                } else {
                    String[] ref0 = attRef.split("::")[0].split("/");
                    String[] ref1 = attRef.split("::")[1].split("/");
                    Frame object = getObjectSubClass(ref0[1].trim());
                    if (object != null) {
                        att = (DomainAttribute) object.resolveName(ref1);
                    }
                }
                if (att == null) {
                    throw new GSimDefException("Attribute referenced in expansion " + attRef + " cannot be resolved.");
                }
                if (att.getType().equals(AttributeConstants.NUMERICAL)) {
                    if (f.getMax().equals(String.valueOf(Double.NaN)) || f.getMin().equals(String.valueOf(Double.NaN))) {
                        throw new GSimDefException("Attribute " + attRef + " is numerical, but no boundaries were specified.");
                    }
                } else if (att.getType().equals(AttributeConstants.SET)) {
                    String[] fillers = att.getFillers();
                    f.setFillers(fillers);
                    n.addExpansion(f);
                    agent.getBehaviour().addRLRule(n);
                } else if (att.getType().equals(AttributeConstants.INTERVAL)) {
                    f.setFillers(att.getFillers());
                    f.setMin(att.getFillers()[0]);
                    f.setMax(att.getFillers()[1]);
                    n.addExpansion(f);
                    agent.getBehaviour().addRLRule(n);
                } else {
                    throw new GSimDefException("Attribute " + attRef + " is of type " + att.getType() + ", but this can't be handled automatically.");
                }

            }
        }

    }

    private void createAgentFromXMLElement(Element sub, GenericAgentClass agent) throws NumberFormatException, GSimDefException {
        Element attributes = sub.getChild("attribute-lists");
        Element actions = sub.getChild("available-actions");
        Element rules = sub.getChild("rules");
        Element classifiers = sub.getChild("rl-nodes");

        if (attributes != null) {
            Iterator iter2 = attributes.getChildren().iterator();
            while (iter2.hasNext()) {
                Element attList = (Element) iter2.next();
                String listName = attList.getAttributeValue("name");

                agent.defineAttributeList(listName);

                Iterator iter3 = attList.getChildren().iterator();
                while (iter3.hasNext()) {
                    Element attElem = (Element) iter3.next();
                    DomainAttribute da = createAttFromXML(attElem, true);
                    DomainAttribute override = agent.getAttribute(listName, attElem.getAttributeValue("name"));
                    if (override != null && attElem.getAttribute("mutable") == null) {
                        da.setMutable(override.isMutable());
                    }
                    agent.addOrSetAttribute(listName, da);
                }
            }
        }

        if (actions != null) {
            Iterator actionsIter = actions.getChildren("action").iterator();

            while (actionsIter.hasNext()) {
                Element action = (Element) actionsIter.next();

                String name = action.getAttributeValue("name").trim();
                ActionFrame a = actionCollections.get(name);
                if (action.getAttribute("parameters") != null) {
                    String[] params = action.getAttributeValue("parameters").split(",");
                    for (String p : params) {
                        a.addObjectClassParam(p, null);
                    }
                }
                agent.getBehaviour().addAction(a);
            }
        }

        if (rules != null) {
            addReactiveRules(agent, rules);
        }
        if (classifiers != null) {
            addClassifiers(agent, classifiers);
        }

    }

    private void createAgentSkeletons() throws GSimDefException {
        maybeSetTopAgentClass(topLevelAgents);
        HashMap<String, GenericAgentClass> top = createTopAgentSkeletons(topLevelAgents);
        HashMap<String, Element> ind = new HashMap<String, Element>();
        Iterator iter = inheritingAgents.values().iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            if (e.getAttribute("extends") != null) {
                ind.put(e.getAttributeValue("name"), e);
            }
        }
        createSubLevelAgentSkeletons(top, ind);
    }

    private DomainAttribute createAttFromXML(org.jdom.Element e, boolean isSystem) throws GSimDefException {

        String name = e.getAttribute("name").getValue();
        String type = e.getName();

        if (e.getAttribute("default") == null) {
            throw new GSimDefException("The attribute 'default' for attribute " + name + " does not exist.");
        }
        String def = e.getAttribute("default").getValue();
        DomainAttribute da = new DomainAttribute(name, type);

        da.setDefault(def);
        da.setMutable(true);
        da.setSystem(isSystem);

        if (type.equals(AttributeConstants.ORDERED_SET) || type.equals(AttributeConstants.SET)) {
            Iterator iter = e.getChildren("value").iterator();
            while (iter.hasNext()) {
                Element elem = (Element) iter.next();
                da.addFiller(elem.getText());
            }
        } else if (type.equals(AttributeConstants.INTERVAL)) {
            if (e.getAttribute("min") == null || e.getAttribute("max") == null) {
                throw new GSimDefException("Either min or max attributes for interval-attribute " + name + " are not set.");
            }
            String min = e.getAttribute("min").getValue();
            String max = e.getAttribute("max").getValue();
            da.setDefault(def);
            // da.setDefault(min + " - " + max);
            da.addFiller(min);
            da.addFiller(max);
        }

        if (e.getAttribute("weight") != null) {
            try {
                da.setWeight(e.getAttribute("weight").getDoubleValue());
            } catch (DataConversionException ex) {
                System.out.println("Couldn't set attribute weight because it didn't seem to be a number");
            }
        }
        if (e.getAttribute("mutable") != null) {
            da.setMutable(Boolean.parseBoolean(e.getAttributeValue("mutable")));
        }

        return da;
    }

    private void createObjectFromXML(Element e, Frame f) throws GSimDefException {
        // Element e = ex.getChild("attribute-lists");
        if (e.getChildren("list") != null) {
            Iterator it = e.getChildren("list").iterator();
            while (it.hasNext()) {
                Element list = (Element) it.next();
                f.defineAttributeList(list.getAttributeValue("name"));
                if (list.hasChildren()) {
                    Iterator it2 = list.getChildren().iterator();
                    String listName = list.getAttributeValue("name");
                    while (it2.hasNext()) {
                        Element att = (Element) it2.next();
                        DomainAttribute a = createAttFromXML(att, true);
                        DomainAttribute override = f.getAttribute(listName, att.getAttributeValue("name"));
                        f.addOrSetAttribute(listName, a);
                        if (override != null && att.getAttribute("mutable") == null) {
                            a.setMutable(override.isMutable());
                        }
                        f.addOrSetAttribute(listName, a);
                    }
                }
            }
        }
    }

    private void createObjects() throws GSimDefException {
        HashMap<String, Frame> top = createTopLevelObjects(topLevelObjects);
        GenericAgentClass[] c = getAgentSubClasses();
        for (int i = 0; i < c.length; i++) {
            top.put(c[i].getTypeName(), c[i]);
        }
        HashMap<String, Element> ind = new HashMap<String, Element>();
        Iterator iter = inheritingObjects.values().iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            if (e.getAttribute("extends-object") != null) {
                ind.put(e.getAttributeValue("name"), e);
            }
        }
        HashMap resolved = createSubLevelObjects(top, ind);
        iter = resolved.values().iterator();
        while (iter.hasNext()) {
            Frame cls = (Frame) iter.next();
            if (!(cls instanceof GenericAgentClass)) {
                addObjectSubClass(cls);
            }
        }

    }

    private HashMap<String, GenericAgentClass> createSubLevelAgentSkeletons(HashMap<String, GenericAgentClass> topLevel,
            HashMap<String, Element> subLevelsXML) throws GSimDefException {

        HashMap<String, GenericAgentClass> ind = new HashMap<String, GenericAgentClass>(topLevel);

        boolean resolvable = true;
        int lastSize = subLevelsXML.size();

        while (subLevelsXML.size() > 0 && resolvable) {
            Iterator iter = subLevelsXML.values().iterator();

            while (iter.hasNext()) {
                Element e = (Element) iter.next();
                String name = e.getAttributeValue("name");
                String parents = e.getAttributeValue("extends").trim();
                String[] parentArray = parents.split(",");
                boolean goes = true;
                for (int i = 0; i < parentArray.length; i++) {
                    if (!ind.containsKey(parentArray[i].trim())) {
                        goes = false;
                    }
                }
                if (goes) {
                    GenericAgentClass[] ps = new GenericAgentClass[parentArray.length];
                    iter.remove();
                    for (int i = 0; i < parentArray.length && goes; i++) {
                        GenericAgentClass parentClass = ind.get(parentArray[i].trim());
                        if (getAgentSubClass(parentArray[i].trim()) == null) {
                            addAgentSubClass(parentClass);
                        }
                        ps[i] = parentClass;
                    }
                    GenericAgentClass sub = new GenericAgentClass(new GenericAgentClass[] { ps[0] }, name);

                    createAgentFromXMLElement(e, sub);
                    addAgentSubClass(sub);
                    for (int i = 1; i < ps.length; i++) {
                        sub = extendAgentClassRole(sub, ps[i]);
                    }
                    ind.put(sub.getTypeName(), sub);
                    addAgentSubClass(sub);

                }
            }

            if (lastSize == subLevelsXML.size()) {
                logger.warn("Not all agents were created because there seem to be circular inheritance relations");
                resolvable = false;
            } else {
                lastSize--;
            }

        }

        return ind;
    }

    private HashMap<String, Frame> createSubLevelObjects(HashMap<String, Frame> topLevel, HashMap<String, Element> subLevelsXML)
            throws GSimDefException {
        HashMap<String, Frame> ind = new HashMap<String, Frame>(topLevel);

        HashMap<String, Frame> resolved = new HashMap<String, Frame>();
        boolean resolvable = true;

        int lastSize = subLevelsXML.size();
        while (subLevelsXML.size() > 0 && resolvable) {
            Iterator iter = subLevelsXML.values().iterator();
            while (iter.hasNext()) {
                Element e = (Element) iter.next();
                String name = e.getAttributeValue("name");
                String parents = e.getAttributeValue("extends-object").trim();
                String[] parentArray = parents.split(",");
                boolean goes = true;
                for (int i = 0; i < parentArray.length; i++) {
                    if (!ind.containsKey(parentArray[i])) {
                        goes = false;
                    }
                }
                if (goes) {
                    iter.remove();
                    Frame[] fs = new Frame[parentArray.length];
                    boolean hasObjectParent = false;
                    for (int i = 0; i < parentArray.length; i++) {
                        Frame parentClass = ind.get(parentArray[i]);
                        if (!(parentClass instanceof GenericAgentClass)) {
                            hasObjectParent = true;
                            resolved.put(parentClass.getTypeName(), parentClass);
                        }
                        fs[i] = parentClass;
                    }
                    if (!hasObjectParent) {
                        Frame[] nn = new Frame[fs.length + 1];
                        Utils.addToArray(fs, nn, getObjectClass());
                        fs = nn;
                    }
                    Frame sub = new Frame(fs, name, "object");
                    createObjectFromXML(e, sub);
                    ind.put(sub.getTypeName(), sub);
                    resolved.put(sub.getTypeName(), sub);
                }
            }

            if (lastSize == subLevelsXML.size()) {
                logger.warn("Not all objects were created because there seem to be circular inheritance relations");
                resolvable = false;
            } else {
                lastSize--;
            }

        }
        resolved.putAll(ind);
        return resolved;

    }

    // create top-level agents without nested objects, in order to be able to
    // resolve cross-references between other agents and objects.
    private HashMap<String, GenericAgentClass> createTopAgentSkeletons(HashMap<String, Element> o) throws GSimDefException {
        HashMap<String, GenericAgentClass> ind = new HashMap<String, GenericAgentClass>();
        Iterator iter = o.values().iterator();
        while (iter.hasNext()) {
            Element sub = (Element) iter.next();
            GenericAgentClass top = getAgentClassRef();

            if (!sub.getAttributeValue("name").equals(top.getTypeName())) {
                GenericAgentClass agent = new GenericAgentClass(new GenericAgentClass[] { top }, sub.getAttributeValue("name"));

                if (getAgentSubClass(agent.getTypeName()) == null) {
                    addAgentSubClass(agent);
                }

                createAgentFromXMLElement(sub, agent);
                ind.put(agent.getTypeName(), agent);
            }
        }
        return ind;
    }

    // create objects that do not inherit from anything.
    private HashMap<String, Frame> createTopLevelObjects(HashMap<String, Element> o) throws GSimDefException {
        maybeSetObjectClass(o);
        HashMap<String, Frame> ind = new HashMap<String, Frame>();
        Iterator iter = o.values().iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            Frame top = getObjectClassRef();
            if (!e.getAttributeValue("name").equals(top.getTypeName())) {
                Frame f = new Frame(new Frame[] { top }, e.getAttributeValue("name"), "object");
                createObjectFromXML(e, f);
                ind.put(f.getTypeName(), f);
            }
        }
        return ind;
    }

    private String findContextRek(String agentName) {

        if (agentName == null) {
            return null;
        }

        String role = null;
        String[] roles = agentMappings.get(agentName);

        if (roles == null) {
            roles = searchDoc(agentName);
            if (roles == null) {
                return findContextRek(findParent(agentName));
            } else {
                return role;
            }
        } else {
            return role;
        }

    }

    private String findParent(String agent) {
        Element root = doc.getRootElement();
        Element objElem = root.getChild("agents");

        if (objElem.hasChildren()) {
            Iterator iter = objElem.getChildren().iterator();
            while (iter.hasNext()) {
                Element elem = (Element) iter.next();
                String name = elem.getAttribute("name").getValue();
                if (name.equals(agent) && elem.getAttribute("extends") != null) {
                    return elem.getAttribute("extends").getValue();
                }

            }
        }
        return null;
    }

    /**
     * Return empty array if the model contains no action.xml file.
     * 
     * @param file
     *            String
     * @return Frame[]
     */
    @SuppressWarnings("unchecked")
    private Frame[] loadPossiblyNewActions() {

        HashSet<Frame> frames = new HashSet<Frame>();
        Element root = null;

        try {

            root = doc.getRootElement();

            Element e1 = root.getChild("actions");

            if (e1 == null) {
                return new Frame[0];
            }

            List actions = e1.getChildren();
            Iterator iter = actions.iterator();
            while (iter.hasNext()) {
                Element action = (Element) iter.next();
                String name = action.getAttributeValue("name");
                String cls = action.getAttributeValue("class");

                ActionFrame f = new ActionFrame(name, cls);
                frames.add(f);

                actionCollections.put(f.getTypeName(), f);
                getActionMappings().put(name, cls);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Frame[] res = new Frame[frames.size()];
        frames.toArray(res);
        return res;

    }

    private void maybeSetObjectClass(HashMap<String, Element> o) throws GSimDefException {
        Iterator iter = o.values().iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            Frame top = getObjectClassRef();
            if (e.getAttributeValue("name").equals(top.getTypeName())) {
                createObjectFromXML(e, top);
                setObjectClass(top);
            }
        }
    }

    private void maybeSetTopAgentClass(HashMap<String, Element> o) throws GSimDefException {
        Iterator iter = o.values().iterator();
        GenericAgentClass top = getAgentClassRef();
        while (iter.hasNext()) {
            Element sub = (Element) iter.next();
            if (sub.getAttributeValue("name").equals(top.getTypeName())) {
                createAgentFromXMLElement(sub, top);
                setAgentClass(top);
            }
        }
    }

    private void putAgentsToLists(Document doc) {
        Element root = doc.getRootElement();
        Element objElem = root.getChild("agents");

        if (objElem.hasChildren()) {
            Iterator iter = objElem.getChildren().iterator();
            while (iter.hasNext()) {
                Element elem = (Element) iter.next();
                String name = elem.getAttribute("name").getValue();
                if (elem.getAttribute("extends") == null) {
                    topLevelAgents.put(name, elem);
                } else {
                    inheritingAgents.put(elem.getAttribute("name").getValue(), elem);
                }
                if (elem.getAttribute("execution-context") != null) {
                    String rolename = elem.getAttributeValue("execution-context");
                    if (rolename != null) {
                        String[] r = agentMappings.get(name);
                        String[] roles;
                        if (r != null) {
                            roles = new String[r.length + 1];
                            System.arraycopy(r, 0, roles, 0, r.length + 1);
                            roles[roles.length - 1] = rolename;
                        } else {
                            roles = r;
                        }
                        agentMappings.put(name, roles);
                    } else {
                        agentMappings.put(name, new String[] { "default" });
                    }
                } else {
                    String role = findContextRek(name);
                    if (role == null) {
                        agentMappings.put(name, new String[] { "default" });
                    } else {
                        String[] r = agentMappings.get(name);
                        String[] roles;
                        if (r != null) {
                            roles = new String[r.length + 1];
                            System.arraycopy(r, 0, roles, 0, r.length + 1);
                            roles[roles.length - 1] = role;
                        } else {
                            roles = r;
                        }
                        agentMappings.put(name, roles);
                    }
                }
            }
        }
    }

    private void putObjectsToLists(Document doc) {
        Element root = doc.getRootElement();

        Element objElem = root.getChild("objects");

        if (objElem != null && objElem.hasChildren()) {
            Iterator iter = objElem.getChildren().iterator();
            while (iter.hasNext()) {
                Element elem = (Element) iter.next();
                if (elem.getAttribute("extends-object") == null) {
                    topLevelObjects.put(elem.getAttribute("name").getValue(), elem);
                } else {
                    inheritingObjects.put(elem.getAttribute("name").getValue(), elem);
                }
            }
        }
    }

    private void readDataHandlers(Document doc) {
        HashMap<String, String> list = new HashMap<String, String>();
        if (doc.getRootElement().getChild("data-handlers") != null) {
            Element root = doc.getRootElement().getChild("data-handlers");
            Iterator iter = root.getChildren("handler").iterator();
            while (iter.hasNext()) {
                Element e = (Element) iter.next();
                String forType = e.getAttributeValue("type");
                String clsName = e.getAttributeValue("class");
                list.put(forType, clsName);
            }
        }
        dataHandlers = list;
    }

    private void readSystemAgents() {
        if (doc == null) {
            return;
        }

        if (doc.getRootElement().getChild("system-agents") != null) {
            Iterator iter = doc.getRootElement().getChild("system-agents").getChildren().iterator();
            while (iter.hasNext()) {
                Element e = (Element) iter.next();
                String clsName = e.getAttributeValue("class");
                String name = e.getAttributeValue("name");
                systemAgents.put(name, clsName);
            }
        }
    }

    private String replace(String test) {
        String s = test.replace("LT", "<");
        s = s.replace("LE", "<=");
        s = s.replace("GT", ">");
        s = s.replace("GE", ">=");
        s = s.replace("EQ", "=");
        return s;
    }

    private String[] searchDoc(String agent) {
        Element root = doc.getRootElement();
        Element objElem = root.getChild("agents");

        if (objElem.hasChildren()) {
            Iterator iter = objElem.getChildren().iterator();
            while (iter.hasNext()) {
                Element elem = (Element) iter.next();
                String name = elem.getAttribute("name").getValue();
                if (name.equals(agent) && elem.getAttribute("execution-context") != null) {
                    String s = elem.getAttribute("execution-context").getValue();
                    if (s != null) {
                        return s.split(",");
                    } else {
                        return null;
                    }
                }

            }
        }
        return null;
    }

    private String translate(String xmlExpr) {
        if (xmlExpr.equals("GT")) {
            return (">");
        } else if (xmlExpr.equals("GE")) {
            return (">=");
        } else if (xmlExpr.equals("LT")) {
            return ("<");

        } else if (xmlExpr.equals("LE")) {
            return ("<=");
        } else if (xmlExpr.equals("EQ")) {
            return ("=");
        } else if (xmlExpr.startsWith("NE")) {
            return ("<>");
        }

        return xmlExpr;
    }

}
