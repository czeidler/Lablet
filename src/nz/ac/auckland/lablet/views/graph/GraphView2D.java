/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import com.androidplot.exception.PlotRenderException;
import com.androidplot.ui.*;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.*;
import nz.ac.auckland.lablet.ExperimentAnalyserActivity;
import nz.ac.auckland.lablet.views.ZoomDialog;

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

    interface IGraphAxis {
        public int size();
        public Number getValue(int index);
        public String getLabel();
        public Number getMinRange();
    }

    public void addListener(IGraphAdapterListener listener);
    public boolean removeListener(IGraphAdapterListener listener);
    public int size();
    public void setTitle(String title);
    public String getTitle();

    public IGraphAxis getXAxis();
    public IGraphAxis getYAxis();
}


/**
 * Abstract base class for graph adapters.
 */
abstract class AbstractGraphAdapter implements IGraphAdapter {
    protected IGraphAxis xAxis;
    protected IGraphAxis yAxis;

    @Override
    public int size() {
        return Math.min(getXAxis().size(), getYAxis().size());
    }

    @Override
    public IGraphAxis getXAxis() {
        return xAxis;
    }

    @Override
    public IGraphAxis getYAxis() {
        return yAxis;
    }

    public void setXAxis(IGraphAxis axis) {
        xAxis = axis;
    }

    public void setYAxis(IGraphAxis axis) {
        yAxis = axis;
    }
}


