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

    private final TypedMap<U> objectLists;

    private final Map<String, List<A>> attributeLists;

    public Unit(String name, boolean isMutable, boolean isSystem) {
    	this(name, isMutable, isSystem, new HashMap<>(), new TypedMap<>());
    }

    private Unit(String name, boolean isMutable, boolean isSystem, Map<String,List<A>> attributes, TypedMap<U> objects) {
    	this.name = name;
    	this.isMutable = isMutable;
    	this.isSystem = isSystem;
    	this.attributeLists = attributes;
    	this.objectLists = objects;
    }

    public abstract Unit<U, A> clone();

    @SuppressWarnings("unchecked")
    public <K extends Unit<?, ?>> K copy() {
        return (K) clone();
    }

    public void defineAttributeList(String listname) {
        attributeLists.put(listname, new ArrayList<>());
    }

    public void defineObjectList(String listname, Frame type) {
        TypedList<U> list = new TypedList<>(type);
        objectLists.put(listname, list);
    }

    public Map<String, List<A>> getAttributeLists() {
        return attributeLists;
    }

    public String getName() {
        return name;
    }

    public TypedMap<U> getObjectLists() {
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

    public boolean removeDeclaredAttributeList(String listname) {
		if (attributeLists.remove(listname) != null) {
			setDirty(true);
			return true;
		}
		return false;
    }

    public boolean removeDeclaredChildInstanceList(String listname) {
        if (objectLists.remove(listname) != null) {
			setDirty(true);
			return true;
        }
        return false;
    }

    public void setDirty(boolean b) {
        isDirty = b;
    }

}
