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
 * Table column for the marker data table adapter. Provides the x-speed.
 */
public class XSpeedDataTableColumn extends DataTableColumn {
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
        return "velocity [" + sensorAnalysis.getXUnit() + "/"
                + sensorAnalysis.getSensorData().getRunValueBaseUnit() + "]";
    }

    public static Number getSpeed(int index, MarkerDataModel markersDataModel, SensorAnalysis sensorAnalysis) {
        SensorData sensorData = sensorAnalysis.getSensorData();
        float delta = markersDataModel.getCalibratedMarkerPositionAt(index + 1).x
                - markersDataModel.getCalibratedMarkerPositionAt(index).x;
        float deltaT = sensorData.getRunValueAt(index + 1) - sensorData.getRunValueAt(index);
        if (sensorAnalysis.getSensorData().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return delta / deltaT;
    }
}
