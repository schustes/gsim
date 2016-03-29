package de.s2.gsim.sim.behaviour.impl;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.s2.gsim.api.sim.agent.impl.RuntimeAgent;
import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;
import de.s2.gsim.def.objects.agent.BehaviourFrame;
import de.s2.gsim.def.objects.agent.GenericAgentClass;
import de.s2.gsim.def.objects.behaviour.ActionDef;
import de.s2.gsim.def.objects.behaviour.ConditionDef;
import de.s2.gsim.def.objects.behaviour.ExpansionDef;
import de.s2.gsim.def.objects.behaviour.RLRule;
import de.s2.gsim.def.objects.behaviour.UserRule;
import de.s2.gsim.def.objects.behaviour.UserRuleFrame;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeConstants;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.objects.attribute.IntervalAttribute;
import de.s2.gsim.sim.GSimEngineException;
import de.s2.gsim.sim.behaviour.GSimBehaviourException;
import de.s2.gsim.sim.behaviour.impl.jessfunction.Expand;
import de.s2.gsim.sim.behaviour.impl.jessfunction.RandomStrategy;
import de.s2.gsim.sim.behaviour.impl.jessfunction.SimpleSoftmaxSelector;
import de.s2.gsim.sim.behaviour.impl.jessfunction.ToInstPath;
import de.s2.gsim.sim.behaviour.util.CollectiveTreeDBWriter;
import de.s2.gsim.sim.behaviour.util.IndividualTreeDBWriter;
import jess.Deftemplate;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;

public class JessHandler implements java.io.Serializable {

    private static Logger logger = Logger.getLogger(JessHandler.class);

    private static final long serialVersionUID = 1L;

    public Rete rete = new Rete();

    private HashSet<String> currentConditionSet = new HashSet<String>();

    private boolean DEBUG = true;

    private String debugDir = "/home/gsim/tmp";

    private boolean dirty = false;

    private int episodeStart = -1;

    private boolean instanceChanged = true;

    // private BuilderUtils builder = new BuilderUtils();

    private HashSet<String> objectParams = new HashSet<String>();

    private transient RuntimeAgent owner = null;

    private RLParameterRanges rlRanges;

    private boolean rulesChanged = true;

    private double time = 0; // used for learning rules

    private String treeDir = null;

    private int writeTreeToDB = -1;

    public JessHandler() {
        clean();
    }

    public JessHandler(RuntimeAgent ownerAgent, Map props) throws GSimEngineException {
        String s = System.getProperty("debugJess");
        String treeDB = System.getProperty("writeTreeDBInterval");
        String treeFile = System.getProperty("writeTreeFile");
        if (treeDB != null) {
            writeTreeToDB = Integer.parseInt(treeDB);
        }
        if (treeFile != null) {
            treeDir = treeFile;
        }
        if (s != null) {
            DEBUG = true;
            String s2 = System.getProperty("debugJessDir");
            if (s2 != null) {
                debugDir = s2;
            }
        }
        clean();
        createEngine(ownerAgent, props);
        ownerAgent.getBehaviour().setFrame(null);
        rlRanges = new RLParameterRanges();
        initRLRanges();
    }

    public void checkRLParams() {
        for (RLRule r : owner.getBehaviour().getRLRules()) {
            for (ExpansionDef e : r.getExpansions()) {
                String path = e.getParameterName();
                DomainAttribute a = extractAtt(path);// (DomainAttribute) this.owner.getDefinition().resolveName(path.split("/"));
                if (a.getType().equals(AttributeConstants.SET)) {
                    ArrayList<String> modifiedSet = rlRanges.getNewCategoricalParameterValues(path, a.getFillers());
                    RLRulesUpdate rlUpdate = new RLRulesUpdate(owner, debugDir);
                    for (String n : modifiedSet) {
                        logger.debug("Modified set, att=" + path + ", new=" + n);
                        rlUpdate.update(a, n, rete.getGlobalContext());
                    }
                } else if (a.getType().equals(AttributeConstants.INTERVAL)) {
                    double[] modifiedRange = rlRanges.getNewIntervalParameterRange(path,
                            new double[] { Double.parseDouble(a.getFillers()[0]), Double.parseDouble(a.getFillers()[1]) });
                    if (modifiedRange != null) {
                        logger.debug("modified interval att=" + path + ", new range=" + modifiedRange[0] + "-" + modifiedRange[1]);
                    }
                }
            }
        }
    }

