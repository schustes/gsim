package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

import de.s2.gsim.objects.attribute.AttributeFactory;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.util.Utils;

public class AgentClassOperations {

    private EntitiesContainer container;

    private AgentInstanceOperations agentInstanceOperations;

    private ObjectInstanceOperations objectInstanceOperations;

    private ObjectClassOperations objectClassOperations;

    AgentClassOperations(EntitiesContainer container, AgentInstanceOperations agentInstanceOperations,
            ObjectInstanceOperations objectInstanceOperations, ObjectClassOperations objectClassOperations) {
        this.container = container;
        this.agentInstanceOperations = agentInstanceOperations;
        this.objectInstanceOperations = objectInstanceOperations;
        this.objectClassOperations = objectClassOperations;
    }

    public GenericAgentClass addAgentClassAttribute(GenericAgentClass cls, Path<DomainAttribute> path, DomainAttribute a) {

        GenericAgentClass here = this.findGenericAgentClass(cls);
        container.replaceAgentSubClass(here, cls);
        here.replaceChildAttribute(path, a);
        here.setDirty(true);

        Set<GenericAgentClass> agentSubClasses = container.getAgentSubClasses();
        for (GenericAgentClass subClass : agentSubClasses) {
            if (subClass.isSuccessor(cls.getName())) {
                subClass.replaceAncestor(here);
                container.getAllInstancesOfClass(subClass, GenericAgent.class).parallelStream().forEach(succ -> {
                    succ.setFrame(subClass);
                    succ.replaceChildAttribute(Path.attributePath(path.toStringArray()), AttributeFactory.createDefaultAttribute(a));
                });
            }
        }

        container.getAllInstancesOfClass(cls, GenericAgent.class).parallelStream().forEach(succ -> {
            succ.setFrame(cls);
            succ.replaceChildAttribute(Path.attributePath(path.toStringArray()), AttributeFactory.createDefaultAttribute(a));
        });


        this.addChildAttributeInReferringAgents(cls, path, a);
        objectClassOperations.addChildAttributeInReferringObjects(cls, path, a);

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
                ListIterator<Instance> successorMembers = getInstancesOfClass(p).listIterator();
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

        ListIterator<Instance> iter2 = getInstancesOfClass(here).listIterator();
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

    public void addChildFrameInReferringAgents(Frame here, String[] path, Frame added) {
        GenericAgentClass[] objects = getAgentSubClasses();
        for (int i = 0; i < objects.length; i++) {
            GenericAgentClass c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                Frame[] ff = c.getChildFrames(s[j]);
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

    public GenericAgentClass addChildObject(GenericAgentClass cls, String[] path, Frame f) {
        GenericAgentClass here = this.findGenericAgentClass(cls);

        String name = f.getTypeName();

        UnitOperations.setChildFrame(here, path, f);
        here.setDirty(true);

        agentSubClasses.add(here);

        String listName = path[path.length - 1];

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();
        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.setDirty(true);
                c.replaceAncestor(here);
                iter.set(c);

                ListIterator<Instance> successors = getInstancesOfClass(c).listIterator();
                while (successors.hasNext()) {
                    GenericAgent a = (GenericAgent) successors.next();
                    a.setDirty(true);
                    a.addChildInstance(listName, new Instance(name, f));
                    a.setFrame(c);
                    successors.set(a);
                }
            }
        }

        ListIterator<Instance> iter2 = getInstancesOfClass(here).listIterator();
        while (iter2.hasNext()) {
            GenericAgent a = (GenericAgent) iter2.next();
            a.addChildInstance(listName, new Instance(name, f));
            a.setFrame(here);
            a.setDirty(true);
            iter2.set(a);
        }

        addChildFrameInReferringAgents(here, path, f);
        addChildFrameInReferringObjects(here, path, f);

        return (GenericAgentClass) here.clone();
    }

    public BehaviourFrame addRuleToBehaviour(BehaviourFrame fr, UserRuleFrame ur) {
        ListIterator<Frame> iter = behaviourClasses.listIterator();
        Frame here = null;
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
                f.replaceAncestor(here);
                iter.set(f);
            }
        }
        return (BehaviourFrame) here.clone();
    }

    public GenericAgentClass changeAgentClassBehaviour(GenericAgentClass c, BehaviourFrame b) {

        GenericAgentClass here = this.findGenericAgentClass(c);
        ListIterator<Instance> successorMembers = null;

        ArrayList<String> removed = new ArrayList<String>(b.removed);
        b.removed.clear();

        BehaviourFrame n = new BehaviourFrame(b);
        here.setBehaviour(n);

        ListIterator iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass p = (GenericAgentClass) iter.next();
            if (p.isSuccessor(c.getTypeName())) {
                p.replaceAncestor(here);
                BehaviourFrame sb = p.getBehaviour();
                sb.replaceAncestor(n);

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

    public GenericAgentClass extendAgentClassRole(GenericAgentClass original, Frame role) {

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
        HashSet<Frame> set = new HashSet<Frame>();
        while (iter.hasNext()) {
            Frame c = (Frame) iter.next();
            if (c.isSuccessor(x)) {
                set.add(c);
            }
        }
        GenericAgentClass[] res = new GenericAgentClass[set.size()];
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
        Frame f = this.findGenericAgentClass(frame);
        HashSet<GenericAgentClass> successors = new HashSet<GenericAgentClass>();
        Iterator iter = agentSubClasses.iterator();
        while (iter.hasNext()) {
            GenericAgentClass p = (GenericAgentClass) iter.next();
            Frame fr = p.getParentFrame(f.getTypeName());
            if (fr != null && fr.getTypeName().equals(frame)) {
                successors.add(p);
            }
        }
        GenericAgentClass[] ff = new GenericAgentClass[successors.size()];
        successors.toArray(ff);
        return ff;
    }




    public GenericAgentClass modifyAgentClassAttribute(GenericAgentClass cls, String[] path, DomainAttribute a) {
        GenericAgentClass here = this.findGenericAgentClass(cls);

        UnitOperations.setChildAttribute(here, Utils.removeFromArray(path, a.getName()), a);
        here.setDirty(true);
        agentSubClasses.add(here);

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();

            if (c.isSuccessor(cls.getTypeName())) {
                c.setDirty(true);
                UnitOperations.setChildAttribute(c, Utils.removeFromArray(path, a.getName()), (DomainAttribute) a.clone());
                c.replaceAncestor(here);
                iter.set(c);
                ListIterator successorMembers = getInstancesOfClass(c).listIterator();
                while (successorMembers.hasNext()) {
                    GenericAgent succ = (GenericAgent) successorMembers.next();
                    succ.setDirty(true);
                    UnitOperations.setChildAttribute(succ, Utils.removeFromArray(path, a.getName()),
                            AttributeFactory.createDefaultAttribute(a));
                    agents.add(succ);
                }
            }
        }

        ListIterator members = getInstancesOfClass(cls).listIterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setDirty(true);
            UnitOperations.setChildAttribute(c, Utils.removeFromArray(path, a.getName()), AttributeFactory.createDefaultAttribute(a));
            agents.add(c);
        }
        return (GenericAgentClass) here.clone();
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
            Frame cc = (Frame) iter.next();
            if (cc.isSuccessor(cls.getTypeName())) {
                iter.remove();
                removed.add(cc);
            }

            ListIterator iter2 = getInstancesOfClass(cc).listIterator();
            while (iter2.hasNext()) {
                Instance c = (Instance) iter2.next();
                removed.add(c);
                iter2.remove();
            }

            if (cc.equals(here)) {
                iter.remove();
                removed.add(here);
            }
        }

        removeFrameInReferringAgents(here, new String[0]);
        removeFrameInReferringObjectClasses(here, new String[0]);

    }

    public GenericAgentClass removeAgentClassAttribute(GenericAgentClass cls, String[] path, String a) {
        GenericAgentClass here = this.findGenericAgentClass(cls);

        UnitOperations.removeAttribute(here, path, a);
        here.setDirty(true);
        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(cls.getTypeName())) {
                UnitOperations.removeAttribute(c, path, a);
                c.setDirty(true);
                c.replaceAncestor(here);
                iter.set(c);
                ListIterator<Instance> successorMembers = getInstancesOfClass(c).listIterator();
                while (successorMembers.hasNext()) {
                    GenericAgent succ = (GenericAgent) successorMembers.next();
                    UnitOperations.removeAttribute(succ, path, a);
                    succ.setDirty(true);
                    succ.setFrame(c);
                    successorMembers.set(succ);
                }
            }
        }

        ListIterator<Instance> members = getInstancesOfClass(cls).listIterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            UnitOperations.removeAttribute(c, path, a);
            c.setDirty(true);
            c.setFrame(cls);
            members.set(c);
        }

        agentSubClasses.add(here);

        removeDeletedAttributeInReferringAgents(cls, path, a);
        removeDeletedAttributeInReferringObjects(cls, path, a);

        return (GenericAgentClass) here.clone();

    }


    public GenericAgentClass removeAttributeList(GenericAgentClass owner, String listName) throws GSimDefException {

        GenericAgentClass here = this.findGenericAgentClass(owner);

        here.removeDeclaredAttributeList(listName);
        agentSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setFrame(here);
            c.removeDeclaredAttributeList(listName);
            agents.add(c);
        }

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.replaceAncestor(here);
                c.removeDeclaredAttributeList(listName);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    GenericAgent cc = (GenericAgent) members.next();
                    cc.setFrame(c);
                    cc.removeDeclaredAttributeList(listName);
                    agents.add(cc);
                }
            }
        }

        return (GenericAgentClass) here.clone();

    }

    public GenericAgentClass removeChildFrame(GenericAgentClass cls, String[] path, String name) {

        GenericAgentClass here = this.findGenericAgentClass(cls);

        String[] fullPath = new String[path.length + 1];
        Utils.addToArray(path, fullPath, name);

        UnitOperations.removeChildFrame(here, path, name);
        agentSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.setFrame(here);
            UnitOperations.removeChildInstance(c, path, name);
            agents.add(c);
        }

        ListIterator<GenericAgentClass> iter = agentSubClasses.listIterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();
            if (c.isSuccessor(here.getTypeName()) && c.getDeclaredFrame(path[0], name) != null) {
                c.replaceAncestor(here);
                UnitOperations.removeChildFrame(c, path, name);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    GenericAgent cc = (GenericAgent) members.next();
                    cc.setFrame(c);
                    UnitOperations.removeChildInstance(cc, path, name);
                    agents.add(cc);
                }
            }
        }

        return (GenericAgentClass) here.clone();
    }



    TimeOrderedSet getGenericAgentSubClassesRef() {
        return agentSubClasses;
    }

    void setAgentClass(GenericAgentClass c) {
        agentClass = c;
    }


    protected TimeOrderedSet<?> clone(TimeOrderedSet<? extends Unit> set) {
        TimeOrderedSet<Unit> n = new TimeOrderedSet<Unit>();
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            Unit u = (Unit) iter.next();
            n.add((Unit) u.clone());
        }
        return n;
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



    protected void removeDeletedAttributeInReferringAgents(Frame here, String[] path, String deleted) {
        GenericAgentClass[] objects = getAgentSubClasses();
        for (int i = 0; i < objects.length; i++) {
            GenericAgentClass c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                Frame[] ff = c.getChildFrames(s[j]);
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

    protected void removeDeletedAttributeInReferringObjects(Frame here, String[] path, String deleted) {
        Frame[] objects = getObjectSubClasses();
        for (int i = 0; i < objects.length; i++) {
            Frame c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                Frame[] ff = c.getChildFrames(s[j]);
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

    protected void removeFrameInReferringAgents(Frame removed, String[] path) {
        GenericAgentClass[] objects = getAgentSubClasses();
        for (int i = 0; i < objects.length; i++) {
            GenericAgentClass c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                Frame[] ff = c.getChildFrames(s[j]);
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

    public void addChildAttributeInReferringAgents(GenericAgentClass here, Path path, DomainAttribute added) {
        for (GenericAgentClass agentClass : getAgentSubClasses()) {
            for (String listname : agentClass.getDeclaredFrameListNames()) {
                for (Frame f : agentClass.getChildFrames(listname)) {
                    if (f.isSuccessor(here.getName())) {
                        Path newPath = Path.attributePath(listname, f.getName(), path.toStringArray());
                        addAgentClassAttribute(agentClass, newPath, added);
                    }
                }
            }
        }
    }


}
