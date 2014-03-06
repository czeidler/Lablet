package nz.ac.aucklanduni.physics.tracker.views.graph;

import nz.ac.aucklanduni.physics.tracker.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.tracker.MarkerData;
import nz.ac.aucklanduni.physics.tracker.MarkersDataModel;

import java.util.ArrayList;
/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
import java.util.List;


public class MarkerGraphAdapter extends AbstractGraphAdapter implements MarkersDataModel.IMarkersDataModelListener {
    private List<IGraphAdapterListener> listeners;
    protected String title;
    protected MarkersDataModel data;
    protected ExperimentAnalysis experimentAnalysis;

    public MarkerGraphAdapter(ExperimentAnalysis experimentAnalysis, String title, MarkerGraphAxis xAxis,
                              MarkerGraphAxis yAxis) {
        listeners = new ArrayList<IGraphAdapterListener>();
        this.experimentAnalysis = experimentAnalysis;
        this.title = title;
        data = experimentAnalysis.getTagMarkers();
        data.addListener(this);

        xAxis.setMarkerGraphAdapter(this);
        yAxis.setMarkerGraphAdapter(this);
        setXAxis(xAxis);
        setYAxis(yAxis);
    }

    public void release() {
        data.removeListener(this);
        listeners.clear();
    }

    public MarkersDataModel getData() {
        return data;
    }

    public ExperimentAnalysis getExperimentAnalysis() {
        return experimentAnalysis;
    }

    @Override
    public void addListener(IGraphAdapterListener listener) {
        listeners.add(listener);
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
        for (IGraphAdapterListener listener : listeners)
            listener.onDataPointAdded(this, index);
    }

    public void notifyDataRemoved(int index) {
        for (IGraphAdapterListener listener : listeners)
            listener.onDataPointRemoved(this, index);
    }

    public void notifyDataChanged(int index, int number) {
        for (IGraphAdapterListener listener : listeners)
            listener.onDataPointChanged(this, index, number);
    }

    public void notifyAllDataChanged() {
        for (IGraphAdapterListener listener : listeners)
            listener.onAllDataPointsChanged(this);
    }

    private void notifyDataSelected(int index) {
        for (IGraphAdapterListener listener : listeners)
            listener.onDataPointSelected(this, index);
    }
}
