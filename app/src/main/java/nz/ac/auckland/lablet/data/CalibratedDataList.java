/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.data;

import android.graphics.PointF;

import nz.ac.auckland.lablet.experiment.CalibrationXY;


/**
 * Data model for the a list of {@link Data} including a xy calibration.
 */
public class CalibratedDataList extends PointDataList implements CalibrationXY.IListener {
    private CalibrationXY calibrationXY;

    public CalibratedDataList(CalibrationXY calibrationXY) {
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
        notifyDataChanged(0, dataList.size());
    }

    @Override
    public PointF getRealMarkerPositionAt(int index) {
        PointData data = getDataAt(index);
        PointF raw = data.getPosition();
        if (calibrationXY == null)
            return raw;
        return calibrationXY.fromRaw(raw);
    }
}