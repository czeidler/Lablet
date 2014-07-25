/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.graphics.*;
import nz.ac.auckland.lablet.views.plotview.*;

import java.util.Arrays;


public class AudioFrequencyMapPainter extends ArrayOffScreenPlotPainter {
    final private double maxFrequencyAmplitude = 1000000;

    public AudioFrequencyMapPainter() {
        setMaxDirtyRanges(3);
    }

    private int heatMap(double value) {
        if (value > 1.)
            return Color.WHITE;

        int[] colors = {
                Color.BLUE,
                Color.CYAN,
                Color.GREEN,
                Color.YELLOW,
                Color.RED,
                };

        int index = 1;
        if (value > 0.25)
            index = 2;
        if (value > 0.5)
            index = 3;
        if (value > 0.75)
            index = 4;

        int red = (int)((1.d - value) * Color.red(colors[index - 1]) + value * Color.red(colors[index]));
        int green = (int)((1.d - value) * Color.green(colors[index - 1]) + value * Color.green(colors[index]));
        int blue = (int)((1.d - value) * Color.blue(colors[index - 1]) + value * Color.blue(colors[index]));

        return Color.rgb(red, green, blue);
    }

    @Override
    protected RectF getRealDataRect(int startIndex, int lastIndex) {
        AudioFrequencyMapAdapter audioAmplitudePlotDataAdapter = (AudioFrequencyMapAdapter)dataAdapter;
        RectF realDataRect = containerView.getRange();
        if (audioAmplitudePlotDataAdapter.getSize() > 0) {
            realDataRect.left = audioAmplitudePlotDataAdapter.getX(startIndex);
            realDataRect.right = audioAmplitudePlotDataAdapter.getX(lastIndex);
        }
        return realDataRect;
    }

    @Override
    protected Range getDataRangeFor(float left, float right) {
        Range range = new Range(0, dataAdapter.getSize() - 1);
        if (dataAdapter.getSize() > 1) {
            AudioFrequencyMapAdapter audioAmplitudePlotDataAdapter = (AudioFrequencyMapAdapter)dataAdapter;
            float value0 = audioAmplitudePlotDataAdapter.getX(0);
            float value1 = audioAmplitudePlotDataAdapter.getX(1);
            float stepSize = value1 - value0;

            range.min = (int)((left - value0) / stepSize);
            range.max = (int)Math.ceil((right - value0) / stepSize);

            if (range.min < 0)
                range.min = 0;
            if (range.max >= dataAdapter.getSize())
                range.max = dataAdapter.getSize() -1;
        }
        return range;
    }

    @Override
    protected void drawRange(Canvas canvas, ArrayRenderPayload payload, Range range) {
        AudioFrequencyMapAdapter adapter = (AudioFrequencyMapAdapter)payload.getAdapter();
        Matrix rangeMatrix = payload.getRangeMatrix();

        int start = range.min;
        int count = range.max - range.min + 1;
        if (start < 0)
            start = 0;
        int dataSize = adapter.getSize();
        if (count > dataSize)
            count = dataSize;

        for (int idx = 0; idx < count; idx++) {
            final int index = start + idx;
            final float[] frequencies = adapter.getY(index);
            final float time = adapter.getX(index);

            final int[] colors = getColors(frequencies, payload);

            float[] screenLeftTop = new float[2];
            screenLeftTop[0] = time;
            screenLeftTop[1] = payload.getRealDataRect().top;
            rangeMatrix.mapPoints(screenLeftTop);

            canvas.drawBitmap(colors, 0, 1, screenLeftTop[0] - 0.5f, screenLeftTop[1], 1,
                    payload.getScreenRect().height(), true, null);
        }
    }

    private int toPixel(float scaledValue, float scaledBottom, float scaledTop, Rect screenRect) {
        return (int)((scaledValue - scaledBottom) / (scaledTop - scaledBottom) * screenRect.height());
    }

    final float frequencyRang = 22050;
    private float getRealValue(int index, int arraySize) {
        return (float)index / arraySize * frequencyRang;
    }

    private int[] getColors(final float[] frequencies, final ArrayRenderPayload payload) {
        final float scaledBottom = yScale.scale(payload.getRealDataRect().bottom);
        final float scaledTop = yScale.scale(payload.getRealDataRect().top);
        final Rect screenRect = payload.getScreenRect();
        final int[] colors = new int[screenRect.height()];
        Arrays.fill(colors, Color.TRANSPARENT);

        float frequencyAmpSum = 0;
        int currentPixel = -1;
        int perPixelCount = 0;
        for (int i = 0; i < frequencies.length; i++) {
            float frequencyAmp = frequencies[i];

            float frequency = getRealValue(i, frequencies.length);
            int pixel = toPixel(yScale.scale(frequency), scaledBottom, scaledTop, screenRect);
            if (pixel < 0)
                continue;
            if (pixel >= colors.length)
                break;
            if (currentPixel == -1)
                currentPixel = pixel;

            if (pixel == currentPixel) {
                frequencyAmpSum += frequencyAmp;
                perPixelCount++;
            } else {
                float frequencyAmpAverage = frequencyAmpSum / perPixelCount;
                double amplitude = Math.log10(Math.abs(frequencyAmpAverage)) / Math.log10(maxFrequencyAmplitude);
                //double amplitude = Math.abs(frequencyAmpAverage) / maxFrequencyAmplitude;
                int colorIndex = colors.length - 1 - currentPixel;
                colors[colorIndex] = heatMap(amplitude);

                frequencyAmpSum = frequencyAmp;
                currentPixel = pixel;
                perPixelCount = 1;
            }
        }
        return colors;
    }
}
