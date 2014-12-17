/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;


interface IPointRenderer {
    public void drawPoint(Canvas canvas, PointF position, DrawConfig config);
}

public class XYConcurrentPainter extends ArrayConcurrentPainter {
    private DrawConfig drawConfig;
    private IPointRenderer pointRenderer = new CrossRenderer();

    public XYConcurrentPainter(CloneablePlotDataAdapter dataAdapter, Context context) {
        super(dataAdapter);

        this.drawConfig = new DrawConfig(context);
    }

    public DrawConfig getDrawConfig() {
        return drawConfig;
    }

    public IPointRenderer getPointRenderer() {
        return pointRenderer;
    }

    public void setPointRenderer(IPointRenderer pointRenderer) {
        this.pointRenderer = pointRenderer;
    }

    @Override
    protected RectF getRealDataRect(int startIndex, int lastIndex) {
        if (startIndex > 0)
            startIndex--;
        AbstractXYDataAdapter adapter = (AbstractXYDataAdapter)dataAdapter;
        RectF realDataRect = getContainerView().getRange();
        realDataRect.left = adapter.getX(startIndex).floatValue();
        realDataRect.right = adapter.getX(lastIndex).floatValue();
        if (realDataRect.width() < 40) {
            realDataRect.left -= 20;
            realDataRect.right += 20;
        }
        return realDataRect;
    }

    @Override
    protected Range getDataRangeFor(float left, float right) {
        AbstractXYDataAdapter xyDataAdapter = (AbstractXYDataAdapter)dataAdapter;
        return xyDataAdapter.getRange(left, right);
    }

    @Override
    protected void drawRange(Canvas bitmapCanvas, ArrayRenderPayload payload, Range range) {
        AbstractXYDataAdapter adapter = (AbstractXYDataAdapter)payload.getAdapter();

        List<float[]> screenPoints = new ArrayList<>();
        for (int i = range.min; i < range.max + 1; i++) {
            float[] screenPoint = new float[2];
            screenPoint[0] = adapter.getX(i).floatValue();
            screenPoint[1] = adapter.getY(i).floatValue();
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
