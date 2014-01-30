/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.util.AttributeSet;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.List;


interface IGraphAdapter {
    interface IGraphDataPoint {
        public double getX();
        public double getY();
    }

    interface IGraphAdapterListener {
        public void onDataPointAdded(IGraphAdapter graph, int index);
        public void onDataPointRemoved(IGraphAdapter graph, int index);
        public void onDataPointChanged(IGraphAdapter graph, int index, int number);
        public void onAllDataPointsChanged(IGraphAdapter graph);
        public void onDataPointSelected(IGraphAdapter graph, int index);
    }

    public void addListener(IGraphAdapterListener listener);
    public int getDataPointCount();
    public IGraphDataPoint getDataPoint(int index);
    public void release();
}


public class GraphView2D extends LineGraphView implements IGraphAdapter.IGraphAdapterListener {
    private IGraphAdapter adapter;
    private GraphViewSeries graphViewSeries = null;

    public GraphView2D(Context context, AttributeSet attrs) {
        super(context, attrs);
        GraphViewDataInterface[] values = new GraphViewData[0];
        graphViewSeries = new GraphViewSeries(values);
        addSeries(graphViewSeries);
        getGraphViewStyle().setTextSize(15);
    }

    public void setAdapter(IGraphAdapter adapter) {
        if (this.adapter != null)
            this.adapter.release();
        this.adapter = adapter;

        if (this.adapter == null) {
            graphViewSeries.resetData(new GraphViewData[0]);
            return;
        }

        this.adapter.addListener(this);

        refillGraph();
    }

    @Override
    public void onDataPointAdded(IGraphAdapter graph, int index) {
        if (index == adapter.getDataPointCount()) {
            IGraphAdapter.IGraphDataPoint dataPoint = adapter.getDataPoint(index);
            graphViewSeries.appendData(new GraphViewData(dataPoint.getX(), dataPoint.getY()), true, 1);
        } else
            refillGraph();
    }

    @Override
    public void onDataPointRemoved(IGraphAdapter graph, int index) {
        refillGraph();
    }

    @Override
    public void onDataPointChanged(IGraphAdapter graph, int index, int number) {
        refillGraph();
    }

    @Override
    public void onAllDataPointsChanged(IGraphAdapter graph) {
        refillGraph();
    }

    @Override
    public void onDataPointSelected(IGraphAdapter graph, int index) {

    }

    private void refillGraph() {
        GraphViewDataInterface[] values = new GraphViewData[adapter.getDataPointCount()];
        for (int i = 0; i < values.length; i++) {
            IGraphAdapter.IGraphDataPoint dataPoint = adapter.getDataPoint(i);
            values[i] = new GraphViewData(dataPoint.getX(), dataPoint.getY());
        }
        graphViewSeries.resetData(values);
    }
}


class MarkerGraphAdapter implements IGraphAdapter, MarkersDataModel.IMarkersDataModelListener {
    private List<IGraphAdapterListener> listeners;
    private MarkersDataModel data;

    public MarkerGraphAdapter(MarkersDataModel markersDataModel) {
        listeners = new ArrayList<IGraphAdapterListener>();
        data = markersDataModel;
        data.addListener(this);
    }

    public void release() {
        data.removeListener(this);
        listeners.clear();
    }

    @Override
    public void addListener(IGraphAdapterListener listener) {
        listeners.add(listener);
    }

    @Override
    public int getDataPointCount() {
        return data.getMarkerCount();
    }

    @Override
    public IGraphDataPoint getDataPoint(int index) {
        return new DataPointAdapter(index);
    }

    class DataPointAdapter implements IGraphDataPoint {
        int index;
        public DataPointAdapter(int index) {
            this.index = index;
        }

        @Override
        public double getX() {
            return data.getCalibratedMarkerPositionAt(index).x;
        }

        @Override
        public double getY() {
            return data.getCalibratedMarkerPositionAt(index).y;
        }
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