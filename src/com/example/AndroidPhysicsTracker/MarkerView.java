package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


interface IMarker {
    public void onDraw(Canvas canvas, float priority);

    public boolean handleActionDown(MotionEvent ev);
    public boolean handleActionUp(MotionEvent ev);
    public boolean handleActionMove(MotionEvent ev);

    public void setSelected(boolean selected);
    public boolean isSelected();
}

/* DragableMarker can be selected. If it is selected there can also be a drag handler to move the marker.
 */
abstract class DragableMarker implements IMarker {
    protected PointF position;
    protected PointF dragOffset;
    protected boolean isSelected;
    protected boolean isDragging;
    protected MarkerSeries parent = null;

    public DragableMarker(MarkerSeries parentContainer) {
        position = new PointF(0, 0);
        dragOffset = new PointF(0, 0);
        isSelected = false;
        isDragging = false;
        parent = parentContainer;
    }

    public void setPosition(PointF pos) {
        position = pos;
    }

    public PointF getPosition() {
        return position;
    }

    public boolean handleActionDown(MotionEvent event) {
        PointF point = new PointF(event.getX(), event.getY());
        dragOffset.x = point.x - position.x;
        dragOffset.y = point.y - position.y;

        if (!isSelected) {
            if (isPointOnSelectArea(point))
                isSelected = true;
            if (isPointOnDragArea(point))
                isDragging = true;

            if (isSelected || isDragging)
                return true;
        } else if (isPointOnDragArea(point)) {
            isDragging = true;
            return true;
        }
        isSelected = false;
        isDragging = false;
        return false;
    }

    public boolean handleActionUp(MotionEvent event) {
        boolean wasDragging = isDragging;

        isDragging = false;

        return wasDragging;
    }

    public boolean handleActionMove(MotionEvent event) {
        if (isDragging) {
            PointF point = new PointF(event.getX(), event.getY());
            point.x -= dragOffset.x;
            point.y -= dragOffset.y;
            onDraggedTo(point);
            return true;
        }
        return false;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        isDragging = false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    protected void onDraggedTo(PointF point) {
        parent.markerMoveRequest(this, point);
    }

    abstract protected boolean isPointOnSelectArea(PointF point);

    protected boolean isPointOnDragArea(PointF point) {
        return isPointOnSelectArea(point);
    }
}


class SimpleMarker extends DragableMarker {
    final static float RADIUS = 30;
    final static float RING_RADIUS = 100;
    private Paint paint = null;
    private int mainAlpha = 255;

    public SimpleMarker(MarkerSeries parentContainer) {
        super(parentContainer);
        paint = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (priority >= 0. && priority <= 1.)
            mainAlpha = (int)(priority * 255.);
        else
            mainAlpha = 255;

        float crossR = RADIUS / (float)1.41421356237;
        paint.setColor(makeColor(100, 20, 20, 20));
        paint.setStrokeWidth(1);
        canvas.drawLine(position.x - crossR, position.y - crossR, position.x + crossR, position.y + crossR, paint);
        canvas.drawLine(position.x + crossR, position.y - crossR, position.x - crossR, position.y + crossR, paint);

        if (priority == 1.)
            paint.setColor(makeColor(Color.RED));
        else
            paint.setColor(makeColor(255, 200, 200, 200));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawCircle(position.x, position.y, RADIUS, paint);

        if (isSelected()) {
            paint.setColor(makeColor(100, 0, 200, 100));
            paint.setStrokeWidth(RADIUS);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(position.x, position.y, RING_RADIUS, paint);
        }
    }

    @Override
    protected boolean isPointOnSelectArea(PointF point) {
        float distance = (float)Math.sqrt(Math.pow(point.x - position.x, 2) + Math.pow(point.y - position.y, 2));
        return distance <= RADIUS;
    }

