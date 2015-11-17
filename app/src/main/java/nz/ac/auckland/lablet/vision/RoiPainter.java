/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision;

import android.graphics.*;
import nz.ac.auckland.lablet.experiment.FrameDataModel;
import nz.ac.auckland.lablet.views.marker.*;
import nz.ac.auckland.lablet.views.plotview.PlotPainterContainerView;
import nz.ac.auckland.lablet.vision.data.RoiData;

import java.util.ArrayList;
import java.util.List;


class RoiModel extends AbstractPointDataModel<PointF> {
    final private RoiData data;

    final private int LEFT_TOP = 0;
    final private int RIGHT_TOP = 1;
    final private int LEFT_BOTTOM = 2;
    final private int RIGHT_BOTTOM = 3;

    public RoiModel(RoiData data) {
        this.data = data;
    }

    public RoiData getRoiData() {
        return data;
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public PointF getAt(int index) {
        return getPosition(index);
    }

    @Override
    public PointF getPosition(PointF data) {
        return data;
    }

    @Override
    public PointF getPosition(int index) {
        switch (index) {
            case LEFT_TOP:
                return new PointF(data.getLeft(), data.getTop());
            case RIGHT_TOP:
                return new PointF(data.getRight(), data.getTop());
            case LEFT_BOTTOM:
                return new PointF(data.getLeft(), data.getBottom());
            case RIGHT_BOTTOM:
                return new PointF(data.getRight(), data.getBottom());
        }
        return null;
    }

    @Override
    public void setPositionNoNotify(PointF point, int index) {
        final float MIN = 1.5f;
        switch (index) {
            case LEFT_TOP:
                if (point.x > data.getRight() - MIN)
                    point.x = data.getRight() - MIN;
                if (point.y < data.getBottom() + MIN)
                    point.y = data.getBottom() + MIN;
                data.setTopLeft(point);
                break;
            case RIGHT_TOP:
                if (point.x < data.getLeft() + MIN)
                    point.x = data.getLeft() + MIN;
                if (point.y < data.getBottom() + MIN)
                    point.y = data.getBottom() + MIN;
                data.setTopRight(point);
                break;
            case LEFT_BOTTOM:
                if (point.x > data.getRight() - MIN)
                    point.x = data.getRight() - MIN;
                if (point.y > data.getTop() - MIN)
                    point.y = data.getTop() - MIN;
                data.setBtmLeft(point);
                break;
            case RIGHT_BOTTOM:
                if (point.x < data.getLeft() + MIN)
                    point.x = data.getLeft() + MIN;
                if (point.y > data.getTop() - MIN)
                    point.y = data.getTop() - MIN;
                data.setBtmRight(point);
                break;
        }
    }

    @Override
    protected int addDataNoNotify(PointF data) {
        return -1;
    }

    @Override
    protected PointF removeDataNoNotify(int index) {
        return null;
    }

    @Override
    protected void clearNoNotify() {

    }
}

class RoiMarker extends SimpleMarker<PointF> {
    final private Paint paint = new Paint();

    public RoiMarker() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (isSelectedForDrag()) {
            super.onDraw(canvas, priority);
            return;
        }
        PointF point = parent.getMarkerScreenPosition(markerIndex);
        RoiPainter.setColor(paint, priority);
        canvas.drawCircle(point.x, point.y, 5, paint);
    }
}

public class RoiPainter extends AbstractMarkerPainter<PointF> {
    final public int MAX_DISPLAYED_MARKERS = 10;

    // device independent sizes:
    private final float LINE_WIDTH_DP = 2f;

    // pixel sizes, set in the constructor
    private float LINE_WIDTH;

    final private MarkerDataModel markerDataModel;
    final private FrameDataModel frameDataModel;

    public RoiPainter(RoiModel model, MarkerDataModel markerDataModel, FrameDataModel frameDataModel) {
        super(model);

        this.markerDataModel = markerDataModel;
        this.frameDataModel = frameDataModel;
    }

    @Override
    public void setContainer(PlotPainterContainerView view) {
        super.setContainer(view);

        if (view != null)
            LINE_WIDTH = view.toPixel(LINE_WIDTH_DP);
    }

    public RoiData getRoiData() {
        return ((RoiModel)markerData).getRoiData();
    }

    @Override
    protected IMarker<AbstractMarkerPainter<PointF>> createMarkerForRow(int row) {
        return new RoiMarker();
    }

    static public void setColor(Paint paint, float priority) {
        if (priority >= 0. && priority < 1.)
            paint.setARGB((int) (priority * 150.), 200, 200, 200);
        else
            paint.setARGB(255, 0, 255, 0);
    }

    @Override
    public void onDraw(Canvas canvas) {
        float priority = RectMarkerListPainter.getPriority(frameDataModel.getCurrentFrame(), getRoiData().getFrameId(),
                MAX_DISPLAYED_MARKERS);

        Paint paint = new Paint();
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setAntiAlias(true);
        setColor(paint, priority);
        paint.setStyle(Paint.Style.STROKE);

        RoiData data = getRoiData();
        RectF rect = containerView.toScreen(new RectF(data.getLeft(), data.getTop(), data.getRight(),
                data.getBottom()));
        canvas.drawRect(rect, paint);

        for (IMarker marker : markerList)
            marker.onDraw(canvas, priority);
    }

    @Override
    public void markerMoveRequest(DraggableMarker<PointF> marker, PointF newPosition, boolean isDragging) {
        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;

        sanitizeScreenPoint(newPosition);

        PointF newReal = new PointF();
        containerView.fromScreen(newPosition, newReal);
        markerData.setPosition(newReal, row);

        // update marker position
        RoiData data = getRoiData();
        int markerIndex = markerDataModel.findMarkerDataById(data.getFrameId());
        if (markerIndex < 0)
            return;
        markerDataModel.setPosition(data.getCenter(), markerIndex);
    }

    @Override
    public List<IMarker> getSelectableMarkerList() {
        if (getRoiData().getFrameId() != frameDataModel.getCurrentFrame())
            return new ArrayList<>();
        return super.getSelectableMarkerList();
    }
}
