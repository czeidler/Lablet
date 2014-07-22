/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.Canvas;


public interface IPlotPainter {
    public void setContainer(PlotPainterContainerView view);

    public void onSizeChanged(int width, int height, int oldw, int oldh);
    public void onDraw(Canvas canvas);

    void setXScale(IScale xScale);
    void setYScale(IScale yScale);

    void onXRangeChanged(float left, float right, float oldLeft, float oldRight);
    void onYRangeChanged(float bottom, float top, float oldBottom, float oldTop);
}
