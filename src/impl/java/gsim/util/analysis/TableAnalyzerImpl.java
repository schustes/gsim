package gsim.util.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import gsim.util.Utils;

public class TableAnalyzerImpl implements java.io.Serializable {

    private static Logger logger = Logger.getLogger(TableAnalyzerImpl.class);

    private static final long serialVersionUID = 1L;

    private ArrayList dimensions = new ArrayList(0);

    private HashMap dimensionvalues = new HashMap(0);

    private int lastupdatedRowIdx = 0;

    private Table table;

    public TableAnalyzerImpl(Table table) {
        this.table = table;
    }

    public TableAnalyzerImpl(Table table, String[] dimensions) {
        this.table = table;
        createDimensions(dimensions);
    }

    public void addColumn(String name, String defaultVal) {
        table.addColumn(name, defaultVal);
    }

    public void addTable(String[][] tableToAdd) {
        table.addTable(tableToAdd);
        updateDimensions();
    }

    /*
     * public double calculateAverage(String variable) { return 0; }
     */
    public String[][] calculateAverage(String variable) {
        // String[] col = table.selectColumn(dim);
        HashMap map = new HashMap();
        HashMap nPerCat = new HashMap();

        String[][] data = table.getBaseTable();
        for (int i = 0; i < data.length; i++) {
            // String dimVal = this.getCellValue(i, dim);
            String cell = getCellValue(i, variable);
            String dimVal = variable;
            if (map.containsKey(dimVal)) {
                double d = ((Double) map.get(dimVal)).doubleValue();
                d += Double.valueOf(cell).doubleValue();
                map.put(dimVal, new Double(d));
                int n = ((Integer) nPerCat.get(dimVal)).intValue();
                n++;
                nPerCat.put(dimVal, new Integer(n));
            } else {
                map.put(dimVal, new Double(cell));
                nPerCat.put(dimVal, new Integer(1));
            }
        }
        Iterator iter = nPerCat.keySet().iterator();
        HashMap result = new HashMap();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            int n = ((Integer) nPerCat.get(key)).intValue();
            double sum = ((Double) map.get(key)).doubleValue();
            double average = sum / n;
            result.put(key, new Double(average));
        }

        List list = new LinkedList(result.values());
        if (list.size() > 0) {
            Collections.sort(list, (obj1, obj2) -> {
                Double score1 = (Double) obj1;
                Double score2 = (Double) obj2;
                score1 = score1 == null ? new Double(0) : score1;
                score2 = score2 == null ? new Double(0) : score2;
                return score1.compareTo(score2);
            });
        }

        String[][] res = new String[result.keySet().size() + 1][3];
        res[0][0] = "Variable";
        res[0][1] = "Average";
        res[0][2] = "Sum";
        Iterator sortIt = list.iterator();
        int row = 1;
        while (sortIt.hasNext()) {
            Double v = (Double) sortIt.next();
            iter = result.keySet().iterator();
            boolean existent = false; // insert only once, even if there are same
            // values...
            while (iter.hasNext()) {
                String key = (String) iter.next();
                Double val = (Double) result.get(key);
                Integer n = (Integer) nPerCat.get(key);
                if (val.doubleValue() == v.doubleValue() && !existent) {
                    if (!containsKey(res, key)) {
                        existent = true;
                        res[row][0] = key;
                        res[row][2] = n.toString();
                        res[row][1] = val.toString();
                        row++;
                    }
                }
            }
        }

