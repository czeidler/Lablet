/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views.graph;


public class SpeedTimeMarkerGraphAxis extends TimeMarkerGraphAxis {
    @Override
    public int size() {
        return getData().getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        float t1 = getExperimentAnalysis().getExperiment().getRunValueAt(index + 1);
        float t0 = getExperimentAnalysis().getExperiment().getRunValueAt(index);
        return t0 + (t1 - t0) / 2f;
    }

    @Override
    public Number getMinRange() {
        return -1;
    }
}
