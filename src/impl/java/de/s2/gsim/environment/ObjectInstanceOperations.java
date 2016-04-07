package de.s2.gsim.environment;

import java.util.Iterator;
import java.util.ListIterator;

import de.s2.gsim.objects.attribute.Attribute;

public class ObjectInstanceOperations {

	
    protected Instance findObject(String extern) {
        Iterator iter = objects.iterator();
        while (iter.hasNext()) {
            Instance cls = (Instance) iter.next();
            if (cls.getName().equals(extern)) {
                return cls;
            }
        }
        return null;
    }

    public Instance modifyObjectAttribute(Instance inst, String[] path, Attribute att) {
        Instance here = findObject(inst.getName());
        UnitOperations.setChildAttribute(here, path, att);
        here.setDirty(true);
        objects.add(here);
        return (Instance) here.clone();
    }

    // delete first all in removed-list from db

    public void removeObject(Instance object) {
        Iterator iter = objects.iterator();
        while (iter.hasNext()) {
            Instance a = (Instance) iter.next();
            if (a.getName().equals(object.getName())) {
                iter.remove();
                removed.add(a);
            }
        }
    }

    public Frame addAttributeList(Frame owner, String listName) throws GSimDefException {
        Frame here = findObjectClass(owner);

        here.defineAttributeList(listName);
        objectSubClasses.add(here);

        Iterator members = getInstancesOfClass(here).iterator();
        while (members.hasNext()) {
            Instance c = (Instance) members.next();
            c.setFrame(here);
            c.defineAttributeList(listName);
            agents.add((GenericAgent) c);
        }

        ListIterator<Frame> iter = objectSubClasses.listIterator();

        while (iter.hasNext()) {
            Frame c = iter.next();
            if (c.isSuccessor(here.getTypeName())) {
                c.replaceAncestor(here);
                iter.set(c);
                members = getInstancesOfClass(c).iterator();
                while (members.hasNext()) {
                    Instance cc = (Instance) members.next();
                    cc.setFrame(c);
                    c.defineAttributeList(listName);
                    agents.add((GenericAgent) cc);
                }
            }
        }

        return (Frame) here.clone();

    }


}
