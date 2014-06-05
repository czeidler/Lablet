/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;


import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;

/**
 * Graph axis for the marker data graph adapter. Provides the x-speed.
 */
public class XSpeedMarkerGraphAxis extends MarkerGraphAxis {
    @Override
    public int size() {
        return getData().getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        ExperimentAnalysis experimentAnalysis = getExperimentAnalysis();
        MarkerDataModel data = getData();

        ExperimentData experimentData = experimentAnalysis.getExperimentData();
        float deltaX = data.getCalibratedMarkerPositionAt(index + 1).x - data.getCalibratedMarkerPositionAt(index).x;
        float deltaT = experimentData.getRunValueAt(index + 1) - experimentData.getRunValueAt(index);
        if (experimentAnalysis.getExperimentData().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return deltaX / deltaT;
    }

    @Override
    public String getLabel() {
        ExperimentAnalysis experimentAnalysis = getExperimentAnalysis();
        return "velocity [" + experimentAnalysis.getXUnit() + "/"
                + experimentAnalysis.getExperimentData().getRunValueBaseUnit() + "]";
    }

    @Override
    public Number getMinRange() {
        return 3;
    }
}
