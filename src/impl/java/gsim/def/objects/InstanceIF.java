package gsim.def.objects;

import de.s2.gsim.objects.attribute.Attribute;

public interface InstanceIF {

    /**
     * Add an instance in the respective list, as long as this list is defined for this type in the frame.
     * 
     * @param listname
     *            String
     * @param instance
     *            Instance
     */
    public void addChildInstance(String listname, Instance instance);

    /**
     * Use only if desperate.
     * 
     * @param newName
     *            String
     */
    public void changeName(String newName);

    public Object clone();

    @Override
    public boolean equals(Object o);

    /**
     * Return an attribute with the specified name. Note that attributes with the same names can be defined in different lists.
     * 
     * @param attrName
     *            String
     * @return Attribute
     */
    public Attribute getAttribute(String attrName);

    /**
     * Return the attribute with the specified name in the specified list.
     * 
     * @param listname
     *            String
     * @param attrName
     *            String
     * @return Attribute
     */
    public Attribute getAttribute(String listname, String attrName);

    /**
     * Return the attributes that sit in the list with the specified name.
     * 
     * @param listname
     *            String
     * @return Attribute[]
     */
    public Attribute[] getAttributes(String listname);

    /**
     * Return the attribute list-names.
     * 
     * @return String[]
     */
    public String[] getAttributesListNames();

    /**
     * Return any child instance with the specified name. However, there may be child-instances with the same name in different lists.
     * 
     * @param name
     *            String
     * @return Instance
     */
    public Instance getChildInstance(String name);

    /**
     * Return the instance with the specified name in the specified list.
     * 
     * @param listname
     *            String
     * @param name
     *            String
     * @return Instance
     */
    public Instance getChildInstance(String listname, String name);

    /**
     * Return the names of the lists that contain instances.
     * 
     * @return String[]
     */
    public String[] getChildInstanceListNames();

    /**
     * Return all instances contained by the specified list.
     * 
     * @param listname
     *            String
     * @return Instance[]
     */
    public Instance[] getChildInstances(String listname);

    /**
     * Return the frame that was used to create this instance.
     * 
     * @return Frame
     */
    public Frame getDefinition();

    public String getName();

    /**
     * Check if this instance has something in common with the specified frame.
     * 
     * @param f
     *            Frame
     * @return boolean
     */
    public boolean inheritsFrom(Frame f);

    /**
     * Check if this instance has something in common with the frame with the specified name.
     * 
     * @param f
     *            String
     * @return boolean
     */
    public boolean inheritsFrom(String f);

    /**
     * Remove an attribute in the specified list.
     * 
     * @param listName
     *            String
     * @param attributeName
     *            String
     */
    public void removeAttribute(String listName, String attributeName);

    /**
     * Remove a contained object in the respective list.
     * 
     * @param listname
     *            String
     * @param instance
     *            Instance
     */
    public void removeChildInstance(String listname, String instance);

    /**
     * Gets the object that is specified by this path, either an attribute or an instance. Similar to the corresponding method in {@link} Frame.
     * 
     * @param path
     *            String[]
     * @return Object
     */
    public Object resolveName(String[] path);

    /**
     * Sets the attribute that *equals* the specified attribute (currently by name). As there can be attributes with the same name in different lists,
     * be careful.
     * 
     * @param a
     *            Attribute
     */
    public void setAttribute(Attribute a);

    /**
     * Set the attribute that equals the specified attribute to this attribute in the respective list.
     * 
     * @param listname
     *            String
     * @param a
     *            Attribute
     */
    public void setAttribute(String listname, Attribute a);

    /**
     * Sets the instance somewhere where an instance that equals the instance is found
     * 
     * @param instance
     *            Instance
     */
    public void setChildInstance(Instance instance);

    /**
     * If there exists somewhere as list with the specified name having an instance that equals the specified instance, it sets it to this instance.
     * 
     * @param listname
     *            String
     * @param instance
     *            Instance
     */
    public void setChildInstance(String listname, Instance instance);

}
