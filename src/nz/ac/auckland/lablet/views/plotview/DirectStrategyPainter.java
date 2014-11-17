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

import java.util.List;


public class DirectStrategyPainter extends StrategyPainter {

    @Override
    public boolean hasThreads() {
        return false;
    }

    @Override
    public boolean hasFreeRenderingPipe() {
        return true;
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        for (ConcurrentPainter painter : childPainters) {
            List<RenderPayload> payloadList = painter.collectRenderPayloads(false, getContainerView().getRange());

            for (RenderPayload renderPayload : payloadList)
                painter.render(canvas, renderPayload);
        }
    }

    @Override
    protected void onNewDirtyRegions() {
        containerView.invalidate();
    }

    @Override
    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance) {
        invalidate();
    }

}
