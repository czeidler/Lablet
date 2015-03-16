/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.graphics.PointF;
import android.os.Bundle;
import nz.ac.auckland.lablet.misc.WeakListenable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MarkerDataModel extends WeakListenable<MarkerDataModel.IListener> {
    public interface IListener {
        public void onDataAdded(MarkerDataModel model, int index);
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data);
        public void onDataChanged(MarkerDataModel model, int index, int number);
        public void onAllDataChanged(MarkerDataModel model);
        public void onDataSelected(MarkerDataModel model, int index);
    }

    final protected List<MarkerData> markerDataList = new ArrayList<>();
    private int selectedDataIndex = -1;

    public void setMarkerDataList(List<MarkerData> markerDataList) {
        this.markerDataList.clear();
        this.markerDataList.addAll(markerDataList);
        notifyAllDataChanged();
    }

    public void selectMarkerData(int index) {
        if (selectedDataIndex == index)
            return;
        selectedDataIndex = index;
        notifyDataSelected(index);
    }

    public void selectMarkerData(MarkerData markerData) {
        int index = markerDataList.indexOf(markerData);
        if (index < 0)
            return;
        selectMarkerData(index);
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
        return addMarkerData(data, true);
    }

    public int addMarkerData(MarkerData data, boolean sort) {
        int i = 0;
        if (sort) {
            for (; i < markerDataList.size(); i++) {
                MarkerData current = markerDataList.get(i);
                if (current.getId() == data.getId())
                    return -1;
                if (current.getId() > data.getId())
                    break;
            }
        } else
            i = markerDataList.size();

        markerDataList.add(i, data);
        notifyDataAdded(i);
        return i;
    }

    public int getLargestRunId() {
        int runId = -1;
        for (MarkerData markerData : markerDataList) {
            if (markerData.getId() > runId)
                runId = markerData.getId();
        }
        return runId;
    }

    public int getMarkerCount() {
        return markerDataList.size();
    }

    public MarkerData getMarkerDataAt(int index) {
        return markerDataList.get(index);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        int[] runIds = new int[getMarkerCount()];
        float[] xPositions = new float[getMarkerCount()];
        float[] yPositions = new float[getMarkerCount()];
        for (int i = 0; i < getMarkerCount(); i++) {
            MarkerData data = getMarkerDataAt(i);
            runIds[i] = data.getId();
            xPositions[i] = data.getPosition().x;
            yPositions[i] = data.getPosition().y;
        }
        bundle.putIntArray("runIds", runIds);
        bundle.putFloatArray("xPositions", xPositions);
        bundle.putFloatArray("yPositions", yPositions);
        return bundle;
    }

    public void fromBundle(Bundle bundle) {
        clear();
        int[] runIds = bundle.getIntArray("runIds");
        float[] xPositions = bundle.getFloatArray("xPositions");
        float[] yPositions = bundle.getFloatArray("yPositions");

        if (runIds != null && xPositions != null && yPositions != null && runIds.length == xPositions.length
                && xPositions.length == yPositions.length) {
            for (int i = 0; i < runIds.length; i++) {
                MarkerData data = new MarkerData(runIds[i]);
                data.getPosition().set(xPositions[i], yPositions[i]);
                addMarkerData(data, false);
            }
        }
    }

    public int findMarkerDataByRun(int run) {
        for (int i = 0; i < getMarkerCount(); i++) {
            MarkerData data = getMarkerDataAt(i);
            if (data.getId() == run)
                return i;
        }
        return -1;
    }

    public PointF getRealMarkerPositionAt(int index) {
        MarkerData data = getMarkerDataAt(index);
        return data.getPosition();
    }

    public MarkerData removeMarkerData(int index) {
        MarkerData data = markerDataList.remove(index);
        notifyDataRemoved(index, data);
        if (index == selectedDataIndex)
            selectMarkerData(-1);
        return data;
    }

    public void clear() {
        markerDataList.clear();
        selectedDataIndex = -1;
        notifyAllDataChanged();
    }

    public void sort(Comparator<? super MarkerData> comparator) {
        Collections.sort(markerDataList, comparator);
        notifyAllDataChanged();
    }

    public void sortXAscending() {
        sort(new Comparator<MarkerData>() {
            @Override
            public int compare(MarkerData markerData, MarkerData markerData2) {
                return (int)(markerData.getPosition().x - markerData2.getPosition().x);
            }
        });
    }

    public void sortYAscending() {
        sort(new Comparator<MarkerData>() {
            @Override
            public int compare(MarkerData markerData, MarkerData markerData2) {
                return (int)(markerData.getPosition().y - markerData2.getPosition().y);
            }
        });
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

    public void notifyDataSelected(int index) {
        for (IListener listener : getListeners())
            listener.onDataSelected(this, index);
    }

}
