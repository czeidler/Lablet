package nz.ac.auckland.lablet.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import nz.ac.auckland.lablet.experiment.Calibration;
/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.plotview.PlotPainterContainerView;


/**
 * Marker for the origin coordinate system.
 */
class OriginMarker extends SimpleMarker {

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
     * @param point the new position the marker was dragged to
     */
    @Override
    protected void onDraggedTo(PointF point) {
        OriginMarkerPainter originMarkerPainter = (OriginMarkerPainter)parent;
        originMarkerPainter.onDraggedTo(this, point);
    }
}


/**
 * Draws the origin coordinate system.
 * <p>
 * Expects a MarkerDataModel with two data points. One for the origin and one for the first axis.
 * </p>
 */
public class OriginMarkerPainter extends AbstractMarkerPainter implements Calibration.ICalibrationListener {
    private Calibration calibration;
    private float angleScreen;
    private boolean firstDraw = true;

    // device independent sizes:
    private final int FONT_SIZE_DP = 20;
    private final float LINE_WIDTH_DP = 1.5f;
    private final float ARROW_WIDTH_DP = 3f;
    private final float ARROW_LENGTH_DP = 12f;
    private final float ARROW_AXIS_OVERLAP_DP = 10f;

    // pixel sizes, set in the constructor
    private int FONT_SIZE;
    private float LINE_WIDTH;
    private float ARROW_WIDTH;
    private float ARROW_LENGTH;
    private float ARROW_AXIS_OVERLAP;
    private float LABEL_TO_AXIS_END_DISTANCE;

    public OriginMarkerPainter(MarkerDataModel model, Calibration calibration) {
        super(model);
        this.calibration = calibration;
        this.calibration.addListener(this);
    }

    @Override
    public void setContainer(PlotPainterContainerView view) {
        super.setContainer(view);

        FONT_SIZE = toPixel(FONT_SIZE_DP);
        LINE_WIDTH = toPixel(LINE_WIDTH_DP);
        ARROW_WIDTH = toPixel(ARROW_WIDTH_DP);
        ARROW_LENGTH = toPixel(ARROW_LENGTH_DP);
        ARROW_AXIS_OVERLAP = toPixel(ARROW_AXIS_OVERLAP_DP);
        LABEL_TO_AXIS_END_DISTANCE = toPixel(ARROW_AXIS_OVERLAP_DP);
    }

