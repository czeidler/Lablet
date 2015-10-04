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


public abstract class AbstractPointDataModel<T> extends WeakListenable<AbstractPointDataModel.IListener> {
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
    abstract public int indexOf(T data);

    abstract public PointF getPosition(T data);
    public void setPosition(T data, PointF point) {
        setPositionNoNotify(data, point);
        notifyDataChanged(indexOf(data), 1);
    }

    public void setPosition(PointF point, int index) {
        T data = getAt(index);
        setPosition(data, point);
    }

    abstract public void setPositionNoNotify(T data, PointF point);
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
