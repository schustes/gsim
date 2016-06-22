package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * Frame class. A frame is a template for any object or agent classes.
 * 
 * @author stephan
 *
 */
public class Frame extends Unit<Frame, DomainAttribute> {

    /**
     * An optional classification.
     */
    protected String category = "";

    /**
     * A map with parent names and the respective frames.
     */
    protected Map<String, Frame> parents = new HashMap<>();

    /**
     * Protected base constructor.
     */
    protected Frame(@NotNull String name) {
        super(name, true, false);
    }

    /**
     * Construct a top-level frame without any attributes and children.
     * 
     * @param name name
     * @param category category
     * @param isMutable true if the object can be modified
     * @param isSystem true if system
     */
    protected Frame(@NotNull String name, Optional<String> category, boolean isMutable, boolean isSystem) {
        super(name, isMutable, isSystem);
        if (category.isPresent()) {
            this.category = category.get();
        }
        super.setDirty(true);
    }

    /**
     * Constructor by inheritance.
     */
    protected Frame(Frame f) {
        super(f.getName(), f.isMutable(), f.isSystem());
        this.category = f.getCategory();
        for (Frame parent : f.getParentFrames()) {
            this.parents.put(f.getName(), parent);
        }
        super.setDirty(true);
    }

    /**
     * Creates a frame that inherits from the given list of parent frames.
     *
     * The inheritance mechanism puts all parent lists, object and attributes in its own container; these objects are not copied into the new frame.
     * Any operations that do not operate on declared entities, are thus implicitly operations on these parent objects. It means also that operations
     * on parents are immediately visible. Modifications applied to this frame are not propagated to the parents.
     * 
     * @param parents parents to inherit from
     * @param name name
     * @param category optional category
     * @return the new frame
     */
    public static Frame inherit(@NotNull List<? extends Frame> parents, @NotNull String name, Optional<String> category) {
        Frame f = new Frame(name, category, true, false);
        for (Frame parent : parents) {
            f.parents.put(parent.getName(), parent);
        }
        f.setDirty(true);
        return f;
    }

    /**
     * Like #inherit(), but sets additional mutable and system attributes.
     * 
     * @param parents parents to inherit from
     * @param name name
     * @param category optional category
     * @param isMutable whether object can be modified
     * @param isSystem whether is system
     * @return the new frame
     */
    private static Frame inherit(@NotNull List<Frame> parents, @NotNull String name, Optional<String> category, boolean isMutable, boolean isSystem) {
        Frame f = new Frame(name, category, isMutable, isSystem);
        for (Frame parent : parents) {
            f.parents.put(f.getName(), parent);
        }
        f.setDirty(true);
        return f;
    }

    /**
     * Creates a new Frame.
     * 
     * @param name the name
     * @param category an additional classification
     * @return the new Frame
     */
    public static Frame newFrame(@NotNull String name, Optional<String> category) {
        return new Frame(name, category, true, false);
    }

    /**
     * Creates a new Frame.
     * 
     * @param name the name
     * @return the new Frame
     */
    public static Frame newFrame(@NotNull String name) {
        return new Frame(name);
    }

    /**
     * Creates a copy of the given frame with a new name, inheriting all its parents and copying all its attribute and object lists.
     * 
     * @param from the frame to copy from
     * @param newName the new name
     * @return the new frame
     */
    public static Frame copy(@NotNull Frame from, @NotNull String newName) {

        Frame frame = Frame.inherit(from.getParentFrames(), newName, Optional.of(from.getCategory()), from.isMutable(), from.isSystem());

        return copyInternal(from, frame);
    }

    /**
     * Creates an exact copy of the given frame with a new name, inheriting all its parents and copying all its attribute and object lists.
     * 
     * @param from the frame to copy from
     * @return the new frame
     */
    public static Frame copy(@NotNull Frame from) {

        Frame frame = Frame.inherit(from.getParentFrames(), from.getName(), Optional.of(from.getCategory()), from.isMutable(), from.isSystem());

        return copyInternal(from, frame);

    }

