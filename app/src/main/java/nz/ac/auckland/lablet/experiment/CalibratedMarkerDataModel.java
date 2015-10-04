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
 * Data model for the a list of {@link MarkerData} including a xy calibration.
 */
public class CalibratedMarkerDataModel extends MarkerDataModel implements CalibrationXY.IListener {
    private CalibrationXY calibrationXY;

    public CalibratedMarkerDataModel(CalibrationXY calibrationXY) {
        setCalibrationXY(calibrationXY);
    }

    /**
     * If calibration is set, listeners get an onAllDataChanged notification when the calibration changed.
     * @param calibrationXY the calibration to use in getRealMarkerPositionAt
     */
    public void setCalibrationXY(CalibrationXY calibrationXY) {
        if (this.calibrationXY != null)
            this.calibrationXY.removeListener(this);
        this.calibrationXY = calibrationXY;
        this.calibrationXY.addListener(this);
        onCalibrationChanged();
    }

    @Override
    public void onCalibrationChanged() {
        notifyDataChanged(0, list.size());
    }

    @Override
    public PointF getRealMarkerPositionAt(int index) {
        MarkerData data = getMarkerDataAt(index);
        PointF raw = data.getPosition();
        if (calibrationXY == null)
            return raw;
        return calibrationXY.fromRaw(raw);
    }
}