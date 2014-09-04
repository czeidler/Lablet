/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import android.graphics.PointF;
import nz.ac.auckland.lablet.experiment.CalibrationXY;


/**
 * Graph axis for the marker data graph adapter. Provides the x position.
 */
public class XPositionMarkerGraphAxis extends MarkerGraphAxis {
    @Override
    public int size() {
        return getData().getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        return getData().getRealMarkerPositionAt(index).x;
    }

    @Override
    public String getLabel() {
        return "x [" + getData().getCalibrationXY().getXUnit().getUnit() + "]";
    }

    @Override
    public Number getMinRange() {
        CalibrationXY calibrationXY = getData().getCalibrationXY();
        PointF point = new PointF();
        point.x = getData().getMaxRangeRaw().x;
        point = calibrationXY.fromRawLength(point);
        return point.x * 0.2f;
    }
}
