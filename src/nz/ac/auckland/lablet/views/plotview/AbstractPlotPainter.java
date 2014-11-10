/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


import android.view.MotionEvent;

abstract public class AbstractPlotPainter implements IPlotPainter {
    protected PlotPainterContainerView containerView;

    protected IScale xScale = new LinearScale();
    protected IScale yScale = new LinearScale();

    public void setContainer(PlotPainterContainerView view) {
        this.containerView = view;
        containerView.invalidate();
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

    @Override
    public void onXRangeChanged(float left, float right, float oldLeft, float oldRight) {

    }

    @Override
    public void onYRangeChanged(float bottom, float top, float oldBottom, float oldTop) {

    }

    @Override
    public void invalidate() {
        if (containerView != null)
            containerView.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
