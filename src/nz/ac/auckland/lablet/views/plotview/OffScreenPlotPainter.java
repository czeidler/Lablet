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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class RenderTask {
    final private OffScreenPlotPainter plotPainter;

    final private AtomicBoolean running = new AtomicBoolean();
    private Thread thread;
    final private Handler uiHandler = new Handler();
    private List<OffScreenPlotPainter.RenderPayload> payloadList;

    public RenderTask(OffScreenPlotPainter plotPainter) {
        this.plotPainter = plotPainter;
    }

    Runnable renderRunnable = new Runnable() {
        @Override
        public void run() {
            int size = payloadList.size();
            int index = 0;
            for (OffScreenPlotPainter.RenderPayload payload : payloadList) {
                Rect screenRect = payload.screenRect;
                Bitmap bitmap = Bitmap.createBitmap(screenRect.width(), screenRect.height(), Bitmap.Config.ARGB_8888);
                Canvas bitmapCanvas = new Canvas(bitmap);

                bitmap.eraseColor(Color.TRANSPARENT);
                plotPainter.render(bitmapCanvas, payload);

                // set running to false before notifying the ui thread
                if (index == size - 1) {
                    payloadList = null;
                    running.set(false);
                }
                publishBitmap(payload, bitmap);
                index++;
            }
        }
    };

    private void publishBitmap(final OffScreenPlotPainter.RenderPayload payload, Bitmap bitmap) {
        payload.resultBitmap = bitmap;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                plotPainter.onOffScreenRenderingFinished(payload);
            }
        });
    }

    public boolean start(List<OffScreenPlotPainter.RenderPayload> payloadList) {
        if (running.get())
            return false;
        this.payloadList = payloadList;
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
    private List<RenderPayload> payloadQueue = new ArrayList<>();

    public class RenderPayload {
        public RectF realDataRect;
        public Rect screenRect;
        public Bitmap resultBitmap;
        public boolean clearParentBitmap = false;

        public RenderPayload(RectF realDataRect, Rect screenRect) {
            this.realDataRect = realDataRect;
            this.screenRect = screenRect;
        }
    }

    protected void triggerOffScreenRendering(RenderPayload payload) {
        if (renderTask.isRendering()) {
            payloadQueue.add(payload);
            return;
        }
        List<RenderPayload> payloadList = new ArrayList<>();
        payloadList.addAll(payloadQueue);
        payloadQueue.clear();
        payloadList.add(payload);
        renderTask.start(payloadList);
    }

    protected void onOffScreenRenderingFinished(RenderPayload payload) {
        Bitmap resultBitmap = payload.resultBitmap;
        Rect sourceRect = new Rect(0, 0, resultBitmap.getWidth(), resultBitmap.getHeight());
        Rect targetRect = containerView.toScreen(payload.realDataRect);
        if (payload.clearParentBitmap)
            this.bitmap.eraseColor(Color.TRANSPARENT);
        bitmapCanvas.drawBitmap(resultBitmap, sourceRect, targetRect, null);
        containerView.invalidate();

        if (!renderTask.isRendering() && payloadQueue.size() > 0) {
            renderTask.start(payloadQueue);
            payloadQueue = new ArrayList<>();
        }
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
