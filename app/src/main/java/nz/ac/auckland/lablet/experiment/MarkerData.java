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
 * Two dimensional data that is associated with an integer id.
 * <p>
 * The id can be used to identify the data in an experiment. For example, in case of the motion analysis, the id is
 * needed because not every frame may have a marker, e.g., when only every second frame is tagged. In this case the id
 * would be the frame id.
 * </p>
 */
public class MarkerData {
    final private int id;
    private PointF positionReal;

    public MarkerData(int id) {
        this.id = id;
        positionReal = new PointF();
    }

    public int getId() {
        return id;
    }

    public PointF getPosition() {
        return positionReal;
    }

    public void setPosition(PointF positionReal) {
        this.positionReal.set(positionReal);
    }
}
