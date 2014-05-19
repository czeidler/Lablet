/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import nz.ac.aucklanduni.physics.lablet.*;
import nz.ac.aucklanduni.physics.lablet.experiment.MarkerData;
import nz.ac.aucklanduni.physics.lablet.experiment.MarkersDataModel;

import java.util.ArrayList;
import java.util.List;


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

/* DragableMarker can be selected. If it is selected there can also be a drag handler to move the marker.
 */
abstract class DragableMarker implements IMarker {
    protected PointF position;
    protected PointF dragOffset;
    protected boolean isSelected;
    protected boolean isDragging;
    protected AbstractMarkersPainter parent = null;

    public DragableMarker(AbstractMarkersPainter parentContainer) {
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

    public boolean handleActionUp(MotionEvent event) {
        boolean wasDragging = isDragging;

        isDragging = false;

        if (wasDragging)
            parent.markerMoveRequest(this, getDragPoint(event));

        return wasDragging;
    }

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

    protected void onDraggedTo(PointF point) {
        setPosition(point);
    }

    abstract protected boolean isPointOnSelectArea(PointF point);

    protected boolean isPointOnDragArea(PointF point) {
        return isPointOnSelectArea(point);
    }
}


class SimpleMarker extends DragableMarker {
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

    public SimpleMarker(AbstractMarkersPainter parentContainer) {
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
        if (distance < RING_RADIUS + RING_WIDTH / 2 && distance > RING_RADIUS - RING_WIDTH / 2)
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


interface IMarkerDataModelPainter {
    public void release();

    public void draw(Canvas canvas, float priority);
    public List<IMarker> getSelectableMarkerList();
    public void onViewSizeChanged();
    public void setCurrentRun(int run);
}

abstract class AbstractMarkersPainter implements IMarkerDataModelPainter, MarkersDataModel.IMarkersDataModelListener {
    protected IExperimentRunView experimentRunView = null;
    protected View markerView = null;
    protected MarkersDataModel markerData = null;
    protected Rect frame = new Rect();
    protected List<IMarker> markerList;


    public AbstractMarkersPainter(View parent, IExperimentRunView runView, MarkersDataModel model) {
        experimentRunView = runView;
        markerView = parent;
        markerData = model;
        markerData.addListener(this);

        markerList = new ArrayList<IMarker>();
        onViewSizeChanged();
    }

    public void release() {
        markerData.removeListener(this);
    }

    public void onViewSizeChanged() {
        markerView.getDrawingRect(frame);

        markerList.clear();
        for (int i = 0; i < markerData.getMarkerCount(); i++)
            addMarker(i);
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

        markerView.invalidate();
    }

    public void markerMoveRequest(DragableMarker marker, PointF newPosition) {
        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;

        sanitizeScreenPoint(newPosition);

        PointF newReal = new PointF();
        experimentRunView.fromScreen(newPosition, newReal);
        markerData.setMarkerPosition(newReal, row);
    }

    protected IMarker getMarkerForRow(int row) {
        if (row < 0 || row >= markerList.size())
            return null;
        return markerList.get(row);
    }

    public int toPixel(float densityIndependentPixel) {
        final float scale = markerView.getResources().getDisplayMetrics().density;
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

    abstract protected DragableMarker createMarkerForRow(int row);

    public void addMarker(int row) {
        DragableMarker marker = createMarkerForRow(row);
        PointF screenPos = new PointF();
        experimentRunView.toScreen(markerData.getMarkerDataAt(row).getPosition(), screenPos);
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
            experimentRunView.toScreen(data.getPosition(), screenPos);
            marker.setPosition(screenPos);
        }
    }

    @Override
    public List<IMarker> getSelectableMarkerList() {
        return markerList;
    }

    @Override
    public void setCurrentRun(int run) {
    }

    @Override
    public void onDataAdded(MarkersDataModel model, int index) {
        addMarker(index);
        markerView.invalidate();
    }

    @Override
    public void onDataRemoved(MarkersDataModel model, int index, MarkerData data) {
        removeMarker(index);
        markerView.invalidate();
    }

    @Override
    public void onDataChanged(MarkersDataModel model, int index, int number) {
        updateMarker(index, number);
        markerView.invalidate();
    }

    @Override
    public void onAllDataChanged(MarkersDataModel model) {
        onViewSizeChanged();
        markerView.invalidate();
    }

    @Override
    public void onDataSelected(MarkersDataModel model, int index) {
        markerView.invalidate();
    }
}

class TagMarkerDataModelPainter extends AbstractMarkersPainter {
    private LastInsertMarkerManager lastInsertMarkerManager = new LastInsertMarkerManager();

    public TagMarkerDataModelPainter(View parent, IExperimentRunView runView, MarkersDataModel data) {
        super(parent, runView, data);
    }

    public void draw(Canvas canvas, float priority) {
        int currentMarkerRow = markerData.getSelectedMarkerData();
        IMarker topMarker = getMarkerForRow(currentMarkerRow);
        for (int i = 0; i < markerList.size(); i++) {
            IMarker marker = markerList.get(i);

            float currentPriority = priority;
            float runDistance = Math.abs(currentMarkerRow - i);
            currentPriority = currentPriority * (float)(1.18 - 0.2 * runDistance);
            if (currentPriority > 1.0)
                currentPriority = (float)1.0;
            if (currentPriority < 0.4)
                currentPriority = (float)0.4;

            marker.onDraw(canvas, currentPriority);
        }
        if (topMarker != null)
            topMarker.onDraw(canvas, (float)1.0);
    }

    public List<IMarker> getSelectableMarkerList() {
        List<IMarker> returnMarkerList = new ArrayList<IMarker>();
        int currentMarkerRow = markerData.getSelectedMarkerData();

        if (currentMarkerRow < 0 || currentMarkerRow >= markerList.size())
            return returnMarkerList;

        returnMarkerList.add(markerList.get(currentMarkerRow));
        return returnMarkerList;
    }

    protected DragableMarker createMarkerForRow(int row) {
        return new SimpleMarker(this);
    }

    /**
     * If the last inserted marker hasn't moved remove it again.
     */
    private class LastInsertMarkerManager {
        private int markerInsertedInLastRun = -1;
        private PointF lastMarkerPosition = new PointF();

        void onCurrentRunChanging(MarkersDataModel markersDataModel) {
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

                // sanatize the new marker position
                PointF screenPos = new PointF();
                experimentRunView.toScreen(data.getPosition(), screenPos);
                sanitizeScreenPoint(screenPos);
                experimentRunView.fromScreen(screenPos, data.getPosition());
            } else {
                // center the first marker
                PointF initPosition = new PointF();
                initPosition.x = experimentRunView.getMaxRawX() * 0.5f;
                initPosition.y = experimentRunView.getMaxRawY() * 0.5f;
                data.setPosition(initPosition);
            }

            int newIndex = markerData.addMarkerData(data);
            markerData.selectMarkerData(newIndex);

            lastInsertMarkerManager.onNewMarkerInserted(newIndex, data);
        }
    }
}

class CalibrationMarker extends SimpleMarker {
    public CalibrationMarker(AbstractMarkersPainter parentContainer) {
        super(parentContainer);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (isSelected())
            super.onDraw(canvas, priority);
    }
}


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

