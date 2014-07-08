/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import java.lang.ref.WeakReference;


public abstract class AbstractPlotDataAdapter {
    WeakReference<IListener> listenerWeakReference = null;

    public interface IListener {
        public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number);
        public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number);
        public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number);
        public void onAllDataChanged(AbstractPlotDataAdapter plot);
    }

    abstract public int getSize();

    public void setListener(IListener listener) {
        if (listener == null) {
            listenerWeakReference = null;
            return;
        }
        listenerWeakReference = new WeakReference<IListener>(listener);
    }

    protected IListener getListener() {
        if (listenerWeakReference == null)
            return null;
        return listenerWeakReference.get();
    }

    protected void notifyDataAdded(int index, int number) {
        IListener listener = getListener();
        if (listener == null)
            return;
        listener.onDataAdded(this, index, number);
    }

    protected void notifyDataRemoved(int index, int number) {
        IListener listener = getListener();
        if (listener == null)
            return;
        listener.onDataRemoved(this, index, number);
    }

    protected void notifyDataChanged(int index, int number) {
        IListener listener = getListener();
        if (listener == null)
            return;
        listener.onDataChanged(this, index, number);
    }

    protected void notifyAllDataChanged() {
        IListener listener = getListener();
        if (listener == null)
            return;
        listener.onAllDataChanged(this);
    }
}
