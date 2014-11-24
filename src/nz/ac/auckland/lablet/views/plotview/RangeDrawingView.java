/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.ViewGroup;


public class RangeDrawingView extends ViewGroup {
    public interface IRangeListener {
        void onRangeChanged(RectF range);
    }

    // Float.MAX_VALUE means unset
    final protected RectF rangeRect = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    private IRangeListener rangeListener;
    protected int viewWidth;
    protected int viewHeight;
    protected int paddedViewWidth;
    protected int paddedViewHeight;
    private float minXRange = -1;
    private float minYRange = -1;

    // Float.MAX_VALUE means there there is no end range (negative or positive)
    private RectF maxRange = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    public RangeDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
    }

    public RangeDrawingView(Context context) {
        super(context);

        setWillNotDraw(false);
    }

    public void setRangeListener(IRangeListener rangeListener) {
        this.rangeListener = rangeListener;
    }

    public RectF getRange() {
        return new RectF(rangeRect);
    }

    public RectF getScreenRect() {
        return toScreen(getRange());
    }

    public void setMaxXRange(float left, float right) {
        maxRange.left = left;
        maxRange.right = right;

        // reset range
        setXRange(getRangeLeft(), getRangeRight());
    }

    public void setMaxYRange(float bottom, float top) {
        maxRange.bottom = bottom;
        maxRange.top = top;

        // reset range
        setYRange(getRangeBottom(), getRangeTop());
    }

    final public void setMaxRange(RectF range) {
        setMaxXRange(range.left, range.right);
        setMaxYRange(range.bottom, range.top);
    }

    public void ensureRectInMaxRange(RectF realDataRect) {
        if (rangeRect.width() > 0) {
            if (realDataRect.left < maxRange.left)
                realDataRect.left = maxRange.left;
            if (realDataRect.right > maxRange.right)
                realDataRect.right = maxRange.right;
        } else {
            if (realDataRect.left > maxRange.left)
                realDataRect.left = maxRange.left;
            if (realDataRect.right < maxRange.right)
                realDataRect.right = maxRange.right;
        }
        if (rangeRect.height() < 0) {
            if (realDataRect.bottom < maxRange.bottom)
                realDataRect.bottom = maxRange.bottom;
            if (realDataRect.top > maxRange.top)
                realDataRect.top = maxRange.top;
        } else {
            if (realDataRect.bottom > maxRange.bottom)
                realDataRect.bottom = maxRange.bottom;
            if (realDataRect.top < maxRange.top)
                realDataRect.top = maxRange.top;
        }

    }
    public RectF getMaxRange() {
        return new RectF(maxRange);
    }

    public float getMinXRange() {
        return minXRange;
    }

    public void setMinXRange(float minXRange) {
        this.minXRange = minXRange;
    }

    public float getMinYRange() {
        return minYRange;
    }

    public void setMinYRange(float minYRange) {
        this.minYRange = minYRange;
    }

    public PointF getRangeMiddle() {
        return new PointF(rangeRect.left + rangeRect.width() / 2, rangeRect.top + rangeRect.height() / 2);
    }

    static class RangeF {
        public RangeF(float start, float end) {
            this.start = start;
            this.end = end;
        }

        public float start;
        public float end;
    }

    private void validateXRange(RangeF range) {
        if (maxRange.left != Float.MAX_VALUE) {
            if (range.end > range.start) {
                if (range.start < maxRange.left)
                    range.start = maxRange.left;
            } else {
                if (range.start > maxRange.left)
                    range.start = maxRange.left;
            }
        }
        if (maxRange.right != Float.MAX_VALUE) {
            if (range.end > range.start) {
                if (range.end > maxRange.right)
                    range.end = maxRange.right;
            } else {
                if (range.end < maxRange.right)
                    range.end = maxRange.right;
            }
        }

        float diff = Math.abs(range.end - range.start);
        if (minXRange > 0 && minXRange > diff) {
            final float middle = (range.end + range.start) / 2;
            if (range.end >= range.start) {
                range.end = middle + minXRange / 2;
                range.start = middle - minXRange / 2;
            } else {
                range.start = middle + minXRange / 2;
                range.end = middle - minXRange / 2;
            }
        }
    }

    private void validateYRange(RangeF range) {
        if (maxRange.bottom != Float.MAX_VALUE) {
            if (range.end > range.start) {
                if (range.start < maxRange.bottom)
                    range.start = maxRange.bottom;
            } else {
                if (range.start > maxRange.bottom)
                    range.start = maxRange.bottom;
            }
        }
        if (maxRange.top != Float.MAX_VALUE) {
            if (range.end > range.start) {
                if (range.end > maxRange.top)
                    range.end = maxRange.top;
            } else {
                if (range.end < maxRange.top)
                    range.end = maxRange.top;
            }
        }

        float diff = Math.abs(range.end - range.start);
        if (minYRange > 0 && minYRange > diff) {
            final float middle = (range.end + range.start) / 2;
            if (range.end >= range.start) {
                range.end = middle + minYRange / 2;
                range.start = middle - minYRange / 2;
            } else {
                range.start = middle + minYRange / 2;
                range.end = middle - minYRange / 2;
            }
        }
    }

    private boolean fuzzyEquals(float value1, float value2) {
        return Math.abs(value1 - value2) < 0.000001;
    }

    final public boolean setXRange(float left, float right) {
        return setXRange(left, right, false);
    }

    final public boolean setYRange(float bottom, float top) {
        return setYRange(bottom, top, false);
    }

    final public void setRange(RectF range) {
        if (setXRange(range.left, range.right) || setYRange(range.bottom, range.top))
            notifyRangeChanged();
    }

    private boolean setXRangeNoNotification(float left, float right, boolean keepDistance) {
        float oldLeft = getRangeLeft();
        float oldRight = getRangeRight();

        RangeF range = new RangeF(left, right);
        validateXRange(range);
        if (keepDistance && !fuzzyEquals(left - right, range.start - range.end)) {
            if (left != range.start)
                range.end = range.start + (right - left);
            else if (right != range.end)
                range.start = range.end - (right - left);
        }
        left = range.start;
        right = range.end;
        if (fuzzyEquals(left, oldLeft) && fuzzyEquals(right, oldRight))
            return false;

        rangeRect.left = left;
        rangeRect.right = right;
        return true;
    }

    public boolean setXRange(float left, float right, boolean keepDistance) {
        boolean result = setXRangeNoNotification(left, right, keepDistance);
        if (result)
            notifyRangeChanged();
        return result;
    }

    private boolean setYRangeNoNotification(float bottom, float top, boolean keepDistance) {
        float oldBottom = getRangeBottom();
        float oldTop = getRangeTop();

        RangeF range = new RangeF(bottom, top);
        validateYRange(range);
        if (keepDistance && !fuzzyEquals(bottom - top, range.start - range.end)) {
            if (bottom != range.start)
                range.end = range.start + (top - bottom);
            else if (top != range.end)
                range.start = range.end - (top - bottom);
        }
        bottom = range.start;
        top = range.end;
        if (fuzzyEquals(bottom, oldBottom) && fuzzyEquals(top, oldTop))
            return false;

        rangeRect.bottom = bottom;
        rangeRect.top = top;
        return true;
    }

    public boolean setYRange(float bottom, float top, boolean keepDistance) {
        boolean result = setYRangeNoNotification(bottom, top, keepDistance);
        if (result)
            notifyRangeChanged();
        return true;
    }

    private void notifyRangeChanged() {
        if (rangeListener != null)
            rangeListener.onRangeChanged(rangeRect);
    }

    public boolean offsetXRange(float offset) {
        return setXRange(getRangeLeft() + offset, getRangeRight() + offset, true);
    }

    public boolean offsetYRange(float offset) {
        return setYRange(getRangeBottom() + offset, getRangeTop() + offset, true);
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    public float toScreenX(float real) {
        return getPaddingLeft() + (real - rangeRect.left) / (rangeRect.right - rangeRect.left) * paddedViewWidth;
    }

    public float toScreenY(float real) {
        return getPaddingTop() + (1.f - (real - rangeRect.bottom) / (rangeRect.top - rangeRect.bottom)) * paddedViewHeight;
    }

    public void toScreen(PointF real, PointF screen) {
        screen.x = toScreenX(real.x);
        screen.y = toScreenY(real.y);
    }

    public float fromScreenX(float screen) {
        return rangeRect.left + (screen - getPaddingLeft()) * (rangeRect.right - rangeRect.left) / paddedViewWidth;
    }

    public float fromScreenY(float screen) {
        return rangeRect.top - (screen - getPaddingTop()) * (rangeRect.top - rangeRect.bottom) / paddedViewHeight;
    }

    public void fromScreen(PointF newPosition, PointF newReal) {
        newReal.x = fromScreenX(newPosition.x);
        newReal.y = fromScreenY(newPosition.y);
    }

    public RectF toScreen(RectF real) {
        RectF screen = new RectF();
        screen.left = toScreenX(real.left);
        screen.top = toScreenY(real.top);
        screen.right = toScreenX(real.right);
        screen.bottom = toScreenY(real.bottom);
        return screen;
    }

    public void applyRangeMatrix(Canvas canvas) {
        canvas.scale(getXScale(), getYScale());
        canvas.translate(-rangeRect.left, -rangeRect.top);
    }

    public void applyRangeMatrix(Path path) {
        Matrix matrix = new Matrix();
        matrix.setScale(getXScale(), getYScale());
        matrix.preTranslate(-rangeRect.left, -rangeRect.top);
        path.transform(matrix);
    }

    public Matrix getRangeMatrixCopy() {
        Matrix matrix = new Matrix();
        matrix.setScale(getXScale(), getYScale());
        matrix.preTranslate(-rangeRect.left, -rangeRect.top);
        return matrix;
    }

    private float getXScale() {
        return (float)paddedViewWidth / (rangeRect.right - rangeRect.left);
    }

    private float getYScale() {
        return (float)paddedViewHeight / (rangeRect.bottom - rangeRect.top);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
        viewHeight = h;
        paddedViewWidth = viewWidth - getPaddingRight() - getPaddingLeft();
        paddedViewHeight = viewHeight - getPaddingTop() - getPaddingBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        applyRangeMatrix(canvas);
    }

    public float getRangeLeft() {
        return rangeRect.left;
    }

    public float getRangeRight() {
        return rangeRect.right;
    }

    public float getRangeTop() {
        return rangeRect.top;
    }

    public float getRangeBottom() {
        return rangeRect.bottom;
    }
}
