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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import nz.ac.auckland.lablet.*;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.plotview.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Interface for a drawable, selectable marker that can handle motion events.
 */
interface IMarker {
    public void onDraw(Canvas canvas, float priority);

    public boolean handleActionDown(MotionEvent ev);
    public boolean handleActionUp(MotionEvent ev);
    public boolean handleActionMove(MotionEvent ev);

    public void setSelected(boolean selected);
    public boolean isSelected();

    public PointF getPosition();
    public void setPosition(PointF position);
}

/**
 * A selectable and draggable marker.
 * <p>
 * Once the marker is selected it can be dragged around. The marker has an area where it can be selected and an area
 * where it can be dragged. An example for an use-case it that the draggable area can be enabled once the marker has
 * been selected and otherwise is disabled.
 * </p>
 */
abstract class DraggableMarker implements IMarker {
    protected PointF position;
    protected PointF dragOffset;
    protected boolean isSelected;
    protected boolean isDragging;
    protected AbstractMarkerPainter parent = null;

    public DraggableMarker(AbstractMarkerPainter parentContainer) {
        position = new PointF(0, 0);
        dragOffset = new PointF(0, 0);
        isSelected = false;
        isDragging = false;
        parent = parentContainer;
    }

    public PointF getPosition() {
        return position;
    }
    public void setPosition(PointF pos) {
        position = pos;
    }

    /**
     * Handle action move down.
     *
     * @param event from the system
     * @return return true if the event has been handled, i.e., the marker has been touched in th drag area
     */
    public boolean handleActionDown(MotionEvent event) {
        PointF point = new PointF(event.getX(), event.getY());
        dragOffset.x = point.x - position.x;
        dragOffset.y = point.y - position.y;

        if (!isSelected) {
            if (isPointOnSelectArea(point))
                setSelected(true);
            if (isSelected && isPointOnDragArea(point))
                isDragging = true;

            if (isSelected || isDragging)
                return true;
        } else if (isPointOnDragArea(point)) {
            isDragging = true;
            return true;
        }
        setSelected(false);
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
            onDraggedTo(getDragPoint(event));
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

    public void setSelected(boolean selected) {
        isSelected = selected;
        parent.markerSelected(this, selected);
    }

    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Notifies a derived class that the user performed a drag operation.
     *
     * @param point the new position the marker was dragged to
     */
    protected void onDraggedTo(PointF point) {
        setPosition(point);
        parent.markerMoveRequest(this, point, true);
    }

    /**
     * Check if a point is in the selectable are.
     *
     * @param point to be checked
     * @return true if the point is in the selectable area
     */
    abstract protected boolean isPointOnSelectArea(PointF point);

    /**
     * Check if a point is in the draggable area of the marker.
     *
     * @param point to be checked
     * @return true if the point is in the drag area
     */
    protected boolean isPointOnDragArea(PointF point) {
        return isPointOnSelectArea(point);
    }
}


/**
 * Default implementation of a draggable marker.
 */
class SimpleMarker extends DraggableMarker {
    // device independent pixels
    private final float INNER_RING_RADIUS_DP = 30;
    private final float INNER_RING_WIDTH_DP = 2;
    private final float RING_RADIUS_DP = 100;
    private final float RING_WIDTH_DP = 40;

    private float INNER_RING_RADIUS;
    private float INNER_RING_WIDTH;
    private float RING_RADIUS;
    private float RING_WIDTH;

    private Paint paint = null;
    private int mainAlpha = 255;

