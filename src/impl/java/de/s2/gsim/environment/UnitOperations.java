package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.Iterator;

import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;
import de.s2.gsim.util.Utils;

/**
 * This class provides common operations on {@link Unit}s.
 * 
 * @author Stephan
 *
 */
public abstract class UnitOperations {

	/**
	 * Copies all properties (instances and attributes) from one instance to another.
	 * 
	 * @param from the instance to copy from
	 * @param to the instance to copy into
	 */
	public static void copyProperties(Instance from, Instance to) {

		String[] attrListNames = from.getAttributesListNames();

		for (int i = 0; i < attrListNames.length; i++) {
			Attribute[] list = from.getAttributes(attrListNames[i]);
			for (int j = 0; j < list.length; j++) {
				to.addOrSetAttribute(attrListNames[i], (Attribute) list[j].clone());
			}
		}

		String[] allchildren = from.getChildInstanceListNames();

		for (int i = 0; i < allchildren.length; i++) {
			Instance[] instances = from.getChildInstances(allchildren[i]);
			for (int j = 0; j < instances.length; j++) {
				to.addChildInstance(allchildren[i], new Instance(instances[j]));
			}
		}

	}

	/**
	 * Remove the whole bunch of attributes in the list. The list remains
	 * defined.
	 * 
	 * @param listName
	 *            String
	 */
	public static void removeAllAttributes(Instance inst, String listName) {
		ArrayList list = (ArrayList) inst.getAttributeLists().get(listName);
		if (list != null) {
			list.clear();
			inst.setDirty(true);
		}
	}

	/**
	 * Remove an attribute. If the attribute is inherited, nothing happens,
	 * because then it must be deleted from the parent!
	 * 
	 * @param pathToChild
	 *            String[]
	 * @param a
	 *            String
	 */
	public static void removeAttribute(Frame f, String[] pathToChild, String a) {
		// the last part of the path must be the list to delete from
		if (pathToChild.length == 1) {
			ArrayList list = (ArrayList) f.getAttributeLists().get(
					pathToChild[0]);
			if (a != null) {
				if (list != null) {
					if (f.isDeclaredAttribute(pathToChild[0], a)) {
						DomainAttribute att = f.getAttribute(pathToChild[0], a);
						f.removeAttribute(pathToChild[0], att);
					}
				}
			}
		} else if (pathToChild.length > 1) {
			String list = pathToChild[0];
			String[] newPath = Utils.removeFromArray(pathToChild,
					pathToChild[0]);

			String[] childrenNames = f.getChildFrameListNames();
			for (int i = 0; i < childrenNames.length; i++) {
				if (childrenNames[i].equals(list)) {
					Frame[] children = f.getChildFrames(childrenNames[i]);
					for (int j = 0; j < children.length; j++) {
						removeAttribute(
								children[j],
								Utils.removeFromArray(newPath,
										children[j].getTypeName()), a);
					}
				}
			}
		} else if (pathToChild.length == 0) {
			// throw new RuntimeException("Owner of attribute " + a.getName() +"
			// not found..");
		}
		f.setDirty(true);
	}

	public static void removeAttribute(Instance inst, String[] pathToChild,
			String a) {
		if (pathToChild.length == 1) {
			ArrayList list = (ArrayList) inst.getAttributeLists().get(
					pathToChild[0]);
			if (a != null) {
				if (list != null) {
					inst.removeAttribute(pathToChild[0], a);
				}
			}
		} else if (pathToChild.length > 1) {
			String list = pathToChild[0];
			String[] newPath = Utils.removeFromArray(pathToChild,
					pathToChild[0]);

			String[] childrenNames = inst.getChildInstanceListNames();
			for (int i = 0; i < childrenNames.length; i++) {
				if (childrenNames[i].equals(list)) {
					Instance[] children = inst
							.getChildInstances(childrenNames[i]);
					for (int j = 0; j < children.length; j++) {
						removeAttribute(
								children[j],
								Utils.removeFromArray(newPath,
										children[j].getName()), a);
					}
				}
			}
		}
		inst.setDirty(true);
	}

