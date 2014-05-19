/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.views.graph;

import android.graphics.PointF;
import nz.ac.aucklanduni.physics.tracker.experiment.Calibration;


public class XPositionMarkerGraphAxis extends MarkerGraphAxis {
    @Override
    public int size() {
        return getData().getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        return getData().getCalibratedMarkerPositionAt(index).x;
    }

    @Override
    public String getLabel() {
        return "x [" + getExperimentAnalysis().getXUnit() + "]";
    }

    @Override
    public Number getMinRange() {
        Calibration calibration = getExperimentAnalysis().getCalibration();
        PointF point = new PointF();
        point.x = getExperimentAnalysis().getExperiment().getMaxRawX();
        point = calibration.fromRawLength(point);
        return point.x * 0.2f;
    }
}
