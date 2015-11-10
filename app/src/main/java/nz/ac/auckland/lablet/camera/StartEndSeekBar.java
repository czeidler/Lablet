/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.content.Context;
import android.graphics.*;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import nz.ac.auckland.lablet.views.marker.MarkerData;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;
import nz.ac.auckland.lablet.views.marker.AbstractMarkerPainter;
import nz.ac.auckland.lablet.views.marker.DraggableMarker;
import nz.ac.auckland.lablet.views.marker.IMarker;
import nz.ac.auckland.lablet.views.plotview.IPlotPainter;
import nz.ac.auckland.lablet.views.plotview.PlotPainterContainerView;


/**
 * Abstract base class that shares code for the start and the end marker.
 */
abstract class StartEndMarker extends DraggableMarker<MarkerData> {
    // dimensions in density-independent  pixels
    private final float WIDTH_DP = 20;

    // dimensions in pixels, calculated in the constructor; use them for drawing
    protected float WIDTH;
    protected float HEIGHT;
    protected int TRIANGLE_HEIGHT;

    protected Paint lightColor = new Paint();
    protected Paint darkenColor = new Paint();
    protected Paint selectedGlowPaint = new Paint();

    public StartEndMarker() {
        lightColor.setColor(Color.rgb(97, 204, 238));
        lightColor.setAntiAlias(true);
        lightColor.setStyle(Paint.Style.FILL);

        darkenColor.setColor(Color.rgb(30, 146, 209));
        darkenColor.setAntiAlias(true);
        darkenColor.setStyle(Paint.Style.FILL);
        darkenColor.setPathEffect(new CornerPathEffect(2));

        selectedGlowPaint.setColor(lightColor.getColor());
    }

    @Override
    public void setTo(AbstractMarkerPainter painter, MarkerData markerData) {
        super.setTo(painter, markerData);

        WIDTH = parent.toPixel(WIDTH_DP);
        HEIGHT = parent.toPixel(StartEndSeekBar.HEIGHT_DP);
        TRIANGLE_HEIGHT = parent.toPixel(StartEndSeekBar.HEIGHT_DP * 0.5f);
    }

    @Override
    public boolean isPointOnSelectArea(PointF screenPoint) {
        PointF position = parent.getMarkerScreenPosition(markerData);
        // build a marker rect with increased width
        final float touchWidth = (float)parent.toPixel(60);
        RectF rect = new RectF();
        rect.left = position.x - touchWidth / 2;
        rect.top = 0;
        rect.right = position.x + touchWidth / 2;
        rect.bottom = HEIGHT + 1;

        return rect.contains(screenPoint.x, screenPoint.y);
    }

    protected void drawGlow(Canvas canvas, float position) {
        final float x = position;
        final float y = 0;
        final float radius = 3 * HEIGHT;

        canvas.save();
        canvas.translate(0, HEIGHT);
        canvas.scale(1f, 0.2f);

        Shader shader = new RadialGradient(x, y, radius, lightColor.getColor(),
                Color.TRANSPARENT, Shader.TileMode.CLAMP);
        selectedGlowPaint.setShader(shader);
        canvas.drawCircle(x, y, radius, selectedGlowPaint);

        canvas.restore();
    }
}


/**
 * Implementation of a "start" marker. (Copies the google text select markers.)
 */
class StartMarker extends StartEndMarker {

    @Override
    public void onDraw(Canvas canvas, float priority) {
        PointF position = parent.getMarkerScreenPosition(markerData);

        if (isDragging) {
            PointF dragPosition = getTouchPosition();
            drawGlow(canvas, dragPosition.x);
        }

        // Note: don't use drawRectangle because in some cases it does not align with drawPath for the triangle...

        // darken bottom
        Path path = new Path();
        path.moveTo(position.x, TRIANGLE_HEIGHT);
        path.lineTo(position.x, HEIGHT);
        path.lineTo(position.x - WIDTH, HEIGHT);
        path.lineTo(position.x - WIDTH, TRIANGLE_HEIGHT - 2);
        canvas.drawPath(path, darkenColor);

        // bright triangle
        path = new Path();
        path.moveTo(position.x, 0);
        path.lineTo(position.x, TRIANGLE_HEIGHT);
        path.lineTo(position.x - WIDTH, TRIANGLE_HEIGHT);
        canvas.drawPath(path, lightColor);
    }
}


/**
 * Implementation of a "end" marker. (Copies the google text select markers.)
 */
class EndMarker extends StartEndMarker {

    @Override
    public void onDraw(Canvas canvas, float priority) {
        PointF position = parent.getMarkerScreenPosition(markerData);

        if (isDragging) {
            PointF dragPosition = getTouchPosition();
            drawGlow(canvas, dragPosition.x);
        }

        // Note: don't use drawRectangle because in some cases it does not align with drawPath for the triangle...

        // darken bottom
        Path path = new Path();
        path.moveTo(position.x, TRIANGLE_HEIGHT);
        path.lineTo(position.x, HEIGHT);
        path.lineTo(position.x + WIDTH, HEIGHT);
        path.lineTo(position.x + WIDTH, TRIANGLE_HEIGHT - 2);
        canvas.drawPath(path, darkenColor);

        // bright triangle
        path = new Path();
        path.moveTo(position.x, 0);
        path.lineTo(position.x, TRIANGLE_HEIGHT);
        path.lineTo(position.x + WIDTH, TRIANGLE_HEIGHT);
        canvas.drawPath(path, lightColor);
    }
}