    /**
     * Copies the attributes and child frames from one frame to another.
     * 
     * @param from the frame to copy from
     * @param to the frame to copy to
     * @return references to the same frame being copied to
     */
    private static Frame copyInternal(@NotNull Frame from, @NotNull Frame to) {

        from.getDeclaredAttributesListNames().stream().forEach(attList -> {
            from.getDeclaredAttributes(attList).forEach(att -> to.addOrSetAttribute(attList, att));
        });

        from.getDeclaredFrameListNames().stream().forEach(frameList -> {
            TypedList<Frame> list = from.getObjectLists().get(frameList);
            Frame fr = list.getType();
            to.getObjectLists().put(frameList, new TypedList<>(fr));
            from.getDeclaredChildFrames(frameList).forEach(child -> {
                to.addChildFrame(frameList, child.clone());
            });

        });

        to.setDirty(true);

        return to;

    }

    /**
     * Adds another object to this frame.
     * 
     * @param listName name of the list where this object is to be placed
     * @param frame the frame to add
     */
    public void addChildFrame(@NotNull String listName, @NotNull Frame frame) {

        TypedList<Frame> list;
        TypedMap<Frame> objectLists = super.getObjectLists();
        if (!objectLists.containsKey(listName)) {
            list = new TypedList<>(frame.clone());
            objectLists.put(listName, list);
        } else {
            list = objectLists.get(listName);
            list.remove(frame);
        }
        list.add(frame.clone());

        setDirty(true);
    }

    /**
     * Upserts an attribute in this frame.
     * 
     * @param listName list name where the attribute is located
     * @param attribute the attribute
     */
    public void addOrSetAttribute(@NotNull String listName, @NotNull DomainAttribute attribute) {

        Map<String, List<DomainAttribute>> lists = getAttributeLists();
        List<DomainAttribute> list;
        if (lists.containsKey(listName)) {
            list = lists.get(listName);
            list.remove(attribute);
        } else {
            list = new ArrayList<DomainAttribute>();
            lists.put(listName, list);
        }
        list.add(attribute.clone());

        setDirty(true);
    }

    @Override
    public Frame clone() {

        Frame f = new Frame(getName(), Optional.of(getCategory()), isMutable(), isSystem());

        this.getParentFrames().stream().forEach(parent -> {
            f.parents.put(parent.getName(), parent);
        });

        this.getDeclaredAttributesListNames().stream().forEach(attList -> {
            this.getDeclaredAttributes(attList).forEach(att -> f.addOrSetAttribute(attList, att.clone()));
        });

        this.getDeclaredFrameListNames().stream().forEach(frameList -> {
            TypedList<Frame> list = this.getObjectLists().get(frameList);
            f.getObjectLists().put(frameList, new TypedList<>(list.getType()));
            this.getDeclaredChildFrames(frameList).forEach(child -> {
                f.addChildFrame(frameList, child.clone());
            });

        });

        f.setDirty(true);
        return f;

    }

    /**
     * Tests whether the given attribute is contained in this frame (declared or inherited).
     * 
     * @param listName the attribute list name
     * @param attributeName the attribute name
     * @return true if contained, false otherwise
     */
    public boolean containsAttribute(@NotNull String listName, @NotNull String attributeName) {

        Map<String, List<DomainAttribute>> attributeLists = getAttributeLists();

        if (attributeLists.containsKey(listName)) {
            return attributeLists.get(listName).stream().anyMatch(att -> att.getName().equals(attributeName));
        }

        return getAncestors().stream().anyMatch(ancestor -> ancestor.containsAttribute(listName, attributeName));

    }

    /**
     * Checks whether this frame (or any of its ancestor frames) contains the given frame.
     * 
     * @param listName the list name
     * @param searched the frame to look for
     * @return true if found, false otherwise
     */
    public boolean containsChildFrame(@NotNull String listName, @NotNull Frame searched) {

        TypedMap<Frame> lists = getObjectLists();

        if (lists.containsKey(listName)) {
            return lists.get(listName).stream().anyMatch(frame -> frame.getName().equals(searched.getName()) || frame.getName().startsWith("{"));
        }

        return getAncestors().stream().anyMatch(ancestor -> ancestor.containsChildFrame(listName, searched));

    }