/**
 * Basically a copy of XYSeriesRenderer but can use different styles to draw points.
 */
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

    public class CrossRenderer extends PointRenderer {
        private final float size = 3f;

        @Override
        public void drawPoint(Canvas canvas, PointF position, LineAndPointFormatter formatter) {

            canvas.drawLine(position.x - size, position.y - size, position.x + size, position.y + size,
                    formatter.getVertexPaint());
            canvas.drawLine(position.x - size, position.y + size, position.x + size, position.y - size,
                    formatter.getVertexPaint());
        }
    }

    private PointRenderer pointRenderer = null;

    public ImprovedPointAndLineRenderer(XYPlot plot) {
        super(plot);

        pointRenderer = new CrossRenderer();
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


/**
 * Custom formatter with wider stroke size.
 */
class ImprovedLineAndPointFormatter extends LineAndPointFormatter {
    public ImprovedLineAndPointFormatter(int lineColor, int markerColor, int fillColor) {
        super(lineColor, markerColor, fillColor, new PointLabelFormatter());

        Paint paint = getVertexPaint();
        paint.setStrokeWidth(2.f);
        setVertexPaint(paint);
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


/**
 * Customized XYPlot from the androidplot library that works with an
 * {@link nz.ac.auckland.lablet.views.graph.IGraphAdapter}.
 */
public class GraphView2D extends XYPlot implements IGraphAdapter.IGraphAdapterListener {
    private IGraphAdapter adapter;

    // max layout sizes in dp
    private int maxWidth = -1;
    private int maxHeight = -1;

    // device independent size
    private float TITLE_TEXT_SIZE_DP = 12;
    private float LABEL_TEXT_SIZE_DP = 10;

    private float TITLE_TEXT_SIZE;
    private float LABEL_TEXT_SIZE;

    // in dp
    public int getMaxWidth() {
        return maxWidth;
    }
    public void setMaxWidth(float maxWidth) {
        final float scale = getResources().getDisplayMetrics().density;
        this.maxWidth = (int)(scale * maxWidth);
    }
    public int getMaxHeight() {
        return maxHeight;
    }
    public void setMaxHeight(int maxHeight) {
        final float scale = getResources().getDisplayMetrics().density;
        this.maxHeight = (int)(scale * maxHeight);
    }

    public GraphView2D(Context context, String title, boolean zoomOnClick) {
        super(context, title);

        init();

        if (zoomOnClick)
            setZoomOnClick(true);
    }

    public GraphView2D(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();

        setZoomOnClick(true);
    }

    private void init() {
        TITLE_TEXT_SIZE = toPixel(TITLE_TEXT_SIZE_DP);
        LABEL_TEXT_SIZE = toPixel(LABEL_TEXT_SIZE_DP);

        getTitleWidget().getLabelPaint().setTextSize(TITLE_TEXT_SIZE);
        getDomainLabelWidget().getLabelPaint().setTextSize(LABEL_TEXT_SIZE);
        getRangeLabelWidget().getLabelPaint().setTextSize(LABEL_TEXT_SIZE);
        getGraphWidget().getDomainLabelPaint().setTextSize(LABEL_TEXT_SIZE);
        getGraphWidget().getRangeLabelPaint().setTextSize(LABEL_TEXT_SIZE);
        getGraphWidget().getDomainOriginLabelPaint().setTextSize(LABEL_TEXT_SIZE);
        getGraphWidget().getRangeOriginLabelPaint().setTextSize(LABEL_TEXT_SIZE);

        doGraphLayout();
    }

    public void setZoomOnClick(boolean zoomable) {
        if (zoomable) {

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //zoom();

                    GraphView2D zoomGraphView = new GraphView2D(getContext(), getTitle(), false);
                    zoomGraphView.setAdapter(getAdapter());

                    Rect startBounds = new Rect();
                    Rect finalBounds = new Rect();

                    getGlobalVisibleRect(startBounds);
                    ViewGroup parent = ((ViewGroup)getRootView());
                    parent.getDrawingRect(finalBounds);

                    ZoomDialog dialog = new ZoomDialog(getContext(), zoomGraphView, startBounds, finalBounds);
                    dialog.show();
                }
            });

        } else {
            setOnClickListener(null);
        }
    }

    private void doGraphLayout() {
        float titleBottom = getTitleWidget().getPositionMetrics().getYPositionMetric().getValue()
                + getTitleWidget().getHeightMetric().getValue();
        float yAxisLabelRight = getRangeLabelWidget().getPositionMetrics().getXPositionMetric().getValue()
                + getRangeLabelWidget().getWidthMetric().getValue();
        float xAxisLabelHeight = getDomainLabelWidget().getPositionMetrics().getYPositionMetric().getValue()
                + getDomainLabelWidget().getHeightMetric().getValue();

        getGraphWidget().position(yAxisLabelRight, XLayoutStyle.ABSOLUTE_FROM_LEFT, titleBottom,
                YLayoutStyle.ABSOLUTE_FROM_TOP);
        getGraphWidget().setWidth(yAxisLabelRight, SizeLayoutType.FILL);
        getGraphWidget().setHeight(titleBottom + xAxisLabelHeight, SizeLayoutType.FILL);

        float xAxisTextHeight = getGraphWidget().getDomainOriginLabelPaint().descent()
                - getGraphWidget().getDomainOriginLabelPaint().ascent();
        // a bit hacky, why "-10"?:
        getGraphWidget().setPaddingBottom(xAxisTextHeight - 10);
    }

    private int toPixel(float densityIndependentPixel) {
        final float scale = getResources().getDisplayMetrics().density;
        return Math.round(densityIndependentPixel * scale);
    }

    @Override
    protected void finalize() {
        if (this.adapter != null)
            this.adapter.removeListener(this);
        this.adapter = null;
    }

    public void setAdapter(IGraphAdapter adapter) {
        if (this.adapter != null)
            this.adapter.removeListener(this);
        this.adapter = adapter;

        if (this.adapter == null) {
            clear();
            return;
        }

        this.adapter.addListener(this);

        // init graph
        clear();

        setTitle(adapter.getTitle());
        setDomainLabel(this.adapter.getXAxis().getLabel());
        setRangeLabel(this.adapter.getYAxis().getLabel());
        getLegendWidget().setVisible(false);
        setTicksPerDomainLabel(2);

        LineAndPointFormatter seriesFormatter = new ImprovedLineAndPointFormatter(Color.argb(255, 216, 228, 159),
                ExperimentAnalyserActivity.MARKER_COLOR, Color.argb(0, 0, 0, 0));
        seriesFormatter.setPointLabeler(null);

        addSeries(new XYSeriesAdapter(adapter), seriesFormatter);

        refillGraph();
    }

    public IGraphAdapter getAdapter() {
        return adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Adjust width as necessary
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if(maxWidth > 0 && maxWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, measureMode);
        }
        // Adjust height as necessary
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if(maxHeight > 0 && maxHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
            return adapter.getXAxis().getValue(i);
        }

        public Number getY(int i) {
            return adapter.getYAxis().getValue(i);
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

        float minXRange = adapter.getXAxis().getMinRange().floatValue();
        // ensure min x range
        if (minXRange > 0.f) {
            float average = 0;
            float max = -Float.MAX_VALUE;
            float min = Float.MAX_VALUE;
            for (int i = 0; i < adapter.size(); i++) {
                float value = adapter.getXAxis().getValue(i).floatValue();
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

        float minYRange = adapter.getYAxis().getMinRange().floatValue();
        // ensure min y range
        if (minYRange > 0.f) {
            float average = 0;
            float max = -Float.MAX_VALUE;
            float min = Float.MAX_VALUE;
            for (int i = 0; i < adapter.size(); i++) {
                float value = adapter.getYAxis().getValue(i).floatValue();
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


