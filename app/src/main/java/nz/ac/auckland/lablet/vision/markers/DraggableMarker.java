package nz.ac.auckland.lablet.vision.markers;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;

import nz.ac.auckland.lablet.vision.data.PointData;
import nz.ac.auckland.lablet.misc.DeviceIndependentPixel;
import nz.ac.auckland.lablet.misc.WeakListenable;
import nz.ac.auckland.lablet.views.plotview.PlotPainterContainerView;

/**
 * A selectable and draggable marker.
 * <p>
 * Once the marker is selected it can be dragged around. The marker has an area where it can be selected and an area
 * where it can be dragged. An example for an use-case it that the draggable area can be enabled once the marker has
 * been selected and otherwise is disabled.
 * </p>
 */
abstract class DraggableMarker extends WeakListenable<DraggableMarker.IListener> implements IMarker<PointData, DraggableMarkerList> {
    protected DraggableMarkerList parent = null;
    PointData data;
    protected PointF currentPosition;
    protected PointF dragOffset = new PointF(0, 0);
    protected boolean isSelectedForDragging = false;
    protected boolean isDragging = false;
    protected PlotPainterContainerView containerView;

    public interface IListener {
        public void onDraggedTo(DraggableMarker marker, PointF newPosition);
        public void onSelectedForDrag(DraggableMarker marker, boolean isSelected);
    }



    @Override
    public void setTo(DraggableMarkerList painter, PointData data) {
        this.parent = painter;
        this.containerView = painter.getContainerView();
        this.data = data;
    }

    /*
    * Used if using a marker without parent list
     */

    public void setTo(PlotPainterContainerView containerView, PointData data) {
        this.containerView = containerView;
        this.data = data;
    }

    /**
     * Handle action move down.
     *
     * @param event from the system
     * @return return true if the event has been handled, i.e., the marker has been touched in th drag area
     */
    public boolean handleActionDown(MotionEvent event) {
        PointF position = getCachedScreenPosition();

        PointF point = new PointF(event.getX(), event.getY());
        dragOffset.x = point.x - position.x;
        dragOffset.y = point.y - position.y;

        if (!isSelectedForDrag()) {
            if (isPointOnSelectArea(point))
                setSelectedForDrag(true);
            if (isSelectedForDrag() && isPointOnDragArea(point))
                isDragging = true;

            if (isSelectedForDrag() || isDragging)
                return true;
        } else if (isPointOnDragArea(point)) {
            isDragging = true;
            return true;
        }
        setSelectedForDrag(false);
        isDragging = false;
        return false;
    }

    /**
     * Handle action up events.
     *
     * @param event from the system
     * @return return true if the event has been handled, i.e., when the marker has been dragged
     */
    public boolean handleActionUp(MotionEvent event) {
        boolean wasDragging = isDragging;

        isDragging = false;

        if (wasDragging) {
            PointF point = this.getDragPoint(event);

            if (parent != null) {
                parent.markerMoveRequest(this, point, isDragging);
            } else {
                PointF newPosition = containerView.sanitizeScreenPoint(point);
                PointF newReal = new PointF();
                containerView.fromScreen(newPosition, newReal);
                this.data.setPosition(newReal);
            }
        }

        invalidate();

        return wasDragging;
    }

    /**
     * Handle action move events.
     *
     * @param event from the system
     * @return return true if the event has been handled, i.e., when the marker is selected/dragged
     */
    public boolean handleActionMove(MotionEvent event) {
        if (isDragging) {
            currentPosition = getDragPoint(event);
            onDraggedTo(currentPosition);
            return true;
        }
        return false;
    }

    private PointF getDragPoint(MotionEvent event) {
        PointF point = new PointF(event.getX(), event.getY());
        point.x -= dragOffset.x;
        point.y -= dragOffset.y;
        return point;
    }

    @Override
    public void setSelectedForDrag(boolean selectedForDrag) {
        this.isSelectedForDragging = selectedForDrag;

        if(parent != null) {
            parent.getMarkerPainterGroup().selectForDrag(this, parent);
        }

        for (IListener listener : getListeners())
            listener.onSelectedForDrag(this, selectedForDrag);
    }

    @Override
    public boolean isSelectedForDrag() {
        return isSelectedForDragging;
    }

    public PointF getCachedScreenPosition() {
        if (currentPosition == null)
            currentPosition = DraggableMarker.getScreenPosition(containerView, this.data);//parent.getScreenPosition(data);

        return currentPosition;
    }

    public static PointF getScreenPosition(PlotPainterContainerView containerView, PointData data) {
        PointF realPosition = data.getPosition();
        PointF screenPosition = new PointF();
        containerView.toScreen(realPosition, screenPosition);
        return screenPosition;
    }

    @Override
    public void invalidate() {
        currentPosition = null;
    }

    public PointF getTouchPosition() {
        PointF position = getCachedScreenPosition();
        position.x += dragOffset.x;
        position.y += dragOffset.y;
        return position;
    }

    /**
     * Notifies a derived class that the user performed a drag operation.
     *
     * @param point the new position the marker was dragged to
     */
    protected void onDraggedTo(PointF point) {
        if(this.parent != null) {
            parent.markerMoveRequest(this, point, true);
        }

        for (IListener listener : getListeners())
            listener.onDraggedTo(this, point);
    }

    /**
     * Check if a point is in the selectable are.
     *
     * @param screenPoint to be checked
     * @return true if the point is in the selectable area
     */
    abstract public boolean isPointOnSelectArea(PointF screenPoint);

    /**
     * Check if a point is in the draggable area of the marker.
     *
     * @param screenPoint to be checked
     * @return true if the point is in the drag area
     */
    protected boolean isPointOnDragArea(PointF screenPoint) {
        return isPointOnSelectArea(screenPoint);
    }

}
