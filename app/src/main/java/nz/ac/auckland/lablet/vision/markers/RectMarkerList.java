/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 */
package nz.ac.auckland.lablet.vision.markers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import nz.ac.auckland.lablet.vision.data.RectData;
import nz.ac.auckland.lablet.vision.data.RectDataList;


class RectMarker implements IMarker<RectData, RectMarkerList> {

    private final float LINE_WIDTH_DP = 2f;
    private float LINE_WIDTH;
    protected RectMarkerList parent = null;
    private RectData data;


    @Override
    public void setTo(RectMarkerList painter, RectData data) {
        this.parent = painter;
        this.data = data;
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
        PointF centreScreen = new PointF();
        parent.getContainerView().toScreen(data.getCentre(), centreScreen);
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

public class RectMarkerList extends MarkerList<RectDataList> {

    public final int MAX_DISPLAYED_MARKERS = 100;
    // boolean drawn = false;

    public RectMarkerList(RectDataList model) {
        super(model);

    }

//    public void setCurrentFrame(int frameId, @Nullable PointF insertHint) {
//        int index = dataList.getIndexByFrameId(frameId);
//        dataList.selectData(index);
//    }

    @Override
    public void onDraw(Canvas canvas) {

        if (this.getDataList().isVisible()) {
            int selectedFrame = dataList.getFrameDataList().getCurrentFrame();// .get .get //dataList.getSelectedData();

            for (int i = 0; i < dataList.size(); i++) {
                RectData data = dataList.getDataAt(i);

                if (data != null) {
                    int frameId = data.getFrameId();
                    IMarker marker = this.getMarker(i);
                    float priority = getPriority(selectedFrame, frameId, dataList.getFrameDataList().getNumberOfFrames());
                    marker.onDraw(canvas, priority);
                }
            }
        }
    }

    @Override
    protected IMarker createMarkerForFrame(int frameId) {
        return new RectMarker();
    }


}