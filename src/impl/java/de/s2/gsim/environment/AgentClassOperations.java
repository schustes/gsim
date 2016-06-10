package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import android.annotation.Nullable;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeFactory;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.util.Utils;

/**
 * TODO all get methods must return a clone!
 * 
 * @author stephan
 *
 */
public class AgentClassOperations {

    private Environment env;

    private EntitiesContainer container;

    private AgentInstanceOperations agentInstanceOperations;

    private ObjectInstanceOperations objectInstanceOperations;

    private ObjectClassOperations objectClassOperations;

    AgentClassOperations(EntitiesContainer container
            , AgentInstanceOperations agentInstanceOperations
            , ObjectInstanceOperations objectInstanceOperations
            , ObjectClassOperations objectClassOperations) {
        this.container = container;

        this.agentInstanceOperations = agentInstanceOperations;
        this.objectInstanceOperations = objectInstanceOperations;
        this.objectClassOperations = objectClassOperations;
    }

    public GenericAgentClass addAgentClassAttribute(GenericAgentClass cls, Path<DomainAttribute> path, DomainAttribute a) {

        GenericAgentClass here = this.findGenericAgentClass(cls);
        container.replaceAgentSubClass(here, cls);
        here.replaceChildAttribute(path, a);

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

        if ((f instanceof RLRuleFrame)) {
            b.addRLRule((RLRuleFrame) f);
        } else {
            b.addRule(f);
        }
        here.setBehaviour(b);

        Set<GenericAgentClass> agentSubClasses = container.getAgentSubClasses();
        for (GenericAgentClass subClass : agentSubClasses) {
            if (subClass.isSuccessor(cls.getName())) {
                BehaviourFrame beh = subClass.getBehaviour();
                if (!(f instanceof RLRuleFrame)) {
                    beh.addRule(UserRuleFrame.inherit(Arrays.asList(f), f.getName(), f.getCategory()));
                } else {
                    beh.addRLRule(RLRuleFrame.inherit(Arrays.asList(f), f.getName(), f.getCategory()));
                }

                subClass.replaceAncestor(here);
                container.getAllInstancesOfClass(subClass, GenericAgent.class).parallelStream().forEach(succ -> {
                    instanciateAndSetRule(f, succ);
                });
            }
        }

        container.getAllInstancesOfClass(cls, GenericAgent.class).parallelStream().forEach(succ -> {
            instanciateAndSetRule(f, succ);
        });

        return (GenericAgentClass) here.clone();
    }

    private void instanciateAndSetRule(UserRuleFrame f, GenericAgent succ) {
        if (!(f instanceof RLRuleFrame)) {
            UserRule urInst = UserRule.instanciate(f, f.getName());
            succ.getBehaviour().addRule(urInst);
        } else {
            RLRule cf = RLRule.instanciate(f, f.getName());
            succ.getBehaviour().addRLRule(cf);
        }
    }

    public void addAgentSubClass(GenericAgentClass cls) {
        container.getAgentSubClasses().add(GenericAgentClass.copy(cls));
        if (!env.getAgentOrder().containsKey(cls.getName())) {
            env.addAgentOrder(cls.getName(), -1);
        }
    }

    /**
     * Adds the frame addedObject to all frames of the same or inherited type of 'here' in all agent classes that refer
     * to this type. For example, agent has objectClass A1; A1 is modified by adding a child object A11, then all agent classes
     * having A1 will be added A11 to their contained A1.
     * 
     * @param here the frame that was modified
     * @param path the path in the modified frame that was added
     * @param addedObject the object that is added in path
     */
    public void addChildFrameInReferringAgents(Frame here, Path<Frame> path, Frame addedObject) {
        container.getAgentSubClasses().parallelStream().forEach(agent -> {
            for (String listName: agent.getDeclaredFrameListNames()) {
                agent.getChildFrames(listName).stream()
                .filter(child -> child.isSuccessor(here.getName()) || child.getName().equals(here.getName()))
                .forEach(child -> {
                    Path<Frame> p = Path.objectPath(listName, child.getName(), path.toStringArray());
                    addChildObject(agent, p, addedObject);
                });
            }
        });
    }

