/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;


import nz.ac.auckland.lablet.camera.ITimeData;
import nz.ac.auckland.lablet.misc.Unit;

/**
 * Table column for the marker data table adapter. Provides a time column for the use in combination with an
 * speed columns.
 */
public class SpeedTimeDataTableColumn extends TimeDataTableColumn {

    public SpeedTimeDataTableColumn(Unit tUnit, ITimeData timeData) {
        super(tUnit, timeData);
    }

    @Override
    public int size() {
        return dataModel.size() - 1;
    }

    @Override
    public Number getValue(int index) {
        float t0 = timeData.getTimeAt(index);
        float t1 = timeData.getTimeAt(index + 1);
        return t0 + (t1 - t0) / 2.f;
    }
}