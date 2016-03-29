package de.s2.gsim.def.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.validation.constraints.NotNull;

import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeFactory;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * An instance is the instantiation of a domain entity or frame. Instances extend frames by assigning concrete value to the attributes. Instances
 * represent similar to objects inheritance hierarchies. If an attributelist is not found in the current instance, it is looked in the superclasses
 * until it is found.
 *
 */
public class InstanceOLD extends UnitOLD {

    static final long serialVersionUID = 28914532862520612L;

    protected FrameOLD frame = null;

    /**
     * Copy constructor.
     * 
     * @param inst
     *            Instance
     */
    public InstanceOLD(InstanceOLD inst) {

        name = inst.getName();
        frame = inst.getDefinition();

        String[] attrListNames = inst.getAttributesListNames();

        for (int i = 0; i < attrListNames.length; i++) {
            attributeLists.put(attrListNames[i], new ArrayList());
            Attribute[] list = inst.getAttributes(attrListNames[i]);
            for (int j = 0; j < list.length; j++) {
                this.setAttribute(attrListNames[i], (Attribute) list[j].clone());
            }
        }

        String[] allchildren = inst.getChildInstanceListNames();

        for (int i = 0; i < allchildren.length; i++) {
            TypedListOLD list = inst.getDefinition().getList(allchildren[i]);// (TypedList)inst.getDefinition().objectLists.get(allchildren[i]);
            FrameOLD fr = list.getType();
            objectLists.put(allchildren[i], new TypedListOLD(fr));
            InstanceOLD[] instances = inst.getChildInstances(allchildren[i]);
            for (int j = 0; j < instances.length; j++) {
                addChildInstance(allchildren[i], new InstanceOLD(instances[j]));
            }
        }

        setSystem(inst.isSystem());
        setMutable(inst.isMutable());
        isDirty = false;
    }

    public InstanceOLD(InstanceOLD inst, String newName) {
        name = newName;
        frame = inst.getDefinition();

        instanciate(inst.getDefinition());

        String[] attrListNames = inst.getAttributesListNames();

        for (int i = 0; i < attrListNames.length; i++) {
            attributeLists.put(attrListNames[i], new ArrayList());
            Attribute[] list = inst.getAttributes(attrListNames[i]);
            for (int j = 0; j < list.length; j++) {
                this.setAttribute(attrListNames[i], (Attribute) list[j].clone());
            }
        }

        String[] allchildren = inst.getChildInstanceListNames();

        for (int i = 0; i < allchildren.length; i++) {
            TypedListOLD list = (TypedListOLD) inst.getDefinition().getObjectLists().get(allchildren[i]);
            FrameOLD fr = list.getType();
            objectLists.put(allchildren[i], new TypedListOLD(fr));

            InstanceOLD[] instances = inst.getChildInstances(allchildren[i]);
            if (instances != null) {
                for (int j = 0; j < instances.length; j++) {
                    addChildInstance(allchildren[i], new InstanceOLD(instances[j]));
                }
            }
        }

        setSystem(inst.isSystem());
        setMutable(inst.isMutable());
        setDirty(true);

    }

    /**
     * Proper constructor to construct an instance which 'instanciates' all its attributes and child-instances as defined in the frame it is defined
     * by.
     * 
     * @param name
     *            String
     * @param frame
     *            Frame
     * @param id
     *            int
     */
    public InstanceOLD(@NotNull String name, @NotNull FrameOLD frame) {
        this.name = name;
        this.frame = frame;
        instanciate(frame);
        setDirty(true);
    }

    protected InstanceOLD() {
        super();
    }

    /**
     * Add an instance in the respective list, as long as this list is defined for this type in the frame.
     * 
     * @param listname
     *            String
     * @param instance
     *            Instance
     */

    public void addChildInstance(String listname, InstanceOLD instance) {

        InstanceOLD n = (InstanceOLD) instance.clone(); // new Instance(instance);

        if (frame.containsChildFrame(listname, instance.getDefinition())) {
            ArrayList list = (ArrayList) objectLists.get(listname);
            if (list != null) {
                list.remove(n);
                list.add(n);
                setDirty(true);
                return;
            }
        }
    }