    public GenericAgentClass addChildObject(GenericAgentClass cls, Path<Frame> path, Frame f) {

        GenericAgentClass here = findGenericAgentClass(cls);

        Path<TypedList<Frame>> frameList = Path.withoutLastAttributeOrObject(path, Path.Type.OBJECT, Frame.class);
        TypedList<Frame> list = here.resolvePath(frameList);
        list.add(f);
        here.setDirty(true);

        Set<GenericAgentClass> agentSubClasses = container.getAgentSubClasses();
        agentSubClasses.parallelStream().filter(subClass->subClass.isSuccessor(cls.getName())).forEach(subClass-> {
            subClass.replaceAncestor(here);
            container.getAllInstancesOfClass(subClass, GenericAgent.class).parallelStream().forEach(succ -> {
                succ.setFrame(subClass);
                Path<TypedList<Instance>> instListPath = Path.withoutLastAttributeOrObject(path, Path.Type.OBJECT, Instance.class);
                TypedList<Instance> instList = succ.resolvePath(instListPath);
                instList.add( new Instance(f.getName(), f));
            });        	
        });

        container.getAllInstancesOfClass(cls, GenericAgent.class).parallelStream().forEach(succ -> {
            succ.setFrame(cls);
            Path<TypedList<Instance>> instListPath = Path.withoutLastAttributeOrObject(path, Path.Type.OBJECT, Instance.class);
            TypedList<Instance> instList = succ.resolvePath(instListPath);
            instList.add( new Instance(f.getName(), f));
        });

        addChildFrameInReferringAgents(here, path, f);

        return (GenericAgentClass) here.clone();
    }

    public BehaviourFrame addRuleToBehaviour(BehaviourFrame fr, UserRuleFrame ur) {
        BehaviourFrame here = null;
        for (BehaviourFrame f: container.getBehaviourClasses()) {
            if (f.getName().equals(fr.getName())) {
                f.addRule(ur);
                here = f;
            }
        }
        for (BehaviourFrame f: container.getBehaviourClasses()) {
            if (f.isSuccessor(fr.getName())) {
                f.addRule(ur);
                f.replaceAncestor(here);
            }
        }
        return (BehaviourFrame) here.clone();
    }

