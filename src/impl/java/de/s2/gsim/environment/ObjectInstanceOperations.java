package de.s2.gsim.environment;

import java.util.Iterator;
import java.util.ListIterator;

import de.s2.gsim.def.GSimDefException;
import de.s2.gsim.def.objects.FrameOLD;
import de.s2.gsim.def.objects.InstanceOLD;
import de.s2.gsim.def.objects.UnitUtils;
import de.s2.gsim.def.objects.agent.GenericAgent;
import de.s2.gsim.objects.attribute.Attribute;

public class ObjectInstanceOperations {

	
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

    public InstanceOLD modifyObjectAttribute(InstanceOLD inst, String[] path, Attribute att) {
        InstanceOLD here = findObject(inst.getName());
        UnitUtils.getInstance().setChildAttribute(here, path, att);
        here.setDirty(true);
        objects.add(here);
        return (InstanceOLD) here.clone();
    }

    // delete first all in removed-list from db

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


}
