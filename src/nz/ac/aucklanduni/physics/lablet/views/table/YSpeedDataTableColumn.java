/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views.table;

import nz.ac.aucklanduni.physics.lablet.experiment.Experiment;
import nz.ac.aucklanduni.physics.lablet.experiment.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.lablet.experiment.MarkersDataModel;


public class YSpeedDataTableColumn extends DataTableColumn {
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
        return "velocity [" + experimentAnalysis.getYUnit() + "/"
                + experimentAnalysis.getExperiment().getRunValueBaseUnit() + "]";
    }

    public static Number getSpeed(int index, MarkersDataModel markersDataModel, ExperimentAnalysis experimentAnalysis) {
        Experiment experiment = experimentAnalysis.getExperiment();
        float delta = markersDataModel.getCalibratedMarkerPositionAt(index + 1).y
                - markersDataModel.getCalibratedMarkerPositionAt(index).y;
        float deltaT = experiment.getRunValueAt(index + 1) - experiment.getRunValueAt(index);
        if (experimentAnalysis.getExperiment().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return delta / deltaT;
    }
}
