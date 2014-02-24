/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import com.androidplot.xy.*;

import java.util.ArrayList;
import java.util.List;


interface IGraphAdapter {
    interface IGraphAdapterListener {
        public void onDataPointAdded(IGraphAdapter graph, int index);
        public void onDataPointRemoved(IGraphAdapter graph, int index);
        public void onDataPointChanged(IGraphAdapter graph, int index, int number);
        public void onAllDataPointsChanged(IGraphAdapter graph);
        public void onDataPointSelected(IGraphAdapter graph, int index);
    }

    public void addListener(IGraphAdapterListener listener);
    public int size();
    public Number getX(int index);
    public Number getY(int index);
    public String getXLabel();
    public String getYLabel();
    public String getTitle();
    public Number getMinXRange();
    public Number getMinYRange();
    public void release();
}


public class GraphView2D extends XYPlot implements IGraphAdapter.IGraphAdapterListener {
    private IGraphAdapter adapter;

    public GraphView2D(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void setAdapter(IGraphAdapter adapter) {
        if (this.adapter != null)
            this.adapter.release();
        this.adapter = adapter;

        if (this.adapter == null) {
            clear();
            return;
        }

        this.adapter.addListener(this);

        // init graph
        clear();

        setTitle(adapter.getTitle());
        setDomainLabel(this.adapter.getXLabel());
        setRangeLabel(this.adapter.getYLabel());
        getLegendWidget().setVisible(false);
        setTicksPerDomainLabel(2);

        LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.GREEN, Color.BLUE, Color.argb(0, 0, 0, 0),
                new PointLabelFormatter());
        seriesFormat.setPointLabeler(null);

        addSeries(new XYSeriesAdapter(adapter), seriesFormat);

        refillGraph();
    }

    @Override
    public void onDataPointAdded(IGraphAdapter graph, int index) {
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

    private class XYSeriesAdapter implements XYSeries {
        private IGraphAdapter adapter = null;

        public XYSeriesAdapter(IGraphAdapter adapter) {
            this.adapter = adapter;
        }

        public int size() {
            return adapter.size();
        }

        public Number getX(int i) {
            return adapter.getX(i);
        }

        public Number getY(int i) {
            return adapter.getY(i);
        }

        @Override
        public String getTitle() {
            return adapter.getTitle();
        }
    }

    private void refillGraph() {
        setDomainLeftMax(null);
        setDomainRightMin(null);
        setRangeBottomMax(null);
        setRangeTopMin(null);

        if (adapter.size() == 0) {
            redraw();
            return;
        }

        float minXRange = adapter.getMinXRange().floatValue();
        // ensure min x range
        if (minXRange > 0.f) {
            float average = 0;
            float max = -Float.MAX_VALUE;
            float min = Float.MAX_VALUE;
            for (int i = 0; i < adapter.size(); i++) {
                float value = adapter.getX(i).floatValue();
                average += value;
                if (value > max)
                    max = value;
                if (value < min)
                    min = value;
            }
            average /= adapter.size();
            float currentRange = max - min;
            if (currentRange < minXRange) {
                setDomainLeftMax(average - minXRange / 2);
                setDomainRightMin(average + minXRange / 2);
            }
        }

        float minYRange = adapter.getMinYRange().floatValue();
        // ensure min y range
        if (minYRange > 0.f) {
            float average = 0;
            float max = -Float.MAX_VALUE;
            float min = Float.MAX_VALUE;
            for (int i = 0; i < adapter.size(); i++) {
                float value = adapter.getY(i).floatValue();
                average += value;
                if (value > max)
                    max = value;
                if (value < min)
                    min = value;
            }
            average /= adapter.size();
            float currentRange = max - min;
            if (currentRange < minYRange) {
                setRangeBottomMax(average - minYRange / 2);
                setRangeTopMin(average + minYRange / 2);
            }
        }

        redraw();
    }
}


class MarkerGraphAdapter implements IGraphAdapter, MarkersDataModel.IMarkersDataModelListener {
    private List<IGraphAdapterListener> listeners;
    protected MarkersDataModel data;
    protected ExperimentAnalysis experimentAnalysis;

