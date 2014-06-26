package nz.ac.auckland.lablet.views;

import android.graphics.PointF;


/**
 * Common interface for a experiment frame view.
 */
public interface IExperimentFrameView {
    public void setCurrentFrame(int frame);

    // convert a coordinate on the screen to the real value of the measurement
    public void fromScreen(PointF screen, PointF real);
    public void toScreen(PointF real, PointF screen);

    public float getMaxRawX();
    public float getMaxRawY();
}