/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.markers;

import android.graphics.*;

import nz.ac.auckland.lablet.data.Data;
import nz.ac.auckland.lablet.data.PointData;
import nz.ac.auckland.lablet.data.PointDataList;

import java.text.DecimalFormat;
import java.util.List;


abstract class CursorMarker extends PointMarker {
    final private Paint cursorPaint = new Paint();
    final private Paint textPaint = new Paint();
    final private Paint textBackgroundPaint = new Paint();

    // device independent pixels
    private class Const {
        static public final float SELECT_RADIUS_DP = 30;
    }

    private float SELECT_RADIUS;

    @Override
    public void setTo(DraggableMarkerList painter, PointData data) {
        super.setTo(painter, data);

        SELECT_RADIUS = parent.toPixel(Const.SELECT_RADIUS_DP);
    }

    @Override
    public boolean isPointOnSelectArea(PointF screenPoint) {
        PointF position = getCachedScreenPosition();
        float distance = Math.abs(getDirection(screenPoint) - getDirection(position));
        return distance <= SELECT_RADIUS;
    }

    @Override
    protected boolean isPointOnDragArea(PointF screenPoint) {
        PointF position = getCachedScreenPosition();
        float distance = Math.abs(getDirection(screenPoint)- getDirection(position));
        if (distance < SELECT_RADIUS)
            return true;
        return false;
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        PointF start = getStartPoint();
        PointF end = getEndPoint();
        cursorPaint.setStyle(Paint.Style.STROKE);
        cursorPaint.setStrokeWidth(1);
        if (isSelectedForDrag())
            cursorPaint.setColor(Color.YELLOW);
        else
            cursorPaint.setColor(Color.GREEN);
        canvas.drawLine(start.x, start.y, end.x, end.y, cursorPaint);

        if (!isSelectedForDrag())
            return;

        // draw position label
        textPaint.setColor(Color.BLACK);
        textPaint.setLinearText(true);
        textBackgroundPaint.setColor(Color.argb(100, 255, 255, 255));
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        final PointF realPosition = new PointF();
        parent.getContainerView().fromScreen(getCachedScreenPosition(), realPosition);
        PointF textPosition = new PointF(start.x, start.y);
        textPosition.offset(2, -2);

        CursorMarkerList cursorDataListPainter = (CursorMarkerList)parent;
        final String positionString = "Position: " + new DecimalFormat(
                cursorDataListPainter.getPositionDecimalFormat()).format(getDirection(realPosition));
        final Rect textBounds = new Rect();
        textPaint.getTextBounds(positionString, 0, positionString.length(), textBounds);
        textBounds.offset((int)textPosition.x, (int)textPosition.y);
        canvas.drawRect(textBounds, textBackgroundPaint);
        canvas.drawText(positionString, textPosition.x, textPosition.y,
                textPaint);
    }

    abstract protected float getDirection(PointF point);
    abstract protected void offsetPoint(Point point, float offset);
    abstract protected PointF getStartPoint();
    abstract protected PointF getEndPoint();
}



abstract public class CursorMarkerList extends DraggableMarkerList {
    private String positionDecimalFormat = "#";

    public void setPositionDecimalFormat(String positionDecimalFormat) {
        this.positionDecimalFormat = positionDecimalFormat;
    }

    public String getPositionDecimalFormat() {
        return positionDecimalFormat;
    }

    public CursorMarkerList(PointDataList data) {
        super(data);

        getMarkerPainterGroup().setSelectOnDrag(true);
    }

    @Override
    public List<IMarker> getSelectableMarkerList() {
        return painterList;
    }

    @Override
    public void onDraw(Canvas canvas) {
        IMarker selectedMarker = null;
        for (int i = 0; i < painterList.size(); i++) {
            IMarker marker = painterList.get(i);
            if (marker.isSelectedForDrag()) {
                selectedMarker = marker;
                continue;
            }
            marker.onDraw(canvas, 1);
        }

        // draw selected marker on the top
        if (selectedMarker != null)
            selectedMarker.onDraw(canvas, 1);
    }

    @Override
    public void markerMoveRequest(DraggableMarker marker, PointF newPosition, boolean isDragging) {
        super.markerMoveRequest(marker, newPosition, isDragging);

        int selectedMarkerId = -1;
        if (marker.isSelectedForDrag())
            selectedMarkerId = dataList.getDataAt(painterList.indexOf(marker)).getFrameId();

        if (!isDragging)
            sort();

        if (selectedMarkerId >= 0) {
            for (int i = 0; i < dataList.getDataCount(); i++) {
                Data data = dataList.getDataAt(i);
                if (data.getFrameId() == selectedMarkerId) {
                    if (!painterList.get(i).isSelectedForDrag())
                        painterList.get(i).setSelectedForDrag(true);
                    break;
                }
            }
        }
    }

    abstract public void sort();
}