	/**
	 * Remove a frame specified by the path. If the path is not defined in this
	 * level, do nothing. A path is defined by a list of names containing
	 * list-names and frame- or attribute-names, e.g.
	 * frame_list1-frame1-frame_list2-frame2 specified a frame that is contained
	 * in the list list2 which is contained by a frame contained by this frame
	 * in the list with name frame_list1.
	 * 
	 * @param pathToChild
	 *            String[]
	 * @param childName
	 *            String
	 */
	public static void removeChildFrame(Frame x, String[] pathToChild,
			String childName) {

		if (pathToChild.length == 1) {
			ArrayList list = (ArrayList) x.getObjectLists().get(pathToChild[0]);
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				Frame f = (Frame) iter.next();
				if (f.getTypeName().equals(childName)) {
					iter.remove();
					x.setDirty(true);
				}
			}
		} else if (pathToChild.length > 1) {
			String list = pathToChild[0];
			String[] newPath = Utils.removeFromArray(pathToChild,
					pathToChild[0]);

			String[] childrenNames = x.getChildFrameListNames();
			for (int i = 0; i < childrenNames.length; i++) {
				if (childrenNames[i].equals(list)) {
					Frame[] children = x.getChildFrames(childrenNames[i]);
					for (int j = 0; j < children.length; j++) {
						removeChildFrame(
								children[j],
								Utils.removeFromArray(newPath,
										children[j].getTypeName()), childName);
					}
				}
			}
		} else if (pathToChild.length == 0) {
			throw new RuntimeException("List not found..");
		}
	}

	/**
	 * Removes a child instance whose position is defined by a path. Same
	 * path-logic as in {@link} class applies.
	 * 
	 * @param pathToChild
	 *            String[]
	 * @param childName
	 *            String
	 */
	public static void removeChildInstance(Instance inst, String[] pathToChild,
			String childName) {
		// the last part of the path must be the list to append to
		if (pathToChild.length == 1) {
			ArrayList list = (ArrayList) inst.getObjectLists().get(
					pathToChild[0]);
			if (list != null) {
				Iterator iter = list.iterator();
				while (iter.hasNext()) {
					Instance in = (Instance) iter.next();
					if (in.getName().equals(childName)) {
						iter.remove();
						inst.setDirty(true);
					}
				}
			}
		} else if (pathToChild.length > 1) {
			String list = pathToChild[0];
			String[] newPath = Utils.removeFromArray(pathToChild,
					pathToChild[0]);

			String[] childrenNames = inst.getChildInstanceListNames();
			for (int i = 0; i < childrenNames.length; i++) {
				if (childrenNames[i].equals(list)) {
					Instance[] children = inst
							.getChildInstances(childrenNames[i]);
					for (int j = 0; j < children.length; j++) {
						removeChildInstance(
								children[j],
								Utils.removeFromArray(newPath,
										children[j].getName()), childName);
					}
				}
			}
		} else if (pathToChild.length == 0) {
			throw new RuntimeException("List not found..");
		}
	}

	/**
	 * Set an attribute that is described by a certain path to it. A path is
	 * defined by a list of names containing list-names and frame- or
	 * attribute-names, e.g. frame_list1-frame1-frame_list2-frame2 specified a
	 * frame that is contained in the list list2 which is contained by a frame
	 * contained by this frame in the list with name frame_list1.
	 * 
	 * @param pathToChild
	 *            String[]
	 * @param a
	 *            DomainAttribute
	 */
    @Deprecated
	public static void setChildAttribute(Frame f, String[] pathToChild,
			DomainAttribute a) {
		if (pathToChild.length == 1) {
			ArrayList list = (ArrayList) f.getAttributeLists().get(
					pathToChild[0]);
			if (a != null) {
				if (list == null) {
					f.addOrSetAttribute(pathToChild[0], a);
				} else {
					f.addOrSetAttribute(pathToChild[0], a);
				}
				f.setDirty(true);
			}
		} else if (pathToChild.length > 1) {
			String list = pathToChild[0];
			String[] newPath = Utils.removeFromArray(pathToChild,
					pathToChild[0]);

			String[] childrenNames = f.getChildFrameListNames();
			for (int i = 0; i < childrenNames.length; i++) {
				if (childrenNames[i].equals(list)) {
					Frame[] children = f.getChildFrames(childrenNames[i]);
					for (int j = 0; j < children.length; j++) {
						setChildAttribute(
								children[j],
								Utils.removeFromArray(newPath,
										children[j].getTypeName()), a);
					}
				}
			}
		} else if (pathToChild.length == 0) {
			// throw new RuntimeException("Owner of attribute " + a.getName() +"
			// not found..");
		}

	}

	/**
	 * Set the attribute that is specified by path only. If the attribute does
	 * not exist yet, it is added (given that its type is defined for this sort
	 * of list in the frame).
	 * 
	 * @param pathToChild
	 *            String[]
	 * @param a
	 *            Attribute
	 */
	public static void setChildAttribute(Instance inst, String[] pathToChild,
			Attribute a) {
		// the last part of the path must be the list to append to
		if (pathToChild.length == 1) {
			ArrayList list = (ArrayList) inst.getAttributeLists().get(
					pathToChild[0]);
			if (a != null && list != null) {
				for (int i = 0; i < list.size(); i++) {
					if (((Attribute) list.get(i)).getName().equals(a.getName())) {
						list.set(i, a);
						inst.setDirty(true);
						return;
					}
				}
				// not existent yet
				list.add(a);
			}
		} else if (pathToChild.length > 1) {
			String list = pathToChild[0];
			String[] newPath = Utils.removeFromArray(pathToChild,
					pathToChild[0]);

			String[] childrenNames = inst.getChildInstanceListNames();
			for (int i = 0; i < childrenNames.length; i++) {
				if (childrenNames[i].equals(list)) {
					Instance[] children = inst
							.getChildInstances(childrenNames[i]);
					for (int j = 0; j < children.length; j++) {
						setChildAttribute(
								children[j],
								Utils.removeFromArray(newPath,
										children[j].getName()), a);
					}
				}
			}
		} else if (pathToChild.length == 0) {
			throw new RuntimeException("Owner of attribute " + a.getName()
					+ " not found..");
		}

	}

	/**
	 * Set a frame specified by a certain path. Copy the list in case the path
	 * belongs to an ancestor. A path is defined by a list of names containing
	 * list-names and frame- or attribute-names, e.g.
	 * frame_list1-frame1-frame_list2-frame2 specified a frame that is contained
	 * in the list list2 which is contained by a frame contained by this frame
	 * in the list with name frame_list1.
	 * 
	 * @param pathToChild
	 *            String[]
	 * @param a
	 *            Frame
	 */
	public static void setChildFrame(Frame x, String[] pathToChild, Frame a) {
		// the last part of the path must be the list to append to
		if (pathToChild.length == 1) {
			ArrayList list = (ArrayList) x.getObjectLists().get(pathToChild[0]);
			if (list != null) {
				a = (Frame) a.clone();
				Iterator iter = list.iterator();
				while (iter.hasNext()) {
					Frame f = (Frame) iter.next();
					if (f.getTypeName().equals(a.getTypeName())) {
						iter.remove();
					}
				}
				list.remove(a);
				list.add(a);
			} else {
				x.addChildFrame(pathToChild[0], a);
			}
			x.setDirty(true);
		} else if (pathToChild.length > 1) {
			String list = pathToChild[0];
			String[] newPath = Utils.removeFromArray(pathToChild,
					pathToChild[0]);
			String[] childrenNames = x.getChildFrameListNames();
			for (int i = 0; i < childrenNames.length; i++) {
				if (childrenNames[i].equals(list)) {
					Frame[] children = x.getChildFrames(childrenNames[i]);
					for (int j = 0; j < children.length; j++) {
						setChildFrame(
								children[j],
								Utils.removeFromArray(newPath,
										children[j].getTypeName()), a);
					}
				}
			}
		} else if (pathToChild.length == 0) {
			throw new RuntimeException("List not found..");
		}
	}

	/**
	 * Sets the instance equaliling this instance in its place specified by the
	 * path. If the instance doesn't exist yet, it is added.
	 * 
	 * @param pathToChild
	 *            String[]
	 * @param a
	 *            Instance
	 */
	public static void setChildInstance(Instance inst, String[] pathToChild,
			Instance a) {
		// the last part of the path must be the list to append to
		if (pathToChild.length == 1) {
			ArrayList list = (ArrayList) inst.getObjectLists().get(
					pathToChild[0]);
			if (list != null) {
				list.remove(a);
				a = new Instance(a);
				list.add(a);
			} else {
				inst.addChildInstance(pathToChild[0], a);
			}
		} else if (pathToChild.length > 1) {
			String list = pathToChild[0];
			String[] newPath = Utils.removeFromArray(pathToChild,
					pathToChild[0]);

			String[] childrenNames = inst.getChildInstanceListNames();
			for (int i = 0; i < childrenNames.length; i++) {
				if (childrenNames[i].equals(list)) {
					Instance[] children = inst
							.getChildInstances(childrenNames[i]);
					for (int j = 0; j < children.length; j++) {
						setChildInstance(
								children[j],
								Utils.removeFromArray(newPath,
										children[j].getName()), a);
					}
				}
			}
		} else if (pathToChild.length == 0) {
			throw new RuntimeException("List not found..");
		}
	}

}
