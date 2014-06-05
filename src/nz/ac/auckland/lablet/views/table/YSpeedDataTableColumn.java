/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Table column for the marker data table adapter. Provides the y-speed.
 */
public class YSpeedDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markerDataModel.getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        return getSpeed(index, markerDataModel, experimentAnalysis);
    }

    @Override
    public String getHeader() {
        return "velocity [" + experimentAnalysis.getYUnit() + "/"
                + experimentAnalysis.getExperimentData().getRunValueBaseUnit() + "]";
    }

    public static Number getSpeed(int index, MarkerDataModel markersDataModel, ExperimentAnalysis experimentAnalysis) {
        ExperimentData experimentData = experimentAnalysis.getExperimentData();
        float delta = markersDataModel.getCalibratedMarkerPositionAt(index + 1).y
                - markersDataModel.getCalibratedMarkerPositionAt(index).y;
        float deltaT = experimentData.getRunValueAt(index + 1) - experimentData.getRunValueAt(index);
        if (experimentAnalysis.getExperimentData().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return delta / deltaT;
    }
}
