package nz.ac.aucklanduni.physics.tracker.views;

import android.graphics.*;
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


public class CalibrationMarkerPainter extends AbstractMarkersPainter {
    // device independent sizes:
    private final int FONT_SIZE_DP = 20;
    private final float LINE_WIDTH_DP = 2f;
    private final float WING_LENGTH_DP = 10;

    // pixel sizes, set in the constructor
    private int FONT_SIZE;
    private float LINE_WIDTH;
    private float WING_LENGTH;

    public CalibrationMarkerPainter(View parent, IExperimentRunView runView, MarkersDataModel model) {
        super(parent, runView, model);

        FONT_SIZE = toPixel(FONT_SIZE_DP);
        LINE_WIDTH = toPixel(LINE_WIDTH_DP);
        WING_LENGTH = toPixel(WING_LENGTH_DP);
    }

    @Override
    protected DragableMarker createMarkerForRow(int row) {
        return new CalibrationMarker(this);
    }

    private void rotate(PointF point, PointF origin, float angleScreen) {
        float x = point.x - origin.x;
        float y = point.y - origin.y;
        point.x = (float)Math.cos(Math.toRadians(angleScreen)) * x - (float)Math.sin(Math.toRadians(angleScreen)) * y;
        point.y = (float)Math.cos(Math.toRadians(angleScreen)) * y + (float)Math.sin(Math.toRadians(angleScreen)) * x;
        point.x += origin.x;
        point.y += origin.y;
    }

    @Override
    public void draw(Canvas canvas, float priority) {
        for (IMarker marker : markerList)
            marker.onDraw(canvas, priority);

        if (markerData.getMarkerCount() != 2)
            return;

        // draw scale
        PointF screenPos1 = getScreenPos(0);
        PointF screenPos2 = getScreenPos(1);
        Paint paint = new Paint();
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        // shorten the line by LINE_WIDTH / 2 at both ends in order to not draw over the wings
        PointF normedVector = new PointF();
        normedVector.x = screenPos2.x - screenPos1.x;
        normedVector.y = screenPos2.y - screenPos1.y;
        double vectorLength = Math.sqrt(Math.pow(normedVector.x, 2) + Math.pow(normedVector.y, 2));
        normedVector.x /= vectorLength;
        normedVector.y /= vectorLength;
        canvas.drawLine(screenPos1.x + normedVector.x * LINE_WIDTH / 2, screenPos1.y + normedVector.y * LINE_WIDTH / 2,
                screenPos2.x - normedVector.x * LINE_WIDTH / 2, screenPos2.y - normedVector.y * LINE_WIDTH / 2, paint);
        // draw wings
        float angleScreen = Calibration.getAngle(screenPos1, screenPos2);
        PointF wing1Top = new PointF(screenPos1.x + LINE_WIDTH / 2, screenPos1.y + WING_LENGTH / 2);
        rotate(wing1Top, screenPos1, angleScreen);
        PointF wing1Bottom = new PointF(screenPos1.x + LINE_WIDTH / 2, screenPos1.y - WING_LENGTH / 2);
        rotate(wing1Bottom, screenPos1, angleScreen);
        canvas.drawLine(wing1Top.x, wing1Top.y, wing1Bottom.x, wing1Bottom.y, paint);
        PointF wing2Top = new PointF(screenPos2.x - LINE_WIDTH / 2, screenPos2.y + WING_LENGTH / 2);
        rotate(wing2Top, screenPos2, angleScreen);
        PointF wing2Bottom = new PointF(screenPos2.x - LINE_WIDTH / 2, screenPos2.y - WING_LENGTH / 2);
        rotate(wing2Bottom, screenPos2, angleScreen);
        canvas.drawLine(wing2Top.x, wing2Top.y, wing2Bottom.x, wing2Bottom.y, paint);

        if (markerData.getSelectedMarkerData() < 0)
            return;

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(FONT_SIZE);
        int scaleLength = toPixel((int) Math.sqrt(Math.pow(screenPos1.x - screenPos2.x, 2)
                + Math.pow(screenPos1.y - screenPos2.y, 2)));
        String text = "Scale length [pixel]: ";
        text += scaleLength;
        float marginPercent = 0.01f;
        PointF textPosition = new PointF(experimentRunView.getMaxRawX() * marginPercent,
                experimentRunView.getMaxRawY() - experimentRunView.getMaxRawX() * marginPercent);
        PointF screenTextPosition = new PointF();
        experimentRunView.toScreen(textPosition, screenTextPosition);
        screenTextPosition.y -= paint.ascent();

        // draw text background box
        Rect textBound = new Rect();
        textBound.left = (int)screenTextPosition.x;
        textBound.top = (int)screenTextPosition.y + (int)Math.ceil(paint.ascent());
        textBound.right = textBound.left + (int)Math.ceil(paint.measureText(text)) + 2;
        textBound.bottom = (int)screenTextPosition.y + (int)Math.ceil(paint.descent()) + 2;
        paint.setColor(Color.argb(150, 100, 100, 100));
        canvas.drawRect(textBound, paint);

        // draw text
        paint.setColor(Color.GREEN);
        canvas.drawText(text, screenTextPosition.x, screenTextPosition.y, paint);


    }

    private PointF getScreenPos(int markerIndex) {
        return markerList.get(markerIndex).getPosition();
    }
}
