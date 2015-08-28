/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.markers;

import android.graphics.Canvas;
import android.view.MotionEvent;


/**
 * Interface for a drawable, selectable marker that can handle motion events.
 */
public interface IMarker<D, L> {
    public void setTo(L painter, D data);
    public void onDraw(Canvas canvas, float priority);
    public boolean handleActionDown(MotionEvent ev);
    public boolean handleActionUp(MotionEvent ev);
    public boolean handleActionMove(MotionEvent ev);
    public void setSelectedForDrag(boolean selectedForDrag);
    public boolean isSelectedForDrag();
    public void invalidate();
}
