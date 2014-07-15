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
 * Table column for the marker data table adapter. Provides a time column for the use in combination with an
 * speed columns.
 */
public class SpeedTimeDataTableColumn extends TimeDataTableColumn {
    @Override
    public int size() {
        return markerDataModel.getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        SensorData sensorData = sensorAnalysis.getSensorData();
        float t0 = sensorData.getRunValueAt(index);
        float t1 = sensorData.getRunValueAt(index + 1);
        return t0 + (t1 - t0) / 2.f;
    }
}