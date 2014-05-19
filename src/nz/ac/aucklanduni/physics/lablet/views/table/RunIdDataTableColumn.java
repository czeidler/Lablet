/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views.table;

public class RunIdDataTableColumn extends DataTableColumn {
    public int size() {
        return markersDataModel.getMarkerCount();
    }

    public Number getValue(int index) {
        return markersDataModel.getMarkerDataAt(index).getRunId();
    }

    public String getStringValue(int index) {
        Number number = getValue(index);
        String text = "";
        text += number.intValue();
        return text;
    }

    public String getHeader() {
        return "id";
    }
}