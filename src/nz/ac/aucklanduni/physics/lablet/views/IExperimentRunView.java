package nz.ac.aucklanduni.physics.lablet.views;

import android.graphics.PointF;


/**
 * Common interface for a experiment run view.
 */
public interface IExperimentRunView {
    public void setCurrentRun(int run);

    // convert a coordinate on the screen to the real value of the measurement
    public void fromScreen(PointF screen, PointF real);
    public void toScreen(PointF real, PointF screen);

    public float getMaxRawX();
    public float getMaxRawY();
}