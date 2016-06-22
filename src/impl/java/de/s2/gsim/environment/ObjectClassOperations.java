package de.s2.gsim.environment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.util.Utils;

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
    
    public Frame modifyObjectClassAttribute(Frame cls, String[] path, DomainAttribute a) {
        Frame here = findObjectClass(cls);

        UnitOperations.setChildAttribute(here, Utils.removeFromArray(path, a.getName()), a);
        here.setDirty(true);

        objectSubClasses.add(here);

        ListIterator<Frame> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            Frame c = iter.next();
            c.setDirty(true);
            if (c.isSuccessor(cls.getTypeName())) {
                c.replaceAncestor(here);
                UnitOperations.setChildAttribute(c, Utils.removeFromArray(path, a.getName()), (DomainAttribute) a.clone());
                iter.set(c);
            }
        }
        return (Frame) here.clone();
    }

    public Frame removeAttributeList(Frame owner, String listName) throws GSimDefException {
        Frame here = findObjectClass(owner);

        here.removeDeclaredAttributeList(listName);
        objectSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            Instance c = (Instance) members.next();
            c.setFrame(here);
            c.removeDeclaredAttributeList(listName);
            agents.add((GenericAgent) c);
        }

        ListIterator<Frame> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            Frame c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.replaceAncestor(here);
                c.removeDeclaredAttributeList(listName);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    Instance cc = (Instance) members.next();
                    cc.setFrame(c);
                    cc.removeDeclaredAttributeList(listName);
                    agents.add((GenericAgent) cc);
                }
            }
        }

        return (Frame) here.clone();

    }

    public Frame removeChildFrame(Frame cls, String[] path, Frame f) {
        Frame here = findObjectClass(cls);
        UnitOperations.removeChildFrame(here, path, f.getTypeName());
        here.setDirty(true);
        objectSubClasses.add(here);

        Frame[] c = getAllObjectSuccessors(cls.getTypeName());
        for (int i = 0; i < c.length; i++) {
            UnitOperations.removeChildFrame(c[i], path, f.getTypeName());
        }

        Iterator iter = getInstancesOfClass(f).iterator();
        while (iter.hasNext()) {
            Instance inst = (Instance) iter.next();

            Instance[] list = (Instance[]) inst.resolveName(path);
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    if (list[i].inheritsFrom(f)) {
                        UnitOperations.removeChildInstance(inst, path, list[i].getName());
                    }
                }
            }
        }
        // this.removeFrameInReferringObjects(f, path);
        // this.removeFrameInReferringAgents(f, path);

        return (Frame) here.clone();
    }
    
    public void removeObjectClass(Frame cls) {
        ListIterator<Frame> iter = null;
        Frame here = findObjectClass(cls);

        iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            Frame c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                removed.add(c);
                iter.remove();
            }
        }

        objectSubClasses.remove(here);

        removeFrameInReferringAgents(cls, new String[0]);

    }

    // implement analogously to AgentClassOperations
    public Frame removeObjectClassAttribute(Frame cls, Path<DomainAttribute> path) {
        Frame here = findObjectClass(cls);

        UnitOperations.removeAttribute(here, path, a);
        here.setDirty(true);

        ListIterator<Frame> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            Frame c = iter.next();
            if (c.isSuccessor(cls.getTypeName())) {
                c.setDirty(true);
                UnitOperations.removeAttribute(c, path, a);
                c.replaceAncestor(here);
                iter.set(c);
            }
        }

        ListIterator<Instance> iter2 = objects.listIterator();
        while (iter2.hasNext()) {
            Instance c = iter2.next();
            if (c.inheritsFrom(cls)) {
                c.setDirty(true);
                UnitOperations.removeAttribute(c, path, a);
                c.setFrame(here);
                iter2.set(c);
            }
        }

        objectSubClasses.add(here);

        removeDeletedAttributeInReferringObjects(here, path, a);
        removeDeletedAttributeInReferringAgents(here, path, a);

        return (Frame) here.clone();

    }
    

    protected Frame findObjectClass(Frame extern) {
        Iterator iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            Frame cls = (Frame) iter.next();
            if (cls.getTypeName().equals(extern.getTypeName())) {
                return cls;
            }
        }
        if (extern.getTypeName().equals(objectClass.getTypeName())) {
            return objectClass;
        }
        return null;
    }

    protected Frame findObjectClass(String extern) {
        Iterator iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            Frame cls = (Frame) iter.next();
            if (cls.getTypeName().equals(extern)) {
                return cls;
            }
        }
        return null;
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
