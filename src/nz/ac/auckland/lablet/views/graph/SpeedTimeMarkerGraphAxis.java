/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;


/**
 * Graph axis for the marker data graph adapter. Provides the time for a speed vs time graph.
 */
public class SpeedTimeMarkerGraphAxis extends TimeMarkerGraphAxis {
    @Override
    public int size() {
        return getData().getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        float t1 = getExperimentAnalysis().getExperimentData().getRunValueAt(index + 1);
        float t0 = getExperimentAnalysis().getExperimentData().getRunValueAt(index);
        return t0 + (t1 - t0) / 2f;
    }

    @Override
    public Number getMinRange() {
        return -1;
    }
}
