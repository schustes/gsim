package de.s2.gsim.def;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;
import de.s2.gsim.def.objects.UnitUtils;
import de.s2.gsim.def.objects.agent.BehaviourDef;
import de.s2.gsim.def.objects.agent.BehaviourFrame;
import de.s2.gsim.def.objects.agent.GenericAgent;
import de.s2.gsim.def.objects.agent.GenericAgentClass;
import de.s2.gsim.def.objects.behaviour.RLRule;
import de.s2.gsim.def.objects.behaviour.RLRuleFrame;
import de.s2.gsim.def.objects.behaviour.UserRule;
import de.s2.gsim.def.objects.behaviour.UserRuleFrame;
import de.s2.gsim.objects.attribute.Attribute;

public class Environment extends EnvironmentBase implements java.io.Serializable {

    public static final int RAND_ATT_ONLY = 2;

    public static final int RAND_ATT_ONLY_UNIFORM = 6;

    public static final int RAND_NONE = 4;

    protected static Logger logger = Logger.getLogger(Environment.class);

    private static int counter = 0;

    private static final long serialVersionUID = 1L;

    public Environment() {
    }// needed by EJB container!

    protected Environment(String ns) {
        this.ns = ns;
    }

    private Environment(String ns, java.io.InputStream setupFile) throws GSimDefException {

        this.ns = ns;
        EnvironmentSetup setup = new EnvironmentSetup(setupFile);

        agentClass = new GenericAgentClass();
        objectClass = new FrameOLD("Object", "object");

        // EnvironmentSetup setup = new EnvironmentSetup(setupFile);

        setup.runSetup();

        createBehaviourClasses();

        copyEnvironment(setup);

    }

    private Environment(String ns, java.io.InputStream setupFile, boolean open) throws GSimDefException {

        this.ns = ns;

        agentClass = new GenericAgentClass();
        objectClass = new FrameOLD("Object", "object");

        EnvironmentSetup setup = new EnvironmentSetup(setupFile);

        setup.runSetup();

        init();

        createBehaviourClasses();

        copyEnvironment(setup);

    }

    public BehaviourFrame activateBehaviourRule(BehaviourFrame fr, UserRuleFrame ur, boolean activated) {
        ListIterator<FrameOLD> iter = behaviourClasses.listIterator();
        BehaviourFrame here = null;
        while (iter.hasNext()) {
            BehaviourFrame f = (BehaviourFrame) iter.next();
            if (f.getTypeName().equals(fr.getTypeName())) {
                here = f;
                UserRuleFrame g = f.getRule(ur.getTypeName());
                g.setActivated(activated);
                f.addRule(g);
                iter.set(f);
            }
        }
        iter = behaviourClasses.listIterator();
        while (iter.hasNext()) {
            BehaviourFrame f = (BehaviourFrame) iter.next();
            if (f.isSuccessor(fr.getTypeName())) {
                UserRuleFrame g = f.getRule(ur.getTypeName());
                g.setActivated(activated);
                f.addRule(g);
                iter.set(f);
            }
        }
        return here;
    }

    public GenericAgent addAgentRule(GenericAgent p, UserRule f) {
        GenericAgent here = (GenericAgent) p.clone();

        if (!(f instanceof RLRule)) {
            here.getBehaviour().addRule(f);
        } else {
            here.getBehaviour().addRLRule((RLRule) f);
        }
        here.setDirty(true);
        here.setDirty(true);
        agents.add(here);
        return (GenericAgent) here.clone();
    }

    public FrameOLD addAttributeList(FrameOLD owner, String listName) throws GSimDefException {
        FrameOLD here = findObjectClass(owner);

        here.defineAttributeList(listName);
        objectSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            InstanceOLD c = (InstanceOLD) members.next();
            c.setFrame(here);
            c.defineAttributeList(listName);
            agents.add((GenericAgent) c);
        }