    public SimpleMarker(AbstractMarkerPainter parentContainer) {
        super(parentContainer);
        paint = new Paint();
        paint.setAntiAlias(true);

        INNER_RING_RADIUS = parent.toPixel(INNER_RING_RADIUS_DP);
        INNER_RING_WIDTH = parent.toPixel(INNER_RING_WIDTH_DP);
        RING_RADIUS = parent.toPixel(RING_RADIUS_DP);
        RING_WIDTH = parent.toPixel(RING_WIDTH_DP);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (priority >= 0. && priority <= 1.)
            mainAlpha = (int)(priority * 255.);
        else
            mainAlpha = 255;

        float crossR = INNER_RING_RADIUS / (float)1.41421356237;
        paint.setColor(makeColor(100, 20, 20, 20));
        paint.setStrokeWidth(1);
        canvas.drawLine(position.x - crossR, position.y - crossR, position.x + crossR, position.y + crossR, paint);
        canvas.drawLine(position.x + crossR, position.y - crossR, position.x - crossR, position.y + crossR, paint);

        if (priority == 1.)
            paint.setColor(ExperimentAnalyserActivity.MARKER_COLOR);
        else
            paint.setColor(makeColor(255, 200, 200, 200));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(INNER_RING_WIDTH);
        canvas.drawCircle(position.x, position.y, INNER_RING_RADIUS, paint);

        if (isSelected()) {
            paint.setColor(makeColor(100, 0, 200, 100));
            paint.setStrokeWidth(RING_WIDTH);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(position.x, position.y, RING_RADIUS, paint);
        }
    }

    @Override
    protected boolean isPointOnSelectArea(PointF point) {
        float distance = (float)Math.sqrt(Math.pow(point.x - position.x, 2) + Math.pow(point.y - position.y, 2));
        return distance <= INNER_RING_RADIUS;
    }

