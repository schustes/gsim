package de.s2.gsim.def.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import de.s2.gsim.objects.attribute.DomainAttribute;

public class FrameOLD extends UnitOLD implements java.io.Serializable {

    static final long serialVersionUID = -651893385550742382L;

    private static Logger logger = Logger.getLogger(FrameOLD.class);

    protected String category = "";

    protected HashMap parents = new HashMap();

    protected String typeName = null;

    /**
     * Copy constructor.
     */
    public FrameOLD(FrameOLD f) {
        this(f.getParentFrames(), f.getTypeName(), f.getCategory());

        setMutable(f.isMutable());
        setSystem(f.isSystem());

        String[] lists = f.getDeclaredAttributesListNames();
        for (int i = 0; i < lists.length; i++) {
            attributeLists.put(lists[i], new ArrayList());
            DomainAttribute[] da = f.getDeclaredAttributes(lists[i]);
            for (int j = 0; j < da.length; j++) {
                addOrSetAttribute(lists[i], (DomainAttribute) da[j].clone());
            }
        }
        String[] children = f.getDeclaredFrameListNames();
        for (int i = 0; i < children.length; i++) {
            TypedListOLD list = (TypedListOLD) f.objectLists.get(children[i]);
            FrameOLD fr = list.getType();
            objectLists.put(children[i], new TypedListOLD(fr));
            FrameOLD[] ch = f.getDeclaredChildFrames(children[i]);
            for (int j = 0; j < ch.length; j++) {
                FrameOLD child = (FrameOLD) ch[j].clone();
                addChildFrame(children[i], child);
            }
        }

        isSystem = f.isSystem();
        isMutable = f.isMutable();
        isDirty = true;

    }

    /**
     * "Inhertiance" constructor. Trying to set parents by ref, in order to save memory.
     */
    public FrameOLD(FrameOLD[] parents, String typeName, String category) {
        this.typeName = typeName;
        this.category = category;
        // this.id = newId;
        for (int i = 0; i < parents.length; i++) {
            // Frame f = (Frame)parents[i].clone();

            FrameOLD f = parents[i];

            String s = f.getTypeName();
            this.parents.put(s, f);
            isSystem = parents[i].isSystem();
            isMutable = parents[i].isMutable();
            isDirty = true;
        }

    }

    /**
     * Copy constructor.
     */
    public FrameOLD(String newName, FrameOLD f) {
        this(f);
        typeName = newName;
    }

