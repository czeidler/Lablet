/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.experiment.SensorData;


/**
 * Table column for the marker data table adapter. Provides the x-acceleration.
 */
public class XAccelerationDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markerDataModel.getMarkerCount() - 2;
    }

    @Override
    public Number getValue(int index) {
        float speed0 = XSpeedDataTableColumn.getSpeed(index, markerDataModel, sensorAnalysis).floatValue();
        float speed1 = XSpeedDataTableColumn.getSpeed(index + 1, markerDataModel, sensorAnalysis).floatValue();
        float delta = speed1 - speed0;

        SensorData sensorData = sensorAnalysis.getSensorData();
        float deltaT = (sensorData.getRunValueAt(index + 2) - sensorData.getRunValueAt(index)) / 2;
        if (sensorAnalysis.getSensorData().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;

        return delta / deltaT;
    }

    @Override
    public String getHeader() {
        return "acceleration [" + sensorAnalysis.getXUnit() + "/"
                + sensorAnalysis.getSensorData().getRunValueBaseUnit() + "^2]";
    }
}
