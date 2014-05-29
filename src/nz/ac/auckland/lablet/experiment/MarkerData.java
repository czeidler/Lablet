/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.graphics.PointF;

/**
 * Two dimensional data that is associated with an integer run id.
 */
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
