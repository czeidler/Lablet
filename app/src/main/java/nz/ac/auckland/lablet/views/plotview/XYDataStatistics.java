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


public class XYDataStatistics extends DataStatistics implements AbstractPlotDataAdapter.IListener {
    final private AbstractXYDataAdapter adapter;
    private float sumX = 0;
    private float sumY = 0;
    final private boolean dataIsContinuously;

    public XYDataStatistics(AbstractXYDataAdapter adapter, boolean dataIsContinuously) {
        this.adapter = adapter;
        this.adapter.addListener(this);
        this.dataIsContinuously = dataIsContinuously;

        reset();
    }

    @Override
    public void setListener(DataStatistics.IListener listener) {
        super.setListener(listener);

        if (listener != null && dataLimits != null)
            listener.onLimitsChanged(this);
    }

    @Override
    public void release() {
        adapter.removeListener(this);
    }

    @Override
    public AbstractPlotDataAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void finalize() {
        release();
    }

    public float getAverageX() {
        return sumX / adapter.getSize();
    }

    public float getAverageY() {
        return sumY / adapter.getSize();
    }

    private boolean includePoint(float x, float y) {
        if (dataLimits == null) {
            dataLimits = new RectF(x, y, x, y);
            sumX = x;
            sumY = y;
            return true;
        }

        RectF oldLimits = new RectF(dataLimits);

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
            previousLimits = oldLimits;

        sumX += x;
        sumY += y;

        return limitsChanged;
    }

    private boolean isInLimit(float x, float y) {
        return dataLimits.contains(x, y);
    }

    private void reset() {
        previousLimits = null;
        dataLimits = null;

        if (adapter.getSize() > 0)
            onDataAdded(adapter, 0, adapter.getSize());
    }

    @Override
    public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number) {
        boolean changed = false;
        if (dataIsContinuously) {
            float x = adapter.getX(index).floatValue();
            float y = adapter.getY(index).floatValue();
            if (includePoint(x, y))
                changed = true;
            x = adapter.getX(index + number - 1).floatValue();
            y = adapter.getY(index + number - 1).floatValue();
            if (includePoint(x, y))
                changed = true;
        } else {
            for (int i = 0; i < number; i++) {
                float x = adapter.getX(index + i).floatValue();
                float y = adapter.getY(index + i).floatValue();
                if (includePoint(x, y))
                    changed = true;
            }
        }
        if (changed)
            notifyLimitsChanged();
    }

    @Override
    public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number) {
        reset();
    }

    @Override
    public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number) {
        for (int i = 0; i < number; i++) {
            float x = adapter.getX(index + i).floatValue();
            float y = adapter.getY(index + i).floatValue();
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
