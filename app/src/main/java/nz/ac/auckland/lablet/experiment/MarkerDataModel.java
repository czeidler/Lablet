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


abstract class AbstractPointDataModel<T> extends WeakListenable<AbstractPointDataModel.IListener> {
    /**
     * Listener interface for the marker data model.
     */
    public interface IListener<List extends AbstractPointDataModel, T> {
        void onDataAdded(List model, int index);
        void onDataRemoved(List model, int index, T data);
        void onDataChanged(List model, int index, int number);
        void onAllDataChanged(List model);
        void onDataSelected(List model, int index);
    }

    private int selectedDataIndex = -1;

    abstract public int size();

    public void selectMarkerData(int index) {
        if (selectedDataIndex == index)
            return;
        selectedDataIndex = index;
        notifyDataSelected(index);
    }

    public int getSelectedMarkerData() {
        return selectedDataIndex;
    }

    abstract public T getAt(int index);

    abstract public PointF getPosition(int index);

    abstract protected int addDataNoNotify(T data);
    abstract protected T removeDataNoNotify(int index);
    abstract protected void clearNoNotify();

    public int addData(T data) {
        int i = addDataNoNotify(data);
        notifyDataAdded(i);
        return i;
    }

    public T removeData(int index) {
        T data = removeDataNoNotify(index);
        notifyDataRemoved(index, data);
        if (index == selectedDataIndex)
            selectMarkerData(-1);
        return data;
    }

    public void clear() {
        clearNoNotify();
        selectedDataIndex = -1;
        notifyAllDataChanged();
    }

    public void notifyDataAdded(int index) {
        for (IListener listener : getListeners())
            listener.onDataAdded(this, index);
    }

    public void notifyDataRemoved(int index, T data) {
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

abstract class AbstractPointDataList<T> extends AbstractPointDataModel<T> {
    protected List<T> list = new ArrayList<>();

    public void setMarkerDataList(List<T> dataList) {
        this.list.clear();
        this.list.addAll(dataList);
        notifyAllDataChanged();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public T getAt(int index) {
        return list.get(index);
    }

    @Override
    protected int addDataNoNotify(T data) {
        int i = list.size();
        list.add(data);
        return i;
    }

    @Override
    protected T removeDataNoNotify(int index) {
        return list.remove(index);
    }

    @Override
    protected void clearNoNotify() {
        list.clear();
    }
}


/**
 * Data model for a list of marker.
 */
public class MarkerDataModel extends AbstractPointDataList<MarkerData> {
    /**
     * Listener interface for the marker data model.
     */
    public interface IListener extends AbstractPointDataModel.IListener<MarkerDataModel, MarkerData> {
        void onDataAdded(MarkerDataModel model, int index);
        void onDataRemoved(MarkerDataModel model, int index, MarkerData data);
        void onDataChanged(MarkerDataModel model, int index, int number);
        void onAllDataChanged(MarkerDataModel model);
        void onDataSelected(MarkerDataModel model, int index);
    }

    public void selectMarkerData(MarkerData markerData) {
        int index = list.indexOf(markerData);
        if (index < 0)
            return;
        selectMarkerData(index);
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
            for (; i < list.size(); i++) {
                MarkerData current = list.get(i);
                if (current.getId() == data.getId())
                    return -1;
                if (current.getId() > data.getId())
                    break;
            }
        } else
            i = list.size();

        list.add(i, data);
        notifyDataAdded(i);
        return i;
    }

    public int getLargestRunId() {
        int runId = -1;
        for (MarkerData markerData : list) {
            if (markerData.getId() > runId)
                runId = markerData.getId();
        }
        return runId;
    }

    public int getMarkerCount() {
        return size();
    }

    public MarkerData getMarkerDataAt(int index) {
        return getAt(index);
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

    @Override
    public PointF getPosition(int index) {
        return getRealMarkerPositionAt(index);
    }

    public PointF getRealMarkerPositionAt(int index) {
        MarkerData data = getMarkerDataAt(index);
        return data.getPosition();
    }

    public MarkerData removeMarkerData(int index) {
        return removeData(index);
    }

    public void sort(Comparator<? super MarkerData> comparator) {
        Collections.sort(list, comparator);
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
}
