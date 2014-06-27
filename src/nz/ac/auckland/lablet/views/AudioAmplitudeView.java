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
import android.view.ViewGroup;


public class AudioAmplitudeView extends ViewGroup {
    final private float BORDER = 0.0f;
    final private int BACKGROUND_COLOR = Color.argb(255, 80, 80, 80);
    final private Paint penMinMaxPaint = new Paint();
    final private Paint penStdPaint = new Paint();

    private Rect viewRect = new Rect();

    private Bitmap bitmap = null;
    private Canvas bitmapCanvas = null;

    private int position = 0;
    private float maxPosition = 0;
    private float samplesPerPixels = 15;
    private float amplitudeMax = 100000;


    public AudioAmplitudeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        penMinMaxPaint.setColor(Color.argb(255, 0, 0, 100));
        penMinMaxPaint.setStrokeWidth(1);
        penMinMaxPaint.setStyle(Paint.Style.STROKE);

        penStdPaint.setColor(Color.BLUE);
        penStdPaint.setStrokeWidth(1);
        penStdPaint.setStyle(Paint.Style.STROKE);
    }

    public void addData(float amplitudes[]) {
        for (int i = 0; i < amplitudes.length; i += samplesPerPixels) {
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            float ampSum = 0;
            float ampSquareSum = 0;
            for (int a = 0; a < samplesPerPixels; a++) {
                int index = i + a;
                if (index >= amplitudes.length)
                    break;
                float value = amplitudes[index];
                ampSum += value;
                ampSquareSum += Math.pow(value, 2);
                if (value < min)
                    min = value;
                if (value > max)
                    max = value;
            }
            float average = ampSum / samplesPerPixels;
            float std = (float)Math.sqrt((samplesPerPixels * ampSquareSum + Math.pow(ampSum, 2))
                    / (samplesPerPixels * (samplesPerPixels - 1)));

            drawAmplitude(position, min, max, average, std);
            position++;
            if (position >= maxPosition) {
                clearBitmap();
                position = 0;
            }
        }
    }

    private float toScreenY(float y) {
        return (viewRect.height() * (1.f - BORDER)) * y / amplitudeMax + getAmpBaseLine();
    }
    private void drawAmplitude(int position, float min, float max, float average, float std) {
        float x = (float)position / samplesPerPixels;

        bitmapCanvas.drawLine(x, toScreenY(min), x, toScreenY(max), penMinMaxPaint);
        bitmapCanvas.drawLine(x, toScreenY(average - std / 2), x, toScreenY(average + std / 2), penStdPaint);
    }

    private int getAmpBaseLine() {
        return viewRect.height() / 2;
    }

    private void clearBitmap() {
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_COLOR);
        backgroundPaint.setStyle(Paint.Style.FILL);
        bitmapCanvas.drawRect(viewRect, backgroundPaint);
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        viewRect.set(0, 0, w, h);
        maxPosition = w * samplesPerPixels;

        clearBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
    }
}
