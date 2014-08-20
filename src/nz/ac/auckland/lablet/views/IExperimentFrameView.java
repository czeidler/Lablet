package nz.ac.auckland.lablet.views;

import android.graphics.PointF;
import android.graphics.RectF;


/**
 * Common interface for a experiment frame view.
 */
public interface IExperimentFrameView {
    public void setCurrentFrame(int frame);

    public RectF getDataRange();
}