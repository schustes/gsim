package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
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
	private String category = "";

	/**
	 * A map with parent names and the respective frames.
	 */
	private Map<String, Frame> parents = new HashMap<>();


	/**
	 * Private base constructor.
	 */
	private Frame(@NotNull String name) {
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
	private Frame(@NotNull String name, Optional<String> category, boolean isMutable, boolean isSystem) {
		super(name, isMutable, isSystem);
		this.category = category.get();
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
	public static Frame inherit(@NotNull List<Frame> parents, @NotNull String name, Optional<String> category) {
		Frame f = new Frame(name, category, true, false);
		for (Frame parent : parents) {
			f.parents.put(f.getTypeName(), parent);
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
			f.parents.put(f.getTypeName(), parent);
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
	public static Frame copy(@NotNull Frame from) {

		Frame frame = Frame.inherit(from.getParentFrames(), from.getTypeName(), Optional.of(from.getCategory()), from.isMutable(), from.isSystem());

		from.getDeclaredAttributesListNames().stream().forEach(attList -> {
			from.getDeclaredAttributes(attList).forEach(att -> frame.addOrSetAttribute(attList, att));
		});

		from.getDeclaredFrameListNames().stream().forEach(frameList -> {
			TypedList<Frame,DomainAttribute> list = from.getObjectLists().get(frameList);
			Frame fr = list.getType();
			frame.getObjectLists().put(frameList, new TypedList<>(fr));
			from.getDeclaredChildFrames(frameList).forEach(child -> {
				frame.addChildFrame(frameList, child.clone());
			});

		});

		frame.setDirty(true);

		return frame;

	}

	/**
	 * Adds another object to this frame.
	 *  
	 * @param listName name of the list where this object is to be placed
	 * @param frame the frame to add
	 */
	public void addChildFrame(@NotNull String listName, @NotNull Frame frame) {

		TypedList<Frame, DomainAttribute> list;
		TypedMap<Frame, DomainAttribute> objectLists = super.getObjectLists();
		if (objectLists.containsKey(listName)) {
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

		Frame f = new Frame(getTypeName(), Optional.of(getCategory()), isMutable(), isSystem());

		this.getParentFrames().stream().forEach(parent -> {
			f.parents.put(parent.getTypeName(), parent);
		});

		this.getDeclaredAttributesListNames().stream().forEach(attList -> {
			this.getDeclaredAttributes(attList).forEach(att -> f.addOrSetAttribute(attList, att.clone()));
		});

		this.getDeclaredFrameListNames().stream().forEach(frameList -> {
			TypedList<Frame,DomainAttribute> list = this.getObjectLists().get(frameList);
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
	public boolean containsAttribute(String listName, String attributeName) {

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
	public boolean containsChildFrame(String listName, Frame searched) {

		TypedMap<Frame, DomainAttribute> lists = getObjectLists();

		if (lists.containsKey(listName)) {
			return lists.get(listName).stream().anyMatch(frame -> frame.getName().equals(searched.getName()) || frame.getName() .startsWith("{"));
		}
		
		return getAncestors().stream().anyMatch(ancestor -> ancestor.containsChildFrame(listName, searched));
	
	}

	/**
	 * Checks whether this frame (or one of its ancestor frames) defines the given object list.
	 * 
	 * @param listName the list name to check for
	 * @return true if the list is defined, false otherwise
	 */
	public boolean definesChildList(String listName) {
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
	public Frame getAncestor(String name) {
		
		if (parents.containsKey(name)) {
			return parents.get(name);
		} 
		
		return getAncestors().stream().filter(ancestor -> ancestor.getAncestor(name)!=null).findFirst().get();
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
	public DomainAttribute getAttribute(String attrName) {
		
		DomainAttribute att = null;
		for (String list: getAttributesListNames()) {
			att = getAttribute(list, attrName);
			if (att != null)  {
				return att;
			}
			
		}
		return att;
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
