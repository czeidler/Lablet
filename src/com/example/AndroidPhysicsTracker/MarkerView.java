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

    public void setPosition(PointF pos) {
        position = pos;
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

    public SimpleMarker(AbstractMarkersPainter parentContainer) {
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


interface IMarkerDataModelPainter {
    public MarkersDataModel getModel();
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

    protected List<IMarker> markerList;


    public AbstractMarkersPainter(View parent, IExperimentRunView runView, MarkersDataModel model) {
        experimentRunView = runView;
        markerView = parent;
        markerData = model;
        markerData.addListener(this);

        markerList = new ArrayList<IMarker>();
        onViewSizeChanged();
    }

    public MarkersDataModel getModel() {
        return markerData;
    }

    public void release() {
        markerData.removeListener(this);
    }

    public void onViewSizeChanged() {
        markerList.clear();
        for (int i = 0; i < markerData.getMarkerCount(); i++)
            addMarker(i);
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

    protected void sanitizeScreenPoint(PointF point) {
        View view = (View)experimentRunView;
        Rect frame = new Rect();
        view.getDrawingRect(frame);
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

    public void updateMarker(int row) {
        IMarker marker = markerList.get(row);
        MarkerData data = markerData.getMarkerDataAt(row);
        PointF screenPos = new PointF();
        experimentRunView.toScreen(data.getPosition(), screenPos);
        marker.setPosition(screenPos);
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
    public void onDataChanged(MarkersDataModel model, int index) {
        updateMarker(index);
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

    public void setCurrentRun(int run) {
        // check if we have the run in the data list
        MarkerData data = null;
        for (int i = 0; i < markerData.getMarkerCount(); i++) {
            MarkerData foundData = markerData.getMarkerDataAt(i);
            if (foundData.getRunId() == run) {
                data = foundData;
                markerData.selectMarkerData(i);
                break;
            }
        }

        if (data == null) {
            data = new MarkerData(run);
            if (markerData.getMarkerCount() > 0) {
                MarkerData prevData = markerData.getMarkerDataAt(markerData.getMarkerCount() - 1);
                data.setPosition(prevData.getPosition());
                // TODO take unit and scale into account
                data.getPosition().x += 5;
                PointF screenPos = new PointF();
                experimentRunView.toScreen(data.getPosition(), screenPos);
                sanitizeScreenPoint(screenPos);
                experimentRunView.fromScreen(screenPos, data.getPosition());
            }
            markerData.addMarkerData(data);
            markerData.selectMarkerData(markerData.getMarkerCount() - 1);
        }
    }
}

class CalibrationMarkerPainter extends AbstractMarkersPainter {

    public CalibrationMarkerPainter(View parent, IExperimentRunView runView, MarkersDataModel model) {
        super(parent, runView, model);
    }

    @Override
    protected DragableMarker createMarkerForRow(int row) {
        return new SimpleMarker(this);
    }

    @Override
    public void draw(Canvas canvas, float priority) {
        for (IMarker marker : markerList)
            marker.onDraw(canvas, priority);

        if (markerData.getMarkerCount() != 2)
            return;

        PointF screenPos1 = getScreenPos(0);
        PointF screenPos2 = getScreenPos(1);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        canvas.drawLine(screenPos1.x, screenPos1.y, screenPos2.x, screenPos2.y, paint);
    }

    public void addMarker(int row) {
        DragableMarker marker = createMarkerForRow(row);
        marker.setPosition(markerData.getMarkerDataAt(row).getPosition());
        markerList.add(row, marker);
    }

    public void updateMarker(int row) {
        IMarker marker = markerList.get(row);
        marker.setPosition(markerData.getMarkerDataAt(row).getPosition());
    }
    
    @Override
    public void markerMoveRequest(DragableMarker marker, PointF newPosition) {
        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;

        sanitizeScreenPoint(newPosition);
        markerData.setMarkerPosition(newPosition, row);
    }

    @Override
    public List<IMarker> getSelectableMarkerList() {
        return markerList;
    }

    @Override
    public void setCurrentRun(int run) {

    }

    private PointF getScreenPos(int markerIndex) {
        MarkerData data = markerData.getMarkerDataAt(markerIndex);
        return data.getPosition();
    }
}

public class MarkerView extends ViewGroup {
    private IMarker selectedMarker = null;
    private List<IMarkerDataModelPainter> markerPainterList;
    private Rect viewFrame = null;
    private boolean touchEventHandledLastTime = false;
    private IExperimentRunView experimentRunView = null;

    private int parentWidth;
    private int parentHeight;

    public MarkerView(Context context, View target) {
        super(context);
        experimentRunView = (IExperimentRunView)target;

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

    public void addXYCalibrationMarkers(MarkersDataModel marker) {
        markerPainterList.add(new CalibrationMarkerPainter(this, experimentRunView, marker));
    }

    public void addTagMarkers(MarkersDataModel marker) {
        markerPainterList.add(new TagMarkerDataModelPainter(this, experimentRunView, marker));
    }

    public boolean removeMarkers(MarkersDataModel model) {
        IMarkerDataModelPainter painter = findMarkersPainter(model);
        if (painter == null)
            return false;
        painter.release();
        markerPainterList.remove(painter);

        return true;
    }

    public void setCurrentRun(int run) {
        if (selectedMarker != null) {
            selectedMarker.setSelected(false);
            selectedMarker = null;
        }

        for (IMarkerDataModelPainter tagMarkerCollection : markerPainterList) {
            tagMarkerCollection.setCurrentRun(run);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (IMarkerDataModelPainter markerPainter : markerPainterList)
            markerPainter.draw(canvas, 1);

        Rect frame = new Rect();
        getDrawingRect(frame);
        if (frame.width() != parentWidth || frame.height() != parentHeight)
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

            getLayoutParams().width = parentWidth;
            getLayoutParams().height = parentHeight;
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

    private IMarkerDataModelPainter findMarkersPainter(MarkersDataModel model) {
        for (IMarkerDataModelPainter painter : markerPainterList) {
            if (painter.getModel() == painter);
            return painter;
        }
        return null;
    }
}