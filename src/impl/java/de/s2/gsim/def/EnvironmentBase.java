package de.s2.gsim.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;
import de.s2.gsim.def.objects.UnitOLD;
import de.s2.gsim.def.objects.UnitUtils;
import de.s2.gsim.def.objects.agent.BehaviourDef;
import de.s2.gsim.def.objects.agent.BehaviourFrame;
import de.s2.gsim.def.objects.agent.GenericAgent;
import de.s2.gsim.def.objects.agent.GenericAgentClass;
import de.s2.gsim.def.objects.behaviour.ActionFrame;
import de.s2.gsim.def.objects.behaviour.RLRule;
import de.s2.gsim.def.objects.behaviour.RLRuleFrame;
import de.s2.gsim.def.objects.behaviour.UserRule;
import de.s2.gsim.def.objects.behaviour.UserRuleFrame;
import de.s2.gsim.objects.attribute.AttributeFactory;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.util.Utils;

/**
 * Environment operations are split in 2 parts to - EnvironmentBase bundles all Frame related operations. Furthermore, the data is stored in this
 * class and is base class for several Environment related classes.
 *
 * @author Stephan
 *
 */
public class EnvironmentBase {

    protected HashMap<String, String> actionMappings = new HashMap<String, String>();

    protected GenericAgentClass agentClass;

    protected HashMap<String, String[]> agentMappings = new HashMap<String, String[]>();

    protected HashMap<String, Integer> agentOrder = new HashMap<String, Integer>();

    protected HashMap<String, String> agentPauses = new HashMap<String, String>();

    protected HashMap<String, String> agentRtClassMappings = new HashMap<String, String>();

    protected TimeOrderedSet<GenericAgent> agents = new TimeOrderedSet<GenericAgent>();

    protected TimeOrderedSet<GenericAgentClass> agentSubClasses = new TimeOrderedSet<GenericAgentClass>();

    protected FrameOLD behaviourClass;

    protected TimeOrderedSet<FrameOLD> behaviourClasses = new TimeOrderedSet<FrameOLD>();

    protected HashMap<String, String> dataHandlers = new HashMap<String, String>();

    protected String ns;

    protected FrameOLD objectClass;

    protected TimeOrderedSet<InstanceOLD> objects = new TimeOrderedSet<InstanceOLD>();

    protected TimeOrderedSet<FrameOLD> objectSubClasses = new TimeOrderedSet<FrameOLD>();

    protected HashSet<UnitOLD> removed = new HashSet<UnitOLD>();

    protected HashMap<String, String> systemAgents = new HashMap<String, String>();

    public EnvironmentBase() {
        agentClass = new GenericAgentClass();
        objectClass = new FrameOLD("Object", "object");
    }

