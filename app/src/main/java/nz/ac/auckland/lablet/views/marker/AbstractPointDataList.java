/*
 * Copyright 2013-2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.marker;

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

    @Override
    public int indexOf(T data) {
        return list.indexOf(data);
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
