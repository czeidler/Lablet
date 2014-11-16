/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.animation.*;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import nz.ac.auckland.lablet.views.plotview.axes.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
                handled = true;
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
            if (plotView.isYDraggable()) {
                float realDelta = rangeView.fromScreenY(y) - rangeView.fromScreenY(0);
                if (rangeView.getRangeBottom() < rangeView.getRangeTop())
                    realDelta *= -1;

                handled = plotView.offsetYRange(realDelta);
            }
            if (plotView.isXDraggable()) {
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
        boolean handled = false;

        if (plotView.isXDraggable() || plotView.isYDraggable())
            handled = dragDetector.onTouchEvent(event);

        if (handled)
            return true;

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

    static public PlotScale linearScale() {
        PlotScale plotScale = new PlotScale();
        plotScale.scale = new LinearScale();
        plotScale.labelPartitioner = new LabelPartitionerLinear();
        return plotScale;
    }

    private TitleView titleView;
    private XAxisView xAxisView;
    private YAxisView yAxisView;
    private PlotPainterContainerView mainView;
    private BackgroundPainter backgroundPainter;
    private RangeInfoPainter rangeInfoPainter;

    final static public int AUTO_RANGE_DISABLED = 0;
    final static public int AUTO_RANGE_SCROLL = 10;
    final static public int AUTO_RANGE_ZOOM = 20;
    // only extend the range when the data can't be displayed
    final static public int AUTO_RANGE_ZOOM_EXTENDING = 21;


    private boolean xDraggable = false;
    private boolean yDraggable = false;
    private boolean xZoomable = false;
    private boolean yZoomable = false;
    private AutoRange autoRange = null;
    private PlotGestureDetector plotGestureDetector;

    class AutoRange implements DataStatistics.IListener {
        final private List<DataStatistics> dataStatisticsList = new ArrayList<>();

        private float offsetRatio = 0.4f;

        private ResizePolicy xPolicy = null;
        private ResizePolicy yPolicy = null;

        private RectF previousLimits;

        public AutoRange(List<AbstractXYDataAdapter> adapters, int behaviourX, int behaviourY) {
            setBehaviour(behaviourX, behaviourY);

            for (AbstractXYDataAdapter adapter : adapters) {
                DataStatistics dataStatistics = new DataStatistics(adapter);
                dataStatistics.addListener(this);
                dataStatisticsList.add(dataStatistics);
            }
        }

        public boolean removePainter(IPlotPainter painter) {
            boolean result = mainView.removePlotPainter(painter);

            if (painter instanceof StrategyPainter) {
                StrategyPainter strategyPainter = (StrategyPainter) painter;
                for (ConcurrentPainter child : strategyPainter.getChildPainters()) {
                    XYConcurrentPainter xyChild = (XYConcurrentPainter)child;
                    AbstractXYDataAdapter xyDataAdapter = (AbstractXYDataAdapter)xyChild.getAdapter();
                    if (xyDataAdapter == null)
                        return true;

                    Iterator<DataStatistics> iterator = dataStatisticsList.iterator();
                    while (iterator.hasNext()) {
                        DataStatistics dataStatistics = iterator.next();

                        if (dataStatistics.getAdapter() != xyDataAdapter)
                            continue;

                        dataStatistics.release();
                        iterator.remove();
                        return true;
                    }
                }
            }

            return result;
        }

        public RectF getDataLimits() {
            RectF limits = null;
            for (DataStatistics statistics : dataStatisticsList) {
                RectF currentLimits = statistics.getDataLimits();
                if (currentLimits == null)
                    continue;
                if (limits == null)
                    limits = currentLimits;
                else {
                    if (limits.left > currentLimits.left)
                        limits.left = currentLimits.left;
                    if (limits.top > currentLimits.top)
                        limits.top = currentLimits.top;
                    if (limits.right < currentLimits.right)
                        limits.right = currentLimits.right;
                    if (limits.bottom < currentLimits.bottom)
                        limits.bottom = currentLimits.bottom;
                }
            }
            return limits;
        }

        public void release() {
            for (DataStatistics dataStatistics : dataStatisticsList)
                dataStatistics.release();
            dataStatisticsList.clear();
        }

        private void swapX(RectF rect) {
            float temp = rect.left;
            rect.left = rect.right;
            rect.right = temp;
        }

        private void swapY(RectF rect) {
            float temp = rect.top;
            rect.top = rect.bottom;
            rect.bottom = temp;
        }

        abstract class ResizePolicy {
            abstract void onLimitsChanged(RectF limits, RectF oldRange, boolean flippedAxis);
        }

        class ZoomPolicyH extends ResizePolicy {
            @Override
            void onLimitsChanged(RectF limits, RectF oldRange, boolean flippedAxis) {
                RectF limitsCopy = new RectF(limits);
                if (flippedAxis)
                    swapX(limitsCopy);
                setXRange(limitsCopy.left, limitsCopy.right);
            }
        }

        class ZoomPolicyV extends ResizePolicy {
            @Override
            void onLimitsChanged(RectF limits, RectF oldRange, boolean flippedAxis) {
                RectF limitsCopy = new RectF(limits);
                if (flippedAxis)
                    swapY(limitsCopy);
                setYRange(limitsCopy.bottom, limitsCopy.top);
            }
        }

        class ZoomExtPolicyH extends ResizePolicy {
            @Override
            void onLimitsChanged(RectF limits, RectF oldRange, boolean flippedAxis) {
                RectF newRange = new RectF(oldRange);
                if (flippedAxis)
                    swapX(newRange);

                if (oldRange.left > limits.left || oldRange.left == Float.MAX_VALUE)
                    newRange.left = limits.left;
                if (oldRange.right < limits.right || oldRange.right == Float.MAX_VALUE)
                    newRange.right = limits.right;

                if (flippedAxis)
                    swapX(newRange);
                setXRange(newRange.left, newRange.right);
            }
        }

        class ZoomExtPolicyV extends ResizePolicy {
            @Override
            void onLimitsChanged(RectF limits, RectF oldRange, boolean flippedAxis) {
                RectF newRange = new RectF(oldRange);
                if (flippedAxis)
                    swapY(newRange);

                if (oldRange.top < limits.top || oldRange.top == Float.MAX_VALUE)
                    newRange.top = limits.top;
                if (oldRange.bottom > limits.bottom || oldRange.bottom == Float.MAX_VALUE)
                    newRange.bottom = limits.bottom;

                if (flippedAxis)
                    swapY(newRange);
                setYRange(newRange.bottom, newRange.top);
            }
        }

        class ScrollPolicyH extends ResizePolicy {
            @Override
            void onLimitsChanged(RectF limits, RectF oldRange, boolean flippedAxis) {
                if (previousLimits == null)
                    return;

                // only offset when the limits have changed
                int xOffset = 0;
                if (previousLimits.left > limits.left && oldRange.left >= limits.left)
                    xOffset = -1;
                if (previousLimits.right < limits.right && oldRange.right <= limits.right)
                    xOffset = 1;

                if (flippedAxis)
                    xOffset *= -1;
                if (xOffset != 0)
                    scrollXBy(oldRange.width() * xOffset * offsetRatio);
            }
        }

        class ScrollPolicyV extends ResizePolicy {
            @Override
            void onLimitsChanged(RectF limits, RectF oldRange, boolean flippedAxis) {
                if (previousLimits == null)
                    return;

                // only offset when the limits have changed
                int yOffset = 0;
                if (previousLimits.top > limits.top && oldRange.top >= limits.top)
                    yOffset = -1;
                if (previousLimits.bottom < limits.bottom && oldRange.bottom <= limits.bottom)
                    yOffset = 1;

                if (flippedAxis)
                    yOffset *= -1;
                if (yOffset != 0)
                    scrollYBy(oldRange.height() * yOffset * offsetRatio);
            }
        }

        @Override
        public void onLimitsChanged(DataStatistics dataStatistics) {
            RectF limits = dataStatistics.getDataLimits();
            RectF oldRange = getRange();

            boolean xFlipped = false;
            boolean yFlipped = false;

            if (oldRange.left > oldRange.right) {
                swapX(oldRange);
                xFlipped = true;
            }
            if (oldRange.top < oldRange.bottom) {
                swapY(oldRange);
                yFlipped = true;
            }

            if (xPolicy != null)
                xPolicy.onLimitsChanged(limits, oldRange, xFlipped);
            if (yPolicy != null)
                yPolicy.onLimitsChanged(limits, oldRange, yFlipped);

            previousLimits = new RectF(limits);
        }

        public void setBehaviour(int behaviourX, int behaviourY) {
            switch (behaviourX) {
                case AUTO_RANGE_DISABLED:
                    xPolicy = null;
                    break;
                case AUTO_RANGE_ZOOM:
                    xPolicy = new ZoomPolicyH();
                    break;
                case AUTO_RANGE_ZOOM_EXTENDING:
                    xPolicy = new ZoomExtPolicyH();
                    break;
                case AUTO_RANGE_SCROLL:
                    xPolicy = new ScrollPolicyH();
                    break;
            }

            switch (behaviourY) {
                case AUTO_RANGE_DISABLED:
                    yPolicy = null;
                    break;
                case AUTO_RANGE_ZOOM:
                    yPolicy = new ZoomPolicyV();
                    break;
                case AUTO_RANGE_ZOOM_EXTENDING:
                    yPolicy = new ZoomExtPolicyV();
                    break;
                case AUTO_RANGE_SCROLL:
                    yPolicy = new ScrollPolicyV();
                    break;
            }
        }
    }

    public PlotView(Context context) {
        super(context);

        init(context);
    }

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
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

    public void setRangeListener(RangeDrawingView.IRangeListener listener) {
        mainView.setRangeListener(listener);
    }

    public void addPlotPainter(IPlotPainter painter) {
        mainView.addPlotPainter(painter);
    }

    protected void removePlotPainter(IPlotPainter painter) {
        mainView.removePlotPainter(painter);

        if (autoRange != null)
            autoRange.removePainter(painter);
    }

    public void addForegroundPainter(IPlotPainter painter) {
        mainView.addForegroundPainter(painter);
    }

    public RectF getRange() {
        return mainView.getRange();
    }

    public PointF getRangeMiddle() {
        return mainView.getRangeMiddle();
    }

    public boolean isXDraggable() {
        return xDraggable;
    }

    public void setXDraggable(boolean xDraggable) {
        this.xDraggable = xDraggable;
    }

    public boolean isYDraggable() {
        return yDraggable;
    }

    public void setYDraggable(boolean yDraggable) {
        this.yDraggable = yDraggable;
    }

    public void setDraggable(boolean draggable) {
        setXDraggable(draggable);
        setYDraggable(draggable);
    }

    public void setAutoRange(int behaviourX, int behaviourY) {
        if (autoRange != null) {
            autoRange.release();
            autoRange = null;
        }

        if (behaviourX == AUTO_RANGE_DISABLED && behaviourY == AUTO_RANGE_DISABLED)
            return;

        autoRange = new AutoRange(mainView.getXYDataAdapters(), behaviourX, behaviourY);
    }

    public void autoZoom() {
        RectF limits = autoRange.getDataLimits();
        if (limits == null)
            return;
        setXRange(limits.left, limits.right);
        setYRange(limits.bottom, limits.top);
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

    public void setZoomable(boolean zoomable) {
        setXZoomable(zoomable);
        setYZoomable(zoomable);
    }

    public void setMaxXRange(float left, float right) {
        mainView.setMaxXRange(left, right);
    }

    public void setMaxYRange(float bottom, float top) {
        mainView.setMaxYRange(bottom, top);
    }

    public void setMinXRange(float range) {
        mainView.setMinXRange(range);
    }

    public void setMinYRange(float range) {
        mainView.setMinYRange(range);
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

    public void setRange(RectF range) {
        setXRange(range.left, range.right);
        setYRange(range.bottom, range.top);
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

    public boolean scrollXBy(float offset) {
        scrollAnimator.animateXScroll(offset);
        return true;
    }

    public boolean scrollYBy(float offset) {
        scrollAnimator.animateYScroll(offset);
        return true;
    }

    ScrollAnimator scrollAnimator = new ScrollAnimator();
    class ScrollAnimator {
        final private int DURATION = 500;
        private AnimatorSet animator = null;

        public void animateXScroll(float offset) {
            ValueAnimator valueAnimator = ObjectAnimator.ofFloat(mainView.getRangeLeft(), mainView.getRangeLeft()
                    + offset);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) animation.getAnimatedValue();
                    setXRange(value, value + mainView.getRangeRight() - mainView.getRangeLeft(), true);
                }
            });
            animate(valueAnimator);
        }

        public void animateYScroll(float offset) {
            ValueAnimator valueAnimator = ObjectAnimator.ofFloat(mainView.getRangeBottom(), mainView.getRangeBottom()
                    + offset);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) animation.getAnimatedValue();
                    setXRange(value, value + mainView.getRangeTop() - mainView.getRangeBottom(), true);
                }
            });
            animate(valueAnimator);
        }

        private void animate(Animator newAnimator) {
            if (animator != null)
                animator.cancel();

            final AnimatorSet set = new AnimatorSet();
            set.play(newAnimator);
            set.setDuration(DURATION);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animator = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animator = null;
                }
            });
            set.start();
            animator = set;
        }
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
        for (IPlotPainter plotPainter : mainView.getPlotPainters())
            plotPainter.invalidate();
        if (titleView != null)
            titleView.invalidate();
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
        boolean handled = super.dispatchTouchEvent(event);
        if (handled)
            return true;

        handled = plotGestureDetector.onTouchEvent(event);
        if (handled) {
            ViewParent parent = getParent();
            if (parent != null)
                parent.requestDisallowInterceptTouchEvent(true);
        }

        return handled;
    }
}
