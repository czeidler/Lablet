package com.example.AndroidPhysicsTracker;


import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;


public class MarkersDataModel {
    interface IMarkersDataModelListener {
        public void onDataAdded(MarkersDataModel model, int index);
        public void onDataRemoved(MarkersDataModel model, int index, MarkerData data);
        public void onDataChanged(MarkersDataModel model, int index);
        public void onDataSelected(MarkersDataModel model, int index);
    }

    private List<MarkerData> markerDataList;
    private List<IMarkersDataModelListener> listeners;
    private int selectedDataIndex = -1;

    public MarkersDataModel() {
        markerDataList = new ArrayList<MarkerData>();
        listeners = new ArrayList<IMarkersDataModelListener>();
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
        data.positionReal = position;
        notifyDataChanged(index);
    }

    public void addListener(IMarkersDataModelListener listener) {
        listeners.add(listener);
    }

    public void addMarkerData(MarkerData data) {
        markerDataList.add(data);
        notifyDataAdded(markerDataList.size() - 1);
    }

    public void addMarkerData(int index, MarkerData data) {
        markerDataList.add(index, data);
        notifyDataAdded(index);
    }

    public int getMarkerCount() {
        return markerDataList.size();
    }

    public MarkerData getMarkerDataAt(int index) {
        return markerDataList.get(index);
    }

    public MarkerData removeMarkerData(int index) {
        MarkerData data = markerDataList.remove(index);
        notifyDataRemoved(index, data);
        return data;
    }

    public void notifyDataAdded(int index) {
        for (IMarkersDataModelListener listener : listeners)
            listener.onDataAdded(this, index);
    }

    public void notifyDataRemoved(int index, MarkerData data) {
        for (IMarkersDataModelListener listener : listeners)
            listener.onDataRemoved(this, index, data);
    }

    public void notifyDataChanged(int index) {
        for (IMarkersDataModelListener listener : listeners)
            listener.onDataChanged(this, index);
    }

    private void notifyDataSelected(int index) {
        for (IMarkersDataModelListener listener : listeners)
            listener.onDataSelected(this, index);
    }
}