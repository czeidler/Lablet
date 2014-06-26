/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.experiment.ExperimentRunData;


/**
 * Table column for the marker data table adapter. Provides the x-acceleration.
 */
public class XAccelerationDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markerDataModel.getMarkerCount() - 2;
    }

    @Override
    public Number getValue(int index) {
        float speed0 = XSpeedDataTableColumn.getSpeed(index, markerDataModel, experimentAnalysis).floatValue();
        float speed1 = XSpeedDataTableColumn.getSpeed(index + 1, markerDataModel, experimentAnalysis).floatValue();
        float delta = speed1 - speed0;

        ExperimentRunData experimentRunData = experimentAnalysis.getExperimentRunData();
        float deltaT = (experimentRunData.getRunValueAt(index + 2) - experimentRunData.getRunValueAt(index)) / 2;
        if (experimentAnalysis.getExperimentRunData().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;

        return delta / deltaT;
    }

    @Override
    public String getHeader() {
        return "acceleration [" + experimentAnalysis.getXUnit() + "/"
                + experimentAnalysis.getExperimentRunData().getRunValueBaseUnit() + "^2]";
    }
}
