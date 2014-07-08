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
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.List;


public class XAxisView extends ViewGroup implements IXAxis {
    private float axisLeftOffset = 0;
    private float axisRightOffset = 0;
    private float realLeft = 0;
    private float realRight = 10;

    private Paint labelPaint = new Paint();
    private float labelHeight = 5;
    private float labelDescent = 2;
    private Paint axisPaint = new Paint();

    private AxisSettings settings = new AxisSettings();

    private String label = "";
    private String unit = "";
    private List<LabelPartitioner.LabelEntry> labels;

    public XAxisView(Context context) {
        super(context);

        setWillNotDraw(false);

        labelPaint.setColor(Color.WHITE);
        labelPaint.setStrokeWidth(1);
        labelPaint.setStyle(Paint.Style.STROKE);
        labelDescent = labelPaint.descent();
        labelHeight = labelDescent - labelPaint.ascent();

        axisPaint.setColor(Color.WHITE);
        axisPaint.setStrokeWidth(2);
        axisPaint.setStyle(Paint.Style.STROKE);

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
    public void setDataRange(float left, float right) {
        realLeft = left;
        realRight = right;

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
        // TODO: calculate the max label width and use it instead of labelHeight!
        LabelPartitioner partitioner = new LabelPartitioner(labelHeight, axisLength, Math.min(realLeft, realRight),
                Math.max(realLeft, realRight));
        labels = partitioner.getLabels();
    }

    @Override
    public float optimalHeight() {
        // axis
        float optimalWidth = settings.getScaleExtent() + labelHeight;
        // label and uni
        if (!label.equals("") || !unit.equals(""))
            optimalWidth += labelHeight;

        return optimalWidth;
    }

    private void calculateAxisOffsets() {
        axisLeftOffset = 0;
        axisRightOffset = 0;
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

            canvas.drawText(completeLabel, getWidth() / 2 - labelPaint.measureText(completeLabel) / 2,
                    getHeight() - labelDescent, labelPaint);
        }
        canvas.drawLine(getAxisLeftOffset(), 1, getWidth() - getAxisRightOffset(), 1, axisPaint);

        for (int i = 0; i < labels.size(); i++) {
            LabelPartitioner.LabelEntry entry = labels.get(i);
            float position = 0;
            if (realRight < realLeft)
                position = getAxisRightOffset() - entry.relativePosition * getAxisLength();
            else
                position = getAxisLeftOffset() + entry.relativePosition * getAxisLength();

            // fix first and last label position
            if (i == 0)
                drawLabel(canvas, entry, position, true, false);
            else if (i == labels.size() - 1)
                drawLabel(canvas, entry, position, false, true);
            else
                drawLabel(canvas, entry, position, false, false);
        }
    }

    private void drawLabel(Canvas canvas, LabelPartitioner.LabelEntry labelEntry, float xPosition, boolean isFirst,
                           boolean isLast) {
        String labelText = labelEntry.label;
        Rect labelRect = new Rect();
        labelPaint.getTextBounds(labelText, 0, labelText.length(), labelRect);

        float labelPosition = xPosition;
        if (isFirst)
            labelPosition = xPosition;
        else if (isLast)
            labelPosition -= labelRect.width();
        else
            labelPosition -= labelRect.width() / 2;

        canvas.drawText(labelText, labelPosition, settings.getScaleExtent() + labelHeight - labelDescent, labelPaint);

        // draw tick
        canvas.drawLine(xPosition, 0, xPosition, settings.getScaleExtent(), axisPaint);
    }

    private float getAxisLength() {
        return getWidth() - getAxisLeftOffset() - getAxisRightOffset();
    }
}