/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import nz.ac.auckland.lablet.experiment.SensorAnalysis;
import nz.ac.auckland.lablet.views.table.*;


/**
 * Fragment component to calculate x speed and acceleration.
 */
public class ScriptComponentCalculateXSpeedFragment extends ScriptComponentCalculateSpeedFragment {
    private XSpeedDataTableColumn speedDataTableColumn;

    @Override
    String getDescriptionLabel() {
        return "Fill table for the x-direction:";
    }

    @Override
    float getPosition(int index) {
        return tagMarker.getCalibratedMarkerPositionAt(index).x;
    }

    @Override
    String getPositionUnit() {
        ScriptComponentExperiment experiment = ((ScriptComponentTreeCalculateSpeed)component).getExperiment();
        SensorAnalysis sensorAnalysis = experiment.getExperimentAnalysis(getActivity());
        if (sensorAnalysis == null)
            return "";
        return sensorAnalysis.getXUnit();
    }

    @Override
    float getSpeed(int index) {
        return speedDataTableColumn.getValue(index).floatValue();
    }

    @Override
    ColumnMarkerDataTableAdapter createSpeedTableAdapter(SensorAnalysis sensorAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, sensorAnalysis);
        speedDataTableColumn = new XSpeedDataTableColumn();
        adapter.addColumn(new SpeedTimeDataTableColumn());
        adapter.addColumn(speedDataTableColumn);

        return adapter;
    }

    @Override
    ColumnMarkerDataTableAdapter createAccelerationTableAdapter(SensorAnalysis sensorAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, sensorAnalysis);
        XAccelerationDataTableColumn accelerationDataTableColumn = new XAccelerationDataTableColumn();
        adapter.addColumn(new AccelerationTimeDataTableColumn());
        adapter.addColumn(accelerationDataTableColumn);
        return adapter;
    }
}
