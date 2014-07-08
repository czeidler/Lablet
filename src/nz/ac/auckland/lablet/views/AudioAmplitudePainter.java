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
import nz.ac.auckland.lablet.views.plotview.AbstractPlotPainter;


public class AudioAmplitudePainter extends AbstractPlotPainter {
    final private int BACKGROUND_COLOR = Color.argb(255, 80, 80, 80);
    final private Paint penMinMaxPaint = new Paint();
    final private Paint penStdPaint = new Paint();

    private Bitmap bitmap = null;
    private Canvas bitmapCanvas = null;


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

    private void clearBitmap() {
        bitmap.eraseColor(BACKGROUND_COLOR);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w <= 0 || h <= 0)
            return;

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        clearBitmap();
    }

    @Override
    public void onDraw(Canvas canvas) {
        final int samplesPerPixels = 10;
        if (dataAdded > 0) {
            Path outerPath = new Path();
            Path innerPath = new Path();

            AudioAmplitudePlotDataAdapter adapter = (AudioAmplitudePlotDataAdapter)dataAdapter;
            int startIndex = dataAdapter.getSize() - dataAdded;
            for (int i = 0; i < dataAdded; i += samplesPerPixels) {
                float min = Float.MAX_VALUE;
                float max = Float.MIN_VALUE;
                float ampSum = 0;
                float ampSquareSum = 0;
                for (int a = 0; a < samplesPerPixels; a++) {
                    int index = i + a;
                    if (index >= dataAdded)
                        break;
                    float value = adapter.getY(startIndex + index);
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
                float x = adapter.getX(startIndex + i);
                outerPath.moveTo(x, min);
                outerPath.lineTo(x, max);

                innerPath.moveTo(x, average - std / 2);
                innerPath.lineTo(x, average + std / 2);
            }
            dataAdded = 0;

            containerView.applyRangeMatrix(outerPath);
            containerView.applyRangeMatrix(innerPath);
            bitmapCanvas.drawPath(outerPath, penMinMaxPaint);
            bitmapCanvas.drawPath(innerPath, penStdPaint);
        }

        if (bitmap != null)
            canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    protected AbstractPlotDataAdapter.IListener createListener() {
        return new AbstractPlotDataAdapter.IListener() {
            @Override
            public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number) {
                dataAdded += number;
                containerView.invalidate();
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
                clearBitmap();
                dataAdded = dataAdapter.getSize();
                containerView.invalidate();
            }
        };
    }
}
