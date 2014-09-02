/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.graphics.PointF;


/**
 * Manages the origin position.
 * <p>
 * <ol>
 * <li>
 * Monitors the origin marker model and re-calibrates when there are changes. For example, when the user moved the
 * origin.
 * </li>
 * <li>
 * The position and rotation of the origin can be set directly, OriginCalibrationSetter then updates the calibration as
 * well as the origin markers.
 * </li>
 * </ol>
 * </p>
 */
public class OriginCalibrationSetter implements MarkerDataModel.IListener {
    private CalibrationXY calibrationXY;
    private MarkerDataModel calibrationMarkers;

    public OriginCalibrationSetter(CalibrationXY calibrationXY, MarkerDataModel data) {
        this.calibrationXY = calibrationXY;
        this.calibrationMarkers = data;
        this.calibrationMarkers.addListener(this);

        calibrate();
    }

    public void setOrigin(PointF origin, PointF axis1) {
        calibrationXY.setOrigin(origin, axis1);
        calibrationMarkers.setMarkerPosition(origin, 0);
        calibrationMarkers.setMarkerPosition(axis1, 1);
    }

    private void calibrate() {
        if (calibrationMarkers.getMarkerCount() != 3)
            return;
        PointF origin = calibrationMarkers.getMarkerDataAt(0).getPosition();
        PointF axis1 = calibrationMarkers.getMarkerDataAt(1).getPosition();

        calibrationXY.setOrigin(origin, axis1);
    }

    @Override
    public void onDataAdded(MarkerDataModel model, int index) {
        calibrate();
    }

    @Override
    public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {
        calibrate();
    }

    @Override
    public void onDataChanged(MarkerDataModel model, int index, int number) {
        calibrate();
    }

    @Override
    public void onAllDataChanged(MarkerDataModel model) {
        calibrate();
    }

    @Override
    public void onDataSelected(MarkerDataModel model, int index) {

    }
}
