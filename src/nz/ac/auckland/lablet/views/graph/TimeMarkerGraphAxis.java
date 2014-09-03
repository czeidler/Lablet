/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Graph axis for the marker data graph adapter. Provides the time.
 */
public class TimeMarkerGraphAxis extends MarkerTimeGraphAxis {
    @Override
    public int size() {
        return getData().getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        MarkerDataModel markerData = getData();
        int runId = markerData.getMarkerDataAt(index).getRunId();
        return getTimeCalibration().getTimeFromRaw(runId);
    }

    @Override
    public String getLabel() {
        return "time [" + getTimeCalibration().getUnit().getUnit() + "]";
    }

    @Override
    public Number getMinRange() {
        return -1;
    }
}
