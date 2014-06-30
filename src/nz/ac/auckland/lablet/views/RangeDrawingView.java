/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;


public class RangeDrawingView extends ViewGroup {
    private float rangeLeft = 0;
    private float rangeRight = 100;
    private float rangeTop = 0;
    private float rangeBottom = 100;

    public RangeDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    public void applyRangeMatrix(Canvas canvas) {
        float xScale = (float)getWidth() / (rangeRight - rangeLeft);
        float yScale = (float)getHeight() / (rangeBottom - rangeTop);
        canvas.scale(xScale, yScale);
        canvas.translate(-rangeLeft, -rangeTop);
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
}
