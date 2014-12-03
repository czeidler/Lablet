/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;


public interface IPlotPainter {
    public void setContainer(PlotPainterContainerView view);

    public void onSizeChanged(int width, int height, int oldw, int oldh);
    public void onDraw(Canvas canvas);
    public boolean onTouchEvent(MotionEvent event);
    /**
     * Invalidate the state of the painter.
     *
     * For example, cached drawings have to be invalidated or a complete redraw has to be triggered.
     */
    public void invalidate();

    public void setXScale(IScale xScale);
    public void setYScale(IScale yScale);

    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance);
}
