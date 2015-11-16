/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.marker;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;
import nz.ac.auckland.lablet.views.plotview.IPlotPainter;
import nz.ac.auckland.lablet.views.plotview.IScale;
import nz.ac.auckland.lablet.views.plotview.PlotPainterContainerView;

import java.util.ArrayList;
import java.util.List;


public class TreePlotPainter<T extends IPlotPainter> implements IPlotPainter {
    private PlotPainterContainerView containerView;
    final protected List<T> childList = new ArrayList<>();

    public void addChild(T child) {
        this.childList.add(child);
        setContainerView(containerView, child);
    }

    public boolean removeChild(T child) {
        if (!this.childList.remove(child))
            return false;

        child.setContainer(null);
        return true;
    }

    @Override
    public void setContainer(PlotPainterContainerView view) {
        this.containerView = view;
        for (IPlotPainter child : childList)
            setContainerView(view, child);
    }

    private void setContainerView(PlotPainterContainerView view, IPlotPainter child) {
        child.setContainer(view);
        if (view == null)
            return;
        if (view.getWidth() > 0 && view.getHeight() > 0)
            child.onSizeChanged(view.getWidth(), view.getHeight(), 0, 0);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        for (IPlotPainter child : childList)
            child.onSizeChanged(width, height, oldw, oldh);
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (IPlotPainter child : childList)
            child.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (IPlotPainter child : childList) {
            if (child.onTouchEvent(event))
                return true;
        }
        return false;
    }

    @Override
    public void invalidate() {
        for (IPlotPainter child : childList)
            child.invalidate();
    }

    @Override
    public void setXScale(IScale xScale) {
        for (IPlotPainter child : childList)
            child.setXScale(xScale);
    }

    @Override
    public void setYScale(IScale yScale) {
        for (IPlotPainter child : childList)
            child.setYScale(yScale);
    }

    @Override
    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance) {
        for (IPlotPainter child : childList)
            child.onRangeChanged(range, oldRange, keepDistance);
    }

    @Override
    public void release() {
        for (IPlotPainter child : childList)
            child.release();
    }
}
