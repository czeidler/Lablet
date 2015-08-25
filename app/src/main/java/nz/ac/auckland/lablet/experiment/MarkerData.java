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
 * Two dimensional data that is associated with an integer frameId.
 * <p>
 * The frameId can be used to identify the data in an experiment. For example, in case of the motion analysis, the frameId is
 * needed because not every frame may have a marker, e.g., when only every second frame is tagged. In this case the frameId
 * would be the frame frameId.
 * </p>
 */

public abstract class MarkerData {
    final private int frameId;

    public MarkerData(int id) {
        this.frameId = id;
    }

    public int getFrameId() {
        return frameId;
    }
}
