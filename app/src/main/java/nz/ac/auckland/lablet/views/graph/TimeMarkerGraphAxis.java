/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.data.PointDataList;
import nz.ac.auckland.lablet.misc.Unit;


/**
 * Graph axis for the marker data graph adapter. Provides the time.
 */
public class TimeMarkerGraphAxis extends MarkerTimeGraphAxis {
    final private Unit tUnit;

    public TimeMarkerGraphAxis(Unit tUnit) {
        this.tUnit = tUnit;
    }

    @Override
    public int size() {
        return getData().size();
    }

    @Override
    public Number getValue(int index) {
        PointDataList markerData = getData();
        int runId = markerData.getDataAt(index).getFrameId();
        return getTimeData().getTimeAt(runId);
    }

    @Override
    public String getTitle() {
        return tUnit.getName();
    }

    @Override
    public Unit getUnit() {
        return tUnit;
    }

    @Override
    public Number getMinRange() {
        return -1;
    }
}
