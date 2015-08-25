package nz.ac.auckland.lablet.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import nz.ac.auckland.lablet.experiment.PointDataModel;
import nz.ac.auckland.lablet.views.plotview.PlotPainterContainerView;

/*
 *
 * Authors:
 *      Jamie Diprose <jdip004@aucklanduni.ac.nz>
 */


/**
 * Marker for region of interest.
 */
class ROIMarker extends SimpleMarker {
    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (isSelectedForDrag())
            super.onDraw(canvas, priority);
    }

    /**
     * Dragging a origin marker needs special treatment since it also affects the other two markers in the coordinate
     * system.
     * <p>
     * Call the painter class that then updates all the markers.
     * </p>
     *
     * @param point the new position the marker was dragged to
     */
    @Override
    protected void onDraggedTo(PointF point) {
        ROIMarkerPainter rectMarkerPainter = (ROIMarkerPainter)parent;
        rectMarkerPainter.onDraggedTo(this, point);
    }
}
public class ROIMarkerPainter extends AbstractMarkerPainter {
    // device independent sizes:
    private final int FONT_SIZE_DP = 20;
    private final float LINE_WIDTH_DP = 2f;
    private final float WING_LENGTH_DP = 10;

    // pixel sizes, set in the constructor
    private int FONT_SIZE;
    private float LINE_WIDTH;
    private float WING_LENGTH;
    private boolean isVisible = true;

    public static final int TOP_LEFT = 3;
    public static final int TOP_RIGHT = 2;
    public static final int BTM_LEFT = 1;
    public static final int BTM_RIGHT = 0;


    public ROIMarkerPainter(PointDataModel model) {
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
        return new ROIMarker();
    }

    private PointF getCurrentScreenPos(int markerIndex) {
        return ((DraggableMarker)markerList.get(markerIndex)).getCachedScreenPosition();
    }

    protected void onDraggedTo(DraggableMarker marker, PointF newPosition)
    {
        int index = markerList.indexOf(marker);
        PointDataModel model  = this.getMarkerModel();

        PointF newPositionReal = new PointF();
        containerView.fromScreen(newPosition, newPositionReal);

        switch (index) {
            case TOP_LEFT:
                PointF btmRightScreen = getCurrentScreenPos(BTM_RIGHT);
                PointF btmRightReal = new PointF();
                containerView.fromScreen(btmRightScreen, btmRightReal);

                model.getMarkerDataAt(TOP_RIGHT).setPosition(new PointF(btmRightReal.x, newPositionReal.y));
                model.getMarkerDataAt(BTM_LEFT).setPosition(new PointF(newPositionReal.x, btmRightReal.y));
                break;

            case TOP_RIGHT:
                PointF btmLeftScreen = getCurrentScreenPos(BTM_LEFT);
                PointF btmLeftReal = new PointF();
                containerView.fromScreen(btmLeftScreen, btmLeftReal);

                model.getMarkerDataAt(TOP_LEFT).setPosition(new PointF(btmLeftReal.x, newPositionReal.y));
                model.getMarkerDataAt(BTM_RIGHT).setPosition(new PointF(newPositionReal.x, btmLeftReal.y));
                break;

            case BTM_LEFT:
                PointF topRightScreen = getCurrentScreenPos(TOP_RIGHT);
                PointF topRightReal = new PointF();
                containerView.fromScreen(topRightScreen, topRightReal);

                model.getMarkerDataAt(TOP_LEFT).setPosition(new PointF(newPositionReal.x, topRightReal.y));
                model.getMarkerDataAt(BTM_RIGHT).setPosition(new PointF(topRightReal.x, newPositionReal.y));
                break;

            case BTM_RIGHT:
                PointF topLeftScreen = getCurrentScreenPos(TOP_LEFT);
                PointF topLeftReal = new PointF();
                containerView.fromScreen(topLeftScreen, topLeftReal);
                model.getMarkerDataAt(TOP_RIGHT).setPosition(new PointF(newPositionReal.x, topLeftReal.y));
                model.getMarkerDataAt(BTM_LEFT).setPosition(new PointF(topLeftReal.x, newPositionReal.y));
                break;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {

        if(this.getMarkerModel().isVisible())
        {
            for (IMarker marker : markerList)
                marker.onDraw(canvas, 1);

            float left;
            float top;
            float right;
            float bottom;

            if(markerList.get(BTM_LEFT).isSelectedForDrag() || markerList.get(TOP_RIGHT).isSelectedForDrag())
            {
                PointF topRight = getCurrentScreenPos(TOP_RIGHT);
                PointF btmLeft = getCurrentScreenPos(BTM_LEFT);
                left = btmLeft.x;
                top = topRight.y;
                right = topRight.x;
                bottom = btmLeft.y;
            }
            else
            {
                PointF topLeft = getCurrentScreenPos(TOP_LEFT);
                PointF btmRight = getCurrentScreenPos(BTM_RIGHT);
                left = topLeft.x;
                top = topLeft.y;
                right = btmRight.x;
                bottom = btmRight.y;
            }

            // Line settings
            Paint paint = new Paint();
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeWidth(LINE_WIDTH);
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);

            canvas.drawRect(left, top, right, bottom, paint); //See android.Graphics.Rect constructor for meaning of params

        }
    }
}
