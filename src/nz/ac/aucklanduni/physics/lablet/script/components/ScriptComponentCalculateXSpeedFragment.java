/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.script.components;

import nz.ac.aucklanduni.physics.lablet.experiment.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.lablet.views.table.*;

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
        ExperimentAnalysis experimentAnalysis = experiment.getExperimentAnalysis(getActivity());
        if (experimentAnalysis == null)
            return "";
        return experimentAnalysis.getXUnit();
    }

    @Override
    float getSpeed(int index) {
        return speedDataTableColumn.getValue(index).floatValue();
    }

    @Override
    ColumnMarkerDataTableAdapter createSpeedTableAdapter(ExperimentAnalysis experimentAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, experimentAnalysis);
        speedDataTableColumn = new XSpeedDataTableColumn();
        adapter.addColumn(new SpeedTimeDataTableColumn());
        adapter.addColumn(speedDataTableColumn);

        return adapter;
    }

    @Override
    ColumnMarkerDataTableAdapter createAccelerationTableAdapter(ExperimentAnalysis experimentAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, experimentAnalysis);
        XAccelerationDataTableColumn accelerationDataTableColumn = new XAccelerationDataTableColumn();
        adapter.addColumn(new AccelerationTimeDataTableColumn());
        adapter.addColumn(accelerationDataTableColumn);
        return adapter;
    }
}
