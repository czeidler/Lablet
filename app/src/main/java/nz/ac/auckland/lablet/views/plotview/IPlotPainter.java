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
    void setContainer(PlotPainterContainerView view);

    void onSizeChanged(int width, int height, int oldw, int oldh);
    void onDraw(Canvas canvas);
    boolean onTouchEvent(MotionEvent event);
    /**
     * Invalidate the state of the painter.
     *
     * For example, cached drawings have to be invalidated or a complete redraw has to be triggered.
     */
    void invalidate();

    void setXScale(IScale xScale);
    void setYScale(IScale yScale);

    void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance);

    void release();
}
