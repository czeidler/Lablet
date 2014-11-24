/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview.axes;

import java.util.ArrayList;
import java.util.List;


class LabelMetric {
    public int digits = 0;
    public int decimalPlaceDigits = 0;
}


class LabelPartitionerHelper {
    static public String createDummyLabel(LabelMetric metric) {
        float dummyValue = 1.f / 3 * (float)Math.pow(10, metric.digits);
        return createLabel(dummyValue, metric);
    }

    static protected String createLabel(float value, LabelMetric labelMetric) {
        if (labelMetric.decimalPlaceDigits > 0) {
            String formatString = "%.";
            formatString += labelMetric.decimalPlaceDigits;
            formatString += "f";
            return String.format(formatString, value);
        } else
            return String.format("%d", (int)value);
    }

    static public LabelMetric estimateLabelMetric(float min, float max) {
        return calculateLabelMetric(0.1f, min, max);
    }

    static protected LabelMetric calculateLabelMetric(float stepFactor, float realStart, float realEnd) {
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

    static protected float getOrderValue(float number) {
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

public abstract class LabelPartitioner {
    final protected float minSpacing = 5;
    protected float optimalSpacing;

    protected float labelExtent;
    protected float axisExtent;
    protected float realStart;
    protected float realEnd;


    public static class LabelEntry {
        public String label = "";
        public boolean isFullTick = true;
        public float realValue;
        public float relativePosition;
    }
    protected List<LabelEntry> labels = new ArrayList<>();

    abstract protected void calculate();

    /**
     *
     * @param labelExtent
     * @param axisExtent
     * @param realStart must be smaller than realEnd
     * @param realEnd
     */
    public List<LabelEntry> calculate(float labelExtent, float axisExtent, float realStart, float realEnd) {
        labels.clear();

        this.labelExtent = labelExtent;
        this.axisExtent = axisExtent;
        this.realStart = realStart;
        this.realEnd = realEnd;
        this.optimalSpacing = axisExtent * 0.15f;

        calculate();

        return labels;
    }
}


