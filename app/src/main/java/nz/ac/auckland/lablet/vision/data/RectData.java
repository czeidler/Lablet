package nz.ac.auckland.lablet.vision.data;

import android.graphics.PointF;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

import nz.ac.auckland.lablet.camera.VideoData;

/**
 * Created by jdip004 on 25/08/2015.
 */

public class RectData extends Data {
    private PointF centre = new PointF();
    private float width = 0;
    private float height = 0;
    private float angle = 0;

    public Rect getRect(VideoData videoData)
    {
        PointF sizeScreen = videoData.toVideoPoint(new PointF(width, height));
        PointF centreScreen = videoData.toVideoPoint(centre);

        RotatedRect rect = new RotatedRect(new Point(centreScreen.x, centreScreen.y), new Size(sizeScreen.x, sizeScreen.y), angle);
        return rect.boundingRect();
    }

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