    protected boolean isPointOnDragArea(PointF point) {
        float distance = (float)Math.sqrt(Math.pow(point.x - position.x, 2) + Math.pow(point.y - position.y, 2));
        if (distance < RING_RADIUS + RING_WIDTH / 2)
            return true;
        return isPointOnSelectArea(point);
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
abstract class AbstractMarkerPainter extends AbstractPlotPainter implements MarkerDataModel.IMarkerDataModelListener {
    public class SharedSelectedMarker {
        private IMarker selectedMarker;

        public IMarker getSelectedMarker() {
            return selectedMarker;
        }

        public void setSelectedMarker(IMarker selectedMarker) {
            if (this.selectedMarker != null)
                this.selectedMarker.setSelected(false);
            this.selectedMarker = selectedMarker;

            if (this.selectedMarker != null)
                this.selectedMarker.setSelected(true);
        }
    }
    private SharedSelectedMarker selectedMarker;

    protected MarkerDataModel markerData = null;
    final protected Rect frame = new Rect();
    final protected List<IMarker> markerList = new ArrayList<>();

    public AbstractMarkerPainter(MarkerDataModel model) {
        markerData = model;
        markerData.addListener(this);
    }

    public SharedSelectedMarker getSharedSelectedMarker() {
        return selectedMarker;
    }

    public void setSharedSelectedMarker(SharedSelectedMarker selectedMarker) {
        this.selectedMarker = selectedMarker;
    }

    public void release() {
        markerData.removeListener(this);
        markerData = null;
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        boolean handled = false;
        if (action == MotionEvent.ACTION_DOWN) {
            for (IMarker marker : markerList) {
                if (marker.handleActionDown(event)) {
                    handled = true;
                    if (marker.isSelected() && selectedMarker != marker) {
                        if (selectedMarker != null)
                            selectedMarker.setSelectedMarker(marker);
                    }
                    break;
                }
            }
            if (handled) {
                ViewParent parent = containerView.getParent();
                if (parent != null)
                    parent.requestDisallowInterceptTouchEvent(true);
            }

        } else if (action == MotionEvent.ACTION_UP) {
            for (IMarker marker : markerList) {
                if (marker.handleActionUp(event)) {
                    handled = true;
                    break;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (IMarker marker : markerList) {
                if (marker.handleActionMove(event)) {
                    handled = true;
                    break;
                }
            }
        }
        if (handled)
            invalidate();

        if (selectedMarker != null && !handled) {
            selectedMarker.setSelectedMarker(null);
            invalidate();
        }

        return handled;
    }

    public void markerSelected(IMarker marker, boolean selected) {
        int markerIndex = markerList.indexOf(marker);
        int selectedIndex = markerData.getSelectedMarkerData();
        if (selected) {
            markerData.selectMarkerData(markerIndex);
        } else {
            if (markerIndex == selectedIndex)
                markerData.selectMarkerData(-1);
        }

        containerView.invalidate();
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
    public void markerMoveRequest(DraggableMarker marker, PointF newPosition, boolean isDragging) {
        if (isDragging)
            return;

        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;

        sanitizeScreenPoint(newPosition);

        PointF newReal = new PointF();
        containerView.fromScreen(newPosition, newReal);
        markerData.setMarkerPosition(newReal, row);
    }

    protected IMarker getMarkerForRow(int row) {
        if (row < 0 || row >= markerList.size())
            return null;
        return markerList.get(row);
    }

    public int toPixel(float densityIndependentPixel) {
        final float scale = containerView.getResources().getDisplayMetrics().density;
        return Math.round(densityIndependentPixel * scale);
    }

    protected void sanitizeScreenPoint(PointF point) {
        if (frame.left > point.x)
            point.x = frame.left;
        if (frame.right < point.x)
            point.x = frame.right;
        if (frame.top > point.y)
            point.y = frame.top;
        if (frame.bottom < point.y)
            point.y = frame.bottom;
    }

    abstract protected DraggableMarker createMarkerForRow(int row);

    public void addMarker(int row) {
        DraggableMarker marker = createMarkerForRow(row);
        PointF screenPos = new PointF();
        containerView.toScreen(markerData.getMarkerDataAt(row).getPosition(), screenPos);
        marker.setPosition(screenPos);
        markerList.add(row, marker);
    }

    public void removeMarker(int row) {
        markerList.remove(row);
        if (row == markerData.getSelectedMarkerData())
            markerData.selectMarkerData(0);
    }

    public void updateMarker(int row, int number) {
        for (int i = row; i < row + number; i++) {
            IMarker marker = markerList.get(i);
            MarkerData data = markerData.getMarkerDataAt(i);
            PointF screenPos = new PointF();
            containerView.toScreen(data.getPosition(), screenPos);
            marker.setPosition(screenPos);
        }
    }

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
        updateMarker(index, number);
        containerView.invalidate();
    }

    @Override
    public void onAllDataChanged(MarkerDataModel model) {
        containerView.invalidate();
    }

    @Override
    public void onDataSelected(MarkerDataModel model, int index) {
        containerView.invalidate();
    }
}


/**
 * Painter for tagged data. For example, the tagged data from a camera experiment.
 */
class TagMarkerDataModelPainter extends AbstractMarkerPainter {
    private LastInsertMarkerManager lastInsertMarkerManager = new LastInsertMarkerManager();

    public TagMarkerDataModelPainter(MarkerDataModel data) {
        super(data);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int currentMarkerRow = markerData.getSelectedMarkerData();
        IMarker topMarker = getMarkerForRow(currentMarkerRow);
        for (int i = 0; i < markerList.size(); i++) {
            IMarker marker = markerList.get(i);
            if (marker == topMarker)
                continue;

            float runDistance = Math.abs(currentMarkerRow - i);
            float currentPriority = (float)(0.35 - 0.1 * runDistance);
            if (currentPriority > 1.0)
                currentPriority = (float)1.0;
            if (currentPriority < 0.1)
                currentPriority = (float)0.1;

            marker.onDraw(canvas, currentPriority);
        }
        if (topMarker != null)
            topMarker.onDraw(canvas, (float)1.0);
    }

    @Override
    public void invalidate() {

    }

    protected DraggableMarker createMarkerForRow(int row) {
        return new SimpleMarker(this);
    }

    /**
     * If the last inserted marker hasn't moved remove it again.
     */
    private class LastInsertMarkerManager {
        private int markerInsertedInLastRun = -1;
        private PointF lastMarkerPosition = new PointF();

        void onCurrentRunChanging(MarkerDataModel markersDataModel) {
            // Index could be out of bounds, e.g., when the marker data has been cleared.
            if (markerInsertedInLastRun >= markerData.getMarkerCount()) {
                markerInsertedInLastRun =-1;
                return;
            }

            if (markerInsertedInLastRun >= 0) {
                MarkerData lastMarkerData = markerData.getMarkerDataAt(markerInsertedInLastRun);
                if (lastMarkerData.getPosition().equals(lastMarkerPosition)) {
                    markerData.removeMarkerData(markerInsertedInLastRun);
                    int selectedIndex = markerInsertedInLastRun - 1;
                    if (selectedIndex < 0)
                        selectedIndex = 0;
                    markersDataModel.selectMarkerData(selectedIndex);
                }
                markerInsertedInLastRun = -1;
            }
        }

        void onNewMarkerInserted(int index, MarkerData data) {
            markerInsertedInLastRun = index;
            lastMarkerPosition.set(data.getPosition());
        }
    }

    public void markerSelected(IMarker marker, boolean selected) {

    }

    public void setCurrentRun(int run) {
        lastInsertMarkerManager.onCurrentRunChanging(markerData);

        // check if we have the run in the data list
        MarkerData data = null;
        int index = markerData.findMarkerDataByRun(run);
        if (index >= 0) {
            data = markerData.getMarkerDataAt(index);
            markerData.selectMarkerData(index);
        }

        if (data == null) {
            data = new MarkerData(run);
            if (markerData.getMarkerCount() > 0) {
                int selectedIndex = markerData.getSelectedMarkerData();
                MarkerData prevData = markerData.getMarkerDataAt(selectedIndex);
                data.setPosition(prevData.getPosition());
                data.getPosition().x += 5;

                // sanitize the new marker position
                PointF screenPos = new PointF();
                containerView.toScreen(data.getPosition(), screenPos);
                sanitizeScreenPoint(screenPos);
                containerView.fromScreen(screenPos, data.getPosition());
            } else {
                // center the first marker
                PointF initPosition = new PointF();
                initPosition.x = (containerView.getRangeRight() - containerView.getRangeLeft()) * 0.5f;
                initPosition.y = (containerView.getRangeTop() - containerView.getRangeBottom()) * 0.5f;
                data.setPosition(initPosition);
            }

            int newIndex = markerData.addMarkerData(data);
            markerData.selectMarkerData(newIndex);

            lastInsertMarkerManager.onNewMarkerInserted(newIndex, data);
        }
    }
}


/**
 * Marker for the calibration length scale.
 */
class CalibrationMarker extends SimpleMarker {
    public CalibrationMarker(AbstractMarkerPainter parentContainer) {
        super(parentContainer);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (isSelected())
            super.onDraw(canvas, priority);
    }
}


/**
 * Marker for the origin coordinate system.
 */
class OriginMarker extends SimpleMarker {
    private OriginMarkerPainter originMarkerPainter;

    public OriginMarker(OriginMarkerPainter parentContainer) {
        super(parentContainer);
        originMarkerPainter = parentContainer;
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (isSelected())
            super.onDraw(canvas, priority);
    }

    /**
     * Dragging a origin marker needs special treatment since it also affects the other two markers in the coordinate
     * system.
     * <p>
     * Call the painter class that then updates all the markers.
     * </p>
     * @param point the new position the marker was dragged to
     */
    @Override
    protected void onDraggedTo(PointF point) {
        originMarkerPainter.onDraggedTo(this, point);
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

    public  void setCurrentRun(int run) {
        for (IPlotPainter painter : allPainters) {
            TagMarkerDataModelPainter tagMarkerDataModelPainter = (TagMarkerDataModelPainter)painter;
            if (tagMarkerDataModelPainter == null)
                continue;
            tagMarkerDataModelPainter.setCurrentRun(run);
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
            TagMarkerDataModelPainter tagMarkerDataModelPainter = (TagMarkerDataModelPainter)painter;
            if (tagMarkerDataModelPainter == null)
                continue;
            tagMarkerDataModelPainter.release();
        }
    }
}