    public GenericAgentClass addAgentClassAttribute(GenericAgentClass cls, String[] path, DomainAttribute a) {

        GenericAgentClass here = this.findGenericAgentClass(cls);
        UnitUtils.getInstance().setChildAttribute(here, Utils.removeFromArray(path, a.getName()), a);
        here.setDirty(true);

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(cls.getTypeName())) {
                c.setAncestor(here);
                c.setDirty(true);
                iter.set(c);
                ListIterator<InstanceOLD> successorMembers = getInstancesOfClass(c).listIterator();
                while (successorMembers.hasNext()) {
                    GenericAgent succ = (GenericAgent) successorMembers.next();
                    succ.setFrame(c);
                    UnitUtils.getInstance().setChildAttribute(succ, path, AttributeFactory.createDefaultAttribute(a));
                    agents.add(succ);
                    succ.setDirty(true);
                }
            }
        }

        ListIterator members = getInstancesOfClass(cls).listIterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setFrame(cls);
            c.setDirty(true);
            UnitUtils.getInstance().setChildAttribute(c, path, AttributeFactory.createDefaultAttribute(a));
            agents.add(c);
        }

        agentSubClasses.add(here);

        addChildAttributeInReferringAgents(cls, path, a);
        addChildAttributeInReferringObjects(cls, path, a);

        return (GenericAgentClass) here.clone();
    }

    public GenericAgentClass addAgentClassRule(GenericAgentClass cls, UserRuleFrame f) {

        GenericAgentClass here = this.findGenericAgentClass(cls);
        BehaviourFrame b = here.getBehaviour();

        if (!(f instanceof RLRuleFrame)) {
            b.addRule(f);
        } else {
            b.addRLRule((RLRuleFrame) f);
        }
        here.setBehaviour(b);
        agentSubClasses.add(here);
        here.setDirty(true);
        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();
        while (iter.hasNext()) {
            GenericAgentClass p = iter.next();
            p.setDirty(true);
            if (p.isSuccessor(here.getTypeName())) {
                BehaviourFrame beh = p.getBehaviour();
                if (!(f instanceof RLRuleFrame)) {
                    beh.addRule(new UserRuleFrame(new UserRuleFrame[] { f }, f.getTypeName(), f.getCategory()));
                } else {
                    beh.addRLRule(new RLRuleFrame(new RLRuleFrame[] { (RLRuleFrame) f }, f.getTypeName(), f.getCategory()));
                }
                iter.set(p);
                ListIterator<InstanceOLD> successorMembers = getInstancesOfClass(p).listIterator();
                while (successorMembers.hasNext()) {
                    GenericAgent a = (GenericAgent) successorMembers.next();
                    a.setDirty(true);
                    if (!(f instanceof RLRuleFrame)) {
                        UserRule urInst = new UserRule(f, f.getTypeName());
                        a.getBehaviour().addRule(urInst);
                    } else {
                        RLRule cf = new RLRule(f, f.getTypeName());
                        a.getBehaviour().addRLRule(cf);
                    }
                    successorMembers.set(a);
                }
            }
        }

        ListIterator<InstanceOLD> iter2 = getInstancesOfClass(here).listIterator();
        while (iter.hasNext()) {
            GenericAgent p = (GenericAgent) iter2.next();
            if (!(f instanceof RLRuleFrame)) {
                UserRule urInst = new UserRule(f, f.getTypeName());
                p.getBehaviour().addRule(urInst);
            } else {
                RLRule cf = new RLRule(f, f.getTypeName());
                p.getBehaviour().addRLRule(cf);
            }
            p.setDirty(true);
            iter2.set(p);
        }

        return (GenericAgentClass) here.clone();
    }

    public void addAgentSubClass(GenericAgentClass cls) {
        agentSubClasses.add(cls);
        if (!agentOrder.containsKey(cls.getTypeName())) {
            agentOrder.put(cls.getTypeName(), -1);
        }
    }

    public FrameOLD addChildFrame(FrameOLD cls, String[] path, FrameOLD f) {
        FrameOLD here = findObjectClass(cls);
        UnitUtils.getInstance().setChildFrame(here, path, f);
        here.setDirty(true);
        objectSubClasses.add(here);

        FrameOLD[] c = getAllObjectSuccessors(cls.getTypeName());
        for (int i = 0; i < c.length; i++) {
            c[i].setAncestor(here);
            // c[i].setChildFrame(path, f);
        }
        Iterator iter = getInstancesOfClass(f).iterator();

        while (iter.hasNext()) {
            InstanceOLD inst = (InstanceOLD) iter.next();
            InstanceOLD instance = new InstanceOLD(f.getTypeName(), f);
            UnitUtils.getInstance().setChildInstance(inst, path, instance);
        }

        addChildFrameInReferringObjects(cls, path, f);
        return (FrameOLD) here.clone();

    }

    public void addChildFrameInReferringAgents(FrameOLD here, String[] path, FrameOLD added) {
        GenericAgentClass[] objects = getAgentSubClasses();
        for (int i = 0; i < objects.length; i++) {
            GenericAgentClass c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                FrameOLD[] ff = c.getChildFrames(s[j]);
                for (int k = 0; k < ff.length; k++) {
                    if (ff[k].isSuccessor(here.getTypeName()) || ff[k].getTypeName().equals(here.getTypeName())) {
                        String[] newPath = new String[path.length + 2];
                        newPath[0] = s[j];
                        newPath[1] = ff[k].getTypeName();
                        for (int m = 0; m < path.length; m++) {
                            newPath[m + 2] = path[m];
                        }
                        addChildObject(c, newPath, added);
                    }
                }
            }
        }
    }

    public GenericAgentClass addChildObject(GenericAgentClass cls, String[] path, FrameOLD f) {
        GenericAgentClass here = this.findGenericAgentClass(cls);

        String name = f.getTypeName();

        UnitUtils.getInstance().setChildFrame(here, path, f);
        here.setDirty(true);

        agentSubClasses.add(here);

        String listName = path[path.length - 1];

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();
        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.setDirty(true);
                c.setAncestor(here);
                iter.set(c);

                ListIterator<InstanceOLD> successors = getInstancesOfClass(c).listIterator();
                while (successors.hasNext()) {
                    GenericAgent a = (GenericAgent) successors.next();
                    a.setDirty(true);
                    a.addChildInstance(listName, new InstanceOLD(name, f));
                    a.setFrame(c);
                    successors.set(a);
                }
            }
        }

        ListIterator<InstanceOLD> iter2 = getInstancesOfClass(here).listIterator();
        while (iter2.hasNext()) {
            GenericAgent a = (GenericAgent) iter2.next();
            a.addChildInstance(listName, new InstanceOLD(name, f));
            a.setFrame(here);
            a.setDirty(true);
            iter2.set(a);
        }

        addChildFrameInReferringAgents(here, path, f);
        addChildFrameInReferringObjects(here, path, f);

        return (GenericAgentClass) here.clone();
    }

    public FrameOLD addObjectClassAttribute(FrameOLD cls, String[] path, DomainAttribute a) {
        FrameOLD here = findObjectClass(cls);

        UnitUtils.getInstance().setChildAttribute(here, Utils.removeFromArray(path, a.getName()), a);
        here.setDirty(true);

        ListIterator<FrameOLD> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            FrameOLD c = iter.next();
            if (c.isSuccessor(cls.getTypeName())) {
                c.setAncestor(here);
                iter.set(c);
            }
        }

        objectSubClasses.add(here);

        addChildAttributeInReferringObjects(cls, path, a);
        addChildAttributeInReferringAgents(cls, path, a);
        return (FrameOLD) here.clone();
    }

    public void addObjectSubClass(FrameOLD cls) {
        objectSubClasses.add(cls);
    }

    public void addOrSetAgentMapping(String agentName, String[] roleNames) {
        agentMappings.put(agentName, roleNames);
    }

    public void addOrSetAgentOrdering(Integer order, String roleName) {
        agentOrder.put(roleName, order);
    }

    public void addOrSetRuntimeRoleMapping(String role, String cls) {
        agentRtClassMappings.put(role, cls);
    }

    public void addOrSetSystemAgent(String name, String cls) {
        systemAgents.put(name, cls);
    }

    public BehaviourFrame addRuleToBehaviour(BehaviourFrame fr, UserRuleFrame ur) {
        ListIterator<FrameOLD> iter = behaviourClasses.listIterator();
        FrameOLD here = null;
        while (iter.hasNext()) {
            BehaviourFrame f = (BehaviourFrame) iter.next();
            if (f.getTypeName().equals(fr.getTypeName())) {
                f.addRule(ur);
                here = f;
                iter.set(f);
            }
        }
        iter = behaviourClasses.listIterator();
        while (iter.hasNext()) {
            BehaviourFrame f = (BehaviourFrame) iter.next();
            if (f.isSuccessor(fr.getTypeName())) {
                f.addRule(ur);
                f.setAncestor(here);
                iter.set(f);
            }
        }
        return (BehaviourFrame) here.clone();
    }

    public GenericAgentClass changeAgentClassBehaviour(GenericAgentClass c, BehaviourFrame b) {

        GenericAgentClass here = this.findGenericAgentClass(c);
        ListIterator<InstanceOLD> successorMembers = null;

        ArrayList<String> removed = new ArrayList<String>(b.removed);
        b.removed.clear();

        BehaviourFrame n = new BehaviourFrame(b);
        here.setBehaviour(n);

        ListIterator iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass p = (GenericAgentClass) iter.next();
            if (p.isSuccessor(c.getTypeName())) {
                p.setAncestor(here);
                BehaviourFrame sb = p.getBehaviour();
                sb.setAncestor(n);

                if (b.isDirty()) {
                    sb.setMaxNodes(b.getMaxNodes());
                    sb.setRevalProb(b.getRevalProb());
                    sb.setRevisitCost(b.getRevisitCost());
                    sb.setStateUpdateInterval(b.getStateUpdateInterval());
                }

                for (UserRuleFrame f : b.getRules()) {
                    if (f.isDirty() && sb.getDeclaredRule(f.getTypeName()) != null) {
                        sb.addRule(f);
                    }
                }
                for (RLRuleFrame f : b.getRLRule()) {
                    if (f.isDirty() && sb.getDeclaredRLRule(f.getTypeName()) != null) {
                        sb.addRule(f);
                    }
                }
                for (ActionFrame f : b.getAvailableActions()) {
                    if (f.isDirty()) {
                        sb.addAction(f);
                    }
                }
                for (String s : removed) {
                    try {
                        sb.removeRLRule(s);
                        sb.removeRule(s);
                    } catch (Exception e) {
                        e.printStackTrace(); // DC
                    }
                }

                p.setBehaviour(sb);

                successorMembers = getInstancesOfClass(p).listIterator();
                while (successorMembers.hasNext()) {
                    GenericAgent a = (GenericAgent) successorMembers.next();
                    a.setDirty(true);
                    BehaviourDef np = new BehaviourDef(sb);
                    a.setFrame(p);
                    a.setBehaviour(np);
                    successorMembers.set(a);
                }
            }
        }

        for (UserRuleFrame f : b.getRules()) {
            f.setDirty(false);
        }
        for (RLRuleFrame f : b.getRLRule()) {
            f.setDirty(false);
        }
        for (ActionFrame f : b.getAvailableActions()) {
            f.setDirty(false);
        }

        b.setDirty(false);
        n = new BehaviourFrame(b);
        here.setBehaviour(n);

        successorMembers = getInstancesOfClass(here).listIterator();
        while (successorMembers.hasNext()) {
            GenericAgent a = (GenericAgent) successorMembers.next();
            a.setDirty(true);
            BehaviourDef np = new BehaviourDef(b);
            a.setFrame(here);
            a.setBehaviour(np);
            successorMembers.set(a);
        }

        return (GenericAgentClass) here.clone();
    }

    public GenericAgentClass createAgentSubclass(String name, GenericAgentClass parent) {

        GenericAgentClass successor = null;
        if (parent != null) {
            successor = new GenericAgentClass(parent, name);
            successor.setSystem(false);
            agentSubClasses.add(successor);
        } else {
            successor = new GenericAgentClass(agentClass, name);
            successor.setSystem(false);
            agentSubClasses.add(successor);
        }

        if (!agentOrder.containsKey(name)) {
            agentOrder.put(name, -1);
        }

        return (GenericAgentClass) successor.clone();

    }

    public BehaviourFrame createBehaviour(String name, BehaviourFrame parent) {
        BehaviourFrame p = null;

        if (parent != null) {
            BehaviourFrame here = getBehaviour(parent.getTypeName());
            p = new BehaviourFrame(name, here, EntityConstants.TYPE_BEHAVIOUR);
        } else {
            p = new BehaviourFrame(name, EntityConstants.TYPE_BEHAVIOUR);
            ActionFrame[] f = agentClass.getBehaviour().getAvailableActions();
            for (int i = 0; i < f.length; i++) {
                p.addAction((ActionFrame) f[i].clone());
            }

        }

        p.setSystem(false);
        behaviourClasses.add(p);
        return (BehaviourFrame) p.clone();
    }

    public FrameOLD createObjectSubClass(String name, FrameOLD parent) {
        if (parent == null) {
            FrameOLD p = new FrameOLD(name, EntityConstants.TYPE_OBJECT);
            p.setSystem(false);
            objectSubClasses.add(p);
            return (FrameOLD) p.clone();
        } else {
            FrameOLD p = new FrameOLD(new FrameOLD[] { parent }, name, EntityConstants.TYPE_OBJECT);
            p.setSystem(false);
            objectSubClasses.add(p);
            return (FrameOLD) p.clone();
        }
    }

    public GenericAgentClass extendAgentClassRole(GenericAgentClass original, FrameOLD role) {

        GenericAgentClass newRoleType = (GenericAgentClass) role.clone();
        // newRoleType.setTypeName(original.getTypeName());

        GenericAgentClass c = this.findGenericAgentClass(original);
        if (c != null) {
            // this.removeAgentClass(c);
        } else {
            c = original;
        }
        GenericAgentClass c2 = (GenericAgentClass) c.clone();
        // agentSubClasses.remove(c);

        GenericAgentClass nc = new GenericAgentClass(c2, newRoleType, original.getTypeName());
        agentSubClasses.add(nc);// overwrites older version!
        return (GenericAgentClass) nc.clone();

    }

    public HashMap getActionMappings() {
        return actionMappings;
    }

    public GenericAgentClass getAgentClassRef() {
        return agentClass;
    }

    public HashMap<String, String> getAgentIntervals() {
        return agentPauses;
    }

    public HashMap getAgentMappings() {
        return agentMappings;
    }

    public String[] getAgentNames(String ofClass) {

        ArrayList<String> set = new ArrayList<String>();
        Iterator iter = agents.iterator();

        while (iter.hasNext()) {
            GenericAgent cust = (GenericAgent) iter.next();
            if (cust.inheritsFrom(ofClass)) {
                set.add(cust.getName());
            }
        }

        String[] ret = new String[set.size()];
        set.toArray(ret);

        return ret;

    }

    // public GenericAgent[] getAgents(String ofClass) throws GSimDefException;

    // public String[] getAgentNames(String ofClass) throws GSimDefException;

    public HashMap getAgentOrdering() {
        return agentOrder;
    }

    public GenericAgent[] getAgents(String ofClass) {

        ArrayList<GenericAgent> set = new ArrayList<GenericAgent>();
        Iterator iter = agents.iterator();

        while (iter.hasNext()) {
            GenericAgent cust = (GenericAgent) iter.next();
            if (cust.inheritsFrom(ofClass)) {
                set.add(cust);
            }
        }

        GenericAgent[] ret = new GenericAgent[set.size()];
        set.toArray(ret);
        return ret;

    }

    public GenericAgentClass getAgentSubClass(String className) {
        Iterator iter = agentSubClasses.iterator();
        while (iter.hasNext()) {
            GenericAgentClass c = (GenericAgentClass) iter.next();
            if (c.getTypeName().equals(className)) {
                return c;
            }
        }
        return null;
    }

    public GenericAgentClass[] getAgentSubClasses() {
        GenericAgentClass[] res = new GenericAgentClass[agentSubClasses.size()];
        agentSubClasses.toArray(res);
        return res;
    }

    public GenericAgentClass[] getAllAgentClassSuccessors(String x) {
        ListIterator iter = agentSubClasses.listIterator();
        HashSet<FrameOLD> set = new HashSet<FrameOLD>();
        while (iter.hasNext()) {
            FrameOLD c = (FrameOLD) iter.next();
            if (c.isSuccessor(x)) {
                set.add(c);
            }
        }
        GenericAgentClass[] res = new GenericAgentClass[set.size()];
        set.toArray(res);
        return res;
    }

    public ArrayList<InstanceOLD> getAllInstancesOfClass(FrameOLD f) {
        ArrayList<InstanceOLD> set = new ArrayList<InstanceOLD>();
        Iterator iter = null;

        if (f instanceof GenericAgentClass) {
            iter = agents.iterator();
        } else if (f instanceof FrameOLD) {
            iter = objects.iterator();
        }

        if (iter != null) {

            while (iter.hasNext()) {
                InstanceOLD cust = (InstanceOLD) iter.next();
                if (cust.inheritsFrom(f)) {
                    set.add(cust);
                }
            }
        }
        return set;
    }

    public FrameOLD[] getAllObjectSuccessors(String x) {
        ListIterator iter = objectSubClasses.listIterator();
        HashSet<FrameOLD> set = new HashSet<FrameOLD>();
        while (iter.hasNext()) {
            FrameOLD c = (FrameOLD) iter.next();
            if (c.isSuccessor(x)) {
                set.add(c);
            }
        }
        FrameOLD[] res = new FrameOLD[set.size()];
        set.toArray(res);
        return res;
    }

    public BehaviourFrame getBehaviour(String behaviourName) {
        Iterator iter = behaviourClasses.iterator();
        while (iter.hasNext()) {
            BehaviourFrame f = (BehaviourFrame) iter.next();
            if (f.getTypeName().equals(behaviourName)) {
                return (BehaviourFrame) f.clone();
            }
        }
        return null;
    }

    public BehaviourFrame[] getBehaviours() {
        BehaviourFrame[] b = new BehaviourFrame[behaviourClasses.size()];
        behaviourClasses.toArray(b);
        return b;
    }

    public GenericAgentClass getGenericAgentClass() {
        return agentClass;
    }

    public GenericAgentClass[] getImmediateAgentClassSuccessors(String frame) {
        FrameOLD f = this.findGenericAgentClass(frame);
        HashSet<GenericAgentClass> successors = new HashSet<GenericAgentClass>();
        Iterator iter = agentSubClasses.iterator();
        while (iter.hasNext()) {
            GenericAgentClass p = (GenericAgentClass) iter.next();
            FrameOLD fr = p.getParentFrame(f.getTypeName());
            if (fr != null && fr.getTypeName().equals(frame)) {
                successors.add(p);
            }
        }
        GenericAgentClass[] ff = new GenericAgentClass[successors.size()];
        successors.toArray(ff);
        return ff;
    }

    public FrameOLD[] getImmediateObjectSuccessors(String frame) {
        FrameOLD f = findProductClass(frame);
        HashSet<FrameOLD> successors = new HashSet<FrameOLD>();
        Iterator iter = objectSubClasses.iterator();
        while (iter.hasNext()) {
            FrameOLD p = (FrameOLD) iter.next();
            FrameOLD fr = p.getParentFrame(f.getTypeName());
            if (fr != null && fr.getTypeName().equals(frame)) {
                successors.add(p);
            }
        }
        FrameOLD[] ff = new FrameOLD[successors.size()];
        successors.toArray(ff);
        return ff;
    }

    public ArrayList<InstanceOLD> getInstancesOfClass(FrameOLD f) {
        ArrayList<InstanceOLD> set = new ArrayList<InstanceOLD>();
        Iterator iter = null;

        if (f instanceof GenericAgentClass) {
            iter = agents.iterator();
        } else if (f instanceof FrameOLD) {
            iter = objects.iterator();
        }

        if (iter != null) {

            while (iter.hasNext()) {
                InstanceOLD cust = (InstanceOLD) iter.next();
                FrameOLD directAncestor = cust.getDefinition();
                if (directAncestor != null && directAncestor.getTypeName().equals(f.getTypeName())) {
                    set.add(cust);
                }
            }
        }
        return set;
    }

    public FrameOLD getObjectClass() {
        return objectClass;
    }

    public FrameOLD getObjectClassRef() {
        return objectClass;
    }

    public FrameOLD getObjectSubClass(String productName) {
        Iterator iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            FrameOLD cls = (FrameOLD) iter.next();
            if (cls.getTypeName().equals(productName)) {
                return (FrameOLD) cls.clone();
            }
        }
        return null;

    }

    public FrameOLD[] getObjectSubClasses() {
        FrameOLD[] res = new FrameOLD[objectSubClasses.size()];
        objectSubClasses.toArray(res);
        return res;
    }

    public HashMap getRuntimeRoleMappings() {
        return agentRtClassMappings;
    }

    public HashMap getSystemAgents() {
        return systemAgents;
    }

    public GenericAgentClass modifyAgentClassAttribute(GenericAgentClass cls, String[] path, DomainAttribute a) {
        GenericAgentClass here = this.findGenericAgentClass(cls);

        UnitUtils.getInstance().setChildAttribute(here, Utils.removeFromArray(path, a.getName()), a);
        here.setDirty(true);
        agentSubClasses.add(here);

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();

            if (c.isSuccessor(cls.getTypeName())) {
                c.setDirty(true);
                UnitUtils.getInstance().setChildAttribute(c, Utils.removeFromArray(path, a.getName()), (DomainAttribute) a.clone());
                c.setAncestor(here);
                iter.set(c);
                ListIterator successorMembers = getInstancesOfClass(c).listIterator();
                while (successorMembers.hasNext()) {
                    GenericAgent succ = (GenericAgent) successorMembers.next();
                    succ.setDirty(true);
                    UnitUtils.getInstance().setChildAttribute(succ, Utils.removeFromArray(path, a.getName()),
                            AttributeFactory.createDefaultAttribute(a));
                    agents.add(succ);
                }
            }
        }

        ListIterator members = getInstancesOfClass(cls).listIterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setDirty(true);
            UnitUtils.getInstance().setChildAttribute(c, Utils.removeFromArray(path, a.getName()), AttributeFactory.createDefaultAttribute(a));
            agents.add(c);
        }
        return (GenericAgentClass) here.clone();
    }

    public FrameOLD modifyObjectClassAttribute(FrameOLD cls, String[] path, DomainAttribute a) {
        FrameOLD here = findObjectClass(cls);

        UnitUtils.getInstance().setChildAttribute(here, Utils.removeFromArray(path, a.getName()), a);
        here.setDirty(true);

        objectSubClasses.add(here);

        ListIterator<FrameOLD> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            FrameOLD c = iter.next();
            c.setDirty(true);
            if (c.isSuccessor(cls.getTypeName())) {
                c.setAncestor(here);
                UnitUtils.getInstance().setChildAttribute(c, Utils.removeFromArray(path, a.getName()), (DomainAttribute) a.clone());
                iter.set(c);
            }
        }
        return (FrameOLD) here.clone();
    }

    public void removeAgentClass(GenericAgentClass cls) {

        ListIterator iter = null;
        GenericAgentClass here = this.findGenericAgentClass(cls);

        iter = agentSubClasses.listIterator();
        while (iter.hasNext()) {
            GenericAgentClass cc = (GenericAgentClass) iter.next();
            if (cc.isSuccessor(cls.getTypeName())) {
                iter.remove();
                removed.add(cc);
            }

            ListIterator iter2 = getInstancesOfClass(cc).listIterator();
            while (iter2.hasNext()) {
                GenericAgent c = (GenericAgent) iter2.next();
                removed.add(c);
                iter2.remove();
            }

            if (cc.equals(here)) {
                iter.remove();
                removed.add(here);
            }
        }

        iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            FrameOLD cc = (FrameOLD) iter.next();
            if (cc.isSuccessor(cls.getTypeName())) {
                iter.remove();
                removed.add(cc);
            }

            ListIterator iter2 = getInstancesOfClass(cc).listIterator();
            while (iter2.hasNext()) {
                InstanceOLD c = (InstanceOLD) iter2.next();
                removed.add(c);
                iter2.remove();
            }

            if (cc.equals(here)) {
                iter.remove();
                removed.add(here);
            }
        }

        removeFrameInReferringAgents(here, new String[0]);
        removeFrameInReferringObjects(here, new String[0]);

    }

    public GenericAgentClass removeAgentClassAttribute(GenericAgentClass cls, String[] path, String a) {
        GenericAgentClass here = this.findGenericAgentClass(cls);

        UnitUtils.getInstance().removeAttribute(here, path, a);
        here.setDirty(true);
        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(cls.getTypeName())) {
                UnitUtils.getInstance().removeAttribute(c, path, a);
                c.setDirty(true);
                c.setAncestor(here);
                iter.set(c);
                ListIterator<InstanceOLD> successorMembers = getInstancesOfClass(c).listIterator();
                while (successorMembers.hasNext()) {
                    GenericAgent succ = (GenericAgent) successorMembers.next();
                    UnitUtils.getInstance().removeAttribute(succ, path, a);
                    succ.setDirty(true);
                    succ.setFrame(c);
                    successorMembers.set(succ);
                }
            }
        }

        ListIterator<InstanceOLD> members = getInstancesOfClass(cls).listIterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            UnitUtils.getInstance().removeAttribute(c, path, a);
            c.setDirty(true);
            c.setFrame(cls);
            members.set(c);
        }

        agentSubClasses.add(here);

        removeDeletedAttributeInReferringAgents(cls, path, a);
        removeDeletedAttributeInReferringObjects(cls, path, a);

        return (GenericAgentClass) here.clone();

    }

    public FrameOLD removeAttributeList(FrameOLD owner, String listName) throws GSimDefException {
        FrameOLD here = findObjectClass(owner);

        here.removeAttributeList(listName);
        objectSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            InstanceOLD c = (InstanceOLD) members.next();
            c.setFrame(here);
            c.removeAttributeList(listName);
            agents.add((GenericAgent) c);
        }

        ListIterator<FrameOLD> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            FrameOLD c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.setAncestor(here);
                c.removeAttributeList(listName);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    InstanceOLD cc = (InstanceOLD) members.next();
                    cc.setFrame(c);
                    cc.removeAttributeList(listName);
                    agents.add((GenericAgent) cc);
                }
            }
        }

        // this.removeFrameInReferringAgents(removed, path);
        // this.removeFrameInReferringObjects(removed, path);

        return (FrameOLD) here.clone();

    }

    public GenericAgentClass removeAttributeList(GenericAgentClass owner, String listName) throws GSimDefException {

        GenericAgentClass here = this.findGenericAgentClass(owner);

        here.removeAttributeList(listName);
        agentSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setFrame(here);
            c.removeAttributeList(listName);
            agents.add(c);
        }

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.setAncestor(here);
                c.removeAttributeList(listName);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    GenericAgent cc = (GenericAgent) members.next();
                    cc.setFrame(c);
                    cc.removeAttributeList(listName);
                    agents.add(cc);
                }
            }
        }

        return (GenericAgentClass) here.clone();

    }

    public FrameOLD removeChildFrame(FrameOLD cls, String[] path, FrameOLD f) {
        FrameOLD here = findObjectClass(cls);
        UnitUtils.getInstance().removeChildFrame(here, path, f.getTypeName());
        here.setDirty(true);
        objectSubClasses.add(here);

        FrameOLD[] c = getAllObjectSuccessors(cls.getTypeName());
        for (int i = 0; i < c.length; i++) {
            UnitUtils.getInstance().removeChildFrame(c[i], path, f.getTypeName());
        }

        Iterator iter = getInstancesOfClass(f).iterator();
        while (iter.hasNext()) {
            InstanceOLD inst = (InstanceOLD) iter.next();

            InstanceOLD[] list = (InstanceOLD[]) inst.resolveName(path);
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].inheritsFrom(f)) {
                        UnitUtils.getInstance().removeChildInstance(inst, path, list[i].getName());
                    }
                }
            }
        }
        // this.removeFrameInReferringObjects(f, path);
        // this.removeFrameInReferringAgents(f, path);

        return (FrameOLD) here.clone();
    }

    public GenericAgentClass removeChildFrame(GenericAgentClass cls, String[] path, String name) {

        GenericAgentClass here = this.findGenericAgentClass(cls);

        String[] fullPath = new String[path.length + 1];
        Utils.addToArray(path, fullPath, name);

        UnitUtils.getInstance().removeChildFrame(here, path, name);
        agentSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setFrame(here);
            UnitUtils.getInstance().removeChildInstance(c, path, name);
            agents.add(c);
        }

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName()) && c.getDeclaredFrame(path[0], name) != null) {
                c.setAncestor(here);
                UnitUtils.getInstance().removeChildFrame(c, path, name);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    GenericAgent cc = (GenericAgent) members.next();
                    cc.setFrame(c);
                    UnitUtils.getInstance().removeChildInstance(cc, path, name);
                    agents.add(cc);
                }
            }
        }

        return (GenericAgentClass) here.clone();
    }

    public void removeObjectClass(FrameOLD cls) {
        ListIterator<FrameOLD> iter = null;
        FrameOLD here = findObjectClass(cls);

        iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            FrameOLD c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                removed.add(c);
                iter.remove();
            }
        }

        objectSubClasses.remove(here);

        removeFrameInReferringAgents(cls, new String[0]);

    }

    public FrameOLD removeObjectClassAttribute(FrameOLD cls, String[] path, String a) {
        FrameOLD here = findObjectClass(cls);

        UnitUtils.getInstance().removeAttribute(here, path, a);
        here.setDirty(true);

        ListIterator<FrameOLD> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            FrameOLD c = iter.next();
            if (c.isSuccessor(cls.getTypeName())) {
                c.setDirty(true);
                UnitUtils.getInstance().removeAttribute(c, path, a);
                c.setAncestor(here);
                iter.set(c);
            }
        }

        ListIterator<InstanceOLD> iter2 = objects.listIterator();
        while (iter2.hasNext()) {
            InstanceOLD c = iter2.next();
            if (c.inheritsFrom(cls)) {
                c.setDirty(true);
                UnitUtils.getInstance().removeAttribute(c, path, a);
                c.setFrame(here);
                iter2.set(c);
            }
        }

        objectSubClasses.add(here);

        removeDeletedAttributeInReferringObjects(here, path, a);
        removeDeletedAttributeInReferringAgents(here, path, a);

        return (FrameOLD) here.clone();

    }

    public void setActionMappings(HashMap<String, String> map) {
        actionMappings = map;
    }

    public void setAgentMappings(HashMap<String, String[]> map) {
        agentMappings = map;
    }

    TimeOrderedSet getGenericAgentSubClassesRef() {
        return agentSubClasses;
    }

    TimeOrderedSet getObjectSubClassesRef() {
        return objectSubClasses;
    }

    void setAgentClass(GenericAgentClass c) {
        agentClass = c;
    }

    void setObjectClass(FrameOLD c) {
        objectClass = c;
    }

    protected TimeOrderedSet<?> clone(TimeOrderedSet<? extends UnitOLD> set) {
        TimeOrderedSet<UnitOLD> n = new TimeOrderedSet<UnitOLD>();
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            UnitOLD u = (UnitOLD) iter.next();
            n.add((UnitOLD) u.clone());
        }
        return n;
    }

    protected void copyEnvironment(EnvironmentBase env) throws GSimDefException {
        try {
            behaviourClasses = new TimeOrderedSet<FrameOLD>(this.clone(env.behaviourClasses));
            objects = new TimeOrderedSet<InstanceOLD>(this.clone(env.objects));
            objectSubClasses = new TimeOrderedSet<FrameOLD>(this.clone(env.objectSubClasses));
            agentClass = (GenericAgentClass) env.agentClass.clone();

            agentSubClasses = new TimeOrderedSet<GenericAgentClass>(this.clone(env.agentSubClasses));
            agents = new TimeOrderedSet<GenericAgent>(this.clone(env.agents));

            if (env.behaviourClass != null) {
                behaviourClass = (FrameOLD) env.behaviourClass.clone();
            }
            agentClass = (GenericAgentClass) env.agentClass.clone();
            objectClass = (FrameOLD) env.objectClass.clone();

            agentPauses = new HashMap<String, String>(env.agentPauses);
            dataHandlers = new HashMap<String, String>(env.dataHandlers);
            agentMappings = new HashMap<String, String[]>(env.agentMappings);
            agentOrder = new HashMap<String, Integer>(env.agentOrder);
            agentRtClassMappings = new HashMap<String, String>(env.agentRtClassMappings);
            systemAgents = new HashMap<String, String>(env.systemAgents);
        } catch (Exception e) {
            throw new GSimDefException("Error in copy env", e);
        }
    }

    protected GenericAgent findGenericAgent(String extern) {
        Iterator iter = agents.iterator();
        while (iter.hasNext()) {
            GenericAgent cls = (GenericAgent) iter.next();
            if (cls.getName().equals(extern)) {
                return cls;
            }
        }
        return null;
    }

    protected GenericAgentClass findGenericAgentClass(GenericAgentClass extern) {
        Iterator iter = agentSubClasses.listIterator();
        while (iter.hasNext()) {
            GenericAgentClass cls = (GenericAgentClass) iter.next();
            if (cls.getTypeName().equals(extern.getTypeName())) {
                return cls;
            }
        }
        if (extern.getTypeName().equals(GenericAgentClass.NAME)) {
            return agentClass;
        }
        return null;
    }

    protected GenericAgentClass findGenericAgentClass(String extern) {
        Iterator iter = agentSubClasses.listIterator();
        while (iter.hasNext()) {
            GenericAgentClass cls = (GenericAgentClass) iter.next();
            if (cls.getTypeName().equals(extern)) {
                return cls;
            }
        }
        if (extern.equals(GenericAgentClass.NAME)) {
            return agentClass;
        }
        return null;
    }

    protected InstanceOLD findObject(String extern) {
        Iterator iter = objects.iterator();
        while (iter.hasNext()) {
            InstanceOLD cls = (InstanceOLD) iter.next();
            if (cls.getName().equals(extern)) {
                return cls;
            }
        }
        return null;
    }

    protected FrameOLD findObjectClass(FrameOLD extern) {
        Iterator iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            FrameOLD cls = (FrameOLD) iter.next();
            if (cls.getTypeName().equals(extern.getTypeName())) {
                return cls;
            }
        }
        if (extern.getTypeName().equals(objectClass.getTypeName())) {
            return objectClass;
        }
        return null;
    }

    protected FrameOLD findProductClass(String extern) {
        Iterator iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            FrameOLD cls = (FrameOLD) iter.next();
            if (cls.getTypeName().equals(extern)) {
                return cls;
            }
        }
        return null;
    }

    protected void removeDeletedAttributeInReferringAgents(FrameOLD here, String[] path, String deleted) {
        GenericAgentClass[] objects = getAgentSubClasses();
        for (int i = 0; i < objects.length; i++) {
            GenericAgentClass c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                FrameOLD[] ff = c.getChildFrames(s[j]);
                for (int k = 0; k < ff.length; k++) {
                    if (ff[k].isSuccessor(here.getTypeName()) || ff[k].getTypeName().equals(here.getTypeName())) {
                        String[] newPath = new String[path.length + 2];
                        newPath[0] = s[j];
                        newPath[1] = ff[k].getTypeName();
                        for (int m = 0; m < path.length; m++) {
                            newPath[m + 2] = path[m];
                        }
                        removeAgentClassAttribute(c, newPath, deleted);
                    }
                }
            }
        }
    }

    protected void removeDeletedAttributeInReferringObjects(FrameOLD here, String[] path, String deleted) {
        FrameOLD[] objects = getObjectSubClasses();
        for (int i = 0; i < objects.length; i++) {
            FrameOLD c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                FrameOLD[] ff = c.getChildFrames(s[j]);
                for (int k = 0; k < ff.length; k++) {
                    if (ff[k].isSuccessor(here.getTypeName()) || ff[k].getTypeName().equals(here.getTypeName())) {
                        String[] newPath = new String[path.length + 2];
                        newPath[0] = s[j];
                        newPath[1] = ff[k].getTypeName();
                        for (int m = 0; m < path.length; m++) {
                            newPath[m + 2] = path[m];
                        }
                        removeObjectClassAttribute(c, newPath, deleted);
                    }
                }
            }
        }
    }

    protected void removeFrameInReferringAgents(FrameOLD removed, String[] path) {
        GenericAgentClass[] objects = getAgentSubClasses();
        for (int i = 0; i < objects.length; i++) {
            GenericAgentClass c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                FrameOLD[] ff = c.getChildFrames(s[j]);
                for (int k = 0; k < ff.length; k++) {
                    if (ff[k] != null && (ff[k].isSuccessor(removed.getTypeName()) || ff[k].getTypeName().equals(removed.getTypeName()))) {
                        String[] newPath = new String[1];
                        newPath[0] = s[j];
                        this.removeChildFrame(c, newPath, ff[k].getTypeName());
                    }
                }
            }
        }
    }

    // if a frame is being removed, delete references to this frame in all other
    // objects that are holding references to this frame.
    protected void removeFrameInReferringObjects(FrameOLD removed, String[] path) {
        FrameOLD[] objects = getObjectSubClasses();
        for (int i = 0; i < objects.length; i++) {
            FrameOLD c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                FrameOLD[] ff = c.getChildFrames(s[j]);
                for (int k = 0; k < ff.length; k++) {
                    if (ff[k].isSuccessor(removed.getTypeName()) || ff[k].getTypeName().equals(removed.getTypeName())) {
                        String[] newPath = new String[path.length + 2];
                        newPath[0] = s[j];
                        newPath[1] = ff[k].getTypeName();
                        for (int m = 0; m < path.length; m++) {
                            newPath[m + 2] = path[m];
                        }
                        this.removeChildFrame(c, newPath, removed);
                    }
                }
            }
        }
    }

    protected void setNamespace(String ns) {
        this.ns = ns;
    }

    private void addChildAttributeInReferringAgents(FrameOLD here, String[] path, DomainAttribute added) {
        GenericAgentClass[] objects = getAgentSubClasses();
        for (int i = 0; i < objects.length; i++) {
            GenericAgentClass c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                FrameOLD[] ff = c.getChildFrames(s[j]);
                for (int k = 0; k < ff.length; k++) {
                    if (ff[k].isSuccessor(here.getTypeName()) || ff[k].getTypeName().equals(here.getTypeName())) {
                        String[] newPath = new String[path.length + 2];
                        newPath[0] = s[j];
                        newPath[1] = ff[k].getTypeName();
                        for (int m = 0; m < path.length; m++) {
                            newPath[m + 2] = path[m];
                        }
                        addAgentClassAttribute(c, newPath, added);
                    }
                }
            }
        }
    }

    private void addChildAttributeInReferringObjects(FrameOLD here, String[] path, DomainAttribute added) {
        FrameOLD[] objects = getObjectSubClasses();
        for (int i = 0; i < objects.length; i++) {
            FrameOLD c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                FrameOLD[] ff = c.getChildFrames(s[j]);
                for (int k = 0; k < ff.length; k++) {
                    if (ff[k].isSuccessor(here.getTypeName()) || ff[k].getTypeName().equals(here.getTypeName())) {
                        String[] newPath = new String[path.length + 2];
                        newPath[0] = s[j];
                        newPath[1] = ff[k].getTypeName();
                        for (int m = 0; m < path.length; m++) {
                            newPath[m + 2] = path[m];
                        }
                        addObjectClassAttribute(c, newPath, added);
                    }
                }
            }
        }
    }

    // when a containing object of a frame was added or in any way modified
    private void addChildFrameInReferringObjects(FrameOLD here, String[] path, FrameOLD added) {
        FrameOLD[] objects = getObjectSubClasses();
        for (int i = 0; i < objects.length; i++) {
            FrameOLD c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                FrameOLD[] ff = c.getChildFrames(s[j]);
                for (int k = 0; k < ff.length; k++) {
                    if (ff[k].isSuccessor(here.getTypeName()) || ff[k].getTypeName().equals(here.getTypeName())) {
                        String[] newPath = new String[path.length + 2];
                        newPath[0] = s[j];
                        newPath[1] = ff[k].getTypeName();
                        for (int m = 0; m < path.length; m++) {
                            newPath[m + 2] = path[m];
                        }
                        addChildFrame(c, newPath, added);
                    }
                }
            }
        }
    }

}
