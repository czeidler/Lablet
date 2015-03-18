/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


import android.graphics.RectF;
import android.view.MotionEvent;

abstract public class AbstractPlotPainter implements IPlotPainter {
    protected PlotPainterContainerView containerView;

    protected IScale xScale = new LinearScale();
    protected IScale yScale = new LinearScale();

    public void setContainer(PlotPainterContainerView view) {
        this.containerView = view;
        if (view == null)
            return;
        invalidateContainerView();
        onAttachedToView();
    }

    protected void onAttachedToView() {

    }

    public void release() {

    }

    public PlotPainterContainerView getContainerView() {
        return containerView;
    }

    @Override
    public void setXScale(IScale xScale) {
        this.xScale = xScale;
    }

    @Override
    public void setYScale(IScale yScale) {
        this.yScale = yScale;
    }

    public IScale getXScale() {
        return xScale;
    }

    public IScale getYScale() {
        return yScale;
    }

    @Override
    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance) {

    }

    @Override
    public void invalidate() {
        invalidateContainerView();
    }

    protected void invalidateContainerView() {
        if (containerView != null)
            containerView.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
