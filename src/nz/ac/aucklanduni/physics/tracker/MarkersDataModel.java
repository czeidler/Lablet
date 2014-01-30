/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;


class MarkerData {
    private int runId;
    private PointF positionReal;

    public MarkerData(int run) {
        runId = run;
        positionReal = new PointF();
    }

    public int getRunId() {
        return runId;
    }

    public PointF getPosition() {
        return positionReal;
    }

    public void setPosition(PointF positionReal) {
        this.positionReal.set(positionReal);
    }
}

public class MarkersDataModel implements Calibration.ICalibrationListener {
    interface IMarkersDataModelListener {
        public void onDataAdded(MarkersDataModel model, int index);
        public void onDataRemoved(MarkersDataModel model, int index, MarkerData data);
        public void onDataChanged(MarkersDataModel model, int index, int number);
        public void onAllDataChanged(MarkersDataModel model);
        public void onDataSelected(MarkersDataModel model, int index);
    }

    private List<MarkerData> markerDataList;
    private List<IMarkersDataModelListener> listeners;
    private int selectedDataIndex = -1;
    private Calibration calibration = null;

    public MarkersDataModel() {
        markerDataList = new ArrayList<MarkerData>();
        listeners = new ArrayList<IMarkersDataModelListener>();
    }

    /**
     * If calibration is set, listeners get an onAllDataChanged notification when the calibration changed.
     * @param calibration the calibration to use in getCalibratedMarkerPositionAt
     */
    public void setCalibration(Calibration calibration) {
        if (this.calibration != null)
            this.calibration.removeListener(this);
        this.calibration = calibration;
        this.calibration.addListener(this);
        onCalibrationChanged();
    }

    @Override
    public void onCalibrationChanged() {
        notifyDataChanged(0, markerDataList.size());
    }

    public PointF getCalibratedMarkerPositionAt(int index) {
        MarkerData data = getMarkerDataAt(index);
        PointF raw = data.getPosition();
        if (calibration == null)
            return raw;
        return new PointF(calibration.fromXRaw(raw.x), calibration.fromYRaw(raw.y));
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

    public void addListener(IMarkersDataModelListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(IMarkersDataModelListener listener) {
        return listeners.remove(listener);
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
        for (IMarkersDataModelListener listener : listeners)
            listener.onDataAdded(this, index);
    }

    public void notifyDataRemoved(int index, MarkerData data) {
        for (IMarkersDataModelListener listener : listeners)
            listener.onDataRemoved(this, index, data);
    }

    public void notifyDataChanged(int index, int number) {
        for (IMarkersDataModelListener listener : listeners)
            listener.onDataChanged(this, index, number);
    }

    public void notifyAllDataChanged() {
        for (IMarkersDataModelListener listener : listeners)
            listener.onAllDataChanged(this);
    }

    private void notifyDataSelected(int index) {
        for (IMarkersDataModelListener listener : listeners)
            listener.onDataSelected(this, index);
    }
}