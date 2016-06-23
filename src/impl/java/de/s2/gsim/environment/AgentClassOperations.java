package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import android.annotation.Nullable;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeFactory;
import de.s2.gsim.objects.attribute.DomainAttribute;

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

    public GenericAgentClass addAgentClassAttribute(GenericAgentClass cls, Path<List<DomainAttribute>> path, DomainAttribute a) {

        GenericAgentClass here = this.findGenericAgentClass(cls);
        container.replaceAgentSubClass(here, cls);

        here.addChildAttribute(path, a);

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
            succ.addChildAttribute(Path.attributePath(path.toStringArray()), AttributeFactory.createDefaultAttribute(a));
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

        Path<TypedList<Frame>> frameList = Path.withoutLastAttributeOrObject(path, Path.Type.OBJECT);
        TypedList<Frame> list = here.resolvePath(frameList);
        list.add(f);
        here.setDirty(true);

        Set<GenericAgentClass> agentSubClasses = container.getAgentSubClasses();
        agentSubClasses.parallelStream().filter(subClass->subClass.isSuccessor(cls.getName())).forEach(subClass-> {
            subClass.replaceAncestor(here);
            container.getAllInstancesOfClass(subClass, GenericAgent.class).parallelStream().forEach(succ -> {
                succ.setFrame(subClass);
                Path<TypedList<Instance>> instListPath = Path.withoutLastAttributeOrObject(path, Path.Type.OBJECT);
                TypedList<Instance> instList = succ.resolvePath(instListPath);
                instList.add( new Instance(f.getName(), f));
            });        	
        });

        container.getAllInstancesOfClass(cls, GenericAgent.class).parallelStream().forEach(succ -> {
            succ.setFrame(cls);
            Path<TypedList<Instance>> instListPath = Path.withoutLastAttributeOrObject(path, Path.Type.OBJECT);
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
                .map(GenericAgent::clone)
                .collect(Collectors.toList());
    }

    public GenericAgentClass getAgentSubClass(String className) {
        return container
                .getAgentSubClasses()
                .parallelStream()
                .filter(sub -> sub.getName().equals(className))
                .map(GenericAgentClass::clone)
                .findAny()
                .get();
    }

    public List<GenericAgentClass> getAgentSubClasses() {
        return container
                .getAgentSubClasses()
                .parallelStream()
                .map(GenericAgentClass::clone)
                .collect(Collectors.toList());
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
                .map(GenericAgentClass::clone)
                .collect(Collectors.toList());
    }

    public BehaviourFrame getBehaviour(String behaviourName) {
        return container
                .getBehaviourClasses()
                .stream()
                .filter(b -> b.getName().equals(behaviourName))
                .map(BehaviourFrame::clone)
                .findAny()
                .get();
    }

    public List<BehaviourFrame> getBehaviours() {
        return container
                .getBehaviourClasses()
                .parallelStream()
                .map(BehaviourFrame::clone)
                .collect(Collectors.toList());
    }

    public GenericAgentClass getGenericAgentClass() {
        return container.getAgentClass().clone();
    }

    public List<GenericAgentClass> getImmediateAgentClassSuccessors(String frame) {
        return container.getAgentSubClasses()
                .parallelStream()
                .filter(p -> p.getParentFrame(frame) != null && p.getName().equals(frame))
                .map(GenericAgentClass::clone)
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

        GenericAgentClass here = this.findGenericAgentClass(cls);

        for (Iterator<GenericAgentClass> iter = container.getAgentSubClasses().iterator(); iter.hasNext();) {
            GenericAgentClass cc = iter.next();
            if (cc.isSuccessor(cls.getName()) || cc.equals(here)) {
                iter.remove();
                removeFrameInReferringAgents(cc);
            }

            for (Iterator<GenericAgent> iter2 = container.getInstancesOfClass(cc, GenericAgent.class).iterator(); iter2.hasNext();) {
                iter2.next();
                iter2.remove();
            }
        }

        for (Iterator<Frame> iter = container.getObjectSubClasses().iterator(); iter.hasNext();) {
            Frame cc = (Frame) iter.next();
            if (cc.isSuccessor(cls.getName()) || cc.equals(here)) {
                objectClassOperations.removeFrameInReferringObjectClasses(cc);
                iter.remove();
            }
            for (Iterator<Instance> iter2 = container.getInstancesOfClass(cc, Instance.class).iterator(); iter2.hasNext();) {
                iter2.remove();
            }
        }

    }

    public GenericAgentClass removeAgentClassAttribute(GenericAgentClass cls, Path<DomainAttribute> path) {
        GenericAgentClass here = this.findGenericAgentClass(cls);
        container.getAgentSubClasses().parallelStream().filter(agentClass -> agentClass.isSuccessor(cls.getName())).forEach(agentClass -> {
            agentClass.replaceAncestor(here);
            container.getInstancesOfClass(agentClass, Instance.class).forEach(inst -> {
                inst.setFrame(agentClass);
            });
        });

        container.getInstancesOfClass(here, Instance.class).forEach(member -> {
            Path<Attribute> instancePath = Path.attributePath(path.toStringArray());
            member.removeChildAttribute(instancePath);
        });

        here.removeChildAttribute(path);

        removeDeletedAttributeInReferringAgents(cls, path);
        removeDeletedAttributeInReferringObjects(cls, path);

        return (GenericAgentClass) here.clone();

    }

    public GenericAgentClass removeAttributeList(GenericAgentClass owner, String listName) throws GSimDefException {

        GenericAgentClass here = this.findGenericAgentClass(owner);

        here.removeDeclaredAttributeList(listName);

        container.getInstancesOfClass(here, GenericAgent.class).parallelStream().forEach(a -> {
            a.setFrame(here);
            a.removeDeclaredAttributeList(listName);
        });


        container.getAgentSubClasses(here).parallelStream().forEach(a -> {
            a.replaceAncestor(here);
            a.removeDeclaredAttributeList(listName);
            for (GenericAgent member : container.getInstancesOfClass(here, GenericAgent.class)) {
                member.setFrame(a);
                member.removeDeclaredAttributeList(listName);
            }
        });

        return (GenericAgentClass) here.clone();

    }

    public GenericAgentClass removeChildFrame(GenericAgentClass cls, Path<Frame> path) {

        GenericAgentClass here = this.findGenericAgentClass(cls);
        here.removeChildFrame(path);
        container.getInstancesOfClass(here, GenericAgent.class).forEach(member -> {
            member.setFrame(here);
            member.removeChildInstance(Path.<Instance> objectPath(path.toStringArray()));
        });
        container.getAgentSubClasses(here).forEach(sub -> {
            sub.replaceAncestor(here);
            sub.removeChildFrame(path);
            for (GenericAgent member : container.getInstancesOfClass(sub, GenericAgent.class)) {
                member.setFrame(sub);
                member.removeChildInstance(Path.<Instance> objectPath(path.toStringArray()));
            }
        });

        return (GenericAgentClass) here.clone();
    }

    private GenericAgentClass findGenericAgentClass(GenericAgentClass extern) {

        if (extern.getName().equals(GenericAgentClass.NAME)) {
            return container.getAgentClass();
        }

        return container.getAgentSubClasses().parallelStream().filter(a -> a.getName().equals(extern.getName())).findAny().get();

    }

    private void removeDeletedAttributeInReferringAgents(GenericAgentClass here, Path<DomainAttribute> path) {
        container.getAgentSubClasses().stream().filter(ac -> ac.hasDeclaredChildFrame(here.getName())).forEach(cls -> {
            for (String list : cls.getListNamesWithDeclaredChildFrame(here.getName())) {
                Path<DomainAttribute> newPath = Path.attributePath(path.toStringArray(), list, here.getName());
                removeAgentClassAttribute(cls, newPath);
            }
        });
    }

    private void removeDeletedAttributeInReferringObjects(Frame here, Path<DomainAttribute> path) {
        container.getObjectSubClasses().stream().filter(ac -> ac.hasDeclaredChildFrame(here.getName())).forEach(cls -> {
            for (String list : cls.getListNamesWithDeclaredChildFrame(here.getName())) {
                Path<DomainAttribute> newPath = Path.attributePath(path.toStringArray(), list, here.getName());
                objectClassOperations.removeObjectClassAttribute(cls, newPath);
            }
        });
    }

    protected void removeFrameInReferringAgents(Frame removed) {
        container.getAgentSubClasses().stream().filter(a -> a.hasDeclaredChildFrame(removed.getName())).forEach(a -> {
            for (String list : a.getListNamesWithDeclaredChildFrame(removed.getName())) {
                Path<Frame> path = Path.objectPath(list, removed.getName());
                this.removeChildFrame(a, path);

            }
        });
    }

    public void addChildAttributeInReferringAgents(Frame here, Path<List<DomainAttribute>> path, DomainAttribute added) {
        for (GenericAgentClass agentClass : getAgentSubClasses()) {
            for (String listname : agentClass.getDeclaredFrameListNames()) {
                for (Frame f : agentClass.getChildFrames(listname)) {
                    if (f.isSuccessor(here.getName())) {
                        Path<List<DomainAttribute>> newPath = Path.attributePath(listname, f.getName(), path.toStringArray());
                        addAgentClassAttribute(agentClass, newPath, added);
                    }
                }
            }
        }
    }

    Set<GenericAgentClass> getGenericAgentSubClassesRef() {
        return container.getAgentSubClasses();
    }

    void setAgentClass(GenericAgentClass c) {
        container.setAgentClass(c);
    }

}
