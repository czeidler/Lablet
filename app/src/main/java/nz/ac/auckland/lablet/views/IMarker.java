/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.graphics.Canvas;
import android.view.MotionEvent;
import nz.ac.auckland.lablet.experiment.MarkerData;


/**
 * Interface for a drawable, selectable marker that can handle motion events.
 */
public interface IMarker {
    public void setTo(AbstractMarkerPainter painter, MarkerData markerData);

    public void onDraw(Canvas canvas, float priority);

    public boolean handleActionDown(MotionEvent ev);
    public boolean handleActionUp(MotionEvent ev);
    public boolean handleActionMove(MotionEvent ev);

    public void setSelectedForDrag(boolean selectedForDrag);
    public boolean isSelectedForDrag();

    public void invalidate();
}
