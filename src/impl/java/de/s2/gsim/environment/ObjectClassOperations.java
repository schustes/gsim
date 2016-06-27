package de.s2.gsim.environment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class ObjectClassOperations {

    private EntitiesContainer container;
    private AgentClassOperations agentClassOperations;
    private ObjectInstanceOperations objectInstanceOperations;

    ObjectClassOperations(EntitiesContainer container, AgentClassOperations agentClassOperations,
            ObjectInstanceOperations objectInstanceOperations) {
        this.container = container;
        this.objectInstanceOperations = objectInstanceOperations;
        this.agentClassOperations = agentClassOperations;
    }

    public Frame addObjectClassAttribute(Frame cls, Path<List<DomainAttribute>> path, DomainAttribute newValue) {

        Frame here = findObjectClass(cls);
        Set<Frame> objectSubClasses = container.getObjectSubClasses();

        here.addChildAttribute(path, newValue);
        here.setDirty(true);

        objectSubClasses.stream().filter(f -> f.isSuccessor(cls.getName())).forEach(f -> f.replaceAncestor(here));

        this.addChildAttributeInReferringObjects(cls, path, newValue);
        agentClassOperations.addChildAttributeInReferringAgents(cls, path, newValue);

        return (Frame) here.clone();
    }

    public void addObjectSubClass(Frame cls) {
        container.addObjectClass(cls.clone());
    }

    public Frame createObjectSubClass(String name, Frame parent) {

        Frame p;
        if (parent == null) {
            p = Frame.inherit(Arrays.asList(container.getObjectClass()), name, Optional.empty());
        } else {
            p = Frame.inherit(Arrays.asList(this.findObjectClass(parent)), name, Optional.empty());
        }
        container.addObjectClass(p);
        return (Frame) p.clone();

    }

    public Frame addChildFrame(Frame cls, Path<TypedList<Frame>> path, Frame frameToAdd) {
        Frame here = findObjectClass(cls);

        here.addChildFrame(path, frameToAdd);

        for (Frame successor : container.getObjectSubClasses(here)) {
            successor.replaceAncestor(here);
        }

        for (Instance member : container.getInstancesOfClass(here, Instance.class)) {
            Instance newInstance = new Instance(frameToAdd.getName(), frameToAdd);
            Path<TypedList<Instance>> instPath = Path.objectListPath(path.toStringArray());
            member.addChildInstance(instPath, newInstance);
        }

        addChildFrameInReferringObjects(cls, path, frameToAdd);

        return (Frame) here.clone();

    }

    public Frame getObjectClass() {
        return container.getObjectClass().clone();
    }

    public Frame getObjectClassRef() {
        return container.getObjectClass();
    }

    public Frame getObjectSubClass(String objectName) {
        return container.getObjectSubClasses().parallelStream().filter(o -> o.getName().equals(objectName)).findAny().get();
    }

    public List<Frame> getObjectSubClasses() {
        return container.getObjectSubClasses().parallelStream().map(Frame::clone).collect(Collectors.toList());
    }

    public Frame modifyObjectClassAttribute(Frame cls, Path<DomainAttribute> path, DomainAttribute a) {
        Frame here = findObjectClass(cls);

        here.replaceChildAttribute(path, a.clone());
        for (Frame successor : container.getObjectSubClasses(here)) {
            successor.replaceAncestor(here);
            successor.replaceChildAttribute(path, a.clone());
        }

        return (Frame) here.clone();
    }

    public Frame removeAttributeList(Frame owner, String listName) throws GSimDefException {
        Frame here = findObjectClass(owner);

        here.removeDeclaredAttributeList(listName);
        container.getInstancesOfClass(owner, Instance.class).parallelStream().forEach((inst) -> {
            inst.setFrame(here);
            inst.removeDeclaredAttributeList(listName);
        });

        container.getObjectSubClasses(here).parallelStream().forEach(succ -> {
            succ.replaceAncestor(here);
            succ.removeDeclaredAttributeList(listName);
            for (Instance member : container.getInstancesOfClass(succ, Instance.class)) {
                member.setFrame(succ);
                member.removeDeclaredAttributeList(listName);
            }
        });

        return (Frame) here.clone();

    }

    public Frame removeChildFrame(Frame cls, Path<Frame> path) {
        Frame here = findObjectClass(cls);
        here.removeChildFrame(path);

        container.getObjectSubClasses(here).parallelStream().forEach(sub -> {
            sub.removeChildFrame(path);
            Path<TypedList<Instance>> instList = Path.objectListPath(Path.withoutLastAttributeOrObject(path, Path.Type.LIST).toStringArray());
            for (Instance member : container.getInstancesOfClass(sub, Instance.class)) {
                TypedList<Instance> list = member.resolvePath(instList);
                if (list != null) {
                    list.clear();
                }
            }
        });

        return (Frame) here.clone();
    }

    public void removeObjectClass(Frame cls) {

        Frame here = findObjectClass(cls);
        Iterator<Frame> iter = container.getObjectSubClasses(here).iterator();
        while (iter.hasNext()) {
            iter.remove();
        }

        container.getObjectSubClasses().remove(here);
        agentClassOperations.removeFrameInReferringAgents(here);

    }

    public GenericAgentClass removeObjectClassAttribute(Frame cls, Path<DomainAttribute> path) {
        Frame here = this.findObjectClass(cls);
        container.getObjectSubClasses(here).parallelStream().forEach(objectClass -> {
            objectClass.replaceAncestor(here);
            container.getInstancesOfClass(objectClass, Instance.class).forEach(inst -> {
                inst.setFrame(objectClass);
            });
        });

        container.getInstancesOfClass(here, Instance.class).forEach(member -> {
            Path<Attribute> instancePath = Path.attributePath(path.toStringArray());
            member.removeChildAttribute(instancePath);
        });

        here.removeChildAttribute(path);

        removeDeletedAttributeInReferringObjects(here, path);
        removeDeletedAttributeInReferringAgents(here, path);

        return (GenericAgentClass) here.clone();

    }

    private void removeDeletedAttributeInReferringAgents(Frame here, Path<DomainAttribute> path) {
        container.modifyChildFrame((cls, attributePath) -> {
                agentClassOperations.removeAgentClassAttribute((GenericAgentClass) cls, attributePath);
        }, here, path);
    }

    private void removeDeletedAttributeInReferringObjects(Frame here, Path<DomainAttribute> path) {
        container.modifyChildFrame((cls, attributePath) -> {
            removeObjectClassAttribute(cls, path);
        }, here, path);
    }

    private Frame findObjectClass(Frame extern) {

        if (extern.getName().equals(container.getObjectClass().getName())) {
            return container.getObjectClass();
        }

        return container.getObjectSubClasses().parallelStream().filter(a -> a.getName().equals(extern.getName())).findAny().get();

    }

    protected void removeFrameInReferringObjectClasses(Frame removed) {
        Frame[] objects = getObjectSubClasses();
        for (int i = 0; i < objects.length; i++) {
            Frame c = objects[i];
            String[] s = c.getDeclaredFrameListNames();
            for (int j = 0; j < s.length; j++) {
                Frame[] ff = c.getChildFrames(s[j]);
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

    private void addChildAttributeInReferringObjectsOld(Frame here, Path<DomainAttribute> path, DomainAttribute added) {


        container.getObjectSubClasses().stream().flatMap(frame -> frame.replaceAncestor().stream());

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
                        addObjectClassAttribute(c, newPath, added);
                    }
                }
            }
        }
    }

    public void addChildAttributeInReferringObjects(Frame here, Path<List<DomainAttribute>> path, DomainAttribute added) {
        for (Frame objectClass : getObjectSubClasses()) {
            for (String listname : objectClass.getDeclaredFrameListNames()) {
                for (Frame f : objectClass.getChildFrames(listname)) {
                    if (f.isSuccessor(here.getName())) {
                        Path<DomainAttribute> newPath = Path.attributePath(listname, f.getName(), path.toStringArray());
                        addObjectClassAttribute(objectClass, newPath, added);
                    }
                }
            }
        }
    }

    // when a containing object of a frame was added or in any way modified
    private void addChildFrameInReferringObjects(Frame here, Path<TypedList<Frame>> path, Frame added) {
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
                        addChildFrame(c, newPath, added);
                    }
                }
            }
        }
    }


    public Frame[] getAllObjectSuccessors(String x) {
        ListIterator iter = objectSubClasses.listIterator();
        HashSet<Frame> set = new HashSet<Frame>();
        while (iter.hasNext()) {
            Frame c = (Frame) iter.next();
            if (c.isSuccessor(x)) {
                set.add(c);
            }
        }
        Frame[] res = new Frame[set.size()];
        set.toArray(res);
        return res;
    }


    TimeOrderedSet getObjectSubClassesRef() {
        return objectSubClasses;
    }

    void setObjectClass(Frame c) {
        objectClass = c;
    }

}