    public void destroy() {
        try {
            if (rete != null) {
                rete.reset();
                rete.clear();
                rete.clearFocusStack();
                rete.store("AGENT", null);
                rete.store("query-result", null);
                rete.clearStorage();
                rete = null;
                owner = null;
            }
        } catch (JessException e) {
            logger.debug("Some problem with JessHandler.destroy()" + e.getMessage());
        }
    }

    public void endEpisode() throws GSimBehaviourException {

        if (episodeStart == -1) {
            throw new GSimBehaviourException("No episode has been started");
        }

        try {
            Deftemplate p = rete.findDeftemplate("episode");
            Fact f = new Fact(p);
            f.setSlotValue("start-time", new Value(episodeStart, RU.FLOAT));
            rete.assertFact(f);
        } catch (JessException e) {
            e.printStackTrace();
        } finally {
            episodeStart = -1;
        }

    }

    /**
     * Executes rules for the current role and time. Note: The rulebase is not cleared, so only rules will become activated that were defined for this
     * role.
     * 
     * @param role
     *            String
     * @param modelTime
     *            int
     */
    // static BuilderUtils u = new BuilderUtils();
    public void executeUserRules(String role, HashMap globals) {
        try {

            long total = System.currentTimeMillis();

            Constant roleParameter = new Constant("executing-role", role);

            reset();

            checkRLParams();

            long l = System.currentTimeMillis();
            insertGlobalVariableValues(globals);
            rete.run();

            logger.debug("rules on globals: " + ((System.currentTimeMillis() - l) / 1000d));

            l = System.currentTimeMillis();

            if (rulesChanged) {
                // in this case the rl-facts are checked anyway, because
                // conditions may have been added
                currentConditionSet = JessHandlerUtils.buildCurrentState(rete, owner);
                if (objectParams.isEmpty()) {
                    for (String s : currentConditionSet) {
                        if (s.contains("::")) {
                            objectParams.add(ParsingUtils.resolveObjectClass(s));
                        }
                    }
                }
                rulesChanged = false;
            } else {
                // if no new rules, rl-facts can only change if new instances
                // referenced as action-arguments have been added.
                JessHandlerUtils.buildCurrentState(rete, owner, currentConditionSet);
                // if (this.instanceChanged) {

                addNewActions();
                retractObsoleteActions();
                instanceChanged = false;
                // }
            }

            logger.debug("build state: " + ((System.currentTimeMillis() - l) / 1000d));

            if (hasReactiveRules(role)) {
                if (DEBUG) {
                    printFacts("****** FACTS BEFORE REACTIVE ******");
                }
                l = System.currentTimeMillis();
                assertParameter(new Constant("exec-r", "dc"));
                assertParameter(roleParameter);
                rete.run();
                logger.debug("rules on reactive: " + ((System.currentTimeMillis() - l) / 1000d));
                if (DEBUG) {
                    printFacts("****** FACTS AFTER REACTIVE ******");
                }
            }

            // int expandInterval = owner.getBehaviour().getStateUpdateInterval();
            // int deleteInterval = (expandInterval - (int)(((double)expandInterval/4d)));
            // if (hasShortcuts(role) || expandInterval%this.time==0 || deleteInterval%this.time==0) {
            if (DEBUG) {
                printFacts("****** FACTS BEFORE SHORT ******");
            }
            l = System.currentTimeMillis();
            assertParameter(new Constant("exec-sc", "dc"));
            assertParameter(roleParameter);
            rete.run();
            logger.debug("rules on short: " + ((System.currentTimeMillis() - l) / 1000d));
            if (DEBUG) {
                printFacts("****** FACTS AFTER SHORT ******");
            }
            // }
            if (hasRLRules(role)) {
                if (DEBUG) {
                    printFacts("****** FACTS BEFORE RLRule ******");
                }
                l = System.currentTimeMillis();
                assertParameter(new Constant("exec-RLRule", "dc"));
                assertParameter(roleParameter);
                rete.run();
                logger.debug("rules on RLRule: " + ((System.currentTimeMillis() - l) / 1000d));
                if (DEBUG) {
                    printFacts("****** FACTS AFTER RLRule ******");
                }
            }

            printTree(role);

            logger.debug("EXECUTE RULES FOR AGENT " + owner.getName() + ", role:" + role + ": " + ((System.currentTimeMillis() - total) / 1000d));

            dirty = true;

        } catch (JessException e) {
            e.printStackTrace();
        }

    }

