/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


class WeakListenable<Listener> {
    private List<WeakReference<Listener>> listeners = new ArrayList<>();

    public void addListener(Listener listener) {
        if (hasListener(listener))
            return;
        listeners.add(new WeakReference<Listener>(listener));
    }

    public boolean hasListener(Listener listener) {
        Iterator<WeakReference<Listener>> it = listeners.iterator();
        while (it.hasNext()) {
            WeakReference<Listener> listenerWeak = it.next();
            Listener listenerStrong = listenerWeak.get();
            if (listenerStrong == null) {
                it.remove();
                continue;
            }
            if (listenerWeak.get() == listener)
                return true;
        }
        return false;
    }

    public void removeListener(Listener listener) {
        Iterator<WeakReference<Listener>> it = listeners.iterator();
        while (it.hasNext()) {
            WeakReference<Listener> listenerWeak = it.next();
            Listener listenerStrong = listenerWeak.get();
            if (listenerStrong == null) {
                it.remove();
                continue;
            }
            if (listenerStrong == listener) {
                it.remove();
                return;
            }
        }
    }

    protected List<Listener> getListeners() {
        List<Listener> outList = new ArrayList<>();

        Iterator<WeakReference<Listener>> it = listeners.iterator();
        while (it.hasNext()) {
            WeakReference<Listener> listenerWeak = it.next();
            Listener listenerStrong = listenerWeak.get();
            if (listenerStrong == null) {
                it.remove();
                continue;
            }
            outList.add(listenerStrong);
        }

        return outList;
    }

}

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
