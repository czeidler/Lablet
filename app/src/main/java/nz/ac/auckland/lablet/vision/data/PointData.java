package nz.ac.auckland.lablet.vision.data;

import android.graphics.PointF;

/**
 * Created by jdip004 on 25/08/2015.
 */
public class PointData extends Data {
    private PointF positionReal = new PointF();

    public PointData(int frameId) {
        super(frameId);
    }

    public PointF getPosition() {
        return positionReal;
    }

    public void setPosition(PointF positionReal) {
        this.positionReal.set(positionReal);
    }
}