    public void initRLRanges() {
        for (RLRule r : owner.getBehaviour().getRLRules()) {
            for (ExpansionDef e : r.getExpansions()) {
                String path = e.getParameterName();
                // DomainAttribute a = (DomainAttribute) this.owner.getDefinition().resolveName(path.split("/"));
                DomainAttribute a = extractAtt(path);
                if (a.getType().equals(AttributeConstants.SET)) {
                    rlRanges.initCategoricalParameters(path, e.getFillers());// e!
                                                                             // because
                                                                             // default
                                                                             // values...
                } else if (a.getType().equals(AttributeConstants.INTERVAL)) {
                    rlRanges.initIntervalParameterRange(path,
                            new double[] { Double.parseDouble(e.getFillers()[0]), Double.parseDouble(a.getFillers()[1]) });
                }
            }
        }
    }

    public void initRLRanges_old() {
        for (RLRule r : owner.getBehaviour().getRLRules()) {
            for (ExpansionDef e : r.getExpansions()) {
                String path = e.getParameterName();
                DomainAttribute a = (DomainAttribute) owner.getDefinition().resolveName(path.split("/"));
                if (a.getType().equals(AttributeConstants.SET)) {
                    rlRanges.initCategoricalParameters(path, a.getFillers());
                } else if (a.getType().equals(AttributeConstants.INTERVAL)) {
                    rlRanges.initIntervalParameterRange(path,
                            new double[] { Double.parseDouble(a.getFillers()[0]), Double.parseDouble(a.getFillers()[1]) });
                }
            }
        }
    }

    public void instanceChanged(String name) {
        if (objectParams.isEmpty()) {
            instanceChanged = true;
            return;
        }

        for (String s : objectParams) {
            if (name.equals(s)) {
                instanceChanged = true;
            }
        }
    }

    public void printTree(String role) {

        if (treeDir != null && time % writeTreeToDB == 0) {
            CollectiveTreeDBWriter f = new CollectiveTreeDBWriter();
            f.output(owner.getName(), rete, treeDir);
        }

        if (writeTreeToDB > -1 && time % writeTreeToDB == 0) {
            IndividualTreeDBWriter f = new IndividualTreeDBWriter();
            f.writeToDB(owner.getNameSpace(), owner.getName(), (int) time, rete, role);
        }
    }

    /**
     * retract all facts except 'static' ones (those used for RLRule). Also retracts the role-facts - with the change to sinlge inheritance this
     * shouldn't cause problems, because now agetns with several roles execute overriden rules in the execution context where the original was
     * defined. For example, if Frame a with context x overrides rule y in Frame b, but frame b as a different exeuction context than a, then the rule
     * will not be executed when Frame a's role turn is, but (still) when Frame b's turn is.
     */
    public void reset() {

        int y = 0;
        try {

            Iterator iter = rete.listFacts();

            while (iter.hasNext()) {

                Fact f = (Fact) iter.next();
                if (!f.getDeftemplate().getBaseName().equals("rl-action-node") && !f.getDeftemplate().getBaseName().equals("average-reward")
                        && !f.getDeftemplate().getBaseName().startsWith("state-fact") && !f.getDeftemplate().getBaseName().equals("list")) {
                    rete.retract(f);
                    y++;
                }
            }

        } catch (Exception e) {

            // logger.debug(y + ":::: >>>>>>>>>>>>>>>>>>>>>>>" + current);

            e.printStackTrace();
            System.exit(0);
            /*
             * Iterator iter = this.rete.listActivations(); while (iter.hasNext()) { Activation a = (Activation) iter.next(); //
             * logger.debug(a.getRule().getName()); }
             */
            // logger.debug(y + ":::: >>>>>>>>>>>>>>>>>>>>>>>" + current);
        }

    }

