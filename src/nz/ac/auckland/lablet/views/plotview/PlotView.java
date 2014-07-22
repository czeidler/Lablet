/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.*;
import nz.ac.auckland.lablet.views.plotview.axes.*;


public class PlotView extends ViewGroup {
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

    private XAxisView xAxisView;
    private YAxisView yAxisView;
    private PlotPainterContainerView mainView;

    private ScaleGestureDetector scaleGestureDetector;
    private DragDetector dragDetector = new DragDetector();

    private boolean xDraggable = false;
    private boolean yDraggable = false;
    private boolean xZoomable = false;
    private boolean yZoomable = false;

    class DragDetector {
        public PointF point = new PointF(-1, -1);

        public boolean onTouchEvent(MotionEvent event) {
            if (event.getPointerCount() > 1) {
                point.set(-1, -1);
                return false;
            }
            int action = event.getActionMasked();
            boolean handled = false;
            if (action == MotionEvent.ACTION_DOWN) {
                point.set(event.getX(), event.getY());
            } else if (action == MotionEvent.ACTION_UP) {
                point.set(-1, -1);
            } else if (action == MotionEvent.ACTION_MOVE) {
                float xDragged = event.getX() - point.x;
                float yDragged = event.getY() - point.y;
                point.set(event.getX(), event.getY());

                handled = onDragged(xDragged, yDragged);
            }
            return handled;
        }

        private boolean onDragged(float x, float y) {
            if (yDraggable) {
                float realDelta = mainView.fromScreenY(y) - mainView.fromScreenY(0);
                if (mainView.getRangeBottom() < mainView.getRangeTop())
                    realDelta *= -1;
                float newBottom = mainView.getRangeBottom() + realDelta;
                float newTop = mainView.getRangeTop() + realDelta;

                setYRange(newBottom, newTop);
                return true;
            }
            if (xDraggable) {
                float realDelta = mainView.fromScreenX(x) - mainView.fromScreenX(0);
                if (mainView.getRangeLeft() < mainView.getRangeRight())
                    realDelta *= -1;
                float newLeft = mainView.getRangeBottom() + realDelta;
                float newRight = mainView.getRangeRight() + realDelta;

                setXRange(newLeft, newRight);
                return true;
            }
            return false;
        }
    }

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                if (yZoomable) {
                    float focusPoint = scaleGestureDetector.getFocusY();
                    float focusPointRatio = focusPoint / mainView.getHeight();

                    float zoom = scaleGestureDetector.getPreviousSpanY() - scaleGestureDetector.getCurrentSpanY();
                    zoom /= getHeight();
                    float currentRange = Math.abs(mainView.getRangeTop() - mainView.getRangeBottom());
                    float zoomValue = zoom * currentRange;

                    float newBottom = mainView.getRangeBottom() - zoomValue * (1 - focusPointRatio);
                    float newTop = mainView.getRangeTop() + zoomValue * focusPointRatio;
                    setYRange(newBottom, newTop);

                    return true;
                }

                if (xZoomable) {
                    float focusPoint = scaleGestureDetector.getFocusX();
                    float focusPointRatio = focusPoint / mainView.getWidth();

                    float zoom = scaleGestureDetector.getPreviousSpanX() - scaleGestureDetector.getCurrentSpanX();
                    zoom /= getWidth();
                    float currentRange = Math.abs(mainView.getRangeLeft() - mainView.getRangeRight());
                    float zoomValue = zoom * currentRange;

                    float newLeft = mainView.getRangeLeft() - zoomValue * focusPointRatio;
                    float newRight = mainView.getRangeRight() + zoomValue * (1 - focusPointRatio);
                    setXRange(newLeft, newRight);

                    return true;
                }
                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

            }
        });

        xAxisView = new XAxisView(context);
        addView(xAxisView);

        yAxisView = new YAxisView(context);
        addView(yAxisView);

        this.mainView = new PlotPainterContainerView(context);
        addView(mainView);
    }

    public void addPlotPainter(IPlotPainter painter) {
        mainView.addPlotPainter(painter);
    }

    public void setYScale(PlotScale plotScale) {
        if (yAxisView != null)
            yAxisView.setLabelPartitioner(plotScale.labelPartitioner);
        for (IPlotPainter painter : mainView.getPlotPainters())
            painter.setYScale(plotScale.scale);
    }


    public void setYRange(float bottom, float top) {
        if (hasYAxis())
            yAxisView.setDataRange(bottom, top);
        mainView.setRangeY(bottom, top);

        invalidate();
    }

    public void setXRange(float left, float right) {
        if (hasXAxis())
            xAxisView.setDataRange(left, right);
        mainView.setRangeX(left, right);

        invalidate();
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

    public void invalidate() {
        mainView.invalidate();
        if (xAxisView != null)
            xAxisView.invalidate();
        if (yAxisView != null)
            yAxisView.invalidate();
    }

    public YAxisView getYAxisView() {
        return yAxisView;
    }

    public XAxisView getXAxisView() {
        return xAxisView;
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
        int xAxisTop = height;
        int xAxisLeftOffset = 0;
        int xAxisRightOffset = 0;
        if (hasXAxis()) {
            xAxisTop -= (int)xAxisView.optimalHeight();
            xAxisRightOffset = (int)xAxisView.getAxisRightOffset();
            xAxisLeftOffset = (int)xAxisView.getAxisLeftOffset();
        }
        final int mainAreaHeight = xAxisTop - (int)yAxisView.getAxisTopOffset();
        final int yAxisRight = (int)yAxisView.optimalWidthForHeight(mainAreaHeight);

        final Rect xAxisRect = new Rect(yAxisRight - xAxisLeftOffset, xAxisTop, width, height);
        final Rect yAxisRect = new Rect(0, 0, yAxisRight, xAxisTop + (int)yAxisView.getAxisBottomOffset());
        final Rect mainViewRect = new Rect(yAxisRight, (int)yAxisView.getAxisTopOffset(),
                width - xAxisRightOffset, xAxisTop);

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
    public boolean onTouchEvent(MotionEvent event) {
        dragDetector.onTouchEvent(event);
        if (scaleGestureDetector.onTouchEvent(event))
            return true;

        return super.onTouchEvent(event);
    }
}
