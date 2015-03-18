/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import nz.ac.auckland.lablet.misc.JoinedList;

import java.util.ArrayList;
import java.util.List;


public class PlotPainterContainerView extends RangeDrawingView {
    final private List<IPlotPainter> backgroundPainters = new ArrayList<>();
    final private List<IPlotPainter> plotPainters = new ArrayList<>();
    final private List<IPlotPainter> foregroundPainters = new ArrayList<>();
    final protected JoinedList<IPlotPainter> allPainters = new JoinedList<>(backgroundPainters, plotPainters,
            foregroundPainters);

    public PlotPainterContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlotPainterContainerView(Context context) {
        super(context);
    }

    public void addBackgroundPainter(IPlotPainter painter) {
        backgroundPainters.add(painter);
        painter.setContainer(this);
    }

    public void addPlotPainter(IPlotPainter painter) {
        plotPainters.add(painter);
        painter.setContainer(this);
        if (getWidth() > 0 && getHeight() > 0)
            painter.onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    public boolean removePlotPainter(IPlotPainter painter) {
        boolean result = plotPainters.remove(painter);
        if (!result)
            return false;
        painter.setContainer(null);
        painter.release();
        invalidate();
        return true;
    }

    public void removeAllPlotPainters() {
        while (plotPainters.size() > 0)
            removePlotPainter(plotPainters.get(0));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        removeAllPlotPainters();
    }

    public void addForegroundPainter(IPlotPainter painter) {
        foregroundPainters.add(painter);
        painter.setContainer(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (IPlotPainter painter : allPainters) {
            canvas.save();
            painter.onDraw(canvas);
            canvas.restore();
        }
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        boolean handled = super.dispatchTouchEvent(event);

        for (IPlotPainter painter : allPainters) {
            if (painter.onTouchEvent(event))
                return true;
        }
        return handled;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        for (IPlotPainter painter : allPainters)
            painter.onSizeChanged(w, h, oldw, oldh);
    }

    public List<IPlotPainter> getPlotPainters() {
        return plotPainters;
    }

    @Override
    public boolean setRange(RectF range, boolean keepDistance) {
        RectF oldRange = getRange();

        if (!super.setRange(range, keepDistance))
            return false;

        RectF newRange = getRange();

        if (newRange.left == oldRange.left && newRange.right == oldRange.right
                && newRange.top == oldRange.top && newRange.bottom == oldRange.bottom)
            return true;

        for (IPlotPainter painter : allPainters)
            painter.onRangeChanged(newRange, oldRange, keepDistance);

        return true;
    }

    @Override
    public boolean setXRange(float left, float right, boolean keepDistance) {
        RectF oldRange = getRange();

        if (!super.setXRange(left, right, keepDistance))
            return false;

        RectF newRange = getRange();

        if (newRange.left == oldRange.left && newRange.right == oldRange.right)
            return true;

        for (IPlotPainter painter : allPainters)
            painter.onRangeChanged(newRange, oldRange, keepDistance);

        return true;
    }

    @Override
    public boolean setYRange(float bottom, float top, boolean keepDistance) {
        RectF oldRange = getRange();

        if (!super.setYRange(bottom, top, keepDistance))
            return false;

        RectF newRange = getRange();

        if (newRange.top == oldRange.top && newRange.bottom == oldRange.bottom)
            return true;

        for (IPlotPainter painter : allPainters)
            painter.onRangeChanged(newRange, oldRange, keepDistance);

        return true;
    }

    public List<AbstractPlotDataAdapter> getPlotDataAdapters() {
        List<AbstractPlotDataAdapter> list = new ArrayList<>();
        for (IPlotPainter painter : allPainters) {
            if (!(painter instanceof StrategyPainter))
                continue;
            List<ConcurrentPainter> childPainters = ((StrategyPainter)painter).getChildPainters();
            for (ConcurrentPainter child : childPainters) {
                if (!(child instanceof  ArrayConcurrentPainter))
                    continue;
                ArrayConcurrentPainter arrayConcurrentPainter = (ArrayConcurrentPainter)child;
                list.add(arrayConcurrentPainter.getAdapter());
            }
        }
        return list;
    }
}
