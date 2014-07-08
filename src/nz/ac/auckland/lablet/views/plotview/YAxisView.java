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
import android.graphics.Rect;
import android.view.ViewGroup;

import java.util.List;


class AxisSettings {
    final private float scaleWidth = 8;
    final private float spacing = 4;

    public float getSpacing() {
        return spacing;
    }

    public float getScaleExtent() {
        return scaleWidth;
    }

}

public class YAxisView extends ViewGroup implements IYAxis {
    private float axisTopOffset = 0;
    private float axisBottomOffset = 0;
    private float realTop = 10;
    private float realBottom = 0;
    private int relevantDigits = 3;

    private Paint labelPaint = new Paint();
    private float labelHeight = 5;
    private Paint axisPaint = new Paint();

    private AxisSettings settings = new AxisSettings();

    private String label = "";
    private String unit = "";
    private List<LabelPartitioner.LabelEntry> labels;

    public YAxisView(Context context) {
        super(context);

        setWillNotDraw(false);

        labelPaint.setColor(Color.WHITE);
        labelPaint.setStrokeWidth(1);
        labelPaint.setStyle(Paint.Style.STROKE);
        labelHeight = labelPaint.descent() - labelPaint.ascent();

        axisPaint.setColor(Color.WHITE);
        axisPaint.setStrokeWidth(2);
        axisPaint.setStyle(Paint.Style.STROKE);

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

        calculateLabels();
    }

    @Override
    public void setDataRange(float bottom, float top) {
        realTop = top;
        realBottom = bottom;

        calculateLabels();
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void setUnit(String unit) {
        this.unit = unit;
    }

    private void calculateLabels() {
        float axisLength = getAxisLength();
        if (axisLength <= 0)
            return;
        LabelPartitioner partitioner = new LabelPartitioner(labelHeight, axisLength, Math.min(realTop, realBottom),
                Math.max(realTop, realBottom));
        labels = partitioner.getLabels();
    }

    @Override
    public float optimalWidthForHeight(float height) {
        List<LabelPartitioner.LabelEntry> labelEntries = labels;
        if (height != getAxisLength()) {
            LabelPartitioner partitioner = new LabelPartitioner(labelHeight, height, Math.min(realTop, realBottom),
                    Math.max(realTop, realBottom));
            labelEntries = partitioner.getLabels();
        }

        float maxLabelWidth = 0;
        for (int i = 0; i < labelEntries.size(); i++) {
            float width = labelPaint.measureText(labelEntries.get(i).label);
            if (width > maxLabelWidth)
                maxLabelWidth = width;
        }

        // axis
        float optimalWidth = settings.getScaleExtent() + settings.getSpacing();
        // tick labels
        optimalWidth += maxLabelWidth + settings.getSpacing();
        // label and uni
        if (!label.equals("") || !unit.equals(""))
            optimalWidth += labelHeight + settings.getSpacing();

        return optimalWidth;
    }

    private void calculateAxisOffsets() {
        float textHeight = labelPaint.descent() - labelPaint.ascent();
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
        boolean hasLabel = !label.equals("");
        boolean hasUnit = !unit.equals("");
        if (hasLabel || hasUnit) {
            String completeLabel = label;
            if (hasLabel && hasUnit)
                completeLabel += " ";
            if (hasUnit)
                completeLabel += "[" + unit + "]";
            canvas.save();
            canvas.translate(labelHeight, ((float)(getHeight() + labelPaint.measureText(completeLabel))) / 2);
            canvas.rotate(-90);
            canvas.drawText(completeLabel, 0, 0, labelPaint);
            canvas.restore();
        }
        canvas.drawLine(getWidth() - 1, getAxisTopOffset(), getWidth() - 1, getHeight() - getAxisBottomOffset(),
                axisPaint);

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
        String labelText = labelEntry.label;
        Rect labelRect = new Rect();
        labelPaint.getTextBounds(labelText, 0, labelText.length(), labelRect);

        canvas.drawText(labelText, getWidth() - labelRect.width() - settings.getSpacing() - settings.getScaleExtent(),
                yPosition + labelRect.height() / 2, labelPaint);

        // draw tick
        canvas.drawLine(getWidth() - settings.getScaleExtent(), yPosition, getWidth(), yPosition, axisPaint);
    }

    private float getAxisLength() {
        return getHeight() - getAxisBottomOffset() - getAxisTopOffset();
    }
}
