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
        // prepare points
        AbstractXYDataAdapter adapter = (AbstractXYDataAdapter)payload.getAdapter();
        List<PointF> screenPoints = new ArrayList<>();
        for (int i = range.min; i < range.max + 1; i++) {
            if (i >= adapter.getSize())
                break;
            float[] screenPoint = new float[2];
            screenPoint[0] = adapter.getX(i).floatValue();
            screenPoint[1] = adapter.getY(i).floatValue();
            payload.getRangeMatrix().mapPoints(screenPoint);
            screenPoints.add(new PointF(screenPoint[0], screenPoint[1]));
        }

        // draw the lines
        for (int i = 0; i < screenPoints.size() - 1; i++) {
            PointF point1 = screenPoints.get(i);
            PointF point2 = screenPoints.get(i + 1);
            bitmapCanvas.drawLine(point1.x, point1.y, point2.x, point2.y, drawConfig.getLinePaint());
        }
        // draw points
        for (int i = 0; i < screenPoints.size(); i++) {
            PointF point = screenPoints.get(i);
            pointRenderer.drawPoint(bitmapCanvas, point, drawConfig);
        }
    }
}
