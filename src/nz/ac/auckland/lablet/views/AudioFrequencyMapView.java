/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import nz.ac.auckland.lablet.views.plotview.RangeDrawingView;


public class AudioFrequencyMapView extends RangeDrawingView {
    final private float BORDER = 0.0f;
    final private int BACKGROUND_COLOR = Color.argb(255, 80, 80, 80);
    final private Paint penPaint = new Paint();

    private Bitmap bitmap = null;
    private Canvas bitmapCanvas = null;

    private int position = 0;
    private float[] frequencies = null;
    private int frequencyCount = 100;
    private double maxFrequency = 100000;

    public AudioFrequencyMapView(Context context, AttributeSet attrs) {
        super(context, attrs);

        penPaint.setColor(Color.GREEN);
        penPaint.setStrokeWidth(1);
        penPaint.setStyle(Paint.Style.FILL);
    }

    public void setFrequencyCount(int frequencyCount) {
        this.frequencyCount = frequencyCount;
    }

    public void addData(float[] frequencies) {
        if (this.frequencies == null) {

        }
        this.frequencies = frequencies;
        invalidate();
    }

    private void clearBitmap() {
        bitmap.eraseColor(BACKGROUND_COLOR);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w <= 0 || h <= 0)
            return;

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        clearBitmap();

        setRangeX(0, w);
        setRangeY(0, frequencyCount);
        applyRangeMatrix(bitmapCanvas);
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
    protected void onDraw(Canvas canvas) {
        if (frequencies != null && frequencies.length > 0) {
            if (position > getWidth()) {
                position = 0;
                clearBitmap();
            }

            for (int i = 0; i < frequencies.length; i++) {
                double value = Math.log10(Math.abs(frequencies[i])) / Math.log10(maxFrequency);
                penPaint.setColor(heatMap(value));
                bitmapCanvas.drawPoint(position, i, penPaint);
            }
            position++;
            frequencies = null;
        }

        if (bitmap != null)
            canvas.drawBitmap(bitmap, 0, 0, null);
    }
}