    protected boolean isPointOnDragArea(PointF point) {
        float distance = (float)Math.sqrt(Math.pow(point.x - position.x, 2) + Math.pow(point.y - position.y, 2));
        if (distance < RING_RADIUS + RADIUS / 2 && distance > RING_RADIUS - RADIUS / 2)
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


class MarkerSeries {
    private IExperimentRunView experimentRunView = null;
    private MarkersDataModel markerData = null;
    private List<DragableMarker> markerList;

    public MarkerSeries(IExperimentRunView runView, MarkersDataModel data) {
        markerList = new ArrayList<DragableMarker>();
        experimentRunView = runView;
        markerData = data;

        for (int i = 0; i < markerData.getMarkerCount(); i++)
            markerAdded(markerData, i);
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

    public void markerMoveRequest(DragableMarker marker, PointF newPosition) {
        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;
        markerData.setMarkerPosition(newPosition, row);
    }

    public List<IMarker> getSelectableMarkerList() {
        List<IMarker> returnMarkerList = new ArrayList<IMarker>();
        int currentMarkerRow = markerData.getSelectedMarkerData();

        if (currentMarkerRow < 0 || currentMarkerRow >= markerList.size())
            return returnMarkerList;

        returnMarkerList.add(markerList.get(currentMarkerRow));
        return returnMarkerList;
    }

    public void markerAdded(MarkersDataModel data, int row) {
        SimpleMarker marker = new SimpleMarker(this);
        PointF screenPos = new PointF();
        experimentRunView.toScreen(data.getMarkerDataAt(row).positionReal, screenPos);
        marker.setPosition(screenPos);
        markerList.add(row, marker);
        markerData.selectMarkerData(row);
    }

    public void markerRemoved(int row) {
        markerList.remove(row);
        if (row == markerData.getSelectedMarkerData())
            markerData.selectMarkerData(0);
    }

    public void markerChanged(int row) {
        DragableMarker marker = markerList.get(row);
        MarkerData data = markerData.getMarkerDataAt(row);
        PointF screenPos = new PointF();
        experimentRunView.toScreen(data.positionReal, screenPos);
        marker.setPosition(screenPos);
    }

    public void setCurrentRun(int run) {
        // check if we have the run in the data list
        MarkerData data = null;
        for (int i = 0; i < markerData.getMarkerCount(); i++) {
            MarkerData foundData = markerData.getMarkerDataAt(i);
            if (foundData.runId == run) {
                data = foundData;
                markerData.selectMarkerData(i);
                break;
            }
        }

        if (data == null) {
            data = new MarkerData();
            data.runId = run;
            markerData.addMarkerData(data);
            markerData.selectMarkerData(markerData.getMarkerCount() - 1);
        }
    }

    public IMarker getMarkerForRow(int row) {
        if (row < 0 || row >= markerList.size())
            return null;
        return markerList.get(row);
    }
}

public class MarkerView extends ViewGroup implements MarkersDataModel.IMarkersDataModelListener {
    private View targetView = null;

    private IMarker selectedMarker = null;
    private List<MarkerSeries> markerSeriesList;
    private List<MarkersDataModel> markerDataList;
    private Rect viewFrame = null;
    private boolean touchEventHandledLastTime = false;
    private IExperimentRunView experimentRunView = null;
    private int currentRun;

    public MarkerView(Context context, View target) {
        super(context);
        targetView = target;
        experimentRunView = (IExperimentRunView)target;

        setWillNotDraw(false);

        viewFrame = new Rect();
        getDrawingRect(viewFrame);

        markerSeriesList = new ArrayList<MarkerSeries>();

        markerDataList = new ArrayList<MarkersDataModel>();
        setCurrentRun(0);
    }

    public void setTagMarkers(MarkersDataModel markers) {
        markerDataList.add(markers);
        markers.addListener(this);
        markerSeriesList.add(new MarkerSeries(experimentRunView, markers));
        setCurrentRun(currentRun);
    }

    public void setCurrentRun(int run) {
        currentRun = run;

        if (selectedMarker != null) {
            selectedMarker.setSelected(false);
            selectedMarker = null;
        }

        for (MarkerSeries markerSeries : markerSeriesList) {
            markerSeries.setCurrentRun(run);
        }
        invalidate();
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (MarkerSeries markerSeries : markerSeriesList)
            markerSeries.draw(canvas, 1);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        List<IMarker> allMarkerList = new ArrayList<IMarker>();
        for (MarkerSeries markerSeries : markerSeriesList)
            allMarkerList.addAll(markerSeries.getSelectableMarkerList());

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
        } else if (action == MotionEvent.ACTION_UP) {
            for (IMarker marker : allMarkerList) {
                if (marker.handleActionUp(event)) {
                    handled = true;
                    break;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (viewFrame.contains((int)event.getX(), (int)event.getY())) {
                for (IMarker marker : allMarkerList) {
                    if (marker.handleActionMove(event)) {
                        handled = true;
                        break;
                    }
                }
            } else
                handled = touchEventHandledLastTime;
        }
        if (handled)
            invalidate();

        if (selectedMarker != null && !handled) {
            selectedMarker.setSelected(false);
            selectedMarker = null;
            invalidate();
        }
        touchEventHandledLastTime = handled;
        return handled;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Rect rect = new Rect();
        targetView.getDrawingRect(rect);
        setMeasuredDimension(rect.width(), rect.height());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewFrame.right = w;
        viewFrame.bottom = h;
    }

    MarkerSeries findMarkerSeriesFor(MarkersDataModel model) {
        for (int i = 0; i < markerDataList.size(); i++) {
            if (markerDataList.get(i) == model)
                return markerSeriesList.get(i);
        }
        return null;
    }


    @Override
    public void onDataAdded(MarkersDataModel model, int index) {
        MarkerSeries markerSeries = findMarkerSeriesFor(model);
        markerSeries.markerAdded(model, index);
        invalidate();
    }

    @Override
    public void onDataRemoved(MarkersDataModel model, int index, MarkerData data) {
        MarkerSeries markerSeries = findMarkerSeriesFor(model);
        markerSeries.markerRemoved(index);
        invalidate();
    }

    @Override
    public void onDataChanged(MarkersDataModel model, int index) {
        MarkerSeries markerSeries = findMarkerSeriesFor(model);
        markerSeries.markerChanged(index);
    }

    @Override
    public void onDataSelected(MarkersDataModel model, int index) {

    }
}