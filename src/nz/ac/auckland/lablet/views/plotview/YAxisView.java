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

import java.util.ArrayList;
import java.util.List;


class LabelMetric {
    public int digits = 0;
    public int decimalPlaceDigits = 0;
}

class LabelPartitioner {
    float labelExtent;
    float axisExtent;
    float realStart;
    float realEnd;

    boolean finishingLabelsMatchEnds = true;

    class LabelEntry {
        String label;
        float realValue;
        float relativePosition;
    }
    List<LabelEntry> labels = new ArrayList<>();

    /**
     *
     * @param labelExtent
     * @param axisExtent
     * @param realStart must be smaller than realEnd
     * @param realEnd
     */
    public LabelPartitioner(float labelExtent, float axisExtent, float realStart, float realEnd) {
        this.labelExtent = labelExtent;
        this.axisExtent = axisExtent;
        this.realStart = realStart;
        this.realEnd = realEnd;

        calculate();
    }

    private float toScreen(float realValue) {
        return axisExtent * realValue / Math.abs(realEnd - realStart);
    }

    static public String createDummyLabel(LabelMetric metric) {
        float dummyValue = 1.f / 3 * 10 * metric.digits;
        return createLabel(dummyValue, metric);
    }

    static public LabelMetric estimateLabelMetric(float min, float max) {
        return calculateLabelMetric(0.1f, min, max);
    }

    static private LabelMetric calculateLabelMetric(float stepFactor, float realStart, float realEnd) {
        float maxValue = Math.max(Math.abs(realEnd), Math.abs(realStart));
        float maxOrder = getOrderValue(maxValue);
        float diffOrder = getOrderValue(Math.abs(realEnd - realStart)) * getOrderValue(stepFactor);

        LabelMetric labelMetric = new LabelMetric();

        int digits = (int)Math.log10(maxOrder);
        if (digits >= 0)
            digits += 1;
        if (digits < 0)
            labelMetric.decimalPlaceDigits = -digits;
        else
            labelMetric.digits = digits;

        int diffDigits = (int)Math.round(Math.log10(diffOrder));
        if (diffDigits >= 0)
            diffDigits += 1;
        if (diffDigits < 0 && diffDigits < digits)
            labelMetric.decimalPlaceDigits = -diffDigits;

        return labelMetric;
    }

    private void fillLabelList(int bestFoundLabelNumber, float stepSize, LabelMetric labelMetric) {
        float diff = Math.abs(realEnd - realStart);
        labels.clear();
        float realLabelStart = (int)(Math.min(realStart, realEnd) / stepSize) * stepSize;
        for (int i = 0; i < bestFoundLabelNumber; i++) {
            LabelEntry entry = new LabelEntry();
            float realValue = realLabelStart + i * stepSize;
            entry.realValue = realValue;
            entry.relativePosition = i * stepSize / diff;
            entry.label = createLabel(realValue, labelMetric);

            labels.add(entry);
        }
    }

    private void updateFinishingLabels(float minSpacing, LabelMetric labelMetric) {
        // fill finishing labels
        if (finishingLabelsMatchEnds && labels.size() >= 2) {
            LabelEntry entryEnd = labels.get(labels.size() - 1);
            if (Math.abs(toScreen(entryEnd.realValue) - toScreen(realEnd)) > minSpacing) {
                entryEnd = new LabelEntry();
                labels.add(entryEnd);
            }
            entryEnd.realValue = realEnd;
            entryEnd.relativePosition = 1;
            entryEnd.label = createLabel(realEnd, labelMetric);

            LabelEntry entryStart = labels.get(0);
            if (Math.abs(toScreen(entryStart.realValue) - toScreen(realStart)) > minSpacing) {
                entryStart = new LabelEntry();
                labels.add(0, entryStart);
            }
            entryStart.realValue = realStart;
            entryStart.relativePosition = 0;
            entryStart.label = createLabel(realStart, labelMetric);
        }
    }

    private void calculate() {
        final float minSpacing = 20;
        final float optimalSpacing = axisExtent * 0.15f;
        int maxLabelNumber = (int)(axisExtent / (labelExtent + minSpacing)) + 1;
        int optimalLabelNumber = (int)(axisExtent / (labelExtent + optimalSpacing)) + 1;

        float diff = Math.abs(realEnd - realStart);
        float diffOrderValue = getOrderValue(diff);

        int bestFoundLabelNumber = 1;
        float stepSize = 1f;
        float stepFactor = 1f;
        for (int i = 0; bestFoundLabelNumber < maxLabelNumber && bestFoundLabelNumber < optimalLabelNumber; i++) {
            stepFactor = getStepFactor(i);
            stepSize = stepFactor * diffOrderValue;
            if (stepSize >= diff)
                continue;

            int labelNumber = (int)(diff / stepSize) + 1;
            if (optimalLabelNumber - labelNumber < optimalLabelNumber - bestFoundLabelNumber)
                bestFoundLabelNumber = labelNumber;
        }

        LabelMetric labelMetric = calculateLabelMetric(stepFactor, realStart, realEnd);

        fillLabelList(bestFoundLabelNumber, stepSize, labelMetric);

        updateFinishingLabels(minSpacing, labelMetric);
    }

    static private String createLabel(float value, LabelMetric labelMetric) {
        if (labelMetric.decimalPlaceDigits > 0) {
            String formatString = "%.";
            formatString += labelMetric.decimalPlaceDigits;
            formatString += "f";
            return String.format(formatString, value);
        } else
            return String.format("%d", (int)value);
    }

    public List<LabelEntry> getLabels() {
        return labels;
    }

    private float getStepFactor(int index) {
        int rest = index % 3;
        float stepBase = 5;
        if (rest == 1)
            stepBase = 2f;
        else if (rest == 2)
            stepBase = 1f;

        return stepBase / (float)Math.pow(10.f, index / 3);
    }

    static private float getOrderValue(float number) {
        number = Math.abs(number);

        if (number == 0.0)
            return 1;

        float order = 1;
        while (number >= 10) {
            number /= 10;
            order *= 10;
        }
        while (number < 1) {
            number *= 10;
            order /= 10;
        }
        return order;
    }
}

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
