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

    private RectF dirtyRect = null;

    private boolean bufferRangeChanged = false;

    private Paint offScreenPaint;

    protected RectF containerViewRangeToBufferRange(RectF range) {
        return range;
    }

    public void setOffScreenPaint(Paint paint) {
        this.offScreenPaint = paint;
    }

    protected RectF getDirtyRect() {
        return dirtyRect;
    }

    protected void clearDirtyRect() {
        dirtyRect = null;
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

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        bufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bufferCanvas = new Canvas(bufferBitmap);
        setBufferRealRect(containerViewRangeToBufferRange(containerView.getRange()));
        invalidate();
    }

    @Override
    public Matrix getRangeMatrixCopy() {
        float xScale = (float) bufferBitmap.getWidth() / (bufferRealRect.right - bufferRealRect.left);
        float yScale = (float) bufferBitmap.getHeight() / (bufferRealRect.bottom - bufferRealRect.top);

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
        if (!clean)
            bufferCanvas.drawBitmap(oldBitmap, null, bufferScreenRect, null);
        setBufferRealRect(containerViewRangeToBufferRange(containerView.getRange()));

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

    @Override
    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance) {
        updateBufferScreenRect();

        bufferRangeChanged = true;

        // if keepDistance is true there was no zoom and we may reuse some parts
        if (keepDistance) {
            RectF bufferRange = containerViewRangeToBufferRange(range);
            RectF bufferOldRange = containerViewRangeToBufferRange(oldRange);
            RectF newDirt = getDirtyRect(bufferRange, bufferOldRange, true);
            onNewDirtyRegions(newDirt);
        } else
            invalidate();

        containerView.invalidate();
    }

    private void setBufferRealRect(RectF rect) {
        bufferRealRect.set(rect);
        updateBufferScreenRect();
    }

    private void updateBufferScreenRect() {
        bufferScreenRect = containerView.toScreen(bufferRealRect);
    }
}
