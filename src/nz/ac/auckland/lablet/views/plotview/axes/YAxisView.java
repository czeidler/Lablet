/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview.axes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import nz.ac.auckland.lablet.views.plotview.PlotView;

import java.util.List;


public class YAxisView extends AbstractYAxis {
    private float axisTopOffset = 0;
    private float axisBottomOffset = 0;

    private AxisSettings settings = new AxisSettings();

    public YAxisView(Context context) {
        super(context);

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
    protected void calculateLabels() {
        final float titleHeight = titlePaint.descent() - titlePaint.ascent();
        final float axisLength = getAxisLength();
        if (axisLength <= 0)
            return;
        labels = labelPartitioner.calculate(titleHeight, axisLength, Math.min(realTop, realBottom),
                Math.max(realTop, realBottom));
    }

    @Override
    public float optimalWidthForHeight(float height) {
        final float labelHeight = titlePaint.descent() - titlePaint.ascent();

        List<LabelPartitioner.LabelEntry> labelEntries = labels;
        if (height != getAxisLength()) {
            labelEntries = labelPartitioner.calculate(labelHeight, height, Math.min(realTop, realBottom),
                    Math.max(realTop, realBottom));
        }

        float maxLabelWidth = 0;
        for (int i = 0; i < labelEntries.size(); i++) {
            float width = titlePaint.measureText(labelEntries.get(i).label);
            if (width > maxLabelWidth)
                maxLabelWidth = width;
        }

        // axis
        float optimalWidth = settings.getFullTickSize() + settings.getSpacing();
        // tick labels
        optimalWidth += maxLabelWidth + settings.getSpacing();
        // title and uni
        if (!title.equals("") || !unit.equals(""))
            optimalWidth += labelHeight + settings.getSpacing();

        return optimalWidth;
    }

    private void calculateAxisOffsets() {
        float textHeight = titlePaint.descent() - titlePaint.ascent();
        axisTopOffset = textHeight / 2;
        axisBottomOffset = textHeight / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed)
            calculateLabels();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float titleHeight = titlePaint.descent() - titlePaint.ascent();

        boolean hasLabel = !title.equals("");
        boolean hasUnit = !unit.equals("");
        if (hasLabel || hasUnit) {
            String completeLabel = title;
            if (hasLabel && hasUnit)
                completeLabel += " ";
            if (hasUnit)
                completeLabel += "[" + unit + "]";
            canvas.save();
            canvas.translate(titleHeight, ((float)(getHeight() + titlePaint.measureText(completeLabel))) / 2);
            canvas.rotate(-90);
            canvas.drawText(completeLabel, 0, 0, titlePaint);
            canvas.restore();
        }
        canvas.drawLine(getWidth() - 1, getAxisTopOffset(), getWidth() - 1, getHeight() - getAxisBottomOffset(),
                axisPaint);

        if (labels == null)
            return;
        for (int i = 0; i < labels.size(); i++) {
            LabelPartitioner.LabelEntry entry = labels.get(i);
            float position = 0;
            if (realTop < realBottom)
                position = getAxisTopOffset() + entry.relativePosition * getAxisLength();
            else
                position = getHeight() - getAxisBottomOffset() - entry.relativePosition * getAxisLength();

            drawLabel(canvas, entry, position);
        }
    }

    private void drawLabel(Canvas canvas, LabelPartitioner.LabelEntry labelEntry, float yPosition) {
        if (yPosition < getAxisTopOffset() || yPosition > getHeight() - getAxisBottomOffset())
            return;

        String labelText = labelEntry.label;
        Rect labelRect = new Rect();
        titlePaint.getTextBounds(labelText, 0, labelText.length(), labelRect);

        canvas.drawText(labelText, getWidth() - labelRect.width() - settings.getSpacing() - settings.getFullTickSize(),
                yPosition + labelRect.height() / 2, titlePaint);

        // draw tick
        float tickLength = settings.getFullTickSize();
        if (!labelEntry.isFullTick)
            tickLength = settings.getShortTickSize();
        canvas.drawLine(getWidth() - tickLength, yPosition, getWidth(), yPosition, axisPaint);
    }
}
