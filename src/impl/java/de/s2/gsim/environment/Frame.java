package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * Frame class. A frame is a template for any object or agent classes.
 * 
 * @author stephan
 *
 */
public class Frame extends Unit {

    /**
     * An optional classification.
     */
    private String category = "";

    /**
     * A map with parent names and the respective frames.
     */
    private Map<String, Frame> parents = new HashMap<>();

    /**
     * Name of the frame.
     */
    private String name;


    /**
     * Private base constructor.
     */
    private Frame(String name) {
        this.name = name;
    }

    /**
     * Construct a top-level frame without any attributes and children.
     * 
     * @param name name
     * @param category category
     */
    private Frame(String name, String category) {
        this.name = name;
        this.category = category;
        isDirty = true;
    }

    /**
     * Creates a frame that inherits from the given list of parent frames.
     *
     * The inheritance mechanism puts all parent lists, object and attributes in its own container; these objects are not copied into the new frame.
     * Any operations that do not operate on declared entities, are thus implicitly operations on these parent objects. It means also that operations
     * on parents are immediately visible. Modifications applied to this frame are not propagated to the parents.
     * 
     * @param parents parents to inherit from
     * @param typeName
     * @param category
     * @return
     */
    public static Frame inherit(List<Frame> parents, String typeName, String category) {
        Frame f = new Frame(typeName, category);
        for (Frame parent : parents) {
            f.parents.put(f.getTypeName(), parent);
            f.isSystem = parent.isSystem();
            f.isMutable = parent.isMutable();
            f.isDirty = true;
        }
        return f;
    }

    /**
     * Creates a new Frame.
     * 
     * @param name the name
     * @param category an additional classification
     * @return the new Frame
     */
    public static Frame newFrame(String name, String category) {
        return new Frame(name, category);
    }

    /**
     * Creates a new Frame.
     * 
     * @param name the name
     * @return the new Frame
     */
    public static Frame newFrame(String name) {
        return new Frame(name);
    }

    /**
     * Creates a copy of the given frame with a new name, inheriting all its parents and copying all its attribute and object lists.
     * 
     * @param from the frame to copy from
     * @param newName the new name
     * @return the new frame
     */
    public static Frame copy(Frame from, String newName) {
        Frame f = copy(from);
        f.setTypeName(newName);
        return f;
    }

