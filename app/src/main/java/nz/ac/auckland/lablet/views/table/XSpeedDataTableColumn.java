/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.camera.ITimeData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.experiment.Unit;


/**
 * Table column for the marker data table adapter. Provides the x-speed.
 */
public class XSpeedDataTableColumn extends UnitDataTableColumn {
    final private Unit xUnit;
    final private Unit tUnit;
    final private ITimeData timeData;

    public XSpeedDataTableColumn(Unit xUnit, Unit tUnit, ITimeData timeData) {
        this.xUnit = xUnit;
        this.tUnit = tUnit;
        this.timeData = timeData;

        listenTo(xUnit);
        listenTo(tUnit);
    }

    @Override
    public int size() {
        return dataModel.getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        return getSpeed(index, dataModel, timeData, tUnit);
    }

    @Override
    public String getHeader() {
        return "velocity [" + xUnit.getUnit() + "/" + tUnit.getBase() + "]";
    }

    public static Number getSpeed(int index, MarkerDataModel markersDataModel, ITimeData timeCalibration, Unit tUnit) {
        float delta = markersDataModel.getRealMarkerPositionAt(index + 1).x
                - markersDataModel.getRealMarkerPositionAt(index).x;
        float deltaT = timeCalibration.getTimeAt(index + 1) - timeCalibration.getTimeAt(index);
        if (tUnit.getPrefix().equals("m"))
            deltaT /= 1000;
        return delta / deltaT;
    }
}
