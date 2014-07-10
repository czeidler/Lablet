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


public class AudioAmplitudePainter extends OffScreenPlotPainter {
    final private Paint penMinMaxPaint = new Paint();
    final private Paint penStdPaint = new Paint();

    private int dataAdded = 0;

    public AudioAmplitudePainter() {
        init();
    }

    private void init() {
        penMinMaxPaint.setColor(Color.argb(255, 0, 0, 100));
        penMinMaxPaint.setStrokeWidth(1);
        penMinMaxPaint.setStyle(Paint.Style.STROKE);

        penStdPaint.setColor(Color.BLUE);
        penStdPaint.setStrokeWidth(1);
        penStdPaint.setStyle(Paint.Style.STROKE);
    }

    class AudioRenderPayload extends RenderPayload {
        public Matrix rangeMatrix;
        public AudioAmplitudePlotDataAdapter adapter;
        public int dataIndex;
        public int dataSize;

        public AudioRenderPayload(RectF realDataRect, Rect screenRect,
                                  Matrix rangeMatrix, AudioAmplitudePlotDataAdapter adapter,
                                  int dataIndex, int dataSize) {
            super(realDataRect, screenRect);
            this.rangeMatrix = rangeMatrix;
            this.adapter = adapter;
            this.dataIndex = dataIndex;
            this.dataSize = dataSize;
        }
    }

    @Override
    protected void render(Canvas bitmapCanvas, RenderPayload payload) {
        AudioRenderPayload renderPayload = (AudioRenderPayload)payload;

        draw(bitmapCanvas, renderPayload.rangeMatrix, renderPayload.adapter, renderPayload.dataIndex,
                renderPayload.dataSize);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (dataAdded > 0) {
            draw(bitmapCanvas, containerView.getRangeMatrix(), (AudioAmplitudePlotDataAdapter)dataAdapter,
                    dataAdapter.getSize() - dataAdded, dataAdded);
            dataAdded = 0;
        }

        super.onDraw(canvas);
    }

    private void draw(Canvas canvas, Matrix rangeMatrix, AudioAmplitudePlotDataAdapter adapter, int start, int count) {
        if (start < 0)
            start = 0;
        int dataSize = adapter.getSize();
        if (count > dataSize)
            count = dataSize;

        final int samplesPerPixels = 10;

        Path outerPath = new Path();
        Path innerPath = new Path();

        for (int i = 0; i < count; i += samplesPerPixels) {
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            float ampSum = 0;
            float ampSquareSum = 0;
            for (int a = 0; a < samplesPerPixels; a++) {
                int index = i + a;
                if (start + index >= dataSize)
                    break;
                float value = adapter.getY(start + index);
                ampSum += value;
                ampSquareSum += Math.pow(value, 2);
                if (value < min)
                    min = value;
                if (value > max)
                    max = value;
            }
            float average = ampSum / samplesPerPixels;
            float std = (float) Math.sqrt((samplesPerPixels * ampSquareSum + Math.pow(ampSum, 2))
                    / (samplesPerPixels * (samplesPerPixels - 1)));

            // drawing
            float x = adapter.getX(start + i);
            outerPath.moveTo(x, min);
            outerPath.lineTo(x, max);

            innerPath.moveTo(x, average - std / 2);
            innerPath.lineTo(x, average + std / 2);
        }

        outerPath.transform(rangeMatrix);
        innerPath.transform(rangeMatrix);
        canvas.drawPath(outerPath, penMinMaxPaint);
        canvas.drawPath(innerPath, penStdPaint);
    }

    @Override
    protected AbstractPlotDataAdapter.IListener createListener() {
        return new AbstractPlotDataAdapter.IListener() {
            @Override
            public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number) {
                //dataAdded += number;
                //containerView.invalidate();

                RectF realDataRect = containerView.getRangeRect();
                Rect screenRect = containerView.toScreen(realDataRect);
                AudioRenderPayload renderPayload = new AudioRenderPayload(realDataRect, screenRect,
                        containerView.getRangeMatrix(), ((AudioAmplitudePlotDataAdapter) dataAdapter).clone(),
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
                AudioRenderPayload renderPayload = new AudioRenderPayload(realDataRect, screenRect,
                        containerView.getRangeMatrix(), ((AudioAmplitudePlotDataAdapter)dataAdapter).clone(),
                        0, dataAdapter.getSize());
                renderPayload.clearParentBitmap = true;
                triggerOffScreenRendering(renderPayload);
            }
        };
    }
}
