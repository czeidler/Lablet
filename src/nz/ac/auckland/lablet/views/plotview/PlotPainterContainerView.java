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

import java.util.ArrayList;
import java.util.List;


public class PlotPainterContainerView extends RangeDrawingView {
    final private List<IPlotPainter> plotPainters = new ArrayList();

    public PlotPainterContainerView(Context context) {
        super(context);
    }

    public void addPlotPainter(IPlotPainter painter) {
        plotPainters.add(painter);
        painter.setContainer(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (IPlotPainter painter : plotPainters) {
            canvas.save();
            painter.onDraw(canvas);
            canvas.restore();
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        for (IPlotPainter painter : plotPainters)
            painter.onSizeChanged(w, h, oldw, oldh);
    }

    public List<IPlotPainter> getPlotPainters() {
        return plotPainters;
    }

    @Override
    public void setRangeX(float left, float right) {
        float oldLeft = getRangeLeft();
        float oldRight = getRangeRight();

        super.setRangeX(left, right);

        for (IPlotPainter painter : getPlotPainters())
            painter.onXRangeChanged(left, right, oldLeft, oldRight);
    }

    public void setRangeY(float bottom, float top) {
        float oldBottom = getRangeBottom();
        float oldTop = getRangeTop();

        super.setRangeY(bottom, top);

        for (IPlotPainter painter : getPlotPainters())
            painter.onYRangeChanged(bottom, top, oldBottom, oldTop);
    }
}
