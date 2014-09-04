/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.experiment.CalibratedMarkerDataModel;

/**
 * Graph axis for the marker data graph adapter. Provides the x-speed.
 */
public class XSpeedMarkerGraphAxis extends MarkerTimeGraphAxis {
    @Override
    public int size() {
        return getData().getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        CalibratedMarkerDataModel data = getData();

        float deltaX = data.getRealMarkerPositionAt(index + 1).x - data.getRealMarkerPositionAt(index).x;
        float deltaT = getTimeCalibration().getTimeFromRaw(index + 1) - getTimeCalibration().getTimeFromRaw(index);
        if (getTimeCalibration().getUnit().getPrefix().equals("m"))
            deltaT /= 1000;
        return deltaX / deltaT;
    }

    @Override
    public String getLabel() {
        return "velocity [" + getData().getCalibrationXY().getXUnit().getUnit() + "/"
                + getTimeCalibration().getUnit().getBase() + "]";
    }

    @Override
    public Number getMinRange() {
        return 3;
    }
}
