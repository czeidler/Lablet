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


public class AudioAmplitudeView extends RangeDrawingView {
    final private float BORDER = 0.0f;
    final private int BACKGROUND_COLOR = Color.argb(255, 80, 80, 80);
    final private Paint penMinMaxPaint = new Paint();
    final private Paint penStdPaint = new Paint();

    private Rect viewRect = new Rect();

    private Bitmap bitmap = null;
    private Canvas bitmapCanvas = null;

    private int position = 0;
    private float maxPosition = 0;
    private float samplesPerPixels = 10;
    private float amplitudeMax = 100000;

    private float[] amplitudes = null;

    public AudioAmplitudeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        penMinMaxPaint.setColor(Color.argb(255, 0, 0, 100));
        penMinMaxPaint.setStrokeWidth(1);
        penMinMaxPaint.setStyle(Paint.Style.STROKE);

        penStdPaint.setColor(Color.BLUE);
        penStdPaint.setStrokeWidth(1);
        penStdPaint.setStyle(Paint.Style.STROKE);

        setRangeY(-amplitudeMax, amplitudeMax);
    }

    public void addData(float amplitudes[]) {
        this.amplitudes = amplitudes;
        invalidate();
    }

    private void drawAmplitude(int position, float min, float max, float average, float std) {
        if (bitmapCanvas == null)
            return;

        float x = (float)position / samplesPerPixels;
        bitmapCanvas.drawLine(x, min, x, max, penMinMaxPaint);
        bitmapCanvas.drawLine(x, average - std / 2, x, average + std / 2, penStdPaint);
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

        viewRect.set(0, 0, w, h);
        maxPosition = w * samplesPerPixels;

        clearBitmap();

        setRangeX(0, w);
        applyRangeMatrix(bitmapCanvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (amplitudes != null) {
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
                float std = (float) Math.sqrt((samplesPerPixels * ampSquareSum + Math.pow(ampSum, 2))
                        / (samplesPerPixels * (samplesPerPixels - 1)));

                drawAmplitude(position, min, max, average, std);
                position++;
                if (position >= maxPosition) {
                    clearBitmap();
                    position = 0;
                }
            }
            amplitudes = null;
        }

        if (bitmap != null)
            canvas.drawBitmap(bitmap, 0, 0, null);
    }
}
