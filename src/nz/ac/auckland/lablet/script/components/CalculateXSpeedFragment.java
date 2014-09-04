/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import nz.ac.auckland.lablet.camera.MotionAnalysis;
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
        return getMotionAnalysis().getXUnit().getUnit();
    }

    @Override
    float getSpeed(int index) {
        return speedDataTableColumn.getValue(index).floatValue();
    }

    @Override
    MarkerDataTableAdapter createSpeedTableAdapter() {
        MotionAnalysis motionAnalysis = getMotionAnalysis();
        MarkerDataTableAdapter adapter = new MarkerDataTableAdapter(tagMarker, timeCalibration);
        speedDataTableColumn = new XSpeedDataTableColumn(motionAnalysis.getXUnit(), motionAnalysis.getTUnit());
        adapter.addColumn(new SpeedTimeDataTableColumn(motionAnalysis.getTUnit()));
        adapter.addColumn(speedDataTableColumn);

        return adapter;
    }

    @Override
    MarkerDataTableAdapter createAccelerationTableAdapter() {
        MotionAnalysis motionAnalysis = getMotionAnalysis();
        MarkerDataTableAdapter adapter = new MarkerDataTableAdapter(tagMarker, timeCalibration);
        XAccelerationDataTableColumn accelerationDataTableColumn = new XAccelerationDataTableColumn(
                motionAnalysis.getXUnit(), motionAnalysis.getTUnit());
        adapter.addColumn(new AccelerationTimeDataTableColumn(motionAnalysis.getTUnit()));
        adapter.addColumn(accelerationDataTableColumn);
        return adapter;
    }
}
