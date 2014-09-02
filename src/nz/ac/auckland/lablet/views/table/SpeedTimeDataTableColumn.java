/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;


/**
 * Table column for the marker data table adapter. Provides a time column for the use in combination with an
 * speed columns.
 */
public class SpeedTimeDataTableColumn extends TimeDataTableColumn {
    @Override
    public int size() {
        return dataModel.getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        float t0 = timeCalibration.getTimeFromRaw(index);
        float t1 = timeCalibration.getTimeFromRaw(index + 1);
        return t0 + (t1 - t0) / 2.f;
    }
}