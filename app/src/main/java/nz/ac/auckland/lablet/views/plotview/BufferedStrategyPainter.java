/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.*;
import android.support.annotation.Nullable;

import java.lang.ref.SoftReference;


class BitmapBuffer {
    private SoftReference<Bitmap> bitmapSoftReference;

    public Bitmap getBuffer(Bitmap reference) {
        Bitmap bitmap = null;
        if (bitmapSoftReference != null)
            bitmap = bitmapSoftReference.get();

        if (bitmap == null || bitmap.getWidth() != reference.getWidth()
                || bitmap.getHeight() != reference.getHeight()) {
            bitmap = Bitmap.createBitmap(reference.getWidth(), reference.getHeight(),  reference.getConfig());
            setBuffer(bitmap);
        } else
            bitmap.eraseColor(Color.TRANSPARENT);

        return bitmap;
    }

    public Bitmap swap(Bitmap bitmap) {
        Bitmap buffer = getBuffer(bitmap);
        setBuffer(bitmap);
        return buffer;
    }

    private void setBuffer(Bitmap bitmap) {
        bitmapSoftReference = new SoftReference<>(bitmap);
    }
}

abstract public class BufferedStrategyPainter extends StrategyPainter {
    private Bitmap bufferBitmap;
    final private RectF bufferRealRect = new RectF();
    private RectF bufferScreenRect;
    private Canvas bufferCanvas;
    private BitmapBuffer bufferCache = new BitmapBuffer();
    private float enlargementFactor = 1f;

    private RectF dirtyRect = null;

    private boolean bufferRangeChanged = false;

    private Paint offScreenPaint;

    public void setOffScreenPaint(Paint paint) {
        this.offScreenPaint = paint;
    }

    protected RectF getDirtyRect() {
        return dirtyRect;
    }

    protected void clearDirtyRect() {
        dirtyRect = null;
    }

    public void setEnlargementFactor(float enlargementFactor) {
        this.enlargementFactor = enlargementFactor;
    }

    @Override
    protected void onNewDirtyRegions(@Nullable RectF newDirt) {
        if (newDirt != null) {
            if (dirtyRect == null)
                dirtyRect = newDirt;
            else
                addRect(dirtyRect, newDirt);
        }
    }

    protected RectF enlargeViewRangeToBufferScreenRange(RectF range) {
        int bufferScreenWidth = (int)(containerView.getWidth() * enlargementFactor) + 2;
        int bufferScreenHeight = (int)(containerView.getHeight() * enlargementFactor) + 2;

        RectF screenRange = containerView.toScreen(range);
        float xEnlargement = screenRange.width() * (enlargementFactor - 1f) / 2;
        float yEnlargement = screenRange.height() * (enlargementFactor - 1f) / 2;

        screenRange.left = (float) Math.floor(screenRange.left - xEnlargement);
        screenRange.top = (float) Math.floor(screenRange.top - yEnlargement);
        screenRange.right = screenRange.left + bufferScreenWidth;
        screenRange.bottom = screenRange.top + bufferScreenHeight;

        return screenRange;
    }

    protected RectF enlargeViewRangeToBufferRange(RectF range) {
        RectF screenRange = enlargeViewRangeToBufferScreenRange(range);

        return containerView.fromScreen(screenRange);
    }

    private void updateBufferRect() {
        bufferScreenRect = enlargeViewRangeToBufferScreenRange(containerView.getRange());
        bufferRealRect.set(containerView.fromScreen(bufferScreenRect));
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        updateBufferRect();

        bufferBitmap = Bitmap.createBitmap((int)bufferScreenRect.width(), (int)bufferScreenRect.height(),
                Bitmap.Config.ARGB_8888);

        bufferCanvas = new Canvas(bufferBitmap);
        bufferCanvas.translate(-bufferScreenRect.left, -bufferScreenRect.top);

        invalidate();
    }

    @Override
    public Matrix getRangeMatrixCopy() {
        float xScale = bufferScreenRect.width() / (bufferRealRect.right - bufferRealRect.left);
        float yScale = bufferScreenRect.height() / (bufferRealRect.bottom - bufferRealRect.top);

        Matrix matrix = new Matrix();
        matrix.setScale(xScale, yScale);
        matrix.preTranslate(-bufferRealRect.left, -bufferRealRect.top);
        return matrix;
    }

