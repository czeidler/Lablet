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
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.ViewGroup;

interface IYAxisView {
    /**
     * Gets the view position of the axis top.
     * @return axis top
     */
    public float getAxisTopOffset();
    /**
     * Gets the view position of the axis bottom measured from the view bottom.
     * @return axis bottom
     */
    public float getAxisBottomOffset();

    /**
     * Set the number of relevant digits.
     *
     * For example, a value of 3 and a top of 9999.3324 and a bottom of 9999.3325 would give results in a displayed
     * range of 9999.32400 to 9999.32500.
     *
     * @param digits
     */
    public void setRelevantLabelDigits(int digits);
    public void setDataRange(float top, float bottom);
}

public class YAxisView extends ViewGroup implements IYAxisView {
    private float axisTopOffset = 0;
    private float axisBottomOffset = 0;
    private float realTop = 0;
    private float realBottom = 0;
    private float optimalWidth = 0;
    private int relevantDigits = 3;

    private Paint labelPaint = new Paint();

    public YAxisView(Context context, AttributeSet attrs) {
        super(context, attrs);

        labelPaint.setColor(Color.BLACK);
        labelPaint.setStrokeWidth(1);
        labelPaint.setStyle(Paint.Style.STROKE);

        calculateAxisOffsets();
    }

    @Override
    public float getAxisTopOffset() {
        return axisTopOffset;
    }

    @Override
    public float getAxisBottomOffset() {
        return axisBottomOffset;
    }

    @Override
    public void setRelevantLabelDigits(int digits) {
        relevantDigits = digits;
    }

    @Override
    public void setDataRange(float top, float bottom) {
        realTop = top;
        realBottom = bottom;
    }

    private void calculateAxisOffsets() {
        float textHeight = labelPaint.descent() - labelPaint.ascent();
        axisTopOffset = textHeight / 2;
        axisBottomOffset = textHeight / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

    }

    @Override
    protected void onDraw(Canvas canvas) {

    }

    private int getTotalDigits() {
        
    }
}
