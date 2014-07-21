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
            if (size == 0) {
                running.set(false);
                return;
            }
            int index = 0;
            for (OffScreenPlotPainter.RenderPayload payload : payloadList) {
                Rect screenRect = payload.getScreenRect();
                Bitmap bitmap = Bitmap.createBitmap(screenRect.width(), screenRect.height(), Bitmap.Config.ARGB_8888);
                Canvas bitmapCanvas = new Canvas(bitmap);
                // move the canvas over the bitmap
                bitmapCanvas.translate(-screenRect.left, -screenRect.top);

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
        payload.setResultBitmap(bitmap);
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
        running.set(true);
        thread = new Thread(renderRunnable);
        thread.start();
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
    private IsRenderingDrawer isRenderingDrawer = new IsRenderingDrawer();

    public class RenderPayload {
        private RectF realDataRect;
        private Rect screenRect;
        private Bitmap resultBitmap;
        private boolean clearParentBitmap = false;

        public RenderPayload(RectF realDataRect, Rect screenRect) {
            this.realDataRect = realDataRect;
            this.screenRect = screenRect;
        }

        public RectF getRealDataRect() {
            return realDataRect;
        }

        public void setRealDataRect(RectF realDataRect) {
            this.realDataRect = realDataRect;
        }

        public Rect getScreenRect() {
            return screenRect;
        }

        public void setScreenRect(Rect screenRect) {
            this.screenRect = screenRect;
        }

        public Bitmap getResultBitmap() {
            return resultBitmap;
        }

        public void setResultBitmap(Bitmap resultBitmap) {
            this.resultBitmap = resultBitmap;
        }

        public boolean isClearParentBitmap() {
            return clearParentBitmap;
        }

        public void setClearParentBitmap(boolean clearParentBitmap) {
            this.clearParentBitmap = clearParentBitmap;
        }
    }

    protected boolean hasFreeRenderingPipe() {
        return !renderTask.isRendering();
    }

    protected void emptyOffScreenRenderingQueue() {
        payloadQueue.clear();
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
        Rect targetRect = containerView.toScreen(payload.realDataRect);
        if (payload.clearParentBitmap)
            this.bitmap.eraseColor(Color.TRANSPARENT);
        bitmapCanvas.drawBitmap(resultBitmap, null, targetRect, null);
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

    private boolean isRendering() {
        return renderTask.isRendering();
    }

    class IsRenderingDrawer {
        private long renderTimerStart = -1;
        private Paint paint = new Paint();
        final private long TIME_THRESHOLD = 300;

        public IsRenderingDrawer() {
            paint.setColor(Color.WHITE);
        }

        public void onDraw(Canvas canvas) {
            // draw rendering notice
            if (isRendering()) {
                long currentTime = System.currentTimeMillis();
                if (renderTimerStart < 0)
                    renderTimerStart = currentTime;
                long timeDiff = currentTime - renderTimerStart;
                if (timeDiff <= TIME_THRESHOLD)
                    return;

                long numberOfDots = (timeDiff / 500) % 4;
                String text = "Rendering";
                for (int i = 0; i < numberOfDots; i++)
                    text += ".";

                float textHeight = paint.descent() - paint.ascent();
                canvas.drawText(text, 5, textHeight + 5, paint);

                containerView.invalidate();
            } else {
                renderTimerStart = -1;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (bitmap != null)
            canvas.drawBitmap(bitmap, 0, 0, null);

        isRenderingDrawer.onDraw(canvas);
    }
}
