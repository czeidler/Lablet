/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.RectF;
import nz.ac.auckland.lablet.misc.WeakListenable;


public class DataStatistics extends WeakListenable<DataStatistics.IListener>
        implements AbstractPlotDataAdapter.IListener {

    public interface IListener {
        public void onLimitsChanged(DataStatistics dataStatistics);
    }

    final private AbstractXYDataAdapter adapter;
    private RectF dataLimits = null;
    private float sumX = 0;
    private float sumY = 0;

    private void notifyLimitsChanged() {
        for (IListener listener : getListeners())
            listener.onLimitsChanged(this);
    }

    public DataStatistics(AbstractXYDataAdapter adapter) {
        this.adapter = adapter;
        this.adapter.addListener(this);

        reset();
    }

    @Override
    public void addListener(DataStatistics.IListener listener) {
        super.addListener(listener);

        if (dataLimits != null)
            listener.onLimitsChanged(this);
    }

    public void release() {
        adapter.removeListener(this);
    }

    @Override
    public void finalize() {
        release();
    }

    public AbstractXYDataAdapter getAdapter() {
        return adapter;
    }

    public RectF getDataLimits() {
        if (dataLimits == null)
            return null;
        return new RectF(dataLimits);
    }

    public float getAverageX() {
        return sumX / adapter.getSize();
    }

    public float getAverageY() {
        return sumY / adapter.getSize();
    }

    private void includePoint(float x, float y) {
        if (dataLimits == null) {
            dataLimits = new RectF(x, y, x, y);
            sumX = x;
            sumY = y;
            notifyLimitsChanged();
            return;
        }

        boolean limitsChanged = false;
        if (dataLimits.left > x) {
            dataLimits.left = x;
            limitsChanged = true;
        }
        if (dataLimits.right < x) {
            dataLimits.right = x;
            limitsChanged = true;
        }
        if (dataLimits.top < y) {
            dataLimits.top = y;
            limitsChanged = true;
        }
        if (dataLimits.bottom > y) {
            dataLimits.bottom = y;
            limitsChanged = true;
        }

        if (limitsChanged)
            notifyLimitsChanged();

        sumX += x;
        sumY += y;
    }

    private boolean isInLimit(float x, float y) {
        return dataLimits.contains(x, y);
    }

    private void reset() {
        dataLimits = null;

        for (int i = 0; i < adapter.getSize(); i++) {
            float x = adapter.getX(i);
            float y = adapter.getY(i);
            includePoint(x, y);
        }
    }

    @Override
    public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number) {
        for (int i = 0; i < number; i++) {
            float x = adapter.getX(index + i);
            float y = adapter.getY(index + i);
            includePoint(x, y);
        }
    }

    @Override
    public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number) {
        reset();
    }

    @Override
    public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number) {
        for (int i = 0; i < number; i++) {
            float x = adapter.getX(index + i);
            float y = adapter.getY(index + i);
            if (!isInLimit(x, y)) {
                reset();
                return;
            }
        }
    }

    @Override
    public void onAllDataChanged(AbstractPlotDataAdapter plot) {
        reset();
    }

}
