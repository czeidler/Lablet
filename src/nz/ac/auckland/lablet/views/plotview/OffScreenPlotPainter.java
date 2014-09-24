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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


class RenderTask {
    final private OffScreenPlotPainter plotPainter;

    final private AtomicBoolean running = new AtomicBoolean();
    private Future thread;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    final private Handler uiHandler = new Handler();
    private List<OffScreenPlotPainter.RenderPayload> payloadList;

    public RenderTask(OffScreenPlotPainter plotPainter) {
        this.plotPainter = plotPainter;
        running.set(false);
    }

    Runnable renderRunnable = new Runnable() {
        @Override
        public void run() {
            int size = payloadList.size();
            if (size == 0) {
                running.set(false);
                return;
            }
            for (int index = 0; payloadList != null && index < payloadList.size(); index++) {
                OffScreenPlotPainter.RenderPayload payload = payloadList.get(index);

                Rect screenRect = payload.getScreenRect();
                Bitmap bitmap;
                if (screenRect.width() > 0 && screenRect.height() > 0) {
                    // create bitmap for drawing
                    bitmap = Bitmap.createBitmap(screenRect.width(), screenRect.height(), Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.TRANSPARENT);
                    payload.setResultBitmap(bitmap);

                    Canvas bitmapCanvas = new Canvas(bitmap);
                    // move the canvas over the bitmap
                    bitmapCanvas.translate(-screenRect.left, -screenRect.top);

                    plotPainter.render(bitmapCanvas, payload);
                }

                boolean done = false;
                if (index == size - 1) {
                    payloadList = null;
                    done = true;
                }
                publishBitmap(payload, done);
            }
        }
    };

    private void publishBitmap(final OffScreenPlotPainter.RenderPayload payload, final boolean done) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                plotPainter.onMergeOffScreenRendering(payload);

                if (done) {
                    running.set(false);
                    plotPainter.onOffScreenRenderingFinished();
                }
            }
        });
    }

    public boolean start(List<OffScreenPlotPainter.RenderPayload> payloadList) {
        if (running.get())
            return false;
        this.payloadList = payloadList;
        running.set(true);
        thread = threadPool.submit(renderRunnable);
        return true;
    }

    public void stop() {
        if (!running.get())
            return;
        running.set(false);
        try {
            thread.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean isRendering() {
        return running.get();
    }
}


abstract public class OffScreenPlotPainter extends AbstractPlotDataPainter {
    final protected OffScreenBitmap offScreenBitmap = new OffScreenBitmap();

    final private RenderTask renderTask = new RenderTask(this);
    private List<RenderPayload> payloadQueue = new ArrayList<>();
    final private IsRenderingDrawer isRenderingDrawer = new IsRenderingDrawer();

    private Paint offScreenPaint = null;

    protected class OffScreenBitmap {
        private Bitmap bitmap;
        private Canvas canvas;
        final private RectF realRect = new RectF(0, 0, Float.MIN_VALUE, Float.MIN_VALUE);

        public void setTo(Bitmap bitmap, RectF real) {
            this.bitmap = bitmap;
            if (bitmap == null)
                this.canvas = null;
            else
                this.canvas = new Canvas(bitmap);
            this.realRect.set(real);
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public Canvas getCanvas() {
            return canvas;
        }

        public RectF getRealRect() {
            return realRect;
        }

        public void setRealRect(RectF realRect) {
            this.realRect.set(realRect);
        }
    }

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

    protected Canvas getBitmapCanvas() {
        return offScreenBitmap.getCanvas();
    }

    protected void triggerOffScreenRendering(RenderPayload payload) {
        if (renderTask.isRendering() || offScreenBitmap.getCanvas() == null) {
            payloadQueue.add(payload);
            return;
        }

        payloadQueue.add(payload);
        renderQueue();
    }

    private void renderQueue() {
        if (payloadQueue.size() == 0)
            return;

        List<RenderPayload> payloadsToRender = payloadQueue;
        payloadQueue = new ArrayList<>();
        renderTask.start(payloadsToRender);
    }

    protected void onMergeOffScreenRendering(RenderPayload payload) {
        if (payload.clearParentBitmap) {
            onSetupOffScreenBitmap();
            offScreenBitmap.getBitmap().eraseColor(Color.TRANSPARENT);
        }

        Bitmap resultBitmap = payload.resultBitmap;
        if (resultBitmap != null) {
            Rect targetRect = containerView.toScreen(payload.realDataRect);
            Rect offScreenRect = containerView.toScreen(offScreenBitmap.getRealRect());
            targetRect.offset(-offScreenRect.left, -offScreenRect.top);
            Canvas canvas = offScreenBitmap.getCanvas();
            canvas.drawBitmap(resultBitmap, null, targetRect, null);
        }
        containerView.invalidate();
    }

    protected void onOffScreenRenderingFinished() {
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

        onSetupOffScreenBitmap();
        invalidate();
    }

    protected void onSetupOffScreenBitmap() {
        if (offScreenBitmap.getBitmap() == null) {
            Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(), containerView.getHeight(),
                    Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            RectF bitmapRealRect = containerView.getRange();
            offScreenBitmap.setTo(bitmap, bitmapRealRect);
        } else
            offScreenBitmap.setRealRect(containerView.getRange());
    }

    public void setOffScreenPaint(Paint paint) {
        this.offScreenPaint = paint;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Bitmap bitmap = offScreenBitmap.getBitmap();
        if (bitmap != null) {
            Rect bitmapScreenRect = containerView.toScreen(offScreenBitmap.getRealRect());
            canvas.drawBitmap(bitmap, null, bitmapScreenRect, offScreenPaint);
        }

        isRenderingDrawer.onDraw(canvas);
    }

    private boolean isRendering() {
        return renderTask.isRendering();
    }

    class IsRenderingDrawer {
        private long renderTimerStart = -1;
        private Paint paint = new Paint();
        private Paint backGroundPaint = new Paint();
        final private long TIME_THRESHOLD = 300;

        public IsRenderingDrawer() {
            paint.setColor(Color.BLACK);
            backGroundPaint.setColor(Color.WHITE);
            backGroundPaint.setStyle(Paint.Style.FILL);
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

                final int offset = 5;
                final Rect textBounds = new Rect();
                paint.getTextBounds("Rendering...", 0, "Rendering...".length(), textBounds);
                textBounds.offset(offset, offset + textBounds.height());
                canvas.drawRect(textBounds, backGroundPaint);
                float textHeight = textBounds.height();
                canvas.drawText(text, offset, textHeight + offset, paint);

                containerView.invalidate();
            } else {
                renderTimerStart = -1;
            }
        }
    }
}
