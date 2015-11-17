/*
 * Copyright 2013-2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.marker;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractPointDataList<T> extends AbstractPointDataModel<T> {
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

    public int indexOf(T data) {
        return list.indexOf(data);
    }

    public PointF getPosition(T data) {
        return getPosition(indexOf(data));
    }

    public boolean removeData(T data) {
        if (removeData(indexOf(data)) != null)
            return true;
        return false;
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
