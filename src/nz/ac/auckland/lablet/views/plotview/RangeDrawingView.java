/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.ViewGroup;


public class RangeDrawingView extends ViewGroup {
    private float rangeLeft = 0;
    private float rangeRight = 100;
    private float rangeTop = 0;
    private float rangeBottom = 100;
    private int width;
    private int height;

    public RangeDrawingView(Context context) {
        super(context);

        setWillNotDraw(false);
    }

    public RangeDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    public float toScreenX(float real) {
        return (real - rangeLeft) / (rangeRight - rangeLeft) * width;
    }

    public float toScreenY(float real) {
        return (1.f - (real - rangeBottom) / (rangeTop - rangeBottom)) * height ;
    }

    public float fromScreenX(float screen) {
        return rangeLeft + screen * (rangeRight - rangeLeft) / width;
    }

    public float fromScreenY(float screen) {
        return rangeTop - screen * (rangeTop - rangeBottom) / height;
    }

    public void applyRangeMatrix(Canvas canvas) {
        canvas.scale(getXScale(), getYScale());
        canvas.translate(-rangeLeft, -rangeTop);
    }

    public void applyRangeMatrix(Path path) {
        Matrix matrix = new Matrix();
        matrix.setScale(getXScale(), getYScale());
        matrix.preTranslate(-rangeLeft, -rangeTop);
        path.transform(matrix);
    }

    private float getXScale() {
        return (float)getWidth() / (rangeRight - rangeLeft);
    }

    private float getYScale() {
        return (float)getHeight() / (rangeBottom - rangeTop);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        applyRangeMatrix(canvas);
    }

    public void setRangeX(float left, float right) {
        rangeLeft = left;
        rangeRight = right;
    }

    public void setRangeY(float bottom, float top) {
        rangeTop = top;
        rangeBottom = bottom;
    }

    public float getRangeLeft() {
        return rangeLeft;
    }

    public float getRangeRight() {
        return rangeRight;
    }

    public float getRangeTop() {
        return rangeTop;
    }

    public float getRangeBottom() {
        return rangeBottom;
    }
}
