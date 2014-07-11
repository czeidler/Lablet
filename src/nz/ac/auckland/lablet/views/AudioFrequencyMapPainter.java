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
import nz.ac.auckland.lablet.views.plotview.OffScreenPlotPainter;


public class AudioFrequencyMapPainter extends OffScreenPlotPainter {
    private int dataAdded = 0;
    private double maxFrequency = 1000000;

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
    protected void render(Canvas bitmapCanvas, RenderPayload payload) {
        FrequencyMapRenderPayload renderPayload = (FrequencyMapRenderPayload)payload;

        draw(bitmapCanvas, renderPayload.rangeMatrix, renderPayload.adapter, renderPayload.dataIndex,
                renderPayload.dataSize);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (dataAdded > 0) {
            draw(bitmapCanvas, containerView.getRangeMatrix(), (AudioFrequencyMapAdapter)dataAdapter,
                    dataAdapter.getSize() - dataAdded, dataAdded);
            dataAdded = 0;
        }

        super.onDraw(canvas);
    }

    private void draw(Canvas canvas, Matrix rangeMatrix, AudioFrequencyMapAdapter adapter, int start, int count) {
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

    class FrequencyMapRenderPayload extends RenderPayload {
        public Matrix rangeMatrix;
        public AudioFrequencyMapAdapter adapter;
        public int dataIndex;
        public int dataSize;

        public FrequencyMapRenderPayload(RectF realDataRect, Rect screenRect,
                                  Matrix rangeMatrix, AudioFrequencyMapAdapter adapter,
                                  int dataIndex, int dataSize) {
            super(realDataRect, screenRect);
            this.rangeMatrix = rangeMatrix;
            this.adapter = adapter;
            this.dataIndex = dataIndex;
            this.dataSize = dataSize;
        }
    }

    @Override
    protected AbstractPlotDataAdapter.IListener createListener() {
        return new AbstractPlotDataAdapter.IListener() {
            int added = 0;

            @Override
            public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number) {
                //dataAdded += number;
                //containerView.invalidate();
                added += number;
                if (added < 3)
                    return;
                index = dataAdapter.getSize() - added;
                if (index < 0)
                    index = 0;
                number = added;
                if (index + number >= dataAdapter.getSize())
                    number = dataAdapter.getSize() - index;
                added = 0;

                AudioFrequencyMapAdapter adapter = (AudioFrequencyMapAdapter)dataAdapter;
                RectF realDataRect = containerView.getRangeRect();
                realDataRect.left = adapter.getX(index);
                realDataRect.right = adapter.getX(index + number);
                Rect screenRect = containerView.toScreen(realDataRect);
                FrequencyMapRenderPayload renderPayload = new FrequencyMapRenderPayload(realDataRect, screenRect,
                        containerView.getRangeMatrix(), ((AudioFrequencyMapAdapter)dataAdapter).clone(),
                        index, number);

                triggerOffScreenRendering(renderPayload);
            }

            @Override
            public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number) {
                triggerRedrawAll();
            }

            @Override
            public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number) {
                triggerRedrawAll();
            }

            @Override
            public void onAllDataChanged(AbstractPlotDataAdapter plot) {
                triggerRedrawAll();
            }

            private void triggerRedrawAll() {
                RectF realDataRect = containerView.getRangeRect();
                Rect screenRect = containerView.toScreen(realDataRect);
                FrequencyMapRenderPayload renderPayload = new FrequencyMapRenderPayload(realDataRect, screenRect,
                        containerView.getRangeMatrix(), ((AudioFrequencyMapAdapter)dataAdapter).clone(),
                        0, dataAdapter.getSize());
                renderPayload.clearParentBitmap = true;
                triggerOffScreenRendering(renderPayload);
            }
        };
    }
}
