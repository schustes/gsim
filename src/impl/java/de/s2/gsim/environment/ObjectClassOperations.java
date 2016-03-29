package de.s2.gsim.environment;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import de.s2.gsim.def.EntityConstants;
import de.s2.gsim.def.GSimDefException;
import de.s2.gsim.def.TimeOrderedSet;
import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;
import de.s2.gsim.def.objects.UnitOLD;
import de.s2.gsim.def.objects.UnitUtils;
import de.s2.gsim.def.objects.agent.GenericAgent;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.util.Utils;

public class ObjectClassOperations {

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

        return (FrameOLD) here.clone();

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

    protected FrameOLD findObjectClass(String extern) {
        Iterator iter = objectSubClasses.listIterator();
        while (iter.hasNext()) {
            FrameOLD cls = (FrameOLD) iter.next();
            if (cls.getTypeName().equals(extern)) {
                return cls;
            }
        }
        return null;
    }
    protected void removeFrameInReferringObjectClasses(FrameOLD removed, String[] path) {
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
    

    TimeOrderedSet getObjectSubClassesRef() {
        return objectSubClasses;
    }

    void setObjectClass(FrameOLD c) {
        objectClass = c;
    }

}