        ListIterator<FrameOLD> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            FrameOLD c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.setAncestor(here);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    InstanceOLD cc = (InstanceOLD) members.next();
                    cc.setFrame(c);
                    c.defineAttributeList(listName);
                    agents.add((GenericAgent) cc);
                }
            }
        }

        return (FrameOLD) here.clone();

    }

    public GenericAgentClass addAttributeList(GenericAgentClass owner, String listName) throws GSimDefException {
        GenericAgentClass here = this.findGenericAgentClass(owner);

        here.defineAttributeList(listName);
        agentSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setFrame(here);
            c.defineAttributeList(listName);
            agents.add(c);
        }

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.setAncestor(here);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    GenericAgent cc = (GenericAgent) members.next();
                    cc.setFrame(c);
                    c.defineAttributeList(listName);
                    agents.add(cc);
                }
            }
        }

        return (GenericAgentClass) here.clone();

    }

    public GenericAgent addChildInstance(GenericAgent a, String[] path, InstanceOLD child) {
        GenericAgent here = findGenericAgent(a.getName());
        UnitUtils.getInstance().setChildInstance(here, path, child);
        UserRule[] rules = here.getBehaviour().getRules();
        for (int i = 0; i < rules.length; i++) {
            here.getBehaviour().addRule(rules[i]);
        }
        here.setDirty(true);
        agents.add(here);
        return (GenericAgent) here.clone();
    }

    public GenericAgentClass addChildObjectList(GenericAgentClass owner, String listName, FrameOLD type) throws GSimDefException {
        GenericAgentClass here = this.findGenericAgentClass(owner);

        here.defineObjectList(listName, type);
        agentSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setFrame(here);
            c.defineObjectList(listName, type);
            agents.add(c);
        }

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.setAncestor(here);
                c.defineObjectList(listName, type);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    GenericAgent cc = (GenericAgent) members.next();
                    cc.setFrame(c);
                    cc.defineObjectList(listName, type);
                    agents.add(cc);
                }
            }
        }

        return (GenericAgentClass) here.clone();

    }

    public GenericAgent changeAgentBehaviour(GenericAgent c, BehaviourDef b) {
        GenericAgent here = findGenericAgent(c.getName());
        here.setBehaviour(b);
        here.setDirty(true);
        agents.add(here);
        return (GenericAgent) here.clone();
    }

    public GenericAgent changeAgentName(GenericAgent inst, String newName) {
        GenericAgent old = findGenericAgent(inst.getName());
        GenericAgent here = (GenericAgent) old.clone();
        removeGenericAgent(old);
        here.changeName(newName);
        here.setDirty(true);
        agents.add(here);
        return (GenericAgent) here.clone();
    }

    public GenericAgent changeChildInstanceName(GenericAgentClass owner, String[] pathToChild, String newName) {
        GenericAgent old = findGenericAgent(owner.getName());
        Object o = old.resolveName(pathToChild);
        if (o instanceof InstanceOLD) {
            InstanceOLD in = (InstanceOLD) o;
            in.changeName(newName);
            UnitUtils.getInstance().setChildInstance(old, pathToChild, in);
        }
        old.setDirty(true);
        agents.add(old);
        return old;
    }

    public Object cloneEnvironment() {
        Environment env = new Environment(ns);
        env.behaviourClasses = new TimeOrderedSet<FrameOLD>(this.clone(behaviourClasses));
        env.objects = new TimeOrderedSet<InstanceOLD>(this.clone(objects));
        env.objectSubClasses = new TimeOrderedSet<FrameOLD>(this.clone(objectSubClasses));
        env.agentClass = (GenericAgentClass) agentClass.clone();

        env.agentSubClasses = new TimeOrderedSet<GenericAgentClass>(this.clone(agentSubClasses));
        env.agents = new TimeOrderedSet<GenericAgent>(this.clone(agents));

        if (behaviourClass != null) {
            env.behaviourClass = (FrameOLD) behaviourClass.clone();
        }
        env.agentClass = (GenericAgentClass) agentClass.clone();
        env.objectClass = (FrameOLD) objectClass.clone();

        env.agentPauses = new HashMap<String, String>(agentPauses);
        env.dataHandlers = new HashMap<String, String>(dataHandlers);
        env.agentMappings = new HashMap<String, String[]>(agentMappings);
        env.agentOrder = new HashMap<String, Integer>(agentOrder);
        env.agentRtClassMappings = new HashMap<String, String>(agentRtClassMappings);
        env.systemAgents = new HashMap<String, String>(systemAgents);

        return env;
    }

    public void dismissChanges() {

        removed.clear();

        FramePersistenceManager frameControl = new FramePersistenceManager(ns);
        InstancePersistenceManager instanceControl = new InstancePersistenceManager(ns);

        ListIterator<GenericAgent> iter = agents.listIterator();
        while (iter.hasNext()) {
            InstanceOLD in = iter.next();
            if (in.isDirty()) {
                GenericAgent f = (GenericAgent) instanceControl.reload(in.getName());
                if (f != null) {
                    iter.set(f);
                } else {
                    iter.remove();
                }
            }
        }

        ListIterator<GenericAgentClass> iter2 = agentSubClasses.listIterator();
        while (iter2.hasNext()) {
            FrameOLD in = iter2.next();
            if (in.isDirty()) {
                GenericAgentClass f = (GenericAgentClass) frameControl.reload(in.getTypeName());
                if (f != null) {
                    iter2.set(f);
                } else {
                    iter2.remove();
                }
            }
        }

        ListIterator<FrameOLD> iter3 = objectSubClasses.listIterator();
        while (iter3.hasNext()) {
            FrameOLD in = iter3.next();
            if (in.isDirty()) {
                FrameOLD f = frameControl.reload(in.getTypeName());
                if (f != null) {
                    iter3.set(f);
                } else {
                    iter3.remove();
                }
            }
        }

        ListIterator<InstanceOLD> iter4 = objects.listIterator();
        while (iter4.hasNext()) {
            InstanceOLD in = iter.next();
            if (in.isDirty()) {
                InstanceOLD f = instanceControl.reload(in.getName());
                if (f != null) {
                    iter4.set(f);
                } else {
                    iter4.remove();
                }
            }
        }

    }

    public InheritanceHierarchy[] exportAgentHierarchy() {

        HashMap<String, InheritanceHierarchy> nodes = new HashMap<String, InheritanceHierarchy>();
        ListIterator iter = agentSubClasses.listIterator();

        FrameOLD root = agentClass;
        InheritanceHierarchy node = new InheritanceHierarchy((FrameOLD) root.clone());
        nodes.put(node.getFrame().getTypeName(), node);

        while (iter.hasNext()) {
            FrameOLD c = (FrameOLD) iter.next();
            FrameOLD cc = (FrameOLD) c.clone();
            node.insert(cc);
            nodes.put(node.getFrame().getTypeName(), node);
        }

        InheritanceHierarchy[] top = new InheritanceHierarchy[nodes.values().size()];
        nodes.values().toArray(top);

        return top;

    }

    public InheritanceHierarchy[] exportBehaviourHierarchy() {

        HashMap<String, InheritanceHierarchy> nodes = new HashMap<String, InheritanceHierarchy>();
        ListIterator iter = behaviourClasses.listIterator();
        FrameOLD root = behaviourClass;
        InheritanceHierarchy node = new InheritanceHierarchy((FrameOLD) root.clone());
        node.insert((FrameOLD) root.clone());

        while (iter.hasNext()) {
            BehaviourFrame c = (BehaviourFrame) iter.next();
            node.insert((FrameOLD) c.clone());
            nodes.put(node.getFrame().getTypeName(), node);
        }

        InheritanceHierarchy[] top = new InheritanceHierarchy[nodes.values().size()];
        nodes.values().toArray(top);

        return top;

    }

    public InheritanceHierarchy[] exportObjectHierarchy() {

        HashMap<String, InheritanceHierarchy> nodes = new HashMap<String, InheritanceHierarchy>();
        ListIterator iter = objectSubClasses.listIterator();

        FrameOLD root = objectClass;
        InheritanceHierarchy node = new InheritanceHierarchy((FrameOLD) root.clone());
        nodes.put(node.getFrame().getTypeName(), node);

        while (iter.hasNext()) {
            FrameOLD c = (FrameOLD) iter.next();
            node.insert((FrameOLD) c.clone());
            nodes.put(node.getFrame().getTypeName(), node);
        }

        InheritanceHierarchy[] top = new InheritanceHierarchy[nodes.values().size()];
        nodes.values().toArray(top);

        return top;

    }

    public GenericAgent getAgent(String name) {
        GenericAgent a = findGenericAgent(name);
        if (a == null) {
            return null;
        }
        return (GenericAgent) a.clone();
    }

    public GenericAgentClass getAgentClass() throws GSimDefException {
        return (GenericAgentClass) agentClass.clone();
    }

    public String[] getAgentNames() {
        HashSet<String> set = new HashSet<String>();
        Iterator iter = agents.iterator();
        while (iter.hasNext()) {
            set.add(((GenericAgent) iter.next()).getName());
        }
        String[] res = new String[set.size()];
        set.toArray(res);
        return res;
    }

    public GenericAgent[] getAgents(String parent, int offset, int count) throws GSimDefException {
        GenericAgent[] a = this.getAgents(parent);
        List<GenericAgent> ret = new ArrayList<GenericAgent>();

        int currentOff = 0;
        for (int i = offset; i < a.length; i++) {
            ret.add((GenericAgent) a[i].clone());
        }
        GenericAgent[] retArray = new GenericAgent[ret.size()];
        ret.toArray(retArray);
        return retArray;
    }

    public GenericAgent[] getGenericAgents() {
        GenericAgent[] res = new GenericAgent[agents.size()];
        agents.toArray(res);
        return res;
    }

    public String getNamespace() {
        return ns;
    }

    public InstanceOLD getObject(String name) {
        InstanceOLD here = findObject(name);
        if (here == null) {
            return null;
        }
        return (InstanceOLD) here.clone();
    }

    public int getTotalAgentCount() {
        logger.debug("getTotalAgentCount() delegate: " + agents.size() + ", this=" + this);
        return agents.size();
    }

    public GenericAgent instanciateAgent(GenericAgentClass cls, String name, int method, double svar) {
        GenericAgent a = new GenericAgent(name, cls);

        if (method != Environment.RAND_NONE) {
            Generator gen = new Generator();
            if (method == Environment.RAND_ATT_ONLY) {
                a = gen.randomiseAttributeValues(a, svar, Generator.Method.Normal);
            } else if (method == Environment.RAND_ATT_ONLY_UNIFORM) {
                a = gen.randomiseAttributeValues(a, svar, Generator.Method.Uniform);
            }
        }
        agents.add(a);
        return (GenericAgent) a.clone();
    }

    public GenericAgent[] instanciateAgents(GenericAgentClass parent, String prefix, int method, double svar, int count) {

        logger.debug("Instanciate agents(" + this + "): " + count + " of " + parent.getTypeName());

        GenericAgent[] a = new GenericAgent[count];

        // double id = this.hashCode() * idNo;
        for (int i = 0; i < count; i++) {
            // int id = Uniform.staticNextIntFromTo(0, 10000);
            counter++;
            String name;
            try {
                name = prefix + "-" + counter + "(" + java.net.InetAddress.getLocalHost().getCanonicalHostName() + ")";
            } catch (Exception e) {
                name = prefix + "-" + counter;
            }
            logger.debug("agent:" + name);
            a[i] = instanciateAgent(parent, name, method, svar);
        }

        Collections.shuffle(agents);

        return a;
    }

    public void instanciateAgents2(GenericAgentClass parent, String prefix, int method, double svar, int count) {

        logger.debug("Instanciate agents(" + this + "): " + count + " of " + parent.getTypeName());

        for (int i = 0; i < count; i++) {
            // int id = Uniform.staticNextIntFromTo(0, 10000);
            counter++;
            String name;
            try {
                name = prefix + "-" + counter + "(" + java.net.InetAddress.getLocalHost().getCanonicalHostName() + ")";
            } catch (Exception e) {
                name = prefix + "-" + counter;
            }
            logger.debug("agent:" + name);
            instanciateAgent(parent, name, method, svar);
        }

        Collections.shuffle(agents);

    }

    public InstanceOLD instanciateFrame(FrameOLD cls, String name) {
        InstanceOLD inst = new InstanceOLD(name, cls);
        objects.add(inst);
        return (InstanceOLD) inst.clone();
    }

    public GenericAgent modifyAgentAttribute(GenericAgent inst, String[] path, Attribute att) {

        GenericAgent agent = findGenericAgent(inst.getName());

        if (agent.resolveName(path) == null) {
            agent.setAttribute(att);
        }

        UnitUtils.getInstance().setChildAttribute(agent, path, att);
        agent.setDirty(true);
        agents.add(agent);
        return (GenericAgent) agent.clone();
    }

    public InstanceOLD modifyObjectAttribute(InstanceOLD inst, String[] path, Attribute att) {
        InstanceOLD here = findObject(inst.getName());
        UnitUtils.getInstance().setChildAttribute(here, path, att);
        here.setDirty(true);
        objects.add(here);
        return (InstanceOLD) here.clone();
    }

    public GenericAgent removeChildInstance(GenericAgent owner, String[] path, String child) {
        GenericAgent here = findGenericAgent(owner.getName());
        UnitUtils.getInstance().removeChildInstance(here, path, child);
        agents.add(here);
        return (GenericAgent) here.clone();
    }

    public GenericAgentClass removeChildObjectList(GenericAgentClass cls, String listName) throws GSimDefException {
        GenericAgentClass here = this.findGenericAgentClass(cls);

        here.removeChildFrameList(listName);
        agentSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setFrame(here);
            c.removeChildInstanceList(listName);
            agents.add(c);
        }

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.setAncestor(here);
                c.removeChildFrameList(listName);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    GenericAgent cc = (GenericAgent) members.next();
                    cc.setFrame(c);
                    cc.removeChildInstanceList(listName);
                    agents.add(cc);
                }
            }
        }

        return (GenericAgentClass) here.clone();

    }

    public void removeGenericAgent(GenericAgent agent) {
        Iterator iter = agents.iterator();
        while (iter.hasNext()) {
            GenericAgent a = (GenericAgent) iter.next();
            if (agent.getName().equals(a.getName())) {
                iter.remove();
                removed.add(a);
            }
        }
    }

    public void removeObject(InstanceOLD object) {
        Iterator iter = objects.iterator();
        while (iter.hasNext()) {
            InstanceOLD a = (InstanceOLD) iter.next();
            if (a.getName().equals(object.getName())) {
                iter.remove();
                removed.add(a);
            }
        }
    }

    // delete first all in removed-list from db

    public void saveEnvironment() {
        FramePersistenceManager frameControl = new FramePersistenceManager(ns);
        InstancePersistenceManager instanceControl = new InstancePersistenceManager(ns);

        Iterator iter = removed.iterator();

        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof FrameOLD) {
                frameControl.deleteFrame((FrameOLD) o);
            } else if (o instanceof InstanceOLD) {
                instanceControl.deleteInstance((InstanceOLD) o);
            }
        }
        removed.clear();

        iter = agentSubClasses.listIterator();
        while (iter.hasNext()) {
            FrameOLD f = (FrameOLD) iter.next();
            if (f.isDirty()) {
                frameControl.saveFrame(f);
            }
        }
        iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            FrameOLD f = (FrameOLD) iter.next();
            if (f.isDirty()) {
                frameControl.saveFrame(f);
            }
        }
        iter = behaviourClasses.listIterator();
        while (iter.hasNext()) {
            FrameOLD f = (FrameOLD) iter.next();
            if (f.isDirty()) {
                frameControl.saveFrame(f);
            }
        }

        iter = agents.iterator();
        while (iter.hasNext()) {
            InstanceOLD inst = (InstanceOLD) iter.next();
            if (inst.isDirty()) {
                inst.setDirty(false);
                instanceControl.saveInstance(inst);
            }
        }
        iter = objects.iterator();
        while (iter.hasNext()) {
            InstanceOLD inst = (InstanceOLD) iter.next();
            if (inst.isDirty()) {
                inst.setDirty(false);
                instanceControl.saveInstance(inst);
            }
        }

    }

    public GenericAgentClass setActivatedStatus(GenericAgentClass cls, String ruleName, boolean status) {

        GenericAgentClass here = this.findGenericAgentClass(cls);
        BehaviourFrame pb1 = here.getBehaviour();
        UserRuleFrame ur1 = pb1.getRule(ruleName);
        if (ur1 == null) {
            ur1 = pb1.getRLRule(ruleName);
            ur1.setActivated(status);
            pb1.addRLRule((RLRuleFrame) ur1);
        } else {
            ur1.setActivated(status);
            pb1.addRule(ur1);
        }
        here.setBehaviour(pb1);
        here.setDirty(true);

        agentSubClasses.add(here);

        ListIterator<InstanceOLD> iter = getInstancesOfClass(here).listIterator();
        while (iter.hasNext()) {
            GenericAgent p = (GenericAgent) iter.next();
            p.setDirty(true);
            BehaviourDef pb = p.getBehaviour();
            UserRule ur = pb.getRule(ruleName);
            if (ur == null) {
                ur = pb.getRLRule(ruleName);
                ur.setActivated(status);
                pb.addRLRule((RLRule) ur);
            } else {
                ur.setActivated(status);
                pb.addRule(ur);
            }
            p.setBehaviour(pb);
            iter.set(p);
        }

        ListIterator<GenericAgentClass> iter2 = agentSubClasses.listIterator();
        while (iter.hasNext()) {

            GenericAgentClass p = iter2.next();
            p.setDirty(true);
            if (p.isSuccessor(here.getTypeName())) {
                BehaviourFrame pb = p.getBehaviour();
                UserRuleFrame ur = pb.getRule(ruleName);
                if (ur == null) {
                    ur = pb.getRLRule(ruleName);
                    ur.setActivated(status);
                    pb.addRLRule((RLRuleFrame) ur);
                } else {
                    ur.setActivated(status);
                    pb.addRule(ur);
                }
                p.setBehaviour(pb);
                iter2.set(p);
                ListIterator<InstanceOLD> successorMembers = getInstancesOfClass(p).listIterator();
                while (successorMembers.hasNext()) {
                    GenericAgent a = (GenericAgent) iter.next();
                    p.setDirty(true);
                    BehaviourDef beh = a.getBehaviour();
                    UserRule ur2 = beh.getRule(ruleName);
                    if (ur2 == null) {
                        ur2 = beh.getRLRule(ruleName);
                        ur2.setActivated(status);
                        beh.addRule(ur2);
                    } else {
                        ur2.setActivated(status);
                        beh.addRule(ur2);
                    }
                    a.setBehaviour(beh);
                    successorMembers.set(a);
                }
            }
        }
        return (GenericAgentClass) here.clone();
    }

    public void setAgent(GenericAgent inst) throws GSimDefException {

        ListIterator<GenericAgent> iter = agents.listIterator();
        while (iter.hasNext()) {
            GenericAgent agent = iter.next();
            if (inst.getName().equals(agent.getName())) {
                if (!agent.getDefinition().getTypeName().equals(inst.getDefinition().getTypeName())) {
                    throw new GSimDefException("Agent " + inst.getName() + " must be of type " + agent.getDefinition().getTypeName() + ".");
                }
                iter.set(inst);
            }

        }

    }

    protected void createBehaviourClasses() {

        BehaviourFrame x = new BehaviourFrame("Behaviour", agentClass.getBehaviour(), de.s2.gsim.def.EntityConstants.TYPE_BEHAVIOUR);
        behaviourClass = x;
        behaviourClasses.add(x);

        x.setMutable(true);
        x.setSystem(false);

        Iterator iter = agentSubClasses.iterator();
        while (iter.hasNext()) {
            GenericAgentClass c = (GenericAgentClass) iter.next();
            BehaviourFrame f = new BehaviourFrame(c.getBehaviour(), EntityConstants.TYPE_BEHAVIOUR);
            f.setAncestor(x);
            behaviourClasses.add(f);
        }

    }

    /**
     * @TODO load here frames and instances. !!!!Make sure that the top-level classes are at the top of the loaded classes!!!!
     * 
     */
    protected void init() {
        FramePersistenceManager frameControl = new FramePersistenceManager(ns);
        InstancePersistenceManager instanceControl = new InstancePersistenceManager(ns);

        FrameOLD[] all = frameControl.getAllFrames();
        InstanceOLD[] in = instanceControl.loadAll();

        for (int i = 0; i < all.length; i++) {
            if (all[i] instanceof GenericAgentClass) {
                agentSubClasses.add((GenericAgentClass) all[i]);
                all[i].setAncestor(agentClass);

                for (int j = 0; j < in.length; j++) {
                    if (in[j].inheritsFrom(all[i])) {
                        in[j].getDefinition().setAncestor(all[i]);
                        agents.add((GenericAgent) in[j]);
                    }
                }
            } else if (all[i] instanceof BehaviourFrame) {
                behaviourClasses.add(all[i]);
            } else {
                all[i].setAncestor(objectClass);
                objectSubClasses.add(all[i]);
                for (int j = 0; j < in.length; j++) {
                    if (in[j].inheritsFrom(all[i])) {
                        in[j].getDefinition().setAncestor(all[i]);
                        objects.add(in[j]);
                    }
                }
            }
        }
    }

    public static Environment newInstance(String ns) throws GSimDefException {
        Environment e = new Environment(ns);
        logger.debug("Environment: " + e);
        return e;
    }

    public static Environment newInstance(String ns, java.io.InputStream setup) throws GSimDefException {
        Environment e = new Environment(ns, setup);
        logger.debug("Environment: " + e);
        return e;
    }

    public static Environment openInstance(String ns, java.io.InputStream setup) throws GSimDefException {
        Environment e = new Environment(ns, setup, true);
        return e;
    }

}
