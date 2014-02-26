/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Pair;
import com.androidplot.exception.PlotRenderException;
import com.androidplot.ui.SeriesRenderer;
import com.androidplot.util.ValPixConverter;
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


// basically a copy from XYSeriesRenderer
class ImprovedPointAndLineRenderer<FormatterType extends LineAndPointFormatter> extends LineAndPointRenderer {
    abstract class PointRenderer {
        abstract public void drawPoint(Canvas canvas, PointF position, LineAndPointFormatter formatter);
    }

    public class CircleRenderer extends PointRenderer {
        private float circleWidth = 3.5f;

        @Override
        public void drawPoint(Canvas canvas, PointF position, LineAndPointFormatter formatter) {
            canvas.drawCircle(position.x, position.y, circleWidth, formatter.getVertexPaint());
        }
    }

    private PointRenderer pointRenderer = null;

    public ImprovedPointAndLineRenderer(XYPlot plot) {
        super(plot);

        pointRenderer = new CircleRenderer();
    }

    @Override
    public void doDrawLegendIcon(Canvas canvas, RectF rect, LineAndPointFormatter formatter) {
        // horizontal icon:
        float centerY = rect.centerY();
        float centerX = rect.centerX();

        if(formatter.getFillPaint() != null) {
            canvas.drawRect(rect, formatter.getFillPaint());
        }
        if(formatter.getLinePaint() != null) {
            canvas.drawLine(rect.left, rect.bottom, rect.right, rect.top, formatter.getLinePaint());
        }

        if(formatter.getVertexPaint() != null) {
            pointRenderer.drawPoint(canvas, new PointF(centerX, centerY), formatter);
        }
    }

    /**
     * This method exists for StepRenderer to override without having to duplicate any
     * additional code.
     */
    protected void appendToPath(Path path, PointF thisPoint, PointF lastPoint) {

        path.lineTo(thisPoint.x, thisPoint.y);
    }

    @Override
    public void onRender(Canvas canvas, RectF plotArea) throws PlotRenderException {
        List<XYSeries> seriesList = getPlot().getSeriesListForRenderer(this.getClass());
        if (seriesList != null) {
            for (XYSeries series : seriesList) {
                //synchronized(series) {
                drawSeries(canvas, plotArea, series, (FormatterType)getFormatter(series));
                //}
            }
        }
    }

    protected void drawSeries(Canvas canvas, RectF plotArea, XYSeries series, FormatterType formatter) {
        PointF thisPoint;
        PointF lastPoint = null;
        PointF firstPoint = null;
        Paint  linePaint = formatter.getLinePaint();

        XYPlot plot = (XYPlot)getPlot();
        //PointF lastDrawn = null;
        Path path = null;
        ArrayList<Pair<PointF, Integer>> points = new ArrayList<Pair<PointF, Integer>>(series.size());
        for (int i = 0; i < series.size(); i++) {
            Number y = series.getY(i);
            Number x = series.getX(i);

            if (y != null && x != null) {
                thisPoint = ValPixConverter.valToPix(
                        x,
                        y,
                        plotArea,
                        plot.getCalculatedMinX(),
                        plot.getCalculatedMaxX(),
                        plot.getCalculatedMinY(),
                        plot.getCalculatedMaxY());
                points.add(new Pair<PointF, Integer>(thisPoint, i));
                //appendToPath(path, thisPoint, lastPoint);
            } else {
                thisPoint = null;
            }

            if(linePaint != null && thisPoint != null) {

                // record the first point of the new Path
                if(firstPoint == null) {
                    path = new Path();
                    firstPoint = thisPoint;
                    // create our first point at the bottom/x position so filling
                    // will look good
                    path.moveTo(firstPoint.x, firstPoint.y);
                }

                if(lastPoint != null) {
                    appendToPath(path, thisPoint, lastPoint);
                }

                lastPoint = thisPoint;
            } else {
                if(lastPoint != null) {
                    renderPath(canvas, plotArea, path, firstPoint, lastPoint, formatter);
                }
                firstPoint = null;
                lastPoint = null;
            }
        }
        if(linePaint != null && firstPoint != null) {
            renderPath(canvas, plotArea, path, firstPoint, lastPoint, formatter);
        }

        // TODO: benchmark this against drawPoints(float[]);
        Paint vertexPaint = formatter.getVertexPaint();
        PointLabelFormatter plf = formatter.getPointLabelFormatter();
        if (vertexPaint != null || plf != null) {
            for (Pair<PointF, Integer> p : points) {
                PointLabeler pointLabeler = formatter.getPointLabeler();

                // if vertexPaint is available, draw vertex:
                if(vertexPaint != null) {
                    pointRenderer.drawPoint(canvas, new PointF(p.first.x, p.first.y), formatter);
                }

                // if textPaint and pointLabeler are available, draw point's text label:
                if(plf != null && pointLabeler != null) {
                    canvas.drawText(pointLabeler.getLabel(series, p.second), p.first.x + plf.hOffset, p.first.y + plf.vOffset, plf.getTextPaint());
                }
            }
        }
    }

