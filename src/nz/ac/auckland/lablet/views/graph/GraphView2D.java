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
import nz.ac.auckland.lablet.views.ZoomDialog;
import nz.ac.auckland.lablet.views.plotview.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base class for graph adapters.
 */
abstract class AbstractGraphAdapter extends AbstractXYDataAdapter {
    public interface IGraphDataAxis {
        public int size();
        public Number getValue(int index);
        public String getLabel();
        public Number getMinRange();
    }

    protected IGraphDataAxis xAxis;
    protected IGraphDataAxis yAxis;

    @Override
    public int getSize() {
        return Math.min(getXAxis().size(), getYAxis().size());
    }

    abstract public void setTitle(String title);
    abstract public String getTitle();

    public IGraphDataAxis getXAxis() {
        return xAxis;
    }

    public IGraphDataAxis getYAxis() {
        return yAxis;
    }

    public void setXAxis(IGraphDataAxis axis) {
        xAxis = axis;
    }

    public void setYAxis(IGraphDataAxis axis) {
        yAxis = axis;
    }

    @Override
    public float getX(int i) {
        return getXAxis().getValue(i).floatValue();
    }

    @Override
    public float getY(int i) {
        return getYAxis().getValue(i).floatValue();
    }

    @Override
    public CloneablePlotDataAdapter clone(Region1D region) {
        XYDataAdapter xyDataAdapter = new XYDataAdapter(region.getMin());
        for (int i = region.getMin(); i < region.getMax() + 1; i++) {
            xyDataAdapter.addData(getX(i), getY(i));
        }
        return xyDataAdapter;
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

                // if textPaint and pointLabeler are available, draw point's text title:
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
 * {@link nz.ac.auckland.lablet.views.graph.AbstractGraphAdapter}.
 */
public class GraphView2D extends PlotView {
    private AbstractGraphAdapter adapter;

    private XYPainter painter;

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
        super(context);

        getTitleView().setTitle(title);

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

        getTitleView().getLabelPaint().setTextSize(TITLE_TEXT_SIZE);
        getXAxisView().getAxisPaint().setTextSize(LABEL_TEXT_SIZE);
        getXAxisView().getTitlePaint().setTextSize(LABEL_TEXT_SIZE);
        getYAxisView().getAxisPaint().setTextSize(LABEL_TEXT_SIZE);
        getYAxisView().getTitlePaint().setTextSize(LABEL_TEXT_SIZE);
    }

    public void setZoomOnClick(boolean zoomable) {
        if (zoomable) {

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //zoom();

                    GraphView2D zoomGraphView = new GraphView2D(getContext(), adapter.getTitle(), false);
                    zoomGraphView.setAdapter(adapter);

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

    private int toPixel(float densityIndependentPixel) {
        final float scale = getResources().getDisplayMetrics().density;
        return Math.round(densityIndependentPixel * scale);
    }

    public void setAdapter(AbstractGraphAdapter adapter) {
        if (this.adapter != null)
            removePlotPainter(painter);
        painter = null;
        this.adapter = adapter;
        if (adapter == null)
            return;

        getTitleView().setTitle(adapter.getTitle());
        getXAxisView().setTitle(this.adapter.getXAxis().getLabel());
        getYAxisView().setTitle(this.adapter.getYAxis().getLabel());

        setMinXRange(this.adapter.getXAxis().getMinRange().floatValue());
        setMinYRange(this.adapter.getYAxis().getMinRange().floatValue());

        painter = new XYPainter();
        painter.setDataAdapter(adapter);
        addPlotPainter(painter);

        setAutoRange(AUTO_RANGE_ZOOM, AUTO_RANGE_ZOOM);
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
}


