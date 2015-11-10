/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.marker;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ViewParent;
import nz.ac.auckland.lablet.misc.DeviceIndependentPixel;
import nz.ac.auckland.lablet.views.plotview.AbstractPlotPainter;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base class to draw a {@link MarkerDataModel} in a
 * {@link MarkerView}.
 */
public abstract class AbstractMarkerPainter<T> extends AbstractPlotPainter {

    static public class MarkerPainterGroup {
        private AbstractMarkerPainter selectedForDragPainter = null;
        private IMarker selectedForDragMarker = null;
        private boolean inSelectForDragMethod = false;
        private boolean selectOnDrag = false;

        public IMarker getSelectedForDragMarker() {
            return selectedForDragMarker;
        }

        public void deselect() {
            if (selectedForDragMarker == null || selectedForDragPainter == null)
                return;
            selectedForDragPainter.containerView.invalidate();
            if (selectOnDrag)
                selectedForDragPainter.markerData.selectMarkerData(-1);

            selectedForDragMarker.setSelectedForDrag(false);
            selectedForDragPainter = null;
            selectedForDragMarker = null;
        }

        /**
         * Set if the MarkerData should be marked as selected if the IMarker is selected for drag.
         *
         * @param selectOnDrag
         */
        public void setSelectOnDrag(boolean selectOnDrag) {
            this.selectOnDrag = selectOnDrag;
        }

        public void selectForDrag(IMarker marker, AbstractMarkerPainter painter) {
            if (inSelectForDragMethod)
                return;
            inSelectForDragMethod = true;
            try {
                if (!marker.isSelectedForDrag()) {
                    // already deselected?
                    if (selectedForDragPainter == null)
                        return;

                    if (selectedForDragPainter == painter && selectedForDragMarker == marker) {
                        if (selectedForDragPainter.containerView != null)
                            selectedForDragPainter.containerView.invalidate();
                        if (selectOnDrag)
                            selectedForDragPainter.markerData.selectMarkerData(-1);
                        selectedForDragPainter = null;
                        selectedForDragMarker = null;
                    }
                    return;
                }
                // marker has been selected; deselect old marker
                if (selectedForDragMarker != null && selectedForDragMarker != marker) {
                    selectedForDragMarker.setSelectedForDrag(false);
                    if (selectOnDrag)
                        selectedForDragPainter.markerData.selectMarkerData(-1);
                    if (selectedForDragPainter.containerView != null)
                        selectedForDragPainter.containerView.invalidate();
                }

                selectedForDragPainter = painter;
                selectedForDragMarker = marker;
                if (selectOnDrag) {
                    int selectedIndex = selectedForDragPainter.getSelectableMarkerList().indexOf(selectedForDragMarker);
                    selectedForDragPainter.markerData.selectMarkerData(selectedIndex);
                }
            } finally {
                inSelectForDragMethod = false;
            }
        }
    }

    private MarkerDataModel.IListener dataListener = new MarkerDataModel.IListener() {

        @Override
        public void onDataAdded(MarkerDataModel model, int index) {
            addMarker(index);
            containerView.invalidate();
        }

        @Override
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {
            removeMarker(index);
            containerView.invalidate();
        }

        @Override
        public void onDataChanged(MarkerDataModel model, int index, int number) {
            invalidateMarker();
            containerView.invalidate();
        }

        @Override
        public void onAllDataChanged(MarkerDataModel model) {
            rebuildMarkerList();
            containerView.invalidate();
        }

        @Override
        public void onDataSelected(MarkerDataModel model, int index) {
            if (getMarkerPainterGroup().selectOnDrag && index >= 0)
                markerList.get(index).setSelectedForDrag(true);

            containerView.invalidate();
        }
    };

    private MarkerPainterGroup markerPainterGroup = new MarkerPainterGroup();

    protected AbstractPointDataModel<T> markerData = null;
    final protected Rect frame = new Rect();
    final protected List<IMarker> markerList = new ArrayList<>();

    public AbstractMarkerPainter(AbstractPointDataModel<T> model) {
        markerData = model;
        markerData.addListener(dataListener);
    }

    @Override
    protected void onAttachedToView() {
        super.onAttachedToView();

        rebuildMarkerList();
    }

