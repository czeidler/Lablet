/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views.table;


public class YPositionDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markersDataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        return markersDataModel.getCalibratedMarkerPositionAt(index).y;
    }

    @Override
    public String getHeader() {
        return "y [" + experimentAnalysis.getYUnit() + "]";
    }
}
