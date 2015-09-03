/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;


import nz.ac.auckland.lablet.misc.Unit;

/**
 * Graph axis for the marker data graph adapter. Provides the time for a speed vs time graph.
 */
public class SpeedTimeMarkerGraphAxis extends TimeMarkerGraphAxis {
    public SpeedTimeMarkerGraphAxis(Unit tUnit) {
        super(tUnit);
    }

    @Override
    public int size() {
        return getData().size() - 1;
    }

    @Override
    public Number getValue(int index) {
        float t1 = getTimeData().getTimeAt(index + 1);
        float t0 = getTimeData().getTimeAt(index);
        return t0 + (t1 - t0) / 2f;
    }

    @Override
    public Number getMinRange() {
        return -1;
    }
}
