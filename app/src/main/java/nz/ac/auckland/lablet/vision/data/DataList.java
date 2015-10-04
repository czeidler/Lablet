/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision.data;

import android.os.Bundle;
import nz.ac.auckland.lablet.misc.WeakListenable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



public abstract class DataList<D extends Data> extends WeakListenable<DataList.IListener> {

    public interface IListener<DL extends DataList> {
        public void onDataAdded(DL dataList, int index);
        public void onDataRemoved(DL dataList, int index, Data data);
        public void onDataChanged(DL dataList, int index, int number);
        public void onAllDataChanged(DL dataList);
        public void onDataSelected(DL dataList, int index);
    }

    final protected List<D> dataList = new ArrayList<D>();
    private int selectedDataIndex = -1;
    protected boolean visibility = true;

    public void setDataList(List<D> dataList) {
        this.dataList.clear();
        this.dataList.addAll(dataList);
        notifyAllDataChanged();
    }

    public void selectData(int index) {
        if (selectedDataIndex == index)
            return;
        selectedDataIndex = index;
        notifyDataSelected(index);
    }

    public void selectData(D markerData) {
        int index = dataList.indexOf(markerData);
        if (index < 0)
            return;
        selectData(index);
    }

    public int getSelectedData() {
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

    public int addData(D data) {
        return addData(data, true);
    }

    public int addData(D data, boolean sort) {
        int i = 0;
        if (sort) {
            for (; i < dataList.size(); i++) {
                D current = dataList.get(i);
                if (current.getFrameId() == data.getFrameId())
                    return -1;
                if (current.getFrameId() > data.getFrameId())
                    break;
            }
        } else
            i = dataList.size();

        dataList.add(i, data);
        notifyDataAdded(i);
        return i;
    }

    public int getLargestRunId() {
        int runId = -1;
        for (D markerData : dataList) {
            if (markerData.getFrameId() > runId)
                runId = markerData.getFrameId();
        }
        return runId;
    }

    public int size() {
        return dataList.size();
    }

    public D getDataAt(int index) {
        return dataList.get(index);
    }

    public abstract Bundle toBundle();

    public abstract void fromBundle(Bundle bundle);

    public int getIndexByFrameId(int frameId) {
        for (int i = 0; i < size(); i++) {
            D data = getDataAt(i);
            if (data.getFrameId() == frameId)
                return i;
        }
        return -1;
    }

    public D getDataByFrameId(int frameId) {
        for (int i = 0; i < size(); i++) {
            D data = getDataAt(i);
            if (data.getFrameId() == frameId)
                return data;
        }
        return null;
    }

    public D removeData(int index) {
        D data = dataList.remove(index);
        notifyDataRemoved(index, data);
        if (index == selectedDataIndex)
            selectData(-1);
        return data;
    }

    public void removeData(D data) {
        int index = dataList.indexOf(data);
        if (index >= 0)
            removeData(index);
    }

    public void clear() {
        dataList.clear();
        selectedDataIndex = -1;
        notifyAllDataChanged();
    }

    public void sort(Comparator<? super D> comparator) {
        Collections.sort(dataList, comparator);
        notifyAllDataChanged();
    }

    public abstract void sortXAscending();

    public abstract void sortYAscending();


    public void notifyDataAdded(int index) {
        for (IListener listener : getListeners())
            listener.onDataAdded(this, index);
    }

    public void notifyDataRemoved(int index, D data) {
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
