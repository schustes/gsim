package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.validation.constraints.NotNull;

import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeFactory;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * An instance is the instantiation of a domain entity or frame. Instances extend frames by assigning concrete value to the attributes. Instances
 * represent similar to objects inheritance hierarchies. If an attributelist is not found in the current instance, it is looked in the superclasses
 * until it is found.
 *
 */
public class Instance extends Unit<Instance, Attribute> {

	/**
	 * The frame from which this instance was created.
	 */
	private Frame frame;

	/**
	 * Creates a new instance by copying its properties from an existing one.
	 * 
	 * @param from the instance to copy from
	 * @return the new instance
	 */
	public static Instance copy(@NotNull Instance from) {
		Instance instance = new Instance(from.getName(), from.getDefinition());
		return copy(from, instance);
	}

	public static Instance copy(Instance from, Instance to) {
		for (String attListName: from.getAttributesListNames()) {
			for (Attribute att: from.getAttributes(attListName)) {
				to.addOrSetAttribute(attListName, att.clone());
			}
		}

		for (String instanceListName: from.getChildInstanceListNames()) {
			TypedList<Frame> list = from.getDefinition().getChildFrameList(instanceListName);
			to.getObjectLists().put(instanceListName, new TypedList<Instance>(list.getType()));
			for (Instance child: from.getChildInstances(instanceListName)) {
				to.addChildInstance(instanceListName, Instance.copy(child));
			}
		}

		to.setDirty(false);

		return to;
	}

	/**
	 * Instanciates a new instance from the given frame.
	 * 
	 * @param instanceName the name of the instance to create
	 * @param frame the frame to instanciate from
	 * @return the new instance
	 */
	public static Instance instanciate(@NotNull String instanceName, @NotNull Frame frame) {
		Instance instance = new Instance(instanceName, frame);
		instance.instanciate(frame);
		return instance;
	}

