package de.s2.gsim.objects.attribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A SetAttribute can hold several values.
 * 
 * @author stephan
 *
 */
public class SetAttribute extends Attribute {

    protected String[] fillers;

    private List<String> entries = new ArrayList<>();

    /**
     * Constructor. The SetAttribute is initialised with an empty set.
     * 
     * @param name the attribute name
     * @param fillers the possible values the set can hold
     */
    public SetAttribute(String name, String[] fillers) {
        super(name);
        this.fillers = fillers;
    }

    /**
     * Adds an entry.
     * 
     * @param value the value to add
     */
    public void addEntry(String value) {
        if (value.length() > 0 && !entries.contains(value)) {
            entries.add(value);
        }
    }

    /**
     * Gets all entries of the attribute.
     * 
     * @return the entries
     */
    public List<String> getEntries() {
        return entries;
    }

    /**
     * Gets all fillers of the attribute, i.e. the possible values that can be in the entry list.
     * 
     * @return the fillers
     */
    public String[] getFillers() {
        return fillers;
    }

    /**
     * Clears the set.
     */
    public void removeAllEntries() {
        entries.clear();
    }

    @Override
    public Object clone() {
        SetAttribute a = new SetAttribute(getName(), fillers);
        Iterator<String> iter = entries.iterator();
        while (iter.hasNext()) {
            a.addEntry((String) iter.next());
        }
        a.setSystem(isSystem());
        a.setMutable(isMutable());

        return a;
    }

    @Override
    public boolean equalsValue(Attribute a) {
        if (a instanceof SetAttribute) {
            SetAttribute att = (SetAttribute) a;
            if (att.getEntries().size() != getEntries().size()) {
                return false;
            }

            Iterator<String> iter1 = getEntries().iterator();
            while (iter1.hasNext()) {
                String s1 = (String) iter1.next();
                if (!att.getEntries().contains(s1)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    @Override
    public String toValueString() {
        Iterator<String> iter = entries.iterator();
        String s = "";
        while (iter.hasNext()) {
            s += (String) iter.next();
            if (iter.hasNext()) {
                s += ",";
            }
        }
        return s;
    }

}
