package com.example.AndroidPhysicsTracker;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class Calibration {
    interface ICalibrationListener {
        public void onCalibrationChanged();
    }

    private float xCalibration;
    private float yCalibration;

    private List<ICalibrationListener> listeners;

    public Calibration() {
        listeners = new ArrayList<ICalibrationListener>();
        xCalibration = 1;
        yCalibration = 1;
    }

    public void addListener(ICalibrationListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(ICalibrationListener listener) {
        return listeners.remove(listener);
    }

    public float getXCalibration() {
        return xCalibration;
    }

    public float getYCalibration() {
        return yCalibration;
    }

    public float fromXRaw(float rawX) {
        return rawX * xCalibration;
    }

    public float fromYRaw(float rawY) {
        return rawY * yCalibration;
    }

    public void setXCalibration(float xCalibration) {
        this.xCalibration = xCalibration;
        notifyCalibrationChanged();
    }

    public void setYCalibration(float yCalibration) {
        this.yCalibration = yCalibration;
        notifyCalibrationChanged();
    }

    public void setCalibration(float xCalibration, float yCalibration) {
        this.xCalibration = xCalibration;
        this.yCalibration = yCalibration;
        notifyCalibrationChanged();
    }

    private void notifyCalibrationChanged() {
        for (ICalibrationListener listener : listeners)
            listener.onCalibrationChanged();
    }
}


class LengthCalibrationSetter implements MarkersDataModel.IMarkersDataModelListener {
    private Calibration calibration;
    private MarkersDataModel calibrationMarkers;

    private float calibrationValue;

    public LengthCalibrationSetter(Calibration calibration, MarkersDataModel data) {
        this.calibration = calibration;
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

    private void calibrate() {
        if (calibrationMarkers.getMarkerCount() != 2)
            return;
        PointF point1 = calibrationMarkers.getMarkerDataAt(0).getPosition();
        PointF point2 = calibrationMarkers.getMarkerDataAt(1).getPosition();

        float length = (float)Math.sqrt(Math.pow(point1.x - point2.x, 2) +  Math.pow(point1.y - point2.y, 2));
        float value = calibrationValue / length;
        calibration.setCalibration(value, value);
    }

    @Override
    public void onDataAdded(MarkersDataModel model, int index) {
        calibrate();
    }

    @Override
    public void onDataRemoved(MarkersDataModel model, int index, MarkerData data) {
        calibrate();
    }

    @Override
    public void onDataChanged(MarkersDataModel model, int index, int number) {
        calibrate();
    }

    @Override
    public void onAllDataChanged(MarkersDataModel model) {
        calibrate();
    }

    @Override
    public void onDataSelected(MarkersDataModel model, int index) {

    }
}