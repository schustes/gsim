package de.s2.gsim.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Top level class for Frame and Instance. The common thing is that these objects have attribute and object lists, ids and so on. This stuff goes
 * here.
 *
 * @author Stephan
 * @version 0.1
 */
public abstract class Unit<U,A> implements Cloneable {

    private boolean isDirty = true;

    private final boolean isMutable;

    private final boolean isSystem;

    private final String name;

    private final TypedMap<U,A> objectLists;

    private final Map<String, List<A>> attributeLists;

    public Unit(String name, boolean isMutable, boolean isSystem) {
    	this(name, isMutable, isSystem, new HashMap<>(), new TypedMap<>());
    }

    private Unit(String name, boolean isMutable, boolean isSystem, Map<String,List<A>> attributes, TypedMap<U,A> objects) {
    	this.name = name;
    	this.isMutable = isMutable;
    	this.isSystem = isSystem;
    	this.attributeLists = attributes;
    	this.objectLists = objects;
    }

    public abstract Unit<U,A> clone();

    public Unit<U,A> copy() {
        return clone();
    }

    public void defineAttributeList(String listname) {
        attributeLists.put(listname, new ArrayList<>());
    }

    public void defineObjectList(String listname, Frame type) {
        TypedList<U,A> list = new TypedList<>(type);
        objectLists.put(listname, list);
    }

    public Map<String, List<A>> getAttributeLists() {
        return attributeLists;
    }

    public String getName() {
        return name;
    }

    public TypedMap<U,A> getObjectLists() {
        return objectLists;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isMutable() {
        return isMutable;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void removeAttributeList(String listname) {
        attributeLists.remove(listname);
    }

    public void removeChildInstanceList(String listname) {
        objectLists.remove(listname);
    }

    public void setDirty(boolean b) {
        isDirty = b;
    }

}
