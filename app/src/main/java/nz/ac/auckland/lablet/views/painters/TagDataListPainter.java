/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.painters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;

import nz.ac.auckland.lablet.data.PointData;
import nz.ac.auckland.lablet.data.PointDataList;

import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of a draggable marker.
 */
class TagDataPainter extends DraggableDataPainter {
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

    public TagDataPainter() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    public void setTo(DraggableDataListPainter painter, PointData data) {
        super.setTo(painter, data);

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

/**
 * Painter for tagged data. For example, the tagged data from a camera experiment.
 */
public class TagDataListPainter extends DraggableDataListPainter {
    private int MAX_DISPLAYED_MARKERS = 100;

    private LastInsertMarkerManager lastInsertMarkerManager = new LastInsertMarkerManager();

    public TagDataListPainter(PointDataList data) {
        super(data);
    }

    @Override
    public List<IDataPainter> getSelectableMarkerList() {
        List<IDataPainter> selectableMarkers = new ArrayList<>();
        IDataPainter selectedMarker = painterList.get(dataList.getSelectedData());
        selectableMarkers.add(selectedMarker);
        return selectableMarkers;
    }

    public IDataPainter getDataPainterAtScreenPosition(PointF screenPosition) {
        int currentMarkerRow = dataList.getSelectedData();

        int start = currentMarkerRow - MAX_DISPLAYED_MARKERS / 2 + 1;
        if (start < 0)
            start = 0;
        int end = currentMarkerRow + MAX_DISPLAYED_MARKERS / 2 + 1;
        if (end > painterList.size())
            end = painterList.size();

        for (int i = start; i < end; i++) {
        IDataPainter marker = painterList.get(i);
            if (!(marker instanceof DraggableDataPainter))
                continue;
            if (((DraggableDataPainter)marker).isPointOnSelectArea(screenPosition))
                return marker;
        }
        return null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int currentMarkerRow = dataList.getSelectedData();
        IDataPainter topMarker = getPainterForFrame(currentMarkerRow);

        int start = currentMarkerRow - MAX_DISPLAYED_MARKERS / 2 + 1;
        if (start < 0)
            start = 0;
        int end = currentMarkerRow + MAX_DISPLAYED_MARKERS / 2 + 1;
        if (end > painterList.size())
            end = painterList.size();

        for (int i = start; i < end; i++) {
            IDataPainter marker = painterList.get(i);
            if (marker == topMarker)
                continue;

            float runDistance = Math.abs(currentMarkerRow - i);
            float currentPriority = (float)(0.35 - 0.1 * runDistance);
            if (currentPriority > 1.0)
                currentPriority = (float)1.0;
            if (currentPriority < 0.1)
                currentPriority = (float)0.1;

            marker.onDraw(canvas, currentPriority);
        }
        if (topMarker != null)
            topMarker.onDraw(canvas, (float)1.0);
    }

    @Override
    protected DraggableDataPainter createPainterForFrame(int frameId) {
        return new TagDataPainter();
    }

    /**
     * If the last inserted marker hasn't moved remove it again.
     */
    private class LastInsertMarkerManager {
        private int markerInsertedInLastRun = -1;
        private PointF lastMarkerPosition = new PointF();

        void onCurrentFrameChanging(PointDataList markersDataModel) {
            // Index could be out of bounds, e.g., when the marker data has been cleared.
            if (markerInsertedInLastRun >= dataList.getDataCount()) {
                markerInsertedInLastRun =-1;
                return;
            }

            if (markerInsertedInLastRun >= 0) {
                PointData lastMarkerData = dataList.getDataAt(markerInsertedInLastRun);
                if (lastMarkerData.getPosition().equals(lastMarkerPosition)) {
                    dataList.removeData(markerInsertedInLastRun);
                    int selectedIndex = markerInsertedInLastRun - 1;
                    if (selectedIndex < 0)
                        selectedIndex = 0;
                    markersDataModel.selectData(selectedIndex);
                }
                markerInsertedInLastRun = -1;
            }
        }

        void onNewMarkerInserted(int index, PointData data) {
            markerInsertedInLastRun = index;
            lastMarkerPosition.set(data.getPosition());
        }
    }

    public void setCurrentFrame(int frame, @Nullable PointF insertHint) {
        lastInsertMarkerManager.onCurrentFrameChanging(dataList);

        // check if we have the run in the data list
        PointData data = null;
        int index = dataList.findDataByRun(frame);
        if (index >= 0) {
            data = dataList.getDataAt(index);
            dataList.selectData(index);
        }

        if (data == null) {
            data = new PointData(frame);
            if (insertHint != null) {
                sanitizeScreenPoint(insertHint);
                PointF insertHintReal = new PointF();
                containerView.fromScreen(insertHint, insertHintReal);
                data.setPosition(insertHintReal);
            } else {
                if (dataList.getDataCount() > 0 && dataList.getSelectedData() > 0) {
                    int selectedIndex = dataList.getSelectedData();
                    PointData prevData = dataList.getDataAt(selectedIndex);
                    data.setPosition(prevData.getPosition());
                    data.getPosition().x += 5;

                    // sanitize the new marker position
                    PointF screenPos = new PointF();
                    containerView.toScreen(data.getPosition(), screenPos);
                    sanitizeScreenPoint(screenPos);
                    containerView.fromScreen(screenPos, data.getPosition());
                } else {
                    // center the first marker
                    PointF initPosition = new PointF();
                    initPosition.x = (containerView.getRangeRight() - containerView.getRangeLeft()) * 0.5f;
                    initPosition.y = (containerView.getRangeTop() - containerView.getRangeBottom()) * 0.5f;
                    data.setPosition(initPosition);
                }
            }

            int newIndex = dataList.addData(data);
            dataList.selectData(newIndex);

            lastInsertMarkerManager.onNewMarkerInserted(newIndex, data);
        }
    }
}
