/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.*;
import nz.ac.auckland.lablet.views.plotview.axes.*;


class PlotGestureDetector {
    private ScaleGestureDetector scaleGestureDetector;
    private DragDetector dragDetector = new DragDetector();

    final private PlotView plotView;
    final private RangeDrawingView rangeView;

    private boolean rangeChanging = false;

    class DragDetector {
        private boolean isDragging = false;
        public PointF point = new PointF(-1, -1);

        public boolean onTouchEvent(MotionEvent event) {
            if (event.getPointerCount() > 1) {
                setDragging(false);
                return false;
            }
            int action = event.getActionMasked();
            boolean handled = false;
            if (action == MotionEvent.ACTION_DOWN) {
                setDragging(true);
                point.set(event.getX(), event.getY());
            } else if (action == MotionEvent.ACTION_UP) {
                setDragging(false);
            } else if (action == MotionEvent.ACTION_MOVE && isDragging) {
                float xDragged = event.getX() - point.x;
                float yDragged = event.getY() - point.y;
                point.set(event.getX(), event.getY());

                handled = onDragged(xDragged, yDragged);
                setRangeIsChanging(handled);
            }
            return handled;
        }

        private void setDragging(boolean dragging) {
            this.isDragging = dragging;
        }

        private boolean onDragged(float x, float y) {
            boolean handled = false;
            if (plotView.isYDragable()) {
                float realDelta = rangeView.fromScreenY(y) - rangeView.fromScreenY(0);
                if (rangeView.getRangeBottom() < rangeView.getRangeTop())
                    realDelta *= -1;

                handled = plotView.offsetYRange(realDelta);
            }
            if (plotView.isXDragable()) {
                float realDelta = rangeView.fromScreenX(x) - rangeView.fromScreenX(0);
                if (rangeView.getRangeLeft() < rangeView.getRangeRight())
                    realDelta *= -1;

                handled = plotView.offsetXRange(realDelta);
            }
            return handled;
        }
    }

    public PlotGestureDetector(Context context, final PlotView plotView, final RangeDrawingView rangeView) {
        this.plotView = plotView;
        this.rangeView = rangeView;

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                boolean handled = false;
                if (plotView.isYZoomable()) {
                    setRangeIsChanging(true);

                    float focusPoint = scaleGestureDetector.getFocusY();
                    float focusPointRatio = focusPoint / plotView.getHeight();

                    float zoom = scaleGestureDetector.getPreviousSpanY() - scaleGestureDetector.getCurrentSpanY();
                    zoom /= plotView.getHeight();
                    float currentRange = Math.abs(rangeView.getRangeTop() - rangeView.getRangeBottom());
                    float zoomValue = zoom * currentRange;

                    float newBottom = rangeView.getRangeBottom() - zoomValue * (1 - focusPointRatio);
                    float newTop = rangeView.getRangeTop() + zoomValue * focusPointRatio;
                    plotView.setYRange(newBottom, newTop);

                    handled = true;
                }

                if (plotView.isXZoomable()) {
                    setRangeIsChanging(true);

                    float focusPoint = scaleGestureDetector.getFocusX();
                    float focusPointRatio = focusPoint / plotView.getWidth();

                    float zoom = scaleGestureDetector.getPreviousSpanX() - scaleGestureDetector.getCurrentSpanX();
                    zoom /= plotView.getWidth();
                    float currentRange = Math.abs(rangeView.getRangeLeft() - rangeView.getRangeRight());
                    float zoomValue = zoom * currentRange;

                    float newLeft = rangeView.getRangeLeft() - zoomValue * focusPointRatio;
                    float newRight = rangeView.getRangeRight() + zoomValue * (1 - focusPointRatio);
                    plotView.setXRange(newLeft, newRight);

                    handled = true;
                }
                return handled;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
                setRangeIsChanging(false);
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = dragDetector.onTouchEvent(event);

        if (plotView.isXZoomable() || plotView.isYZoomable())
            handled = scaleGestureDetector.onTouchEvent(event);

        return handled;
    }

