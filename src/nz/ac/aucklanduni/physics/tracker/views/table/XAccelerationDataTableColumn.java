/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.views.table;

import nz.ac.aucklanduni.physics.tracker.Experiment;
import nz.ac.aucklanduni.physics.tracker.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.tracker.MarkersDataModel;


public class XAccelerationDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markersDataModel.getMarkerCount() - 2;
    }

    @Override
    public Number getValue(int index) {
        float speed0 = XSpeedDataTableColumn.getSpeed(index, markersDataModel, experimentAnalysis).floatValue();
        float speed1 = XSpeedDataTableColumn.getSpeed(index + 1, markersDataModel, experimentAnalysis).floatValue();
        float delta = speed1 - speed0;

        Experiment experiment = experimentAnalysis.getExperiment();
        float deltaT = (experiment.getRunValueAt(index + 2) - experiment.getRunValueAt(index)) / 2;
        if (experimentAnalysis.getExperiment().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;

        return delta / deltaT;
    }

    @Override
    public String getHeader() {
        return "acceleration [" + experimentAnalysis.getXUnit() + "/"
                + experimentAnalysis.getExperiment().getRunValueBaseUnit() + "^2]";
    }
}
