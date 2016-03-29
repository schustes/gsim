package de.s2.gsim.def.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.s2.gsim.objects.attribute.Attribute;

/**
 * Top level class for Frame and Instance. The common thing is that these objects have attribute and object lists, ids and so on. This stuff goes
 * here.
 *
 * @author Stephan
 * @version 0.1
 */
public class UnitOLD implements Cloneable {

    public static final long serialVersionUID = -5778191656497160894L;

    protected HashMap<String, List<Attribute>> attributeLists = new HashMap<>();

    protected boolean isDirty = true;

    protected boolean isMutable = true;

    protected boolean isSystem = false;

    protected String name = "";

    protected TypedMapOLD objectLists = new TypedMapOLD();

    public UnitOLD() {
    }

    @SuppressWarnings("unchecked")
    public <K extends UnitOLD> K copy() {
        return (K) clone();
    }

    @Override
    public UnitOLD clone() {
        UnitOLD u = new UnitOLD();
        u.isSystem = isSystem;
        u.isMutable = isMutable;
        u.objectLists = new TypedMapOLD(objectLists);
        u.attributeLists = new HashMap<String, List<Attribute>>();
        return u;
    }

    public void defineAttributeList(String listname) {
        attributeLists.put(listname, new ArrayList<Attribute>());
    }

    public void defineObjectList(String listname, FrameOLD type) {
        TypedListOLD list = new TypedListOLD(type);
        objectLists.put(listname, list);
    }

    public HashMap<String, List<Attribute>> getAttributeLists() {
        return attributeLists;
    }

    public String getName() {
        return name;
    }

    public TypedMapOLD getObjectLists() {
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

    public void setMutable(boolean b) {
        isMutable = b;
        setDirty(true);
    }

    public void setSystem(boolean b) {
        isSystem = b;
        setDirty(true);
    }

}
