package com.example.AndroidPhysicsTracker;


import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;


class StartEndMarker extends DragableMarker {
    private final float WIDTH = 30;
    private final float HEIGHT = 35;

    public StartEndMarker(AbstractMarkersPainter parentContainer) {
        super(parentContainer);
    }

    @Override
    protected boolean isPointOnSelectArea(PointF point) {
        return getRect().contains(point.x, point.y);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        final int TRIANGLE_HEIGHT = 10;
        Path path = new Path();
        path.moveTo(position.x, 0);
        path.lineTo(position.x + WIDTH / 2, TRIANGLE_HEIGHT);
        path.lineTo(position.x - WIDTH / 2, TRIANGLE_HEIGHT);

        canvas.drawPath(path, paint);

        RectF rect = getRect();
        rect.top = TRIANGLE_HEIGHT;
        canvas.drawRect(rect, paint);
    }

    private RectF getRect() {
        RectF rect = new RectF();
        rect.left = position.x - WIDTH / 2;
        rect.top = position.y;
        rect.right = position.x + WIDTH / 2;
        rect.bottom = position.y + HEIGHT;
        return rect;
    }

    protected void onDraggedTo(PointF point) {
        parent.markerMoveRequest(this, point);
    }
}

class StartEndPainter extends AbstractMarkersPainter {
    public StartEndPainter(View parent, IExperimentRunView runView, MarkersDataModel data) {
        super(parent, runView, data);
    }

    @Override
    protected DragableMarker createMarkerForRow(int row) {
        return new StartEndMarker(this);
    }


    @Override
    public void draw(Canvas canvas, float priority) {
        for (int i = 0; i < markerList.size(); i++) {
            IMarker marker = markerList.get(i);
            marker.onDraw(canvas, priority);
        }
    }

    @Override
    public void markerMoveRequest(DragableMarker marker, PointF newPosition) {
        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;

        PointF newReal = new PointF();
        sanitizeScreenPoint(newPosition);
        experimentRunView.fromScreen(newPosition, newReal);

        if (row == 0) {
            MarkerData marker2 = markerData.getMarkerDataAt(1);
            if (newReal.x > marker2.getPosition().x)
                markerData.setMarkerPosition(newReal, 1);
        } else {
            MarkerData marker1 = markerData.getMarkerDataAt(0);
            if (newReal.x < marker1.getPosition().x)
                markerData.setMarkerPosition(newReal, 0);
        }
        markerData.setMarkerPosition(newReal, row);
    }
}


public class StartEndSeekBar extends MarkerView implements IExperimentRunView {
    private MarkersDataModel markersDataModel;

    public StartEndSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        markersDataModel = new MarkersDataModel();
        markersDataModel.addMarkerData(new MarkerData(0));
        markersDataModel.addMarkerData(new MarkerData(1));

        addPainter(new StartEndPainter(this, this, markersDataModel));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (IMarkerDataModelPainter markerPainter : markerPainterList)
            markerPainter.draw(canvas, 1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getLayoutParams().width, 35);
    }

    public MarkersDataModel getMarkersDataModel() {
        return markersDataModel;
    }

    @Override
    public void setCurrentRun(int run) {

    }

    @Override
    public int getNumberOfRuns() {
        return 0;
    }

    @Override
    public void fromScreen(PointF screen, PointF real) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int barWidth = viewFrame.width() - paddingLeft - paddingRight;

        if (screen.x < paddingLeft)
            screen.x = paddingLeft;
        if (screen.x > viewFrame.width() - paddingRight)
            screen.x = viewFrame.width() - paddingRight;

        screen.x -= paddingLeft;

        real.x = screen.x / barWidth;
        real.y = 0;
    }

    @Override
    public void toScreen(PointF real, PointF screen) {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int barWidth = viewFrame.width() - paddingLeft - paddingRight;

        screen.x = paddingLeft + real.x * barWidth;
        screen.y = 0;
    }
}
