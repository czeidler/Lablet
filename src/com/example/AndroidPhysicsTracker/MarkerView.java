package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


interface IMarker {
    public void onDraw(Canvas canvas);

    public boolean handleActionDown(MotionEvent ev);
    public boolean handleActionUp(MotionEvent ev);
    public boolean handleActionMove(MotionEvent ev);

    public void setSelected(boolean selected);
    public boolean isSelected();
}


/* DragableMarker can be selected. If it is selected there can also be a drag handler to move the marker.
 */
abstract class DragableMarker implements  IMarker {
    protected PointF position;
    protected PointF dragOffset;
    protected boolean isSelected;
    protected boolean isDragging;

    public DragableMarker() {
        position = new PointF(0, 0);
        dragOffset = new PointF(0, 0);
        isSelected = false;
        isDragging = false;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void setPosition(PointF pos) {
        setPosition(pos.x, pos.y);
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

    public SimpleMarker() {
        paint = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas) {
        float crossR = RADIUS / (float)1.41421356237;
        paint.setColor(Color.argb(100, 20, 20, 20));
        paint.setStrokeWidth(1);
        canvas.drawLine(position.x - crossR, position.y - crossR, position.x + crossR, position.y + crossR, paint);
        canvas.drawLine(position.x + crossR, position.y - crossR, position.x - crossR, position.y + crossR, paint);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawCircle(position.x, position.y, RADIUS, paint);

        if (isSelected()) {
            paint.setColor(Color.argb(100, 0, 200, 100));
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
}


public class MarkerView extends ViewGroup {
    private View targetView = null;

    private IMarker selectedMarker = null;
    private List<IMarker> markerList;
    private Rect viewFrame = null;
    private boolean touchEventHandledLastTime = false;

    public MarkerView(Context context, View target) {
        super(context);
        targetView = target;
        init();
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (IMarker marker : markerList)
            marker.onDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        boolean handled = false;
        if (action == MotionEvent.ACTION_DOWN) {
            for (IMarker marker : markerList) {
                if (marker.handleActionDown(event)) {
                    handled = true;
                    if (marker.isSelected())
                        selectedMarker = marker;
                    break;
                }
            }
        } else if (action == MotionEvent.ACTION_UP) {
            for (IMarker marker : markerList) {
                if (marker.handleActionUp(event)) {
                    handled = true;
                    break;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (viewFrame.contains((int)event.getX(), (int)event.getY())) {
                for (IMarker marker : markerList) {
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
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
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
        markerList.add(new SimpleMarker());
    }

}