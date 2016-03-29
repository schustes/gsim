package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeFactory;
import de.s2.gsim.objects.attribute.DomainAttribute;

/**
 * An instance is the instantiation of a domain entity or frame. Instances extend frames by assigning concrete value to the attributes. Instances
 * represent similar to objects inheritance hierarchies. If an attributelist is not found in the current instance, it is looked in the superclasses
 * until it is found.
 *
 */
public class Instance extends Unit {

    static final long serialVersionUID = 28914532862520612L;

    protected Frame frame = null;

    /**
     * Copy constructor.
     * 
     * @param inst
     *            Instance
     */
    public Instance(Instance inst) {

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
            TypedList list = inst.getDefinition().getList(allchildren[i]);// (TypedList)inst.getDefinition().objectLists.get(allchildren[i]);
            Frame fr = list.getType();
            objectLists.put(allchildren[i], new TypedList(fr));
            Instance[] instances = inst.getChildInstances(allchildren[i]);
            for (int j = 0; j < instances.length; j++) {
                addChildInstance(allchildren[i], new Instance(instances[j]));
            }
        }

        setSystem(inst.isSystem());
        setMutable(inst.isMutable());
        isDirty = false;
    }

    public Instance(Instance inst, String newName) {
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
            TypedList list = (TypedList) inst.getDefinition().getObjectLists().get(allchildren[i]);
            Frame fr = list.getType();
            objectLists.put(allchildren[i], new TypedList(fr));

            Instance[] instances = inst.getChildInstances(allchildren[i]);
            if (instances != null) {
                for (int j = 0; j < instances.length; j++) {
                    addChildInstance(allchildren[i], new Instance(instances[j]));
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
    public Instance(String name, Frame frame) {
        this.name = name;
        this.frame = frame;
        instanciate(frame);
        setDirty(true);
    }

    protected Instance() {
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

    public void addChildInstance(String listname, Instance instance) {

        Instance n = (Instance) instance.clone(); // new Instance(instance);

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
    public Object clone() {
        Instance in = new Instance(this);

        String[] attrListNames = getAttributesListNames();

        for (int i = 0; i < attrListNames.length; i++) {
            Attribute[] list = getAttributes(attrListNames[i]);
            for (int j = 0; j < list.length; j++) {
                in.setAttribute(attrListNames[i], (Attribute) list[j].clone());
            }
        }

        String[] allchildren = getChildInstanceListNames();

        for (int i = 0; i < allchildren.length; i++) {
            Instance[] instances = getChildInstances(allchildren[i]);
            for (int j = 0; j < instances.length; j++) {
                in.addChildInstance(allchildren[i], new Instance(instances[j]));
            }
        }

        in.setSystem(isSystem());
        in.setMutable(isMutable());
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

    public  Instance getChildInstance(String name) {
        String[] listNames = getChildInstanceListNames();
        Instance result = null;
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

    public  Instance getChildInstance(String listname, String name) {
        ArrayList list = (ArrayList) objectLists.get(listname);
        if (list != null) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                Instance instance = (Instance) iter.next();
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
                    Instance inst = (Instance) it.next();
                    Instance pass = inst.getChildInstance(listname, name);
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

    public  Instance[] getChildInstances(String listname) {
        listname = listname.trim();
        ArrayList list = (ArrayList) objectLists.get(listname.trim());
        if (list != null) {
            Instance[] in = new Instance[list.size()];
            list.toArray(in);
            return in;
        }
        Iterator iter = objectLists.values().iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            ArrayList l = (ArrayList) o;
            for (int i = 0; i < l.size(); i++) {
                Instance inst = (Instance) l.get(i);
                Instance[] pass = inst.getChildInstances(listname);
                if (pass != null) {
                    return pass;
                }
            }
        }

        return new Instance[0];
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
     * @param f
     *            Frame
     * @return boolean
     */

    public  boolean inheritsFrom(Frame f) {
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
            Frame anyOther = frame.getAncestor(f);
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
        Instance instance = this.getChildInstance(listname, instanceName);
        if (frame.containsChildFrame(listname, instance.getDefinition())) {
            ArrayList list = (ArrayList) objectLists.get(listname);
            if (list != null) {
                ListIterator iter = list.listIterator();
                while (iter.hasNext()) {
                    Instance inst = (Instance) iter.next();
                    if (inst.getName().equals(instance.getName())) {
                        iter.remove();
                        setDirty(true);
                    }
                }
            } else {
                Iterator iter = objectLists.values().iterator();
                while (iter.hasNext()) {
                    Instance inst = (Instance) iter.next();
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
                Instance inst = this.getChildInstance(path[0], path[1]);
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

    public  void setChildInstance(Instance instance) {
        String[] names = getChildInstanceListNames();
        for (int i = 0; i < names.length; i++) {
            this.setChildInstance(names[i], instance);
            Instance[] children = getChildInstances(names[i]);
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

    public  void setChildInstance(String listname, Instance instance) {

        if (frame.definesChildList(listname)) {
            ArrayList list = (ArrayList) objectLists.get(listname);
            if (list != null) {
                ListIterator iter = list.listIterator();
                while (iter.hasNext()) {
                    Instance inst = (Instance) iter.next();
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
                        Instance inst = (Instance) list2.get(i);
                        inst.setChildInstance(listname, instance);
                    }
                }
            }
        }
    }

    /**
     * Set a parent somewhere in the definition frame hierarchy. By ref!
     */
    public  void setFrame(Frame parent) {
        if (parent == null) {
            return;
        }

        Frame n = parent;
        Frame f = getDefinition();
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
    protected  void instanciate(Frame frame) {
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
            TypedList l = frame.getList(allchildren[i]);
            Frame def = l.getType();
            Frame[] dep = frame.getChildFrames(allchildren[i]);
            TypedList list = new TypedList(def);

            for (int j = 0; j < dep.length; j++) {
                Instance ii = null;
                if (dep[j] != null && !dep[j].getTypeName().startsWith("{")) {
                    ii = new Instance(dep[j].getTypeName(), dep[j]);
                }

                if (objectLists.containsKey(listname)) {
                    list = (TypedList) objectLists.get(listname);
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
    private void modifyInheritanceByNewFrame(Frame frame) {
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
                Frame[] dep = frame.getChildFrames(allchildren[i]);
                for (int j = 0; j < dep.length; j++) {
                    Instance ii = null;
                    if (dep[j] != null && !dep[j].getTypeName().startsWith("{")) {
                        ii = new Instance(dep[j].getTypeName(), dep[j]);
                    }
                    TypedList list = (TypedList) objectLists.get(listname);

                    if (list == null) {
                        TypedList l = frame.getList(listname);
                        Frame fr = l.getType();
                        list = new TypedList(fr);
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
