package nz.ac.auckland.lablet.data;

import android.graphics.PointF;

/**
 * Created by jdip004 on 25/08/2015.
 */

public class RectData extends Data {
    private PointF centre = new PointF();
    private float width = 0;
    private float height = 0;
    private float angle = 0;

    public RectData(int frameId) {
        super(frameId);
    }

    public PointF getCentre() {
        return centre;
    }

    public void setCentre(PointF centre) {
        this.centre = centre;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}