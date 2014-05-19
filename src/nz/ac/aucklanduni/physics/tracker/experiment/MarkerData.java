/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.experiment;

import android.graphics.PointF;

public class MarkerData {
    private int runId;
    private PointF positionReal;

    public MarkerData(int run) {
        runId = run;
        positionReal = new PointF();
    }

    public int getRunId() {
        return runId;
    }

    public PointF getPosition() {
        return positionReal;
    }

    public void setPosition(PointF positionReal) {
        this.positionReal.set(positionReal);
    }
}
