/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.graphics.PointF;
import nz.ac.auckland.lablet.misc.WeakListenable;

import java.util.ArrayList;
import java.util.List;


/**
 * Data model for the a list of {@link MarkerData}.
 */
public class MarkerDataModel extends WeakListenable<MarkerDataModel.IListener> implements CalibrationXY.IListener {
    public interface IListener {
        public void onDataAdded(MarkerDataModel model, int index);
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data);
        public void onDataChanged(MarkerDataModel model, int index, int number);
        public void onAllDataChanged(MarkerDataModel model);
        public void onDataSelected(MarkerDataModel model, int index);
    }

    final private List<MarkerData> markerDataList = new ArrayList<>();
    private int selectedDataIndex = -1;
    private CalibrationXY calibrationXY;
    private PointF maxRangeRaw = new PointF(100, 100);

    public MarkerDataModel() {
    }

    /**
     * If calibration is set, listeners get an onAllDataChanged notification when the calibration changed.
     * @param calibrationXY the calibration to use in getCalibratedMarkerPositionAt
     */
    public void setCalibrationXY(CalibrationXY calibrationXY) {
        if (this.calibrationXY != null)
            this.calibrationXY.removeListener(this);
        this.calibrationXY = calibrationXY;
        this.calibrationXY.addListener(this);
        onCalibrationChanged();
    }

    public CalibrationXY getCalibrationXY() {
        return calibrationXY;
    }

    public PointF getMaxRangeRaw() {
        return maxRangeRaw;
    }

    public void setMaxRangeRaw(float x, float y) {
        this.maxRangeRaw.set(x, y);
    }

    @Override
    public void onCalibrationChanged() {
        notifyDataChanged(0, markerDataList.size());
    }

    public PointF getCalibratedMarkerPositionAt(int index) {
        MarkerData data = getMarkerDataAt(index);
        PointF raw = data.getPosition();
        if (calibrationXY == null)
            return raw;
        return calibrationXY.fromRaw(raw);
    }

    public void selectMarkerData(int index) {
        selectedDataIndex = index;
        notifyDataSelected(index);
    }

    public int getSelectedMarkerData() {
        return selectedDataIndex;
    }

    public void setMarkerPosition(PointF position, int index) {
        MarkerData data = getMarkerDataAt(index);
        data.setPosition(position);
        notifyDataChanged(index, 1);
    }

    public int addMarkerData(MarkerData data) {
        int i = 0;
        for (; i < markerDataList.size(); i++) {
            MarkerData current = markerDataList.get(i);
            if (current.getRunId() == data.getRunId())
                return -1;
            if (current.getRunId() > data.getRunId())
                break;
        }
        markerDataList.add(i, data);
        notifyDataAdded(i);
        return i;
    }

    public int getMarkerCount() {
        return markerDataList.size();
    }

    public MarkerData getMarkerDataAt(int index) {
        return markerDataList.get(index);
    }

    public int findMarkerDataByRun(int run) {
        for (int i = 0; i < getMarkerCount(); i++) {
            MarkerData data = getMarkerDataAt(i);
            if (data.getRunId() == run)
                return i;
        }
        return -1;
    }

    public MarkerData removeMarkerData(int index) {
        MarkerData data = markerDataList.remove(index);
        notifyDataRemoved(index, data);
        return data;
    }

    public void clear() {
        markerDataList.clear();
        notifyAllDataChanged();
    }

    public void notifyDataAdded(int index) {
        for (IListener listener : getListeners())
            listener.onDataAdded(this, index);
    }

    public void notifyDataRemoved(int index, MarkerData data) {
        for (IListener listener : getListeners())
            listener.onDataRemoved(this, index, data);
    }

    public void notifyDataChanged(int index, int number) {
        for (IListener listener : getListeners())
            listener.onDataChanged(this, index, number);
    }

    public void notifyAllDataChanged() {
        for (IListener listener : getListeners())
            listener.onAllDataChanged(this);
    }

    private void notifyDataSelected(int index) {
        for (IListener listener : getListeners())
            listener.onDataSelected(this, index);
    }
}