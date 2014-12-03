/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import java.io.IOException;
import java.io.Writer;


public class CSVWriter {
    static public void writeTable(ColumnDataTableAdapter data, Writer writer, char separator) {
        if (data.getColumnCount() == 0)
            return;

        try {
            String header = "";
            for (int i = 0; i < data.getColumnCount(); i++) {
                DataTableColumn column = data.getColumn(i);
                header += column.getHeader();
                if (i < data.getColumnCount() - 1)
                    header += separator;
            }
            writer.write(header);
            writer.write('\n');

            int rowCount = data.getColumn(0).size();
            for (int rowId = 0; rowId < rowCount; rowId++) {
                String row = "";
                for (int i = 0; i < data.getColumnCount(); i++) {
                    DataTableColumn column = data.getColumn(i);
                    row += column.getStringValue(rowId);
                    if (i < data.getColumnCount() - 1)
                        row += separator;
                }
                writer.write(row);
                writer.write('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
