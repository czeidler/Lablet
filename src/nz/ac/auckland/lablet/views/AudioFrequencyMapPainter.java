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


public class AudioFrequencyMapPainter extends AbstractPlotPainter {
    final private int BACKGROUND_COLOR = Color.argb(255, 80, 80, 80);
    final private Paint penPaint = new Paint();

    private Bitmap bitmap = null;
    private Canvas bitmapCanvas = null;

    private int dataAdded = 0;
    private double maxFrequency = 1000000;

    public AudioFrequencyMapPainter() {
        init();
    }

    private void init() {
        penPaint.setColor(Color.GREEN);
        penPaint.setStrokeWidth(1);
        penPaint.setStyle(Paint.Style.FILL);
    }

    private void clearBitmap() {
        bitmap.eraseColor(BACKGROUND_COLOR);
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
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        clearBitmap();
    }

    @Override
    public void onDraw(Canvas canvas) {
        AudioFrequencyMapAdapter adapter = (AudioFrequencyMapAdapter)dataAdapter;

        while (dataAdded > 0) {
            int index = adapter.getSize() - dataAdded;
            float[] frequencies = adapter.getY(index);
            float time = adapter.getX(index);

            for (int i = 0; i < frequencies.length; i++) {
                double amplitude = Math.log10(Math.abs(frequencies[i])) / Math.log10(maxFrequency);
                penPaint.setColor(heatMap(amplitude));
                float frequency = (float)i / (frequencies.length - 1)
                        * (containerView.getRangeTop() - containerView.getRangeBottom());
                bitmapCanvas.drawPoint(containerView.toScreenX(time), containerView.toScreenY(frequency), penPaint);
            }
            dataAdded = 0;
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
