/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview.axes;

import nz.ac.auckland.lablet.views.plotview.IScale;
import nz.ac.auckland.lablet.views.plotview.Log10Scale;


public class LabelPartitionerLog10 extends LabelPartitioner {
    private IScale scale = new Log10Scale();

    private float toScreen(float realValue) {
        return axisExtent * scale.scale(realValue) / Math.abs(scale.scale(realEnd) - scale.scale(realStart));
    }

    @Override
    public LabelMetric estimateLabelMetric(float min, float max) {
        return null;
    }

    @Override
    protected void calculate() {
        final LabelMetric labelMetric = calculateLabelMetric(0.1f, realStart, realEnd);

        final float realStartLog = scale.scale(realStart);
        final float realEndLog = scale.scale(realEnd);

        int logValue = (int)realStartLog;
        if (logValue < realStartLog)
            logValue++;

        // realEndLog + 1 to get the intermediate ticks at the top
        while (logValue < realEndLog + 1) {
            // correct value if when
            LabelEntry entry = new LabelEntry();
            float realValue = (float)Math.pow(10, logValue);
            entry.realValue = realValue;
            entry.relativePosition = logValue / (realEndLog - realStartLog);
            entry.label = createLabel(realValue, labelMetric);

            labels.add(entry);

            logValue++;
        }

        // intermediate ticks
        for (int i = labels.size() - 1; i >= 0; i--) {
            LabelEntry entry = labels.get(i);
            float step = entry.realValue / 10;
            float previousScreenPosition = toScreen(entry.realValue);
            for (int a = 1; a < 10; a++) {
                float currentReal = entry.realValue - a * step;
                if (currentReal > realEnd)
                    continue;
                // TODO: also handle range 0 to 1
                if (currentReal <= realStart || currentReal < 1)
                    break;
                LabelEntry newEntry = new LabelEntry();
                newEntry.realValue = currentReal;
                newEntry.relativePosition = scale.scale(currentReal) / (realEndLog - realStartLog);
                newEntry.isFullTick = false;
                labels.add(i, newEntry);

                float screenPosition = toScreen(currentReal);
                if (Math.abs(previousScreenPosition - screenPosition) > labelExtent + 0.0 * minSpacing)
                    newEntry.label = createLabel(currentReal, labelMetric);
                previousScreenPosition = screenPosition;
            }
        }
    }
}
