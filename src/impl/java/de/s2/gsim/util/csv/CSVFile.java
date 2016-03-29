package de.s2.gsim.util.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

import de.s2.gsim.util.analysis.PrimaryKey;
import de.s2.gsim.util.analysis.Table;

public class CSVFile implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String name;

    private Table table;

    public CSVFile(String name, String[] columnNames, int len) {
        this.name = name;
        table = new Table(columnNames, new String[len][columnNames.length]);
    }

    private CSVFile(String name, Table t) {
        this.name = name;
        table = t;
    }

    public int addRow() {
        table.addRow(new String[table.getColumnNames().length]);
        return table.getBaseTable().length - 1;
    }

    public int getRowIndex(PrimaryKey k) {
        return table.findByPrimaryKey(k);
    }

    public int getRowIndex(String colName, String value) {
        PrimaryKey k = new PrimaryKey();
        k.add(colName, value);
        return table.findByPrimaryKey(k);
    }

    public String getValue(String colName, int row) {
        int idx = table.getColumnIndex(colName);
        return table.getBaseTable()[row][idx];
    }

    public void save() {
        try {
            File f = new File(name);
            FileWriter w = new FileWriter(f);
            w.write(table.toCSV());
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setValue(String colName, String val, int row) {
        int idx = table.getColumnIndex(colName);
        table.getBaseTable()[row][idx] = val;
    }

    public static CSVFile create(String fileName) {
        try {
            File f = new File(fileName);
            BufferedReader r = new BufferedReader(new FileReader(f));
            String s = "";
            String str = "";
            while ((s = r.readLine()) != null) {
                str += s + "\n";
            }
            Table t = Table.createFromCSVString(str);
            r.close();
            return new CSVFile(fileName, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static CSVFile create(String fileName, java.io.InputStream stream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            String s = "";
            String str = "";
            while ((s = r.readLine()) != null) {
                str += s + "\n";
            }
            Table t = Table.createFromCSVString(str);
            r.close();
            return new CSVFile(fileName, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
