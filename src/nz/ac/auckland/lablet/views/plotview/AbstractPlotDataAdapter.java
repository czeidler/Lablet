/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


import nz.ac.auckland.lablet.misc.WeakListenable;

public abstract class AbstractPlotDataAdapter extends WeakListenable<AbstractPlotDataAdapter.IListener> {

    public interface IListener {
        public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number);
        public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number);
        public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number);
        public void onAllDataChanged(AbstractPlotDataAdapter plot);
    }

    abstract public int getSize();


    protected void notifyDataAdded(int index, int number) {
        for (IListener listener : getListeners())
            listener.onDataAdded(this, index, number);
    }

    protected void notifyDataRemoved(int index, int number) {
        for (IListener listener : getListeners())
            listener.onDataRemoved(this, index, number);
    }

    protected void notifyDataChanged(int index, int number) {
        for (IListener listener : getListeners())
            listener.onDataChanged(this, index, number);
    }

    protected void notifyAllDataChanged() {
        for (IListener listener : getListeners())
            listener.onAllDataChanged(this);
    }
}