    @Override
    protected void onDraggedTo(PointF point) {
        originMarkerPainter.onDraggedTo(this, point);
    }
}


public class MarkerView extends ViewGroup {
    private IMarker selectedMarker = null;
    protected List<IMarkerDataModelPainter> markerPainterList;
    protected Rect viewFrame = null;

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

        viewFrame = new Rect();
        getDrawingRect(viewFrame);

        markerPainterList = new ArrayList<IMarkerDataModelPainter>();
    }

    public void release() {
        for (IMarkerDataModelPainter data : markerPainterList)
            data.release();

        markerPainterList.clear();
    }

    public void addMarkerPainter(IMarkerDataModelPainter painter) {
        markerPainterList.add(painter);
        painter.onViewSizeChanged();
        invalidate();
    }

    public boolean removeMarkerPainter(IMarkerDataModelPainter painter) {
        boolean removed = markerPainterList.remove(painter);
        invalidate();
        return removed;
    }

    public void setCurrentRun(int run) {
        if (selectedMarker != null) {
            selectedMarker.setSelected(false);
            selectedMarker = null;
        }

        for (IMarkerDataModelPainter tagMarkerCollection : markerPainterList)
            tagMarkerCollection.setCurrentRun(run);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (IMarkerDataModelPainter markerPainter : markerPainterList)
            markerPainter.draw(canvas, 1);

        if (viewFrame.width() != parentWidth || viewFrame.height() != parentHeight)
            requestLayout();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        List<IMarker> allMarkerList = new ArrayList<IMarker>();
        for (IMarkerDataModelPainter markerPainter : markerPainterList)
            allMarkerList.addAll(markerPainter.getSelectableMarkerList());

        int action = event.getActionMasked();
        boolean handled = false;
        if (action == MotionEvent.ACTION_DOWN) {
            for (IMarker marker : allMarkerList) {
                if (marker.handleActionDown(event)) {
                    handled = true;
                    if (marker.isSelected() && selectedMarker != marker) {
                        if (selectedMarker != null)
                            selectedMarker.setSelected(false);
                        selectedMarker = marker;
                    }
                    break;
                }
            }
            if (handled) {
                ViewParent parent = getParent();
                if (parent != null)
                    parent.requestDisallowInterceptTouchEvent(true);
            }

        } else if (action == MotionEvent.ACTION_UP) {
            for (IMarker marker : allMarkerList) {
                if (marker.handleActionUp(event)) {
                    handled = true;
                    break;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (IMarker marker : allMarkerList) {
                if (marker.handleActionMove(event)) {
                    handled = true;
                    break;
                }
            }
        }
        if (handled)
            invalidate();

        if (selectedMarker != null && !handled) {
            selectedMarker.setSelected(false);
            selectedMarker = null;
            invalidate();
        }

        return handled;
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewFrame.right = w;
        viewFrame.bottom = h;
        for (IMarkerDataModelPainter tagMarkerCollection : markerPainterList)
            tagMarkerCollection.onViewSizeChanged();
        invalidate();
    }
}