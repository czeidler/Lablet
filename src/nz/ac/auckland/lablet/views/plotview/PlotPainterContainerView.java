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
    private boolean autoZoom = true;

    public PlotPainterContainerView(Context context) {
        super(context);
    }

    public void addPlotPainter(IPlotPainter painter) {
        plotPainters.add(painter);
        painter.setContainer(this);
    }

    public void onXRangeChanged(IPlotPainter painter) {
        if (autoZoom)
            autoZoomXRange();
    }

    public void onYRangeChanged(IPlotPainter painter) {
        if (autoZoom)
            autoZoomYRange();
    }

    private void autoZoomXRange() {

    }

    private void autoZoomYRange() {

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
}
