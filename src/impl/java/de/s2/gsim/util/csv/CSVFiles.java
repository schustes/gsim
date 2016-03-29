package de.s2.gsim.util.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import de.s2.gsim.util.analysis.Table;

public class CSVFiles {
    public CSVFiles() {
        super();
    }

    public void mergeFiles(File[] names, String fileName, String key) {
        TableMerger merger = new TableMerger();
        Table sum = null;
        for (int i = 0; i < names.length; i++) {
            Table t = open(names[i]);
            if (sum == null) {
                sum = t;
            } else {
                sum = merger.mergeBySumming(key, sum, t);
            }
        }
        merger.divide(key, sum, names.length);
        save(fileName, sum);
    }

    public Table open(File file) {
        try {
            File f = file;// new File(file);
            BufferedReader r = new BufferedReader(new FileReader(f));
            String s = "";
            String str = "";
            while ((s = r.readLine()) != null) {
                str += s + "\n";
            }
            Table t = Table.createFromCSVString(str);
            r.close();
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save(String name, Table table) {
        try {
            File f = new File(name);
            FileWriter w = new FileWriter(f);
            w.write(table.toCSV());
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