    /**
     * Creates an exact copy of the given frame with a new name, inheriting all its parents and copying all its attribute and object lists.
     * 
     * @param from the frame to copy from
     * @return the new frame
     */
    public static Frame copy(Frame from) {

        Frame frame = Frame.inherit(from.getParentFrames(), from.getTypeName(), from.getCategory());
        frame.setMutable(from.isMutable());
        frame.setSystem(from.isSystem());

        from.getDeclaredAttributesListNames().stream().forEach(attList -> {
            frame.attributeLists.put(attList, new ArrayList<DomainAttribute>());
            from.getDeclaredAttributes(attList).forEach(att -> frame.addOrSetAttribute(attList, att));
        });

        from.getDeclaredFrameListNames().stream().forEach(frameList -> {
            TypedList list = (TypedList) from.objectLists.get(frameList);
            Frame fr = list.getType();
            frame.objectLists.put(frameList, new TypedList(fr));
            from.getDeclaredChildFrames(frameList).forEach(child -> {
                frame.addChildFrame(frameList, child.clone());
            });

        });

        frame.isSystem = from.isSystem();
        frame.isMutable = from.isMutable();
        frame.isDirty = true;

        return frame;

    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#addOrSetAttribute(java.lang.String, gsim.def.objects.attribute.DomainAttribute)
     */

    public void addChildFrame(String listName, Frame a) {

        if (a == null) {
            return;
        }

        TypedList list = (TypedList) objectLists.get(listName);

        if (list == null) {
            // try to clone parent-list first:
            Frame type = getListType(listName);
            if (type != null) {
                list = new TypedList((Frame) type.clone());
            } else {
                list = new TypedList((Frame) a.clone());
            }
        }

        if (list.contains(a)) {
            list.remove(a);
            a = (Frame) a.clone();
            list.add(a);
        } else {
            list.add((Unit) a.clone());
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
    public Frame clone() {

        Frame f = new Frame(getTypeName(), getCategory());

        f.setMutable(isMutable());
        f.setSystem(isSystem());

        Frame[] parents = getParentFrames();
        for (int i = 0; i < parents.length; i++) {
            Frame fr = parents[i];
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
            TypedList list = getList(children[i]);// (TypedList)f.objectLists.get(children[i]);

            if (list != null) {
                Frame fr = list.getType();
                f.objectLists.put(children[i], new TypedList(fr));
            }

            Frame[] ch = getChildFrames(children[i]);
            for (int j = 0; j < ch.length; j++) {
                Frame child = (Frame) ch[j].clone();
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
            Frame[] fs = getAncestors();
            for (int i = 0; i < fs.length; i++) {
                Frame f = fs[i];
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

    public boolean containsChildFrame(String listName, Frame f) {
        Frame[] children = getChildFrames(listName);

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

        Frame[] parents = getAncestors();
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

        Frame[] parents = getAncestors();
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
        if (!(o instanceof Frame)) {
            return false;
        }

        if (getTypeName().equals(((Frame) o).getTypeName())) {
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

    public Frame getAncestor(String typeName) {
        if (parents.containsKey(typeName)) {
            return (Frame) parents.get(typeName);
        } else {
            Frame[] fs = getAncestors();
            for (int i = 0; i < fs.length; i++) {
                Frame f = fs[i].getParentFrame(typeName);
                if (f != null) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * Returns all ancestors, i.e. parents, grandparents and so on, in a flat list.
     * 
     * @return the ancestors or empty list if there are none
     */
    public List<Frame> getAncestors() {

        List<Frame> ancestors = new ArrayList<>();
        parents.values().stream().forEach(frame -> {
            ancestors.add(frame);
            for (Frame f : frame.getAncestors()) {
                ancestors.add(f);
            }
        });

        return ancestors;
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
        Frame[] fs = getAncestors();
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

        Frame[] fs = getAncestors();
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

        Frame[] fs = getAncestors();
        for (int i = 0; i < fs.length; i++) {
            Frame f = fs[i];
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

    public Frame getChildFrame(String listname, String frameName) {
        ArrayList list = (ArrayList) objectLists.get(listname);
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                Frame attr = (Frame) iter.next();
                if (attr.getTypeName().equals(frameName)) {
                    return (Frame) attr.clone();
                }
            }
        } else {
            Frame[] fs = getAncestors();
            for (int i = 0; i < fs.length; i++) {
                Frame da = fs[i].getChildFrame(listname, frameName);
                if (da != null) {
                    return (Frame) da.clone();
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

        Frame[] parents = getAncestors();
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

    public List<Frame> getChildFrames(String listname) {

        List<Frame> declared = objectLists.get(listname).stream().filter(f -> f instanceof Frame).map(f -> (Frame) f).collect(Collectors.toList());

        HashSet<Frame> sum = new HashSet<>();

        Frame[] fs = getAncestors();

        for (int i = 0; i < fs.length; i++) {
            Frame[] da = fs[i].getChildFrames(listname);
            if (da != null) {
                for (int j = 0; j < da.length; j++) {
                    sum.add(da[j]);
                }
            }
        }

        Frame[] children = new Frame[sum.size()];
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

    /**
     * Get the attributes declared on this frame for the given list.
     * 
     * @param listname the listname to retrieve the attributes from
     * @return a list of attributes
     */
    public List<DomainAttribute> getDeclaredAttributes(String listname) {
        return attributeLists.get(listname).stream().filter(a -> a instanceof DomainAttribute).map(da -> (DomainAttribute) da)
                .collect(Collectors.toList());
    }

    public List<String> getDeclaredAttributesListNames() {
        return new ArrayList<String>(attributeLists.keySet());
    }

    /**
     * Gets the declared objects held by this frame.
     * 
     * @param listname the listname to retrieve the objects from
     * @return a list of frames or empty list if the list does not exist
     */
    public List<Frame> getDeclaredChildFrames(String listname) {
        if (!objectLists.containsKey(listname)) {
            return new ArrayList<Frame>();
        }
        return objectLists.get(listname).stream().filter(o -> o instanceof Frame).map(o -> (Frame) o).collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getDeclaredAttributesListNames()
     */

    public Frame getDeclaredFrame(String listname, String attrName) {
        ArrayList list = (ArrayList) objectLists.get(listname);
        if (list != null) {
            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                Frame attr = (Frame) iter.next();
                if (attr.getTypeName().equals(attrName)) {
                    return attr;
                }
            }
        }
        return null;
    }

    public List<String> getDeclaredFrameListNames() {
        return new ArrayList<String>(objectLists.keySet());
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getDeclaredFrame(java.lang.String, java.lang.String)
     */

    public TypedList getList(String name) {
        TypedList l = (TypedList) objectLists.get(name);
        if (l == null) {
            Iterator iter = parents.values().iterator();
            while (iter.hasNext()) {
                Frame p = (Frame) iter.next();
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

    public Frame getListType(String listName) {
        TypedList list = getList(listName);
        if (list == null) {
            Iterator iter = parents.values().iterator();
            while (iter.hasNext()) {
                Frame p = (Frame) iter.next();
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

    public Frame getParentFrame(String typeName) {
        if (parents.containsKey(typeName)) {
            return (Frame) parents.get(typeName);
        }
        return null;
    }

    /**
     * Get the parents (immediate ancestors).
     * 
     * @return the parent frames or emtpy lists if none exist
     */
    public List<Frame> getParentFrames() {
        return new ArrayList<>(parents.values());
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#getTypeName()
     */

    public String getTypeName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see gsim.def.objects.Frame#isDeclaredAttribute(java.lang.String, java.lang.String)
     */

    public boolean isDeclaredAttribute(String listName, String attributeName) {
        Frame[] p = getAncestors();
        for (Frame f : p) {
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
        Frame[] p = getAncestors();
        for (Frame f : p) {
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
        Frame[] ancestors = getAncestors();
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
                Frame attr = (Frame) iter.next();
                if (attr.getTypeName().equals(frameName)) {
                    iter.remove();
                    isDirty = true;
                    return;
                }
            }
        } else {
            Frame[] fs = getAncestors();
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
        Frame[] fs = getAncestors();
        for (int i = 0; i < fs.length; i++) {
            fs[i].removeChildFrameList(listname);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T resolvePath(Path<T> path) {
        return resolvePath(path, true);
    }

    @SuppressWarnings("unchecked")
    private <T> T resolvePath(Path<T> path, boolean searchAncestors) {

        // Terminal frame
        if (this.getTypeName().equals(path.getName()) && path.isTerminal()) {
            return (T) this;
        }

        // Frame is in path but has successors - step into child object
        if (this.getTypeName().equals(path.getName())) {
            return (T) resolvePath(path.next());
        }

        // Step into attribute lists to find attribute
        if (attributeLists.containsKey(path.getName()) && !path.isTerminal()) {
            return (T) this.getAttribute(path.getName(), path.next().getName());
        }

        // found attribute
        if (attributeLists.containsKey(path.getName())) {
            return (T) this.attributeLists.get(path.getName());
        }

        // step into object lists to find object
        if (objectLists.containsKey(path.getName()) && !path.isTerminal()) {
            return (T) this.getChildFrame(path.getName(), path.next().getName()).resolvePath(path.next());
        }

        // found object
        if (objectLists.containsKey(path.getName())) {
            return (T) this.getObjectLists().get(path.getName());
        }

        // If not found, check whether the object is declared in any parent frame
        if (searchAncestors) {
            for (Frame ancestor : getAncestors()) {
                T t = ancestor.resolvePath(path);
                if (t != null) {
                    return t;
                }
            }
        }

        throw new GSimDefException(String.format("The path %s could not be resolved", path));

    }

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
            Frame[] p = getAncestors();
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
                Frame inst = getChildFrame(path[0], path[1]);
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

        Frame[] p = getAncestors();
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

    public void setAncestor(Frame newParent) {
        if (parents.containsKey(newParent.getTypeName())) {
            parents.put(newParent.getTypeName(), newParent);
        } else {
            Frame[] fs = getAncestors();
            for (int i = 0; i < fs.length; i++) {
                fs[i].setAncestor(newParent);
            }
        }
        isDirty = true;
    }

    public void setTypeName(String s) {
        name = s;
        isDirty = true;
    }

    /**
     * Replaces the values of attribute identified by {@link Path} with the ones from the given.
     * 
     * @param path the path
     * @param newValue the new attribute (values are copied)
     */
    public void setChildAttribute(Path<DomainAttribute> path, DomainAttribute newValue) {
        DomainAttribute attr = this.resolvePath(path, false);
        attr.copyFrom(newValue);
    }

}