    /**
     * Construct a top-level frame without any attributes and children.
     * 
     * @param name
     *            String
     * @param category
     *            String
     * @param uniqueId
     *            int
     */
    public FrameOLD(String name, String category) {
        typeName = name;
        this.category = category;
        // this.id = uniqueId;
        isDirty = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#addOrSetAttribute(java.lang.String, gsim.def.objects.attribute.DomainAttribute)
     */

    public void addChildFrame(String listName, FrameOLD a) {

        if (a == null) {
            return;
        }

        TypedListOLD list = (TypedListOLD) objectLists.get(listName);

        if (list == null) {
            // try to clone parent-list first:
            FrameOLD type = getListType(listName);
            if (type != null) {
                list = new TypedListOLD((FrameOLD) type.clone());
            } else {
                list = new TypedListOLD((FrameOLD) a.clone());
            }
        }

        if (list.contains(a)) {
            list.remove(a);
            a = (FrameOLD) a.clone();
            list.add(a);
        } else {
            list.add((UnitOLD) a.clone());
        }
        objectLists.put(listName, list);
        isDirty = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#addChildFrame(java.lang.String, gsim.def.objects.Frame)
     */

    @SuppressWarnings("unchecked")
    public void addOrSetAttribute(String listName, DomainAttribute a) {
        ArrayList list = (ArrayList) attributeLists.get(listName); // searches in
                                                                   // current
        // layer

        if (list == null) {
            list = new ArrayList();
        }

        if (!list.contains(a)) {
            list.add(a.clone());
        } else {
            list.remove(a); // equals is overriden
            list.add(a.clone());
        }
        attributeLists.put(listName, list);
        isDirty = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#clone()
     */

    @Override
    public FrameOLD clone() {

        FrameOLD f = new FrameOLD(getTypeName(), getCategory());

        f.setMutable(isMutable());
        f.setSystem(isSystem());

        FrameOLD[] parents = getParentFrames();
        for (int i = 0; i < parents.length; i++) {
            FrameOLD fr = parents[i];
            String s = fr.getTypeName();
            f.parents.put(s, fr);
            // f.parents.put(s, (Frame) fr.clone());
        }

        String[] lists = getDeclaredAttributesListNames();
        for (int i = 0; i < lists.length; i++) {
            f.attributeLists.put(lists[i], new ArrayList());
            DomainAttribute[] da = getAttributes(lists[i]);
            for (int j = 0; j < da.length; j++) {
                try {
                    f.addOrSetAttribute(lists[i], (DomainAttribute) da[j].clone());
                } catch (Exception e) {
                    logger.debug(da[j].getName());
                    e.printStackTrace();
                }
            }
        }
        String[] children = getDeclaredFrameListNames();
        for (int i = 0; i < children.length; i++) {
            TypedListOLD list = getList(children[i]);// (TypedList)f.objectLists.get(children[i]);

            if (list != null) {
                FrameOLD fr = list.getType();
                f.objectLists.put(children[i], new TypedListOLD(fr));
            }

            FrameOLD[] ch = getChildFrames(children[i]);
            for (int j = 0; j < ch.length; j++) {
                FrameOLD child = (FrameOLD) ch[j].clone();
                f.addChildFrame(children[i], child);
            }
        }

        f.setDirty(true);
        return f;

    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#containsAttribute(java.lang.String, java.lang.String)
     */

    public boolean containsAttribute(String listName, String attributeName) {
        ArrayList list = (ArrayList) attributeLists.get(listName);
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                DomainAttribute att = (DomainAttribute) iter.next();
                if (att.getName().equals(attributeName)) {
                    return true;
                }
            }
        } else {
            FrameOLD[] fs = getAncestors();
            for (int i = 0; i < fs.length; i++) {
                FrameOLD f = fs[i];
                if (f.containsAttribute(listName, attributeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#containsChildFrame(java.lang.String, gsim.def.objects.Frame)
     */

    public boolean containsChildFrame(String listName, FrameOLD f) {
        FrameOLD[] children = getChildFrames(listName);

        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].getCategory().equals(f.getCategory())) {
                    if (children[i].getTypeName().startsWith("{")) {
                        return true;
                    }
                    if (children[i].getTypeName().equals(f.getTypeName())) {
                        return true;
                    }
                }
            }
        }

        FrameOLD[] parents = getAncestors();
        for (int i = 0; i < parents.length; i++) {
            boolean b = parents[i].containsChildFrame(listName, f);
            if (b) {
                return b;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#definesChildList(java.lang.String)
     */

    public boolean definesChildList(String listName) {

        for (String s : getChildFrameListNames()) {
            if (s.equals(listName)) {
                return true;
            }
        }

        FrameOLD[] parents = getAncestors();
        for (int i = 0; i < parents.length; i++) {
            boolean b = parents[i].definesChildList(listName);
            if (b) {
                return b;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FrameOLD)) {
            return false;
        }

        if (getTypeName().equals(((FrameOLD) o).getTypeName())) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getListType(java.lang.String)
     */

    public FrameOLD getAncestor(String typeName) {
        if (parents.containsKey(typeName)) {
            return (FrameOLD) parents.get(typeName);
        } else {
            FrameOLD[] fs = getAncestors();
            for (int i = 0; i < fs.length; i++) {
                FrameOLD f = fs[i].getParentFrame(typeName);
                if (f != null) {
                    return f;
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#equals(java.lang.Object)
     */

    public FrameOLD[] getAncestors() {
        ArrayList list = new ArrayList();
        Iterator iter = parents.keySet().iterator();
        while (iter.hasNext()) {
            String s = (String) iter.next();
            FrameOLD f = (FrameOLD) parents.get(s);
            list.add(f);
            FrameOLD[] fs = f.getAncestors();
            for (int i = 0; i < fs.length; i++) {
                list.add(fs[i]);
            }
        }
        FrameOLD[] all = new FrameOLD[list.size()];
        list.toArray(all);
        return all;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getAncestor(java.lang.String)
     */

    public DomainAttribute getAttribute(String attrName) {
        String[] lists = getAttributesListNames();
        for (int i = 0; i < lists.length; i++) {
            String list = lists[i];
            DomainAttribute da = this.getAttribute(list, attrName);
            if (da != null) {
                return da;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getAncestors()
     */

    public DomainAttribute getAttribute(String listname, String attrName) {
        ArrayList list = (ArrayList) attributeLists.get(listname);
        if (list != null) {

            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                DomainAttribute attr = (DomainAttribute) iter.next();
                if (attr.getName().equals(attrName)) {
                    return (DomainAttribute) attr.clone();
                }
            }
        }
        FrameOLD[] fs = getAncestors();
        for (int i = 0; i < fs.length; i++) {
            DomainAttribute da = fs[i].getAttribute(listname, attrName);
            if (da != null) {
                return (DomainAttribute) da.clone();
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getAttribute(java.lang.String)
     */

    public DomainAttribute[] getAttributes(String listname) {
        ArrayList list = (ArrayList) attributeLists.get(listname);
        HashSet list2 = new HashSet();
        if (list != null) {
            list2.addAll(list);
        }

        FrameOLD[] fs = getAncestors();
        for (int i = 0; i < fs.length; i++) {
            DomainAttribute[] da = fs[i].getAttributes(listname);
            if (da != null) {
                for (int j = 0; j < da.length; j++) {
                    list2.add(da[j]);
                }
            }
        }
        DomainAttribute[] res = new DomainAttribute[list2.size()];
        list2.toArray(res);
        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getAttribute(java.lang.String, java.lang.String)
     */

    public String[] getAttributesListNames() {
        Iterator iter = attributeLists.keySet().iterator();
        ArrayList list = new ArrayList();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            list.add(key);
        }

        FrameOLD[] fs = getAncestors();
        for (int i = 0; i < fs.length; i++) {
            FrameOLD f = fs[i];
            String[] s = f.getAttributesListNames();
            for (int j = 0; j < s.length; j++) {
                if (!list.contains(s[j])) {
                    list.add(s[j]);
                }
            }
        }

        String[] keys = new String[list.size()];
        list.toArray(keys);
        return keys;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getAttributes(java.lang.String)
     */

    public String getCategory() {
        return category;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getAttributesListNames()
     */

    public FrameOLD getChildFrame(String listname, String frameName) {
        ArrayList list = (ArrayList) objectLists.get(listname);
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                FrameOLD attr = (FrameOLD) iter.next();
                if (attr.getTypeName().equals(frameName)) {
                    return (FrameOLD) attr.clone();
                }
            }
        } else {
            FrameOLD[] fs = getAncestors();
            for (int i = 0; i < fs.length; i++) {
                FrameOLD da = fs[i].getChildFrame(listname, frameName);
                if (da != null) {
                    return (FrameOLD) da.clone();
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getCategory()
     */

    public String[] getChildFrameListNames() {
        Iterator iter = objectLists.keySet().iterator();
        ArrayList list = new ArrayList();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (!list.contains(key)) {
                list.add(key);
            }
        }

        FrameOLD[] parents = getAncestors();
        for (int i = 0; i < parents.length; i++) {
            String[] s = parents[i].getChildFrameListNames();
            for (int j = 0; j < s.length; j++) {
                if (!list.contains(s[j])) {
                    list.add(s[j]);
                }
            }
        }

        String[] keys = new String[list.size()];
        list.toArray(keys);
        return keys;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getChildFrame(java.lang.String, java.lang.String)
     */

    public FrameOLD[] getChildFrames(String listname) {

        ArrayList list = (ArrayList) objectLists.get(listname);
        HashSet sum = new HashSet();

        if (list != null) {
            sum.addAll(list);
        }

        FrameOLD[] fs = getAncestors();
        for (int i = 0; i < fs.length; i++) {
            FrameOLD[] da = fs[i].getChildFrames(listname);
            if (da != null) {
                for (int j = 0; j < da.length; j++) {
                    sum.add(da[j]);
                }
            }
        }

        FrameOLD[] children = new FrameOLD[sum.size()];
        sum.toArray(children);
        return children;

    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getChildFrameListNames()
     */

    public DomainAttribute getDeclaredAttribute(String listname, String attrName) {
        ArrayList list = (ArrayList) attributeLists.get(listname);
        if (list != null) {
            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                DomainAttribute attr = (DomainAttribute) iter.next();
                if (attr.getName().equals(attrName)) {
                    return attr;
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getChildFrames(java.lang.String)
     */

    public DomainAttribute[] getDeclaredAttributes(String listname) {
        ArrayList list = (ArrayList) attributeLists.get(listname);
        if (list != null) {
            DomainAttribute[] atts = new DomainAttribute[list.size()];
            list.toArray(atts);
            return atts;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getDeclaredAttribute(java.lang.String, java.lang.String)
     */

    public String[] getDeclaredAttributesListNames() {
        String[] keys = new String[attributeLists.keySet().size()];
        attributeLists.keySet().toArray(keys);
        return keys;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getDeclaredAttributes(java.lang.String)
     */

    public FrameOLD[] getDeclaredChildFrames(String listname) {
        ArrayList list = (ArrayList) objectLists.get(listname);
        if (list != null) {
            FrameOLD[] atts = new FrameOLD[list.size()];
            list.toArray(atts);
            return atts;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getDeclaredAttributesListNames()
     */

    public FrameOLD getDeclaredFrame(String listname, String attrName) {
        ArrayList list = (ArrayList) objectLists.get(listname);
        if (list != null) {
            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                FrameOLD attr = (FrameOLD) iter.next();
                if (attr.getTypeName().equals(attrName)) {
                    return attr;
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getDeclaredChildFrames(java.lang.String)
     */

    public String[] getDeclaredFrameListNames() {
        Iterator iter = objectLists.keySet().iterator();
        ArrayList list = new ArrayList();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            list.add(key);
        }
        String[] keys = new String[list.size()];
        list.toArray(keys);
        return keys;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getDeclaredFrame(java.lang.String, java.lang.String)
     */

    public TypedListOLD getList(String name) {
        TypedListOLD l = (TypedListOLD) objectLists.get(name);
        if (l == null) {
            Iterator iter = parents.values().iterator();
            while (iter.hasNext()) {
                FrameOLD p = (FrameOLD) iter.next();
                return p.getList(name);
            }
        } else {
            return l;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getDeclaredFrameListNames()
     */

    public FrameOLD getListType(String listName) {
        TypedListOLD list = getList(listName);
        if (list == null) {
            Iterator iter = parents.values().iterator();
            while (iter.hasNext()) {
                FrameOLD p = (FrameOLD) iter.next();
                return p.getListType(listName);
            }
        } else {
            return list.getType();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getParentFrame(java.lang.String)
     */

    public FrameOLD getParentFrame(String typeName) {
        if (parents.containsKey(typeName)) {
            return (FrameOLD) parents.get(typeName);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getParentFrames()
     */

    public FrameOLD[] getParentFrames() {
        Collection list = parents.values();
        FrameOLD[] all = new FrameOLD[list.size()];
        list.toArray(all);
        return all;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getTypeName()
     */

    public String getTypeName() {
        return typeName;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#isDeclaredAttribute(java.lang.String, java.lang.String)
     */

    public boolean isDeclaredAttribute(String listName, String attributeName) {
        FrameOLD[] p = getAncestors();
        for (FrameOLD f : p) {
            ArrayList list = (ArrayList) f.attributeLists.get(listName);
            if (list != null) {
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    DomainAttribute att = (DomainAttribute) iter.next();
                    if (att.getName().equals(attributeName)) {
                        return false;
                    }
                }
            }
        }
        ArrayList list = (ArrayList) attributeLists.get(listName);
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                DomainAttribute att = (DomainAttribute) iter.next();
                if (att.getName().equals(attributeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#isDeclaredAttributeList1(java.lang.String)
     */

    public boolean isDeclaredAttributeList(String listName) {
        FrameOLD[] p = getAncestors();
        for (FrameOLD f : p) {
            String[] l = f.getDeclaredAttributesListNames();
            for (String s : l) {
                if (s.equals(listName)) {
                    return false;
                }
            }
        }
        String[] l = getDeclaredAttributesListNames();
        for (String s : l) {
            if (s.equals(listName)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#isDeclaredAttributeList(java.lang.String)
     */

    public boolean isDeclaredAttributeList1(String listName) {
        String[] l = getDeclaredAttributesListNames();
        for (String s : l) {
            if (s.equals(listName)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#isSuccessor(java.lang.String)
     */

    public boolean isSuccessor(String ancestorName) {
        FrameOLD[] ancestors = getAncestors();
        for (int i = 0; i < ancestors.length; i++) {
            if (ancestors[i].getTypeName().equals(ancestorName)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#isSystem()
     */

    @Override
    public boolean isSystem() {
        return isSystem;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#removeAttribute(java.lang.String, gsim.def.objects.attribute.DomainAttribute)
     */

    public void removeAttribute(String listname, DomainAttribute a) {
        ArrayList l = (ArrayList) attributeLists.get(listname);
        if (l != null) {
            Iterator iter = l.iterator();
            while (iter.hasNext()) {
                DomainAttribute d = (DomainAttribute) iter.next();
                if (d.getName().equals(a.getName())) {
                    iter.remove();
                }
            }
        }
        isDirty = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#removeAttributeList(java.lang.String)
     */

    @Override
    public void removeAttributeList(String listname) {
        String[] lists = getDeclaredAttributesListNames();
        for (int i = 0; i < lists.length; i++) {
            if (lists[i].equals(listname)) {
                attributeLists.remove(lists[i]);
                // return;
            }
        }
        /*
         * Frame[] fs = getAncestors(); for (int i = 0; i < fs.length; i++) { fs[i].removeAttributeList(listname); }
         */
        isDirty = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#removeChildFrame(java.lang.String, java.lang.String)
     */

    public void removeChildFrame(String listname, String frameName) {
        ArrayList list = (ArrayList) objectLists.get(listname);
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                FrameOLD attr = (FrameOLD) iter.next();
                if (attr.getTypeName().equals(frameName)) {
                    iter.remove();
                    isDirty = true;
                    return;
                }
            }
        } else {
            FrameOLD[] fs = getAncestors();
            for (int i = 0; i < fs.length; i++) {
                fs[i].removeChildFrame(listname, frameName);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#removeChildFrameList(java.lang.String)
     */

    public void removeChildFrameList(String listname) {
        String[] lists = getDeclaredFrameListNames();
        if (lists != null) {
            for (int i = 0; i < lists.length; i++) {
                if (lists[i].equals(listname)) {
                    objectLists.remove(listname);
                    isDirty = true;
                    return;
                }
            }
        }
        FrameOLD[] fs = getAncestors();
        for (int i = 0; i < fs.length; i++) {
            fs[i].removeChildFrameList(listname);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#resolveName(java.lang.String[])
     */

    public Object resolveName(String[] path) {

        // cannot happen when used correctly...
        if (path.length == 0) {
            return null;
        }

        // first element in name has to be listname
        if (attributeLists.containsKey(path[0]) && path.length == 2) {
            // logger.debug("-->"+this.getTypeName()+","+path.length+"/"+path[0]+","+path[1]);
            DomainAttribute a = this.getAttribute(path[0], path[1]);
            // logger.debug("***"+a);
            return a;
            // } else if (attributeLists.containsKey(path[0]) && path.length > 2) {

            // throw new RuntimeException("Path contains key to attribute, but has
            // unresolved names left");
        } else if (path.length == 2) {
            FrameOLD[] p = getAncestors();
            for (int i = 0; i < p.length; i++) {
                Object a = p[i].resolveName(path);
                if (a != null) {
                    return a;
                }
            }
        } else if (attributeLists.containsKey(path[0]) && path.length == 1) {
            return attributeLists.get(path[0]);
        } else if (objectLists.containsKey(path[0]) && path.length == 1) {
            return objectLists.get(path[0]);
        }

        if (objectLists.containsKey(path[0]) && path.length > 0) {
            if (path.length == 2) {
                return getChildFrame(path[0], path[1]);
            } else if (path.length > 2) {
                FrameOLD inst = getChildFrame(path[0], path[1]);
                logger.debug("..." + inst.getTypeName());
                if (inst != null) {
                    String[] nPath = new String[path.length - 2];
                    for (int i = 2; i < path.length; i++) {
                        nPath[i - 2] = path[i];
                    }
                    return inst.resolveName(nPath);
                }
            } else {
                return objectLists.get(path[0]);
                // throw new RuntimeException("Path terminates in a list name");
            }
        } else {
            // throw new RuntimeException("Path contains key to child frame
            // list, but specifies no instance name");
        }

        FrameOLD[] p = getAncestors();
        for (int i = 0; i < p.length; i++) {
            Object a = p[i].resolveName(path);
            if (a != null) {
                return a;
            }
        }

        // there is no path to the first element in path - return null for
        // unsuccessful search
        return null;

    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#setAncestor(gsim.def.objects.Frame)
     */

    public void setAncestor(FrameOLD newParent) {
        if (parents.containsKey(newParent.getTypeName())) {
            parents.put(newParent.getTypeName(), newParent);
        } else {
            FrameOLD[] fs = getAncestors();
            for (int i = 0; i < fs.length; i++) {
                fs[i].setAncestor(newParent);
            }
        }
        isDirty = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#setTypeName(java.lang.String)
     */

    public void setTypeName(String s) {
        typeName = s;
        isDirty = true;
    }

}
