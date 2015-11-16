/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.marker;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;


/**
 * Default implementation of a draggable marker.
 */
public class SimpleMarker<T> extends DraggableMarker<T> {
    // device independent pixels
    private class Const {
        static public final float INNER_RING_RADIUS_DP = 30;
        static public final float INNER_RING_WIDTH_DP = 2;
        static public final float RING_RADIUS_DP = 100;
        static public final float RING_WIDTH_DP = 40;
    }

    final public static int MARKER_COLOR = Color.argb(255, 100, 200, 20);
    final public static int DRAG_HANDLE_COLOR = Color.argb(100, 0, 200, 100);

    private float INNER_RING_RADIUS;
    private float INNER_RING_WIDTH;
    private float RING_RADIUS;
    private float RING_WIDTH;

    private Paint paint = null;
    private int mainAlpha = 255;

    public SimpleMarker() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    public void setTo(AbstractMarkerPainter<T> painter, int index) {
        super.setTo(painter, index);

        INNER_RING_RADIUS = parent.toPixel(Const.INNER_RING_RADIUS_DP);
        INNER_RING_WIDTH = parent.toPixel(Const.INNER_RING_WIDTH_DP);
        RING_RADIUS = parent.toPixel(Const.RING_RADIUS_DP);
        RING_WIDTH = parent.toPixel(Const.RING_WIDTH_DP);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        PointF position = getCachedScreenPosition();

        if (priority >= 0. && priority <= 1.)
            mainAlpha = (int)(priority * 255.);
        else
            mainAlpha = 255;

        float crossR = INNER_RING_RADIUS / 1.41421356237f;
        paint.setColor(makeColor(100, 20, 20, 20));
        paint.setStrokeWidth(1);
        canvas.drawLine(position.x - crossR, position.y - crossR, position.x + crossR, position.y + crossR, paint);
        canvas.drawLine(position.x + crossR, position.y - crossR, position.x - crossR, position.y + crossR, paint);

        if (priority == 1.)
            paint.setColor(MARKER_COLOR);
        else
            paint.setColor(makeColor(255, 200, 200, 200));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(INNER_RING_WIDTH);
        canvas.drawCircle(position.x, position.y, INNER_RING_RADIUS, paint);

        if (isSelectedForDrag()) {
            paint.setColor(DRAG_HANDLE_COLOR);
            paint.setStrokeWidth(RING_WIDTH);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(position.x, position.y, RING_RADIUS, paint);
        }
    }

    @Override
    public boolean isPointOnSelectArea(PointF screenPoint) {
        PointF position = getCachedScreenPosition();
        float distance = (float)Math.sqrt(Math.pow(screenPoint.x - position.x, 2) + Math.pow(screenPoint.y - position.y, 2));
        return distance <= INNER_RING_RADIUS;
    }

    @Override
    protected boolean isPointOnDragArea(PointF screenPoint) {
        PointF position = getCachedScreenPosition();
        float distance = (float)Math.sqrt(Math.pow(screenPoint.x - position.x, 2) + Math.pow(screenPoint.y - position.y, 2));
        if (distance < RING_RADIUS + RING_WIDTH / 2)
            return true;
        return isPointOnSelectArea(screenPoint);
    }

    protected int makeColor(int alpha, int red, int green, int blue) {
        int finalAlpha = composeAlpha(alpha, mainAlpha);
        return Color.argb(finalAlpha, red, green, blue);
    }

    protected int makeColor(int color) {
        int finalAlpha = composeAlpha(Color.alpha(color), mainAlpha);
        return Color.argb(finalAlpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int composeAlpha(int alpha1, int alpha2) {
        float newAlpha = (float)(alpha1 * alpha2) / 255;
        return (int)newAlpha;
    }
}
