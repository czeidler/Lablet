/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview.axes;


public class LabelPartitionerLinear extends LabelPartitioner {

    private float toScreen(float realValue) {
        return axisExtent * realValue / Math.abs(realEnd - realStart);
    }

    private void fillLabelList(final int bestFoundLabelNumber, final float stepSize, LabelMetric labelMetric) {
        final float realDiff = Math.abs(realEnd - realStart);
        labels.clear();
        final float realLabelStart = (int)(Math.min(realStart, realEnd) / stepSize) * stepSize;
        final float realLabelStartOffset = realLabelStart - Math.min(realStart, realEnd);

        for (int i = 0; i < bestFoundLabelNumber; i++) {
            LabelEntry entry = new LabelEntry();
            final float realValue = realLabelStart + i * stepSize;
            entry.realValue = realValue;
            entry.relativePosition = (realLabelStartOffset + i * stepSize) / realDiff;
            entry.label = LabelPartitionerHelper.createLabel(realValue, labelMetric);

            labels.add(entry);
        }
    }

    private boolean fuzzyEqual(float value1, float value2) {
        return Math.abs(value1 - value2) < 0.0000001;
    }

    @Override
    protected void calculate() {
        if (fuzzyEqual(realEnd, realStart))
            return;

        int maxLabelNumber = (int)(axisExtent / (labelExtent + minSpacing)) + 1;
        int optimalLabelNumber = (int)(axisExtent / (labelExtent + optimalSpacing)) + 1;

        float diff = Math.abs(realEnd - realStart);
        float diffOrderValue = LabelPartitionerHelper.getOrderValue(diff);

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

        LabelMetric labelMetric = LabelPartitionerHelper.calculateLabelMetric(stepFactor, realStart, realEnd);

        fillLabelList(bestFoundLabelNumber, stepSize, labelMetric);
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
}
