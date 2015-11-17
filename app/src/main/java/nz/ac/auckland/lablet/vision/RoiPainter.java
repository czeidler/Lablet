/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import nz.ac.auckland.lablet.experiment.FrameDataModel;
import nz.ac.auckland.lablet.views.marker.*;
import nz.ac.auckland.lablet.views.plotview.PlotPainterContainerView;
import nz.ac.auckland.lablet.vision.data.RoiData;


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
        switch (index) {
            case LEFT_TOP:
                data.setTopLeft(point);
                break;
            case RIGHT_TOP:
                data.setTopRight(point);
                break;
            case LEFT_BOTTOM:
                data.setBtmLeft(point);
                break;
            case RIGHT_BOTTOM:
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

public class RoiPainter extends AbstractMarkerPainter<PointF> {
    // device independent sizes:
    private final float LINE_WIDTH_DP = 2f;

    // pixel sizes, set in the constructor
    private float LINE_WIDTH;

    final private FrameDataModel frameDataModel;

    public RoiPainter(RoiModel model, FrameDataModel frameDataModel) {
        super(model);

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
        return new SimpleMarker<>();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (getRoiData().getFrameId() != frameDataModel.getCurrentFrame())
            return;

        int priority = 1;


        //Set color and alpha
        int a, r, g, b;

        if (priority >= 0. && priority < 1.) {
            a = (int) (priority * 150.);
            r = 200;
            b = 200;
            g = 200;
        } else {
            a = 255;
            r = 0;
            b = 0;
            g = 255;
        }

        Paint paint = new Paint();
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setAntiAlias(true);
        paint.setARGB(a, r, g, b);
        paint.setStyle(Paint.Style.STROKE);

        RoiData data = getRoiData();
        RectF rect = containerView.toScreen(new RectF(data.getLeft(), data.getTop(), data.getRight(),
                data.getBottom()));
        canvas.drawRect(rect, paint);

        for (IMarker marker : markerList) {
            if (marker.isSelectedForDrag())
                marker.onDraw(canvas, 1);
        }
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
    }
}
