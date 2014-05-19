/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.views.table;

import nz.ac.aucklanduni.physics.tracker.experiment.Experiment;
import nz.ac.aucklanduni.physics.tracker.experiment.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.tracker.experiment.MarkersDataModel;


public class XSpeedDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markersDataModel.getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        return getSpeed(index, markersDataModel, experimentAnalysis);
    }

    @Override
    public String getHeader() {
        return "velocity [" + experimentAnalysis.getXUnit() + "/"
                + experimentAnalysis.getExperiment().getRunValueBaseUnit() + "]";
    }

    public static Number getSpeed(int index, MarkersDataModel markersDataModel, ExperimentAnalysis experimentAnalysis) {
        Experiment experiment = experimentAnalysis.getExperiment();
        float delta = markersDataModel.getCalibratedMarkerPositionAt(index + 1).x
                - markersDataModel.getCalibratedMarkerPositionAt(index).x;
        float deltaT = experiment.getRunValueAt(index + 1) - experiment.getRunValueAt(index);
        if (experimentAnalysis.getExperiment().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return delta / deltaT;
    }
}
