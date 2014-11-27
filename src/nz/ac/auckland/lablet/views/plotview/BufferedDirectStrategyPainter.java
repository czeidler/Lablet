/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.*;

import java.util.ArrayList;
import java.util.List;


public class BufferedDirectStrategyPainter extends BufferedStrategyPainter {

    private RectF dirtyRect = null;
    private boolean invalidated = false;
    private PointF moveBitmap = new PointF();

    @Override
    public boolean hasThreads() {
        return false;
    }

    @Override
    public boolean hasFreeRenderingPipe() {
        return true;
    }

    @Override
    protected void onNewDirtyRegions() {
        containerView.invalidate();
    }

    @Override
    protected void onDirectDraw() {
        RectF range = null;
        if (dirtyRect != null)
            range = dirtyRect;
        dirtyRect = null;

        List<RenderPayload> payloadList = collectAllRenderPayloads(false, range);
        Canvas canvas = startEditingBufferBitmap(isCompleteRedraw(payloadList) || invalidated);
        for (RenderPayload payload : payloadList) {
            ConcurrentPainter painter = payload.getPainter();
            painter.render(canvas, payload);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        dirtyRect = new RectF(getBufferRealRect());
        invalidated = true;
    }

    @Override
    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance) {
        if (keepDistance || false) {
            dirtyRect = getDirtyRect(range, oldRange, keepDistance);

            RectF oldScreen = containerView.toScreen(oldRange);
            RectF screen = containerView.toScreen(range);
            moveBitmap.offset(oldScreen.left - screen.left, oldScreen.top - screen.top);
            containerView.invalidate();
        } else
            invalidate();
    }

    private List<RenderPayload> collectAllRenderPayloads(boolean geometryInfoNeeded, RectF requestedRealRect) {
        List<RenderPayload> payloadList = new ArrayList<>();
        for (ConcurrentPainter painter : childPainters)
            payloadList.addAll(painter.collectRenderPayloads(geometryInfoNeeded, requestedRealRect));

        return payloadList;
    }

    private boolean isCompleteRedraw(List<RenderPayload> payloadList) {
        for (RenderPayload payload : payloadList) {
            if (payload.isCompleteRedraw())
                return true;
        }
        return false;
    }
}