    public AbstractPointDataModel<T> getMarkerModel() {
        return markerData;
    }

    public MarkerPainterGroup getMarkerPainterGroup() {
        return markerPainterGroup;
    }

    public void setMarkerPainterGroup(MarkerPainterGroup markerPainterGroup) {
        this.markerPainterGroup = markerPainterGroup;
    }

    public void release() {
        if (markerData != null) {
            markerData.removeListener(dataListener);
            markerData = null;
        }
    }

    public List<IMarker> getSelectableMarkerList() {
        return markerList;
    }

    public PointF getMarkerScreenPosition(T markerData) {
        PointF realPosition = this.markerData.getPosition(markerData);
        PointF screenPosition = new PointF();
        containerView.toScreen(realPosition, screenPosition);
        return screenPosition;
    }

    public RectF getScreenRect() {
        return containerView.getScreenRect();
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        frame.set(0, 0, width, height);
        rebuildMarkerList();
    }

    private void rebuildMarkerList() {
        markerList.clear();
        for (int i = 0; i < markerData.size(); i++)
            addMarker(i);
    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        List<IMarker> selectableMarkers = getSelectableMarkerList();
        int action = event.getActionMasked();
        boolean handled = false;
        if (action == MotionEvent.ACTION_DOWN) {
            for (IMarker marker : selectableMarkers) {
                if (marker.handleActionDown(event)) {
                    handled = true;
                    break;
                }
            }
            if (handled) {
                ViewParent parent = containerView.getParent();
                if (parent != null)
                    parent.requestDisallowInterceptTouchEvent(true);
            }

        } else if (action == MotionEvent.ACTION_UP) {
            for (IMarker marker : selectableMarkers) {
                if (marker.handleActionUp(event)) {
                    handled = true;
                    break;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (IMarker marker : selectableMarkers) {
                if (marker.handleActionMove(event)) {
                    handled = true;
                    break;
                }
            }
        }
        if (handled)
            containerView.invalidate();

        return handled;
    }

    /**
     * Is called by a child marker.
     * <p>
     * Default implementation directly returns if marker is still be dragged, i.e., only on touch up events the marker
     * is moved. This circumvents some performance problems.
     * </p>
     *
     * @param marker that has been moved
     * @param newPosition the marker has been moved too
     */
    public void markerMoveRequest(DraggableMarker<T> marker, PointF newPosition, boolean isDragging) {
        if (isDragging)
            return;

        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;

        sanitizeScreenPoint(newPosition);

        PointF newReal = new PointF();
        containerView.fromScreen(newPosition, newReal);
        markerData.setPosition(newReal, row);
    }

    public IMarker getMarkerForRow(int row) {
        if (row < 0 || row >= markerList.size())
            return null;
        return markerList.get(row);
    }

    public int toPixel(float densityIndependentPixel) {
        return DeviceIndependentPixel.toPixel(densityIndependentPixel, containerView);
    }

    protected void sanitizeScreenPoint(PointF point) {
        if (frame.left + containerView.getPaddingLeft() > point.x)
            point.x = frame.left + containerView.getPaddingLeft();
        if (frame.right - containerView.getPaddingRight()< point.x)
            point.x = frame.right - containerView.getPaddingRight();
        if (frame.top + containerView.getPaddingTop() > point.y)
            point.y = frame.top + containerView.getPaddingTop();
        if (frame.bottom - containerView.getPaddingBottom() < point.y)
            point.y = frame.bottom - containerView.getPaddingBottom();
    }

    abstract protected DraggableMarker createMarkerForRow(int row);

    public void addMarker(int row) {
        IMarker marker = createMarkerForRow(row);
        marker.setTo(this, markerData.getAt(row));
        markerList.add(row, marker);
    }

    public int markerIndexOf(IMarker marker) {
        return markerList.indexOf(marker);
    }

    public void removeMarker(int row) {
        markerList.remove(row);
        if (row == markerData.getSelectedMarkerData())
            markerData.selectMarkerData(-1);
    }

    @Override
    public void onRangeChanged(RectF range, RectF oldRange, boolean keepDistance) {
        super.onRangeChanged(range, oldRange, keepDistance);

        invalidateMarker();
    }

    private void invalidateMarker() {
        for (IMarker marker : markerList)
            marker.invalidate();
    }
}
