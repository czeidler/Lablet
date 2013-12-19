package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
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


class MarkerDataModel {
    private PointF screenPosition;
    private PointF realPosition;
    private IExperimentRunView experimentRunView;

    MarkerDataModel(IExperimentRunView runView) {
        experimentRunView = runView;
        screenPosition = new PointF();
        realPosition = new PointF();
    }

    public PointF getScreenPosition() {
        return screenPosition;
    }

    public PointF getRealPosition() {
        return realPosition;
    }

    public void setScreenPosition(PointF screen) {
        screenPosition = screen;
        experimentRunView.fromScreen(screenPosition, realPosition);
    }

    public void setRealPosition(PointF real) {
        realPosition = real;
        experimentRunView.toScreen(realPosition, screenPosition);
    }
}

/* DragableMarker can be selected. If it is selected there can also be a drag handler to move the marker.
 */
abstract class DragableMarker implements IMarker {
    protected MarkerDataModel data;
    protected PointF dragOffset;
    protected boolean isSelected;
    protected boolean isDragging;

    public DragableMarker(MarkerDataModel markerData) {
        data = markerData;
        dragOffset = new PointF(0, 0);
        isSelected = false;
        isDragging = false;
    }

    public void setPosition(PointF pos) {
        data.setScreenPosition(pos);
    }

    public boolean handleActionDown(MotionEvent event) {
        PointF position = data.getScreenPosition();

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
    private Paint paint = null;
    private int mainAlpha = 255;

    public SimpleMarker(MarkerDataModel dataModel) {
        super(dataModel);
        paint = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (priority >= 0. && priority <= 1.)
            mainAlpha = (int)(priority * 255.);
        else
            mainAlpha = 255;

        PointF position = data.getScreenPosition();
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
        PointF position = data.getScreenPosition();
        float distance = (float)Math.sqrt(Math.pow(point.x - position.x, 2) + Math.pow(point.y - position.y, 2));
        return distance <= RADIUS;
    }

    protected boolean isPointOnDragArea(PointF point) {
        PointF position = data.getScreenPosition();
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

    private int currentRun;
    private RunEntry[] runEntryList;

    class RunEntry {
        public float runValue;
        public MarkerDataModel data;
        public IMarker marker;

        public RunEntry(float runValue, MarkerDataModel dataModel, IMarker newMarker) {
            this.runValue = runValue;
            data = dataModel;
            marker = newMarker;
        }
    }

    public void draw(Canvas canvas, float priority) {
        IMarker topMarker = getMarkerForRun(currentRun);
        for (int i = 0; i < runEntryList.length; i++) {
            RunEntry entry = runEntryList[i];
            if (entry == null)
                continue;
            IMarker marker = entry.marker;
            if (marker == topMarker)
                continue;
            float currentPriority = priority;
            float runDistance = Math.abs(currentRun - i);
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

    public MarkerSeries(IExperimentRunView runView) {
        runEntryList = new RunEntry[runView.getNumberOfRuns()];
        experimentRunView = runView;
    }

    public List<IMarker> getSelectableMarkerList() {
        List<IMarker> markerList = new ArrayList<IMarker>();

        if (currentRun < 0 || currentRun >= experimentRunView.getNumberOfRuns())
            return markerList;

        RunEntry entry = runEntryList[currentRun];
        if (entry == null)
            return markerList;

        markerList.add(entry.marker);
        return markerList;
    }

    public void setCurrentRun(int run) {
        if (run < 0 || run >= runEntryList.length)
            return;
        currentRun = run;

        if (runEntryList[run] == null) {
            MarkerDataModel data = new MarkerDataModel(experimentRunView);
            IMarker marker = new SimpleMarker(data);
            RunEntry entry = new RunEntry(0, data, marker);
            runEntryList[run] = entry;
            if (run > 0 && runEntryList[run - 1] != null) {
                RunEntry lastEntry = runEntryList[run - 1];
                entry.data.setRealPosition(lastEntry.data.getRealPosition());
            }
        }
    }

    public IMarker getMarkerForRun(int run) {
        if (run < 0 || run >= runEntryList.length)
            return null;
        return runEntryList[run].marker;
    }
}

public class MarkerView extends ViewGroup {
    private View targetView = null;

    private IMarker selectedMarker = null;
    private List<IMarker> markerList;
    private List<MarkerSeries> markerSeriesList;
    private Rect viewFrame = null;
    private boolean touchEventHandledLastTime = false;
    private IExperimentRunView experimentRunView = null;

    public MarkerView(Context context, View target) {
        super(context);
        targetView = target;
        experimentRunView = (IExperimentRunView)target;
        init();
    }

    public void setCurrentRun(int run) {
        if (selectedMarker != null) {
            selectedMarker.setSelected(false);
            selectedMarker = null;
        }
        for (MarkerSeries markerSeries : markerSeriesList)
            markerSeries.setCurrentRun(run);
        invalidate();
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (IMarker marker : markerList)
            marker.onDraw(canvas, 1);

        for (MarkerSeries markerSeries : markerSeriesList)
            markerSeries.draw(canvas, 1);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        List<IMarker> allMarkerList = new ArrayList<IMarker>();
        allMarkerList.addAll(markerList);
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

    private void init() {
        setWillNotDraw(false);

        viewFrame = new Rect();
        getDrawingRect(viewFrame);

        markerList = new ArrayList<IMarker>();

        markerSeriesList = new ArrayList<MarkerSeries>();
        markerSeriesList.add(new MarkerSeries(experimentRunView));

        setCurrentRun(0);
    }

}