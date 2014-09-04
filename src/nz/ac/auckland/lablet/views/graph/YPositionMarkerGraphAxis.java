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
import nz.ac.auckland.lablet.experiment.Unit;


/**
 * Graph axis for the marker data graph adapter. Provides the y-position.
 */
public class YPositionMarkerGraphAxis extends MarkerGraphAxis {
    final private Unit unit;
    final private CalibrationXY calibrationXY;

    public YPositionMarkerGraphAxis(Unit xUnit, CalibrationXY calibrationXY) {
        this.unit = xUnit;
        this.calibrationXY = calibrationXY;
    }

    @Override
    public int size() {
        return getData().getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        return getData().getRealMarkerPositionAt(index).y;
    }

    @Override
    public String getLabel() {
        return unit.getName() + " [" + unit.getUnit() + "]";
    }

    @Override
    public Number getMinRange() {
        if (calibrationXY == null)
            return -1;
        PointF point = new PointF();
        point.y = getData().getMaxRangeRaw().x;
        point = calibrationXY.fromRawLength(point);
        return point.y * 0.2f;
    }
}
