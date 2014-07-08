/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

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