        return res;

    }

    /**
     * assert: rowname and colname must be table.getColumnNames()
     * 
     * @param rowname
     *            the attribute to appear as row
     * @param colname
     *            the attribute to appear as column
     */
    public String[][] crosstabs(String rowname, String colname) {

        String[] col1 = table.selectColumn(rowname);
        String[] col2 = table.selectColumn(colname);
        HashMap map = new HashMap();

        for (int i = 0; i < col1.length; i++) {
            String cat = col1[i]; // current "entity"
            String val = col2[i]; // value for this entity

            ComposedKey key = new ComposedKey(cat, val);
            if (!map.containsKey(key)) {
                map.put(key, new Integer(1));
            } else {
                int counter = ((Integer) map.get(key)).intValue();
                counter++;
                map.put(key, new Integer(counter));
            }
        }

        // create Table-Representation
        ArrayList crossrows = new ArrayList(0);
        ArrayList crosscols = new ArrayList(0);
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext()) {
            ComposedKey key = (ComposedKey) iter.next();
            String row = key.getRow();
            String col = key.getCol();
            if (!crossrows.contains(row)) {
                crossrows.add(row);
            }
            if (!crosscols.contains(col)) {
                crosscols.add(col);
            }
        }

        String[][] crosstab = createEmptyCrossTab(crossrows, crosscols);

        Iterator iter2 = map.keySet().iterator();
        while (iter2.hasNext()) {
            ComposedKey key = (ComposedKey) iter2.next();

            int rowidx = findRowIndex(crosstab, key.getRow());
            int colidx = findColIndex(crosstab, key.getCol());

            String s = crosstab[rowidx][colidx];
            if (s != null && s.equals("")) {
                int hs = ((Integer) map.get(key)).intValue();
                s = String.valueOf(hs);
            } else {
                int c = Integer.parseInt(s);
                int hs = ((Integer) map.get(key)).intValue();
                s = String.valueOf(c + hs);
            }
            crosstab[rowidx][colidx] = s;
        }

        for (int i = 0; i < crosstab.length; i++) {
            if (i == 0) {
                crosstab[i][0] = "Value labels (" + rowname + "/" + colname + ")";
            }
            if (i > 0) {
                crosstab[i][0] = crosstab[i][0];
            }
            if (i == 0) {
                for (int j = 1; j < crosstab[i].length; j++) {
                    // crosstab[i][j] = colname+"=" + crosstab[i][j];
                }
            }
        }

        return crosstab;
    }

    public String[][] frequencies(String attr) {
        String[] col = table.selectColumn(attr);
        HashMap map = new HashMap();

        for (int i = 0; i < col.length; i++) {
            String cat = col[i]; // current "entity"

            if (!map.containsKey(cat)) {
                map.put(cat, new Integer(1));
            } else {
                int counter = ((Integer) map.get(cat)).intValue();
                counter++;
                map.put(cat, new Integer(counter));
            }
        }

        String[][] fretable = new String[map.size() + 1][2];

        Set set = map.keySet();
        LinkedList list = new LinkedList(set);
        Collections.sort(list, (obj1, obj2) -> {
            String score1 = (String) obj1;
            String score2 = (String) obj2;
            score1 = score1 == null ? "" : score1;
            score2 = score2 == null ? "" : score2;
            if (TableAnalyzerImpl.this.isNumeric(score1) && TableAnalyzerImpl.this.isNumeric(score2)) {
                return new Integer(score1).compareTo(new Integer(score2));
            }
            return (new String(score1)).compareTo(new String(score2));
        });

        Iterator iter = list.iterator();
        int i = 1;
        ArrayList l = new ArrayList();
        fretable[0][1] = "Frequency";
        while (iter.hasNext()) {
            String key = (String) iter.next();
            fretable[i][0] = key;
            fretable[i][1] = ((Integer) map.get(key)).toString();
            i++;
        }
        fretable[0][0] = "Value label (" + attr + ")";
        return fretable;
    }

    public String[][] getBaseTable() {
        return table.getBaseTable();
    }

    public String getCellValue(int rowIdx, String colname) {
        String[] row = table.getBaseTable()[rowIdx];

        int i = findColumnNameIndex(table.getColumnNames(), colname);
        if (i == -1) {
            logger.debug("No column " + colname + "in table");
            return null;
        } else {
            String[][] s = table.getBaseTable();
            return (s[rowIdx][i]);
        }

    }

    public String[] getColumnNames() {
        ArrayList list = new ArrayList();
        for (int i = 0; i < table.getColumnNames().length; i++) {
            if (!table.getColumnNames()[i].startsWith("{")) {
                list.add(table.getColumnNames()[i]);
            }
        }
        String[] s = new String[list.size()];
        list.toArray(s);
        return s;
    }

    public String[] getDimensionRange(String dimension) {
        for (int i = 0; i < dimensions.size(); i++) {
            Dimension dim = (Dimension) dimensions.get(i);
            if (dim.toString().equals(dimension)) {
                return dim.getDomainValues();
            }
        }
        // "Range not found: " + dimension);
        return null;
    }

    public String[] getDimensions() {
        String[] dims = new String[dimensions.size()];
        for (int i = 0; i < dims.length; i++) {
            Dimension d = (Dimension) dimensions.get(i);
            dims[i] = d.toString();
        }
        return dims;
    }

    public String getDimensionValue(String dimension) {
        return (String) dimensionvalues.get(dimension);
    }

    public int getLastUpdate() {
        return lastupdatedRowIdx;
    }

    public String[] getPKDef() {
        int[] fields = table.getPkFields();
        String[] str = new String[table.getPkFields().length];
        for (int i = 0; i < fields.length; i++) {
            str[i] = getColumnName(fields[i]);
        }
        return str;
    }

    public Table getTable() {
        return table;
    }

    public String[][] getViewData() {
        ArrayList colnames = new ArrayList();
        ArrayList cols = new ArrayList();
        String[][] tbl = table.getBaseTable();

        for (int i = 0; i < table.getColumnNames().length; i++) {
            String n = table.getColumnNames()[i];
            if (!n.startsWith("{")) {
                String[] col = new String[tbl.length];
                colnames.add(n);
                for (int j = 0; j < tbl.length; j++) {
                    col[j] = tbl[j][i];
                }
                cols.add(col);
            }
        }
        String[] visiblecols = new String[colnames.size()];
        String[][] view = new String[tbl.length][visiblecols.length];
        colnames.toArray(visiblecols);

        for (int i = 0; i < cols.size(); i++) {
            String[] rr = (String[]) cols.get(i);
            for (int j = 0; j < view.length; j++) {
                view[j][i] = rr[j];
            }
        }

        return view;
    }

    // market, b2b
    /**
     * if value is null, the dimension will be set back to include all dimensions
     */
    public void setDimensionValue(String dimension, String value) {
        if (value == null) {
            dimensionvalues.remove(dimension);
        } else {
            dimensionvalues.put(dimension, value);
        }
        table.setDimensionConstraints(dimensionvalues);
    }

    public void sortNumerical(String colName) {

        List list = new LinkedList();
        String[][] t = getBaseTable();
        final int colIdx = findColumnNameIndex(getColumnNames(), colName);

        for (int i = 0; i < t.length; i++) {
            String[] row = t[i];
            list.add(row);
        }

        Collections.sort(list, (obj1, obj2) -> {
            String[] row1 = (String[]) obj1;
            String[] row2 = (String[]) obj2;
            Double score1 = Double.valueOf(row1[colIdx]);
            Double score2 = Double.valueOf(row2[colIdx]);
            score1 = score1 == null ? new Double(0) : score1;
            score2 = score2 == null ? new Double(0) : score2;
            return score1.compareTo(score2);
        });

        Iterator iter = list.iterator();
        String[][] nt = new String[t.length][t[0].length];
        int i = 0;
        while (iter.hasNext()) {
            String[] row = (String[]) iter.next();
            nt[i] = row;
            i++;
        }
        table.setData(nt);
    }

    public double sum(String col) {
        String[] column = table.selectColumn(col);
        double sum = 0;
        if (isNumerical(column)) {
            // logger.debug("is numerical");
            for (int i = 0; i < column.length; i++) {

                double d = Double.parseDouble(column[i]);
                // logger.debug("i: " + i+ ", val: " + d);
                sum += d;
                // logger.debug("i: " + i+ ", sum: " + sum);
            }
        }
        return sum;
    }

    public void updateColumn(String name, String val) {
        table.updateColumn(name, val, null);
        updateDimensions();
    }

    public void updateRow(String[] row, PrimaryKey pk) {

    }

    public void updateTable(Table table) {
        this.table = table;
    }

    public void updateTableCell(PrimaryKey pk, String colname, String val) throws IllegalArgumentException {
        int colidx = findColumnNameIndex(table.getColumnNames(), colname);
        int rowidx = table.findByPrimaryKey(pk);
        if (colidx == -1 || rowidx == -1) {
            throw new IllegalArgumentException("Column " + colname + " not found;");
        }
        table.getBaseTable()[rowidx][colidx] = val;
        lastupdatedRowIdx = rowidx;
    }

    private boolean containsKey(String[][] t, String k) {
        for (int i = 0; i < t.length; i++) {
            String key = t[i][0];
            if (key != null && key.equals(k)) {
                return true;
            }
        }
        return false;
    }

    private void createDimensions(String[] names) {
        for (int i = 0; i < names.length; i++) {
            Dimension m = new Dimension(names[i]);
            int c = findArrayIndexByName(table.getColumnNames(), names[i]);
            if (c == -1) {
                logger.debug("Dimension " + m + " not contained by basetable");
            }
            String[] col = table.selectColumn(names[i]);
            for (int j = 0; j < col.length; j++) {
                String dimval = col[j];
                m.addDomainValue(dimval);
            }
            dimensions.add(m);
        }
    }

    private String[][] createEmptyCrossTab(ArrayList rownames, ArrayList colnames) {
        String[][] crosstable = new String[rownames.size() + 1][colnames.size() + 1];

        crosstable[0][0] = "";
        Collections.sort(colnames, (obj1, obj2) -> {
            String score1 = (String) obj1;
            String score2 = (String) obj2;
            score1 = score1 == null ? "" : score1;
            score2 = score2 == null ? "" : score2;

            if (TableAnalyzerImpl.this.isNumeric(score1) && TableAnalyzerImpl.this.isNumeric(score2)) {
                return new Double(score1).compareTo(new Double(score2));
            }
            return (new String(score1)).compareTo(new String(score2));
        });
        Collections.sort(rownames, (obj1, obj2) -> {
            String score1 = (String) obj1;
            String score2 = (String) obj2;
            score1 = score1 == null ? "" : score1;
            score2 = score2 == null ? "" : score2;
            if (TableAnalyzerImpl.this.isNumeric(score1) && TableAnalyzerImpl.this.isNumeric(score2)) {
                return new Double(score1).compareTo(new Double(score2));
            }
            return (new String(score1)).compareTo(new String(score2));
        });

        for (int i = 0; i < rownames.size(); i++) {
            String s = (String) rownames.get(i);
            crosstable[i + 1][0] = s != null ? s : "";
            for (int j = 0; j < colnames.size(); j++) {
                crosstable[i + 1][j + 1] = "";
            }
        }

        for (int i = 0; i < colnames.size(); i++) {
            String s1 = (String) colnames.get(i);
            crosstable[0][i + 1] = s1 != null ? s1 : "";
        }

        return crosstable;
    }

    private int findArrayIndexByName(String[] str, String n) {
        for (int i = 0; i < str.length; i++) {
            if (n.equals(str[i])) {
                return i;
            }
        }
        return -1;
    }

    private int findColIndex(String[][] crosstable, String colname) {
        for (int i = 1; i < crosstable[0].length; i++) {
            if (crosstable[0][i].equals(colname)) {
                return i;
            }
        }
        // "col " + colname +" not found");
        return -1;
    }

    private int findColumnNameIndex(String[] cols, String s) {
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].equals(s)) {
                return i;
            }
        }
        return -1;
    }

    private int findRowIndex(String[][] crosstable, String rowname) {
        for (int i = 1; i < crosstable.length; i++) {
            if (crosstable[i][0].equals(rowname)) {
                return i;
            }
        }
        // "Row " + rowname +" not found");
        return -1;
    }

    private String getColumnName(int colIdx) {
        return table.getColumnNames()[colIdx];
    }

    private boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isNumerical(String[] col) {
        for (int i = 0; i < col.length; i++) {
            if (!Utils.isNumerical(col[i])) {
                return false;
            }
        }
        return true;
    }

    private void updateDimensions() {
        Iterator iter = dimensions.iterator();
        int i = 0;
        while (iter.hasNext()) {
            Dimension m = (Dimension) iter.next();
            m.removeDomainValues();
            String[] col = table.selectColumn(m.toString());
            for (int j = 0; j < col.length; j++) {
                String dimval = col[j];
                m.addDomainValue(dimval);
            }
            i++;
        }

    }

    private class ComposedKey {
        private String key;

        private String post;

        private String pre;

        public ComposedKey(String row, String col) {
            pre = row == null ? "--" : row;
            post = col == null ? "--" : col;
            key = pre + "-" + post;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ComposedKey) {
                ComposedKey k = (ComposedKey) o;
                return k.getKey().equals(key);
            }
            return false;
        }

        public String getCol() {
            return post;
        }

        public String getKey() {
            return key;
        }

        public String getRow() {
            return pre;
        }

        @Override
        public int hashCode() {
            return 1;
        }

    }

    private class Dimension {
        private ArrayList domain = new ArrayList(0);

        private String name;

        public Dimension(String name) {
            this.name = name;
        }

        public void addDomainValue(String value) {
            if (!domain.contains(value)) {
                domain.add(value);
            }
        }

        public String[] getDomainValues() {
            String[] vals = new String[domain.size()];
            domain.toArray(vals);
            return vals;
        }

        public void removeDomainValues() {
            domain.clear();
        }

        @Override
        public String toString() {
            return name;
        }

    }

}
