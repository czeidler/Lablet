/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.graphics.*;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import nz.ac.auckland.lablet.experiment.AbstractPointDataModel;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.misc.DeviceIndependentPixel;
import nz.ac.auckland.lablet.views.plotview.*;

import java.util.ArrayList;
import java.util.List;


/**
 * A selectable and draggable marker.
 * <p>
 * Once the marker is selected it can be dragged around. The marker has an area where it can be selected and an area
 * where it can be dragged. An example for an use-case it that the draggable area can be enabled once the marker has
 * been selected and otherwise is disabled.
 * </p>
 */
abstract class DraggableMarker<T> implements IMarker<T> {
    protected AbstractMarkerPainter parent = null;
    protected T markerData;
    protected PointF currentPosition;
    protected PointF dragOffset = new PointF(0, 0);
    protected boolean isSelectedForDragging = false;
    protected boolean isDragging = false;

    @Override
    public void setTo(AbstractMarkerPainter painter, T markerData) {
        this.parent = painter;
        this.markerData = markerData;
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
            currentPosition = parent.getMarkerScreenPosition(markerData);

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


/**
 * Default implementation of a draggable marker.
 */
class SimpleMarker<T> extends DraggableMarker<T> {
    // device independent pixels
    private class Const {
        static public final float INNER_RING_RADIUS_DP = 30;
        static public final float INNER_RING_WIDTH_DP = 2;
        static public final float RING_RADIUS_DP = 100;
        static public final float RING_WIDTH_DP = 40;
    }

    final public static int MARKER_COLOR = Color.argb(255, 100, 200, 20);
    final public static int DRAG_HANDLE_COLOR = Color.argb(100, 0, 200, 100);

    private float INNER_RING_RADIUS;
    private float INNER_RING_WIDTH;
    private float RING_RADIUS;
    private float RING_WIDTH;

    private Paint paint = null;
    private int mainAlpha = 255;

    public SimpleMarker() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    public void setTo(AbstractMarkerPainter painter, T markerData) {
        super.setTo(painter, markerData);

        INNER_RING_RADIUS = parent.toPixel(Const.INNER_RING_RADIUS_DP);
        INNER_RING_WIDTH = parent.toPixel(Const.INNER_RING_WIDTH_DP);
        RING_RADIUS = parent.toPixel(Const.RING_RADIUS_DP);
        RING_WIDTH = parent.toPixel(Const.RING_WIDTH_DP);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        PointF position = getCachedScreenPosition();

        if (priority >= 0. && priority <= 1.)
            mainAlpha = (int)(priority * 255.);
        else
            mainAlpha = 255;

        float crossR = INNER_RING_RADIUS / 1.41421356237f;
        paint.setColor(makeColor(100, 20, 20, 20));
        paint.setStrokeWidth(1);
        canvas.drawLine(position.x - crossR, position.y - crossR, position.x + crossR, position.y + crossR, paint);
        canvas.drawLine(position.x + crossR, position.y - crossR, position.x - crossR, position.y + crossR, paint);

        if (priority == 1.)
            paint.setColor(MARKER_COLOR);
        else
            paint.setColor(makeColor(255, 200, 200, 200));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(INNER_RING_WIDTH);
        canvas.drawCircle(position.x, position.y, INNER_RING_RADIUS, paint);

        if (isSelectedForDrag()) {
            paint.setColor(DRAG_HANDLE_COLOR);
            paint.setStrokeWidth(RING_WIDTH);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(position.x, position.y, RING_RADIUS, paint);
        }
    }

    @Override
    public boolean isPointOnSelectArea(PointF screenPoint) {
        PointF position = getCachedScreenPosition();
        float distance = (float)Math.sqrt(Math.pow(screenPoint.x - position.x, 2) + Math.pow(screenPoint.y - position.y, 2));
        return distance <= INNER_RING_RADIUS;
    }

    @Override
    protected boolean isPointOnDragArea(PointF screenPoint) {
        PointF position = getCachedScreenPosition();
        float distance = (float)Math.sqrt(Math.pow(screenPoint.x - position.x, 2) + Math.pow(screenPoint.y - position.y, 2));
        if (distance < RING_RADIUS + RING_WIDTH / 2)
            return true;
        return isPointOnSelectArea(screenPoint);
    }

    protected int makeColor(int alpha, int red, int green, int blue) {
        int finalAlpha = composeAlpha(alpha, mainAlpha);
        return Color.argb(finalAlpha, red, green, blue);
    }

    protected int makeColor(int color) {
        int finalAlpha = composeAlpha(Color.alpha(color), mainAlpha);
        return Color.argb(finalAlpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int composeAlpha(int alpha1, int alpha2) {
        float newAlpha = (float)(alpha1 * alpha2) / 255;
        return (int)newAlpha;
    }
}


/**
 * Abstract base class to draw a {@link nz.ac.auckland.lablet.experiment.MarkerDataModel} in a
 * {@link nz.ac.auckland.lablet.views.MarkerView}.
 */
abstract class AbstractMarkerPainter<T> extends AbstractPlotPainter {

    public class MarkerPainterGroup {
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
    final protected List<IMarker<T>> markerList = new ArrayList<>();

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

    public List<IMarker<T>> getSelectableMarkerList() {
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
        List<IMarker<T>> selectableMarkers = getSelectableMarkerList();
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
        IMarker<T> marker = createMarkerForRow(row);
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


/**
 * Displays one or more of marker datasets.
 *
 * <p>
 * The MarkerView also takes track of the currently selected {@link nz.ac.auckland.lablet.views.IMarker}.
 * </p>
 */
public class MarkerView extends PlotPainterContainerView {
    final protected Rect viewFrame = new Rect();

    private int parentWidth;
    private int parentHeight;

    private AbstractMarkerPainter.MarkerPainterGroup markerPainterGroup = null;

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public MarkerView(Context context) {
        super(context);

        init();
    }

    private void init() {
        setWillNotDraw(false);

        getDrawingRect(viewFrame);
    }

    public boolean isAnyMarkerSelectedForDrag() {
        if (markerPainterGroup == null)
            return false;
        return markerPainterGroup.getSelectedForDragMarker() != null;
    }

    @Override
    public void addPlotPainter(IPlotPainter painter) {
        super.addPlotPainter(painter);

        if (painter instanceof AbstractMarkerPainter) {
            AbstractMarkerPainter markerPainter = (AbstractMarkerPainter)painter;
            if (markerPainterGroup == null)
                markerPainterGroup = markerPainter.getMarkerPainterGroup();
            else
                markerPainter.setMarkerPainterGroup(markerPainterGroup);
        }
    }

    public void setCurrentFrame(int frame, @Nullable PointF insertHint) {
        for (IPlotPainter painter : allPainters) {
            if (!(painter instanceof TagMarkerDataModelPainter))
                continue;
            TagMarkerDataModelPainter tagMarkerDataModelPainter = (TagMarkerDataModelPainter)painter;
            tagMarkerDataModelPainter.setCurrentFrame(frame, insertHint);
            // deselect any marker
            tagMarkerDataModelPainter.getMarkerPainterGroup().deselect();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewFrame.width() != parentWidth || viewFrame.height() != parentHeight)
            requestLayout();
    }

    public void setSize(int width, int height) {
        parentWidth = width;
        parentHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = parentWidth;
        int height = parentHeight;

        if (specWidthMode == MeasureSpec.AT_MOST || specHeightMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);

            LayoutParams params = getLayoutParams();
            assert params != null;
            params.width = parentWidth;
            params.height = parentHeight;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        setMeasuredDimension(parentWidth, parentHeight);
    }

    public void release() {
        for (IPlotPainter painter : allPainters) {
            if (!(painter instanceof TagMarkerDataModelPainter))
                continue;
            TagMarkerDataModelPainter tagMarkerDataModelPainter = (TagMarkerDataModelPainter)painter;
            tagMarkerDataModelPainter.release();
        }
    }
}