    public MarkerGraphAdapter(ExperimentAnalysis experimentAnalysis) {
        listeners = new ArrayList<IGraphAdapterListener>();
        this.experimentAnalysis = experimentAnalysis;
        data = experimentAnalysis.getTagMarkers();
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
    public int size() {
        return data.getMarkerCount();
    }

    @Override
    public Number getX(int index) {
        return data.getCalibratedMarkerPositionAt(index).x;
    }

    @Override
    public Number getY(int index) {
        return data.getCalibratedMarkerPositionAt(index).y;
    }

    @Override
    public String getXLabel() {
        return "x [" + experimentAnalysis.getXUnit() + "]";
    }

    @Override
    public String getYLabel() {
        return "y [" + experimentAnalysis.getYUnit() + "]";
    }

    @Override
    public String getTitle() {
        return "Position Plot";
    }

    @Override
    public Number getMinXRange() {
        Calibration calibration = experimentAnalysis.getCalibration();
        PointF point = new PointF(calibration.getOrigin().x + experimentAnalysis.getExperiment().getMaxRawX(), 0);
        point = calibration.fromRaw(point);
        return point.x * 0.2f;
    }

    @Override
    public Number getMinYRange() {
        Calibration calibration = experimentAnalysis.getCalibration();
        PointF point = new PointF(0, calibration.getOrigin().y + experimentAnalysis.getExperiment().getMaxRawY());
        point = calibration.fromRaw(point);
        return point.y * 0.2f;
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


class YVelocityMarkerGraphAdapter extends MarkerGraphAdapter {
    public YVelocityMarkerGraphAdapter(ExperimentAnalysis experimentAnalysis) {
        super(experimentAnalysis);
    }

    @Override
    public int size() {
        return data.getMarkerCount() - 1;
    }

    @Override
    public Number getX(int index) {
        return experimentAnalysis.getExperiment().getRunValueAt(index);
    }

    @Override
    public Number getY(int index) {
        Experiment experiment = experimentAnalysis.getExperiment();
        float deltaX = data.getCalibratedMarkerPositionAt(index + 1).y - data.getCalibratedMarkerPositionAt(index).y;
        float deltaT = experiment.getRunValueAt(index + 1) - experiment.getRunValueAt(index);
        if (experimentAnalysis.getExperiment().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return deltaX / deltaT;
    }

    @Override
    public Number getMinXRange() {
        return -1;
    }

    @Override
    public Number getMinYRange() {
        return 3;
    }

    @Override
    public String getXLabel() {
        return "time [" + experimentAnalysis.getExperiment().getRunValueUnit() + "]";
    }

    @Override
    public String getYLabel() {
        return "speed [" + experimentAnalysis.getXUnit() + "/" + experimentAnalysis.getExperiment().getRunValueBaseUnit() + "]";
    }

    @Override
    public String getTitle() {
        return "y-Speed Plot";
    }
}


class XVelocityMarkerGraphAdapter extends MarkerGraphAdapter {
    public XVelocityMarkerGraphAdapter(ExperimentAnalysis experimentAnalysis) {
        super(experimentAnalysis);
    }

    @Override
    public int size() {
        return data.getMarkerCount() - 1;
    }

    @Override
    public Number getX(int index) {
        return experimentAnalysis.getExperiment().getRunValueAt(index);
    }

    @Override
    public Number getY(int index) {
        Experiment experiment = experimentAnalysis.getExperiment();
        float deltaX = data.getCalibratedMarkerPositionAt(index + 1).x - data.getCalibratedMarkerPositionAt(index).x;
        float deltaT = experiment.getRunValueAt(index + 1) - experiment.getRunValueAt(index);
        if (experimentAnalysis.getExperiment().getRunValueUnitPrefix().equals("m"))
            deltaT /= 1000;
        return deltaX / deltaT;
    }

    @Override
    public Number getMinXRange() {
        return -1;
    }

    @Override
    public Number getMinYRange() {
        return 3;
    }

    @Override
    public String getXLabel() {
        return "time [" + experimentAnalysis.getExperiment().getRunValueUnit() + "]";
    }

    @Override
    public String getYLabel() {
        return "speed [" + experimentAnalysis.getXUnit() + "/" + experimentAnalysis.getExperiment().getRunValueBaseUnit() + "]";
    }

    @Override
    public String getTitle() {
        return "x-Speed Plot";
    }
}