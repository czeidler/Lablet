/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 */
package nz.ac.auckland.lablet.vision;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import nz.ac.auckland.lablet.experiment.FrameDataModel;
import nz.ac.auckland.lablet.views.marker.AbstractMarkerPainter;
import nz.ac.auckland.lablet.views.marker.IMarker;
import nz.ac.auckland.lablet.vision.data.RectData;
import nz.ac.auckland.lablet.vision.data.RectDataList;


class RectMarker implements IMarker<AbstractMarkerPainter<RectData>> {
    private final float LINE_WIDTH_DP = 2f;
    private float LINE_WIDTH;
    protected RectMarkerListPainter parent = null;
    private int markerIndex;

    @Override
    public void setTo(AbstractMarkerPainter<RectData> painter, int index) {
        this.parent = (RectMarkerListPainter)painter;
        this.markerIndex = index;

        LINE_WIDTH = painter.getContainerView().toPixel(LINE_WIDTH_DP);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
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

        // Line settings
        Paint paint = new Paint();
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setAntiAlias(true);
        paint.setARGB(a, r, g, b);
        paint.setStyle(Paint.Style.STROKE);

        //Draw rotated rectangle
        canvas.save();

        //Convert centre to screen position
        RectData data = parent.getMarkerModel().getAt(markerIndex);
        PointF centreScreen = parent.getMarkerScreenPosition(markerIndex);
        canvas.rotate(data.getAngle(), centreScreen.x, centreScreen.y);

        //Convert width and height to screen size, kinda hacky
        PointF size = new PointF();
        parent.getContainerView().toScreen(new PointF(data.getWidth(), data.getHeight()), size);

        float left = centreScreen.x - size.x / 2;
        float right = centreScreen.x + size.x / 2;
        float top = centreScreen.y - size.y / 2;
        float bottom = centreScreen.y + size.y / 2;

        canvas.drawRect(left, top, right, bottom, paint);
        canvas.restore();
    }

    @Override
    public boolean handleActionDown(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean handleActionUp(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean handleActionMove(MotionEvent ev) {
        return false;
    }

    @Override
    public void setSelectedForDrag(boolean selectedForDrag) {

    }

    @Override
    public boolean isSelectedForDrag() {
        return false;
    }

    @Override
    public void invalidate() {

    }
}

public class RectMarkerListPainter extends AbstractMarkerPainter<RectData> {
    final private FrameDataModel frameDataModel;
    final public int MAX_DISPLAYED_MARKERS = 10;

    public RectMarkerListPainter(RectDataList model, FrameDataModel frameDataModel) {
        super(model);

        this.frameDataModel = frameDataModel;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!((RectDataList)markerData).isVisible())
            return;

        int selectedFrame = frameDataModel.getCurrentFrame();

        for (int i = 0; i < markerData.size(); i++) {
            RectData data = markerData.getAt(i);

            if (data != null && data.getFrameId() <= selectedFrame) {
                int frameId = data.getFrameId();
                nz.ac.auckland.lablet.views.marker.IMarker marker = markerList.get(i);
                float priority = getPriority(selectedFrame, frameId, MAX_DISPLAYED_MARKERS);
                marker.onDraw(canvas, priority);
            }
        }
    }

    static public float getPriority(int selectedFrameId, int markerFrameId, int maxDisplayedMarkers) {
        float priority;
        int distance = markerFrameId - selectedFrameId;

        if (selectedFrameId == markerFrameId)
            priority = 1;
        else if (distance < 0 && distance >= -maxDisplayedMarkers)
            priority = (float)(maxDisplayedMarkers - Math.abs(distance)) / maxDisplayedMarkers;
        else
            priority = 0;

        return priority;
    }

    @Override
    protected IMarker<AbstractMarkerPainter<RectData>> createMarkerForRow(int row) {
        return new RectMarker();
    }
}