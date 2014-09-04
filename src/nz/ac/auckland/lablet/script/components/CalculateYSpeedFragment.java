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
 * Fragment component to calculate y speed and acceleration.
 */
public class CalculateYSpeedFragment extends CalculateSpeedFragment {
    private YSpeedDataTableColumn speedDataTableColumn;

    @Override
    String getDescriptionLabel() {
        return "Fill table for the y-direction:";
    }

    @Override
    float getPosition(int index) {
        return tagMarker.getRealMarkerPositionAt(index).y;
    }

    @Override
    String getPositionUnit() {
        return tagMarker.getCalibrationXY().getYUnit().getUnit();
    }

    @Override
    float getSpeed(int index) {
        return speedDataTableColumn.getValue(index).floatValue();
    }

    @Override
    MarkerDataTableAdapter createSpeedTableAdapter() {
        MarkerDataTableAdapter adapter = new MarkerDataTableAdapter(tagMarker, timeCalibration);
        speedDataTableColumn = new YSpeedDataTableColumn();
        adapter.addColumn(new SpeedTimeDataTableColumn());
        adapter.addColumn(speedDataTableColumn);

        return adapter;
    }

    @Override
    MarkerDataTableAdapter createAccelerationTableAdapter() {
        MarkerDataTableAdapter adapter = new MarkerDataTableAdapter(tagMarker, timeCalibration);
        YAccelerationDataTableColumn accelerationDataTableColumn = new YAccelerationDataTableColumn();
        adapter.addColumn(new AccelerationTimeDataTableColumn());
        adapter.addColumn(accelerationDataTableColumn);
        return adapter;
    }
}
