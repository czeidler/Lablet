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

    private void calculate() {
        final float minSpacing = 20;
        final float optimalSpacing = axisExtent * 0.15f;
        int maxLabelNumber = (int)(axisExtent / (labelExtent + minSpacing)) + 1;
        int optimalLabelNumber = (int)(axisExtent / (labelExtent + optimalSpacing)) + 1;

        float diff = Math.abs(realEnd - realStart);
        float diffOrderValue = getOrderValue(diff);

        int bestFoundLabelNumber = 1;
        float stepSize = 1;
        for (int i = 0; bestFoundLabelNumber < maxLabelNumber && bestFoundLabelNumber < optimalLabelNumber; i++) {
            stepSize = getStepFactor(i) * diffOrderValue;
            if (stepSize > diff)
                continue;

            int labelNumber = (int)(diff / stepSize) + 1;
            if (optimalLabelNumber - labelNumber < optimalLabelNumber - bestFoundLabelNumber)
                bestFoundLabelNumber = labelNumber;
        }

        // fill label list
        labels.clear();
        float realLabelStart = (int)(Math.min(realStart, realEnd) / stepSize) * stepSize;
        for (int i = 0; i < bestFoundLabelNumber; i++) {
            LabelEntry entry = new LabelEntry();
            float realValue = realLabelStart + i * stepSize;
            entry.realValue = realValue;
            entry.relativePosition = realValue / diff;
            entry.label = createLabel(realValue);

            labels.add(entry);
        }

        // fill finishing labels
        if (finishingLabelsMatchEnds && labels.size() >= 2) {
            LabelEntry entryStart = labels.get(0);
            if (Math.abs(toScreen(entryStart.realValue) - toScreen(realStart)) > minSpacing) {
                entryStart = new LabelEntry();
                labels.add(0, entryStart);
            }
            entryStart.realValue = realStart;
            entryStart.relativePosition = 0;
            entryStart.label = createLabel(realStart);

            LabelEntry entryEnd = labels.get(labels.size() - 1);
            if (Math.abs(toScreen(entryEnd.realValue) - toScreen(realEnd)) > minSpacing) {
                entryStart = new LabelEntry();
                labels.add(entryStart);
            }
            entryEnd.realValue = realEnd;
            entryEnd.relativePosition = 1;
            entryEnd.label = createLabel(realEnd);
        }
    }

    private String createLabel(float value) {
        return String.format("%1.0f", value);
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

    private float getOrderValue(float number) {
        number = Math.abs(number);

        if (number == 0.0)
            return 1;

        float order = 1;
        while (number >= 10) {
            number /= 10;
            order *= 10;
        }
        while (number <= 0.1) {
            number *= 10;
            order /= 10;
        }
        return order;
    }
}

public class YAxisView extends ViewGroup implements IYAxis {
    private float axisTopOffset = 0;
    private float axisBottomOffset = 0;
    private float realTop = 0;
    private float realBottom = 0;
    private float optimalWidth = 0;
    private int relevantDigits = 3;

    private Paint labelPaint = new Paint();
    private float labelHeight = 5;
    private Paint axisPaint = new Paint();

    final private float SCALE_WIDTH = 8;
    final private float SPACING = 4;

    private String label = "";
    private String unit = "";

    public YAxisView(Context context, AttributeSet attrs) {
        super(context, attrs);

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

        calculateOptimalWidth();
    }

    @Override
    public void setDataRange(float bottom, float top) {
        realTop = top;
        realBottom = bottom;

        calculateOptimalWidth();
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

    private void calculateOptimalWidth() {
        int labelSize = getTotalDigits(realTop, realBottom, relevantDigits);

        // minus sign?
        if (realTop < 0 || realBottom < 0)
            labelSize++;

        int maxPower = Math.abs(Math.max(getOrder(realTop), getOrder(realBottom)) - (relevantDigits - 1));

        if (maxPower > 0)
            labelSize += 2 + maxPower;

        String dummyLabel = "";
        for (int i = 0; i < labelSize; i++)
            dummyLabel += "9";

        Rect bounds = new Rect();
        labelPaint.getTextBounds(dummyLabel, 0, dummyLabel.length(), bounds);

        optimalWidth = SCALE_WIDTH + 2 * SPACING + bounds.width();
    }

    @Override
    public float getOptimalWidth() {
        return 100;
        //return optimalWidth;
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

        LabelPartitioner partitioner = new LabelPartitioner(labelHeight, getAxisLength(), Math.min(realTop, realBottom),
                Math.max(realTop, realBottom));
        List<LabelPartitioner.LabelEntry> labels = partitioner.getLabels();
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

        canvas.drawText(labelText, getWidth() - labelRect.width() - SPACING - SCALE_WIDTH,
                yPosition + labelRect.height() / 2, labelPaint);

        // draw tick
        canvas.drawLine(getWidth() - SCALE_WIDTH, yPosition, getWidth(), yPosition, axisPaint);
    }

    private float getAxisLength() {
        return getHeight() - getAxisBottomOffset() - getAxisTopOffset();
    }

    private String toLabel(float value) {
        return String.format("%1.0f", value);
    }

    private int getOrder(float number) {
        number = Math.abs(number);

        if (number == 0.0)
            return 1;

        int order = 0;
        while (number >= 10) {
            number /= 10;
            order++;
        }
        while (number <= 0.1) {
            number *= 10;
            order--;
        }
        return order;
    }

    private int getTotalDigits(float value1, float value2, int relevantDigits) {
        int maxOrder = Math.max(getOrder(value1), getOrder(value2));
        int diffOrder = getOrder(value2 - value2);
        int minOrder = diffOrder + (relevantDigits - 1);

        return Math.abs(minOrder - maxOrder) + 1;
    }
}
