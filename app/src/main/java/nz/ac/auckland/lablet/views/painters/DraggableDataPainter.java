package nz.ac.auckland.lablet.views.painters;

import android.graphics.PointF;
import android.view.MotionEvent;

import nz.ac.auckland.lablet.data.PointData;

/**
 * A selectable and draggable marker.
 * <p>
 * Once the marker is selected it can be dragged around. The marker has an area where it can be selected and an area
 * where it can be dragged. An example for an use-case it that the draggable area can be enabled once the marker has
 * been selected and otherwise is disabled.
 * </p>
 */
abstract class DraggableDataPainter implements IDataPainter<PointData, DraggableDataListPainter> {
    protected DraggableDataListPainter parent = null;
    PointData data;
    protected PointF currentPosition;
    protected PointF dragOffset = new PointF(0, 0);
    protected boolean isSelectedForDragging = false;
    protected boolean isDragging = false;

    @Override
    public void setTo(DraggableDataListPainter painter, PointData data) {
        this.parent = painter;
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

        if (wasDragging)
            parent.markerMoveRequest(this, getDragPoint(event), isDragging);

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
        parent.getMarkerPainterGroup().selectForDrag(this, parent);
    }

    @Override
    public boolean isSelectedForDrag() {
        return isSelectedForDragging;
    }

    public PointF getCachedScreenPosition() {
        if (currentPosition == null)
            currentPosition = parent.getScreenPosition(data);

        return currentPosition;
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
        parent.markerMoveRequest(this, point, true);
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
