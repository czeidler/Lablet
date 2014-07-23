/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


import android.graphics.*;

public class RangeInfoPainter implements IPlotPainter {
    final private PlotView plotView;
    private PlotPainterContainerView containerView;
    private Paint rangePaint = new Paint();
    private Paint rangeDisplayPaint = new Paint();

    private RectF relativeRangDisplay = new RectF();
    private Rect displayRect = new Rect();
    private Rect scaledRangeDisplayRect = new Rect();

    public RangeInfoPainter(PlotView plotView) {
        this.plotView = plotView;

        rangePaint.setColor(Color.GREEN);
        rangePaint.setStrokeWidth(2);
        rangePaint.setStyle(Paint.Style.STROKE);

        rangeDisplayPaint.setColor(Color.argb(100, 100, 100, 100));
        rangeDisplayPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void setContainer(PlotPainterContainerView view) {
        containerView = view;
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        recalculateRangeValue();
    }

    private Rect scaleRect(RectF relativeRect, int width, int height) {
        Rect rect = new Rect();
        rect.left = (int)(relativeRect.left * width);
        rect.right = (int)(relativeRect.right * width);
        rect.bottom = (int)(relativeRect.bottom * height);
        rect.top = (int)(relativeRect.top * height);
        return rect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (scaledRangeDisplayRect.contains(displayRect))
            return;

        canvas.drawRect(displayRect, rangeDisplayPaint);
        canvas.save();
        canvas.clipRect(displayRect.left, displayRect.top, displayRect.right, displayRect.bottom);
        canvas.drawRect(scaledRangeDisplayRect, rangePaint);
        canvas.restore();
    }

    private float getRelativePosition(float value, float start, float end) {
        return (value - start) / (end - start);
    }

    @Override
    public void setXScale(IScale xScale) {

    }

    @Override
    public void setYScale(IScale yScale) {

    }

    @Override
    public void onXRangeChanged(float left, float right, float oldLeft, float oldRight) {
        recalculateRangeValue();
    }

    @Override
    public void onYRangeChanged(float bottom, float top, float oldBottom, float oldTop) {
        recalculateRangeValue();
    }

    private void recalculateRangeValue() {
        RectF range = containerView.getRangeRect();
        RectF maxRange = plotView.getMaxRange();

        if (maxRange.left == Float.MAX_VALUE)
            relativeRangDisplay.left = -1;
        else
            relativeRangDisplay.left = getRelativePosition(range.left, maxRange.left, maxRange.right);
        if (maxRange.right == Float.MAX_VALUE)
            relativeRangDisplay.right = 2;
        else
            relativeRangDisplay.right = getRelativePosition(range.right, maxRange.left, maxRange.right);
        if (maxRange.top == Float.MAX_VALUE)
            relativeRangDisplay.top = -1;
        else
            relativeRangDisplay.top = (1 - getRelativePosition(range.top, maxRange.bottom, maxRange.top));
        if (maxRange.bottom == Float.MAX_VALUE)
            relativeRangDisplay.bottom = 2;
        else
            relativeRangDisplay.bottom = (1 - getRelativePosition(range.bottom, maxRange.bottom, maxRange.top));

        int screenWidth = containerView.getWidth();
        int screenHeight = containerView.getHeight();

        final int xOffset = (int)(screenWidth * 0.05);
        final int yOffset = (int)(screenWidth * 0.05);
        final int displayWidth = (int)(screenWidth * 0.2);
        final int displayHeight = (int)(screenHeight * 0.2);
        displayRect = new Rect(screenWidth - displayWidth - xOffset, screenHeight - displayHeight - yOffset,
                screenWidth - xOffset, screenHeight - yOffset);

        scaledRangeDisplayRect = scaleRect(relativeRangDisplay, displayWidth, displayHeight);
        scaledRangeDisplayRect.offset(displayRect.left, displayRect.top);
    }
}
