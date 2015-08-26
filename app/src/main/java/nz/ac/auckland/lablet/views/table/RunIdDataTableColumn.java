/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;


/**
 * Table column for the marker data table adapter. Provides the run id.
 */
public class RunIdDataTableColumn extends DataTableColumn {
    private String header = "id";
    public RunIdDataTableColumn(String header) {
        this.header = header;
    }

    public int size() {
        return dataModel.getDataCount();
    }

    public Number getValue(int index) {
        return dataModel.getDataAt(index).getFrameId();
    }

    public String getStringValue(int index) {
        Number number = getValue(index);
        String text = "";
        text += number.intValue();
        return text;
    }

    public String getHeader() {
        return header;
    }
}