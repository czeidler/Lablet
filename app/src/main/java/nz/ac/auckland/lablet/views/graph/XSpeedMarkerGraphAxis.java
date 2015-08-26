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
 * Graph axis for the marker data graph adapter. Provides the x-speed.
 */
public class XSpeedMarkerGraphAxis extends MarkerTimeGraphAxis {
    final private Unit xUnit;
    final private Unit tUnit;
    final private Unit vUnit = new Unit("m/s");

    public XSpeedMarkerGraphAxis(Unit xUnit, Unit tUnit) {
        this.xUnit = xUnit;
        this.tUnit = tUnit;
    }

    @Override
    public int size() {
        return getData().getDataCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        PointDataList data = getData();

        float deltaX = data.getRealMarkerPositionAt(index + 1).x - data.getRealMarkerPositionAt(index).x;
        float deltaT = getTimeData().getTimeAt(index + 1) - getTimeData().getTimeAt(index);
        deltaX *= Math.pow(10, xUnit.getBaseExponent());
        deltaT *= Math.pow(10, tUnit.getBaseExponent());
        return deltaX / deltaT;
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
