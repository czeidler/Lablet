/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.Canvas;
import android.graphics.PointF;


public class CircleRenderer implements IPointRenderer {

    @Override
    public void drawPoint(Canvas canvas, PointF position, DrawConfig config) {
        canvas.drawCircle(position.x, position.y, config.getMarkerSize() / 2, config.getMarkerPaint());
    }
}
