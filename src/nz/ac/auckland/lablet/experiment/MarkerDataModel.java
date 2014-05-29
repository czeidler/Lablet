/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.graphics.PointF;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 * Data model for the a list of {@link MarkerData}.
 */
public class MarkerDataModel implements Calibration.ICalibrationListener {
    public interface IMarkerDataModelListener {
        public void onDataAdded(MarkerDataModel model, int index);
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data);
        public void onDataChanged(MarkerDataModel model, int index, int number);
        public void onAllDataChanged(MarkerDataModel model);
        public void onDataSelected(MarkerDataModel model, int index);
    }

    private List<MarkerData> markerDataList;
    private List<WeakReference<IMarkerDataModelListener>> listeners;
    private int selectedDataIndex = -1;
    private Calibration calibration = null;

    public MarkerDataModel() {
        markerDataList = new ArrayList<MarkerData>();
        listeners = new ArrayList<WeakReference<IMarkerDataModelListener>>();
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
        return calibration.fromRaw(raw);
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

    public void addListener(IMarkerDataModelListener listener) {
        listeners.add(new WeakReference<IMarkerDataModelListener>(listener));
    }

    public boolean removeListener(IMarkerDataModelListener listener) {
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
        for (ListIterator<WeakReference<IMarkerDataModelListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IMarkerDataModelListener listener = it.next().get();
            if (listener != null)
                listener.onDataAdded(this, index);
            else
                it.remove();
        }
    }

    public void notifyDataRemoved(int index, MarkerData data) {
        for (ListIterator<WeakReference<IMarkerDataModelListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IMarkerDataModelListener listener = it.next().get();
            if (listener != null)
                listener.onDataRemoved(this, index, data);
            else
                it.remove();
        }
    }

    public void notifyDataChanged(int index, int number) {
        for (ListIterator<WeakReference<IMarkerDataModelListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IMarkerDataModelListener listener = it.next().get();
            if (listener != null)
                listener.onDataChanged(this, index, number);
            else
                it.remove();
        }
    }

    public void notifyAllDataChanged() {
        for (ListIterator<WeakReference<IMarkerDataModelListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IMarkerDataModelListener listener = it.next().get();
            if (listener != null)
                listener.onAllDataChanged(this);
            else
                it.remove();
        }
    }

    private void notifyDataSelected(int index) {
        for (ListIterator<WeakReference<IMarkerDataModelListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IMarkerDataModelListener listener = it.next().get();
            if (listener != null)
                listener.onDataSelected(this, index);
            else
                it.remove();
        }
    }
}