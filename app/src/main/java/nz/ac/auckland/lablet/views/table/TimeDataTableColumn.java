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
 * position columns.
 */
public class TimeDataTableColumn extends UnitDataTableColumn {
    final private Unit tUnit;
    final protected ITimeData timeData;

    public TimeDataTableColumn(Unit tUnit, ITimeData timeData) {
        this.tUnit = tUnit;
        this.timeData = timeData;

        listenTo(tUnit);
    }

    @Override
    public int size() {
        return dataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        int runId = dataModel.getMarkerDataAt(index).getFrameId();
        return timeData.getTimeAt(runId);
    }

    @Override
    public String getStringValue(int index) {
        Number number = getValue(index);
        return String.format("%.1f", number.floatValue());
    }

    @Override
    public String getHeader() {
        return tUnit.getName() + " [" + tUnit.getTotalUnit() + "]";
    }
}