/**
 * Painter for the start and end marker.
 * <p>
 * The used data model should contain exactly two data points.
 * </p>
 */
class StartEndPainter extends AbstractMarkerPainter<MarkerData> {
    int numberOfSteps = 10;

    /**
     * Constructor.
     *
     * @param data should contain exactly two data points, one for the start and one for the end marker
     */
    public StartEndPainter(MarkerDataModel data) {
        super(data);
    }

    @Override
    protected DraggableMarker createMarkerForRow(int row) {
        if (row == 0)
            return new StartMarker();
        else
            return new EndMarker();
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (IMarker marker : markerList)
            marker.onDraw(canvas, 1);
    }

    @Override
    public void markerMoveRequest(DraggableMarker marker, PointF newPosition, boolean isDragging) {
        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;

        PointF newReal = new PointF();
        sanitizeScreenPoint(newPosition);
        containerView.fromScreen(newPosition, newReal);
        newReal.x = toStepPosition(newReal.x);

        if (row == 0) {
            MarkerData marker2 = markerData.getAt(1);
            if (newReal.x > marker2.getPosition().x)
                markerData.setPosition(newReal, 1);
        } else {
            MarkerData marker1 = markerData.getAt(0);
            if (newReal.x < marker1.getPosition().x)
                markerData.setPosition(newReal, 0);
        }
        markerData.setPosition(newReal, row);
    }

    public void setNumberOfSteps(int steps) {
        numberOfSteps = steps;

        MarkerData marker1 = markerData.getAt(0);
        marker1.getPosition().x = toStepPosition(marker1.getPosition().x);
        markerData.setPosition(marker1.getPosition(), 0);

        MarkerData marker2 = markerData.getAt(1);
        marker2.getPosition().x = toStepPosition(marker2.getPosition().x);
        markerData.setPosition(marker2.getPosition(), 1);
    }

    private float toStepPosition(float floatPosition) {
        if (numberOfSteps <= 2)
            return 0.5f;
        float stepSize = 1.0f / (numberOfSteps - 1);
        int stepPosition = Math.round(floatPosition / stepSize);
        return stepSize * stepPosition;
    }
}


/**
 * A seek bar with a start and an end marker. For example, used to select video start and end point.
 */
public class StartEndSeekBar extends PlotPainterContainerView {
    private MarkerDataModel markerDataModel;
    private StartEndPainter startEndPainter;

    // dimensions in density-independent  pixels
    public static final float HEIGHT_DP = 35;

    public StartEndSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        markerDataModel = new MarkerDataModel();
        markerDataModel.addMarkerData(new MarkerData(0));
        markerDataModel.addMarkerData(new MarkerData(1));

        setXRange(0, 1);
        setYRange(0, 1);

        startEndPainter = new StartEndPainter(markerDataModel);
        addPlotPainter(startEndPainter);
        invalidate();
    }

    public float getStart() {
        return markerDataModel.getMarkerDataAt(0).getPosition().x;
    }

    public float getEnd() {
        return markerDataModel.getMarkerDataAt(1).getPosition().x;
    }

    private void setStart(float start) {
        PointF point = markerDataModel.getMarkerDataAt(0).getPosition();
        point.x = start;
        markerDataModel.setMarkerPosition(point, 0);
    }

    private void setEnd(float end) {
        PointF point = markerDataModel.getMarkerDataAt(1).getPosition();
        point.x = end;
        markerDataModel.setMarkerPosition(point, 1);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);
        savedState.start = getStart();
        savedState.end = getEnd();

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());

        setStart(savedState.start);
        setEnd(savedState.end);
    }

    static class SavedState extends BaseSavedState {
        float start;
        float end;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            start = in.readFloat();
            end = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(start);
            out.writeFloat(end);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (IPlotPainter markerPainter : allPainters)
            markerPainter.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LayoutParams params = getLayoutParams();
        assert params != null;
        setMeasuredDimension(params.width, startEndPainter.toPixel(HEIGHT_DP));
    }

    public MarkerDataModel getMarkerDataModel() {
        return markerDataModel;
    }

    @Override
    public float toScreenX(float real) {
        float paddingLeft = getPaddingLeft();
        float paddingRight = getPaddingRight();
        float width = viewWidth - paddingLeft - paddingRight;
        return paddingLeft + (real - rangeRect.left) / (rangeRect.right - rangeRect.left) * width;
    }

    @Override
    public float toScreenY(float real) {
        float paddingTop = getPaddingTop();
        float paddingBottom = getPaddingBottom();
        float height = viewHeight - paddingTop - paddingBottom;
        return paddingTop + (1.f - (real - rangeRect.bottom) / (rangeRect.top - rangeRect.bottom)) * height;
    }

    /**
     * Set range from 0 to max.
     *
     * @param max the end of the range
     */
    public void setMax(int max) {
        startEndPainter.setNumberOfSteps(max + 1);
    }
}
