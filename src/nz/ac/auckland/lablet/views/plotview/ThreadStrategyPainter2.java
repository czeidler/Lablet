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


class PainterRenderTask {
    final private ThreadStrategyPainter2 plotPainter;

    final private AtomicBoolean running = new AtomicBoolean();
    private Future thread;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    final private Handler uiHandler = new Handler();
    private ThreadStrategyPainter2.ThreadCookie cookie;

    public PainterRenderTask(ThreadStrategyPainter2 plotPainter) {
        this.plotPainter = plotPainter;
        running.set(false);
    }

    Runnable renderRunnable = new Runnable() {
        @Override
        public void run() {
            List<StrategyPainter.RenderPayload> payloadList = cookie.payloads;
            int size = payloadList.size();
            if (size == 0) {
                running.set(false);
                return;
            }

            Bitmap bitmap = cookie.bitmap;
            Canvas bitmapCanvas = new Canvas(bitmap);
            for (int index = 0; payloadList != null && index < payloadList.size(); index++) {
                StrategyPainter.RenderPayload payload = payloadList.get(index);
                payload.getPainter().render(bitmapCanvas, payload);
            }

            publishBitmap(cookie);
        }
    };

    private void publishBitmap(final ThreadStrategyPainter2.ThreadCookie cookie) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                plotPainter.onMergeOffScreenRendering(cookie);

                running.set(false);

                plotPainter.onRenderingFinished();
            }
        });
    }

    public boolean start(ThreadStrategyPainter2.ThreadCookie threadCookie) {
        if (running.get())
            return false;
        this.cookie = threadCookie;
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

public class ThreadStrategyPainter2 extends BufferedStrategyPainter {
    final private PainterRenderTask renderTask = new PainterRenderTask(this);
    final private BitmapBuffer threadBitmap = new BitmapBuffer();
    private boolean invalidated = false;

    public class ThreadCookie {
        final public List<RenderPayload> payloads;
        final public Bitmap bitmap;
        final public RectF bitmapRealRange;
        final public boolean isCompleteRedraw;

        public ThreadCookie(List<RenderPayload> payloads, Bitmap bitmap, RectF bitmapRealRange,
                            boolean isCompleteRedraw) {
            this.payloads = payloads;
            this.bitmap = bitmap;
            this.bitmapRealRange = bitmapRealRange;
            this.isCompleteRedraw = isCompleteRedraw;
        }
    }

    @Override
    protected void onNewDirtyRegions() {
        if (invalidated) {
            triggerOffScreenRendering(true);
            invalidated = false;
        } else
            triggerOffScreenRendering(false);
    }

    @Override
    public void invalidate() {
        invalidated = true;
        triggerOffScreenRendering(true);
    }

    private boolean unprocessedCompleteRedraw = false;

    @Override
    public boolean hasThreads() {
        return true;
    }

    @Override
    protected void onDirectDraw() {

    }

    private boolean isRendering() {
        return renderTask.isRendering();
    }

    @Override
    public boolean hasFreeRenderingPipe() {
        return !renderTask.isRendering();
    }


    private void triggerOffScreenRendering(boolean completeRedraw) {
        if (containerView == null || getBufferBitmap() == null)
            return;
        if (isRendering()) {
            unprocessedCompleteRedraw = completeRedraw;
            return;
        }

        invalidated = false;

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

        Bitmap renderBitmap = threadBitmap.getBuffer(getBufferBitmap());
        renderBitmap.eraseColor(Color.TRANSPARENT);
        renderTask.start(new ThreadCookie(dirt, renderBitmap, new RectF(getBufferRealRect()), completeRedraw));
    }

    protected void onMergeOffScreenRendering(ThreadCookie cookie) {
        Canvas canvas = startEditingBufferBitmap(cookie.isCompleteRedraw);

        Bitmap resultBitmap = cookie.bitmap;
        if (resultBitmap != null) {
            RectF targetRect = containerView.toScreen(cookie.bitmapRealRange);
            RectF offScreenRect = containerView.toScreen(getBufferRealRect());
            targetRect.offset(-offScreenRect.left, -offScreenRect.top);
            canvas.drawBitmap(resultBitmap, null, targetRect, null);
        }
    }

    protected void onRenderingFinished() {
        containerView.invalidate();

        if (!hasFreeRenderingPipe())
            return;

        onNewDirtyRegions();
    }

    @Override
    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance) {
        super.onRangeChanged(range, oldRange, keepDistance);

        invalidate();
    }
}
