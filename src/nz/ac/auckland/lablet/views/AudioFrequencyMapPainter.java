/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.graphics.*;
import nz.ac.auckland.lablet.views.plotview.AbstractPlotDataAdapter;
import nz.ac.auckland.lablet.views.plotview.ArrayOffScreenPlotPainter;
import nz.ac.auckland.lablet.views.plotview.Range;


public class AudioFrequencyMapPainter extends ArrayOffScreenPlotPainter {
    private double maxFrequency = 1000000;

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
    protected RectF getRealDataRect(AbstractPlotDataAdapter adapter, int startIndex, int lastIndex) {
        AudioFrequencyMapAdapter audioAmplitudePlotDataAdapter = (AudioFrequencyMapAdapter)adapter;
        RectF realDataRect = containerView.getRangeRect();
        realDataRect.left = audioAmplitudePlotDataAdapter.getX(startIndex);
        realDataRect.right = audioAmplitudePlotDataAdapter.getX(lastIndex);
        return realDataRect;
    }

    @Override
    protected void drawRange(Canvas canvas, ArrayRenderPayload payload, Range range) {
        AudioFrequencyMapAdapter adapter = (AudioFrequencyMapAdapter)payload.adapter;
        Matrix rangeMatrix = payload.rangeMatrix;

        int start = range.min;
        int count = range.max - range.min + 1;
        if (start < 0)
            start = 0;
        int dataSize = adapter.getSize();
        if (count > dataSize)
            count = dataSize;

        Paint paint = new Paint();
        for (int idx = 0; idx < count; idx++) {
            int index = start + idx;
            float[] frequencies = adapter.getY(index);
            float time = adapter.getX(index);

            for (int i = 0; i < frequencies.length; i++) {
                double amplitude = Math.log10(Math.abs(frequencies[i])) / Math.log10(maxFrequency);
                paint.setColor(heatMap(amplitude));
                float frequency = (float)i / (frequencies.length - 1)
                        * 44100 / 2;

                float[] points = new float[2];
                points[0] = time;
                points[1] = frequency;
                rangeMatrix.mapPoints(points);
                canvas.drawPoint(points[0], points[1], paint);
            }
        }
    }
}
