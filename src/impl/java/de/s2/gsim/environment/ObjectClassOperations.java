package de.s2.gsim.environment;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import de.s2.gsim.def.EntityConstants;
import de.s2.gsim.def.GSimDefException;
import de.s2.gsim.def.TimeOrderedSet;
import de.s2.gsim.def.objects.Frame;
import de.s2.gsim.def.objects.Instance;
import de.s2.gsim.def.objects.Unit;
import de.s2.gsim.def.objects.UnitUtils;
import de.s2.gsim.def.objects.agent.GenericAgent;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.util.Utils;

public class ObjectClassOperations {

    public Frame addObjectClassAttribute(Frame cls, String[] path, DomainAttribute a) {
        Frame here = findObjectClass(cls);

        UnitUtils.getInstance().setChildAttribute(here, Utils.removeFromArray(path, a.getName()), a);
        here.setDirty(true);

        ListIterator<Frame> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            Frame c = iter.next();
            if (c.isSuccessor(cls.getTypeName())) {
                c.setAncestor(here);
                iter.set(c);
            }
        }

        objectSubClasses.add(here);

        addChildAttributeInReferringObjects(cls, path, a);
        addChildAttributeInReferringAgents(cls, path, a);
        return (Frame) here.clone();
    }

    public void addObjectSubClass(Frame cls) {
        objectSubClasses.add(cls);
    }

    public Frame createObjectSubClass(String name, Frame parent) {
        if (parent == null) {
            Frame p = new Frame(name, EntityConstants.TYPE_OBJECT);
            p.setSystem(false);
            objectSubClasses.add(p);
            return (Frame) p.clone();
        } else {
            Frame p = new Frame(new Frame[] { parent }, name, EntityConstants.TYPE_OBJECT);
            p.setSystem(false);
            objectSubClasses.add(p);
            return (Frame) p.clone();
        }
    }

    public Frame addChildFrame(Frame cls, String[] path, Frame f) {
        Frame here = findObjectClass(cls);
        UnitUtils.getInstance().setChildFrame(here, path, f);
        here.setDirty(true);
        objectSubClasses.add(here);

        Frame[] c = getAllObjectSuccessors(cls.getTypeName());
        for (int i = 0; i < c.length; i++) {
            c[i].setAncestor(here);
            // c[i].setChildFrame(path, f);
        }
        Iterator iter = getInstancesOfClass(f).iterator();

        while (iter.hasNext()) {
            Instance inst = (Instance) iter.next();
            Instance instance = new Instance(f.getTypeName(), f);
            UnitUtils.getInstance().setChildInstance(inst, path, instance);
        }

        addChildFrameInReferringObjects(cls, path, f);
        return (Frame) here.clone();

    }
    
    public Frame[] getImmediateObjectSuccessors(String frame) {
        Frame f = findProductClass(frame);
        HashSet<Frame> successors = new HashSet<Frame>();
        Iterator iter = objectSubClasses.iterator();
        while (iter.hasNext()) {
            Frame p = (Frame) iter.next();
            Frame fr = p.getParentFrame(f.getTypeName());
            if (fr != null && fr.getTypeName().equals(frame)) {
                successors.add(p);
            }
        }
        Frame[] ff = new Frame[successors.size()];
        successors.toArray(ff);
        return ff;
    }

    public Frame getObjectClass() {
        return objectClass;
    }

    public Frame getObjectClassRef() {
        return objectClass;
    }

    public Frame getObjectSubClass(String productName) {
        Iterator iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            Frame cls = (Frame) iter.next();
            if (cls.getTypeName().equals(productName)) {
                return (Frame) cls.clone();
            }
        }
        return null;

    }

    public Frame[] getObjectSubClasses() {
        Frame[] res = new Frame[objectSubClasses.size()];
        objectSubClasses.toArray(res);
        return res;
    }
    
    public Frame modifyObjectClassAttribute(Frame cls, String[] path, DomainAttribute a) {
        Frame here = findObjectClass(cls);

        UnitUtils.getInstance().setChildAttribute(here, Utils.removeFromArray(path, a.getName()), a);
        here.setDirty(true);

        objectSubClasses.add(here);

        ListIterator<Frame> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            Frame c = iter.next();
            c.setDirty(true);
            if (c.isSuccessor(cls.getTypeName())) {
                c.setAncestor(here);
                UnitUtils.getInstance().setChildAttribute(c, Utils.removeFromArray(path, a.getName()), (DomainAttribute) a.clone());
                iter.set(c);
            }
        }
        return (Frame) here.clone();
    }

    public Frame removeAttributeList(Frame owner, String listName) throws GSimDefException {
        Frame here = findObjectClass(owner);

        here.removeAttributeList(listName);
        objectSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            Instance c = (Instance) members.next();
            c.setFrame(here);
            c.removeAttributeList(listName);
            agents.add((GenericAgent) c);
        }

        ListIterator<Frame> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            Frame c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.setAncestor(here);
                c.removeAttributeList(listName);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    Instance cc = (Instance) members.next();
                    cc.setFrame(c);
                    cc.removeAttributeList(listName);
                    agents.add((GenericAgent) cc);
                }
            }
        }

        return (Frame) here.clone();

    }

    public Frame removeChildFrame(Frame cls, String[] path, Frame f) {
        Frame here = findObjectClass(cls);
        UnitUtils.getInstance().removeChildFrame(here, path, f.getTypeName());
        here.setDirty(true);
        objectSubClasses.add(here);

        Frame[] c = getAllObjectSuccessors(cls.getTypeName());
        for (int i = 0; i < c.length; i++) {
            UnitUtils.getInstance().removeChildFrame(c[i], path, f.getTypeName());
        }

        Iterator iter = getInstancesOfClass(f).iterator();
        while (iter.hasNext()) {
            Instance inst = (Instance) iter.next();

            Instance[] list = (Instance[]) inst.resolveName(path);
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

    public Frame removeObjectClassAttribute(Frame cls, String[] path, String a) {
        Frame here = findObjectClass(cls);

        UnitUtils.getInstance().removeAttribute(here, path, a);
        here.setDirty(true);

        ListIterator<Frame> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            Frame c = iter.next();
            if (c.isSuccessor(cls.getTypeName())) {
                c.setDirty(true);
                UnitUtils.getInstance().removeAttribute(c, path, a);
                c.setAncestor(here);
                iter.set(c);
            }
        }

        ListIterator<Instance> iter2 = objects.listIterator();
        while (iter2.hasNext()) {
            Instance c = iter2.next();
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
    protected void removeFrameInReferringObjectClasses(Frame removed, String[] path) {
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
    
    private void addChildAttributeInReferringObjects(Frame here, String[] path, DomainAttribute added) {
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

    // when a containing object of a frame was added or in any way modified
    private void addChildFrameInReferringObjects(Frame here, String[] path, Frame added) {
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