    protected void finalize() {
        calibration.removeListener(this);
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    protected DraggableMarker createMarkerForRow(int row) {
        return new OriginMarker();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (firstDraw) {
            firstDraw = false;
            setRealFromScreen(getMarkerScreenPosition(0));
        }

        for (IMarker marker : markerList)
            marker.onDraw(canvas, 1);

        if (markerData.getMarkerCount() != 3)
            return;

        PointF origin = getMarkerScreenPosition(0);
        PointF xAxis = getMarkerScreenPosition(1);
        PointF yAxis = getMarkerScreenPosition(2);

        Paint paint = new Paint();
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        canvas.drawLine(origin.x, origin.y, xAxis.x, xAxis.y, paint);
        canvas.drawLine(origin.x, origin.y, yAxis.x, yAxis.y, paint);

        // draw labels
        paint.setTextSize(FONT_SIZE);
        String label1;
        String label2;
        String labelOrigin = "0";
        float textAngle = angleScreen;
        PointF labelOriginPosition = new PointF();
        PointF label1Position = new PointF();
        PointF label2Position = new PointF();
        // text height from baseline to top:
        final float textHeight = -paint.ascent();
        final float textXOffset = 1;
        final float textYOffset = 0;
        final float labelAxisLength = getScreenAxisLength() - LABEL_TO_AXIS_END_DISTANCE;
        if (calibration.getSwapAxis()) {
            label1 = "Y";
            label2 = "X";
            textAngle += 90;

            labelOriginPosition.set(origin);
            labelOriginPosition.x -= textHeight + textYOffset;
            labelOriginPosition.y -= paint.measureText(labelOrigin) / 2;

            label1Position.set(origin);
            label1Position.x += labelAxisLength;
            label1Position.x -= textHeight + textYOffset;
            label1Position.y += 1;

            label2Position.set(origin);
            label2Position.y -= labelAxisLength;
            label2Position.x -= textHeight + textYOffset;
            label2Position.y += 1;
        } else {
            label1 = "X";
            label2 = "Y";

            labelOriginPosition.set(origin);
            labelOriginPosition.x -= paint.measureText(labelOrigin) / 2;
            labelOriginPosition.y += textHeight + textYOffset;

            label1Position.set(origin);
            label1Position.x += labelAxisLength;
            label1Position.x -= paint.measureText(label1);
            label1Position.y += textHeight + textYOffset;

            label2Position.set(origin);
            label2Position.y -= labelAxisLength;
            label2Position.x -= paint.measureText(label2) + textXOffset;
            label2Position.y += textHeight;
        }

        transform(labelOriginPosition, origin);
        transform(label1Position, origin);
        transform(label2Position, origin);

        // 0-title
        drawLabel(canvas, paint, labelOrigin, labelOriginPosition, textAngle);
        // x-title
        drawLabel(canvas, paint, label1, label1Position, textAngle);
        // y-title
        drawLabel(canvas, paint, label2, label2Position, textAngle);

        drawArrows(canvas, origin, paint);
    }

    private void drawLabel(Canvas canvas, Paint paint, String label, PointF position, float textAngle) {
        canvas.save();
        canvas.translate(position.x, position.y);
        canvas.rotate(textAngle);
        canvas.drawText(label, 0, 0, paint);
        canvas.restore();
    }

    private void drawArrows(Canvas canvas, PointF origin, Paint paint) {
        // do a prototype arrow in x direction
        PointF xArrowTip = new PointF();
        xArrowTip.x += getScreenAxisLength() + ARROW_LENGTH - ARROW_AXIS_OVERLAP;

        PointF xArrowTopEnd = new PointF();
        xArrowTopEnd.set(xArrowTip);
        xArrowTopEnd.x -= ARROW_LENGTH;
        xArrowTopEnd.y += ARROW_WIDTH;
        PointF xArrowBottomEnd = new PointF();
        xArrowBottomEnd.set(xArrowTip);
        xArrowBottomEnd.x -= ARROW_LENGTH;
        xArrowBottomEnd.y -= ARROW_WIDTH;

        canvas.save();
        canvas.translate(origin.x, origin.y);
        canvas.rotate(angleScreen);
        canvas.drawLine(xArrowTopEnd.x, xArrowTopEnd.y, xArrowTip.x, xArrowTip.y, paint);
        canvas.drawLine(xArrowTip.x, xArrowTip.y, xArrowBottomEnd.x, xArrowBottomEnd.y, paint);
        canvas.restore();

        canvas.save();
        canvas.translate(origin.x, origin.y);
        canvas.rotate(angleScreen - 90);
        canvas.drawLine(xArrowTopEnd.x, xArrowTopEnd.y, xArrowTip.x, xArrowTip.y, paint);
        canvas.drawLine(xArrowTip.x, xArrowTip.y, xArrowBottomEnd.x, xArrowBottomEnd.y, paint);
        canvas.restore();
    }

    private void transform(PointF point, PointF origin) {
        PointF diff = new PointF();
        diff.x = point.x - origin.x;
        diff.y = point.y - origin.y;

        float x = diff.x;
        float y = diff.y;
        point.x = (float)Math.cos(Math.toRadians(angleScreen)) * x - (float)Math.sin(Math.toRadians(angleScreen)) * y;
        point.y = (float)Math.cos(Math.toRadians(angleScreen)) * y + (float)Math.sin(Math.toRadians(angleScreen)) * x;

        point.x += origin.x;
        point.y += origin.y;
    }

    @Override
    public void markerMoveRequest(DraggableMarker marker, PointF newPosition, boolean isDragging) {
        // don't update all the time
        if (isDragging)
            return;

        onDraggedTo(marker, newPosition);
    }

    /**
     * Is called when one of the markers has been dragged.
     * <p>
     * This method is responsible to update the whole coordinate system according to the dragged marker.
     * </p>
     * @param marker the dragged marker
     * @param newPosition the new position of the dragged marker
     */
    protected void onDraggedTo(DraggableMarker marker, PointF newPosition) {
        int row = markerList.indexOf(marker);
        if (row < 0)
            return;

        if (row == 0) {
            // translation
            sanitizeScreenPoint(newPosition);
            setRealFromScreen(newPosition);
        } else {
            // x rotation
            PointF origin = new PointF();
            origin.set(markerData.getMarkerDataAt(0).getPosition());
            PointF originScreen = new PointF();
            containerView.toScreen(origin, originScreen);
            angleScreen = Calibration.getAngle(originScreen, newPosition);
            if (row == 2)
                angleScreen += 90;

            setRealFromScreen(originScreen);
        }
    }

    private void setRealFromScreen(PointF originScreen) {
        float axisLength = getScreenAxisLength();
        PointF xAxisScreen = new PointF(originScreen.x + axisLength, originScreen.y);
        transform(xAxisScreen, originScreen);
        PointF yAxisScreen = new PointF(originScreen.x, originScreen.y - axisLength);
        transform(yAxisScreen, originScreen);

        PointF origin = new PointF();
        PointF xAxis = new PointF();
        PointF yAxis = new PointF();
        containerView.fromScreen(originScreen, origin);
        containerView.fromScreen(xAxisScreen, xAxis);
        containerView.fromScreen(yAxisScreen, yAxis);

        markerData.setMarkerPosition(origin, 0);
        markerData.setMarkerPosition(xAxis, 1);
        markerData.setMarkerPosition(yAxis, 2);
    }

    private float getScreenAxisLength() {
        final PointF axisLengthPoint = new PointF(25, 0);
        PointF screen = new PointF();
        containerView.toScreen(axisLengthPoint, screen);
        return screen.x;
    }

    @Override
    public void onCalibrationChanged() {
        // just trigger a redraw
        containerView.invalidate();
    }
}
