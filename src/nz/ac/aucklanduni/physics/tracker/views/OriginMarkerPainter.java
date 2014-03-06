package nz.ac.aucklanduni.physics.tracker.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;
import nz.ac.aucklanduni.physics.tracker.Calibration;
import nz.ac.aucklanduni.physics.tracker.IExperimentRunView;
/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
import nz.ac.aucklanduni.physics.tracker.MarkersDataModel;


public class OriginMarkerPainter extends AbstractMarkersPainter implements Calibration.ICalibrationListener {
    private Calibration calibration;
    private float angleScreen;
    private boolean firstDraw = true;

    public OriginMarkerPainter(View parent, IExperimentRunView runView, MarkersDataModel model,
                               Calibration calibration) {
        super(parent, runView, model);
        this.calibration = calibration;
        this.calibration.addListener(this);
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
    protected DragableMarker createMarkerForRow(int row) {
        return new OriginMarker(this);
    }

    @Override
    public void draw(Canvas canvas, float priority) {
        if (firstDraw) {
            firstDraw = false;
            setToScreenFromReal(markerData.getMarkerDataAt(0).getPosition(),
                    markerData.getMarkerDataAt(1).getPosition());

            // also init the correct y axis marker position
            PointF yAxis = new PointF();
            experimentRunView.fromScreen(getScreenPos(2), yAxis);
            markerData.setMarkerPosition(yAxis, 2);
        }

        for (IMarker marker : markerList)
            marker.onDraw(canvas, priority);

        if (markerData.getMarkerCount() != 3)
            return;

        PointF origin = getScreenPos(0);
        PointF xAxis = getScreenPos(1);
        PointF yAxis = getScreenPos(2);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        canvas.drawLine(origin.x, origin.y, xAxis.x, xAxis.y, paint);
        canvas.drawLine(origin.x, origin.y, yAxis.x, yAxis.y, paint);

        // draw labels
        paint.setTextSize(20);
        String label1;
        String label2;
        float textAngle = angleScreen;
        PointF label1Position = new PointF();
        PointF label2Position = new PointF();
        // text height from baseline to top:
        float textHeight = -paint.ascent() - paint.descent();
        if (calibration.getSwapAxis()) {
            label1 = "y";
            label2 = "x";
            textAngle += 90;
            label1Position.set(origin);
            label1Position.x += getScreenAxisLength();
            label1Position.x -= textHeight + 1;
            label1Position.y += 1;
            transform(label1Position);

            label2Position.set(origin);
            label2Position.y -= getScreenAxisLength();
            label2Position.x -= textHeight + 1;
            label2Position.y += 1;
            transform(label2Position);
        } else {
            label1 = "x";
            label2 = "y";
            label1Position.set(origin);
            label1Position.x += getScreenAxisLength();
            label1Position.x -= paint.measureText(label1);
            label1Position.y += textHeight + 1;
            transform(label1Position);

            label2Position.set(origin);
            label2Position.y -= getScreenAxisLength();
            label2Position.x -= paint.measureText(label2);
            label2Position.y += textHeight + 1;
            transform(label2Position);
        }
        canvas.save();
        canvas.translate(label1Position.x, label1Position.y);
        canvas.rotate(textAngle);
        canvas.drawText(label1, 0, 0, paint);
        canvas.restore();
        canvas.save();
        canvas.translate(label2Position.x, label2Position.y);
        canvas.rotate(textAngle);
        canvas.drawText(label2, 0, 0, paint);
        canvas.restore();
    }

    private void transform(PointF point) {
        PointF origin = getScreenPos(0);
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
    public void markerMoveRequest(DragableMarker marker, PointF newPosition) {
        onDraggedTo(marker, newPosition);

        PointF origin = new PointF();
        PointF xAxis = new PointF();
        PointF yAxis = new PointF();
        experimentRunView.fromScreen(getScreenPos(0), origin);
        experimentRunView.fromScreen(getScreenPos(1), xAxis);
        experimentRunView.fromScreen(getScreenPos(2), yAxis);

        markerData.setMarkerPosition(origin, 0);
        markerData.setMarkerPosition(xAxis, 1);
        markerData.setMarkerPosition(yAxis, 2);
    }

    protected void onDraggedTo(DragableMarker marker, PointF newPosition) {
        int row = markerList.lastIndexOf(marker);
        if (row < 0)
            return;

        if (row == 0) {
            // translation
            sanitizeScreenPoint(newPosition);
            setToScreenFromScreen(newPosition);
        } else {
            // x rotation
            PointF origin = new PointF();
            origin.set(markerData.getMarkerDataAt(0).getPosition());
            PointF originScreen = new PointF();
            experimentRunView.toScreen(origin, originScreen);
            float angle = Calibration.getAngle(originScreen, newPosition);
            if (row == 2)
                angle += 90;

            setToScreenFromScreen(originScreen, angle);
        }
    }

    private void setToScreenFromScreen(PointF originScreen, float angleScreen) {
        this.angleScreen = angleScreen;

        setToScreenFromScreen(originScreen);
    }

    private void setToScreenFromScreen(PointF originScreen) {

        float axisLength = getScreenAxisLength();
        PointF xAxisScreen = new PointF(originScreen.x + axisLength, originScreen.y);
        transform(xAxisScreen);
        PointF yAxisScreen = new PointF(originScreen.x, originScreen.y - axisLength);
        transform(yAxisScreen);

        setScreenPos(0, originScreen);
        setScreenPos(1, xAxisScreen);
        setScreenPos(2, yAxisScreen);
    }

    private void setToScreenFromReal(PointF origin, PointF axis1) {
        PointF originScreen = new PointF();
        experimentRunView.toScreen(origin, originScreen);
        PointF xAxisScreen = new PointF();
        experimentRunView.toScreen(axis1, xAxisScreen);
        angleScreen = Calibration.getAngle(originScreen, xAxisScreen);

        setToScreenFromScreen(originScreen, angleScreen);
    }

    private float getScreenAxisLength() {
        final PointF axisLengthPoint = new PointF(25, 0);
        PointF screen = new PointF();
        experimentRunView.toScreen(axisLengthPoint, screen);
        return screen.x;
    }


    private PointF getScreenPos(int markerIndex) {
        return markerList.get(markerIndex).getPosition();
    }

    private void setScreenPos(int markerIndex, PointF point) {
        markerList.get(markerIndex).setPosition(point);
    }

    @Override
    public void onCalibrationChanged() {
        // just trigger a redraw
        markerView.invalidate();
    }
}
