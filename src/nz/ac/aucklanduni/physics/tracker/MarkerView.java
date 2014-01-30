/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
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
            parent.markerMoveRequest(this, position);

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
    final static float RADIUS = 30;
    final static float RING_RADIUS = 100;
    final static float RING_WIDTH = 40;
    private Paint paint = null;
    private int mainAlpha = 255;

    public SimpleMarker(AbstractMarkersPainter parentContainer) {
        super(parentContainer);
        paint = new Paint();
        paint.setAntiAlias(true);
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
            paint.setStrokeWidth(RING_WIDTH);
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

    public MarkersDataModel getModel() {
        return markerData;
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

                PointF screenPos = new PointF();
                experimentRunView.toScreen(data.getPosition(), screenPos);
                sanitizeScreenPoint(screenPos);
                experimentRunView.fromScreen(screenPos, data.getPosition());
            }
            int newIndex = markerData.addMarkerData(data);
            markerData.selectMarkerData(newIndex);

            lastInsertMarkerManager.onNewMarkerInserted(newIndex, data);
        }
    }
}

class CalibrationMarker extends SimpleMarker {
    private Paint paint = null;

    public CalibrationMarker(AbstractMarkersPainter parentContainer) {
        super(parentContainer);
        paint = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (isSelected()) {
            super.onDraw(canvas, priority);
            return;
        }

        paint.setColor(Color.argb(100, 0, 255, 0));
        canvas.drawCircle(position.x, position.y, 7, paint);
    }
}

class CalibrationMarkerPainter extends AbstractMarkersPainter {
    private Calibration calibration;

    public CalibrationMarkerPainter(View parent, IExperimentRunView runView, MarkersDataModel model) {
        super(parent, runView, model);
    }

    @Override
    protected DragableMarker createMarkerForRow(int row) {
        return new CalibrationMarker(this);
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
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        canvas.drawLine(screenPos1.x, screenPos1.y, screenPos2.x, screenPos2.y, paint);

        if (markerData.getSelectedMarkerData() < 0)
            return;

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(15);
        int scaleLength = (int)Math.sqrt(Math.pow(screenPos1.x - screenPos2.x, 2)
                + Math.pow(screenPos1.y - screenPos2.y, 2));
        String text = "Scale length [pixel]: ";
        text += scaleLength;
        PointF textPosition = new PointF(1f, 96f);
        PointF screenTextPosition = new PointF();
        experimentRunView.toScreen(textPosition, screenTextPosition);

        // draw text background box
        Rect textBound = new Rect();
        textBound.left = (int)screenTextPosition.x;
        textBound.top = (int)screenTextPosition.y + (int)Math.ceil(paint.ascent());
        textBound.right = textBound.left + (int)Math.ceil(paint.measureText(text)) + 2;
        textBound.bottom = (int)screenTextPosition.y + (int)Math.ceil(paint.descent()) + 2;
        paint.setColor(Color.argb(150, 100, 100, 100));
        canvas.drawRect(textBound, paint);

        // draw text
        paint.setColor(Color.GREEN);
        canvas.drawText(text, screenTextPosition.x, screenTextPosition.y, paint);


    }

    private PointF getScreenPos(int markerIndex) {
        return markerList.get(markerIndex).getPosition();
    }
}

public class MarkerView extends ViewGroup {
    private IMarker selectedMarker = null;
    protected List<IMarkerDataModelPainter> markerPainterList;
    protected Rect viewFrame = null;
    private boolean touchEventHandledLastTime = false;
    private IExperimentRunView experimentRunView = null;

    private int parentWidth;
    private int parentHeight;

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public MarkerView(Context context, View target) {
        super(context);
        experimentRunView = (IExperimentRunView)target;

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

    public void addXYCalibrationMarkers(MarkersDataModel marker) {
        markerPainterList.add(new CalibrationMarkerPainter(this, experimentRunView, marker));
    }

    public void addTagMarkers(MarkersDataModel marker) {
        addPainter(new TagMarkerDataModelPainter(this, experimentRunView, marker));
    }

    protected void addPainter(IMarkerDataModelPainter painter) {
        markerPainterList.add(painter);
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

        for (IMarkerDataModelPainter tagMarkerCollection : markerPainterList)
            tagMarkerCollection.setCurrentRun(run);

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
            if (handled)
                getParent().requestDisallowInterceptTouchEvent(true);

        } else if (action == MotionEvent.ACTION_UP) {
            for (IMarker marker : allMarkerList) {
                if (marker.handleActionUp(event)) {
                    handled = true;
                    break;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            //if (viewFrame.contains((int)event.getX(), (int)event.getY())) {
                for (IMarker marker : allMarkerList) {
                    if (marker.handleActionMove(event)) {
                        handled = true;
                        break;
                    }
                }
            //} else
            //    handled = touchEventHandledLastTime;
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