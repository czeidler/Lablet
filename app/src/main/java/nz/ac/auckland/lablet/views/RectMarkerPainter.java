package nz.ac.auckland.lablet.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.plotview.PlotPainterContainerView;

/*
 *
 * Authors:
 *      Jamie Diprose <jdip004@aucklanduni.ac.nz>
 */


/**
 * Marker for region of interest.
 */
class RectMarker extends SimpleMarker {
    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (isSelectedForDrag())
            super.onDraw(canvas, priority);
    }
}

/**
 * Draws a rectangle used for a region of interest.
 * <p>
 * The painter expect a {@link MarkerDataModel} with two data points; top left corner
 * and bottom right corner of the rectangle.
 * </p>
 */
public class RectMarkerPainter extends AbstractMarkerPainter {
    // device independent sizes:
    private final int FONT_SIZE_DP = 20;
    private final float LINE_WIDTH_DP = 2f;
    private final float WING_LENGTH_DP = 10;

    // pixel sizes, set in the constructor
    private int FONT_SIZE;
    private float LINE_WIDTH;
    private float WING_LENGTH;

    public RectMarkerPainter(MarkerDataModel model) {
        super(model);
    }

    @Override
    public void setContainer(PlotPainterContainerView view) {
        super.setContainer(view);

        if (view == null)
            return;

        FONT_SIZE = toPixel(FONT_SIZE_DP);
        LINE_WIDTH = toPixel(LINE_WIDTH_DP);
        WING_LENGTH = toPixel(WING_LENGTH_DP);
    }

    @Override
    protected DraggableMarker createMarkerForRow(int row) {
        return new RectMarker();
    }

    private PointF getCurrentScreenPos(int markerIndex) {
        return ((DraggableMarker)markerList.get(markerIndex)).getCachedScreenPosition();
    }

    @Override
    public void onDraw(Canvas canvas) {

        // Rectangle corner points
        PointF btmRight = getCurrentScreenPos(0);
        PointF topLeft = getCurrentScreenPos(1);

        // Line settings
        Paint paint = new Paint();
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(topLeft.x, topLeft.y, btmRight.x, btmRight.y, paint); //See android.Graphics.Rect constructor for meaning of params
       // canvas.drawRect(10, 10, 100, 100, paint);
        //for (IMarker marker : markerList)
         //   marker.onDraw(canvas, 1);
    }
}
