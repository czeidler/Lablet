/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.graphics.*;
import nz.ac.auckland.lablet.views.plotview.ArrayConcurrentPainter;
import nz.ac.auckland.lablet.views.plotview.CloneablePlotDataAdapter;
import nz.ac.auckland.lablet.views.plotview.Range;
import nz.ac.auckland.lablet.views.plotview.StrategyPainter;

import java.util.List;


public class AudioAmplitudePainter extends ArrayConcurrentPainter {
    final private Paint penMinMaxPaint = new Paint();
    final private Paint penStdPaint = new Paint();

    public AudioAmplitudePainter(CloneablePlotDataAdapter dataAdapter) {
        super(dataAdapter);
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

    @Override
    public List<StrategyPainter.RenderPayload> collectRenderPayloads(boolean geometryInfoNeeded,
                                                                     RectF requestedRealRect, RectF maxRealRect) {
        // we need the geometry info to calculate getSamplesPerPixel
        return super.collectRenderPayloads(true, requestedRealRect, maxRealRect);
    }

    @Override
    protected RectF getRealDataRect(int startIndex, int lastIndex) {
        AudioAmplitudePlotDataAdapter audioAmplitudePlotDataAdapter = (AudioAmplitudePlotDataAdapter)dataAdapter;
        RectF realDataRect = getContainerView().getRange();
        realDataRect.left = audioAmplitudePlotDataAdapter.getX(startIndex).floatValue();
        realDataRect.right = audioAmplitudePlotDataAdapter.getX(lastIndex).floatValue();
        return realDataRect;
    }

    protected Range getDataRangeFor(float left, float right) {
        return new Range(0, dataAdapter.getSize() - 1);
    }

    private int getSamplesPerPixel(ArrayRenderPayload payload) {
        RectF screenRect = payload.getScreenRect();

        int dataRange = payload.getRegion().getMax() - payload.getRegion().getMin();

        return (int)Math.ceil((double)dataRange / screenRect.width());
    }

    @Override
    protected void drawRange(Canvas canvas, ArrayRenderPayload payload, Range range) {
        AudioAmplitudePlotDataAdapter adapter = (AudioAmplitudePlotDataAdapter)payload.getAdapter();
        Matrix rangeMatrix = payload.getRangeMatrix();

        int start = range.min;
        int count = range.max - range.min + 1;
        if (start < 0)
            start = 0;
        int dataSize = adapter.getSize();
        if (count > dataSize)
            count = dataSize;

        final int samplesPerPixel = getSamplesPerPixel(payload);

        Path outerPath = new Path();
        Path innerPath = new Path();

        for (int i = 0; i < count; i += samplesPerPixel) {
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            float ampSum = 0;
            float ampSquareSum = 0;
            for (int a = 0; a < samplesPerPixel; a++) {
                int index = i + a;
                if (start + index >= dataSize)
                    break;
                float value = adapter.getY(start + index).floatValue();
                ampSum += value;
                ampSquareSum += Math.pow(value, 2);
                if (value < min)
                    min = value;
                if (value > max)
                    max = value;
            }
            float average = ampSum / samplesPerPixel;
            float std = (float) Math.sqrt((samplesPerPixel * ampSquareSum + Math.pow(ampSum, 2))
                    / (samplesPerPixel * (samplesPerPixel - 1)));

            // drawing
            float x = adapter.getX(start + i).floatValue();
            outerPath.moveTo(x, min);
            outerPath.lineTo(x, max);

            innerPath.moveTo(x, average - std / 2.f * 3.f);
            innerPath.lineTo(x, average + std / 2.f * 3.f);
        }

        outerPath.transform(rangeMatrix);
        innerPath.transform(rangeMatrix);
        canvas.drawPath(outerPath, penMinMaxPaint);
        canvas.drawPath(innerPath, penStdPaint);
    }
}
