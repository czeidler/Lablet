/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;


public class BufferedStrategyPainter extends StrategyPainter {
    private Bitmap offScreenBitmap;
    private Canvas offScreenCanvas;

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
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        offScreenBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        offScreenCanvas = new Canvas(offScreenBitmap);
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

    @Override
    public void onDraw(Canvas canvas) {
        if (offScreenBitmap == null)
            return;

        RectF range = null;
        if (invalidated)
            range = containerView.getRange();


        List<RenderPayload> payloadList = collectAllRenderPayloads(false, range);
        if (isCompleteRedraw(payloadList) || invalidated)
            offScreenBitmap.eraseColor(Color.TRANSPARENT);

        for (RenderPayload payload : payloadList) {
            ConcurrentPainter painter = payload.getPainter();
            painter.render(offScreenCanvas, payload);
        }

        canvas.drawBitmap(offScreenBitmap, 0, 0, null);

        invalidated = false;
    }

    @Override
    protected void onNewDirtyRegions() {
        containerView.invalidate();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        invalidated = true;
    }
}