/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views.table;

import nz.ac.aucklanduni.physics.lablet.experiment.Experiment;


public class SpeedTimeDataTableColumn extends TimeDataTableColumn {
    @Override
    public int size() {
        return markersDataModel.getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        Experiment experiment = experimentAnalysis.getExperiment();
        float t0 = experiment.getRunValueAt(index);
        float t1 = experiment.getRunValueAt(index + 1);
        return t0 + (t1 - t0) / 2.f;
    }
}