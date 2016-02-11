package gsim.util.csv;

import gsim.util.analysis.PrimaryKey;
import gsim.util.analysis.Table;

public class TableMerger {

    public TableMerger() {
        super();
    }

    public Table addHorizontally(Table table1, Table table2) {
        return null;
    }

    public Table addVertically(Table table1, Table table2) {
        return null;
    }

    // key column not divide
    public Table divide(String excludedKeyName, Table table, double by) {
        int idx = table.getColumnIndex(excludedKeyName);

        for (int i = 0; i < table.getBaseTable().length; i++) {
            for (int j = 0; j < table.getBaseTable()[i].length; j++) {
                if (j != idx) {
                    double val = Double.parseDouble(table.getBaseTable()[i][j]);
                    val = val / by;
                    table.getBaseTable()[i][j] = String.valueOf(val);
                }
            }
        }
        return table;
    }

    public Table mergeBySumming(String keyColumn, Table table1, Table table2) {
        int idx1 = table1.getColumnIndex(keyColumn);
        int idx2 = table2.getColumnIndex(keyColumn);
        String[][] newData = new String[table2.getBaseTable().length][table2.getColumnNames().length];

        for (int i = 0; i < table1.getBaseTable().length; i++) {
            String keyVal = table1.getBaseTable()[i][idx1];
            PrimaryKey k = new PrimaryKey();
            k.add(keyColumn, keyVal);
            int row = table2.findByPrimaryKey(k);

            for (int j = 0; j < table2.getBaseTable()[i].length; j++) {
                if (j != idx2) {
                    String n = table2.getColumnNames()[j];
                    int idx3 = table1.getColumnIndex(n);
                    String val1 = table1.getBaseTable()[i][idx3];
                    String val2 = table2.getBaseTable()[row][j];
                    double sum = Double.parseDouble(val1) + Double.parseDouble(val2);
                    newData[i][j] = String.valueOf(sum);
                } else {
                    newData[i][j] = keyVal;
                }
            }
        }
        return new Table(table2.getColumnNames(), newData);
    }
}