    private void setRangeIsChanging(boolean changing) {
        rangeChanging = changing;
    }
}


public class PlotView extends ViewGroup {
    final static public int DEFAULT_PEN_COLOR = Color.WHITE;

    public static class PlotScale {
        public IScale scale;
        public LabelPartitioner labelPartitioner;
    }
    static public PlotScale log10Scale() {
        PlotScale plotScale = new PlotScale();
        plotScale.scale = new Log10Scale();
        plotScale.labelPartitioner = new LabelPartitionerLog10();
        return plotScale;
    }

    private TitleView titleView;
    private XAxisView xAxisView;
    private YAxisView yAxisView;
    private PlotPainterContainerView mainView;
    private BackgroundPainter backgroundPainter;
    private RangeInfoPainter rangeInfoPainter;

    private boolean xDraggable = false;
    private boolean yDraggable = false;
    private boolean xZoomable = false;
    private boolean yZoomable = false;
    private PlotGestureDetector plotGestureDetector;

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        titleView = new TitleView(context);
        addView(titleView);

        xAxisView = new XAxisView(context);
        addView(xAxisView);

        yAxisView = new YAxisView(context);
        addView(yAxisView);

        this.mainView = new PlotPainterContainerView(context);
        addView(mainView);

        backgroundPainter = new BackgroundPainter(xAxisView, yAxisView);
        mainView.addBackgroundPainter(backgroundPainter);

        rangeInfoPainter = new RangeInfoPainter(this);
        mainView.addForegroundPainter(rangeInfoPainter);

