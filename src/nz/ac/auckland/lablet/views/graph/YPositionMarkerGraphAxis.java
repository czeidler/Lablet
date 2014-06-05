/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import android.graphics.PointF;
import nz.ac.auckland.lablet.experiment.Calibration;


/**
 * Graph axis for the marker data graph adapter. Provides the y-position.
 */
public class YPositionMarkerGraphAxis extends MarkerGraphAxis {
    @Override
    public int size() {
        return getData().getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        return getData().getCalibratedMarkerPositionAt(index).y;
    }

    @Override
    public String getLabel() {
        return "y [" + getExperimentAnalysis().getYUnit() + "]";
    }

    @Override
    public Number getMinRange() {
        Calibration calibration = getExperimentAnalysis().getCalibration();
        PointF point = new PointF();
        point.y = getExperimentAnalysis().getExperimentData().getMaxRawY();
        point = calibration.fromRawLength(point);
        return point.y * 0.2f;
    }
}
