/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script.components;

import nz.ac.aucklanduni.physics.tracker.experiment.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.tracker.views.table.*;

public class ScriptComponentCalculateYSpeedFragment extends ScriptComponentCalculateSpeedFragment {
    private YSpeedDataTableColumn speedDataTableColumn;

    @Override
    String getDescriptionLabel() {
        return "Fill table for the y-direction:";
    }

    @Override
    float getPosition(int index) {
        return tagMarker.getCalibratedMarkerPositionAt(index).y;
    }

    @Override
    String getPositionUnit() {
        ScriptComponentExperiment experiment = ((ScriptComponentTreeCalculateSpeed)component).getExperiment();
        ExperimentAnalysis experimentAnalysis = experiment.getExperimentAnalysis(getActivity());
        if (experimentAnalysis == null)
            return "";
        return experimentAnalysis.getYUnit();
    }

    @Override
    float getSpeed(int index) {
        return speedDataTableColumn.getValue(index).floatValue();
    }

    @Override
    ColumnMarkerDataTableAdapter createSpeedTableAdapter(ExperimentAnalysis experimentAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, experimentAnalysis);
        speedDataTableColumn = new YSpeedDataTableColumn();
        adapter.addColumn(new SpeedTimeDataTableColumn());
        adapter.addColumn(speedDataTableColumn);

        return adapter;
    }

    @Override
    ColumnMarkerDataTableAdapter createAccelerationTableAdapter(ExperimentAnalysis experimentAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, experimentAnalysis);
        YAccelerationDataTableColumn accelerationDataTableColumn = new YAccelerationDataTableColumn();
        adapter.addColumn(new AccelerationTimeDataTableColumn());
        adapter.addColumn(accelerationDataTableColumn);
        return adapter;
    }
}
