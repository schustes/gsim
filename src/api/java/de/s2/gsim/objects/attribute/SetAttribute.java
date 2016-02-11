package de.s2.gsim.objects.attribute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SetAttribute extends Attribute {

    public static final long serialVersionUID = -4982728557786529264L;

    protected String[] fillers;

    private List<String> entries = new ArrayList<>();

    public SetAttribute(String name, String[] fillers) {
        super(name);
        this.fillers = fillers;
    }

    public void addEntry(String value) {
        if (value.length() > 0 && !entries.contains(value)) {
            entries.add(value);
        }
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

    public List<String> getEntries() {
        return entries;
    }

    public String[] getFillers() {
        return fillers;
    }

    public String[] getFillersAndEntries() {
        List<String> set = new ArrayList<>();
        set.addAll(entries);
        for (String s : fillers) {
            set.add(s);
        }
        String[] ret = new String[set.size()];
        set.toArray(ret);
        return ret;
    }

    public void removeAllEntries() {
        entries.clear();
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
