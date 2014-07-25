/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.*;

import java.util.ArrayList;
import java.util.List;


interface IPointRenderer {
    public void drawPoint(Canvas canvas, PointF position, DrawConfig config);
}

class DrawConfig {
    private Paint markerPaint = new Paint();
    private Paint linePaint = new Paint();

    public DrawConfig() {
        markerPaint.setColor(Color.GREEN);
        markerPaint.setStyle(Paint.Style.STROKE);

        linePaint.setColor(Color.YELLOW);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
    }

    public Paint getMarkerPaint() {
        return markerPaint;
    }

    public void setMarkerPaint(Paint markerPaint) {
        this.markerPaint = markerPaint;
    }

    public Paint getLinePaint() {
        return linePaint;
    }

    public void setLinePaint(Paint linePaint) {
        this.linePaint = linePaint;
    }
}

class CircleRenderer implements IPointRenderer {
    private float circleWidth = 3.5f;

    @Override
    public void drawPoint(Canvas canvas, PointF position, DrawConfig config) {
        canvas.drawCircle(position.x, position.y, circleWidth, config.getMarkerPaint());
    }
}

class CrossRenderer implements IPointRenderer {
    private final float size = 3f;

    @Override
    public void drawPoint(Canvas canvas, PointF position, DrawConfig config) {

        canvas.drawLine(position.x - size, position.y - size, position.x + size, position.y + size,
                config.getMarkerPaint());
        canvas.drawLine(position.x - size, position.y + size, position.x + size, position.y - size,
                config.getMarkerPaint());
    }
}

public class XYPainter extends ArrayOffScreenPlotPainter {
    private DrawConfig drawConfig = new DrawConfig();
    private IPointRenderer pointRenderer = new CrossRenderer();

    @Override
    protected RectF getRealDataRect(int startIndex, int lastIndex) {
        if (startIndex > 0)
            startIndex--;
        XYDataAdapter adapter = (XYDataAdapter)dataAdapter;
        RectF realDataRect = containerView.getRange();
        realDataRect.left = adapter.getX(startIndex);
        realDataRect.right = adapter.getX(lastIndex);
        if (realDataRect.width() < 40) {
            realDataRect.left -= 20;
            realDataRect.right += 20;
        }
        return realDataRect;
    }

    @Override
    protected void drawRange(Canvas bitmapCanvas, ArrayRenderPayload payload, Range range) {
        XYDataAdapter adapter = (XYDataAdapter)payload.getAdapter();

        // start with the previous value (the data adapter assures that there is one more entry in the data)
        int start = range.min;
        if (start > 0)
            start--;

        List<float[]> screenPoints = new ArrayList<>();
        for (int i = start; i < range.max + 1; i++) {
            float[] screenPoint = new float[2];
            screenPoint[0] = adapter.getX(i);
            screenPoint[1] = adapter.getY(i);
            payload.getRangeMatrix().mapPoints(screenPoint);
            screenPoints.add(screenPoint);
        }

        for (int i = 0; i < screenPoints.size() - 1; i++) {
            float[] point1 = screenPoints.get(i);
            float[] point2 = screenPoints.get(i + 1);
            bitmapCanvas.drawLine(point1[0], point1[1], point2[0], point2[1], drawConfig.getLinePaint());
        }

        for (int i = 0; i < screenPoints.size(); i++) {
            float[] point = screenPoints.get(i);
            pointRenderer.drawPoint(bitmapCanvas, new PointF(point[0], point[1]), drawConfig);
        }
    }
}

