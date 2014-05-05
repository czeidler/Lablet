/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.views.graph;

import nz.ac.aucklanduni.physics.tracker.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.tracker.MarkerData;
import nz.ac.aucklanduni.physics.tracker.MarkersDataModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class MarkerGraphAdapter extends AbstractGraphAdapter implements MarkersDataModel.IMarkersDataModelListener {
    private List<WeakReference<IGraphAdapterListener>> listeners;
    protected String title;
    protected MarkersDataModel data;
    protected ExperimentAnalysis experimentAnalysis;

    public MarkerGraphAdapter(ExperimentAnalysis experimentAnalysis, String title, MarkerGraphAxis xAxis,
                              MarkerGraphAxis yAxis) {
        listeners = new ArrayList<WeakReference<IGraphAdapterListener>>();
        this.title = title;
        setExperimentAnalysis(experimentAnalysis);

        xAxis.setMarkerGraphAdapter(this);
        yAxis.setMarkerGraphAdapter(this);
        setXAxis(xAxis);
        setYAxis(yAxis);
    }

    public void setExperimentAnalysis(ExperimentAnalysis experimentAnalysis) {
        if (data != null)
            data.removeListener(this);
        this.experimentAnalysis = experimentAnalysis;
        data = experimentAnalysis.getTagMarkers();
        data.addListener(this);

        notifyAllDataChanged();
    }

    @Override
    protected void finalize() {
        data.removeListener(this);
    }

    public static MarkerGraphAdapter createPositionAdapter(ExperimentAnalysis experimentAnalysis, String title) {
        return new MarkerGraphAdapter(experimentAnalysis, title, new XPositionMarkerGraphAxis(),
                new YPositionMarkerGraphAxis());
    }

    public static MarkerGraphAdapter createXSpeedAdapter(ExperimentAnalysis experimentAnalysis, String title) {
        return new MarkerGraphAdapter(experimentAnalysis, title, new SpeedTimeMarkerGraphAxis(),
                new XSpeedMarkerGraphAxis());
    }

    public static MarkerGraphAdapter createYSpeedAdapter(ExperimentAnalysis experimentAnalysis, String title) {
        return new MarkerGraphAdapter(experimentAnalysis, title, new SpeedTimeMarkerGraphAxis(),
                new YSpeedMarkerGraphAxis());
    }

    public MarkersDataModel getData() {
        return data;
    }

    public ExperimentAnalysis getExperimentAnalysis() {
        return experimentAnalysis;
    }

    @Override
    public void addListener(IGraphAdapterListener listener) {
        listeners.add(new WeakReference<IGraphAdapterListener>(listener));
    }

    @Override
    public boolean removeListener(IGraphAdapterListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void onDataAdded(MarkersDataModel model, int index) {
        notifyDataAdded(index);
    }

    @Override
    public void onDataRemoved(MarkersDataModel model, int index, MarkerData data) {
        notifyDataRemoved(index);
    }

    @Override
    public void onDataChanged(MarkersDataModel model, int index, int number) {
        notifyDataChanged(index, number);
    }

    @Override
    public void onAllDataChanged(MarkersDataModel model) {
        notifyAllDataChanged();
    }

    @Override
    public void onDataSelected(MarkersDataModel model, int index) {
        notifyDataSelected(index);
    }

    public void notifyDataAdded(int index) {
        for (ListIterator<WeakReference<IGraphAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IGraphAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onDataPointAdded(this, index);
            else
                it.remove();
        }
    }

    public void notifyDataRemoved(int index) {
        for (ListIterator<WeakReference<IGraphAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IGraphAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onDataPointRemoved(this, index);
            else
                it.remove();
        }
    }

    public void notifyDataChanged(int index, int number) {
        for (ListIterator<WeakReference<IGraphAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IGraphAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onDataPointChanged(this, index, number);
            else
                it.remove();
        }
    }

    public void notifyAllDataChanged() {
        for (ListIterator<WeakReference<IGraphAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IGraphAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onAllDataPointsChanged(this);
            else
                it.remove();
        }
    }

    private void notifyDataSelected(int index) {
        for (ListIterator<WeakReference<IGraphAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            IGraphAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onDataPointSelected(this, index);
            else
                it.remove();
        }
    }
}
