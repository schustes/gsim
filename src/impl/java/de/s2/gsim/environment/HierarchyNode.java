package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Is a tree for holding a hierarchy of class Frame.
 *
 */
public class HierarchyNode implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private HashMap children = new HashMap();

    private Frame object;

    public HierarchyNode(Frame rootFrame) {
        this.object = rootFrame;
    }

    public void addChild(HierarchyNode f) {
        if (!children.containsKey(f.getFrame().getTypeName())) {
            children.put(f.getFrame().getTypeName(), f);
        }
    }

    /**
     * returns an ind-depth enumeration of successors.
     */
    public Iterator getAllChildren() {
        Iterator iter = children.values().iterator();
        ArrayList list = new ArrayList();
        while (iter.hasNext()) {
            HierarchyNode h = (HierarchyNode) iter.next();
            h.getAllChildrenRek(list);
        }
        return list.iterator();
    }

    public HierarchyNode getChild(String name) {
        HierarchyNode o = (HierarchyNode) children.get(name);
        if (o == null) {
            Iterator iter = getChildren();
            while (iter.hasNext()) {
                HierarchyNode child = (HierarchyNode) iter.next();
                HierarchyNode h = child.getChild(name);
                if (h != null) {
                    return h;
                }
            }
        } else {
            return o;
        }
        return null;
    }

    /**
     * returns successor at the next level
     */
    public Iterator getChildren() {
        return children.values().iterator();
    }

    public Iterator getChildrenNames() {
        return children.keySet().iterator();
    }

    public Frame getFrame() {
        return object;
    }

    public boolean hasChildren() {
        return children.size() > 0;
    }

    public boolean insert(Frame f) {
        Frame[] pf = f.getParentFrames();

        for (int i = 0; i < pf.length; i++) {
            String s = pf[i].getTypeName();
            this.insert(pf[i]);
            this.insert(s, f);
        }

        return true;
    }

    public boolean insert(String parent, Frame f) {

        Frame p = object;

        if (p.getTypeName().equals(parent)) {
            HierarchyNode h = new HierarchyNode(f);
            addChild(h);
            return true;
        }

        Iterator iter = getChildren();
        while (iter.hasNext()) {
            HierarchyNode h2 = (HierarchyNode) iter.next();
            h2.insert(parent, f);
        }

        return false;

    }

    private void getAllChildrenRek(ArrayList list) {
        list.add(this);
        Iterator iter = children.values().iterator();
        while (iter.hasNext()) {
            HierarchyNode h = (HierarchyNode) iter.next();
            h.getAllChildrenRek(list);
        }
    }

}
