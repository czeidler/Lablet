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
import android.graphics.Rect;

import java.util.List;


public class XAxisView extends AbstractXAxis {
    private float axisLeftOffset = 0;
    private float axisRightOffset = 0;


    private AxisSettings settings = new AxisSettings();

    public XAxisView(Context context) {
        super(context);

        calculateAxisOffsets();
    }

    @Override
    public float getAxisLeftOffset() {
        return axisLeftOffset;
    }

    @Override
    public float getAxisRightOffset() {
        return axisRightOffset;
    }

    @Override
    protected List<LabelPartitioner.LabelEntry> calculateLabels() {
        determineUsedPrefix(realLeft, realRight);
        float usedLeft = realLeft;
        float usedRight = realRight;
        if (usedPrefix != null) {
            usedLeft = unit.transformToPrefix(realLeft, usedPrefix);
            usedRight = unit.transformToPrefix(realRight, usedPrefix);
        }
        float axisLength = getAxisLength();
        if (axisLength <= 0)
            return null;
        float maxLabelWidth = titlePaint.measureText(LabelPartitionerHelper.createDummyLabel(
                LabelPartitionerHelper.estimateLabelMetric(usedLeft, usedRight)));
        return labelPartitioner.calculate(maxLabelWidth, axisLength, Math.min(usedLeft, usedRight),
                Math.max(usedLeft, usedRight));
    }

    @Override
    public float optimalHeight() {
        final float titleHeight = titlePaint.descent() - titlePaint.ascent();
        // axis
        float optimalHeight = settings.getFullTickSize() + titleHeight;
        // title and uni
        if (!title.equals("") || !unit.getTotalUnit().equals(""))
            optimalHeight += titleHeight;

        return optimalHeight;
    }

    private void calculateAxisOffsets() {
        axisLeftOffset = 0;
        axisRightOffset = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // first get the labels and the usedPrefix
        List<LabelPartitioner.LabelEntry> labels = getLabels();

        boolean hasTitle = !title.equals("");
        boolean hasUnit = !unit.getTotalUnit(usedPrefix).equals("");
        if (hasTitle || hasUnit) {
            String completeLabel = title;
            if (hasTitle && hasUnit)
                completeLabel += " ";
            if (hasUnit)
                completeLabel += "[" + unit.getTotalUnit(usedPrefix) + "]";

            canvas.drawText(completeLabel, getWidth() / 2 - titlePaint.measureText(completeLabel) / 2,
                    getHeight() - titlePaint.descent(), titlePaint);
        }
        canvas.drawLine(getAxisLeftOffset(), 1, getWidth() - getAxisRightOffset(), 1, axisPaint);

        if (labels == null)
            return;
        for (LabelPartitioner.LabelEntry entry : labels) {
            float position;
            if (realRight < realLeft)
                position = getAxisRightOffset() - entry.relativePosition * getAxisLength();
            else
                position = getAxisLeftOffset() + entry.relativePosition * getAxisLength();

            drawLabel(canvas, entry, position);
        }
    }

    private void drawLabel(Canvas canvas, LabelPartitioner.LabelEntry labelEntry, float xPosition) {
        if (xPosition < getAxisLeftOffset() || xPosition > getWidth() - getAxisRightOffset())
            return;

        String labelText = labelEntry.label;
        Rect labelRect = new Rect();
        titlePaint.getTextBounds(labelText, 0, labelText.length(), labelRect);

        float labelPosition = xPosition - labelRect.width() / 2;
        if (labelPosition > getWidth() - labelRect.width())
            labelPosition = getWidth() - labelRect.width();
        else if (labelPosition < 0)
            labelPosition = 0;

        canvas.drawText(labelText, labelPosition, settings.getFullTickSize() - titlePaint.ascent(), titlePaint);

        // draw tick
        float tickLength = settings.getFullTickSize();
        if (!labelEntry.isFullTick)
            tickLength = settings.getShortTickSize();
        canvas.drawLine(xPosition, 0f, xPosition, tickLength, axisPaint);
    }

    private float getAxisLength() {
        return getWidth() - getAxisLeftOffset() - getAxisRightOffset();
    }
}