    public GenericAgentClass changeAgentClassBehaviour(GenericAgentClass c, BehaviourFrame behaviourFrame) {

        GenericAgentClass here = this.findGenericAgentClass(c);

        ArrayList<String> removed = new ArrayList<String>(behaviourFrame.removed);
        behaviourFrame.removed.clear();

        BehaviourFrame newBehaviour = BehaviourFrame.copy(behaviourFrame);
        here.setBehaviour(newBehaviour);

        for (GenericAgentClass p: container.getAgentSubClasses()) {
            if (p.isSuccessor(c.getName())) {
                p.replaceAncestor(here);
                BehaviourFrame sb = p.getBehaviour();
                sb.replaceAncestor(newBehaviour);

                if (behaviourFrame.isDirty()) {
                    sb.setMaxNodes(behaviourFrame.getMaxNodes());
                    sb.setRevalProb(behaviourFrame.getRevalProb());
                    sb.setRevisitCost(behaviourFrame.getRevisitCost());
                    sb.setStateUpdateInterval(behaviourFrame.getStateUpdateInterval());
                }

                for (UserRuleFrame f : behaviourFrame.getRules()) {
                    if (f.isDirty() && sb.getDeclaredRule(f.getName()) != null) {
                        sb.addRule(f);
                    }
                }
                for (RLRuleFrame f : behaviourFrame.getRLRule()) {
                    if (f.isDirty() && sb.getDeclaredRLRule(f.getName()) != null) {
                        sb.addRule(f);
                    }
                }
                for (ActionFrame f : behaviourFrame.getAvailableActions()) {
                    if (f.isDirty()) {
                        sb.addAction(f);
                    }
                }
                for (String s : removed) {
                    try {
                        sb.removeRLRule(s);
                        sb.removeRule(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                p.setBehaviour(sb);

                for (GenericAgent agent: container.getAllInstancesOfClass(p, GenericAgent.class)) {
                    agent.setDirty(true);
                    BehaviourDef np = new BehaviourDef(sb);
                    agent.setFrame(p);
                    agent.setBehaviour(np);
                }
            }
        }

        for (UserRuleFrame reactiveRule : behaviourFrame.getRules()) {
            reactiveRule.setDirty(false);
        }
        for (RLRuleFrame rlRule : behaviourFrame.getRLRule()) {
            rlRule.setDirty(false);
        }
        for (ActionFrame actionFrame : behaviourFrame.getAvailableActions()) {
            actionFrame.setDirty(false);
        }

        behaviourFrame.setDirty(false);
        here.setBehaviour(newBehaviour);

        for (GenericAgent a: container.getAllInstancesOfClass(here, GenericAgent.class)) {
            a.setDirty(true);
            BehaviourDef np = new BehaviourDef(behaviourFrame);
            a.setFrame(here);
            a.setBehaviour(np);
        }

        return (GenericAgentClass) here.clone();
    }

    public GenericAgentClass createAgentSubclass(String name, GenericAgentClass parent) {

        GenericAgentClass successor = null;
        if (parent == null) {
            successor = GenericAgentClass.inherit(parent, name);
        } else {
            successor = GenericAgentClass.inherit(parent, name);
        }

        container.addAgentClass(successor);

        if (!env.getAgentOrder().containsKey(name)) {
            env.addAgentOrder(name, -1);
        }

        return (GenericAgentClass) successor.clone();

    }

    public BehaviourFrame createBehaviour(String name, @Nullable BehaviourFrame parent) {
        BehaviourFrame behaviourFrame = null;

        if (parent != null) {
            BehaviourFrame here = getBehaviour(parent.getName());
            behaviourFrame = BehaviourFrame.inherit(name, Arrays.asList(here));
        } else {
            behaviourFrame = BehaviourFrame.newBehaviour(name); 
            for (ActionFrame actionFrame: container.getAgentClass().getBehaviour().getAvailableActions()) {
                behaviourFrame.addAction((ActionFrame) actionFrame.clone());
            }
        }

        container.addBehaviourClass(behaviourFrame);

        return (BehaviourFrame) behaviourFrame.clone();
    }

    public GenericAgentClass extendAgentClassRole(GenericAgentClass original, Frame role) {

        GenericAgentClass newRoleType = (GenericAgentClass) role.clone();

        GenericAgentClass c = this.findGenericAgentClass(original);
        if (c == null) {
            c = original;
        }
        GenericAgentClass c2 = (GenericAgentClass) c.clone();

        GenericAgentClass nc = GenericAgentClass.copyFromAndExtendWith(c2, newRoleType, original.getName());
        container.replaceAgentSubClass(c, nc);
        return (GenericAgentClass) nc.clone();

    }

    public List<String> getAgentNames(String ofClass) {

        return container
                .getAgents()
                .parallelStream()
                .filter(agent -> agent.inheritsFrom(ofClass))
                .map(GenericAgent::getName)
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getAgentOrdering() {
        return env.getAgentOrder();
    }

    public List<GenericAgent> getAgents(String ofClass) {
        return container
                .getAgents()
                .parallelStream()
                .filter(agent -> agent.inheritsFrom(ofClass))
                .collect(Collectors.toList());

    }

    public GenericAgentClass getAgentSubClass(String className) {
        return container
                .getAgentSubClasses()
                .parallelStream()
                .filter(sub -> sub.getName().equals(className))
                .findAny()
                .get();
    }

    public List<GenericAgentClass> getAgentSubClasses() {
        return new ArrayList<>(container.getAgentSubClasses());
    }

    /**
     * Returns all successors (not only the immediate ones).
     * 
     * @param agentClassName
     * @return list of successors
     */
    public List<GenericAgentClass> getAllAgentClassSuccessors(String agentClassName) {
        return container
                .getAgentSubClasses()
                .parallelStream()
                .filter(c -> c.isSuccessor(agentClassName))
                .collect(Collectors.toList());
    }

    public BehaviourFrame getBehaviour(String behaviourName) {
        return container
                .getBehaviourClasses()
                .stream()
                .filter(b -> b.getName().equals(behaviourName))
                .findAny()
                .get();
    }

    public List<BehaviourFrame> getBehaviours() {
        return new ArrayList<BehaviourFrame>(container.getBehaviourClasses());
    }

    public GenericAgentClass getGenericAgentClass() {
        return container.getAgentClass();
    }

    public List<GenericAgentClass> getImmediateAgentClassSuccessors(String frame) {
        return container.getAgentSubClasses()
                .parallelStream()
                .filter(p -> p.getParentFrame(frame) != null && p.getName().equals(frame))
                .collect(Collectors.toList());
    }

    public GenericAgentClass modifyAgentClassAttribute(GenericAgentClass cls, Path<DomainAttribute> path, DomainAttribute domainAttribute) {

        GenericAgentClass actualRef = this.findGenericAgentClass(cls);

        actualRef.replaceChildAttribute(path, domainAttribute);

        Iterator<GenericAgentClass> iter = container.getAgentSubClasses().iterator();

        while (iter.hasNext()) {
            GenericAgentClass c = iter.next();

            if (c.isSuccessor(actualRef.getName())) {
                c.replaceChildAttribute(path, domainAttribute);
                c.replaceAncestor(actualRef);
                replaceMemberAttributes(c, path, domainAttribute);
            }
        }

        replaceMemberAttributes(actualRef, path, domainAttribute);

        return (GenericAgentClass) actualRef.clone();
    }

    /**
     * Replaces attributes of all members (immediate successors) with the default value of the given domain attribute.
     * 
     * @param agentClass the class of the members to replace.
     * @param attributePath the path
     */
    private void replaceMemberAttributes(GenericAgentClass agentClass, Path<DomainAttribute> path, DomainAttribute replacement) {
        Path<Attribute> attributePath = Path.attributePath(path.toStringArray());
        Iterator<GenericAgent> members = container.getInstancesOfClass(agentClass, GenericAgent.class).iterator();
        while (members.hasNext()) {
            GenericAgent c = (GenericAgent) members.next();
            c.replaceChildAttribute(attributePath, AttributeFactory.createDefaultAttribute(replacement));
        }
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

    Set getGenericAgentSubClassesRef() {
        return container.getAgentSubClasses();
    }

    void setAgentClass(GenericAgentClass c) {
        container.setAgentClass(c);
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