    abstract protected void onDirectDraw();

    protected Bitmap getBufferBitmap() {
        return bufferBitmap;
    }

    protected Canvas startEditingBufferBitmap(boolean clean) {
        if (bufferBitmap == null)
            return null;
        if (!bufferRangeChanged) {
            if (clean)
                bufferBitmap.eraseColor(Color.TRANSPARENT);
            return bufferCanvas;
        }

        Bitmap oldBitmap = bufferBitmap;
        bufferBitmap = bufferCache.swap(bufferBitmap);
        bufferCanvas = new Canvas(bufferBitmap);

        RectF oldBufferScreenRect = bufferScreenRect;
        updateBufferRect();

        bufferCanvas.translate(-bufferScreenRect.left, -bufferScreenRect.top);
        if (!clean)
            bufferCanvas.drawBitmap(oldBitmap, null, oldBufferScreenRect, null);

        bufferRangeChanged = false;

        return bufferCanvas;
    }

    protected RectF getBufferRealRect() {
        return bufferRealRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (bufferBitmap == null)
            return;

        onDirectDraw();

        canvas.drawBitmap(bufferBitmap, null, bufferScreenRect, offScreenPaint);
    }

    private RectF getDirtyRect(RectF rangeOrg, RectF oldRangeOrg, boolean keepDistance) {
        NormRectF dirt = new NormRectF(new RectF(rangeOrg));
        if (!keepDistance)
            return dirt.get();

        NormRectF range = new NormRectF(rangeOrg);
        NormRectF oldRange = new NormRectF(oldRangeOrg);
        if (range.getTop() == oldRange.getTop() && range.getBottom() == oldRange.getBottom()) {
            if (oldRange.getLeft() > range.getLeft() && oldRange.getLeft() < range.getRight())
                dirt.setRight(oldRange.getLeft());
            else if (oldRange.getRight() > range.getLeft() && oldRange.getRight() < range.getRight())
                dirt.setLeft(oldRange.getRight());
        }

        if (range.getLeft() == oldRange.getLeft() && range.getRight() == oldRange.getRight()) {
            if (oldRange.getTop() > range.getTop() && oldRange.getTop() < range.getBottom())
                dirt.setBottom(oldRange.getTop());
            else if (oldRange.getBottom() > range.getTop() && oldRange.getBottom() < range.getBottom())
                dirt.setTop(oldRange.getBottom());
        }

        return dirt.get();
    }

    protected RectF addRect(RectF source, RectF toAdd) {
        NormRectF sourceN = new NormRectF(source);
        NormRectF toAddN = new NormRectF(toAdd);

        if (sourceN.getLeft() > toAddN.getLeft())
            sourceN.setLeft(toAddN.getLeft());
        if (sourceN.getTop() > toAddN.getTop())
            sourceN.setTop(toAddN.getTop());
        if (sourceN.getRight() < toAddN.getRight())
            sourceN.setRight(toAddN.getRight());
        if (sourceN.getBottom() < toAddN.getBottom())
            sourceN.setBottom(toAddN.getBottom());

        return sourceN.get();
    }

    // Its very difficult to stitch the old bitmap together with the new drawing (the drawing is a bit off at times).
    private boolean forceInvalidateOnRangeChanged = true;

    public void setForceInvalidateOnRangeChanged(boolean forceInvalidateOnRangeChanged) {
        this.forceInvalidateOnRangeChanged = forceInvalidateOnRangeChanged;
    }

    @Override
    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance) {
        bufferScreenRect = containerView.toScreen(bufferRealRect);

        bufferRangeChanged = true;

        // if keepDistance is true there was no zoom and we may reuse some parts
        if (keepDistance && !forceInvalidateOnRangeChanged) {
            RectF bufferRange = enlargeViewRangeToBufferRange(range);
            RectF bufferOldRange = enlargeViewRangeToBufferRange(oldRange);
            if (!bufferRange.equals(bufferOldRange)) {
                RectF newDirt = getDirtyRect(bufferRange, bufferOldRange, true);
                onNewDirtyRegions(newDirt);
            }
        } else
            invalidate();

        containerView.invalidate();
    }
}
