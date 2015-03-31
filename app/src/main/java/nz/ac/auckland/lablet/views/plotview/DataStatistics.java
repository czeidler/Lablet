/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.RectF;


public abstract class DataStatistics {
    private IListener listener;
    protected RectF dataLimits = null;
    protected RectF previousLimits = null;

    public interface IListener {
        void onLimitsChanged(DataStatistics dataStatistics);
    }

    public void setListener(IListener listener) {
        this.listener = listener;
    }

    protected void notifyLimitsChanged() {
        if (listener != null)
            listener.onLimitsChanged(this);
    }


    public RectF getDataLimits() {
        if (dataLimits == null)
            return null;
        return new RectF(dataLimits);
    }

    public RectF getPreviousDataLimits() {
        return previousLimits;
    }

    public abstract void release();

    public abstract AbstractPlotDataAdapter getAdapter();
}
