package de.s2.gsim.util.analysis;

import java.util.ArrayList;

public class PrimaryKey implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private ArrayList names = new ArrayList();

    private ArrayList values = new ArrayList();

    public PrimaryKey() {
    }

    public void add(String colname, String val) {
        values.add(val);
        names.add(colname);
    }

    public String[] getFields() {
        String[] fields = new String[names.size()];

        for (int i = 0; i < names.size(); i++) {
            String s = (String) names.get(i);
            fields[i] = s;
        }
        return fields;
    }

    public String getFieldValue(String colname) {
        for (int i = 0; i < names.size(); i++) {
            if (((String) names.get(i)).equals(colname)) {
                String s = (String) values.get(i);
                return s;
            }
        }
        return null;
    }

}
