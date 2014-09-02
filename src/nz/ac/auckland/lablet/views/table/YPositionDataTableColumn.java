/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;


/**
 * Table column for the marker data table adapter. Provides the y-position.
 */
public class YPositionDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return dataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        return dataModel.getCalibratedMarkerPositionAt(index).y;
    }

    @Override
    public String getHeader() {
        return "y [" + dataModel.getCalibrationXY().getYUnit().getUnit() + "]";
    }
}
