package nz.ac.auckland.lablet.vision.data;

import android.graphics.PointF;
import nz.ac.auckland.lablet.views.marker.MarkerData;


public class RoiData {
    private float left;
    private float top;
    private float right;
    private float bottom;
    private MarkerData markerData;

    public RoiData(MarkerData markerData) {
        this.markerData = markerData;
    }

    public float getWidth() {
        return right - left;
    }

    public float getHeight() {
        return top - bottom;
    }

    private PointF getPointData(float x, float y) {
        return new PointF(x, y);
    }

    public MarkerData getMarkerData() {
        return markerData;
    }

    public PointF getTopLeft() {
        return getPointData(left, top);
    }

    public void setTopLeft(PointF topLeft) {
        this.left = topLeft.x;
        this.top = topLeft.y;
    }

    public PointF getTopRight() {
        return getPointData(right, top);
    }

    public void setTopRight(PointF topRight) {
        this.right = topRight.x;
        this.top = topRight.y;
    }

    public PointF getBtmLeft() {
        return getPointData(left, bottom);
    }

    public void setBtmLeft(PointF btmLeft) {
        this.left = btmLeft.x;
        this.bottom = btmLeft.y;
    }

    public PointF getBtmRight() {
        return getPointData(right, bottom);
    }

    public void setBtmRight(PointF btmRight) {
        this.right = btmRight.x;
        this.bottom = btmRight.y;
    }

    public float getLeft() {
        return left;
    }

    public float getTop() {
        return top;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    public int getFrameId() {
        return markerData.getId();
    }
}
