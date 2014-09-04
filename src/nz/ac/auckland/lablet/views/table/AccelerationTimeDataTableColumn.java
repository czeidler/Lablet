/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;


import nz.ac.auckland.lablet.experiment.Unit;

/**
 * Table column for the marker data table adapter. Provides a time column for the use in combination with an
 * acceleration columns.
 */
public class AccelerationTimeDataTableColumn extends TimeDataTableColumn {
    public AccelerationTimeDataTableColumn(Unit tUnit) {
        super(tUnit);
    }

    @Override
    public int size() {
        return dataModel.getMarkerCount() - 2;
    }

    @Override
    public Number getValue(int index) {
        float t0 = timeData.getTimeAt(index);
        float t2 = timeData.getTimeAt(index + 2);
        return t0 + (t2 - t0) / 2.f;
    }
}
