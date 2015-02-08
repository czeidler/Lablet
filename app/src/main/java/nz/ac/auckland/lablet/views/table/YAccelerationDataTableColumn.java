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
 * Table column for the marker data table adapter. Provides the y-acceleration.
 */
public class YAccelerationDataTableColumn extends UnitDataTableColumn {
    final private Unit yUnit;
    final private Unit tUnit;
    final private ITimeData timeData;

    public YAccelerationDataTableColumn(Unit yUnit, Unit tUnit, ITimeData timeData) {
        this.yUnit = yUnit;
        this.tUnit = tUnit;
        this.timeData = timeData;

        listenTo(yUnit);
        listenTo(tUnit);
    }

    @Override
    public int size() {
        return dataModel.getMarkerCount() - 2;
    }

    @Override
    public Number getValue(int index) {
        float speed0 = YSpeedDataTableColumn.getSpeed(index, dataModel, timeData, tUnit).floatValue();
        float speed1 = YSpeedDataTableColumn.getSpeed(index + 1, dataModel, timeData, tUnit).floatValue();
        float delta = speed1 - speed0;

        float deltaT = (timeData.getTimeAt(index + 2) - timeData.getTimeAt(index)) / 2;
        deltaT *= Math.pow(10, tUnit.getBaseExponent());

        return delta / deltaT;
    }

    @Override
    public String getHeader() {
        return "acceleration [" + yUnit.getTotalUnit() + "/" + tUnit.getBaseUnit() + "^2]";
    }
}
