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


/**
 * A plot painter is used to draw within a {@link PlotPainterContainerView}.
 *
 * The plot painter receives touch events.
 */
public interface IPlotPainter {
    /**
     * Sets the parent container view.
     *
     * @param view parent container
     */
    void setContainer(PlotPainterContainerView view);

    void onSizeChanged(int width, int height, int oldw, int oldh);
    void onDraw(Canvas canvas);

    /**
     * Handle touch events from the parent view.
     *
     * @param event that has been received in the parent view
     * @return true if the event has been handled
     */
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
