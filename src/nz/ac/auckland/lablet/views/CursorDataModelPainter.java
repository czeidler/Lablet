/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.graphics.*;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;

import java.util.List;


abstract class CursorMarker extends DraggableMarker {
    final private Paint cursorPaint = new Paint();
    final private Paint dragPaint = new Paint();

    // device independent pixels
    private class Const {
        static public final float SELECT_RADIUS_DP = 30;
        static public final float DRAG_RADIUS_DP = 50;
        static public final float DRAG_LINE_WIDTH_DP = 40;
    }

    private float SELECT_RADIUS;
    private float DRAG_RADIUS;
    private float DRAG_LINE_WIDTH;

    @Override
    public void setTo(AbstractMarkerPainter painter, int markerIndex) {
        super.setTo(painter, markerIndex);

        SELECT_RADIUS = parent.toPixel(Const.SELECT_RADIUS_DP);
        DRAG_RADIUS = parent.toPixel(Const.DRAG_RADIUS_DP);
        DRAG_LINE_WIDTH = parent.toPixel(Const.DRAG_LINE_WIDTH_DP);

        cursorPaint.setColor(Color.BLACK);
        cursorPaint.setStyle(Paint.Style.STROKE);

        dragPaint.setColor(SimpleMarker.DRAG_HANDLE_COLOR);
        dragPaint.setStrokeWidth(DRAG_LINE_WIDTH);
        dragPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected boolean isPointOnSelectArea(PointF point) {
        PointF position = getCachedScreenPosition();
        float distance = Math.abs(getDirection(point) - getDirection(position));
        return distance <= SELECT_RADIUS;
    }

    @Override
    protected boolean isPointOnDragArea(PointF point) {
        PointF position = getCachedScreenPosition();
        float distance = Math.abs(getDirection(point)- getDirection(position));
        if (distance < DRAG_RADIUS)
            return true;
        return false;
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        Point start = getStartPoint();
        Point end = getEndPoint();
        cursorPaint.setStrokeWidth(2);
        canvas.drawLine(start.x, start.y, end.x, end.y, cursorPaint);

        if (!isSelectedForDrag())
            return;

        final PointF realPosition = new PointF();
        parent.getContainerView().fromScreen(getCachedScreenPosition(), realPosition);
        cursorPaint.setStrokeWidth(1);
        canvas.drawText(String.format("Position: %d", (int)getDirection(realPosition)), start.x, start.y, cursorPaint);

        offsetPoint(start, DRAG_RADIUS);
        offsetPoint(end, DRAG_RADIUS);
        canvas.drawLine(start.x, start.y, end.x, end.y, dragPaint);

        offsetPoint(start, -2 * DRAG_RADIUS);
        offsetPoint(end, -2 * DRAG_RADIUS);
        canvas.drawLine(start.x, start.y, end.x, end.y, dragPaint);
    }

    abstract protected float getDirection(PointF point);
    abstract protected void offsetPoint(Point point, float offset);
    abstract protected Point getStartPoint();
    abstract protected Point getEndPoint();
}

class HCursorMarker extends CursorMarker {
    @Override
    protected float getDirection(PointF point) {
        return point.y;
    }

    @Override
    protected void offsetPoint(Point point, float offset) {
        point.y += offset;
    }

    @Override
    protected Point getStartPoint() {
        return new Point(0, (int)getCachedScreenPosition().y);
    }

    @Override
    protected Point getEndPoint() {
        return new Point(parent.getScreenRect().width(), (int)getCachedScreenPosition().y);
    }
}


class VCursorMarker extends CursorMarker {
    @Override
    protected float getDirection(PointF point) {
        return point.x;
    }

    @Override
    protected void offsetPoint(Point point, float offset) {
        point.x += offset;
    }

    @Override
    protected Point getStartPoint() {
        return new Point((int)getCachedScreenPosition().x, parent.getScreenRect().height());
    }

    @Override
    protected Point getEndPoint() {
        return new Point((int)getCachedScreenPosition().x, 0);
    }
}


abstract public class CursorDataModelPainter extends AbstractMarkerPainter {

    public CursorDataModelPainter(MarkerDataModel data) {
        super(data);
    }

    @Override
    public List<IMarker> getSelectableMarkerList() {
        return markerList;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < markerList.size(); i++) {
            IMarker marker = markerList.get(i);
            marker.onDraw(canvas, 1);
        }
    }

    @Override
    public void markerMoveRequest(DraggableMarker marker, PointF newPosition, boolean isDragging) {
        super.markerMoveRequest(marker, newPosition, isDragging);

        if (!isDragging)
            markerData.sortYAscending();
    }
}


