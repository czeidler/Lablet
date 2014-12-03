/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.*;

import java.util.List;


public class BufferedDirectStrategyPainter extends BufferedStrategyPainter {
    private boolean invalidated = false;

    @Override
    public boolean hasThreads() {
        return false;
    }

    @Override
    public boolean hasFreeRenderingPipe() {
        return true;
    }

    @Override
    protected void onNewDirtyRegions(RectF newDirt) {
        super.onNewDirtyRegions(newDirt);

        containerView.invalidate();
    }

    @Override
    protected void onDirectDraw() {
        Canvas canvas = startEditingBufferBitmap(invalidated);

        RectF range = getDirtyRect();
        RectF bufferRealRect = getBufferRealRect();
        if (invalidated)
            range = new RectF(bufferRealRect);
        clearDirtyRect();

        List<RenderPayload> payloadList = collectAllRenderPayloads(false, range, bufferRealRect);
        for (RenderPayload payload : payloadList) {
            ConcurrentPainter painter = payload.getPainter();
            painter.render(canvas, payload);
        }
        invalidated = false;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        invalidated = true;
    }
}