	/**
	 * Constructs a new instance with reference to the given frame.
	 * 
	 * @param name instance name
	 * @param frame frame serving as instanciation template
	 */
	protected Instance(@NotNull String name, @NotNull Frame frame) {
		super(name, true, false);
		this.frame = frame;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param inst the instance to create the new instance from
	 */
	protected Instance(@NotNull Instance inst) {
		super(inst.getName(), inst.isMutable(), inst.isSystem());
		this.frame = inst.getDefinition();
		copy(inst, this);

	}

	/**
	 * Instanciates an instance. The instance is constructed using the frame and its default value as template.
	 * 
	 * @param frame the frame to instanciate from
	 */
	protected final void instanciate(@NotNull Frame frame) {

		for (String attrListName: frame.getAttributesListNames()) {
			List<Attribute> attrList = new ArrayList<>();
			for (DomainAttribute da: frame.getAttributes(attrListName)) {
				attrList.add(AttributeFactory.createDefaultAttribute(da));
			}
			getAttributeLists().put(attrListName, attrList);
		}

		for (String childListName: frame.getChildFrameListNames()) {
			Frame childType = frame.getChildFrameList(childListName).getType();
			List<Frame> childFrames = frame.getChildFrames(childListName);

			//this is necessary, because we allow same list names in different levels of the hierarchy. But these get flattened during instanciation:
			TypedList<Instance> instanceList;
			if (getObjectLists().containsKey(childListName)) {
				instanceList = getObjectLists().get(childListName);
			} else {
				instanceList = new TypedList<>(childType);
				getObjectLists().put(childListName, instanceList);
			}

			for (Frame childFrame: childFrames) {
				if (!childFrame.getName().startsWith("{")) {
					instanceList.add(Instance.instanciate(childFrame.getName(), childFrame));
				}
			}
		}

	}

	/**
	 * Add an instance in the respective list, as long as this list is defined for this type in the frame.
	 * 
	 * @param listname name of the object list
	 * @param instance the instance to add
	 */
	public void addChildInstance(@NotNull String listname, @NotNull Instance instance) {

		// if (!frame.containsChildFrame(listname, instance.getDefinition())) {
		// throw new IllegalArgumentException(String.format("Instance %s cannot contain instance of type %s, but the instance to add %s is
		// of this type.", this.getName(), instance.getDefinition().getName(), instance.getName()));
		// }

		if (!getObjectLists().containsKey(listname)
		        || ((TypedList<Instance>) getObjectLists().get(listname)).getType().isSuccessor(instance.getDefinition().getName())) {
			throw new IllegalArgumentException(String.format("Listname %s is not defined for this type of instance", listname));
		}

		Instance in = instance.clone();
		List<Instance> list = getObjectLists().get(listname); 
		list.remove(instance);
		list.add(in);
		setDirty(true);
	}

	@Override
	public Instance clone() {
		Instance in = Instance.copy(this);
		in.setDirty(true);
		return in;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Instance)) {
			return false;
		}
		return (((Instance) o).getName().equals(getName()));
	}

	/**
	 * Return the first attribute with the specified name that is matched in any attribute list.
	 * 
	 * @param attrName the attribute name
	 * @return the attribute or null if not found
	 */

	public Attribute getAttribute(@NotNull String attrName) {
		return getAttributeLists()
				.values()
				.stream()
				.flatMap(List::stream)
				.filter(attr -> attr.getName().equals(attrName))
				.findFirst().get();
	}

	/**
	 * Return the attribute with the specified name in the given list.
	 * 
	 * @param listname the name of the list
	 * @param attrName the attribute name
	 * @return the attribute or null if nothing was found 
	 */

	public  Attribute getAttribute(@NotNull String listname, @NotNull String attrName) {
		if (!getAttributeLists().containsKey(listname)) {
			throw new NoSuchElementException("List name " + listname + " does not exist");
		}

		return getAttributeLists()
				.get(listname)
				.stream()
				.filter(attr -> attr.getName().equals(attrName))
				.findFirst().get();
	}

	public boolean containsAttribute(@NotNull String listname, @NotNull String attrName) {
		try {
			return getAttribute(listname, attrName) != null;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	public boolean containsAttribute(@NotNull String attrName) {
		try {
			return getAttribute(attrName) != null;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	/**
	 * Return all attributes in the list with the specified name.
	 * 
	 * @param listname the name of the attribute list
	 * @return a list of attributes or empty list if the list is empty or does not exist
	 */

	public List<Attribute> getAttributes(@NotNull String listname) {

		if (!getAttributeLists().containsKey(listname)) {
			return new ArrayList<>();
		} else  {
			return getAttributeLists().get(listname);
		}

	}

	/**
	 * Return the attribute list names.
	 * 
	 * @return a list of names or emtpy list if none are defined
	 */
	public  List<String> getAttributesListNames() {
		return frame.getAttributesListNames();
	}

	/**
	 * Return the first matching instance with the given name, no matter in which list or subentity it is contained.
	 * This method looks recursively in all children, children of them, and so on.
	 * 
	 * @param name the name of the instance
	 * @return Instance the instance or null if none with the provided name exits
	 */
	public  Instance getChildInstance(@NotNull String name) {
		Instance inst = null;
		for (String listname: getChildInstanceListNames()) {
			if ((inst = this.getChildInstance(listname, name))!=null) {
				return inst;
			}
		}
		return null;
	}

	/**
	 * Return the instance with the specified name in the specified list.
	 * 
	 * @param listname the name of the list
	 * @param name the name of the instance
	 * @return the first matching instance or null if nothing found
	 */

	public  Instance getChildInstance(@NotNull String listname, @NotNull String name) {

		if (getObjectLists().containsKey(listname)) {
			return getObjectLists().get(listname).stream().filter(inst -> inst.getName().equals(name)).findFirst().get();
		}

		return null;
	}

	/**
	 * Return all child instance list names.
	 * 
	 * @return a list of strings or empty list if none is defined
	 */

	public List<String> getChildInstanceListNames() {
		return frame.getChildFrameListNames();
	}

	/**
	 * Return all instances contained by the specified list.
	 * 
	 * @param listname the name of the list
	 * @return list of instance or empty list if none present
	 */

	public  List<Instance> getChildInstances(@NotNull String listname) {

		if (!getObjectLists().containsKey(listname)) {
			return java.util.Collections.emptyList();
		}

		return getObjectLists().get(listname);

	}

	/**
	 * Return the frame that was used to create this instance.
	 * 
	 * @return Frame
	 */
	public  Frame getDefinition() {
		return frame;
	}

	/**
	 * Check if this instance has something in common with the specified frame.
	 * 
	 * @param frame the frame
	 * @return boolean true if the instance was created from this frame (possibly amongst others)
	 */

	public  boolean inheritsFrom(@NotNull Frame frame) {
		return this.inheritsFromOrIsOfType(frame.getName());
	}

	/**
	 * Check if this instance has an ancestor or is an instance of the given
	 * type.
	 * 
	 * @param frameName
	 *            the name of the frame
	 * @return boolean true if the instance was created from this frame
	 *         (possibly amongst others)
	 */
	public boolean inheritsFromOrIsOfType(@NotNull String frameName) {

		if (frame.getName().equals(frameName)) {
			return true;
		} else {
			return inheritsFrom(frameName);
		}
	}

	/**
	 * Check if this instance has an ancestor of the given type.
	 * 
	 * @param frameName
	 *            the name of the frame
	 * @return boolean true if the instance was created from this frame
	 *         (possibly amongst others)
	 */
	public boolean inheritsFrom(@NotNull String frameName) {

		try {
			Frame f = frame.getAncestor(frameName);
			System.out.println(f);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	/**
	 * Remove an attribute in the specified list.
	 * 
	 * @param listName
	 *            name of the attribute list
	 * @param attributeName
	 *            attribute name
	 * @return true if the attribute was removed, false otherwise, e.g. if the
	 *         list or attribute does not exist
	 */
	public  boolean removeAttribute(@NotNull String listName, @NotNull String attributeName) {

		if (getAttributeLists().containsKey(listName)) {
			return false;
		}

		Iterator<Attribute> iter = getAttributeLists().get(listName).iterator();
		while (iter.hasNext()) {
			Attribute a = iter.next();
			if (a.getName().equals(attributeName)) {
				iter.remove();
				return true;
			}
		}

		return false;

	}

	/**
	 * Remove an object in the respective list. No propagation.
	 * 
	 * @param listname the name of the object list
	 * @param instance the instance to remove
	 * @return true if the instance was removed or false if this was not possible, because the object does not exist (throws IllegalArgumentException if the frame does not define the list or object)
	 */

	public boolean removeChildInstance(@NotNull String listname, @NotNull String instanceName) {

		Instance instance = this.getChildInstance(listname, instanceName);

		if (!frame.containsChildFrame(listname, instance.getDefinition())) {
			throw new IllegalArgumentException(String.format("Instance %s cannot contain instance of type %s, but the instance to add %s is of this type.", this.getName(), instance.getDefinition().getName(), instance.getName()));
		}

		if (!getObjectLists().containsKey(listname)) {
			throw new IllegalArgumentException(String.format("Listname %s is not defined for this type of instance", listname));
		}

		Iterator<Instance> iter = getObjectLists().get(listname).iterator();
		while (iter.hasNext()) {
			if (iter.next().getName().equals(instanceName)) {
				iter.remove();
				setDirty(true);
				return true;
			}
		}

		return false;

	}

	@SuppressWarnings("unchecked")
	public <T> T resolvePath(Path<T> path) {

		try {

			// Terminal instance
			if (this.getName().equals(path.getName()) && path.isTerminal()) {
				return (T) this;
			}

			// Instance is in path but has successors - step into child object
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
				return (T) this.getChildInstance(path.getName(), path.next().getName()).resolvePath(path.next());
			}

			// found object
			if (getObjectLists().containsKey(path.getName())) {
				return (T) this.getObjectLists().get(path.getName());
			}
		} catch (NoSuchElementException e) {
			throw new GSimDefException(String.format("The path %s could not be resolved", path));
		}

		throw new GSimDefException(String.format("The path %s could not be resolved", path));

	}

	/**
	 * Sets the attribute that equals the specified attribute (by name). If the attribute is present in different lists, all attributes are replaced. If the attribute is not defined, it will not be added.
	 * 
	 * @param a the attribute to set
	 * @return true if the attribute was set, false if this was not possible, because, e.g. it was not defined by its frame
	 */
	public boolean setAttribute(@NotNull Attribute a) {

		boolean replaced = false;
		for (String listname : getAttributesListNames()) {
			DomainAttribute da = frame.getAttribute(listname, a.getName());
			if (da != null) {
				ListIterator<Attribute> iter = getAttributeLists().get(listname).listIterator();
				while (iter.hasNext()) {
					if (iter.next().getName().equals(a.getName())) {
						iter.set(a.clone());
						replaced = true;
					}
				}
			}
		}

		setDirty(replaced);

		return replaced;
	}

	/**
	 * Updates or inserts the attribute in the respective list. If the list is not defined, it is added.
	 * 
	 * @param listname the name of the attribute list
	 * @param attribute the attribute
	 */
	public void addOrSetAttribute(@NotNull String listname,@NotNull Attribute attribute) {

		List<Attribute> list = getAttributeLists().putIfAbsent(listname, new ArrayList<>());
		if (list == null) {
			list = getAttributeLists().get(listname);
		}
		list.remove(attribute);
		list.add(attribute.clone());
		setDirty(true);

	}

	/**
	 * Replaces the child instances in the given list. If the list does not exists, nothing happens; no further propagation to child instances on deeper levels.
	 * 
	 * @param listname the name of the list
	 * @param instance the instance
	 * @return true if the instance was replaced, false if it was not found
	 */
	public void setChildInstance(@NotNull String listname, @NotNull Instance instance) {

		if (!frame.definesChildList(listname)) {
			throw new IllegalArgumentException(String.format("Instance %s cannot contain instance in list %s, because its type %s does not define it.", this.getName(), listname, instance.getDefinition().getName()));
		}

		ListIterator<Instance> iter = getObjectLists().get(listname).listIterator();
		while (iter.hasNext()) {
			Instance inst = (Instance) iter.next();
			if (inst.getName().equals(instance.getName())) {
				iter.set(instance);
				setDirty(true);
			}
		}

	}

	/**
	 * Exchanges the original frame from which this instance is generated.
	 * 
	 * @param parent the new parent
	 */
	public void setFrame(@NotNull Frame parent) {

		Frame f = getDefinition();
		if (f.getName().equals(parent.getName())) {
			frame = parent;
		} else {
			f.replaceAncestor(parent);
		}

		modifyInheritanceByNewFrame(parent);
		setDirty(true);
	}

	/**
	 * Replaces the values of attribute identified by {@link Path} with the ones from the given.
	 * 
	 * @param path the path
	 * @param newValue the new attribute (values are copied)
	 * @return true if the attribute was replaced, false if this was not possible because it does not exist
	 */
	public boolean replaceChildAttribute(Path<Attribute> path, Attribute newValue) {
		Path<List<Attribute>> p = Path.withoutLast(path);
		ListIterator<Attribute> attIter = this.resolvePath(p).listIterator();
		while (attIter.hasNext()) {
			if (attIter.next().getName().equals(newValue.getName())) {
				attIter.set(newValue);
				setDirty(true);
				return true;
			}
		}
		return false;
	}

	public boolean addChildAttribute(Path<List<Attribute>> path, Attribute newValue) {
		List<Attribute> attr = this.resolvePath(path);
		if (attr != null) {
			attr.add(newValue.clone());
			setDirty(true);
		}
		return false;
	}

	public boolean addChildInstance(Path<TypedList<Instance>> path, Instance newValue) {
		TypedList<Instance> instance = this.resolvePath(path);
		if (instance != null) {
			instance.add(newValue.clone());
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
	public boolean removeChildAttribute(Path<Attribute> path) {

		Attribute attr = this.resolvePath(path);
		if (attr != null) {
			Path<List<Attribute>> attrListPath = Path.withoutLastAttributeOrObject(path, Path.Type.LIST);
			List<Attribute> attrList = this.resolvePath(attrListPath);
			if (attrList != null) {
				attrList.remove(attr);
				return true;
			}
		}
		return false;
	}

	public boolean removeChildInstance(Path<Instance> instancePath) {
		Instance inst = this.resolvePath(instancePath);
		if (inst != null) {
			Path<List<Instance>> childListPath = Path.withoutLastAttributeOrObject(instancePath, Path.Type.LIST);
			if (childListPath != null) {
				List<Instance> list = this.resolvePath(childListPath);
				if (list != null) {
					list.remove(inst);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sets the frame to the new frame, and then checks if any attributes and frames have been added or removed and updates the instance accordingly.
	 * Note: If a list in this instance is defined that the frame does not contain, this list is also removed.
	 * 
	 * @param frame the frame to set
	 */
	private void modifyInheritanceByNewFrame(Frame frame) {
		this.frame = frame; 

		for (String listname: frame.getAttributesListNames()) {
			if (!this.getAttributeLists().containsKey(listname)) {
				getAttributeLists().putIfAbsent(listname, new ArrayList<>());
				List<Attribute> attrList = getAttributeLists().get(listname);
				for (DomainAttribute da: frame.getAttributes(listname)) {
					attrList.add(AttributeFactory.createDefaultAttribute(da));
				}
			} else {
				Iterator<Attribute> iter = getAttributes(listname).iterator();
				while (iter.hasNext()) {
					String attName = iter.next().getName();
					if (!frame.getAttributes(listname).stream().anyMatch(a -> a.getName().equals(attName))) {
						iter.remove();
					}
				}
			}
		}

		//add only new lists, because if the list already exists, instances of the type may already exists, and it is not possible
		//to decide whether a new type should be instanciated or not
		for (String childListName: frame.getChildFrameListNames()) {

			if (!frame.getObjectLists().containsKey(childListName)) {
				Frame type = frame.getChildFrameList(childListName).getType();
				TypedList<Instance> instanceList = this.getObjectLists().put(childListName, new TypedList<Instance>(type));
				frame.getChildFrames(childListName).stream().forEach(f -> {
					if (!f.getName().startsWith("{")) {
						instanceList.add(Instance.instanciate(f.getName(), f));
					}
				});
			}
		}

		super.setDirty(true);

	}

	public boolean removeChildInstanceList(Path<TypedList<Instance>> objectListPath) {
		Path<Instance> containingInstancePath = Path.withoutLast(objectListPath);

		TypedList<Instance> list = this.resolvePath(objectListPath);
		Instance containingInstance = this.resolvePath(containingInstancePath);

		if (containingInstance != null && list != null) {
			containingInstance.removeDeclaredChildInstanceList(objectListPath.lastAsString());
			setDirty(true);
			return true;
		}

		return false;

	}

}
