/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.experiment.SensorAnalysis;
import nz.ac.auckland.lablet.experiment.SensorData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Table column for the marker data table adapter. Provides the y-speed.
 */
public class YSpeedDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markerDataModel.getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        return getSpeed(index, markerDataModel, sensorAnalysis);
    }

    @Override
    public String getHeader() {
        return "velocity [" + sensorAnalysis.getYUnit() + "/"
                + sensorAnalysis.getSensorData().getRunValueBaseUnit() + "]";
    }

    public static Number getSpeed(int index, MarkerDataModel markersDataModel, SensorAnalysis sensorAnalysis) {
        SensorData sensorData = sensorAnalysis.getSensorData();
        float delta = markersDataModel.getCalibratedMarkerPositionAt(index + 1).y
                - markersDataModel.getCalibratedMarkerPositionAt(index).y;
        float deltaT = sensorData.getRunValueAt(index + 1) - sensorData.getRunValueAt(index);
        if (sensorAnalysis.getSensorData().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return delta / deltaT;
    }
}
