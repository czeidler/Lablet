package nz.ac.auckland.lablet.data;

import android.graphics.PointF;

/**
 * Created by jdip004 on 27/08/2015.
 */
public class RoiData extends Data {
    private PointData centre;// = new PointF();
    private PointData topLeft;// = new PointF();
    private PointData topRight;// = new PointF();
    private PointData btmLeft;// = new PointF();
    private PointData btmRight;// = new PointF();

    public RoiData(int frameId) {
        super(frameId);
        centre = new PointData(frameId);
        topLeft = new PointData(frameId); //Hacky, shouldn't need to specify frame id here
        topRight = new PointData(frameId);
        btmLeft = new PointData(frameId);
        btmRight = new PointData(frameId);
    }

    public PointData getCentre() {
        return centre;
    }

    public float getWidth()
    {
        return Math.abs(topRight.getPosition().x - topLeft.getPosition().x);
    }

    public float getHeight()
    {
        return Math.abs(topRight.getPosition().y - btmRight.getPosition().y);
    }

    public void setCentre(PointF centre) {
        this.centre.setPosition(centre);
    }

    public PointData getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(PointF topLeft) {
        this.topLeft.setPosition(topLeft);
    }

    public PointData getTopRight() {
        return topRight;
    }

    public void setTopRight(PointF topRight) {
        this.topRight.setPosition(topRight);
    }

    public PointData getBtmLeft() {
        return btmLeft;
    }

    public void setBtmLeft(PointF btmLeft) {
        this.btmLeft.setPosition(btmLeft);
    }

    public PointData getBtmRight() {
        return btmRight;
    }

    public void setBtmRight(PointF btmRight) {
        this.btmRight.setPosition(btmRight);
    }
}
