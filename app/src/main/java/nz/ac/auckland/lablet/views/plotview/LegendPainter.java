/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.*;
import android.view.MotionEvent;
import nz.ac.auckland.lablet.misc.DeviceIndependentPixel;

import java.util.ArrayList;
import java.util.List;


public class LegendPainter implements IPlotPainter {
    private Paint labelPaint = new Paint();
    private Paint backgroundPaint = new Paint();
    private Paint borderPaint = new Paint();
    final private PointF tempPoint = new PointF();
        // for drawing
    final private List<Entry> entryList = new ArrayList<>();
    private LayoutInfo layoutInfo;

    private PlotPainterContainerView containerView;

    class Entry {
        final private XYConcurrentPainter painter;
        final private String label;

        public Entry(XYConcurrentPainter painter, String label) {
            this.painter = painter;
            this.label = label;
        }

        public XYConcurrentPainter getPainter() {
            return painter;
        }

        public String getLabel() {
            return label;
        }
    }

    public LegendPainter() {
        labelPaint.setColor(Color.BLACK);
        backgroundPaint.setColor(Color.argb(200, 200, 200, 200));
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
    }

    public void addEntry(XYConcurrentPainter painter, String label) {
        entryList.add(new Entry(painter, label));
        layoutInfo = null;
    }

    @Override
    public void setContainer(PlotPainterContainerView view) {
        containerView = view;

        labelPaint.setTextSize(DeviceIndependentPixel.toPixel(PlotView.Defaults.LABEL_TEXT_SIZE_DP, view));
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {

    }

    static class LayoutInfo {
        final public RectF frame = new RectF();
        public float margin = 5f;
        public float labelHeight;
        public float labelSpacing = 3f;
        public float markerWidth;
        public float markerLineSpace = 5f;
        public float markerXSpacing = 5f;

        public float getLabelStartX() {
            return margin + 2 * markerLineSpace + markerWidth + markerXSpacing;
        }

        public float getMarkerLineLeft() {
            return margin;
        }

        public float getMarkerLineRight() {
            return getMarkerLineLeft() + markerLineSpace + markerWidth + markerLineSpace;
        }

        public float getMarkerX() {
            return margin + markerLineSpace + markerWidth / 2;
        }
    }

    private LayoutInfo layout() {
        LayoutInfo info = new LayoutInfo();

        info.labelHeight = labelPaint.descent() - labelPaint.ascent();

        float maxMarkerSize = Float.MIN_VALUE;
        float maxLength = Float.MIN_VALUE;
        for (Entry entry : entryList) {
            float length = labelPaint.measureText(entry.label);
            if (maxLength < length)
                maxLength = length;
            float markerSize = entry.painter.getDrawConfig().getMarkerSize();
            if (markerSize > maxMarkerSize)
                maxMarkerSize = markerSize;
        }

        info.markerWidth = maxMarkerSize;

        info.frame.left = 0;
        info.frame.top = 0;
        info.frame.right = 2 * info.margin + info.markerWidth + info.markerXSpacing + maxLength
                + 2 * info.markerLineSpace;
        info.frame.bottom = 2 * info.margin + entryList.size() * (info.labelHeight + info.labelSpacing);

        return info;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (layoutInfo == null)
            layoutInfo = layout();

        float width = containerView.getWidth();
        float margin = 10f;

        canvas.save();
        canvas.translate(width - layoutInfo.frame.width() - margin, margin);
        // draw background
        canvas.drawRect(layoutInfo.frame, backgroundPaint);
        canvas.drawRect(layoutInfo.frame, borderPaint);

        // draw markers
        float x = layoutInfo.getMarkerX();
        float lineLeft = layoutInfo.getMarkerLineLeft();
        float lineRight = layoutInfo.getMarkerLineRight();
        for (int i = 0; i < entryList.size(); i++) {
            Entry entry = entryList.get(i);
            float y = layoutInfo.margin + (layoutInfo.labelHeight + layoutInfo.labelSpacing) * (0.5f + i);
            canvas.drawLine(lineLeft, y, lineRight, y, entry.getPainter().getDrawConfig().getLinePaint());
            tempPoint.set(x, y);
            IPointRenderer pointRenderer = entry.painter.getPointRenderer();
            pointRenderer.drawPoint(canvas, tempPoint, entry.painter.getDrawConfig());
        }

        // draw labels
        x = layoutInfo.getLabelStartX();
        for (int i = 0; i < entryList.size(); i++) {
            Entry entry = entryList.get(i);
            float y = layoutInfo.margin + (layoutInfo.labelHeight + layoutInfo.labelSpacing) * i
                    + layoutInfo.labelHeight;
            canvas.drawText(entry.getLabel(), x, y, labelPaint);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public void invalidate() {

    }

    @Override
    public void setXScale(IScale xScale) {

    }

    @Override
    public void setYScale(IScale yScale) {

    }

    @Override
    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance) {

    }

    @Override
    public void release() {

    }
}
