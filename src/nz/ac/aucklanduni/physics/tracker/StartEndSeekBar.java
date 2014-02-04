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
import android.view.View;


class StartEndMarker extends DragableMarker {
    final float WIDTH = 30;
    final float HEIGHT = 35;

    Paint lightColor = new Paint();
    Paint darkenColor = new Paint();

    public StartEndMarker(AbstractMarkersPainter parentContainer) {
        super(parentContainer);

        lightColor.setColor(Color.rgb(0, 200, 0));
        lightColor.setAntiAlias(true);
        lightColor.setStyle(Paint.Style.FILL);

        darkenColor.setColor(Color.rgb(0, 180, 0));
        darkenColor.setAntiAlias(true);
        darkenColor.setStyle(Paint.Style.FILL);
    }

    @Override
    protected boolean isPointOnSelectArea(PointF point) {
        return getRect().contains(point.x, point.y);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {


        final int TRIANGLE_HEIGHT = 10;
        Path path = new Path();

        // bright left
        path.moveTo(position.x, 0);
        path.lineTo(position.x - WIDTH / 2, TRIANGLE_HEIGHT);
        path.lineTo(position.x, TRIANGLE_HEIGHT);
        canvas.drawPath(path, lightColor);

        RectF rect = getRect();
        rect.top = TRIANGLE_HEIGHT;
        rect.right = position.x;
        canvas.drawRect(rect, lightColor);

        // darken right
        path = new Path();
        path.moveTo(position.x - 1, 0);
        path.lineTo(position.x + WIDTH / 2, TRIANGLE_HEIGHT);
        path.lineTo(position.x - 1, TRIANGLE_HEIGHT);
        canvas.drawPath(path, darkenColor);

        rect = getRect();
        rect.top = TRIANGLE_HEIGHT;
        rect.left = position.x -1;
        canvas.drawRect(rect, darkenColor);
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
    int numberOfSteps = 10;

    public StartEndPainter(View parent, IExperimentRunView runView, MarkersDataModel data) {
        super(parent, runView, data);
    }

    @Override
    protected DragableMarker createMarkerForRow(int row) {
        return new StartEndMarker(this);
    }

    @Override
    public void draw(Canvas canvas, float priority) {
        for (IMarker marker : markerList)
            marker.onDraw(canvas, priority);
    }

    @Override
    public void markerMoveRequest(DragableMarker marker, PointF newPosition) {
        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;

        PointF newReal = new PointF();
        sanitizeScreenPoint(newPosition);
        experimentRunView.fromScreen(newPosition, newReal);
        newReal.x = toStepPosition(newReal.x);

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

    public void setNumberOfSteps(int steps) {
        numberOfSteps = steps;

        MarkerData marker1 = markerData.getMarkerDataAt(0);
        marker1.getPosition().x = toStepPosition(marker1.getPosition().x);
        markerData.setMarkerPosition(marker1.getPosition(), 0);

        MarkerData marker2 = markerData.getMarkerDataAt(1);
        marker2.getPosition().x = toStepPosition(marker2.getPosition().x);
        markerData.setMarkerPosition(marker2.getPosition(), 1);
    }

    private float toStepPosition(float floatPosition) {
        if (numberOfSteps <= 1)
            return 0.5f;
        float stepSize = 1.0f / (numberOfSteps - 1);
        int stepPosition = Math.round(floatPosition / stepSize);
        return stepSize * stepPosition;
    }
}


public class StartEndSeekBar extends MarkerView implements IExperimentRunView {
    private MarkersDataModel markersDataModel;
    private StartEndPainter startEndPainter;

    public StartEndSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        markersDataModel = new MarkersDataModel();
        markersDataModel.addMarkerData(new MarkerData(0));
        markersDataModel.addMarkerData(new MarkerData(1));

        startEndPainter = new StartEndPainter(this, this, markersDataModel);
        addMarkerPainter(startEndPainter);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (IMarkerDataModelPainter markerPainter : markerPainterList)
            markerPainter.draw(canvas, 1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LayoutParams params = getLayoutParams();
        assert params != null;
        setMeasuredDimension(params.width, 35);
    }

    public MarkersDataModel getMarkersDataModel() {
        return markersDataModel;
    }

    @Override
    public void setCurrentRun(int run) {

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

    public void setMax(int max) {
        startEndPainter.setNumberOfSteps(max + 1);
    }
}
