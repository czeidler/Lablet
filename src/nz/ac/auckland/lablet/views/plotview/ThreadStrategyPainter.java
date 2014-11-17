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
import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


class ThreadRenderTask {
    final private ThreadStrategyPainter plotPainter;

    final private AtomicBoolean running = new AtomicBoolean();
    private Future thread;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    final private Handler uiHandler = new Handler();
    private List<StrategyPainter.RenderPayload> payloadList;

    public ThreadRenderTask(ThreadStrategyPainter plotPainter) {
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
                StrategyPainter.RenderPayload payload = payloadList.get(index);

                Rect screenRect = payload.getScreenRect();
                Bitmap bitmap = null;
                if (screenRect.width() > 0 && screenRect.height() > 0) {
                    // create bitmap for drawing
                    bitmap = Bitmap.createBitmap(screenRect.width(), screenRect.height(), Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.TRANSPARENT);

                    Canvas bitmapCanvas = new Canvas(bitmap);
                    // move the canvas over the bitmap
                    bitmapCanvas.translate(-screenRect.left, -screenRect.top);

                    payload.getPainter().render(bitmapCanvas, payload);
                }

                boolean done = false;
                if (index == size - 1) {
                    payloadList = null;
                    done = true;
                }
                publishBitmap(payload, bitmap, done);
            }
        }
    };

    private void publishBitmap(final StrategyPainter.RenderPayload payload, final Bitmap resultBitmap,
                               final boolean done) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                plotPainter.onMergeOffScreenRendering(payload, resultBitmap);

                if (done) {
                    running.set(false);
                    plotPainter.onOffScreenRenderingFinished();
                }
            }
        });
    }

    public boolean start(List<StrategyPainter.RenderPayload> payloadList) {
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


public class ThreadStrategyPainter extends StrategyPainter {
    private Map<ConcurrentPainter, OffScreenBitmap> bitmapMap = new ArrayMap<>();

    final private ThreadRenderTask renderTask = new ThreadRenderTask(this);
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

    protected void onSetupOffScreenBitmap() {
        for (OffScreenBitmap offScreenBitmap : bitmapMap.values()) {
            Bitmap currentBitmap = offScreenBitmap.getBitmap();
            if (currentBitmap == null || currentBitmap.getWidth() != containerView.getWidth()
                    || currentBitmap.getHeight() != containerView.getHeight()) {
                int width = containerView.getWidth();
                if (width <= 0)
                    width = 100;
                int height = containerView.getHeight();
                if (height <= 0)
                    height = 100;
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.TRANSPARENT);
                RectF bitmapRealRect = containerView.getRange();
                offScreenBitmap.setTo(bitmap, bitmapRealRect);
            } else
                offScreenBitmap.setRealRect(containerView.getRange());
        }
    }

    protected void onMergeOffScreenRendering(RenderPayload payload, Bitmap resultBitmap) {
        OffScreenBitmap offScreenBitmap = bitmapMap.get(payload.getPainter());
        if (payload.isCompleteRedraw()) {
            onSetupOffScreenBitmap();
            offScreenBitmap.getBitmap().eraseColor(Color.TRANSPARENT);
        }

        if (resultBitmap != null) {
            Rect targetRect = containerView.toScreen(payload.getRealDataRect());
            Rect offScreenRect = containerView.toScreen(offScreenBitmap.getRealRect());
            targetRect.offset(-offScreenRect.left, -offScreenRect.top);
            Canvas canvas = offScreenBitmap.getCanvas();
            canvas.drawBitmap(resultBitmap, null, targetRect, null);
        }

    }

    protected void onOffScreenRenderingFinished() {
        if (!renderTask.isRendering() && payloadQueue.size() > 0) {
            renderTask.start(payloadQueue);
            payloadQueue = new ArrayList<>();
        }
        if (!hasFreeRenderingPipe())
            return;

        containerView.invalidate();
        onNewDirtyRegions();
    }

    @Override
    public void addChild(ConcurrentPainter painter) {
        super.addChild(painter);
        bitmapMap.put(painter, new OffScreenBitmap());
    }

    @Override
    public void removedChild(ConcurrentPainter painter) {
        super.removedChild(painter);
        bitmapMap.remove(painter);
    }

    @Override
    public boolean hasThreads() {
        return true;
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        if (width <= 0 || height <= 0)
            return;

        onSetupOffScreenBitmap();
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (ConcurrentPainter painter : childPainters) {
            OffScreenBitmap offScreenBitmap = bitmapMap.get(painter);

            Bitmap bitmap = offScreenBitmap.getBitmap();
            if (bitmap != null) {
                Rect bitmapScreenRect = containerView.toScreen(offScreenBitmap.getRealRect());
                canvas.drawBitmap(bitmap, null, bitmapScreenRect, offScreenPaint);
            }

            isRenderingDrawer.onDraw(canvas);
        }
    }

    public void setOffScreenPaint(Paint paint) {
        this.offScreenPaint = paint;
    }

    private boolean isRendering() {
        return renderTask.isRendering();
    }

    @Override
    protected void onNewDirtyRegions() {
        triggerOffScreenRendering(false);
    }

    @Override
    public boolean hasFreeRenderingPipe() {
        return !renderTask.isRendering();
    }

    private void renderQueue() {
        if (payloadQueue.size() == 0)
            return;

        List<RenderPayload> payloadsToRender = payloadQueue;
        payloadQueue = new ArrayList<>();
        renderTask.start(payloadsToRender);
    }

    private boolean unprocessedCompleteRedraw = false;
    private void triggerOffScreenRendering(boolean completeRedraw) {
        if (containerView == null)
            return;
        if (isRendering()) {
            unprocessedCompleteRedraw = completeRedraw;
            return;
        }
        RectF range = null;
        if (unprocessedCompleteRedraw || completeRedraw) {
            completeRedraw = true;
            range = getContainerView().getRange();
            unprocessedCompleteRedraw = false;
        }

        List<RenderPayload> dirt = new ArrayList<>();
        for (ConcurrentPainter painter : childPainters) {
            List<RenderPayload> subList = painter.collectRenderPayloads(true, range);
            if (completeRedraw) {
                for (RenderPayload payload : subList)
                    payload.setCompleteRedraw(true);
            }
            dirt.addAll(subList);
        }

        for (RenderPayload payload : dirt) {
            OffScreenBitmap offScreenBitmap = bitmapMap.get(payload.getPainter());
            if (offScreenBitmap.getCanvas() != null)
                payloadQueue.add(payload);
        }

        renderQueue();
    }

    @Override
    public void invalidate() {
        triggerOffScreenRendering(true);
    }

    @Override
    public void onXRangeChanged(float left, float right, float oldLeft, float oldRight) {
        super.onXRangeChanged(left, right, oldLeft, oldRight);

        invalidate();
    }

    @Override
    public void onYRangeChanged(float bottom, float top, float oldBottom, float oldTop) {
        super.onYRangeChanged(bottom, top, oldBottom, oldTop);

        invalidate();
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
