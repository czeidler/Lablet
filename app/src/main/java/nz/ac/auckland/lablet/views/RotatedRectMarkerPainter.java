package nz.ac.auckland.lablet.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;

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
class RotatedRectMarker extends SimpleMarker {
    @Override
    public void onDraw(Canvas canvas, float priority) {
        if (isSelectedForDrag())
            super.onDraw(canvas, priority);
    }
}

public class RotatedRectMarkerPainter extends AbstractMarkerPainter {
    // device independent sizes:
    private final float LINE_WIDTH_DP = 2f;

    // pixel sizes, set in the constructor
    private float LINE_WIDTH;
    private boolean isVisible = true;

    public static final int TOP_LEFT = 0;
    public static final int TOP_RIGHT = 1;
    public static final int BTM_LEFT = 2;
    public static final int BTM_RIGHT = 3;
    private Integer currentFrame = null;

    public RotatedRectMarkerPainter(PointDataModel model) {
        super(model);
    }

    @Override
    public void setContainer(PlotPainterContainerView view) {
        super.setContainer(view);

        if (view == null)
            return;

        LINE_WIDTH = toPixel(LINE_WIDTH_DP);
    }

//    @Override
//    protected DraggableMarker createMarkerForRow(int row) {
//        return new RotatedRectMarker();
//    }

    private PointF getCurrentScreenPos(int markerIndex) {
        return ((DraggableMarker)markerList.get(markerIndex)).getCachedScreenPosition();
    }

    public void setCurrentFrame(int frame, @Nullable PointF insertHint) {
        this.currentFrame = frame;
    }

    @Override
    public void onDraw(Canvas canvas) {

        if(this.getMarkerModel().isVisible())
        {

            int currentFrame = markerData.getSelectedMarkerData();

            markerData.
            markerData.get
            PointF topLeft = markerData.getMarkerDataAt(RotatedRectMarkerPainter.TOP_LEFT).getPosition();
            PointF topRight = markerData.getMarkerDataAt(RotatedRectMarkerPainter.TOP_RIGHT).getPosition();
            PointF btmRight = markerData.getMarkerDataAt(RotatedRectMarkerPainter.BTM_RIGHT).getPosition();
            PointF btmLeft = markerData.getMarkerDataAt(RotatedRectMarkerPainter.BTM_LEFT).getPosition();

            markerData.getMarkerDataAt().getFrameId()
            // Line settings
            Paint paint = new Paint();
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeWidth(LINE_WIDTH);
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);

            canvas.drawLine(topLeft.x, topLeft.y, topRight.x, topRight.y, paint);
            canvas.drawLine(topRight.x, topRight.y, btmRight.x, btmRight.y, paint);
            canvas.drawLine(btmRight.x, btmRight.y, btmLeft.x, btmLeft.y, paint);
            canvas.drawLine(btmLeft.x, btmLeft.y, topLeft.x, topLeft.y, paint);
        }
    }
}
