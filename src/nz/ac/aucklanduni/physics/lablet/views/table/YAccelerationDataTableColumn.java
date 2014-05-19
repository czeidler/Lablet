/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views.table;

import nz.ac.aucklanduni.physics.lablet.experiment.Experiment;


public class YAccelerationDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markersDataModel.getMarkerCount() - 2;
    }

    @Override
    public Number getValue(int index) {
        float speed0 = YSpeedDataTableColumn.getSpeed(index, markersDataModel, experimentAnalysis).floatValue();
        float speed1 = YSpeedDataTableColumn.getSpeed(index + 1, markersDataModel, experimentAnalysis).floatValue();
        float delta = speed1 - speed0;

        Experiment experiment = experimentAnalysis.getExperiment();
        float deltaT = (experiment.getRunValueAt(index + 2) - experiment.getRunValueAt(index)) / 2;
        if (experimentAnalysis.getExperiment().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;

        return delta / deltaT;
    }

    @Override
    public String getHeader() {
        return "acceleration [" + experimentAnalysis.getYUnit() + "/"
                + experimentAnalysis.getExperiment().getRunValueBaseUnit() + "^2]";
    }
}
