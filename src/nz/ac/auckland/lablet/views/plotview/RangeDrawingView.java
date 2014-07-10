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
    final private RectF rangeRect = new RectF(0, 0, 100, 100);

    private int viewWidth;
    private int viewHeight;

    public RangeDrawingView(Context context) {
        super(context);

        setWillNotDraw(false);
    }

    public RangeDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
    }

    public RectF getRangeRect() {
        return rangeRect;
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    public float toScreenX(float real) {
        return (real - rangeRect.left) / (rangeRect.right - rangeRect.left) * viewWidth;
    }

    public float toScreenY(float real) {
        return (1.f - (real - rangeRect.bottom) / (rangeRect.top - rangeRect.bottom)) * viewHeight;
    }

    public float fromScreenX(float screen) {
        return rangeRect.left + screen * (rangeRect.right - rangeRect.left) / viewWidth;
    }

    public float fromScreenY(float screen) {
        return rangeRect.top - screen * (rangeRect.top - rangeRect.bottom) / viewHeight;
    }

    public Rect toScreen(RectF real) {
        Rect screen = new Rect();
        screen.left = Math.round(toScreenX(real.left));
        screen.top = Math.round(toScreenY(real.top));
        screen.right = Math.round(toScreenX(real.right));
        screen.bottom = Math.round(toScreenY(real.bottom));
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

    public Matrix getRangeMatrix() {
        Matrix matrix = new Matrix();
        matrix.setScale(getXScale(), getYScale());
        matrix.preTranslate(-rangeRect.left, -rangeRect.top);
        return matrix;
    }

    private float getXScale() {
        return (float)getWidth() / (rangeRect.right - rangeRect.left);
    }

    private float getYScale() {
        return (float)getHeight() / (rangeRect.bottom - rangeRect.top);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        applyRangeMatrix(canvas);
    }

    public void setRangeX(float left, float right) {
        rangeRect.left = left;
        rangeRect.right = right;
    }

    public void setRangeY(float bottom, float top) {
        rangeRect.top = top;
        rangeRect.bottom = bottom;
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
