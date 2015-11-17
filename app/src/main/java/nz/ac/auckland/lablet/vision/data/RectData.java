/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 */
package nz.ac.auckland.lablet.vision.data;

import android.graphics.PointF;


public class RectData {
    final private int frameId;
    private PointF centre = new PointF();
    private float width = 0;
    private float height = 0;
    private float angle = 0;

    public RectData(int frameId) {
        this.frameId = frameId;
    }

    public int getFrameId() {
        return frameId;
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