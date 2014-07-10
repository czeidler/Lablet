/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.*;
import android.os.Handler;

import java.util.concurrent.atomic.AtomicBoolean;

class RenderTask {
    final private OffScreenPlotPainter plotPainter;

    final private AtomicBoolean running = new AtomicBoolean();
    private Thread thread;
    final private Handler uiHandler = new Handler();
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private OffScreenPlotPainter.RenderPayload payload;

    public RenderTask(OffScreenPlotPainter plotPainter) {
        this.plotPainter = plotPainter;
    }

    Runnable renderRunnable = new Runnable() {
        @Override
        public void run() {
            bitmap.eraseColor(Color.TRANSPARENT);
            plotPainter.render(bitmapCanvas, payload);
            publishBitmap(payload, bitmap);
            // don't touch it anymore after publishing it in the ui thread
            payload = null;
            bitmap = null;
            bitmapCanvas = null;

            running.set(false);
        }
    };

    private void publishBitmap(final OffScreenPlotPainter.RenderPayload payload, final Bitmap b) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                plotPainter.onOffScreenRenderingFinished(payload, b);
            }
        });
    }

    public boolean start(OffScreenPlotPainter.RenderPayload payload, Rect screenRect) {
        if (running.get())
            return false;
        this.payload = payload;
        bitmap = Bitmap.createBitmap(screenRect.width(), screenRect.height(), Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        thread = new Thread(renderRunnable);
        thread.start();
        running.set(true);
        return true;
    }

    public void stop() {
        if (!running.get())
            return;
        running.set(false);
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isRendering() {
        return running.get();
    }
}


abstract public class OffScreenPlotPainter extends AbstractPlotPainter {
    protected Bitmap bitmap = null;
    protected Canvas bitmapCanvas = null;
    final private RenderTask renderTask = new RenderTask(this);
    protected long validId = 0;
    //private RenderTask renderTask = new RenderTask(this);

    public class RenderPayload {
        public RenderPayload(RectF realDataRect) {
            this.realDataRect = realDataRect;
        }

        public RectF realDataRect;
        public long validId;
    }

    protected void triggerOffScreenRendering(RenderPayload payload) {
        Rect screenRect = containerView.toScreen(payload.realDataRect);
        //renderTask = new RenderTask(this);
        payload.validId = validId;
        renderTask.start(payload, screenRect);
    }

    protected void onOffScreenRenderingFinished(RenderPayload payload, Bitmap bitmap) {
        if (validId > payload.validId)
            return;

        Rect sourceRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect targetRect = containerView.toScreen(payload.realDataRect);
        bitmapCanvas.drawBitmap(bitmap, sourceRect, targetRect, null);
        containerView.invalidate();
    }

    protected void discardOngoingRendering() {
        validId++;
    }

    abstract protected void render(Canvas bitmapCanvas, RenderPayload payload);

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w <= 0 || h <= 0)
            return;

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        bitmap.eraseColor(Color.TRANSPARENT);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (bitmap != null)
            canvas.drawBitmap(bitmap, 0, 0, null);
    }
}
