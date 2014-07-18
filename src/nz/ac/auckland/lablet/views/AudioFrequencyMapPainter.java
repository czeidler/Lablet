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


public class AudioFrequencyMapPainter extends ArrayOffScreenPlotPainter {
    final private double maxFrequencyAmplitude = 1000000;
    final private int maxFrequency = 22050;
    float maxScaledFrequency;

    public AudioFrequencyMapPainter() {
        setMaxDirtyRanges(3);

        maxScaledFrequency = yScale.scale(maxFrequency);
    }

    @Override
    public void setYScale(IScale yScale) {
        super.setYScale(yScale);

        maxScaledFrequency = this.yScale.scale(maxFrequency);
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
    protected RectF getRealDataRect(AbstractPlotDataAdapter adapter, int startIndex, int lastIndex) {
        AudioFrequencyMapAdapter audioAmplitudePlotDataAdapter = (AudioFrequencyMapAdapter)adapter;
        RectF realDataRect = containerView.getRangeRect();
        realDataRect.left = audioAmplitudePlotDataAdapter.getX(startIndex);
        realDataRect.right = audioAmplitudePlotDataAdapter.getX(lastIndex);
        return realDataRect;
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

            final int[] colors = getColors(frequencies, payload.getScreenRect().height());

            float[] screenLeftTop = new float[2];
            screenLeftTop[0] = time;
            screenLeftTop[1] = maxFrequency;
            rangeMatrix.mapPoints(screenLeftTop);

            canvas.drawBitmap(colors, 0, 1, screenLeftTop[0] - 0.5f, screenLeftTop[1], 1,
                    payload.getScreenRect().height(), false, null);
        }
    }

    private int toPixel(float value, int screenRange) {
        return (int)(value / maxScaledFrequency * screenRange);
    }

    private float getRealValue(int index, int arraySize, float frequencyRang) {
        return (float)index / arraySize * frequencyRang;
    }

    private int[] getColors(final float[] frequencies, final int screenHeight) {
        final int[] colors = new int[screenHeight];

        float frequencyAmpSum = 0;
        int currentPixel = 0;
        int perPixelCount = 0;
        for (int i = 0; i < frequencies.length; i++) {
            float frequencyAmp = frequencies[i];

            float frequency = getRealValue(i, frequencies.length, maxFrequency);
            int pixel = toPixel(yScale.scale(frequency), screenHeight);
            if (pixel == currentPixel) {
                frequencyAmpSum += frequencyAmp;
                perPixelCount++;
            } else {
                float frequencyAmpAverage = frequencyAmpSum / perPixelCount;
                double amplitude = Math.log10(Math.abs(frequencyAmpAverage)) / Math.log10(maxFrequencyAmplitude);
                colors[colors.length - 1 - currentPixel] = heatMap(amplitude);

                frequencyAmpSum = frequencyAmp;
                currentPixel++;
                perPixelCount = 1;
            }
        }
        return colors;
    }
}
