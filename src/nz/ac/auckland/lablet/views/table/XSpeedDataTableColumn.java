/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.experiment.ExperimentRunData;
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Table column for the marker data table adapter. Provides the x-speed.
 */
public class XSpeedDataTableColumn extends DataTableColumn {
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
        return "velocity [" + experimentAnalysis.getXUnit() + "/"
                + experimentAnalysis.getExperimentRunData().getRunValueBaseUnit() + "]";
    }

    public static Number getSpeed(int index, MarkerDataModel markersDataModel, ExperimentAnalysis experimentAnalysis) {
        ExperimentRunData experimentRunData = experimentAnalysis.getExperimentRunData();
        float delta = markersDataModel.getCalibratedMarkerPositionAt(index + 1).x
                - markersDataModel.getCalibratedMarkerPositionAt(index).x;
        float deltaT = experimentRunData.getRunValueAt(index + 1) - experimentRunData.getRunValueAt(index);
        if (experimentAnalysis.getExperimentRunData().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return delta / deltaT;
    }
}