        plotGestureDetector = new PlotGestureDetector(context, this, mainView);
    }

    public void addPlotPainter(IPlotPainter painter) {
        mainView.addPlotPainter(painter);
    }


    public boolean isXDragable() {
        return xDraggable;
    }

    public void setXDragable(boolean xDragable) {
        this.xDraggable = xDragable;
    }

    public boolean isYDragable() {
        return yDraggable;
    }

    public void setYDragable(boolean yDragable) {
        this.yDraggable = yDragable;
    }

    public boolean isXZoomable() {
        return xZoomable;
    }

    public void setXZoomable(boolean xZoomable) {
        this.xZoomable = xZoomable;
    }

    public boolean isYZoomable() {
        return yZoomable;
    }

    public void setYZoomable(boolean yZoomable) {
        this.yZoomable = yZoomable;
    }

    public void setMaxXRange(float left, float right) {
        mainView.setMaxXRange(left, right);
    }

    public void setMaxYRange(float bottom, float top) {
        mainView.setMaxYRange(bottom, top);
    }

    public RectF getMaxRange() {
        return mainView.getMaxRange();
    }

    public void setYScale(PlotScale plotScale) {
        if (yAxisView != null)
            yAxisView.setLabelPartitioner(plotScale.labelPartitioner);
        for (IPlotPainter painter : mainView.getPlotPainters())
            painter.setYScale(plotScale.scale);
    }

    public BackgroundPainter getBackgroundPainter() {
        return backgroundPainter;
    }

    public boolean setXRange(float left, float right) {
        return setXRange(left, right, false);
    }

    public boolean setYRange(float bottom, float top) {
        return setYRange(bottom, top, false);
    }

    public boolean offsetXRange(float offset) {
        return setXRange(mainView.getRangeLeft() + offset, mainView.getRangeRight() + offset, true);
    }

    public boolean offsetYRange(float offset) {
        return setYRange(mainView.getRangeBottom() + offset, mainView.getRangeTop() + offset, true);
    }

    private boolean setXRange(float left, float right, boolean keepDistance) {
        if (!mainView.setXRange(left, right, keepDistance))
            return false;

        // always use the validated range values from the mainView!
        if (hasXAxis())
            xAxisView.setDataRange(mainView.getRangeLeft(), mainView.getRangeRight());

        invalidate();
        return true;
    }

    private boolean setYRange(float bottom, float top, boolean keepDistance) {
        if (!mainView.setYRange(bottom, top, keepDistance))
            return false;

        // always use the validated range values from the mainView!
        if (hasYAxis())
            yAxisView.setDataRange(mainView.getRangeBottom(), mainView.getRangeTop());

        invalidate();
        return true;
    }

    public void invalidate() {
        mainView.invalidate();
        if (xAxisView != null)
            xAxisView.invalidate();
        if (yAxisView != null)
            yAxisView.invalidate();
    }

    public TitleView getTitleView() {
        return titleView;
    }

    public YAxisView getYAxisView() {
        return yAxisView;
    }

    public XAxisView getXAxisView() {
        return xAxisView;
    }

    private boolean hasTitle() {
        return titleView != null && titleView.getVisibility() == View.VISIBLE;
    }

    private boolean hasXAxis() {
        return xAxisView != null && xAxisView.getVisibility() == View.VISIBLE;
    }

    private boolean hasYAxis() {
        return yAxisView != null && yAxisView.getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;
        final int height = bottom - top;
        int titleBottom = 0;
        if (hasTitle())
            titleBottom = (int)titleView.getPreferredHeight();
        final int titleHeight = titleBottom;

        int xAxisTop = height;
        int xAxisLeftOffset = 0;
        int xAxisRightOffset = 0;
        if (hasXAxis()) {
            xAxisTop -= (int)xAxisView.optimalHeight();
            xAxisRightOffset = (int)xAxisView.getAxisRightOffset();
            xAxisLeftOffset = (int)xAxisView.getAxisLeftOffset();
        }

        int yAxisRight = 0;
        int yAxisTopOffset = 0;
        int yAxisBottomOffset = 0;
        if (hasYAxis()) {
            final int mainAreaHeight = xAxisTop - (int)Math.max(yAxisView.getAxisTopOffset(), titleHeight);
            yAxisRight = (int) yAxisView.optimalWidthForHeight(mainAreaHeight);
            yAxisTopOffset = (int) yAxisView.getAxisTopOffset();
            yAxisBottomOffset = (int)yAxisView.getAxisBottomOffset();
        }

        final Rect titleRect = new Rect(yAxisRight, 0, width, titleBottom);
        final Rect xAxisRect = new Rect(yAxisRight - xAxisLeftOffset, xAxisTop, width, height);
        final Rect yAxisRect = new Rect(0, Math.max(0, titleBottom - yAxisTopOffset), yAxisRight,
                xAxisTop + yAxisBottomOffset);
        final Rect mainViewRect = new Rect(yAxisRight, Math.max(titleBottom, yAxisTopOffset), width - xAxisRightOffset,
                xAxisTop);

        if (hasTitle()) {
            titleView.measure(MeasureSpec.makeMeasureSpec(titleRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(titleRect.height(), MeasureSpec.EXACTLY));
            titleView.layout(titleRect.left, titleRect.top, titleRect.right, titleRect.bottom);
        }

        if (hasXAxis()) {
            xAxisView.measure(MeasureSpec.makeMeasureSpec(xAxisRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(xAxisRect.height(), MeasureSpec.EXACTLY));
            xAxisView.layout(xAxisRect.left, xAxisRect.top, xAxisRect.right, xAxisRect.bottom);
        }

        if (hasYAxis()) {
            yAxisView.measure(MeasureSpec.makeMeasureSpec(yAxisRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(yAxisRect.height(), MeasureSpec.EXACTLY));
            yAxisView.layout(yAxisRect.left, yAxisRect.top, yAxisRect.right, yAxisRect.bottom);
        }

        if (mainView != null) {
            mainView.measure(MeasureSpec.makeMeasureSpec(mainViewRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mainViewRect.height(), MeasureSpec.EXACTLY));
            mainView.layout(mainViewRect.left, mainViewRect.top, mainViewRect.right, mainViewRect.bottom);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled = plotGestureDetector.onTouchEvent(event);

        if (handled) {
            ViewParent parent = getParent();
            if (parent != null)
                parent.requestDisallowInterceptTouchEvent(true);
        }

        return handled;
    }
}
