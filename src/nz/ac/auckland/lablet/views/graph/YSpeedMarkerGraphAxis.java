/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

/**
 * Graph axis for the marker data graph adapter. Provides the y-speed.
 */
public class YSpeedMarkerGraphAxis extends MarkerTimeGraphAxis {
    @Override
    public int size() {
        return getData().getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        float deltaX = getData().getCalibratedMarkerPositionAt(index + 1).y - getData().getCalibratedMarkerPositionAt(index).y;
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