    /**
     * Checks whether this frame (or one of its ancestor frames) defines the given object list.
     * 
     * @param listName the list name to check for
     * @return true if the list is defined, false otherwise
     */
    public boolean definesChildList(@NotNull String listName) {
        return getChildFrameListNames().contains(listName);
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Frame)) {
            return false;
        }

        if (getName().equals(((Frame) o).getName())) {
            return true;
        }

        return false;

    }

    /**
     * Gets ancestor identified by its name.
     * 
     * @param name the ancestor's name
     * @return the ancestor or null if not found
     */
    public Frame getAncestor(@NotNull String name) {

        if (parents.containsKey(name)) {
            return parents.get(name);
        }

        return getAncestors().stream().filter(ancestor -> ancestor.getAncestor(name) != null).findFirst().get();
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
            frame.getAncestors().stream().forEach(a -> ancestors.add(a));
        });

        return ancestors;
    }

    /**
     * Gets the first attribute matching the given name, regardless whether is is defined in several attribute lists.
     * 
     * @param attrName the attribute name
     * @return the attribute or null if nothing was found
     */
    public DomainAttribute getAttribute(@NotNull String attrName) {

        DomainAttribute att = null;
        for (String list : getAttributesListNames()) {
            att = getAttribute(list, attrName);
            if (att != null) {
                return att;
            }

        }
        return att;
    }

    /**
     * Gets the specified attribute from given list.
     * 
     * @param listname the attribute list name
     * @param attrName the attribute name
     * @return the attribute or null if not found
     */
    public DomainAttribute getAttribute(@NotNull String listname, @NotNull String attrName) {

        if (getAttributeLists().containsKey(listname)) {
            List<DomainAttribute> list = getAttributeLists().get(listname);
            Optional<DomainAttribute> thisFrameAtt = list.stream().filter(attr -> attr.getName().equals(attrName)).reduce((a1, a2) -> a2);
            if (thisFrameAtt.isPresent()) {
                return thisFrameAtt.get().clone();
            }
        }

        for (Frame ancestor : getAncestors()) {
            DomainAttribute da = ancestor.getAttribute(listname, attrName);
            if (da != null) {
                return (DomainAttribute) da.clone();
            }
        }

        return null;
    }

    /**
     * Gets all attributes from a list.
     * 
     * @param listname the attribute list name
     * @return the attributes or empty list if no attributes are in the list or the list does not exist
     */
    public List<DomainAttribute> getAttributes(@NotNull String listname) {

        Set<DomainAttribute> allAttributes = new HashSet<DomainAttribute>();
        if (getAttributeLists().containsKey(listname)) {
            getAttributeLists().get(listname).forEach(a -> allAttributes.add(a.clone()));
        }

        for (Frame ancestor : getAncestors()) {
            ancestor.getAttributes(listname).forEach(attr -> allAttributes.add(attr.clone()));
        }

        return new ArrayList<DomainAttribute>(allAttributes);
    }

    /**
     * Gets all attribute list names defined in this frame or one of its ancestors.
     * 
     * @return list of list names or empty list if nothing was found
     */
    public List<String> getAttributesListNames() {

        Set<String> set = getAttributeLists().keySet().stream().collect(Collectors.toSet());
        set.addAll(getAncestors().stream().flatMap(ancestor -> ancestor.getAttributesListNames().stream()).collect(Collectors.toSet()));

        return new ArrayList<String>(set);
    }

    public String getCategory() {
        return category;
    }

    /**
     * Gets a frame out of the specified object list.
     * 
     * @param listname name of the list where the frame is to be searched
     * @param frameName name of the frame
     * @return the frame or null if nothing was found
     */
    public Frame getChildFrame(@NotNull String listname, @NotNull String frameName) {

        if (getObjectLists().containsKey(listname)) {
            Optional<Frame> thisLevelChild = super.getObjectLists().get(listname).stream().filter(frame -> frame.getName().equals(frameName))
                    .findFirst();
            if (thisLevelChild.isPresent()) {
                return thisLevelChild.get().clone();
            }
        }

        getAncestors().stream().forEach(frame -> frame.getChildFrame(listname, frameName));

        for (Frame ancestor : getAncestors()) {
            Frame af = ancestor.getChildFrame(listname, frameName);
            if (af != null) {
                return af.clone();
            }
        }

        return null;
    }

    /**
     * Gets all child frame list names defined in this frame (or one of its ancestor frames).
     * 
     * @return the list of names or empty list if no child frame lists are defined at all
     */
    public List<String> getChildFrameListNames() {

        Set<String> all = new HashSet<String>();
        all.addAll(getObjectLists().keySet());

        getAncestors().stream().forEach(ancestor -> {
            all.addAll(ancestor.getChildFrameListNames());
        });

        return new ArrayList<String>(all);
    }

    /**
     * Gets all frames contained in the given object list declared in this frame or in its ancestor frames.
     * 
     * @param listname the list name to get the contained frames from
     * @return a list of frames or empty list if nothing was found
     */
    public List<Frame> getChildFrames(@NotNull String listname) {

        Set<Frame> all = getObjectLists().get(listname).stream().filter(f -> f instanceof Frame).map(f -> (Frame) f).collect(Collectors.toSet());

        getAncestors().stream().forEach((Frame a) -> a.getChildFrames(listname).stream().forEach(child -> all.add(child)));

        return new ArrayList<Frame>(all);

    }

    /**
     * Gets an attribute from the given list declared in this frame (attributes of the same list name in ancestors are ignored).
     * 
     * @param listname the list name
     * @param attrName the attribute name
     * @return the attribute or null if nothing was found
     */
    public DomainAttribute getDeclaredAttribute(@NotNull String listname, @NotNull String attrName) {
        List<DomainAttribute> attrs = getAttributeLists().getOrDefault(listname, new ArrayList<DomainAttribute>());
        return attrs.stream().filter(attr -> attr.getName().equals(attrName)).findFirst().get();
    }

    /**
     * Get the attributes declared on this frame for the given list.
     * 
     * @param listname the list name to retrieve the attributes from
     * @return a list of attributes
     */
    public List<DomainAttribute> getDeclaredAttributes(@NotNull String listname) {
        return getAttributeLists().get(listname).stream().filter(a -> a instanceof DomainAttribute).map(da -> (DomainAttribute) da)
                .collect(Collectors.toList());
    }

    /**
     * Gets the attribute list names declared on this frame.
     * 
     * @return list of strings or empty list if no attribute list is defined
     */
    public List<String> getDeclaredAttributesListNames() {
        return new ArrayList<String>(getAttributeLists().keySet());
    }

    /**
     * Gets the declared objects held by this frame.
     * 
     * @param listname the list name to retrieve the objects from
     * @return a list of frames or empty list if the list does not exist
     */
    public List<Frame> getDeclaredChildFrames(@NotNull String listname) {
        if (!getObjectLists().containsKey(listname)) {
            return new ArrayList<Frame>();
        }
        return getObjectLists().get(listname).stream().filter(o -> o instanceof Frame).map(o -> (Frame) o).collect(Collectors.toList());
    }

    /**
     * Gets the frame with the specified name from the declared frames in the given list.
     * 
     * @param listname the list to search in
     * @param name the frame name
     * @return the frame or null if not found or the list doesn't exist
     */
    public Frame getDeclaredFrame(@NotNull String listname, @NotNull String name) {

        if (!getObjectLists().containsKey(listname)) {
            return null;
        }

        TypedList<Frame> declaredList = getObjectLists().get(listname);
        for (Frame f : declaredList) {
            if (f.getName().equals(name)) {
                return f;
            }
        }

        return null;
    }

    /**
     * Gets the object list names declared in this frame.
     * 
     * @return a list of names or empty list if no lists are defined
     */
    public List<String> getDeclaredFrameListNames() {
        return new ArrayList<String>(getObjectLists().keySet());
    }

    /**
     * Gets the object list with the specified name from this frame or one of its ancestor frames.
     * 
     * @param name the list name
     * @return a list with frames or empty list if the frame or the list does not exist
     */
    public TypedList<Frame> getChildFrameList(@NotNull String name) {

        if (getObjectLists().containsKey(name)) {
            return getObjectLists().get(name);
        }

        for (Frame ancestor : getAncestors()) {
            return ancestor.getChildFrameList(name);
        }
        return null;
    }

    /**
     * Gets the type of an object list, which is defined by a frame that this list must contain.
     * 
     * @param listName the list name
     * @return the frame that the specified list must hold (or instances thereof)
     */
    public Frame getListType(@NotNull String listName) {
        TypedList<Frame> list = getChildFrameList(listName);

        if (list == null) {
            throw new IllegalArgumentException(String.format(
                    "To get a type for the given list, the list must exist in this frame or one of its ancestors, but list %s was not found",
                    listName));
        }
        return list.getType();
    }

    /**
     * Gets the ancestor with the given name.
     * 
     * @param name the name of the frame that is an ancestor of this frame
     * @return the ancestor frame of null if not existing
     */
    public Frame getParentFrame(@NotNull String name) {
        if (parents.containsKey(name)) {
            return (Frame) parents.get(name);
        }
        return null;
    }

    /**
     * Get the ancestors.
     * 
     * @return the parent frames or empty lists if none exist
     */
    public List<Frame> getParentFrames() {
        return new ArrayList<>(parents.values());
    }

    /**
     * Tests whether the given attribute is declared in this frame. An attribute is declared if there is ancestor which declares the attribute also.
     * 
     * @param listName the list name to search the attribute in
     * @param attributeName the attribute name
     * @return true if the attribute is declared, false if not existing or declared in parent
     */
    public boolean isDeclaredAttribute(@NotNull String listName, @NotNull String attributeName) {

        if (getAncestors().stream().anyMatch(f -> f.getDeclaredAttribute(listName, attributeName) != null)) {
            return false;
        }

        return this.getDeclaredAttribute(listName, attributeName) != null;

    }

    /**
     * Checks whether the given attribute list is defined in this frame or not. An attribute list is not declared if it does not exist in this frame
     * or any of its ancestors defines the list.
     * 
     * @param listName the list name
     * @return true if declared, false otherwise
     */
    public boolean isDeclaredAttributeList(@NotNull String listName) {

        if (getAncestors().stream().anyMatch(f -> f.getDeclaredAttributesListNames().contains(listName))) {
            return false;
        }

        return getDeclaredAttributesListNames().stream().anyMatch(l -> l.equals(listName));

    }

    /**
     * Checks whether this frame is a successor of the given ancestor.
     * 
     * @param ancestorName the name of the ancestor frame
     * @return true if this frame is successor, false otherwise
     */
    public boolean isSuccessor(@NotNull String ancestorName) {
        return getAncestors().stream().anyMatch(f -> f.getName().equals(ancestorName));
    }

    /**
     * Removes the given attribute from the specified attribute list.
     * 
     * @param listname the attribute list name
     * @param attr the attribute to remove
     * @return true if attribute was removed, false if this was not possible, e.g. because the attribute does not exist
     */
    public boolean removeDeclaredAttribute(@NotNull String listname, @NotNull DomainAttribute attr) {

        if (!getAttributeLists().containsKey(listname)) {
            return false;
        }

        if (getAttributeLists().get(listname).remove(attr)) {
            super.setDirty(true);
            return true;
        }

        return false;
    }

    /**
     * Removes a frame from the given list in this frame or one of the ancestor frames.
     * 
     * @param listname the child frame list
     * @param name the name of the frame
     * @return true if the frame was removed, false if that was not possible, e.g. because the list or frame is not existing
     */
    public boolean removeChildFrame(@NotNull String listname, @NotNull String name) {

        if (getObjectLists().containsKey(listname)) {
            Iterator<Frame> iter = getObjectLists().get(listname).iterator();
            while (iter.hasNext()) {
                Frame f = iter.next();
                if (f.getName().equals(name)) {
                    iter.remove();
                    setDirty(true);
                    return true;
                }
            }
        }

        for (Frame ancestor : getAncestors()) {
            return ancestor.removeChildFrame(listname, name);
        }

        return false;
    }

    /**
     * Removes the given child frame list with all contained frames in this frame or in one of the ancestor frames.
     * 
     * @param listname the object list name
     * @return true if the list was removed or false if this was not possible, e.g. because the list does not exist
     */
    public boolean removeChildFrameList(@NotNull String listname) {

        if (getObjectLists().remove(listname) != null) {
            setDirty(true);
            return true;
        }

        for (Frame ancestor : getAncestors()) {
            return ancestor.removeChildFrameList(listname);
        }

        return false;

    }

    /**
     * Tries to resolve the given path to the expected object (Frame or {@link DomainAttribute}):
     * 
     * @param path the path
     * @return the resolved object or null if nothing could be resolved
     */
    public <T> T resolvePath(Path<T> path) {
        return resolvePath(path, true);
    }

    /**
     * Performs the actual resolution recursion.
     * 
     * @param path the path
     * @param searchAncestors true if also ancestor frames are to be looked in
     * @return the resolved object or null if nothing could be resolved
     */
    @SuppressWarnings("unchecked")
    private <T> T resolvePath(Path<T> path, boolean searchAncestors) {

        // Terminal frame
        if (this.getName().equals(path.getName()) && path.isTerminal()) {
            return (T) this;
        }

        // Frame is in path but has successors - step into child object
        if (this.getName().equals(path.getName())) {
            return (T) resolvePath(path.next());
        }

        // Step into attribute lists to find attribute
        if (getAttributeLists().containsKey(path.getName()) && !path.isTerminal()) {
            return (T) this.getAttribute(path.getName(), path.next().getName());
        }

        // found attribute
        if (getAttributeLists().containsKey(path.getName())) {
            return (T) this.getAttributeLists().get(path.getName());
        }

        // step into object lists to find object
        if (getObjectLists().containsKey(path.getName()) && !path.isTerminal()) {
            return (T) this.getChildFrame(path.getName(), path.next().getName()).resolvePath(path.next());
        }

        // found object
        if (getObjectLists().containsKey(path.getName())) {
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

    /**
     * Replaces the ancestor in this frame or within the ancestor hierarchy if it exists somewhere.
     * 
     * @param ancestor the ancestor to replace
     * @return true if the frame was replaced, false if this was not possible, e.g. because it does not exist
     */
    public boolean replaceAncestor(Frame ancestor) {

        if (parents.replace(ancestor.getName(), ancestor) != null) {
            setDirty(true);
            return true;
        }

        for (Frame a : getAncestors()) {
            return a.replaceAncestor(ancestor);
        }

        return false;
    }

    /**
     * Replaces the values of attribute identified by {@link Path} with the ones from the given.
     * 
     * @param path the path
     * @param newValue the new attribute (values are copied)
     * @return true if the attribute was replaced, false if this was not possible because it does not exist
     */
    public boolean replaceChildAttribute(Path<DomainAttribute> path, DomainAttribute newValue) {
        DomainAttribute attr = this.resolvePath(path, false);
        if (attr != null) {
            attr.copyFrom(newValue);
            setDirty(true);
        }
        return false;
    }

    public boolean addChildAttribute(Path<List<DomainAttribute>> path, DomainAttribute newValue) {
        List<DomainAttribute> attr = this.resolvePath(path, false);
        if (attr != null) {
            attr.add(newValue.clone());
            setDirty(true);
        }
        return false;
    }

    /**
     * Removes the attribute identified by {@link Path} somewhere in the tree of attributes or child attributes.
     * 
     * @param path the path
     * @return true if the attribute was removed, false if none with the given attribute path could be found and/or deleted
     */
    public boolean removeChildAttribute(Path<DomainAttribute> path) {

        DomainAttribute attr = this.resolvePath(path);
        if (attr != null) {
            Path<List<DomainAttribute>> attrListPath = Path.withoutLastAttributeOrObject(path, Path.Type.LIST, DomainAttribute.class);
            List<DomainAttribute> attrList = this.resolvePath(attrListPath, false);
            if (attrList != null) {
                attrList.remove(attr);
                setDirty(true);
                return true;
            }
        }
        return false;
    }

    public List<String> getListNamesWithDeclaredChildFrame(String frameName) {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, TypedList<Frame>> m : this.getObjectLists().entrySet()) {
            if (m.getValue().getType().getName().equals(frameName)) {
                names.add(m.getKey());
            }
        }
        return names;
    }

    public boolean hasDeclaredChildFrame(String frameName) {
        return !getListNamesWithDeclaredChildFrame(frameName).isEmpty();
    }

    public boolean removeChildFrame(Path<Frame> framePath) {
        Frame f = this.resolvePath(framePath);
        if (f != null) {
            Path<List<Frame>> childFrameListPath = Path.withoutLastAttributeOrObject(framePath, Path.Type.LIST, Frame.class);
            if (childFrameListPath != null) {
                List<Frame> list = this.resolvePath(childFrameListPath, false);
                if (list != null) {
                    list.remove(f);
                    setDirty(true);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addChildFrame(Path<TypedList<Frame>> framePath, Frame frameToAdd) {
        TypedList<Frame> list = this.resolvePath(framePath);
        if (list != null) {
            list.add(frameToAdd.clone());
            setDirty(true);
            return true;
        }
        return false;
    }

}
