/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.experiment.Unit;


/**
 * Graph axis for the marker data graph adapter. Provides the x-speed.
 */
public class XSpeedMarkerGraphAxis extends MarkerTimeGraphAxis {
    final private Unit xUnit;
    final private Unit tUnit;

    public XSpeedMarkerGraphAxis(Unit xUnit, Unit tUnit) {
        this.xUnit = xUnit;
        this.tUnit = tUnit;
    }

    @Override
    public int size() {
        return getData().getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        MarkerDataModel data = getData();

        float deltaX = data.getRealMarkerPositionAt(index + 1).x - data.getRealMarkerPositionAt(index).x;
        float deltaT = getTimeData().getTimeAt(index + 1) - getTimeData().getTimeAt(index);
        if (tUnit.getPrefix().equals("m"))
            deltaT /= 1000;
        return deltaX / deltaT;
    }

    @Override
    public String getLabel() {
        return "velocity [" + xUnit.getUnit() + "/" + tUnit.getBase() + "]";
    }

    @Override
    public Number getMinRange() {
        return 3;
    }
}
