package nz.ac.auckland.lablet.experiment;

import android.graphics.PointF;

/**
 * Created by jdip004 on 25/08/2015.
 */

public class RotatedRectData extends MarkerData {
    private PointF centre = new PointF();
    private int width = 0;
    private int height = 0;
    private float angle = 0;

    public RotatedRectData(int frameId) {
        super(frameId);
    }

    public PointF getCentre() {
        return centre;
    }

    public void setCentre(PointF centre) {
        this.centre = centre;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}