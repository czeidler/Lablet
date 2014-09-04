/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import nz.ac.auckland.lablet.views.table.*;


/**
 * Fragment component to calculate x speed and acceleration.
 */
public class CalculateXSpeedFragment extends CalculateSpeedFragment {
    private XSpeedDataTableColumn speedDataTableColumn;

    @Override
    String getDescriptionLabel() {
        return "Fill table for the x-direction:";
    }

    @Override
    float getPosition(int index) {
        return tagMarker.getRealMarkerPositionAt(index).x;
    }

    @Override
    String getPositionUnit() {
        return tagMarker.getCalibrationXY().getXUnit().getUnit();
    }

    @Override
    float getSpeed(int index) {
        return speedDataTableColumn.getValue(index).floatValue();
    }

    @Override
    MarkerDataTableAdapter createSpeedTableAdapter() {
        MarkerDataTableAdapter adapter = new MarkerDataTableAdapter(tagMarker, timeCalibration);
        speedDataTableColumn = new XSpeedDataTableColumn();
        adapter.addColumn(new SpeedTimeDataTableColumn());
        adapter.addColumn(speedDataTableColumn);

        return adapter;
    }

    @Override
    MarkerDataTableAdapter createAccelerationTableAdapter() {
        MarkerDataTableAdapter adapter = new MarkerDataTableAdapter(tagMarker, timeCalibration);
        XAccelerationDataTableColumn accelerationDataTableColumn = new XAccelerationDataTableColumn();
        adapter.addColumn(new AccelerationTimeDataTableColumn());
        adapter.addColumn(accelerationDataTableColumn);
        return adapter;
    }
}