    protected void renderPath(Canvas canvas, RectF plotArea, Path path, PointF firstPoint, PointF lastPoint, LineAndPointFormatter formatter) {
        Path outlinePath = new Path(path);
        XYPlot plot = (XYPlot)getPlot();

        // determine how to close the path for filling purposes:
        // We always need to calculate this path because it is also used for
        // masking off for region highlighting.
        switch (formatter.getFillDirection()) {
            case BOTTOM:
                path.lineTo(lastPoint.x, plotArea.bottom);
                path.lineTo(firstPoint.x, plotArea.bottom);
                path.close();
                break;
            case TOP:
                path.lineTo(lastPoint.x, plotArea.top);
                path.lineTo(firstPoint.x, plotArea.top);
                path.close();
                break;
            case RANGE_ORIGIN:
                float originPix = ValPixConverter.valToPix(
                        plot.getRangeOrigin().doubleValue(),
                        plot.getCalculatedMinY().doubleValue(),
                        plot.getCalculatedMaxY().doubleValue(),
                        plotArea.height(),
                        true);
                originPix += plotArea.top;

                path.lineTo(lastPoint.x, originPix);
                path.lineTo(firstPoint.x, originPix);
                path.close();
                break;
            default:
                throw new UnsupportedOperationException("Fill direction not yet implemented: " + formatter.getFillDirection());
        }

        if (formatter.getFillPaint() != null) {
            canvas.drawPath(path, formatter.getFillPaint());
        }


        //}

        // draw any visible regions on top of the base region:
        double minX = plot.getCalculatedMinX().doubleValue();
        double maxX = plot.getCalculatedMaxX().doubleValue();
        double minY = plot.getCalculatedMinY().doubleValue();
        double maxY = plot.getCalculatedMaxY().doubleValue();

        // draw each region:
        for (RectRegion r : RectRegion.regionsWithin(formatter.getRegions().elements(), minX, maxX, minY, maxY)) {
            XYRegionFormatter f = formatter.getRegionFormatter(r);
            RectF regionRect = r.getRectF(plotArea, minX, maxX, minY, maxY);
            if (regionRect != null) {
                try {
                    canvas.save(Canvas.ALL_SAVE_FLAG);
                    canvas.clipPath(path);
                    canvas.drawRect(regionRect, f.getPaint());
                } finally {
                    canvas.restore();
                }
            }
        }

        // finally we draw the outline path on top of everything else:
        if(formatter.getLinePaint() != null) {
            canvas.drawPath(outlinePath, formatter.getLinePaint());
        }

        path.rewind();
    }
}

class ImprovedLineAndPointFormatter extends LineAndPointFormatter {
    public ImprovedLineAndPointFormatter(int lineColor, int markerColor, int fillColor) {
        super(lineColor, markerColor, fillColor, new PointLabelFormatter());
    }

    @Override
    public Class<? extends SeriesRenderer> getRendererClass() {
        return ImprovedPointAndLineRenderer.class;
    }

    @Override
    public SeriesRenderer getRendererInstance(XYPlot plot) {
        return new ImprovedPointAndLineRenderer(plot);
    }
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

        LineAndPointFormatter seriesFormat = new ImprovedLineAndPointFormatter(Color.argb(255, 216, 228, 159),
                ExperimentAnalyserActivity.MARKER_COLOR, Color.argb(0, 0, 0, 0));
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