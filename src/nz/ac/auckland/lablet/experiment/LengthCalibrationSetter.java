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
 * Class to manage the length calibration scale.
 * <p>
 * <ol>
 * <li>
 * Monitors the scale marker model and re-calibrates when there are changes. For example, when the user moved the scale.
 * </li>
 * <li>
 * One can set the length of the calibration scale and LengthCalibrationSetter automatically sets the x and y scale
 * values of the {@link CalibrationXY} class. For example, the length has been
 * updated in the calibration dialog.
 * </li>
 * </ol>
 * </p>
 */
public class LengthCalibrationSetter implements MarkerDataModel.IListener {
    private CalibrationXY calibrationXY;
    private MarkerDataModel calibrationMarkers;

    private float calibrationValue;

    public LengthCalibrationSetter(MarkerDataModel data) {
        this.calibrationXY = data.getCalibrationXY();
        this.calibrationMarkers = data;
        this.calibrationMarkers.addListener(this);

        calibrationValue = 1;
        calibrate();
    }

    public void setCalibrationValue(float value) {
        calibrationValue = value;
        calibrate();
    }

    public float getCalibrationValue() {
        return calibrationValue;
    }

    public CalibrationXY getCalibrationXY() {
        return calibrationXY;
    }

    public float scaleLength() {
        PointF point1 = calibrationMarkers.getMarkerDataAt(0).getPosition();
        PointF point2 = calibrationMarkers.getMarkerDataAt(1).getPosition();

        return  (float)Math.sqrt(Math.pow(point1.x - point2.x, 2) +  Math.pow(point1.y - point2.y, 2));
    }

    private void calibrate() {
        if (calibrationMarkers.getMarkerCount() != 2)
            return;
        float value = calibrationValue / scaleLength();
        calibrationXY.setScale(value, value);
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
