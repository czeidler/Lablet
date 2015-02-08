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
 * Table column for the marker data table adapter. Provides the x-acceleration.
 */
public class XAccelerationDataTableColumn extends UnitDataTableColumn {
    final private Unit xUnit;
    final private Unit tUnit;
    final private ITimeData timeData;

    public XAccelerationDataTableColumn(Unit xUnit, Unit tUnit, ITimeData timeData) {
        this.xUnit = xUnit;
        this.tUnit = tUnit;
        this.timeData = timeData;

        listenTo(xUnit);
        listenTo(tUnit);
    }

    @Override
    public int size() {
        return dataModel.getMarkerCount() - 2;
    }

    @Override
    public Number getValue(int index) {
        float speed0 = XSpeedDataTableColumn.getSpeed(index, dataModel, timeData, tUnit).floatValue();
        float speed1 = XSpeedDataTableColumn.getSpeed(index + 1, dataModel, timeData, tUnit).floatValue();
        float delta = speed1 - speed0;

        float deltaT = (timeData.getTimeAt(index + 2) - timeData.getTimeAt(index)) / 2;
        deltaT *= Math.pow(10, tUnit.getBaseExponent());

        return delta / deltaT;
    }

    @Override
    public String getHeader() {
        return "acceleration [" + xUnit.getTotalUnit() + "/"
                + tUnit.getBaseUnit() + "^2]";
    }
}
