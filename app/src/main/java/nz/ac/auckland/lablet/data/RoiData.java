package nz.ac.auckland.lablet.data;

import android.graphics.PointF;

/**
 * Created by jdip004 on 27/08/2015.
 */
public class RoiData extends Data {
    private PointData topLeft;// = new PointF();
    private PointData topRight;// = new PointF();
    private PointData btmLeft;// = new PointF();
    private PointData btmRight;// = new PointF();

    public RoiData(int frameId) {
        super(frameId);
        topLeft = new PointData(0); //Hacky, shouldn't need to specify frame id here
        topRight = new PointData(1);
        btmLeft = new PointData(2);
        btmRight = new PointData(3);
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
