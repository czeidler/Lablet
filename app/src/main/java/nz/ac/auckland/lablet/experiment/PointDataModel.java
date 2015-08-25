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


public class PointDataModel extends WeakListenable<PointDataModel.IListener> {
    public interface IListener {
        public void onDataAdded(PointDataModel model, int index);
        public void onDataRemoved(PointDataModel model, int index, PointData data);
        public void onDataChanged(PointDataModel model, int index, int number);
        public void onAllDataChanged(PointDataModel model);
        public void onDataSelected(PointDataModel model, int index);
    }

    final protected List<PointData> markerDataList = new ArrayList<>();
    private int selectedDataIndex = -1;
    private boolean visibility = true;

    public void setMarkerDataList(List<PointData> markerDataList) {
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

    public void selectMarkerData(PointData markerData) {
        int index = markerDataList.indexOf(markerData);
        if (index < 0)
            return;
        selectMarkerData(index);
    }

    public int getSelectedMarkerData() {
        return selectedDataIndex;
    }

    public void setVisibility(boolean visibility)
    {
        this.visibility = visibility;
    }

    public boolean isVisible()
    {
        return this.visibility;
    }

    public void setMarkerPosition(PointF position, int index) {
        PointData data = getMarkerDataAt(index);
        data.setPosition(position);
        notifyDataChanged(index, 1);
    }

    public int addMarkerData(PointData data) {
        return addMarkerData(data, true);
    }

    public int addMarkerData(PointData data, boolean sort) {
        int i = 0;
        if (sort) {
            for (; i < markerDataList.size(); i++) {
                MarkerData current = markerDataList.get(i);
                if (current.getFrameId() == data.getFrameId())
                    return -1;
                if (current.getFrameId() > data.getFrameId())
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
            if (markerData.getFrameId() > runId)
                runId = markerData.getFrameId();
        }
        return runId;
    }

    public int getMarkerCount() {
        return markerDataList.size();
    }

    public PointData getMarkerDataAt(int index) {
        return markerDataList.get(index);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        int[] runIds = new int[getMarkerCount()];
        float[] xPositions = new float[getMarkerCount()];
        float[] yPositions = new float[getMarkerCount()];
        for (int i = 0; i < getMarkerCount(); i++) {
            PointData data = getMarkerDataAt(i);
            runIds[i] = data.getFrameId();
            xPositions[i] = data.getPosition().x;
            yPositions[i] = data.getPosition().y;
        }
        bundle.putIntArray("runIds", runIds);
        bundle.putFloatArray("xPositions", xPositions);
        bundle.putFloatArray("yPositions", yPositions);
        bundle.putBoolean("visibility", this.visibility);
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
                PointData data = new PointData(runIds[i]);
                data.getPosition().set(xPositions[i], yPositions[i]);
                addMarkerData(data, false);
            }
        }
    }

    public int findMarkerDataByRun(int run) {
        for (int i = 0; i < getMarkerCount(); i++) {
            MarkerData data = getMarkerDataAt(i);
            if (data.getFrameId() == run)
                return i;
        }
        return -1;
    }

    public PointF getRealMarkerPositionAt(int index) {
        PointData data = getMarkerDataAt(index);
        return data.getPosition();
    }

    public PointData removeMarkerData(int index) {
        PointData data = markerDataList.remove(index);
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
        sort(new Comparator<PointData>() {
            @Override
            public int compare(PointData markerData, PointData markerData2) {
                return (int)(markerData.getPosition().x - markerData2.getPosition().x);
            }
        });
    }

    public void sortYAscending() {
        sort(new Comparator<PointData>() {
            @Override
            public int compare(PointData markerData, PointData markerData2) {
                return (int)(markerData.getPosition().y - markerData2.getPosition().y);
            }
        });
    }

    public void notifyDataAdded(int index) {
        for (IListener listener : getListeners())
            listener.onDataAdded(this, index);
    }

    public void notifyDataRemoved(int index, PointData data) {
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
