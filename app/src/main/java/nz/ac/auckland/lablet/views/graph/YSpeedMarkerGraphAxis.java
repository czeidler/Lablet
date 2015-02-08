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
 * Graph axis for the marker data graph adapter. Provides the y-speed.
 */
public class YSpeedMarkerGraphAxis extends MarkerTimeGraphAxis {
    final private Unit yUnit;
    final private Unit tUnit;
    final private Unit vUnit = new Unit("m/s");

    public YSpeedMarkerGraphAxis(Unit yUnit, Unit tUnit) {
        this.yUnit = yUnit;
        this.tUnit = tUnit;
    }

    @Override
    public int size() {
        return getData().getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        float deltaY = getData().getRealMarkerPositionAt(index + 1).y - getData().getRealMarkerPositionAt(index).y;
        float deltaT = getTimeData().getTimeAt(index + 1) - getTimeData().getTimeAt(index);
        deltaY *= Math.pow(10, yUnit.getBaseExponent());
        deltaT *= Math.pow(10, tUnit.getBaseExponent());
        return deltaY / deltaT;
    }

    @Override
    public String getTitle() {
        return "velocity";
    }

    @Override
    public Unit getUnit() {
        return vUnit;
    }

    @Override
    public Number getMinRange() {
        return 3;
    }
}
