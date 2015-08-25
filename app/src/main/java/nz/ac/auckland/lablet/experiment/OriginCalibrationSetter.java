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
public class OriginCalibrationSetter {
    private CalibrationXY calibrationXY;
    private PointDataModel calibrationMarkers;

    private PointDataModel.IListener dataListener = new PointDataModel.IListener() {
        @Override
        public void onDataAdded(PointDataModel model, int index) {
            calibrate();
        }

        @Override
        public void onDataRemoved(PointDataModel model, int index, MarkerData data) {
            calibrate();
        }

        @Override
        public void onDataChanged(PointDataModel model, int index, int number) {
            calibrate();
        }

        @Override
        public void onAllDataChanged(PointDataModel model) {
            calibrate();
        }

        @Override
        public void onDataSelected(PointDataModel model, int index) {

        }
    };

    public OriginCalibrationSetter(CalibrationXY calibrationXY, PointDataModel data) {
        this.calibrationXY = calibrationXY;
        this.calibrationMarkers = data;
        this.calibrationMarkers.addListener(dataListener);

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

}