    public void retractConstant(String paramName) {
        try {
            Iterator iter = rete.listFacts();
            while (iter.hasNext()) {
                Fact f = (Fact) iter.next();
                if (f.getDeftemplate().getBaseName().equals("parameter")) {
                    String s = f.getSlotValue("name").stringValue(rete.getGlobalContext());
                    if (s.equals(paramName)) {
                        rete.retract(f);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void retractExecutedFact(String qualifiedName) {
        try {
            Iterator iter = rete.listFacts();
            while (iter.hasNext()) {
                Fact f = (Fact) iter.next();
                if (f.getDeftemplate().getBaseName().equals("RLRule-action-node")) {
                    ValueVector s = f.getSlotValue("arg").listValue(rete.getGlobalContext());
                    for (int i = 0; i < s.size(); i++) {
                        String str = (s.get(i)).stringValue(rete.getGlobalContext());
                        if (str.equals(qualifiedName)) {
                            rete.retract(f);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOwner(RuntimeAgent ownerAgent) {

        if (rete != null) {
            rete.store("AGENT", ownerAgent);
            try {
                rete.executeCommand("(bind ?*agent* = (fetch AGENT))");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        owner = ownerAgent;
    }

    public void startEpisode() throws GSimBehaviourException {
        if (episodeStart > -1) {
            throw new GSimBehaviourException("Only one episode can be running at a time");
        }
        episodeStart = (int) time;
    }

    /**
     * Updates only variables that are referenced in the evaluators of RLRule-nodes.
     */
    public void updateRewards() {

        if (!dirty) {
            return;
        }

        long l = System.currentTimeMillis();

        reset2();

        double timeLocal = time + 1;

        try {
            Deftemplate p = rete.findDeftemplate("timer");
            Fact f = new Fact(p);
            f.setSlotValue("time", new Value(timeLocal, RU.FLOAT));
            rete.assertFact(f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (RLRule c : owner.getBehaviour().getRLRules()) {
            ConditionDef cond = c.getEvaluationFunction();
            if (cond != null) {
                String attName = cond.getParameterName();
                Attribute ref = (Attribute) owner.resolveName(attName.split("/"));
                if (ref != null) {
                    retractConstant(cond.getParameterName());

                    if (ref instanceof IntervalAttribute) {
                        double val = (((IntervalAttribute) ref).getFrom() + ((IntervalAttribute) ref).getTo() / 2d);
                        JessHandlerUtils.assertParameter(rete, attName, String.valueOf(val));
                    } else {
                        JessHandlerUtils.assertParameter(rete, attName, ref.toValueString());
                    }

                    // JessHandlerUtils.assertParameter(rete, attName,
                    // ref.toValueString());
                } else {
                    logger.debug("Condition in RLRule node " + c.getName() + " refers to variable " + attName
                            + ", but that variable does at the moment not exist in agent " + owner.getName() + " of type "
                            + owner.getDefinition().getTypeName() + ".");
                }
            }
        }

        try {
            if (DEBUG) {
                printFacts("*****FACTS BEFORE UPDATE REWARDS (and rete.run()) *******\n");
            }
            assertParameter(new Constant("exec-update-rewards", "dc"));
            rete.run();
            if (DEBUG) {
                printFacts("*****FACTS AFTER UPDATE REWARDS (and rete.run()) *******\n");
            }
        } catch (JessException e) {
            e.printStackTrace();
        }

        logger.debug("update rewards: " + ((System.currentTimeMillis() - l) / 1000d));
        dirty = false;
    }

    private void addNewActions() throws JessException {
        for (InstanceOLD r : owner.getBehaviour().getChildInstances(BehaviourFrame.RL_LIST)) {
            String sfn = r.getName() + "_0" + 0;
            FactHandler.getInstance().insertNonExistentExecutedFinalFacts(rete, owner, new RLRule(r), sfn);
        }

    }

    private void assertParameter(Constant param) {
        Fact f = null;
        try {
            Deftemplate p = rete.findDeftemplate("parameter");
            String val = param.getValue();

            if (val == null) {
                val = "0";
            }
            f = new Fact(p);
            f.setSlotValue("name", new Value(param.getName(), RU.STRING));

            if (de.s2.gsim.util.Utils.isNumerical(val)) {
                f.setSlotValue("value", new Value(Double.valueOf(val).doubleValue(), RU.FLOAT));
            } else {
                f.setSlotValue("value", new Value(val, RU.STRING));
            }
            rete.assertFact(f);
        } catch (JessException e) {
            f = null;
            e.printStackTrace();
        }

    }

    private void clean() {
        try {
            File dir = new File(debugDir);
            File[] contents = dir.listFiles();
            long now = System.currentTimeMillis();
            for (File f : contents) {
                File[] files = f.listFiles();
                for (File file : files) {
                    if (now - file.lastModified() > 1000 * 60 * 60 * 12) {
                        file.delete();
                    }
                }
            }

        } catch (Exception e) {

        }
    }

    @SuppressWarnings("unchecked")
    private void createEngine(RuntimeAgent a, Map props) throws GSimEngineException {

        owner = a;

        double maxReward = 1;
        if (props.containsKey("MAX_REWARD")) {
            maxReward = Double.parseDouble((String) props.get("MAX_REWARD"));
        } else {
            // logger.debug("******* NO MAX REWARD ********");
        }

        props.put("MAX_DEPTH", a.getBehaviour().getMaxDepth());
        props.put("MAX_NODE_COUNT", a.getBehaviour().getMaxNodes());

        rete = new Rete();
        rete.store("AGENT", owner);

        String script = "";
        GlobalsBuilder g = new GlobalsBuilder();
        String s0 = g.build(rete, props);
        if (s0.length() > 0) {
            script += s0 + "\n";
        }

        // RLRulesBuilder b = new RLRulesBuilder(this.owner);
        RLParser b = new RLParser(owner);
        s0 = b.build(rete);
        if (s0.length() > 0) {
            script += s0 + "\n";
        }
        ReactiveRuleBuilder c = new ReactiveRuleBuilder(owner);
        s0 = c.build(rete);
        if (s0.length() > 0) {
            script += s0;
        }

        if (DEBUG) {
            printScript(script);
        }

        // rete.addUserfunction(new ComparisonSelector());
        try {
            rete.setStrategy(new RandomStrategy());
        } catch (JessException e) {
            e.printStackTrace();
        }
        rete.addUserfunction(new SimpleSoftmaxSelector(maxReward));
        rete.addUserfunction(new ToInstPath());
        rete.addUserfunction(new Expand());
        // rete.addUserfunction(new Contract());

    }

    private DomainAttribute extractAtt(String path) {
        DomainAttribute a = null;
        if (path.contains("::")) {
            ConditionBuilder cb = new ConditionBuilder();
            String obj = cb.resolveObjectClass(path);
            FrameOLD f = (FrameOLD) owner.getDefinition().resolveName(obj.split("/"));
            if (f == null) {
                String list = cb.resolveList(path);
                f = owner.getDefinition().getListType(list);

            }
            String att = cb.resolveAttribute(path);
            a = (DomainAttribute) f.resolveName(att.split("/"));
        } else {
            a = (DomainAttribute) owner.getDefinition().resolveName(path.split("/"));
        }
        return a;
    }

    private boolean hasExpansions(RuntimeAgent a) {
        for (RLRule r : a.getBehaviour().getRLRules()) {
            if (r.hasExpansions()) {
                return true;
            }
        }

        return false;
    }

    private boolean hasReactiveRules(String role) {
        for (UserRule rule : owner.getBehaviour().getRules()) {
            String definingRole = ParsingUtils.getDefiningRoleForRule(owner, rule.getName());
            if (definingRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRLRules(String role) {
        for (RLRule rule : owner.getBehaviour().getRLRules()) {
            String definingRole = ParsingUtils.getDefiningRoleForRLRule(owner, rule.getName());
            if (definingRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasShortcuts(String role) {
        for (RLRule rule : owner.getBehaviour().getRLRules()) {
            String definingRole = ParsingUtils.getDefiningRoleForRLRule(owner, rule.getName());
            if (definingRole.equals(role)) {
                if (rule.getShortSelectionRules().length > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void insertGlobalVariableValues(HashMap<?, ?> globals) {

        time = (Long) globals.get("TIME");

        try {
            Deftemplate p = rete.findDeftemplate("timer");
            Fact f = new Fact(p);
            f.setSlotValue("time", new Value(time, RU.FLOAT));
            rete.assertFact(f);

            JessHandlerUtils.assertParameter(rete, "current-agent-count", (String) globals.get("AGENT_COUNT"));

            JessHandlerUtils.assertParameter(rete, "exec-interval", (String) globals.get("INTERVAL"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isRoleExecuting(String role, HashMap globals) {

        GenericAgentClass def = (GenericAgentClass) owner.getDefinition();

        if (def.getTypeName().equals(role)) {

        } else {
            FrameOLD[] ancestors = def.getAncestors();
            for (FrameOLD f : ancestors) {
                GenericAgentClass agentClass = (GenericAgentClass) f;
                if (agentClass.getTypeName().equals(role)) {

                }
            }
        }

        return true;
    }

    private void printFacts(String head) {
        String v = head + "\n";
        Iterator<?> it = rete.listFacts();
        while (it.hasNext()) {
            v += ((Fact) it.next()).toString() + "\n";
        }

        try {
            File dir = new File(debugDir + "/facts");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileWriter f = new FileWriter(dir.getCanonicalPath() + "/FACTS-" + owner.getName() + ".txt", true);
            f.write(v);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void printScript(String s) {
        try {
            File dir = new File(debugDir + "/rules/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileWriter f = new FileWriter(dir.getCanonicalPath() + "/RULES-" + owner.getName() + ".txt");
            f.write(s);
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reset2() {
        try {
            rete.halt();
            Iterator iter = rete.listFacts();
            while (iter.hasNext()) {
                Fact f = (Fact) iter.next();
                if (f.getDeftemplate().getBaseName().startsWith("exec")) {
                    rete.retract(f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void retractObsoleteActions() throws JessException {

        for (InstanceOLD rule : owner.getBehaviour().getChildInstances(BehaviourFrame.RL_LIST)) {
            if (rule.getAttribute("retract-osbolete-actions") != null
                    && rule.getAttribute("retract-osbolete-actions").toValueString().equalsIgnoreCase("true")) {
                for (InstanceOLD a : rule.getChildInstances(UserRuleFrame.INST_LIST_CONS)) {
                    ActionDef action = new ActionDef(a);
                    String[] params = action.getObjectClassParams();

                    List<String> arg = null;
                    for (String s : params) {
                        String[] path = s.split("/");
                        String list = path[0];
                        InstanceOLD[] children = owner.getChildInstances(list);
                        for (int i = 0; i < children.length; i++) {
                            InstanceOLD object = children[i];
                            String name = list + "/" + object.getName();

                            if (arg == null) {
                                arg = new ArrayList<String>();
                            }
                            arg.add(name);
                        }
                    }
                    for (String s : arg) {
                        String actionName = action.getClassName();
                        List<Fact> facts = FactHandler.getInstance().getActionsByNonMatchingArgs(actionName, arg, rete.getGlobalContext());
                        for (Fact f : facts) {
                            rete.retract(f);
                        }
                    }
                }
            }
        }

    }

    private static void deleteDirContents(String dirName) {
        File dir = new File(dirName);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        } else {
            dir.mkdirs();
        }
    }

    static {
        deleteDirContents("/home/gsim/tmp/agentsrv-logging");
        deleteDirContents("/home/gsim/tmp/facts");
        deleteDirContents("/home/gsim/tmp/trees");
        deleteDirContents("/home/gsim/tmp/rules");
    }

}
