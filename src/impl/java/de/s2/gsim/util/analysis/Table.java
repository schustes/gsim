package de.s2.gsim.util.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Class is used only for holding and selecting tabledata. The construction of tabledata from Objects is done in class TableBuilder
 */
public class Table implements Cloneable, java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String[] columnnames = null;

    /**
     * mapping between a variable name and a value (range?) that sets a selection on the table and is applied with each retrieval method.
     */
    private HashMap constraints = new HashMap(0);

    private String[][] currentview = null;

    private String[][] data = null;

    private ArrayList pkfields = new ArrayList();

    private String[] rows = null;

    public Table(String[] columnnames, String[][] data) {
        this.data = data;
        currentview = data;
        this.columnnames = columnnames;
    }

    public void addColumn(String name, String defaultvalue) {

        String[] columnnames1 = new String[columnnames.length + 1];
        for (int i = 0; i < columnnames.length; i++) {
            columnnames1[i] = columnnames[i];
        }
        columnnames1[columnnames1.length - 1] = name;
        columnnames = columnnames1;

        for (int i = 0; i < data.length; i++) {
            String[] row = data[i];
            String[] newRow = new String[columnnames.length];
            for (int j = 0; j < row.length; j++) {
                newRow[j] = row[j];
            }
            newRow[newRow.length - 1] = defaultvalue;
            data[i] = newRow;
        }

    }

    public void addPkField(int colidx) {
        pkfields.add(new Integer(colidx));
    }

    public void addRow(String[] row) {
        String[][] n = new String[data.length + 1][row.length];
        for (int i = 0; i < data.length; i++) {
            n[i] = data[i];
        }
        n[n.length - 1] = row;
        data = n;
        currentview = data;
    }

    /**
     * Assuming that the values in the table to add are in the same order as in this table.
     * 
     * @param newTable
     */
    public void addTable(String[][] newTable) {
        if (newTable.length == 0) {
            return;
        }

        if (newTable[0].length != columnnames.length) {
            // "Not merged, different length");
            return;
        } else {
            String[][] largeTable = new String[data.length + newTable.length][columnnames.length];
            for (int i = 0; i < data.length; i++) {
                largeTable[i] = data[i];
            }
            int j = 0;
            for (int i = data.length; i < largeTable.length && j < newTable.length; i++) {
                largeTable[i] = newTable[j];
                j++;
            }
            data = largeTable;
            updateView();
        }
    }

    public void addTable(Table newTable) {
        if (newTable.getBaseTable().length == 0) {
            return;
        }

        String[] colNew = newTable.getColumnNames();
        for (int i = 0; i < colNew.length; i++) {
            if (!containsColumn(colNew[i])) {
                addColumn(colNew[i], "0");
            }
        }

        String[][] largeTable = new String[data.length + newTable.getBaseTable().length][columnnames.length];

        for (int i = 0; i < data.length; i++) {
            largeTable[i] = data[i];
        }
        for (int i = data.length; i < largeTable.length; i++) {
            for (int j = 0; j < columnnames.length; j++) {
                largeTable[i][j] = "0";
            }
        }

        for (int i = 0; i < colNew.length; i++) {
            for (int j = 0; j < newTable.getBaseTable().length; j++) {
                String val = newTable.getBaseTable()[j][i];
                int idx = findColumnIndex(columnnames, colNew[i]);
                largeTable[data.length + j][idx] = val;
            }
        }

        data = largeTable;
        updateView();
    }

    public void clear(int columnOffset) {
        for (int i = 0; i < data.length; i++) {
            String[] row = data[i];
            for (int j = columnOffset; j < row.length; j++) {
                row[j] = "";
            }
            data[i] = row;
        }
    }

    @Override
    public Object clone() {
        String[] c = columnnames.clone();
        String[][] d = data.clone();
        Table t = new Table(c, d);
        return t;
    }

    public boolean containsColumn(String n) {
        for (int i = 0; i < columnnames.length; i++) {
            try {
                if (columnnames[i].equals(n)) {
                    return true;
                }
            } catch (NullPointerException e) {
                // logger.debug("n: " + n + ", c: " + columnnames[i]);
            }
        }
        return false;
    }

    public int findByPrimaryKey(PrimaryKey pk) throws IllegalArgumentException {
        String[] fields = pk.getFields();
        for (int i = 0; i < data.length; i++) {
            boolean b = true;
            for (int j = 0; j < fields.length; j++) {
                int colidx = findColumnIndex(columnnames, fields[j]);
                if (colidx == -1) {
                    throw new IllegalArgumentException("Column " + fields[j] + "not found");
                }
                String s = data[i][colidx];
                if (!pk.getFieldValue(fields[j]).equals(s)) {
                    // if (!s.equals(pk.getFieldValue(fields[j]))) {
                    b = false;
                }
            }
            if (b) {
                return i;
            }
        }
        return -1;
    }

    /**
     * ?????
     */
    public String[][] getBaseTable() {
        return currentview;
    }

    public int getColumnIndex(String colName) {
        return findColumnIndex(columnnames, colName);
    }

    public String[] getColumnNames() {
        return columnnames;
    }

    public int[] getPkFields() {
        int[] fields = new int[pkfields.size()];
        for (int i = 0; i < pkfields.size(); i++) {
            Integer integer = (Integer) pkfields.get(i);
            fields[i] = integer.intValue();
        }
        return fields;
    }

    public void removeColumn(String name) {

        if (!containsColumn(name)) {
            return;
        }
        String s = "";
        for (int i = 0; i < columnnames.length; i++) {
            s += columnnames[i] + ",";
        }
        // logger.debug("not contains column: " + name+": "+s);

        String[] columnnames1 = new String[columnnames.length - 1];
        int j = 0;
        int idx = 0;
        for (int i = 0; i < columnnames.length; i++) {
            if (!columnnames[i].equals(name)) {
                // logger.debug("c: " + columnnames[i]+ ", " + name +" ,i:"+i+",
                // j:"+j +", idx: " + idx);
                columnnames1[j] = columnnames[i];
                j++;
            } else {
                idx = i;
            }
        }
        columnnames = columnnames1;

        for (int i = 0; i < data.length; i++) {
            String[] row = data[i];
            String[] newRow = new String[row.length - 1];
            int m = 0;
            // logger.debug("&&& " + name + ", " + idx);
            for (int k = 0; k < row.length; k++) {
                if (k != idx) {
                    newRow[m] = row[k];
                    m++;
                }
            }
            data[i] = newRow;
        }
    }

    public void removeRow(int idx) {

        String[][] n = new String[data.length - 1][columnnames.length];
        int k = 0;
        for (int i = 0; i < data.length; i++) {
            if (i != idx) {
                n[i] = data[i];
                k++;
            }
        }
        data = n;
        currentview = data;
    }

    public String[] selectColumn(String cName) {
        String col[] = new String[currentview.length];
        int idx = findColumnIndex(columnnames, cName);
        if (idx > -1) {
            for (int i = 0; i < currentview.length; i++) {
                col[i] = currentview[i][idx];
            }
            return col;
        }
        // "table - column " +cName +" not found");
        return null;
    }

    public String[] selectColumn(String cName, String dimension, String dimensionval) {
        String col[] = new String[data.length];
        int constraintCol = findColumnIndex(getColumnNames(), dimension);
        for (int i = 0; i < columnnames.length; i++) {
            if (columnnames[i] == cName) {
                for (int j = 0; j < data.length; j++) {
                    if (data[j][constraintCol] != dimensionval) {
                        col[j] = data[j][i];
                    }
                }
                return col;
            }
        }
        return null;
    }

    public String[][] selectColumns(String[] cName) {
        ArrayList columns = new ArrayList();
        for (int i = 0; i < cName.length; i++) {
            if (containsColumn(cName[i])) {
                columns.add(this.selectColumn(cName[i]));
            }
        }
        String[][] result = new String[data.length][cName.length];
        Iterator iter = columns.iterator();
        int k = 0;
        while (iter.hasNext()) {
            String[] column = (String[]) iter.next();
            for (int i = 0; i < column.length; i++) {
                result[i][k] = column[i];
                // logger.debug("k: " + k+", i: "+i+";"+column[i]);
            }
            k++;
        }

        return result;
    }

    public String[] selectRow(int rowIdx) {
        return data[rowIdx];
    }

    public String[][] selectRows(Object condition) {
        throw new RuntimeException("not implemented yet");
    }

    public void setData(String[][] data) {
        this.data = data;
        updateView();
    }

    /**
     * @cons is a hashmap of String-String pairs with the key containing a columnname, and the value a value which has a selection to be based on.
     */
    public void setDimensionConstraints(HashMap cons) {
        constraints = cons;

        ArrayList view = new ArrayList();
        Set set = constraints.keySet();

        for (int i = 0; i < data.length; i++) {
            boolean belongsToDimension = true;
            Iterator iter = set.iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String value = (String) constraints.get(key);
                int idx = findColumnIndex(columnnames, key);
                if (!data[i][idx].equals(value)) {
                    belongsToDimension = false;
                }
            } // while
            if (belongsToDimension) {
                view.add(data[i]);
            }
        }
        currentview = new String[view.size()][columnnames.length];
        view.toArray(currentview);
    }

    public String toCSV() {
        String csv = "";
        String head = "";
        for (int i = 0; i < columnnames.length; i++) {
            head += columnnames[i];
            if (i < columnnames.length - 1) {
                head += ",";
            } else {
                head += "\n";
            }
        }
        csv += head;
        for (int i = 0; i < data.length; i++) {
            String line = "";
            for (int j = 0; j < data[i].length; j++) {
                line += data[i][j];
                if (j < data[i].length - 1) {
                    line += ",";
                } else {
                    line += "\n";
                }
            }
            csv += line;
        }
        return csv;
    }

    public void updateColumn(String name, String value, String selectVal) {
        int col = findColumnIndex(columnnames, name);
        if (selectVal == null) {
            for (int i = 0; i < data.length; i++) {
                data[i][col] = value;
            }
        }
    }

    private int findColumnIndex(String[] columnnames, String colname) {
        for (int i = 0; i < columnnames.length; i++) {
            if (columnnames[i].equals(colname)) {
                return i;
            }
        }
        return -1;
    }

    private void updateView() {

        ArrayList view = new ArrayList();
        Set set = constraints.keySet();

        for (int i = 0; i < data.length; i++) {
            boolean belongsToDimension = true;
            Iterator iter = set.iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String value = (String) constraints.get(key);
                int idx = findColumnIndex(columnnames, key);
                if (!data[i][idx].equals(value)) {
                    belongsToDimension = false;
                }
            } // while
            if (belongsToDimension) {
                view.add(data[i]);
            }
        }
        currentview = new String[view.size()][columnnames.length];
        view.toArray(currentview);
    }

    public static Table createFromCSVString(String str) {
        String[] colnames = null;

        String[] rows = str.split("\n");
        if (rows.length == 1) {
            rows = str.split("\r");
        }

        String header = rows[0];
        String[] headerFields = header.split(",");
        colnames = headerFields;

        String[][] rawData = new String[rows.length - 1][headerFields.length];

        for (int i = 1; i < rows.length; i++) {
            String[] fields = rows[i].split(",");
            rawData[i - 1] = fields;
        }

        return new Table(colnames, rawData);
    }

}