    /**
     * Use only if desperate.
     * 
     * @param newName
     *            String
     */

    public void changeName(String newName) {
        name = newName;
    }

    @Override
    public InstanceOLD clone() {
        InstanceOLD in = new InstanceOLD(this);

        String[] attrListNames = getAttributesListNames();

        for (int i = 0; i < attrListNames.length; i++) {
            Attribute[] list = getAttributes(attrListNames[i]);
            for (int j = 0; j < list.length; j++) {
                in.setAttribute(attrListNames[i], (Attribute) list[j].clone());
            }
        }

        String[] allchildren = getChildInstanceListNames();

        for (int i = 0; i < allchildren.length; i++) {
            InstanceOLD[] instances = getChildInstances(allchildren[i]);
            for (int j = 0; j < instances.length; j++) {
                in.addChildInstance(allchildren[i], new InstanceOLD(instances[j]));
            }
        }

        in.setSystem(isSystem());
        in.setMutable(isMutable());
        in.setDirty(true);
        return in;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InstanceOLD)) {
            return false;
        }
        return (((InstanceOLD) o).getName().equals(getName()));
    }

    /**
     * Return an attribute with the specified name. Note that attributes with the same names can be defined in different lists.
     * 
     * @param attrName
     *            String
     * @return Attribute
     */

    public  Attribute getAttribute(String attrName) {
        String[] lists = getAttributesListNames();
        for (int i = 0; i < lists.length; i++) {
            String list = lists[i];
            Attribute da = this.getAttribute(list, attrName);
            if (da != null) {
                return da;
            }
        }
        return null;
    }

    /**
     * Return the attribute with the specified name in the specified list.
     * 
     * @param listname
     *            String
     * @param attrName
     *            String
     * @return Attribute
     */

    public  Attribute getAttribute(String listname, String attrName) {
        if (attributeLists.containsKey(listname)) {
            ArrayList list = (ArrayList) attributeLists.get(listname);
            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                Attribute attr = (Attribute) iter.next();
                if (attr.getName().equalsIgnoreCase(attrName)) {
                    return attr;
                }
            }
        }
        return null;
    }

    /**
     * Return the attributes that sit in the list with the specified name.
     * 
     * @param listname
     *            String
     * @return Attribute[]
     */

    public  Attribute[] getAttributes(String listname) {

        if (attributeLists.containsKey(listname)) {
            ArrayList list = (ArrayList) attributeLists.get(listname);
            Attribute[] atts = new Attribute[list.size()];
            list.toArray(atts);
            return atts;
        }
        return null;
    }

    /**
     * Return the attribute list-names.
     * 
     * @return String[]
     */

    public  String[] getAttributesListNames() {
        return frame.getAttributesListNames();
    }

    /**
     * Return any child instance with the specified name. However, there may be child-instances with the same name in different lists.
     * 
     * @param name
     *            String
     * @return Instance
     */

    public  InstanceOLD getChildInstance(String name) {
        String[] listNames = getChildInstanceListNames();
        InstanceOLD result = null;
        for (int i = 0; i < listNames.length; i++) {
            result = this.getChildInstance(listNames[i], name);
            if (result != null) {
                return result;
            }
        }
        return result;
    }

    /**
     * Return the instance with the specified name in the specified list.
     * 
     * @param listname
     *            String
     * @param name
     *            String
     * @return Instance
     */

    public  InstanceOLD getChildInstance(String listname, String name) {
        ArrayList list = (ArrayList) objectLists.get(listname);
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                InstanceOLD instance = (InstanceOLD) iter.next();
                if (instance.getName().equals(name)) {
                    return instance;
                }
            }
        } else {
            Iterator iter = objectLists.values().iterator();
            while (iter.hasNext()) {
                ArrayList l = (ArrayList) iter.next();
                Iterator it = l.iterator();
                while (it.hasNext()) {
                    InstanceOLD inst = (InstanceOLD) it.next();
                    InstanceOLD pass = inst.getChildInstance(listname, name);
                    if (pass != null) {
                        return pass;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return the names of the lists that contain instances.
     * 
     * @return String[]
     */

    public  String[] getChildInstanceListNames() {
        return frame.getChildFrameListNames();
    }

    /**
     * Return all instances contained by the specified list.
     * 
     * @param listname
     *            String
     * @return Instance[]
     */

    public  InstanceOLD[] getChildInstances(String listname) {
        listname = listname.trim();
        ArrayList list = (ArrayList) objectLists.get(listname.trim());
        if (list != null) {
            InstanceOLD[] in = new InstanceOLD[list.size()];
            list.toArray(in);
            return in;
        }
        Iterator iter = objectLists.values().iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            ArrayList l = (ArrayList) o;
            for (int i = 0; i < l.size(); i++) {
                InstanceOLD inst = (InstanceOLD) l.get(i);
                InstanceOLD[] pass = inst.getChildInstances(listname);
                if (pass != null) {
                    return pass;
                }
            }
        }

        return new InstanceOLD[0];
    }

    /**
     * Return the frame that was used to create this instance.
     * 
     * @return Frame
     */

    public  FrameOLD getDefinition() {
        return frame;
    }

    /**
     * Check if this instance has something in common with the specified frame.
     * 
     * @param f
     *            Frame
     * @return boolean
     */

    public  boolean inheritsFrom(FrameOLD f) {
        return this.inheritsFrom(f.getTypeName());
    }

    /**
     * Check if this instance has something in common with the frame with the specified name.
     * 
     * @param f
     *            String
     * @return boolean
     */

    public  boolean inheritsFrom(String f) {

        if (frame.getTypeName().equals(f)) {
            return true;
        } else {
            FrameOLD anyOther = frame.getAncestor(f);
            if (anyOther != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove an attribute in the specified list.
     * 
     * @param listName
     *            String
     * @param attributeName
     *            String
     */

    public  void removeAttribute(String listName, String attributeName) {
        ArrayList list = (ArrayList) attributeLists.get(listName);
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                Attribute a = (Attribute) iter.next();
                if (a.getName().equals(attributeName)) {
                    iter.remove();
                    setDirty(true);
                }
            }
        }
    }

    /**
     * Remove a contained object in the respective list.
     * 
     * @param listname
     *            String
     * @param instance
     *            Instance
     */

    public  void removeChildInstance(String listname, String instanceName) {
        InstanceOLD instance = this.getChildInstance(listname, instanceName);
        if (frame.containsChildFrame(listname, instance.getDefinition())) {
            ArrayList list = (ArrayList) objectLists.get(listname);
            if (list != null) {
                ListIterator iter = list.listIterator();
                while (iter.hasNext()) {
                    InstanceOLD inst = (InstanceOLD) iter.next();
                    if (inst.getName().equals(instance.getName())) {
                        iter.remove();
                        setDirty(true);
                    }
                }
            } else {
                Iterator iter = objectLists.values().iterator();
                while (iter.hasNext()) {
                    InstanceOLD inst = (InstanceOLD) iter.next();
                    inst.removeChildInstance(listname, instanceName);
                }
            }
        }

    }

    /**
     * Gets the object that is specified by this path, either an attribute or an instance. Similar to the corresponding method in {@link} Frame.
     * 
     * @param path
     *            String[]
     * @return Object
     */

    public  Object resolveName(String[] path) {

        if (path.length == 0) {
            return null;
        }

        if (attributeLists.containsKey(path[0]) && path.length == 2) {
            return getDeclaredAttribute(path[0].trim(), path[1].trim());
        } else if (attributeLists.containsKey(path[0]) && path.length == 1) {
            return attributeLists.get(path[0]);
        }

        if (objectLists.containsKey(path[0]) && path.length > 0) {
            if (path.length == 2) {
                return this.getChildInstance(path[0], path[1]);
            } else if (path.length > 2) {
                InstanceOLD inst = this.getChildInstance(path[0], path[1]);
                if (inst != null) {
                    String[] nPath = new String[path.length - 2];
                    for (int i = 2; i < path.length; i++) {
                        nPath[i - 2] = path[i];
                    }
                    return inst.resolveName(nPath);
                }
            } else {
                if (super.getObjectLists().containsKey(path[0])) {
                    ArrayList list = (ArrayList) super.getObjectLists().get(path[0]);
                    return list;
                } else if (super.getAttributeLists().containsKey(path[0])) {
                    ArrayList list = (ArrayList) super.getAttributeLists().get(path[0]);
                    return list;
                }
            }
        }
        return null;

    }

    /**
     * Sets the attribute that *equals* the specified attribute (currently by name). As there can be attributes with the same name in different lists,
     * be careful.
     * 
     * @param a
     *            Attribute
     */

    public  void setAttribute(Attribute a) {

        for (String listname : getAttributesListNames()) {
            DomainAttribute da = frame.getAttribute(listname, a.getName());
            if (da != null) {
                ArrayList list = (ArrayList) attributeLists.get(listname);
                ListIterator iter = list.listIterator();
                while (iter.hasNext()) {
                    Attribute attr = (Attribute) iter.next();
                    if (attr.getName().equals(a.getName())) {
                        iter.remove();
                        iter.add(a.clone());
                        return;
                    }
                }
                // erstes mal kommt bis hier =>
                list.add(a.clone());
            }
        }
    }

    /**
     * Set the attribute that equals the specified attribute to this attribute in the respective list.
     * 
     * @param listname
     *            String
     * @param a
     *            Attribute
     */

    public  void setAttribute(String listname, Attribute a) {
        if (attributeLists.containsKey(listname)) {
            ArrayList list = (ArrayList) attributeLists.get(listname);
            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                Attribute attr = (Attribute) iter.next();
                if (attr.getName().equals(a.getName())) {
                    iter.remove();
                    iter.add(a);
                    setDirty(true);
                    return;
                }
            }
            // attribute doesn't seem to be in the list, so just add it
            list.add(a);
            return;
        }

        ArrayList l = new ArrayList();
        l.add(a);
        attributeLists.put(listname, l);
    }

    /**
     * Sets the instance somewhere where an instance that equals the instance is found
     * 
     * @param instance
     *            Instance
     */

    public  void setChildInstance(InstanceOLD instance) {
        String[] names = getChildInstanceListNames();
        for (int i = 0; i < names.length; i++) {
            this.setChildInstance(names[i], instance);
            InstanceOLD[] children = getChildInstances(names[i]);
            for (int j = 0; j < children.length; j++) {
                children[j].setChildInstance(instance);
            }
        }
    }

    /**
     * If there exists somewhere as list with the specified name having an instance that equals the specified instance, it sets it to this instance.
     * 
     * @param listname
     *            String
     * @param instance
     *            Instance
     */

    public  void setChildInstance(String listname, InstanceOLD instance) {

        if (frame.definesChildList(listname)) {
            ArrayList list = (ArrayList) objectLists.get(listname);
            if (list != null) {
                ListIterator iter = list.listIterator();
                while (iter.hasNext()) {
                    InstanceOLD inst = (InstanceOLD) iter.next();
                    if (inst.getName().equals(instance.getName())) {
                        iter.set(instance);
                        setDirty(true);
                    }
                }
            } else {
                Iterator iter = objectLists.values().iterator();
                while (iter.hasNext()) {
                    ArrayList list2 = (ArrayList) iter.next();
                    for (int i = 0; i < list2.size(); i++) {
                        InstanceOLD inst = (InstanceOLD) list2.get(i);
                        inst.setChildInstance(listname, instance);
                    }
                }
            }
        }
    }

    /**
     * Set a parent somewhere in the definition frame hierarchy. By ref!
     */
    public  void setFrame(FrameOLD parent) {
        if (parent == null) {
            return;
        }

        FrameOLD n = parent;
        FrameOLD f = getDefinition();
        if (f.getTypeName().equals(parent.getTypeName())) {
            frame = n;
        } else {
            f.setAncestor(n);
        }
        modifyInheritanceByNewFrame(n);
        setDirty(true);
    }

    /**
     * Proper 'instanciation'. The instance is constructed using the frame and its default value as template.
     * 
     * @param frame
     *            Frame
     */
    protected  void instanciate(FrameOLD frame) {
        this.frame = frame; // by ref???
        String[] listnames = frame.getAttributesListNames();
        for (int i = 0; i < listnames.length; i++) {
            List<Attribute> arl = new ArrayList<>();
            DomainAttribute[] d = frame.getAttributes(listnames[i]);

            for (int j = 0; j < d.length; j++) {
                Attribute a = AttributeFactory.createDefaultAttribute(d[j]);
                arl.add(a);
            }
            attributeLists.put(listnames[i], arl);
        }

        String[] allchildren = frame.getChildFrameListNames();
        for (int i = 0; i < allchildren.length; i++) {
            String listname = allchildren[i];
            TypedListOLD l = frame.getList(allchildren[i]);
            FrameOLD def = l.getType();
            FrameOLD[] dep = frame.getChildFrames(allchildren[i]);
            TypedListOLD list = new TypedListOLD(def);

            for (int j = 0; j < dep.length; j++) {
                InstanceOLD ii = null;
                if (dep[j] != null && !dep[j].getTypeName().startsWith("{")) {
                    ii = new InstanceOLD(dep[j].getTypeName(), dep[j]);
                }

                if (objectLists.containsKey(listname)) {
                    list = (TypedListOLD) objectLists.get(listname);
                }
                if (ii != null) {
                    list.add(ii);
                }
            }
            objectLists.put(listname, list);
        }
        isMutable = frame.isMutable();
        isSystem = frame.isSystem();
    }

    private Attribute getDeclaredAttribute(String listname, String attrName) {
        if (attributeLists.containsKey(listname)) {
            ArrayList list = (ArrayList) attributeLists.get(listname);
            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                Attribute attr = (Attribute) iter.next();
                if (attr.getName().equals(attrName)) {
                    return attr;
                }
            }
        }
        return null;
    }

    /**
     * Sets the frame to the new frame, and then checks if any attributes and frames have been added or removed and updates the instance accordingly.
     * No values are changed when their carriers were already defined in the instance.
     * 
     * @param frame
     *            Frame
     */
    private void modifyInheritanceByNewFrame(FrameOLD frame) {
        this.frame = frame; // by ref???
        String[] listnames = frame.getAttributesListNames();
        for (int i = 0; i < listnames.length; i++) {
            if (!attributeLists.containsKey(listnames[i])) {
                ArrayList arl = new ArrayList();
                DomainAttribute[] d = frame.getAttributes(listnames[i]);
                for (int j = 0; j < d.length; j++) {
                    Attribute a = AttributeFactory.createDefaultAttribute(d[j]);
                    arl.add(a);
                }
                attributeLists.put(listnames[i], arl);
            } else {
                // check for removed attributes
                DomainAttribute[] d = frame.getAttributes(listnames[i]);
                Attribute[] a = getAttributes(listnames[i]);
                for (int j = 0; j < a.length; j++) {
                    boolean contained = false;
                    for (int k = 0; k < d.length; k++) {
                        if (d[k].getName().equals(a[j].getName())) {
                            contained = true;
                        }
                    }
                    if (!contained) {
                        removeAttribute(listnames[i], a[j].getName());
                    }
                }

            }
        }

        String[] allchildren = frame.getChildFrameListNames();
        for (int i = 0; i < allchildren.length; i++) {
            if (!objectLists.containsKey(allchildren[i])) {
                String listname = allchildren[i];
                FrameOLD[] dep = frame.getChildFrames(allchildren[i]);
                for (int j = 0; j < dep.length; j++) {
                    InstanceOLD ii = null;
                    if (dep[j] != null && !dep[j].getTypeName().startsWith("{")) {
                        ii = new InstanceOLD(dep[j].getTypeName(), dep[j]);
                    }
                    TypedListOLD list = (TypedListOLD) objectLists.get(listname);

                    if (list == null) {
                        TypedListOLD l = frame.getList(listname);
                        FrameOLD fr = l.getType();
                        list = new TypedListOLD(fr);
                    }

                    if (ii != null) {
                        list.add(ii);
                    }
                    objectLists.put(listname, list);
                }
            }
        }

        isMutable = frame.isMutable();
        isSystem = frame.isSystem();
    }

}
