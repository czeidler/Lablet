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
import edu.emory.mathcs.jtransforms.dct.DoubleDCT_1D;


public class AudioFrequencyView extends ViewGroup {
    final private float BORDER = 0.0f;
    final private int BACKGROUND_COLOR = Color.argb(255, 80, 80, 80);
    final private Paint penPaint = new Paint();

    private Rect viewRect = new Rect();

    private Bitmap bitmap = null;
    private Canvas bitmapCanvas = null;

    private int position = 0;
    private float valueMax = 44100;


    public AudioFrequencyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);

        penPaint.setColor(Color.GREEN);
        penPaint.setStrokeWidth(1);
        penPaint.setStyle(Paint.Style.FILL);
    }

    public void addData(float amplitudes[]) {
        clearBitmap();
        position = 0;
        double[] frequencies = fourier(amplitudes);
        //float[] bins = binIt(frequencies, 10, valueMax);

        for (int i = 0; i < frequencies.length; i++) {

            drawFrequencies(position, (float)Math.abs(frequencies[i]));

            position++;
        }
    }

    private float toScreenY(float y) {
        return getAmpBaseLine() - ((viewRect.height() * (1.f - BORDER)) * y / valueMax);
    }

    private void drawFrequencies(int position, float frequency) {
        float binWidth = 3.f;
        float binPostition = binWidth * position;
        bitmapCanvas.drawRect(binPostition, toScreenY(frequency), binPostition + binWidth, toScreenY(0), penPaint);
    }

    private int getAmpBaseLine() {
        return viewRect.height();
    }

    private void clearBitmap() {
        bitmap.eraseColor(BACKGROUND_COLOR);
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w <= 0 || h <= 0)
            return;

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        viewRect.set(0, 0, w, h);

        clearBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null)
            canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private void hammingWindow(double[] samples) {
        for (int i = 0; i < samples.length; i++)
            samples[i] *= (0.54f - 0.46f * Math.cos(2 * Math.PI * i / (samples.length - 1)));
    }

    private double[] fourier(float[] in) {
        double trafo[] = new double[in.length];
        for (int i = 0; i < in.length; i++)
            trafo[i] = in[i];

        DoubleDCT_1D dct = new DoubleDCT_1D(trafo.length);

        // in place window
        hammingWindow(trafo);

        // in place transform: timeData becomes frequency data
        dct.forward(trafo, true);

        return trafo;
    }
}
