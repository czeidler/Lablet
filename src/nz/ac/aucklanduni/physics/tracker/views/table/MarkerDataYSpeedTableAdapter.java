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

public class MarkerDataYSpeedTableAdapter extends MarkerDataSpeedTableAdapter {
    public MarkerDataYSpeedTableAdapter(MarkersDataModel model, ExperimentAnalysis experimentAnalysis) {
        super(model, experimentAnalysis);
    }

    public float getSpeed(int index) {
        Experiment experiment = experimentAnalysis.getExperiment();
        float delta = model.getCalibratedMarkerPositionAt(index + 1).y - model.getCalibratedMarkerPositionAt(index).y;
        float deltaT = experiment.getRunValueAt(index + 1) - experiment.getRunValueAt(index);
        if (experimentAnalysis.getExperiment().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return delta / deltaT;
    }

    public String getUnit() {
        return experimentAnalysis.getYUnit();
    }
}
